package stackpot.stackpot.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import stackpot.stackpot.common.BaseEntity;
import stackpot.stackpot.feed.entity.Series;
import stackpot.stackpot.user.entity.enums.Provider;
import stackpot.stackpot.user.entity.enums.Role;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.user.entity.enums.UserType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
        name = "user",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_provider_provider_id",
                columnNames = {"provider", "providerId"}
        )
)
public class User extends BaseEntity implements UserDetails{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Primary Key

    @Column
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(nullable = true)
    private String providerId;

    @Column(nullable = false)
    private UserType userType;

    @Column(nullable = true, length = 255)
    private String nickname; // 닉네임

    @ElementCollection(targetClass = Role.class)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private List<Role> roles = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "user_interests", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "interest")
    private List<String> interests = new ArrayList<>();  // List<String> 사용

    @Column(nullable = true, columnDefinition = "TEXT")
    private String userIntroduction; // 한 줄 소개

    @Column(nullable = true, columnDefinition = "TEXT")
    private String userDescription;; // 나의 소개

    @Getter
    @Setter
    @Column(nullable = true)
    private Integer userTemperature; // 유저 온도

    @Column(nullable = true)
    private String email; // 이메일

    @Column(nullable = true)
    private String kakaoId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<Pot> pots;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Series> seriesList ;

    private boolean isDeleted = false; // 삭제 여부 필드

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
        return String.valueOf(this.id); // 사용자 식별자로 아이디을 사용
    }
    public Long getUserId() {
        return id;
    }

    public void deleteUser() {
        this.isDeleted = true;
        this.nickname = "(알 수 없음)";  // 표시용 변경
        this.providerId = null;
        if (this.roles != null) {
            this.roles.clear();  // 기존 역할 초기화
        }
        this.roles = new ArrayList<>();
        this.roles.add(Role.UNKNOWN);  // UNKNOWN 역할로 변경

        this.kakaoId = null;
        if (this.interests != null) {
            this.interests.clear();  // 기존 관심사 목록 비우기
        }
        this.interests = new ArrayList<>();  // 새로운 관심사 목록 생성
        this.interests.add("UNKNOWN");  // "UNKNOWN"을 관심사 목록에 추가

        if (this.seriesList != null) {
            this.seriesList.clear(); // 연관된 시리즈 비우기
        }

        this.userTemperature = null;
        this.email = null;
        this.userIntroduction = null;
        this.userDescription = null;
        // 이메일 등 개인 정보 삭제
    }

    public void updateUserDescription(String userDescription) {
        this.userDescription = userDescription;
    }
    public List<String> getRoleNames() {
        return roles != null
                ? roles.stream().map(Enum::name).collect(Collectors.toList())
                : Collections.emptyList();
    }


}

