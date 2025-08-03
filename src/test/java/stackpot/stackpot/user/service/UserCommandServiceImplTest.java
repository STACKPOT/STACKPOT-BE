package stackpot.stackpot.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import stackpot.stackpot.config.security.JwtTokenProvider;
import stackpot.stackpot.user.dto.response.TokenServiceResponse;
import stackpot.stackpot.user.dto.response.UserResponseDto;
import stackpot.stackpot.user.entity.TempUser;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.entity.enums.Provider;
import stackpot.stackpot.user.entity.enums.Role;
import stackpot.stackpot.user.entity.enums.UserType;
import stackpot.stackpot.user.repository.TempUserRepository;
import stackpot.stackpot.user.repository.UserRepository;

import java.lang.reflect.Field;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class UserCommandServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private TempUserRepository tempUserRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserCommandServiceImpl userCommandService; // 테스트 대상

    //기존_유저인_경우_isNewUser_false를_반환
    @Test
    void existingUser() {
        // given
        Provider provider = Provider.KAKAO;
        String providerId = "1";
        String email = "test@example.com";

        User mockUser = User.builder()
                .id(1L)
                .email(email)
                .provider(provider)
                .providerId(providerId)
                .userType(UserType.USER)
                .role(stackpot.stackpot.user.entity.enums.Role.BACKEND)
                .build();

        TokenServiceResponse mockToken = TokenServiceResponse.of("access", "refresh");

        given(userRepository.findByProviderAndProviderId(provider, providerId)).willReturn(Optional.of(mockUser));
        given(jwtTokenProvider.createToken(any(), any(), any(), any())).willReturn(mockToken);

        // when
        UserResponseDto.loginDto result = userCommandService.isnewUser(provider, providerId, email);

        // then
        assertThat(result.getIsNewUser()).isFalse();
        assertThat(result.getRole()).isEqualTo(stackpot.stackpot.user.entity.enums.Role.BACKEND);
        assertThat(result.getTokenServiceResponse()).isEqualTo(mockToken);
    }

    //신규_유저인_경우_isNewUser_true를_반환하고_TempUser를_저장
    @Test
    void newUser() {
        // given
        Provider provider = Provider.KAKAO;
        String providerId = "67890L";
        String email = "new@example.com";

        TokenServiceResponse mockToken = TokenServiceResponse.of("access", null);

        given(userRepository.findByProviderAndProviderId(provider, providerId)).willReturn(Optional.empty());
        given(tempUserRepository.save(any(TempUser.class))).willAnswer(invocation -> {
            TempUser tempUser = invocation.getArgument(0);
            // ID 세팅된 것처럼 시뮬레이션
            Field idField = TempUser.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(tempUser, 99L);
            return tempUser;
        });
        given(jwtTokenProvider.createToken(any(), any(), eq(UserType.TEMP), any())).willReturn(mockToken);

        // when
        UserResponseDto.loginDto result = userCommandService.isnewUser(provider, providerId, email);

        // then
        assertThat(result.getIsNewUser()).isTrue();
        assertThat(result.getRole()).isNull();
        assertThat(result.getTokenServiceResponse()).isEqualTo(mockToken);

        then(tempUserRepository).should().save(any(TempUser.class));
    }

    //정상적으로_User로_전환되고_TempUser는 삭제
    @Test
    void saveNickname() throws Exception {
        // given
        String nickname = "테스트닉네임";
        Provider provider = Provider.KAKAO;

        TempUser tempUser = TempUser.builder()
                .id(1L)
                .email("temp@example.com")
                .interest("개발")
                .kakaoId("123456")
                .provider(provider)
                .providerId(999L)
                .role(Role.BACKEND)

                .build();

        Authentication authentication = mock(Authentication.class);
        given(authentication.getPrincipal()).willReturn(tempUser);

        SecurityContext securityContext = mock(SecurityContext.class);
        given(securityContext.getAuthentication()).willReturn(authentication);
        SecurityContextHolder.setContext(securityContext); // 인증 객체 주입

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        TokenServiceResponse mockToken = TokenServiceResponse.of("access-token", "refresh-token");

        given(jwtTokenProvider.createToken(any(), any(), any(), any()))
                .willReturn(mockToken);

        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(savedUser, 101L); // 가짜 ID 부여
            return savedUser;
        });

        // when
        TokenServiceResponse result = userCommandService.saveNickname(nickname);

        // then
        then(userRepository).should().save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo(tempUser.getEmail());
        assertThat(savedUser.getNickname()).isEqualTo(nickname.trim());
        assertThat(savedUser.getUserType()).isEqualTo(UserType.USER);
        assertThat(savedUser.getRole()).isEqualTo(tempUser.getRole());
        assertThat(savedUser.getUserIntroduction()).contains(nickname);

        then(tempUserRepository).should(times(1)).delete(tempUser); // 삭제 검증
        then(tempUserRepository).shouldHaveNoMoreInteractions();   // 불필요한 호출 없음

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
    }
}
