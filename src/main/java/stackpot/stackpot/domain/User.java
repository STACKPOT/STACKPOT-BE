package stackpot.stackpot.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import stackpot.stackpot.domain.common.BaseEntity;
import stackpot.stackpot.domain.enums.Role;

import java.util.Collection;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseEntity implements UserDetails{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Primary Key

//    @Column(nullable = false, length = 255)
//    private String loginId; // 로그인 아이디
//
////    @Column(nullable = true, length = 12)
//    private String userName; // 유저 카톡 설정 이름

//    @Column(nullable = false, length = 255)
//    private String snsKey; // SNS 키

    @Column(nullable = true, length = 255)
    private String nickname; // 닉네임

    @Column(nullable = true, length = 255)
    private Role role; // 역할

    @Column(nullable = true, length = 255)
    private String interest; // 관심사

    @Column(nullable = true, columnDefinition = "TEXT")
    private String userIntroduction; // 한 줄 소개

    @Column(nullable = true)
    private Integer userTemperature; // 유저 온도

    @Column(nullable = false, unique = true)
    private String email; // 이메일

    @Column(nullable = true, unique = true)
    private String kakaoId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email; // 사용자 식별자로 이메일을 사용
    }
    public Long getUserId() {
        return id;
    }

}

