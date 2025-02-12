package stackpot.stackpot.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.config.security.JwtTokenProvider;
import stackpot.stackpot.converter.UserConverter;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.enums.Role;
import stackpot.stackpot.repository.BlacklistRepository;
import stackpot.stackpot.repository.RefreshTokenRepository;
import stackpot.stackpot.service.KakaoService;
import stackpot.stackpot.service.UserCommandService;
import stackpot.stackpot.web.dto.*;

import java.io.IOException;

@Tag(name = "User Management", description = "유저 관리 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserCommandService userCommandService;
    private final KakaoService kakaoService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistRepository blacklistRepository;

    @Operation(summary = "토큰 테스트 API")
    @GetMapping("/login/token")
    public ResponseEntity<String> testEndpoint(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }
        return ResponseEntity.ok("Authenticated user: " + authentication.getName());
    }

    @GetMapping("/oauth/kakao")
    @Operation(summary = "로그인 및 토큰발급 API", description = "\"code\" 와 함께 요청시 기존/신규 유저 구분 및 Accesstoken을 발급합니다. isNewUser : false( DB 조회 확인 기존 유저 ), ture ( DB에 없음 신규 유저 )" )
    public ResponseEntity<ApiResponse<UserResponseDto.loginDto>> callback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {

        log.info("Authorization code : {}", code);
        String accessToken = kakaoService.getAccessTokenFromKakao(code);
        KakaoUserInfoResponseDto userInfo = kakaoService.getUserInfo(accessToken);

        String email = userInfo.getKakaoAccount().getEmail();

        UserResponseDto.loginDto userResponse = userCommandService.isnewUser(email);
        return ResponseEntity.ok(ApiResponse.onSuccess(userResponse));
    }

    @Operation(summary = "회원가입 API")
    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<UserSignUpResponseDto>> signup(@Valid @RequestBody UserRequestDto.JoinDto request) {
        UserSignUpResponseDto user = userCommandService.joinUser(request);
        return ResponseEntity.ok(ApiResponse.onSuccess(user));
    }

    @Operation(summary = "닉네임 생성 API")
    @GetMapping("/nickname")
    public ResponseEntity<ApiResponse<String>> nickname(@RequestParam Role role){
        String nickName = userCommandService.createNickname(role);
        return ResponseEntity.ok(ApiResponse.onSuccess(nickName));
    }

    @Operation(summary = "닉네임 저장 API", description = "사용자의 닉네임을 저장하고 회원가입을 완료합니다.")
    @PostMapping("/nickname/save")
    public ResponseEntity<ApiResponse<String>> saveNickname(@RequestParam String nickname) {
        String savedNickname = userCommandService.saveNickname(nickname);
        return ResponseEntity.ok(ApiResponse.onSuccess(savedNickname));
    }

    @PostMapping("/logout")
    @Operation(summary = "회원 로그아웃 API", description = "AccessToken 토큰과 함께 요청 시 로그아웃 ")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String accessToken, @RequestBody String refreshToken) {
        String response = userCommandService.logout(accessToken,refreshToken);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "[ 구현 중 ] 회원 탈퇴 API", description = "AccessToken 토큰과 함께 요청 시 회원 탈퇴 ")
    public ResponseEntity<ApiResponse<String>> deleteUser(@RequestHeader("Authorization") String accessToken, @RequestBody String refreshToken) {
        String response = userCommandService.deleteUser(accessToken, refreshToken);
        return ResponseEntity.ok(ApiResponse.onSuccess("회원 탈퇴 성공"));
    }


    @Operation(summary = "사용자별 정보 조회 API", description = "userId를 통해 '마이페이지'의 피드, 끓인 팟을 제외한 사용자 정보만을 제공하는 API입니다. 사용자의 Pot, FEED 조회와 조합해서 마이페이지를 제작하실 수 있습니다.")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDto.Userdto>> usersPages(
            @PathVariable Long userId
    ){
        UserResponseDto.Userdto userDetails = userCommandService.getUsers(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(userDetails));
    }

    @Operation(summary = "나의 정보 조회 API", description = "토큰을 통해 '설정 페이지'와 '마이페이지'의 피드, 끓인 팟을 제외한 사용자 자신의 정보만을 제공하는 API입니다. 사용자의 Pot, FEED 조회와 조합해서 마이페이지를 제작하실 수 있습니다.")
    @GetMapping("")
    public ResponseEntity<ApiResponse<UserResponseDto.Userdto>> usersMyPages(){
        UserResponseDto.Userdto userDetails = userCommandService.getMyUsers();
        return ResponseEntity.ok(ApiResponse.onSuccess(userDetails));
    }

    @Operation(summary = "나의 마이페이지 조회 API", description = "토큰을 통해 자신의 [정보 조회 API + 피드 + 끓인 팟] 모두를 제공하는 API로 마이페이지 전체의 정보를 제공하는 API입니다. dataType = pot / feed / (null : pot + feed)")
    @GetMapping("/mypages")
    public ResponseEntity<ApiResponse<UserMyPageResponseDto>> usersMypages(
            @RequestParam(required = false) String dataType){
        UserMyPageResponseDto userDetails = userCommandService.getMypages(dataType);
        return ResponseEntity.ok(ApiResponse.onSuccess(userDetails));
    }


    @Operation(summary = "사용자별 마이페이지 조회 API", description = "userId를 통해 사용자의 [정보 조회 API + 피드 + 끓인 팟] 모두를 제공하는 API로 마이페이지 전체의 정보를 제공하는 API입니다. dataType = pot / feed / (null : pot + feed)")
    @GetMapping("/{userId}/mypages")
    public ResponseEntity<ApiResponse<UserMyPageResponseDto>> getUserMypage(
            @PathVariable Long userId,
            @RequestParam(required = false) String dataType) {
        UserMyPageResponseDto response = userCommandService.getUserMypage(userId, dataType);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @PatchMapping("/profile/update")
    @Operation(summary = "나의 프로필 수정 API", description = "사용자의 역할, 관심사, 한 줄 소개, 카카오 아이디를 수정합니다.")
    public ResponseEntity<ApiResponse<UserResponseDto.Userdto>> updateUserProfile(
            @RequestBody UserUpdateRequestDto requestDto) {

        UserResponseDto.Userdto updatedUser = userCommandService.updateUserProfile(requestDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(updatedUser));
    }


}
