package stackpot.stackpot.domain;

import jakarta.persistence.*;
import lombok.*;
import stackpot.stackpot.domain.common.BaseEntity;
import stackpot.stackpot.domain.enums.ModeOfOperation;

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
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(nullable = false, length = 255)
    private String potName;

    @Column(nullable = true)
    private LocalDate startDate;

    @Column(nullable = true)
    private LocalDate endDate;

    @Column(nullable = false, length = 255)
    private String expectedDuration;

    @Column(nullable = false, length = 255)
    private String languagesUsed;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 255)
    private String potStatus;

    @Column(nullable = true, length = 100)
    private String oneLineSummary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModeOfOperation modeOfOperation; // 팟 진행 방식

    @Column(nullable = true, length = 700)
    private String potSummary; // 팟 요약
}