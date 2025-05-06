package stackpot.stackpot.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import stackpot.stackpot.user.dto.request.TokenRequestDto;
import stackpot.stackpot.user.dto.request.UserRequestDto;
import stackpot.stackpot.user.dto.request.UserUpdateRequestDto;
import stackpot.stackpot.user.dto.response.*;
import stackpot.stackpot.user.entity.enums.Role;
import stackpot.stackpot.pot.dto.CompletedPotDetailResponseDto;
import stackpot.stackpot.pot.dto.CompletedPotRequestDto;
import stackpot.stackpot.pot.dto.PotResponseDto;
import stackpot.stackpot.user.service.KakaoService;
import stackpot.stackpot.pot.service.MyPotService;
import stackpot.stackpot.pot.service.PotService;
import stackpot.stackpot.user.service.UserCommandService;

import java.io.IOException;

@Tag(name = "User Management", description = "유저 관리 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserCommandService userCommandService;
    private final KakaoService kakaoService;
    private final MyPotService myPotService;
    private final PotService potService;


    @GetMapping("/login/token")
    @Operation(
            summary = "토큰 테스트 API",
            description = "현재 로그인 된 사용자의 토큰을 테스트 하는 api입니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "유요한 토큰",
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
            summary = "로그인 및 토큰발급 API",
            description = "\"code\" 와 함께 요청시 기존/신규 유저 구분 및 Accesstoken을 발급합니다. isNewUser : false( DB 조회 확인 기존 유저 ), ture ( DB에 없음 신규 유저 )",
            responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공적으로 토큰 발급",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.loginDto.class))
            )
        }
    )
    public ResponseEntity<ApiResponse<UserResponseDto.loginDto>> callback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {

        log.info("Authorization code : {}", code);
        String accessToken = kakaoService.getAccessTokenFromKakao(code);
        KakaoUserInfoResponseDto userInfo = kakaoService.getUserInfo(accessToken);

        String email = userInfo.getKakaoAccount().getEmail();

        UserResponseDto.loginDto userResponse = userCommandService.isnewUser(email);
        return ResponseEntity.ok(ApiResponse.onSuccess(userResponse));
    }

    @PatchMapping("/profile")
    @Operation(
            summary = "회원가입 API",
            description = "신규 User 회원가입 시 필요한 정보를 저장합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "유저 정보 저장 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserSignUpResponseDto.class))
            )
            }
    )
    public ResponseEntity<ApiResponse<UserSignUpResponseDto>> signup(@Valid @RequestBody UserRequestDto.JoinDto request) {
        UserSignUpResponseDto user = userCommandService.joinUser(request);
        return ResponseEntity.ok(ApiResponse.onSuccess(user));
    }

    @GetMapping("/nickname")
    @Operation(
            summary = "닉네임 생성 API",
            description = "닉네임 생성 시 역할별로 닉네임이 생성됩니다. 기존 유저와 중복되지 않는 닉네임이 생성됩니다.",
            responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "닉네임 생성 성공",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = NicknameResponseDto.class))
                )
            }
    )
    public ResponseEntity<ApiResponse<NicknameResponseDto>> nickname(@RequestParam("role") Role role){
        NicknameResponseDto nickName = userCommandService.createNickname(role);
        return ResponseEntity.ok(ApiResponse.onSuccess(nickName));
    }

    @PostMapping("/nickname/save")
    @Operation(
            summary = "닉네임 저장 API",
            description = "사용자의 닉네임을 저장하고 회원가입을 완료합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "신규 유저 생성 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))
                    )
            }
    )
    public ResponseEntity<ApiResponse<String>> saveNickname(@RequestParam("nickname") String nickname) {
        String savedNickname = userCommandService.saveNickname(nickname);
        return ResponseEntity.ok(ApiResponse.onSuccess(savedNickname));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "회원 로그아웃 API",
            description = "AccessToken 토큰과 함께 요청 시 로그아웃",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "로그아웃 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))
                    )
            }
    )
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String accessToken, @RequestBody TokenRequestDto refreshToken) {
        String response = userCommandService.logout(accessToken,refreshToken.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @DeleteMapping("/delete")
    @Operation(
            summary = "회원 탈퇴 API",
            description = "AccessToken 토큰과 함께 요청 시 회원 탈퇴 "+
            "-pot 생성자인 경우 softDelet\n"+
            "-pot 생성자가 아닌 경우 hardDelet",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "회읜탈퇴 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))
                    )
            }

    )
    public ResponseEntity<ApiResponse<String>> deleteUser(@RequestHeader("Authorization") String accessToken) {
        String response = userCommandService.deleteUser(accessToken);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @GetMapping("/{userId}")
    @Operation(
            summary = "사용자별 정보 조회 API",
            description = "userId를 통해 '마이페이지'의 피드, 끓인 팟을 제외한 사용자 정보만을 제공하는 API입니다. 사용자의 Pot, FEED 조회와 조합해서 마이페이지를 제작하실 수 있습니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "유저 정보 조회 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.Userdto.class))
                    )
            }
    )
    public ResponseEntity<ApiResponse<UserResponseDto.Userdto>> usersPages(
            @PathVariable(name = "userId") Long userId){
        UserResponseDto.Userdto userDetails = userCommandService.getUsers(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(userDetails));
    }

    @GetMapping("")
    @Operation(
            summary = "나의 정보 조회 API",
            description = "토큰을 통해 '설정 페이지'와 '마이페이지'의 피드, 끓인 팟을 제외한 사용자 자신의 정보만을 제공하는 API입니다. 사용자의 Pot, FEED 조회와 조합해서 마이페이지를 제작하실 수 있습니다.",
            responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "사용자 정보 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.Userdto.class))
            )
    }
    )
    public ResponseEntity<ApiResponse<UserResponseDto.Userdto>> usersMyPages(){
        UserResponseDto.Userdto userDetails = userCommandService.getMyUsers();
        return ResponseEntity.ok(ApiResponse.onSuccess(userDetails));
    }

    @GetMapping("/mypages")
    @Operation(
            summary = "나의 마이페이지 조회 API",
            description = "토큰을 통해 자신의 [정보 조회 API + 피드 + 끓인 팟] 모두를 제공하는 API로 마이페이지 전체의 정보를 제공하는 API입니다. dataType = pot / feed / (null : pot + feed)",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "나의 페이지 조회 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserMyPageResponseDto.class))
                    )
            }
    )
    public ResponseEntity<ApiResponse<UserMyPageResponseDto>> usersMypages(
            @RequestParam(name = "dataType", required = false) String dataType){
        UserMyPageResponseDto userDetails = userCommandService.getMypages(dataType);
        return ResponseEntity.ok(ApiResponse.onSuccess(userDetails));
    }


    @GetMapping("/{userId}/mypages")
    @Operation(
            summary = "사용자별 마이페이지 조회 API",
            description = "userId를 통해 사용자의 [정보 조회 API + 피드 + 끓인 팟] 모두를 제공하는 API로 마이페이지 전체의 정보를 제공하는 API입니다.\n"+"dataType = pot / feed / (null : pot + feed)",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "사용자별 myPage 조회 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserMyPageResponseDto.class))
                    )
            }
    )
    public ResponseEntity<ApiResponse<UserMyPageResponseDto>> getUserMypage(
            @PathVariable("userId") Long userId,
            @RequestParam(name = "dataType", required = false) String dataType) {
        UserMyPageResponseDto response = userCommandService.getUserMypage(userId, dataType);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @PatchMapping("/profile/update")
    @Operation(
            summary = "나의 프로필 수정 API",
            description = "사용자의 역할, 관심사, 한 줄 소개, 카카오 아이디를 수정합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "사용자 정보 수정 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.Userdto.class))
                    )
            }
            )
    public ResponseEntity<ApiResponse<UserResponseDto.Userdto>> updateUserProfile(
            @RequestBody UserUpdateRequestDto requestDto) {

        UserResponseDto.Userdto updatedUser = userCommandService.updateUserProfile(requestDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(updatedUser));
    }

    @GetMapping("/{pot_id}/details")
    @Operation(
            summary = "마이페이지 끓인 팟 상세 보기 모달",
            description = "'끓인 팟 상세보기 모달'에 쓰이는 COMPLETED 상태인 팟의 상세 정보를 가져옵니다. 팟 멤버들의 userPotRole : num과 나의 역할도 함께 반환합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "팟 상세 정보 가져오기 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CompletedPotDetailResponseDto.class))
                    )
            }
    )
    public ResponseEntity<ApiResponse<CompletedPotDetailResponseDto>> getCompletedPotDetail(
            @PathVariable(name = "pot_id") Long potId) {
        CompletedPotDetailResponseDto response = myPotService.getCompletedPotDetail(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @PatchMapping("/{pot_id}")
    @Operation(
            summary = "끓인 팟 수정하기 API",
            description = "끓은 팟의 정보를 수정하는 api 입니다.",
            responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "팟 수정 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PotResponseDto.class))
            )
        }
    )
    public ResponseEntity<ApiResponse<PotResponseDto>> updatePot(
            @PathVariable(name = "pot_id") Long potId,
            @RequestBody @Valid CompletedPotRequestDto requestDto) {
        // 팟 수정 로직 호출
        PotResponseDto responseDto = potService.UpdateCompletedPot(potId, requestDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(responseDto)); // 수정된 팟 정보 반환
    }

}
