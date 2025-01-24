//package stackpot.stackpot.web.controller;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import stackpot.stackpot.converter.UserConverter;
//import stackpot.stackpot.service.KakaoService;
//import stackpot.stackpot.web.dto.KakaoUserInfoResponseDto;
//
//import static com.mysql.cj.conf.PropertyKey.logger;
//
//@Slf4j
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("")
//public class KakaoLoginController {
//    private final KakaoService kakaoService;
//
//    @GetMapping("/users/oauth/kakao")
//    public ResponseEntity<KakaoUserInfoResponseDto> callback(@RequestParam("code") String code) {
//
//        log.info("Authorization code: {}", code); // 인증 코드 확인
//        String accessToken = kakaoService.getAccessTokenFromKakao(code);
//        KakaoUserInfoResponseDto userInfo = kakaoService.getUserInfo(accessToken);
//
//        String email = userInfo.getKakaoAccount().getEmail();// 이메일 가져오기
//        return ResponseEntity.ok(userInfo);
//    }
//}