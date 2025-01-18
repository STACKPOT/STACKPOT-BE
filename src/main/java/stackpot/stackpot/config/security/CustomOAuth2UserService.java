package stackpot.stackpot.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.repository.UserRepository.UserRepository;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//        OAuth2User oAuth2User = super.loadUser(userRequest);
//
//        // 사용자 정보 가져오기
//        Map<String, Object> attributes = oAuth2User.getAttributes();
//        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
//
//        if (kakaoAccount == null) {
//            throw new OAuth2AuthenticationException("Failed to retrieve kakao_account from attributes.");
//        }
//
//        // 이메일 가져오기
//        String email = (String) kakaoAccount.get("email");
//        if (email == null) {
//            throw new OAuth2AuthenticationException("Email not found in kakao_account.");
//        }
//        // 사용자 정보 조회 (DB에 저장 여부 확인)
//        userRepository.findByEmail(email).ifPresentOrElse(
//                user -> System.out.println("Existing user found: " + email),
//                () -> System.out.println("No user found. Redirecting to signup.")
//        );
//
//        Map<String, Object> modifiedAttributes = new HashMap<>(attributes);
//        modifiedAttributes.put("email", email); // 확실히 email 필드가 포함되도록 보장
//
//        System.out.println("modifiedAttributes : " + modifiedAttributes);
//
//        if (!modifiedAttributes.containsKey("email")) {
//            throw new IllegalStateException("Email is not present in modifiedAttributes!");
//        }
//        // 사용자 정보를 DefaultOAuth2User로 반환
//        return new DefaultOAuth2User(
//                oAuth2User.getAuthorities(),
//                attributes,
//                "email" // Principal로 사용할 필드
//        );
//    }
@Override
public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);

    // 사용자 정보 가져오기
    Map<String, Object> attributes = oAuth2User.getAttributes();
    Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

    if (kakaoAccount == null) {
        throw new OAuth2AuthenticationException("Failed to retrieve kakao_account from attributes.");
    }

    // 이메일 가져오기
    String email = (String) kakaoAccount.get("email");
    if (email == null) {
        throw new OAuth2AuthenticationException("Email not found in kakao_account.");
    }

    // 사용자 정보 확인
    userRepository.findByEmail(email).ifPresentOrElse(
            user -> System.out.println("Existing user found: " + email),
            () -> System.out.println("No user found. Redirecting to signup.")
    );

    // attributes에 email 추가
    Map<String, Object> modifiedAttributes = new HashMap<>(attributes);
    modifiedAttributes.put("email", email);

    // 디버깅: modifiedAttributes 확인
    System.out.println("Final Modified Attributes: " + modifiedAttributes);

    // DefaultOAuth2User 생성
    return new DefaultOAuth2User(
            oAuth2User.getAuthorities(),
            modifiedAttributes,
            "email" // Principal로 사용할 필드 이름
    );
}
}