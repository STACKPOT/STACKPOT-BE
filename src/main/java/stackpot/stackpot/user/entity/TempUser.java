package stackpot.stackpot.user.entity;

import jakarta.persistence.*;
import lombok.*;

import stackpot.stackpot.common.BaseEntity;
import stackpot.stackpot.user.entity.enums.Provider;
import stackpot.stackpot.user.entity.enums.Role;



@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TempUser extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Primary Key

    @Column(nullable = true, length = 255)
    private String nickname; // 닉네임

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 255)
    private Role role; // 역할

    @Column(nullable = true, length = 255)
    private String interest; // 관심사

    @Column(nullable = true, unique = true)
    private String email; // 이메일

    @Column(nullable = true, unique = true)
    private String kakaoId;

    @Column
    @Enumerated(EnumType.STRING)
    private Provider provider;



}

