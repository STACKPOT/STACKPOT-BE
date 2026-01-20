package stackpot.stackpot.user.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.common.swagger.ApiErrorCodeExamples;
import stackpot.stackpot.feed.service.FeedQueryService;
import stackpot.stackpot.pot.dto.AppealContentDto;
import stackpot.stackpot.pot.dto.CompletedPotRequestDto;
import stackpot.stackpot.pot.dto.OngoingPotResponseDto;
import stackpot.stackpot.pot.dto.PotResponseDto;
import stackpot.stackpot.pot.dto.PotSummaryDto;
import stackpot.stackpot.pot.service.pot.MyPotService;
import stackpot.stackpot.pot.service.pot.PotCommandService;
import stackpot.stackpot.user.dto.request.MyDescriptionRequestDto;
import stackpot.stackpot.user.dto.request.TokenRequestDto;
import stackpot.stackpot.user.dto.request.UserRequestDto;
import stackpot.stackpot.user.dto.request.UserUpdateRequestDto;
import stackpot.stackpot.user.dto.response.KakaoUserInfoResponseDto;
import stackpot.stackpot.user.dto.response.MyDescriptionResponseDto;
import stackpot.stackpot.user.dto.response.NaverUserInfoResponseDto;
import stackpot.stackpot.user.dto.response.NicknameResponseDto;
import stackpot.stackpot.user.dto.response.TokenServiceResponse;
import stackpot.stackpot.user.dto.response.UserMyPageResponseDto;
import stackpot.stackpot.user.dto.response.UserResponseDto;
import stackpot.stackpot.user.dto.response.UserSignUpResponseDto;
import stackpot.stackpot.user.entity.enums.Provider;
import stackpot.stackpot.user.service.UserCommandService;
import stackpot.stackpot.user.service.UserQueryService;
import stackpot.stackpot.user.service.oauth.GoogleOAuthFacade;
import stackpot.stackpot.user.service.oauth.GoogleService;
import stackpot.stackpot.user.service.oauth.KakaoService;
import stackpot.stackpot.user.service.oauth.NaverService;

