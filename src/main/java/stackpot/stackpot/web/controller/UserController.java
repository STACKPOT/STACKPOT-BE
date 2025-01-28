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
    private final UserRepository userRepository;
    private final KakaoService kakaoService;
    private final JwtTokenProvider jwtTokenProvider;
    @Operation(summary = "토큰 테스트 API")
    @GetMapping("/login/token")
    public ResponseEntity<String> testEndpoint(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }
        return ResponseEntity.ok("Authenticated user: " + authentication.getName());
    }

//    @GetMapping("/oauth/kakao")
//    public ResponseEntity<TokenServiceResponse> callback(@RequestParam("code") String code) {
//
//        log.info("Authorization code: {}", code); // 인증 코드 확인
//        String accessToken = kakaoService.getAccessTokenFromKakao(code);
//        KakaoUserInfoResponseDto userInfo = kakaoService.getUserInfo(accessToken);
//
//        String email = userInfo.getKakaoAccount().getEmail();// 이메일 가져오기
//        log.info("userInfo.getEmail -> ", email);
//
//        User user = userCommandService.saveNewUser(email);
//
//        TokenServiceResponse token = jwtTokenProvider.createToken(user);
//        log.info("STACKPOT ACESSTOKEN : " + token.getAccessToken());
//
//
//        return ResponseEntity.ok(token);
//    }

    @GetMapping("/oauth/kakao")
    @Operation(summary = "토큰 조회 API")
    public void callback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {

        log.info("Authorization code: {}", code);
        String accessToken = kakaoService.getAccessTokenFromKakao(code);
        KakaoUserInfoResponseDto userInfo = kakaoService.getUserInfo(accessToken);

        String email = userInfo.getKakaoAccount().getEmail();
        log.info("userInfo.getEmail -> {}", email);

        User user = userCommandService.saveNewUser(email);

        TokenServiceResponse token = jwtTokenProvider.createToken(user);
        log.info("AccessToken: {}", token.getAccessToken());

        if (user.getId() == null) {
            // 미가입 유저: 회원가입 페이지로 리다이렉트 (토큰을 헤더로 추가)
            response.setHeader("Authorization", "Bearer " + token.getAccessToken());
            response.sendRedirect("http://localhost:5173/sign-up");
        } else {
            // 가입된 유저: 홈 페이지로 리다이렉트 (토큰을 헤더로 추가)
            response.setHeader("Authorization", "Bearer " + token.getAccessToken());
            response.sendRedirect("http://localhost:5173/callback");
        }
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

    @Operation(summary = "나의 마이페이지 조회 API")
    @GetMapping("/mypages")
    public ResponseEntity<ApiResponse<UserResponseDto>> usersMypages(){
        UserResponseDto userDetails = userCommandService.getMypages();
        return ResponseEntity.ok(ApiResponse.onSuccess(userDetails));
    }

    @Operation(summary = "다른 사람 마이페이지 조회 API", description = "dataType = pot / feed / (null : pot + feed)")
    @GetMapping("/{userId}/mypages")
    public ResponseEntity<ApiResponse<UserMypageResponseDto>> getUserMypage(
            @PathVariable Long userId,
            @RequestParam(required = false) String dataType) {
        UserMypageResponseDto response = userCommandService.getUserMypage(userId, dataType);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @PatchMapping("/profile/update")
    @Operation(summary = "나의 프로필 수정 API", description = "사용자의 역할, 관심사, 한 줄 소개를 수정합니다.")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUserProfile(
            @RequestBody @Valid UserUpdateRequestDto requestDto) {

        UserResponseDto updatedUser = userCommandService.updateUserProfile(requestDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(updatedUser));
    }


}
