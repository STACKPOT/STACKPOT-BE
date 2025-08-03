package stackpot.stackpot.user.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import stackpot.stackpot.badge.dto.BadgeDto;
import stackpot.stackpot.feed.converter.FeedConverter;
import stackpot.stackpot.feed.entity.Feed;
import stackpot.stackpot.feed.entity.Series;
import stackpot.stackpot.feed.repository.FeedLikeRepository;
import stackpot.stackpot.pot.converter.MyPotConverter;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.save.repository.FeedSaveRepository;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.badge.repository.PotMemberBadgeRepository;
import stackpot.stackpot.pot.repository.PotMemberRepository;
import stackpot.stackpot.user.dto.response.UserMyPageResponseDto;
import stackpot.stackpot.user.entity.enums.Role;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMypageConverter {
    private final PotMemberRepository potMemberRepository;
    private final FeedConverter feedConverter;
    private final MyPotConverter myPotConverter;
    private final PotMemberBadgeRepository potMemberBadgeRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final FeedSaveRepository feedSaveRepository;



    public UserMyPageResponseDto toDto(User user, List<Pot> completedPots, List<Feed> feeds) {

        // 현재 유저가 좋아요 누른 피드 ID 목록
        List<Long> likedFeedIds = feedLikeRepository.findFeedIdsByUserId(user.getId());

        // 현재 유저가 저장한 피드 ID 목록
        List<Long> savedFeedIds = feedSaveRepository.findFeedIdsByUserId(user.getId());

        List<String> seriesComments = user.getSeriesList().stream()
                .map(Series::getComment)
                .collect(Collectors.toList());
        return UserMyPageResponseDto.builder()
                .id(user.getId())
                .nickname(user.getNickname() + Role.toVegetable(String.valueOf(user.getRole())))
                .role(user.getRole())
                .userTemperature(user.getUserTemperature())
                .userIntroduction(user.getUserIntroduction())
                .seriesComments(seriesComments)

                .completedPots(completedPots.stream()
                        .map(pot -> {
                            List<Object[]> roleCounts = potMemberRepository.findRoleCountsByPotId(pot.getPotId());
                            Map<String, Integer> roleCountsMap = roleCounts.stream()
                                    .collect(Collectors.toMap(
                                            roleCount -> ((Role) roleCount[0]).name(),
                                            roleCount -> ((Long) roleCount[1]).intValue()
                                    ));

                            String formattedMembers = roleCountsMap.entrySet().stream()
                                    .map(entry -> Role.toKoreanName(entry.getKey()) + "(" + entry.getValue() + ")")
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
                        .map(feed -> {
                            boolean isOwner = feed.getUser().getId().equals(user.getId());
                            Boolean isLiked = likedFeedIds.contains(feed.getFeedId());
                            Boolean isSaved = savedFeedIds.contains(feed.getFeedId());
                            int saveCount = feedSaveRepository.countByFeed(feed);

                            return feedConverter.feedDto(feed, isOwner, isLiked, isSaved, saveCount);
                        })
                        .collect(Collectors.toList()))
                .build();
    }
}