@Tag(name = "User or MyPage Management", description = "유저 및 마이페이지 관리 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

	private final UserCommandService userCommandService;
	private final KakaoService kakaoService;
	private final NaverService naverService;
	private final GoogleOAuthFacade googleOAuthFacade;
	private final MyPotService myPotService;
	private final PotCommandService potCommandService;
	private final UserQueryService userQueryService;
	private final FeedQueryService feedQueryService;

	@GetMapping("/login/token")
	@Operation(
		summary = "토큰 테스트 API",
		description = "현재 로그인 된 사용자의 토큰을 테스트 하는 api입니다.",
		responses = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
				responseCode = "200",
				description = "유요한 토큰",
				content = @Content(mediaType = "application/json")
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
				responseCode = "401",
				description = "유요하지 않은 토큰",
				content = @Content(mediaType = "application/json")
			)
		}
	)
	public ResponseEntity<String> testEndpoint(Authentication authentication) {
		if (authentication == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
		}
		return ResponseEntity.ok("Authenticated user: " + authentication.getName());
	}

	@GetMapping("/oauth/kakao")
	@Operation(
		summary = "카카오 로그인 및 토큰발급 API",
		description = "\"code\" 와 함께 요청시 기존/신규 유저 구분 및 accessToken을 발급합니다. 이떄 발급된 AccessToken은 회원가입 관련 엔드포인트만 접급 가능합니다.\nisNewUser : false( DB 조회 확인 기존 유저 ), ture ( DB에 없음 신규 유저 )",
		responses = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
				responseCode = "200",
				description = "성공적으로 토큰 발급",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.loginDto.class))
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
				responseCode = "400",
				description = "Invalid Parameter",
				content = @Content(mediaType = "application/json")
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
				responseCode = "500",
				description = "Internal Server Error",
				content = @Content(mediaType = "application/json")
			)
		}
	)
	public ResponseEntity<ApiResponse<UserResponseDto.loginDto>> kakaoCallback(@RequestParam("code") String code,
		HttpServletResponse response) throws IOException {
		String accessToken = kakaoService.getAccessTokenFromKakao(code);
		KakaoUserInfoResponseDto userInfo = kakaoService.getUserInfo(accessToken);

		String providerId = String.valueOf(userInfo.getId());
		String email = userInfo.getKakaoAccount().getEmail();

		UserResponseDto.loginDto userResponse = userCommandService.isnewUser(Provider.KAKAO, providerId, email);
		return ResponseEntity.ok(ApiResponse.onSuccess(userResponse));
	}

	@GetMapping("/oauth/naver")
	@Operation(
		summary = "네이버 로그인 및 토큰발급 API",
		description = "\"code\" 와 함께 요청시 기존/신규 유저 구분 및 accessToken을 발급합니다. 이떄 발급된 AccessToken은 회원가입 관련 엔드포인트만 접급 가능합니다.\nisNewUser : false( DB 조회 확인 기존 유저 ), ture ( DB에 없음 신규 유저 )",
		responses = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
				responseCode = "200",
				description = "성공적으로 토큰 발급",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.loginDto.class))
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
				responseCode = "400",
				description = "Invalid Parameter",
				content = @Content(mediaType = "application/json")
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
				responseCode = "500",
				description = "Internal Server Error",
				content = @Content(mediaType = "application/json")
			)
		}
	)
	public ResponseEntity<ApiResponse<UserResponseDto.loginDto>> naverCallback(@RequestParam("code") String code,
		HttpServletResponse response) throws IOException {
		String accessToken = naverService.getAccessTokenFromNaver(code);
		NaverUserInfoResponseDto userInfo = naverService.getUserInfo(accessToken);

		String providerId = userInfo.getResponse().id();
		String email = userInfo.getResponse().email();

		UserResponseDto.loginDto userResponse = userCommandService.isnewUser(Provider.NAVER, providerId, email);
		return ResponseEntity.ok(ApiResponse.onSuccess(userResponse));
	}

	@GetMapping("/oauth/google")
	@Operation(
		summary = "구글 로그인 및 토큰발급 API",
		description = "\"code\" 와 함께 요청시 기존/신규 유저 구분 및 accessToken을 발급합니다. 이떄 발급된 AccessToken은 회원가입 관련 엔드포인트만 접급 가능합니다.\nisNewUser : false( DB 조회 확인 기존 유저 ), ture ( DB에 없음 신규 유저 )",
		responses = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
				responseCode = "200",
				description = "성공적으로 토큰 발급",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.loginDto.class))
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
				responseCode = "400",
				description = "Invalid Parameter",
				content = @Content(mediaType = "application/json")
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
				responseCode = "500",
				description = "Internal Server Error",
				content = @Content(mediaType = "application/json")
			)
		}
	)
	public ResponseEntity<ApiResponse<UserResponseDto.loginDto>> googleCallback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
		UserResponseDto.loginDto userResponse = googleOAuthFacade.login(code);
		return ResponseEntity.ok(ApiResponse.onSuccess(userResponse));
	}

	@PatchMapping("/profile")
	@Operation(
		summary = "회원가입 API",
		description = "신규 User 회원가입 시 필요한 정보를 저장합니다.\n" +
			"- interests: 다중 선택 가능하며 string입니다. [사이드 프로젝트, 1인 개발, 공모전, 창업, 네트워킹 행사]\n"
	)
	public ResponseEntity<ApiResponse<UserSignUpResponseDto>> signup(
		@Valid @RequestBody UserRequestDto.JoinDto request) {
		UserSignUpResponseDto user = userCommandService.joinUser(request);
		return ResponseEntity.ok(ApiResponse.onSuccess(user));
	}

	@GetMapping("/nickname")
	@Operation(
		summary = "닉네임 생성 API",
		description = "새싹 관련 닉네임이 생성됩니다. 기존 유저와 중복되지 않는 닉네임이 생성됩니다."
	)
	public ResponseEntity<ApiResponse<NicknameResponseDto>> nickname() {
		NicknameResponseDto nickName = userCommandService.createNickname();
		return ResponseEntity.ok(ApiResponse.onSuccess(nickName));
	}

	@PostMapping("/nickname/save")
	@Operation(
		summary = "닉네임 저장 및 회원가입 완료 API",
		description = "사용자의 닉네임을 저장하고 회원가입을 완료합니다. accessToken과 refreshToken도 함께 반환합니다."
	)
	@ApiErrorCodeExamples({
		ErrorStatus.USER_NOT_FOUND
	})
	public ResponseEntity<ApiResponse<TokenServiceResponse>> saveNickname(@RequestParam("nickname") String nickname) {
		TokenServiceResponse tokenServiceResponse = userCommandService.saveNickname(nickname);
		return ResponseEntity.ok(ApiResponse.onSuccess(tokenServiceResponse));
	}

	@PostMapping("/logout")
	@Operation(
		summary = "회원 로그아웃 API",
		description = "AccessToken 토큰과 함께 요청 시 로그아웃"
	)
	@ApiErrorCodeExamples({
		ErrorStatus.INVALID_AUTH_TOKEN,
		ErrorStatus.USER_NOT_FOUND,
		ErrorStatus.REDIS_KEY_NOT_FOUND,
		ErrorStatus.REDIS_BLACKLIST_SAVE_FAILED
	})
	public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String accessToken,
		@RequestBody TokenRequestDto refreshToken) {
		String response = userCommandService.logout(accessToken, refreshToken.getRefreshToken());
		return ResponseEntity.ok(ApiResponse.onSuccess(response));
	}

	@DeleteMapping("/delete")
	@Operation(
		summary = "회원 탈퇴 API",
		description = "AccessToken 토큰과 함께 요청 시 회원 탈퇴 " +
			"-pot 생성자인 경우 softDelet\n" +
			"-pot 생성자가 아닌 경우 hardDelet"
	)
	@ApiErrorCodeExamples({
		ErrorStatus.USER_NOT_FOUND,
		ErrorStatus.REDIS_BLACKLIST_SAVE_FAILED,
		ErrorStatus.USER_WITHDRAWAL_FAILED
	})
	public ResponseEntity<ApiResponse<String>> deleteUser(@RequestHeader("Authorization") String accessToken) {
		String response = userCommandService.deleteUser(accessToken);
		return ResponseEntity.ok(ApiResponse.onSuccess(response));
	}

	@GetMapping("/{userId}")
	@Operation(
		summary = "사용자별 정보 조회 API",
		description = "userId를 통해 '마이페이지'의 피드, 끓인 팟을 제외한 사용자 정보만을 제공하는 API입니다. 사용자의 Pot, FEED 조회와 조합해서 마이페이지를 제작하실 수 있습니다."
	)
	@ApiErrorCodeExamples({
		ErrorStatus.USER_ALREADY_WITHDRAWN,
		ErrorStatus.USER_NOT_FOUND
	})
	public ResponseEntity<ApiResponse<UserResponseDto.UserInfoDto>> usersPages(
		@PathVariable(name = "userId") Long userId) {
		UserResponseDto.UserInfoDto userDetails = userCommandService.getUsers(userId);
		return ResponseEntity.ok(ApiResponse.onSuccess(userDetails));
	}

	@GetMapping("")
	@Operation(
		summary = "나의 정보 조회 API",
		description = "토큰을 통해 '설정 페이지'와 '마이페이지'의 피드, 끓인 팟을 제외한 사용자 자신의 정보만을 제공하는 API입니다. 사용자의 Pot, FEED 조회와 조합해서 마이페이지를 제작하실 수 있습니다."
	)
	@ApiErrorCodeExamples({
		ErrorStatus.USER_NOT_FOUND,
		ErrorStatus.USER_ALREADY_WITHDRAWN,
	})
	public ResponseEntity<ApiResponse<UserResponseDto.UserInfoDto>> usersMyPages() {
		UserResponseDto.UserInfoDto userDetails = userCommandService.getMyUsers();
		return ResponseEntity.ok(ApiResponse.onSuccess(userDetails));
	}

	@PatchMapping("/profile/update")
	@Operation(
		summary = "나의 프로필 수정 API",
		description = "사용자의 역할, 관심사, 한 줄 소개, 카카오 아이디를 수정합니다.\n" +
			"- interests: 다중 선택 가능하며 string입니다. [사이드 프로젝트, 1인 개발, 공모전, 창업, 네트워킹 행사]\n"
	)
	@ApiErrorCodeExamples({
		ErrorStatus.USER_NOT_FOUND
	})
	public ResponseEntity<ApiResponse<UserResponseDto.Userdto>> updateUserProfile(
		@RequestBody UserUpdateRequestDto requestDto) {
		UserResponseDto.Userdto updatedUser = userCommandService.updateUserProfile(requestDto);
		return ResponseEntity.ok(ApiResponse.onSuccess(updatedUser));
	}

	@GetMapping("/pots")
	@Operation(summary = "마이페이지 팟 조회 API", description = "나의 모든 팟(모집중 / 진행중 / 완료)을 조회합니다. status = all / recruiting / ongoing / completed")
	public ResponseEntity<ApiResponse<List<OngoingPotResponseDto>>> getMyAllInvolvedPots(
		@RequestParam(name = "potStatus", required = false) String dataType) {
		List<OngoingPotResponseDto> pots = myPotService.getMyAllInvolvedPots(dataType);
		return ResponseEntity.ok(ApiResponse.onSuccess(pots));
	}

	@GetMapping("/pots/{user_id}")
	@Operation(summary = "사용자별 마이페이지 팟 조회 API", description = "나의 모든 팟(모집중 / 진행중 / 완료)을 조회합니다. status = all / recruiting / ongoing / completed")
	public ResponseEntity<ApiResponse<List<OngoingPotResponseDto>>> getUserAllInvolvedPots(
		@PathVariable("user_id") Long user_id, @RequestParam(name = "potStatus", required = false) String dataType) {
		List<OngoingPotResponseDto> pots = myPotService.getUserAllInvolvedPots(user_id, dataType);
		return ResponseEntity.ok(ApiResponse.onSuccess(pots));
	}

	@GetMapping("/potAppealContent/{pot_id}")
	@Operation(
		summary = "마이페이지 '여기서 저는요' 모달 조회 API",
		description = "'끓인 팟 상세보기 모달'에 쓰이는 Role, Badge, Appeal Content를 반환합니다."
	)
	@ApiErrorCodeExamples({
		ErrorStatus.USER_NOT_FOUND,
	})
	public ResponseEntity<ApiResponse<AppealContentDto>> getAppealContent(
		@PathVariable(name = "pot_id") Long potId) {
		AppealContentDto response = myPotService.getAppealContent(potId);
		return ResponseEntity.ok(ApiResponse.onSuccess(response));
	}

	@GetMapping("/potAppealContent/{pot_id}/{user_id}")
	@Operation(
		summary = "다른 사람 마이페이지 '여기서 저는요' 모달 조회 API",
		description = "'끓인 팟 상세보기 모달'에 쓰이는 Role, Badge, Appeal Content를 반환합니다."
	)
	@ApiErrorCodeExamples({
		ErrorStatus.USER_NOT_FOUND,
	})
	public ResponseEntity<ApiResponse<AppealContentDto>> getAppealContent(
		@PathVariable(name = "pot_id") Long potId,
		@PathVariable(name = "user_id") Long userId) {
		AppealContentDto response = myPotService.getUserAppealContent(potId, userId);
		return ResponseEntity.ok(ApiResponse.onSuccess(response));
	}

	@GetMapping("/potSummary/{pot_id}")
	@Operation(
		summary = "끓인 팟 AI 요약 모달 조회 API",
		description = "끓인 팟을 상세보기할 때 쓰이는 PotSummary, potLan을 반환합니다."
	)
	@ApiErrorCodeExamples({
		ErrorStatus.POT_NOT_FOUND
	})
	public ResponseEntity<ApiResponse<PotSummaryDto>> getPotSummary(
		@PathVariable(name = "pot_id") Long potId) {
		PotSummaryDto response = myPotService.getPotSummary(potId);
		return ResponseEntity.ok(ApiResponse.onSuccess(response));
	}

	@PatchMapping("/{pot_id}")
	@Operation(
		summary = "끓인 팟 수정하기 API",
		description = "끓은 팟의 정보를 수정하는 api 입니다."
	)
	@ApiErrorCodeExamples({
		ErrorStatus.POT_NOT_FOUND,
		ErrorStatus.POT_FORBIDDEN,
	})
	public ResponseEntity<ApiResponse<PotResponseDto>> updatePot(
		@PathVariable(name = "pot_id") Long potId,
		@RequestBody @Valid CompletedPotRequestDto requestDto) {
		// 팟 수정 로직 호출
		PotResponseDto responseDto = potCommandService.updateCompletedPot(potId, requestDto);
		return ResponseEntity.ok(ApiResponse.onSuccess(responseDto)); // 수정된 팟 정보 반환
	}

	@GetMapping("/description")
	@Operation(
		summary = "나의 소개 조회 API",
		description = "로그인한 사용자의 소개를 조회합니다."
	)
	@ApiErrorCodeExamples({
		ErrorStatus.USER_NOT_FOUND
	})
	public ResponseEntity<ApiResponse<MyDescriptionResponseDto>> getMyDescription() {
		MyDescriptionResponseDto responseDto = userQueryService.getMyDescription();
		return ResponseEntity.ok(ApiResponse.onSuccess(responseDto));
	}

	@GetMapping("/description/{userId}")
	@Operation(
		summary = "특정 사용자의 소개 조회 API",
		description = "특정 사용자의 소개를 조회합니다."
	)
	@ApiErrorCodeExamples({
		ErrorStatus.USER_NOT_FOUND
	})
	public ResponseEntity<ApiResponse<MyDescriptionResponseDto>> getUserDescription(@PathVariable Long userId) {

		MyDescriptionResponseDto responseDto = userQueryService.getUserDescription(userId);
		return ResponseEntity.ok(ApiResponse.onSuccess(responseDto));
	}

	@PatchMapping("/description")
	@Operation(
		summary = "나의 소개 수정 또는 추가 API",
		description = "로그인한 사용자의 소개를 수정하거나 추가합니다."
	)
	@ApiErrorCodeExamples({
		ErrorStatus.USER_NOT_FOUND,

	})
	public ResponseEntity<ApiResponse<MyDescriptionResponseDto>> upsertMyDescription(
		@RequestBody @Valid MyDescriptionRequestDto dto) {
		MyDescriptionResponseDto responseDto = userCommandService.upsertDescription(dto);
		return ResponseEntity.ok(ApiResponse.onSuccess(responseDto));
	}

	@DeleteMapping("/description")
	@Operation(
		summary = "나의 소개 삭제 API",
		description = "로그인한 사용자의 소개를 삭제합니다."
	)
	@ApiErrorCodeExamples({
		ErrorStatus.USER_NOT_FOUND
	})
	public ResponseEntity<ApiResponse<Void>> deleteMyDescription() {
		userCommandService.deleteDescription();
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{userId}/feeds")
	@Operation(
		summary = "사용자별 피드 조회 API",
		description = "userId에 해당하는 사용자의 시리즈 코멘트와 피드를 반환합니다. 피드는 커서 기반 페이지네이션을 지원합니다. \n" +
			"시리즈가 '전체보기' 일 때는 seriesId = 0"
	)
	@ApiErrorCodeExamples({
		ErrorStatus.USER_NOT_FOUND,
		ErrorStatus.USER_ALREADY_WITHDRAWN
	})
	public ResponseEntity<ApiResponse<UserMyPageResponseDto>> getFeedsByUserId(
		@Parameter(description = "사용자 ID", example = "1")
		@PathVariable("userId") Long userId,

		@Parameter(description = "커서", example = "100", required = false)
		@RequestParam(value = "cursor", required = false) Long cursor,

		@Parameter(description = "페이지 크기", example = "10")
		@RequestParam(value = "size", defaultValue = "10") int size,

		@RequestParam(value = "seriesId", required = false, defaultValue = "0") Long seriesId
	) {
		UserMyPageResponseDto mypage = feedQueryService.getFeedsByUserId(userId, cursor, size, seriesId);
		return ResponseEntity.ok(ApiResponse.onSuccess(mypage));
	}

	@Operation(
		summary = "나의 피드 조회 API",
		description = "로그인한 사용자의 시리즈 코멘트와 피드를 반환합니다. 피드는 커서 기반 페이지네이션을 지원합니다. \n" +
			"시리즈가 '전체보기' 일 때는 seriesId = 0"
	)
	@GetMapping("/feeds")
	@ApiErrorCodeExamples({
		ErrorStatus.USER_NOT_FOUND,
		ErrorStatus.USER_ALREADY_WITHDRAWN
	})
	public ResponseEntity<ApiResponse<UserMyPageResponseDto>> getFeeds(
		@RequestParam(name = "cursor", required = false) Long cursor,
		@RequestParam(name = "size", defaultValue = "10") int size,
		@RequestParam(value = "seriesId", required = false, defaultValue = "0") Long seriesId
	) {
		UserMyPageResponseDto mypage = feedQueryService.getFeeds(cursor, size, seriesId);
		return ResponseEntity.ok(ApiResponse.onSuccess(mypage));
	}

}
