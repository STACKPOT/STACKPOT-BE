package stackpot.stackpot.pot.service.potApplication;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.PotHandler;
import stackpot.stackpot.common.service.EmailService;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.common.util.RoleNameMapper;
import stackpot.stackpot.notification.dto.NotificationResponseDto;
import stackpot.stackpot.notification.event.PotApplicationEvent;
import stackpot.stackpot.notification.service.NotificationCommandService;
import stackpot.stackpot.pot.converter.PotApplicationConverter;
import stackpot.stackpot.pot.dto.PotApplicationRequestDto;
import stackpot.stackpot.pot.dto.PotApplicationResponseDto;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.pot.entity.enums.ApplicationStatus;
import stackpot.stackpot.pot.entity.mapping.PotApplication;
import stackpot.stackpot.pot.repository.PotApplicationRepository;
import stackpot.stackpot.pot.repository.PotRepository;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PotApplicationCommandServiceImpl implements PotApplicationCommandService {

    private final NotificationCommandService notificationCommandService;
    private final PotApplicationRepository potApplicationRepository;
    private final PotRepository potRepository;
    private final UserRepository userRepository;
    private final PotApplicationConverter potApplicationConverter;
    private final EmailService emailService;
    private final AuthService authService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    @Override
    public PotApplicationResponseDto applyToPot(PotApplicationRequestDto dto, Long potId) {
        User user = authService.getCurrentUser();
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        if (potApplicationRepository.existsByUserIdAndPot_PotId(user.getId(), potId)) {
            throw new PotHandler(ErrorStatus.DUPLICATE_APPLICATION);
        }

        PotApplication application = potApplicationConverter.toEntity(dto, pot, user);
        application.setApplicationStatus(ApplicationStatus.PENDING);
        application.setAppliedAt(LocalDateTime.now());

        PotApplication savedApplication = potApplicationRepository.save(application);

        sendSupportEmailAsync(user, pot, savedApplication);

        NotificationResponseDto.UnReadNotificationDto notiDto = notificationCommandService.createPotApplicationNotification(
                pot.getPotId(), savedApplication.getApplicationId(), user.getNickname());

        applicationEventPublisher.publishEvent(new PotApplicationEvent(pot.getUser().getId(), notiDto));

        return potApplicationConverter.toDto(savedApplication);
    }

    @Transactional
    @Override
    public void cancelApplication(Long potId) {
        User user = authService.getCurrentUser();
        PotApplication application = potApplicationRepository.findByUserIdAndPot_PotId(user.getId(), potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.APPLICATION_NOT_FOUND));
        notificationCommandService.deletePotApplicationNotification(application.getApplicationId());
        potApplicationRepository.delete(application);
    }

    private void sendSupportEmailAsync(User user, Pot pot, PotApplication application) {
        String appliedRole = application.getPotRole().name();
        String appliedRoleName = RoleNameMapper.mapRoleName(appliedRole);
        String applicantRole = user.getRoles().stream()
                .map(role -> RoleNameMapper.mapRoleName(role.name()))
                .collect(Collectors.joining(", "));
        CompletableFuture.runAsync(() -> emailService.sendSupportNotification(
                pot.getUser().getEmail(),
                pot.getPotName(),
                user.getNickname() + applicantRole,
                appliedRoleName,
                appliedRole,
                Optional.ofNullable(user.getUserIntroduction()).orElse("없음")
        ));
    }


}

