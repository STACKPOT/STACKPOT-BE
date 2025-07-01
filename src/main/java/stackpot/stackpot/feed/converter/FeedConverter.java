package stackpot.stackpot.feed.converter;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import stackpot.stackpot.common.util.DateFormatter;
import stackpot.stackpot.feed.entity.Feed;
import stackpot.stackpot.feed.dto.FeedRequestDto;
import stackpot.stackpot.feed.dto.FeedResponseDto;
import stackpot.stackpot.feed.dto.FeedSearchResponseDto;
import stackpot.stackpot.feed.entity.Series;
import stackpot.stackpot.feed.entity.enums.Interest;
import stackpot.stackpot.feed.repository.FeedLikeRepository;
import stackpot.stackpot.user.entity.User;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import static stackpot.stackpot.common.util.RoleNameMapper.mapRoleName;

@RequiredArgsConstructor
@Component
public class FeedConverter{

    public FeedResponseDto.FeedDto feedDto(Feed feed) {
        return FeedResponseDto.FeedDto.builder()
                .feedId(feed.getFeedId())
                .writerId(feed.getUser().getId())
                .writer(feed.getUser().getNickname()+" "+mapRoleName(String.valueOf(feed.getUser().getRole())))
                .writerRole(feed.getUser().getRole())
//                .category(feed.getCategory())
                .title(feed.getTitle())
                .content(feed.getContent())
                .likeCount(feed.getLikeCount())
                .createdAt(DateFormatter.koreanFormatter(feed.getCreatedAt()))
                .isOwner(null)
                .build();
    }

    public FeedResponseDto.CreatedFeedDto createFeedDto(Feed feed) {
        Map<String, Object> seriesMap = null;
        if (feed.getSeries() != null) {
            seriesMap = Map.of(
                    "seriesId", feed.getSeries().getSeriesId(),
                    "comment", feed.getSeries().getComment()
            );
        }

        return FeedResponseDto.CreatedFeedDto.builder()
                .feedId(feed.getFeedId())
                .title(feed.getTitle())
                .content(feed.getContent())
                .writerId(feed.getUser().getId())
                .writer(feed.getUser().getNickname()+" "+mapRoleName(String.valueOf(feed.getUser().getRole())))
                .writerRole(feed.getUser().getRole())
                .categories(feed.getCategories().stream()
                        .map(Enum::name)
                        .collect(Collectors.toList()))
                .interests(feed.getInterests().stream()
                        .map(Interest::getLabel) // 혹은 .name()
                        .collect(Collectors.toList()))
                .series(seriesMap)
                .likeCount(feed.getLikeCount())
                .createdAt(DateFormatter.koreanFormatter(feed.getCreatedAt()))
                .build();
    }

    public Feed toFeed(FeedRequestDto.createDto dto, Series series) {
        return Feed.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .categories(dto.getCategories() != null ? dto.getCategories() : new ArrayList<>())
                .interests(dto.getInterests() != null ? dto.getInterests() : new ArrayList<>())
                .series(series) // null일 수 있음
                .build();
    }

    public FeedSearchResponseDto toSearchDto(Feed feed) {

        return FeedSearchResponseDto.builder()
                .userId(feed.getUser().getId())
                .feedId(feed.getFeedId())
                .title(feed.getTitle())
                .content(feed.getContent())
                .creatorNickname(feed.getUser().getNickname()+" "+mapRoleName(String.valueOf(feed.getUser().getRole())))
                .creatorRole(mapRoleName(String.valueOf(feed.getUser().getRole())))
                .createdAt(DateFormatter.koreanFormatter(feed.getCreatedAt()))
                .likeCount(feed.getLikeCount()) // 좋아요 개수 포함
                .build();
    }

    public FeedResponseDto.AuthorizedFeedDto toAuthorizedFeedDto(Feed feed, boolean isOwner) {
        Map<String, Object> seriesMap = null;
        if (feed.getSeries() != null) {
            seriesMap = Map.of(
                    "seriesId", feed.getSeries().getSeriesId(),
                    "comment", feed.getSeries().getComment()
            );
        }

        FeedResponseDto.CreatedFeedDto createdDto = FeedResponseDto.CreatedFeedDto.builder()
                .feedId(feed.getFeedId())
                .writerId(feed.getUser().getId())
                .writer(feed.getUser().getNickname()+ " " +mapRoleName(String.valueOf(feed.getUser().getRole())))
                .writerRole(feed.getUser().getRole())
                .title(feed.getTitle())
                .content(feed.getContent())
                .likeCount(feed.getLikeCount())
                .createdAt(DateFormatter.koreanFormatter(feed.getCreatedAt()))
                .categories(feed.getCategories().stream().map(Enum::name).toList())
                .interests(feed.getInterests().stream().map(Interest::getLabel).toList())
                .series(seriesMap)
                .build();

        return FeedResponseDto.AuthorizedFeedDto.builder()
                .feed(createdDto)
                .isOwner(isOwner)
                .build();
    }

    public Series toEntity(String comment, User user) {
        return Series.builder()
                .comment(comment)
                .user(user)
                .build();
    }
}