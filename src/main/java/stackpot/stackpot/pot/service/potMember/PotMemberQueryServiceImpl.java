package stackpot.stackpot.pot.service.potMember;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.PotHandler;
import stackpot.stackpot.pot.converter.PotMemberConverter;
import stackpot.stackpot.pot.dto.PotMemberInfoResponseDto;
import stackpot.stackpot.pot.dto.UserMemberIdDto;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.pot.entity.mapping.PotMember;
import stackpot.stackpot.pot.repository.PotApplicationRepository;
import stackpot.stackpot.pot.repository.PotMemberRepository;
import stackpot.stackpot.pot.repository.PotRepository;
import stackpot.stackpot.user.entity.enums.Role;
import stackpot.stackpot.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PotMemberQueryServiceImpl implements PotMemberQueryService {


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
                        return potMemberConverter.toKaKaoCreatorDto(potMember);
                    } else {
                        return potMemberConverter.toKaKaoMemberDto(potMember);
                    }
                })
                .collect(Collectors.toList());

        // owner가 true인 팟 생성자가 항상 맨 위로 오도록 정렬
        memberDtos.sort((a, b) -> Boolean.compare(b.isOwner(), a.isOwner()));

        return memberDtos;
    }

    @Override
    public Long selectPotMemberIdByUserIdAndPotId(Long userId, Long potId) {
        return potMemberRepository.selectByPotMemberIdByUserIdAndPotId(userId, potId).orElseThrow(() -> new PotHandler(ErrorStatus.POT_MEMBER_NOT_FOUND));
    }

    @Override
    public List<Long> selectPotMembersIdsByUserIdsAndPotId(List<Long> userIds, Long potId) {
        List<Long> potMemberIds = potMemberRepository.selectByPotMemberIdsByUserIdsAndPotId(userIds, potId);
        if (potMemberIds.isEmpty())
            throw new PotHandler(ErrorStatus.POT_MEMBER_NOT_FOUND);
        return potMemberIds;
    }

    @Override
    public List<Long> selectUserIdsAboutPotMembersByPotId(Long potId) {
        List<Long> userIds = potMemberRepository.selectUserIdsAboutPotMembersByPotId(potId);
        if (userIds.isEmpty())
            throw new PotHandler(ErrorStatus.POT_MEMBER_NOT_FOUND);
        return userIds;
    }

    @Override
    public List<UserMemberIdDto> selectPotMemberIdsByUserId(Long userId) {
        return potMemberRepository.selectPotMemberIdsByUserId(userId);
    }

    @Override
    public List<PotMember> selectPotMembersByPotMemberIds(List<Long> potMemberIds) {
        List<PotMember> potMembers = potMemberRepository.selectPotMembersByPotMemberIds(potMemberIds);
        if (potMemberIds.isEmpty())
            throw new PotHandler(ErrorStatus.POT_MEMBER_NOT_FOUND);
        return potMembers;
    }

    @Override
    public Role selectRoleByUserIdAndPotId(Long userId, Long potId) {
        return potMemberRepository.selectRoleByUserIdAndPotId(userId, potId).orElseThrow(() -> new PotHandler(ErrorStatus.POT_MEMBER_NOT_FOUND));
    }
}
