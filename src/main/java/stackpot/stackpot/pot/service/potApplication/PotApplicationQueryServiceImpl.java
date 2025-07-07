package stackpot.stackpot.pot.service.potApplication;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.PotHandler;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.common.util.RoleNameMapper;
import stackpot.stackpot.pot.converter.PotApplicationConverter;
import stackpot.stackpot.pot.converter.PotDetailConverter;
import stackpot.stackpot.pot.dto.PotApplicationResponseDto;
import stackpot.stackpot.pot.dto.PotDetailResponseDto;
import stackpot.stackpot.pot.dto.PotDetailWithApplicantsResponseDto;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.pot.entity.mapping.PotApplication;
import stackpot.stackpot.pot.repository.PotApplicationRepository;
import stackpot.stackpot.pot.repository.PotRepository;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PotApplicationQueryServiceImpl implements PotApplicationQueryService {

    private final PotApplicationRepository potApplicationRepository;
    private final PotRepository potRepository;
    private final UserRepository userRepository;
    private final PotApplicationConverter potApplicationConverter;
    private final PotDetailConverter potDetailConverter;
    private final AuthService authService;

    @Override
    @Transactional(readOnly = true)
    public List<PotApplicationResponseDto> getApplicantsByPotId(Long potId) {
        User user = authService.getCurrentUser();
        Pot pot = getPotById(potId);

        if (!pot.getUser().equals(user)) {
            throw new PotHandler(ErrorStatus.UNAUTHORIZED_ACCESS);
        }

        return potApplicationRepository.findByPot_PotId(potId).stream()
                .map(potApplicationConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PotDetailWithApplicantsResponseDto getPotDetailsAndApplicants(Long potId) {
        User user = authService.getCurrentUser();

        Pot pot = potRepository.findPotWithRecruitmentDetailsByPotId(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        boolean isOwner = user.getId().equals(pot.getUser().getId());
        boolean isApplied = pot.getPotApplication().stream()
                .anyMatch(application -> application.getUser().getId().equals(user.getId()));

        String recruitmentDetails = pot.getRecruitmentDetails().stream()
                .map(rd -> RoleNameMapper.mapRoleName(rd.getRecruitmentRole().name()) + "(" + rd.getRecruitmentCount() + ")")
                .collect(Collectors.joining(", "));

        PotDetailResponseDto potDetailDto = potDetailConverter.toPotDetailResponseDto(pot.getUser(), pot, recruitmentDetails, isOwner, isApplied, null);

        List<PotApplicationResponseDto> applicants = Collections.emptyList();
        if (isOwner && "RECRUITING".equals(pot.getPotStatus())) {
            applicants = potApplicationRepository.findByPot_PotId(potId).stream()
                    .map(potApplicationConverter::toDto)
                    .collect(Collectors.toList());
        }

        return PotDetailWithApplicantsResponseDto.builder()
                .potDetail(potDetailDto)
                .applicants(applicants)
                .build();
    }

    private Pot getPotById(Long potId) {
        return potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));
    }

    public PotApplication getPotApplicationById(Long applicationId) {
        return potApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.APPLICATION_NOT_FOUND));
    }
}
