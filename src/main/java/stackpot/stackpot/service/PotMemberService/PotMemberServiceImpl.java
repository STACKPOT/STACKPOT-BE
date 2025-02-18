package stackpot.stackpot.service.PotMemberService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.MemberHandler;
import stackpot.stackpot.apiPayload.exception.handler.PotHandler;
import stackpot.stackpot.converter.PotMemberConverter.PotMemberConverter;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.enums.ApplicationStatus;
import stackpot.stackpot.domain.mapping.PotApplication;
import stackpot.stackpot.domain.mapping.PotMember;
import stackpot.stackpot.repository.PotApplicationRepository.PotApplicationRepository;
import stackpot.stackpot.repository.PotMemberRepository;
import stackpot.stackpot.repository.PotRepository.PotRepository;
import stackpot.stackpot.repository.UserRepository.UserRepository;
import stackpot.stackpot.web.dto.PotMemberAppealResponseDto;
import stackpot.stackpot.web.dto.PotMemberInfoResponseDto;
import stackpot.stackpot.web.dto.PotMemberRequestDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PotMemberServiceImpl implements PotMemberService {

    private final PotRepository potRepository;
    private final UserRepository userRepository;
    private final PotApplicationRepository potApplicationRepository;
    private final PotMemberRepository potMemberRepository;
    private final PotMemberConverter potMemberConverter;

    @Transactional
    @Override

    public List<PotMemberInfoResponseDto> getPotMembers(Long potId) {
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        List<PotMember> potMembers = potMemberRepository.findByPotId(potId);

        List<PotMemberInfoResponseDto> memberDtos = potMembers.stream()
                .map(potMember -> {
                    if (potMember.isOwner()) {
                        // 팟 생성자의 역할은 User 테이블에서 가져옴
                        return potMemberConverter.toKaKaoCreatorDto(potMember);
                    } else {
                        // 나머지 멤버들의 역할은 지원 테이블(PotApplication)에서 가져옴
                        return potMemberConverter.toKaKaoMemberDto(potMember);
                    }
                })
                .collect(Collectors.toList());

        // owner가 true인 팟 생성자가 항상 맨 위로 오도록 정렬
        memberDtos.sort((a, b) -> Boolean.compare(b.isOwner(), a.isOwner()));

        return memberDtos;
    }

    @Transactional
    @Override
    public List<PotMemberAppealResponseDto> addMembersToPot (Long potId, PotMemberRequestDto requestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 1. 팟 조회
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("해당 팟을 찾을 수 없습니다."));

        // 2. 팟 상태를 "ing"로 설정
        pot.setPotStatus("ONGOING");

        // 3. 팟의 시작 날짜를 현재 날짜로 설정
        pot.setPotStartDate(LocalDate.now()); // 필드 이름에 따라 메서드 호출
        potRepository.save(pot); // 변경 사항 저장

        // 4. 팟 생성자의 temperature 증가
        User potCreator = pot.getUser();  // 팟 생성자 가져오기
        potCreator.setUserTemperature(potCreator.getUserTemperature() + 3); // 3도 증가
        userRepository.save(potCreator);   // 변경 사항 저장


        List<PotApplication> allApplications = potApplicationRepository.findByPot_PotId(potId);


        List<Long> approvedApplicantIds = requestDto.getApplicantIds();
        List<PotMember> newMembers = new ArrayList<>();


        for (PotApplication application : allApplications) {
            User user = application.getUser();

            if (approvedApplicantIds.contains(application.getApplicationId())) {
                // ✅ 선택된 지원자 → APPROVED & 멤버로 추가
                application.setStatus(ApplicationStatus.APPROVED);

                // 지원자의 temperature 증가
                user.setUserTemperature(user.getUserTemperature() + 3);
                userRepository.save(user);

                PotMember member = potMemberConverter.toEntity(user, pot, application, false);
                newMembers.add(member);
            } else {
                // ✅ 선택되지 않은 지원자 → REJECTED
                application.setStatus(ApplicationStatus.REJECTED);
            }
            potApplicationRepository.save(application);
        }

        User potCreatUser = userRepository.findByEmail(email)
                .orElseThrow(()-> new IllegalArgumentException("유저를 찾을 수 없스니다. 유저 email : " + email));

        PotMember member = potMemberConverter.toEntity(potCreatUser, pot, null, true);
        newMembers.add(member);

        // 5. 저장 및 응답 반환
        List<PotMember> savedMembers = potMemberRepository.saveAll(newMembers);
        return savedMembers.stream()
                .map(potMemberConverter::toDto)
                .collect(Collectors.toList());
    }
    @Transactional
    @Override
    public void updateAppealContent(Long potId, String appealContent) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new MemberHandler(ErrorStatus.AUTHENTICATION_FAILED);
        }
        String email = authentication.getName();

        // 1. 현재 로그인한 사용자의 멤버 정보 조회
        PotMember potMember = potMemberRepository.findByPot_PotIdAndUser_Email(potId, email)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_MEMBER_NOT_FOUND));

        // 2. 어필 내용 업데이트
        potMember.setAppealContent(appealContent);
        potMemberRepository.save(potMember);
    }

    @Override
    public void validateIsOwner(Long potId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("해당 팟을 찾을 수 없습니다."));

        if (!pot.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException("해당 작업은 팟 생성자만 수행할 수 있습니다.");
        }
    }


}
