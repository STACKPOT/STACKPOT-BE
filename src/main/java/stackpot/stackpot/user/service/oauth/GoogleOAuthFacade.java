package stackpot.stackpot.user.service.oauth;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import stackpot.stackpot.user.dto.response.GoogleUserInfoResponseDto;
import stackpot.stackpot.user.dto.response.UserResponseDto;
import stackpot.stackpot.user.entity.enums.Provider;
import stackpot.stackpot.user.service.UserCommandService;

@Service
@RequiredArgsConstructor
public class GoogleOAuthFacade {

	// 프론트 Origin(요청 출처) 기준으로 분기
	private static final String LOCAL_FRONT_ORIGIN = "http://localhost:5173";
	private static final String PROD_FRONT_ORIGIN  = "https://stackpot.co.kr";

	// 구글 콘솔에 등록된 redirect_uri (프론트 콜백 라우트)
	private static final String LOCAL_FRONT_CALLBACK = "http://localhost:5173/callback/google";
	private static final String PROD_FRONT_CALLBACK  = "https://stackpot.co.kr/callback/google";

	private final HttpServletRequest request;
	private final GoogleService googleService;
	private final UserCommandService userCommandService;

	public UserResponseDto.loginDto login(String code) {
		String redirectUri = resolveRedirectUriFromRequest();

		String googleAccessToken = googleService.getAccessTokenFromGoogle(code, redirectUri);
		GoogleUserInfoResponseDto userInfo = googleService.getUserInfo(googleAccessToken);

		String providerId = userInfo.getId();
		String email = userInfo.getEmail();

		return userCommandService.isnewUser(Provider.GOOGLE, providerId, email);
	}

	private String resolveRedirectUriFromRequest() {
		String origin = request.getHeader("Origin");
		if (origin != null) {
			if (origin.startsWith(LOCAL_FRONT_ORIGIN)) return LOCAL_FRONT_CALLBACK;
			if (origin.startsWith(PROD_FRONT_ORIGIN))  return PROD_FRONT_CALLBACK;
		}

		String referer = request.getHeader("Referer");
		if (referer != null) {
			if (referer.startsWith(LOCAL_FRONT_ORIGIN)) return LOCAL_FRONT_CALLBACK;
			if (referer.startsWith(PROD_FRONT_ORIGIN))  return PROD_FRONT_CALLBACK;
		}

		// Swagger 등에서 직접 호출하면 Origin/Referer가 없을 수 있음
		// 정책: 운영 기본값
		return PROD_FRONT_CALLBACK;
	}
}