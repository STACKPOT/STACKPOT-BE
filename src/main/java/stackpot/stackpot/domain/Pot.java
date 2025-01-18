package stackpot.stackpot.domain;

import jakarta.persistence.*;
import lombok.*;
import stackpot.stackpot.domain.common.BaseEntity;
import stackpot.stackpot.domain.enums.PotModeOfOperation;

import java.time.LocalDate;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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

    @Column(nullable = false, length = 255)
    private String potStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PotModeOfOperation potModeOfOperation; // 팟 진행 방식

    @Column(nullable = true, length = 400)
    private String potSummary; // 팟 요약

    @Column(nullable = false)
    private LocalDate recruitmentDeadline;
}