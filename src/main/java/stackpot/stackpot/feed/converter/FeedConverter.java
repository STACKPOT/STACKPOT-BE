package stackpot.stackpot.feed.converter;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import stackpot.stackpot.common.util.DateFormatter;
import stackpot.stackpot.common.util.RoleNameMapper;
import stackpot.stackpot.feed.dto.FeedCacheDto;
import stackpot.stackpot.feed.entity.Feed;
import stackpot.stackpot.feed.dto.FeedRequestDto;
import stackpot.stackpot.feed.dto.FeedResponseDto;
import stackpot.stackpot.feed.entity.Series;
import stackpot.stackpot.feed.entity.enums.Interest;
import stackpot.stackpot.feed.repository.FeedCommentRepository;
import stackpot.stackpot.user.entity.User;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class FeedConverter{

    private final FeedCommentRepository feedCommentRepository;

    public FeedResponseDto.FeedDto feedDto(Feed feed, Boolean isOwner, Boolean isLiked, Boolean isSaved, long saveCount) {

        Long commentCount = feedCommentRepository.countByFeedId(feed.getFeedId());
        String writerNickname = RoleNameMapper.getWriterNickname(feed.getUser());

        return FeedResponseDto.FeedDto.builder()
                .feedId(feed.getFeedId())
                .writerId(feed.getUser().getId())
                .writer(writerNickname)
                .writerRoles(feed.getUser().getRoleNames())
                .title(feed.getTitle())
                .content(feed.getContent())
                .likeCount(feed.getLikeCount())
                .isLiked(isLiked)
                .saveCount(saveCount)
                .isSaved(isSaved)
                .commentCount(commentCount)
                .createdAt(DateFormatter.koreanFormatter(feed.getCreatedAt()))
                .isOwner(isOwner)
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
        Long commentCount = feedCommentRepository.countByFeedId(feed.getFeedId());


        return FeedResponseDto.CreatedFeedDto.builder()
                .feedId(feed.getFeedId())
                .title(feed.getTitle())
                .content(feed.getContent())
                .writerId(feed.getUser().getId())
                .writer(feed.getUser().getNickname()+ " 새싹")
                .writerRoles(feed.getUser().getRoleNames())
                .categories(feed.getCategories().stream()
                        .map(Enum::name)
                        .collect(Collectors.toList()))
                .interests(feed.getInterests().stream()
                        .map(Interest::getLabel) // 혹은 .name()
                        .collect(Collectors.toList()))
                .series(seriesMap)
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

    public FeedResponseDto.AuthorizedFeedDto toAuthorizedFeedDto(Feed feed, boolean isOwner, boolean isLiked, boolean isSaved, Long commentCount) {
        Map<String, Object> seriesMap = null;
        if (feed.getSeries() != null) {
            seriesMap = Map.of(
                    "seriesId", feed.getSeries().getSeriesId(),
                    "comment", feed.getSeries().getComment()
            );
        }
        String writerNickname = RoleNameMapper.getWriterNickname(feed.getUser());
        FeedResponseDto.CreatedFeedDto createdDto = FeedResponseDto.CreatedFeedDto.builder()
                .feedId(feed.getFeedId())
                .writerId(feed.getUser().getId())
                .writer(writerNickname)
                .writerRoles(feed.getUser().getRoleNames())
                .title(feed.getTitle())
                .content(feed.getContent())
                .createdAt(DateFormatter.koreanFormatter(feed.getCreatedAt()))
                .categories(feed.getCategories().stream().map(Enum::name).toList())
                .interests(feed.getInterests().stream().map(Interest::getLabel).toList())
                .series(seriesMap)
                .build();

        return FeedResponseDto.AuthorizedFeedDto.builder()
                .feed(createdDto)
                .isOwner(isOwner)
                .isLiked(isLiked)
                .isSaved(isSaved)
                .commentCount(commentCount)
                .build();
    }

    public Series toEntity(String comment, User user) {
        return Series.builder()
                .comment(comment)
                .user(user)
                .build();
    }
    public FeedResponseDto.FeedDto toFeedDtoFromCache(FeedCacheDto feed, boolean isLiked, boolean isSaved, long likeCount, long saveCount, long commentCount, boolean isOwner) {
        return FeedResponseDto.FeedDto.builder()
                .feedId(feed.getFeedId())
                .writerId(feed.getUserId())
                .writer(feed.getWriter())
                .writerRoles(feed.getWriterRoles())
                .title(feed.getTitle())
                .content(feed.getContent())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .saveCount(saveCount)
                .isLiked(isLiked)
                .isSaved(isSaved)
                .createdAt(feed.getCreatedAt())
                .isOwner(isOwner)
                .build();
    }
    public FeedCacheDto toFeedCacheDto(Feed feed) {
        return FeedCacheDto.builder()
                .feedId(feed.getFeedId())
                .userId(feed.getUser().getId())
                .writer(feed.getUser().getNickname())
                .writerRoles(feed.getUser().getRoleNames())
                .title(feed.getTitle())
                .content(feed.getContent())
                .createdAt(feed.getCreatedAt().toString())
                .build();
    }



}