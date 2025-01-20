package stackpot.stackpot.domain;

import jakarta.persistence.*;
import lombok.*;
import stackpot.stackpot.domain.common.BaseEntity;
import stackpot.stackpot.domain.enums.PotModeOfOperation;
import stackpot.stackpot.domain.mapping.PotApplication;
import stackpot.stackpot.domain.mapping.PotMember;

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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "pot")
    private List<PotRecruitmentDetails> recruitmentDetails;

    @OneToMany(mappedBy = "pot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PotApplication> potApplication;

    @OneToMany(mappedBy = "pot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PotMember> potMembers;

    @Column(nullable = false, length = 255)
    private String potName;

    @Column(nullable = false)
    private LocalDate potStartDate;

    @Column(nullable = false)
    private LocalDate potEndDate;

    @Column(nullable = false, length = 255)
    private String potDuration;

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

    @Column(nullable = false)
    private LocalDate recruitmentDeadline;
    public void updateFields(Map<String, Object> updates) {
        updates.forEach((key, value) -> {
            if (value != null) {
                switch (key) {
                    case "potName" -> this.potName = (String) value;
                    case "potStartDate" -> this.potStartDate = (LocalDate) value;
                    case "potEndDate" -> this.potEndDate = (LocalDate) value;
                    case "potDuration" -> this.potDuration = (String) value;
                    case "potLan" -> this.potLan = (String) value;
                    case "potContent" -> this.potContent = (String) value;
                    case "potStatus" -> this.potStatus = (String) value;
                    case "potModeOfOperation" -> this.potModeOfOperation = PotModeOfOperation.valueOf((String) value);
                    case "potSummary" -> this.potSummary = (String) value;
                    case "recruitmentDeadline" -> this.recruitmentDeadline = (LocalDate) value;
                }
            }
        });
    }




}