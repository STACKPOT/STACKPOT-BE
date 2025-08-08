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
    private final FeedConverter feedConverter;
    private final FeedLikeRepository feedLikeRepository;
    private final FeedSaveRepository feedSaveRepository;



    public UserMyPageResponseDto toDto(User user, List<Feed> feeds) {

        // 현재 유저가 좋아요 누른 피드 ID 목록
        List<Long> likedFeedIds = feedLikeRepository.findFeedIdsByUserId(user.getId());

        // 현재 유저가 저장한 피드 ID 목록
        List<Long> savedFeedIds = feedSaveRepository.findFeedIdsByUserId(user.getId());

        // 시리즈 코멘트
        List<String> seriesComments = user.getSeriesList().stream()
                .map(Series::getComment)
                .collect(Collectors.toList());

        return UserMyPageResponseDto.builder()
                .id(user.getId())
                .seriesComments(seriesComments)



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
