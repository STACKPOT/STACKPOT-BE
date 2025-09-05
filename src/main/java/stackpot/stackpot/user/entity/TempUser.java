package stackpot.stackpot.user.entity;

import jakarta.persistence.*;
import lombok.*;

import stackpot.stackpot.common.BaseEntity;
import stackpot.stackpot.user.entity.enums.Provider;
import stackpot.stackpot.user.entity.enums.Role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @ElementCollection(targetClass = Role.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "temp_user_roles", joinColumns = @JoinColumn(name = "temp_user_id"))
    @Column(name = "role")
    private List<Role> roles = new ArrayList<>();

    @Column(nullable = true)
    @ElementCollection
    private List<String> interest;// 관심사

    @Column(nullable = true)
    private String email; // 이메일

    @Column(nullable = true)
    private String kakaoId;

    public List<String> getRoleNames() {
        return roles != null
                ? roles.stream().map(Enum::name).collect(Collectors.toList())
                : Collections.emptyList();
    }

}

