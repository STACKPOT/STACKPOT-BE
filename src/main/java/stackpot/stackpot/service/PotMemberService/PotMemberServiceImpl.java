package stackpot.stackpot.service.PotMemberService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stackpot.stackpot.converter.PotMemberConverter.PotMemberConverter;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.mapping.PotApplication;
import stackpot.stackpot.domain.mapping.PotMember;
import stackpot.stackpot.repository.PotApplicationRepository.PotApplicationRepository;
import stackpot.stackpot.repository.PotMemberRepository;
import stackpot.stackpot.repository.PotRepository.PotRepository;
import stackpot.stackpot.repository.UserRepository.UserRepository;
import stackpot.stackpot.web.dto.PotMemberRequestDto;
import stackpot.stackpot.web.dto.PotMemberAppealResponseDto;

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
    public List<PotMemberAppealResponseDto> addMembersToPot (Long potId, PotMemberRequestDto requestDto) {
        // 1. 팟 조회
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("해당 팟을 찾을 수 없습니다."));

        // 2. 팟 상태를 "ing"로 설정
        pot.setPotStatus("ing");
        potRepository.save(pot); // 변경 사항 저장


        // 4. 선택된 지원자들을 멤버로 추가
        List<Long> applicantIds = requestDto.getApplicantIds();
        List<PotMember> newMembers = new ArrayList<>();

        for (Long applicantId : applicantIds) {

            PotApplication application = potApplicationRepository.findById(applicantId)
                    .orElseThrow(() -> new IllegalArgumentException("지원자를 찾을 수 없습니다."));
            User user = application.getUser();
            PotMember member = potMemberConverter.toEntity(user, pot, application, false);
            newMembers.add(member);
        }

        // 5. 저장 및 응답 반환
        List<PotMember> savedMembers = potMemberRepository.saveAll(newMembers);
        return savedMembers.stream()
                .map(potMemberConverter::toDto)
                .collect(Collectors.toList());
    }
    @Transactional
    @Override
    public void updateAppealContent(Long potId, Long memberId, String appealContent) {
        // 1. 멤버 조회
        PotMember potMember = potMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 멤버를 찾을 수 없습니다."));

        // 2. 멤버가 해당 팟에 속해 있는지 확인
        if (!potMember.getPot().getPotId().equals(potId)) {
            throw new IllegalArgumentException("해당 멤버는 지정된 팟에 속해 있지 않습니다.");
        }

        // 3. 어필 내용 업데이트
        potMember.setAppealContent(appealContent);
        potMemberRepository.save(potMember); // 변경 사항 저장
    }
}
