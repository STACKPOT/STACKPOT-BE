package stackpot.stackpot.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import stackpot.stackpot.domain.Feed;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.repository.BadgeRepository.PotMemberBadgeRepository;
import stackpot.stackpot.repository.FeedLikeRepository;
import stackpot.stackpot.repository.PotMemberRepository;
import stackpot.stackpot.web.dto.*;
import stackpot.stackpot.domain.enums.Role;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mysql.cj.util.TimeUtil.DATE_FORMATTER;

@Component
@RequiredArgsConstructor
public class UserMypageConverter {
    private final PotMemberRepository potMemberRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final FeedConverter feedConverter;
    private final MyPotConverter myPotConverter;
    private final PotMemberBadgeRepository potMemberBadgeRepository;


    public UserMyPageResponseDto toDto(User user, List<Pot> completedPots, List<Feed> feeds) {
        return UserMyPageResponseDto.builder()
                .id(user.getId())
                .nickname(user.getNickname() + getVegetableNameByRole(String.valueOf(user.getRole())))
                .role(user.getRole())
                .userTemperature(user.getUserTemperature())
                .userIntroduction(user.getUserIntroduction())
                .completedPots(completedPots.stream()
                        .map(pot -> {
                            // 기존 로직 활용
                            List<Object[]> roleCounts = potMemberRepository.findRoleCountsByPotId(pot.getPotId());
                            Map<String, Integer> roleCountsMap = roleCounts.stream()
                                    .collect(Collectors.toMap(
                                            roleCount -> ((Role) roleCount[0]).name(),
                                            roleCount -> ((Long) roleCount[1]).intValue()
                                    ));

                            String formattedMembers = roleCountsMap.entrySet().stream()
                                    .map(entry -> getKoreanRoleName(entry.getKey()) + "(" + entry.getValue() + ")")
                                    .collect(Collectors.joining(", "));

                            Role userPotRole = pot.getUser().getId().equals(user.getId()) ?
                                    pot.getUser().getRole() :
                                    potMemberRepository.findRoleByUserId(pot.getPotId(), user.getId())
                                            .orElse(pot.getUser().getRole());

                            List<BadgeDto> myBadges = potMemberBadgeRepository
                                    .findByPotMember_Pot_PotIdAndPotMember_User_Id(pot.getPotId(), user.getId())
                                    .stream()
                                    .map(potMemberBadge -> new BadgeDto(
                                            potMemberBadge.getBadge().getBadgeId(),
                                            potMemberBadge.getBadge().getName()
                                    ))
                                    .collect(Collectors.toList());

                            return myPotConverter.toCompletedPotBadgeResponseDto(pot, formattedMembers, userPotRole, myBadges);
                        })
                        .collect(Collectors.toList()))
                .feeds(feeds.stream()
                        .map(feedConverter::feedDto)
                        .collect(Collectors.toList()))
                .build();
    }


    private String getVegetableNameByRole(String role) {
        Map<String, String> roleToVegetableMap = Map.of(
                "BACKEND", " 양파",
                "FRONTEND", " 버섯",
                "DESIGN", " 브로콜리",
                "PLANNING", " 당근",
                "UNKNOWN",""
        );
        return roleToVegetableMap.getOrDefault(role, "알 수 없음");
    }

    private String getKoreanRoleName(String role) {
        Map<String, String> roleToKoreanMap = Map.of(
                "BACKEND", "백엔드",
                "FRONTEND", "프론트엔드",
                "DESIGN", "디자인",
                "PLANNING", "기획"
        );
        return roleToKoreanMap.getOrDefault(role, "알 수 없음");
    }
}
