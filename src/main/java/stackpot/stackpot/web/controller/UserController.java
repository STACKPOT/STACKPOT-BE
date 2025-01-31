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
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.config.security.JwtTokenProvider;
import stackpot.stackpot.converter.UserConverter;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.repository.UserRepository.UserRepository;
import stackpot.stackpot.service.KakaoService;
import stackpot.stackpot.service.UserCommandService;
import stackpot.stackpot.web.dto.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "User Management", description = "유저 관리 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserCommandService userCommandService;
    private final KakaoService kakaoService;
    @Operation(summary = "토큰 테스트 API")
    @GetMapping("/login/token")
    public ResponseEntity<String> testEndpoint(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }
        return ResponseEntity.ok("Authenticated user: " + authentication.getName());
    }

    @GetMapping("/oauth/kakao")
    @Operation(summary = "로그인 및 토큰발급 API")
    public ResponseEntity<ApiResponse<UserResponseDto.loginDto>> callback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {

        log.info("Authorization code: {}", code);
        String accessToken = kakaoService.getAccessTokenFromKakao(code);
        KakaoUserInfoResponseDto userInfo = kakaoService.getUserInfo(accessToken);

        String email = userInfo.getKakaoAccount().getEmail();

        UserResponseDto.loginDto userResponse = userCommandService.isnewUser(email);
        return ResponseEntity.ok(ApiResponse.onSuccess(userResponse));
    }

    @Operation(summary = "회원가입 API")
    @PatchMapping("/profile")
    public ResponseEntity<?> signup(@Valid @RequestBody UserRequestDto.JoinDto request,
                                    BindingResult bindingResult) {
        // 유효성 검사 실패 처리
        if (bindingResult.hasErrors()) {
            // 에러 메시지 수집
            List<String> errors = bindingResult.getAllErrors()
                    .stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }
        // 정상 처리
        User user = userCommandService.joinUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserConverter.toDto(user));
    }

    @Operation(summary = "[질문 수정 필요] 닉네임 생성 API")
    @GetMapping("/nickname")
    public ResponseEntity<ApiResponse<String>> nickname(){
        String nickName = userCommandService.createNickname();

        return ResponseEntity.ok(ApiResponse.onSuccess(nickName));
    }

    @Operation(summary = "나의 정보 조회 API")
    @GetMapping("")
    public ResponseEntity<ApiResponse<UserResponseDto.Userdto>> usersMyPages(){
        UserResponseDto.Userdto userDetails = userCommandService.getMyUsers();
        return ResponseEntity.ok(ApiResponse.onSuccess(userDetails));
    }

    @Operation(summary = "사용자별 정보 조회 API")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDto.Userdto>> usersPages(
            @PathVariable Long userId
    ){
        UserResponseDto.Userdto userDetails = userCommandService.getUsers(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(userDetails));
    }

    @Operation(summary = "나의 마이페이지 조회 API", description = "dataType = pot / feed / (null : pot + feed)")
    @GetMapping("/mypages")
    public ResponseEntity<ApiResponse<UserMypageResponseDto>> usersMypages(
            @RequestParam(required = false) String dataType){
        UserMypageResponseDto userDetails = userCommandService.getMypages(dataType);
        return ResponseEntity.ok(ApiResponse.onSuccess(userDetails));
    }


    @Operation(summary = "사용자별 마이페이지 조회 API", description = "dataType = pot / feed / (null : pot + feed)")
    @GetMapping("/{userId}/mypages")
    public ResponseEntity<ApiResponse<UserMypageResponseDto>> getUserMypage(
            @PathVariable Long userId,
            @RequestParam(required = false) String dataType) {
        UserMypageResponseDto response = userCommandService.getUserMypage(userId, dataType);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @PatchMapping("/profile/update")
    @Operation(summary = "나의 프로필 수정 API", description = "사용자의 역할, 관심사, 한 줄 소개를 수정합니다.")
    public ResponseEntity<ApiResponse<UserResponseDto.Userdto>> updateUserProfile(
            @RequestBody @Valid UserUpdateRequestDto requestDto) {

        UserResponseDto.Userdto updatedUser = userCommandService.updateUserProfile(requestDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(updatedUser));
    }


}
