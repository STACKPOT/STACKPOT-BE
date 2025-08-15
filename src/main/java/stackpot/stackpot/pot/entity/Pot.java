package stackpot.stackpot.pot.entity;

import jakarta.persistence.*;
import lombok.*;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.common.BaseEntity;
import stackpot.stackpot.pot.entity.enums.PotModeOfOperation;
import stackpot.stackpot.pot.entity.mapping.PotApplication;
import stackpot.stackpot.pot.entity.mapping.PotMember;

import java.time.LocalDate;
import java.util.Map;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Pot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long potId;

    @Setter
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "pot",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PotRecruitmentDetails> recruitmentDetails;

    @OneToMany(mappedBy = "pot", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<PotApplication> potApplication;

    @OneToMany(mappedBy = "pot", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<PotMember> potMembers;

    @Setter
    @Column(nullable = false, length = 255)
    private String potName;

    // 예상기간: potStartDate ~ potEndDate(2025.07 ~ 2025.08)
    @Setter
    @Column(nullable = false)
    private String potStartDate;

    @Column(nullable = true)
    private String potEndDate;

    // 모집 마감일: potRecruitmentDeadline
    @Column(nullable = false)
    private LocalDate potRecruitmentDeadline;

    @Column(nullable = false, length = 255)
    private String potLan;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String potContent;

    @Setter
    @Column(nullable = false, length = 255)
    private String potStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PotModeOfOperation potModeOfOperation; // 팟 진행 방식

    @Column(nullable = true, length = 400)
    private String potSummary; // 팟 요약

    public void updateFields(Map<String, Object> updates) {
        updates.forEach((key, value) -> {
            if (value != null) {
                switch (key) {
                    case "potName" -> this.potName = (String) value;
                    case "potStartDate" -> this.potStartDate = (String) value;
                    case "potEndDate" -> this.potEndDate = (String) value;
                    case "potLan" -> this.potLan = (String) value;
                    case "potContent" -> this.potContent = (String) value;
                    case "potStatus" -> this.potStatus = (String) value;
                    case "potModeOfOperation" -> this.potModeOfOperation = PotModeOfOperation.valueOf((String) value);
                    case "potSummary" -> this.potSummary = (String) value;
                    case "potRecruitmentDeadline" -> this.potRecruitmentDeadline = (LocalDate) value;
                }
            }
        });
    }




}