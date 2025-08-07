package stackpot.stackpot.user.entity;

import jakarta.persistence.*;
import lombok.*;

import stackpot.stackpot.common.BaseEntity;
import stackpot.stackpot.user.entity.enums.Provider;
import stackpot.stackpot.user.entity.enums.Role;

import java.util.List;
import java.util.Set;

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

    @Column
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(nullable = false)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 255)
    private Role role; // 역할

    @Column(nullable = true)
    @ElementCollection
    private List<String> interest;// 관심사

    @Column(nullable = true)
    private String email; // 이메일

    @Column(nullable = true)
    private String kakaoId;

}

