package stackpot.stackpot.pot.service.pot;


import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.PotHandler;
import stackpot.stackpot.badge.service.BadgeService;
import stackpot.stackpot.chat.service.chatroom.ChatRoomCommandService;
import stackpot.stackpot.chat.service.chatroom.ChatRoomQueryService;
import stackpot.stackpot.chat.service.chatroominfo.ChatRoomInfoCommandService;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.notification.dto.NotificationResponseDto;
import stackpot.stackpot.notification.event.PotEndEvent;
import stackpot.stackpot.notification.service.NotificationCommandService;
import stackpot.stackpot.pot.converter.PotConverter;
import stackpot.stackpot.pot.converter.PotMemberConverter;
import stackpot.stackpot.pot.dto.CompletedPotRequestDto;
import stackpot.stackpot.pot.dto.PotNameUpdateRequestDto;
import stackpot.stackpot.pot.dto.PotRequestDto;
import stackpot.stackpot.pot.dto.PotResponseDto;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.pot.entity.PotRecruitmentDetails;
import stackpot.stackpot.pot.entity.mapping.PotApplication;
import stackpot.stackpot.pot.entity.mapping.PotMember;
import stackpot.stackpot.pot.repository.PotMemberRepository;
import stackpot.stackpot.pot.repository.PotRecruitmentDetailsRepository;
import stackpot.stackpot.pot.repository.PotRepository;
import stackpot.stackpot.todo.service.UserTodoService;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.entity.enums.Role;
import stackpot.stackpot.user.repository.UserRepository;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PotCommandServiceImpl implements PotCommandService {

    private final ChatRoomQueryService chatRoomQueryService;
    private final ChatRoomInfoCommandService chatRoomInfoCommandService;
    private final ChatRoomCommandService chatRoomCommandService;
    private final PotRepository potRepository;
    private final PotRecruitmentDetailsRepository recruitmentDetailsRepository;
    private final PotConverter potConverter;
    private final UserRepository userRepository;
    private final PotMemberRepository potMemberRepository;
    private final UserTodoService userTodoService;
    private final AuthService authService;
    private final BadgeService badgeService;
    private final PotMemberConverter potMemberConverter;
    private final NotificationCommandService notificationCommandService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public PotResponseDto createPotWithRecruitments(PotRequestDto requestDto) {
        User user = authService.getCurrentUser();

        if (requestDto == null || requestDto.getRecruitmentDetails() == null) {
            throw new PotHandler(ErrorStatus._BAD_REQUEST);
        }
        if (!List.of("ONLINE", "OFFLINE", "HYBRID").contains(requestDto.getPotModeOfOperation())) {
            throw new PotHandler(ErrorStatus.INVALID_POT_MODE_OF_OPERATION);
        }

        Pot pot = potConverter.toEntity(requestDto, user);
        pot.setPotStatus("RECRUITING");
        Pot savedPot = potRepository.save(pot);

        PotMember creator = potMemberConverter.toCreatorEntity(user, savedPot, String.valueOf(requestDto.getPotRole()));
        potMemberRepository.save(creator);

        List<PotRecruitmentDetails> recruitmentDetails = requestDto.getRecruitmentDetails().stream()
                .map(dto -> PotRecruitmentDetails.builder()
                        .recruitmentRole(Role.valueOf(dto.getRecruitmentRole()))
                        .recruitmentCount(dto.getRecruitmentCount())
                        .pot(savedPot)
                        .build())
                .collect(Collectors.toList());

        recruitmentDetailsRepository.saveAll(recruitmentDetails);

        return potConverter.toDto(savedPot, recruitmentDetails);
    }

    @Override
    @Transactional
    public PotResponseDto updatePotWithRecruitments(Long potId, PotRequestDto requestDto) {
        User user = authService.getCurrentUser();
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        if (!pot.getUser().equals(user)) {
            throw new PotHandler(ErrorStatus.POT_FORBIDDEN);
        }

        if (!List.of("ONLINE", "OFFLINE", "HYBRID").contains(requestDto.getPotModeOfOperation())) {
            throw new PotHandler(ErrorStatus.INVALID_POT_MODE_OF_OPERATION);
        }

        Map<String, Object> updateValues = new LinkedHashMap<>();
        updateValues.put("potName", requestDto.getPotName());
        updateValues.put("potStartDate", requestDto.getPotStartDate());
        updateValues.put("potEndDate", requestDto.getPotEndDate());
        updateValues.put("potLan", requestDto.getPotLan());
        updateValues.put("potContent", requestDto.getPotContent());
        updateValues.put("potModeOfOperation", requestDto.getPotModeOfOperation());
        updateValues.put("potRecruitmentDeadline", requestDto.getPotRecruitmentDeadline());
        if (requestDto.getPotSummary() != null) {
            updateValues.put("potSummary", requestDto.getPotSummary());
        }

        pot.updateFields(updateValues);
        recruitmentDetailsRepository.deleteByPot_PotId(potId);

        List<PotRecruitmentDetails> recruitmentDetails = requestDto.getRecruitmentDetails().stream()
                .map(dto -> PotRecruitmentDetails.builder()
                        .recruitmentRole(Role.valueOf(dto.getRecruitmentRole()))
                        .recruitmentCount(dto.getRecruitmentCount())
                        .pot(pot)
                        .build())
                .collect(Collectors.toList());

        recruitmentDetailsRepository.saveAll(recruitmentDetails);

        return potConverter.toDto(pot, recruitmentDetails);
    }

    @Override
    @Transactional
    public void deletePot(Long potId) {
        User user = authService.getCurrentUser();
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        if (!pot.getUser().equals(user)) {
            throw new PotHandler(ErrorStatus.POT_FORBIDDEN);
        }

        recruitmentDetailsRepository.deleteByPot_PotId(potId);
        potRepository.delete(pot);
    }

    // 특정 팟 지원자의 좋아요 상태 변경
    @Override
    public void patchLikes(Long potId, Long applicationId, Boolean liked) {
        // 현재 로그인한 사용자 조회
        User currentUser = authService.getCurrentUser();

        // 팟 조회
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        // 팟 생성자 확인
        if (!pot.getUser().getId().equals(currentUser.getId())) {
            throw new PotHandler(ErrorStatus.POT_FORBIDDEN);
        }

        // 지원자 목록에서 해당 지원자 찾기
        PotApplication application = pot.getPotApplication().stream()
                .filter(app -> app.getApplicationId().equals(applicationId))
                .findFirst()
                .orElseThrow(() -> new PotHandler(ErrorStatus.APPLICATION_NOT_FOUND));

        application.setLiked(liked);
        potRepository.save(pot);
    }


    @Override
    @Transactional
    public PotResponseDto patchPotWithRecruitments(Long potId, CompletedPotRequestDto requestDto) {
        User user = authService.getCurrentUser();
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        if (!pot.getUser().equals(user)) {
            throw new PotHandler(ErrorStatus.POT_FORBIDDEN);
        }

        user.setUserTemperature(Math.min(user.getUserTemperature() + 5, 100));
        List<User> potMembers = pot.getPotMembers().stream()
                .map(PotMember::getUser)
                .collect(Collectors.toList());

        userRepository.save(user);
        userRepository.saveAll(potMembers);

        Map<String, Object> updateValues = new LinkedHashMap<>();
        updateValues.put("potName", requestDto.getPotName());
        updateValues.put("potStartDate", requestDto.getPotStartDate());
        updateValues.put("potEndDate", String.valueOf(LocalDate.now()));
        updateValues.put("potStatus", "COMPLETED");
        updateValues.put("potLan", requestDto.getPotLan());
        updateValues.put("potSummary", requestDto.getPotSummary());
        pot.updateFields(updateValues);

        potRepository.save(pot);

        List<PotRecruitmentDetails> recruitmentDetails = pot.getRecruitmentDetails().stream()
                .map(dto -> PotRecruitmentDetails.builder()
                        .recruitmentRole(Role.valueOf(dto.getRecruitmentRole().name()))
                        .recruitmentCount(dto.getRecruitmentCount())
                        .pot(pot)
                        .build())
                .collect(Collectors.toList());

        badgeService.assignBadgeToTopMembers(potId);

        NotificationResponseDto.UnReadNotificationDto dto = notificationCommandService.createdPotEndNotification(potId);
        applicationEventPublisher.publishEvent(new PotEndEvent(potId, dto));

        return potConverter.toDto(pot, recruitmentDetails);
    }

    @Override
    @Transactional
    public String removePotOrMember(Long potId) {
        User user = authService.getCurrentUser();
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));
        Long chatRoomId = chatRoomQueryService.selectChatRoomIdByPotId(potId);
        if (pot.getUser().equals(user)) {
            // 채팅방 정보, 채팅방 삭제
            List<Long> potMemberIds = potMemberRepository.selectPotMemberIdsByPotId(potId);
            chatRoomInfoCommandService.deleteChatRoomInfo(potMemberIds);
            chatRoomCommandService.deleteChatRoomByPotId(potId);
            recruitmentDetailsRepository.deleteByPot_PotId(potId);
            potRepository.delete(pot);
            return "팟이 성공적으로 삭제되었습니다.";
        } else {
            PotMember member = potMemberRepository.findByPotAndUser(pot, user)
                    .orElseThrow(() -> new PotHandler(ErrorStatus.POT_MEMBER_NOT_FOUND));
            // 채팅방 정보 삭제
            chatRoomInfoCommandService.deleteChatRoomInfo(member.getPotMemberId(), chatRoomId);
            potMemberRepository.delete(member);
            return "팟 멤버가 성공적으로 삭제되었습니다.";
        }
    }


    @Override
    @Transactional
    public PotResponseDto updateCompletedPot(Long potId, CompletedPotRequestDto requestDto) {
        User user = authService.getCurrentUser();
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        if (!pot.getUser().equals(user)) {
            throw new PotHandler(ErrorStatus.POT_FORBIDDEN);
        }

        Map<String, Object> updateValues = new LinkedHashMap<>();
        updateValues.put("potName", requestDto.getPotName());
        updateValues.put("potStartDate", requestDto.getPotStartDate());
        updateValues.put("potLan", requestDto.getPotLan());
        updateValues.put("potSummary", requestDto.getPotSummary());
        pot.updateFields(updateValues);

        List<PotRecruitmentDetails> recruitmentDetails = pot.getRecruitmentDetails().stream()
                .map(dto -> PotRecruitmentDetails.builder()
                        .recruitmentRole(Role.valueOf(dto.getRecruitmentRole().name()))
                        .recruitmentCount(dto.getRecruitmentCount())
                        .pot(pot)
                        .build())
                .collect(Collectors.toList());

        return potConverter.toDto(pot, recruitmentDetails);
    }

    @Override
    @Transactional
    public String updatePotName(Long potId, PotNameUpdateRequestDto request) {
        User user = authService.getCurrentUser();
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        if (!pot.getUser().equals(user)) {
            throw new PotHandler(ErrorStatus.POT_FORBIDDEN);
        }
        pot.setPotName(request.getPotName());

        return pot.getPotName();
    }
}
