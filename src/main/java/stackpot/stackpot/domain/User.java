package stackpot.stackpot.domain;

import jakarta.persistence.*;
import lombok.*;
import stackpot.stackpot.domain.common.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Primary Key

    @Column(nullable = false, length = 255)
    private String loginId; // 로그인 아이디

    @Column(nullable = true, length = 255)
    private String userName; // 유저 이름

    @Column(nullable = false, length = 255)
    private String snsKey; // SNS 키

    @Column(nullable = false, length = 255)
    private String nickname; // 닉네임

    @Column(nullable = false, length = 255)
    private String role; // 역할

    @Column(nullable = false, length = 255)
    private String kakaoId; // 카카오 아이디

    @Column(nullable = false, length = 255)
    private String interest; // 관심사

    @Column(nullable = true, columnDefinition = "TEXT")
    private String introduction; // 한 줄 소개

    @Column(nullable = false)
    private Integer userTemperature; // 유저 온도

    @Column(nullable = false, unique = true)
    private String email; // 이메일
}
