package stackpot.stackpot.web.controller;

import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    @Operation(summary = "토큰 test api")
    @GetMapping("/login/token")
    public ResponseEntity<String> testEndpoint(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }
        return ResponseEntity.ok("Authenticated user: " + authentication.getName());
    }

    @GetMapping("/oauth/kakao")
    public ResponseEntity<TokenServiceResponse> callback(@RequestParam("code") String code) {

        log.info("Authorization code: {}", code); // 인증 코드 확인
        String accessToken = kakaoService.getAccessTokenFromKakao(code);
        KakaoUserInfoResponseDto userInfo = kakaoService.getUserInfo(accessToken);

        String email = userInfo.getKakaoAccount().getEmail();// 이메일 가져오기
        log.info("userInfo.getEmail -> ", email);

        User user = userCommandService.saveNewUser(email);

        TokenServiceResponse token = jwtTokenProvider.createToken(user);
        log.info("STACKPOT ACESSTOKEN : " + token.getAccessToken());


        return ResponseEntity.ok(token);
    }

    @Operation(summary = "회원가입 api")
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

    @Operation(summary = "닉네임 생성 [질문 수정 필요]")
    @GetMapping("/nickname")
    public ResponseEntity<ApiResponse<String>> nickname(){
        String nickName = userCommandService.createNickname();


        return ResponseEntity.ok(ApiResponse.onSuccess(nickName));
    }

    @Operation(summary = "마이페이지 사용자 정보 조회 API")
    @GetMapping("/mypages")
    public ResponseEntity<ApiResponse<UserResponseDto>> usersMypages(){
        UserResponseDto userDetails = userCommandService.getMypages();
        return ResponseEntity.ok(ApiResponse.onSuccess(userDetails));
    }

    @Operation(summary = "다른 사람 마이페이지(프로필) 조회 API")
    @GetMapping("/{userId}/mypages")
    public ResponseEntity<ApiResponse<UserMypageResponseDto>> getUserMypage(@PathVariable Long userId) {
        UserMypageResponseDto response = userCommandService.getUserMypage(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @PatchMapping("/profile/update")
    @Operation(summary = "사용자 프로필 수정 API", description = "사용자의 역할, 관심사, 한 줄 소개를 수정합니다.")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUserProfile(
            @RequestBody @Valid UserUpdateRequestDto requestDto) {

        UserResponseDto updatedUser = userCommandService.updateUserProfile(requestDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(updatedUser));
    }


}
