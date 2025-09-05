package stackpot.stackpot.pot.service.potMember;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.PotHandler;
import stackpot.stackpot.chat.service.chatroom.ChatRoomCommandService;
import stackpot.stackpot.chat.service.chatroom.ChatRoomQueryService;
import stackpot.stackpot.chat.service.chatroominfo.ChatRoomInfoCommandService;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.pot.converter.PotMemberConverter;
import stackpot.stackpot.pot.dto.PotMemberAppealResponseDto;
import stackpot.stackpot.pot.dto.PotMemberRequestDto;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.pot.entity.enums.ApplicationStatus;
import stackpot.stackpot.pot.entity.mapping.PotApplication;
import stackpot.stackpot.pot.entity.mapping.PotMember;
import stackpot.stackpot.pot.repository.PotApplicationRepository;
import stackpot.stackpot.pot.repository.PotMemberRepository;
import stackpot.stackpot.pot.repository.PotRepository;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.repository.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PotMemberCommandServiceImpl implements PotMemberCommandService {

    private final ChatRoomInfoCommandService chatRoomInfoCommandService;
    private final ChatRoomCommandService chatRoomCommandService;
    private final PotRepository potRepository;
    private final UserRepository userRepository;
    private final PotApplicationRepository potApplicationRepository;
    private final PotMemberRepository potMemberRepository;
    private final PotMemberConverter potMemberConverter;
    private final AuthService authService;

    @Transactional
    @Override
    public List<PotMemberAppealResponseDto> addMembersToPot(Long potId, PotMemberRequestDto requestDto) {
        User potCreatUser = authService.getCurrentUser();

        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("해당 팟을 찾을 수 없습니다."));

        pot.setPotStatus("ONGOING");
        pot.setPotStartDate(String.valueOf(LocalDate.now()));
        potRepository.save(pot);

        User potCreator = pot.getUser();
        potCreator.setUserTemperature(potCreator.getUserTemperature() + 3);
        userRepository.save(potCreator);

        List<PotApplication> allApplications = potApplicationRepository.findByPot_PotId(potId);
        List<Long> approvedApplicantIds = requestDto.getApplicantIds();
        List<PotMember> newMembers = new ArrayList<>();

        for (PotApplication application : allApplications) {
            User user = application.getUser();

            if (approvedApplicantIds.contains(application.getApplicationId())) {
                application.setStatus(ApplicationStatus.APPROVED);
                user.setUserTemperature(user.getUserTemperature() + 3);
                userRepository.save(user);

                PotMember member = potMemberConverter.toEntity(user, pot, application, false);
                newMembers.add(member);
            } else {
                application.setStatus(ApplicationStatus.REJECTED);
            }
            potApplicationRepository.save(application);
        }

        // 생성자 PotMember는 이미 createPot 단계에서 저장됨 → 새로 만들지 말고 조회
        PotMember creatorPM = potMemberRepository.findByPotIdAndUserId(potId, potCreator.getId());
        if (creatorPM == null) {
            throw new PotHandler(ErrorStatus.POT_MEMBER_NOT_FOUND);
        }

        // 승인된 멤버들만 저장
        List<PotMember> savedMembers = potMemberRepository.saveAll(newMembers);

        // ChatRoom + ChatRoomInfo 생성
        Long chatRoomId = chatRoomCommandService.createChatRoom(pot.getPotName(), pot);
        List<Long> potMemberIds = Stream.concat(
                Stream.of(creatorPM.getPotMemberId()),
                savedMembers.stream().map(PotMember::getPotMemberId)
        ).toList();

        chatRoomInfoCommandService.createChatRoomInfo(potMemberIds, chatRoomId);

        // 응답 DTO = 생성자 + 신규 승인자 모두 포함
        return Stream.concat(Stream.of(creatorPM), savedMembers.stream())
                .map(potMemberConverter::toDto)
                .toList();
    }


    @Transactional
    @Override
    public void updateAppealContent(Long potId, String appealContent) {
        User user = authService.getCurrentUser();

        PotMember potMember = potMemberRepository.findByPotPotIdAndUser(potId, user)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_MEMBER_NOT_FOUND));

        potMember.setAppealContent(appealContent);
        potMemberRepository.save(potMember);
    }

    @Override
    @Transactional
    public void removeMemberFromPot(Long potId) {
        User user = authService.getCurrentUser();
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));
        PotMember member = potMemberRepository.findByPotAndUser(pot, user)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_MEMBER_NOT_FOUND));
        potMemberRepository.delete(member);
    }
}
