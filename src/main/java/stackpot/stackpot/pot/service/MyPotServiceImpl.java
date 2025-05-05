package stackpot.stackpot.pot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.MemberHandler;
import stackpot.stackpot.apiPayload.exception.handler.PotHandler;
import stackpot.stackpot.badge.dto.BadgeDto;
import stackpot.stackpot.badge.dto.CompletedPotBadgeResponseDto;
import stackpot.stackpot.common.util.RoleNameMapper;
import stackpot.stackpot.pot.converter.MyPotConverter;
import stackpot.stackpot.pot.converter.PotDetailConverter;
import stackpot.stackpot.pot.dto.CompletedPotDetailResponseDto;
import stackpot.stackpot.pot.dto.OngoingPotResponseDto;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.entity.enums.Role;
import stackpot.stackpot.pot.entity.mapping.PotMember;
import stackpot.stackpot.badge.repository.PotMemberBadgeRepository;
import stackpot.stackpot.pot.repository.PotMemberRepository;
import stackpot.stackpot.pot.repository.PotRepository;
import stackpot.stackpot.user.repository.UserRepository;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class MyPotServiceImpl implements MyPotService {

    private final PotRepository potRepository;
    private final UserRepository userRepository;
    private final PotMemberRepository potMemberRepository;
    private final PotDetailConverter potDetailConverter;
    private final MyPotConverter myPotConverter;
    private final PotMemberBadgeRepository potMemberBadgeRepository;

    @Override
    public List<OngoingPotResponseDto> getMyPots() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // 내가 PotMember로 참여 중이고 상태가 'ONGOING'인 팟 조회 (내가 만든 팟 제외)
        List<Pot> ongoingMemberPots = potRepository.findByPotMembers_UserIdAndPotStatusOrderByCreatedAtDesc(user.getId(), "ONGOING");

        // DTO 변환 시 userId 추가
        return ongoingMemberPots.stream()
                .map(pot -> myPotConverter.convertToOngoingPotResponseDto(pot, user.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<OngoingPotResponseDto> getMyOngoingPots() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // 내가 생성한 ONGOING 상태의 팟 조회
        List<Pot> ongoingOwnedPots = potRepository.findByUserIdAndPotStatus(user.getId(), "ONGOING");

        // DTO 변환 시 userId 추가
        return ongoingOwnedPots.stream()
                .map(pot -> myPotConverter.convertToOngoingPotResponseDto(pot, user.getId()))
                .collect(Collectors.toList());
    }


    @Transactional
    @Override
    public CompletedPotDetailResponseDto getCompletedPotDetail(Long potId) {
        // 현재 로그인한 사용자 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 팟 조회
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("해당 팟을 찾을 수 없습니다."));

        // 팟 상태 확인
        if (!"COMPLETED".equals(pot.getPotStatus())) {
            throw new IllegalArgumentException("해당 팟은 COMPLETED 상태가 아닙니다.");
        }

        // 팟 멤버에서 어필 내용 가져오기
        PotMember potMember = potMemberRepository.findByPotAndUser(pot, user)
                .orElse(null);

        String appealContent = (potMember != null) ? potMember.getAppealContent() : null;

        String userPotRole;

        // Pot 멤버의 Role 조회 후 변환
        userPotRole = potMemberRepository.findRoleByUserId(pot.getPotId(), user.getId())
                .map(role -> RoleNameMapper.getKoreanRoleName(role.name()))
                .orElse(RoleNameMapper.getKoreanRoleName(pot.getUser().getRole().name()));


        // DTO 반환
        return potDetailConverter.toCompletedPotDetailDto(pot, userPotRole, appealContent);
    }

    @Transactional
    @Override
    public List<CompletedPotBadgeResponseDto> getCompletedPotsWithBadges() {
        // 현재 인증된 사용자 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        //  사용자가 참여한 모든 COMPLETED 상태의 팟 조회 (뱃지 유무와 관계없이 가져옴)
        List<Pot> completedPots = potRepository.findCompletedPotsByUserId(user.getId());


        //  Pot -> DTO 변환
        return completedPots.stream()
                .map(pot -> {
                    //  역할별 참여자 수 조회 및 변환
                    List<Object[]> roleCounts = potMemberRepository.findRoleCountsByPotId(pot.getPotId());
                    Map<String, Integer> roleCountsMap = roleCounts.stream()
                            .collect(Collectors.toMap(
                                    roleCount -> ((Role) roleCount[0]).name(),
                                    roleCount -> ((Long) roleCount[1]).intValue()
                            ));

                    //  "프론트엔드(2), 백엔드(1)" 형식으로 변환
                    String formattedMembers = roleCountsMap.entrySet().stream()
                            .map(entry -> RoleNameMapper.getKoreanRoleName(entry.getKey()) + "(" + entry.getValue() + ")")
                            .collect(Collectors.joining(", "));

                    Role userPotRole = potMemberRepository.findRoleByUserId(pot.getPotId(), user.getId())
                            .orElse(Role.FRONTEND);

                    //  사용자의 뱃지 조회 (뱃지가 없으면 빈 리스트 반환)
                    List<BadgeDto> myBadges = potMemberBadgeRepository.findByPotMember_Pot_PotIdAndPotMember_User_Id(pot.getPotId(), user.getId())
                            .stream()
                            .map(potMemberBadge -> new BadgeDto(
                                    potMemberBadge.getBadge().getBadgeId(),
                                    potMemberBadge.getBadge().getName()
                            ))
                            .collect(Collectors.toList());

                    //  Pot -> CompletedPotBadgeResponseDto 변환
                    return myPotConverter.toCompletedPotBadgeResponseDto(pot, formattedMembers, userPotRole, myBadges);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public List<CompletedPotBadgeResponseDto> getUserCompletedPotsWithBadges(Long userId) {

        //  사용자가 참여한 모든 COMPLETED 상태의 팟 조회 (뱃지 유무와 관계없이 가져옴)
        List<Pot> completedPots = potRepository.findCompletedPotsByUserId(userId);

        //  Pot -> DTO 변환
        return completedPots.stream()
                .map(pot -> {
                    //  역할별 참여자 수 조회 및 변환
                    List<Object[]> roleCounts = potMemberRepository.findRoleCountsByPotId(pot.getPotId());
                    Map<String, Integer> roleCountsMap = roleCounts.stream()
                            .collect(Collectors.toMap(
                                    roleCount -> ((Role) roleCount[0]).name(),
                                    roleCount -> ((Long) roleCount[1]).intValue()
                            ));

                    //  "프론트엔드(2), 백엔드(1)" 형식으로 변환
                    String formattedMembers = roleCountsMap.entrySet().stream()
                            .map(entry -> RoleNameMapper.getKoreanRoleName(entry.getKey()) + "(" + entry.getValue() + ")")
                            .collect(Collectors.joining(", "));

                    Optional<Role> userPotRole = potMemberRepository.findRoleByUserId(pot.getPotId(), userId);

                    //  사용자의 뱃지 조회 (뱃지가 없으면 빈 리스트 반환)
                    List<BadgeDto> myBadges = potMemberBadgeRepository.findByPotMember_Pot_PotIdAndPotMember_User_Id(pot.getPotId(), userId)
                            .stream()
                            .map(potMemberBadge -> new BadgeDto(
                                    potMemberBadge.getBadge().getBadgeId(),
                                    potMemberBadge.getBadge().getName()
                            ))
                            .collect(Collectors.toList());

                    //  Pot -> CompletedPotBadgeResponseDto 변환
                    return myPotConverter.toCompletedPotBadgeResponseDto(pot, formattedMembers, userPotRole.orElse(null), myBadges);
                })
                .collect(Collectors.toList());
    }



    public boolean isOwner(Long potId) {
        // 현재 로그인한 사용자 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 팟 조회
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        // 소유자 여부 확인
        return pot.getUser().equals(user);
    }
}