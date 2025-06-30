package stackpot.stackpot.feed.converter;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import stackpot.stackpot.common.util.DateFormatter;
import stackpot.stackpot.common.util.RoleNameMapper;
import stackpot.stackpot.feed.entity.Feed;
import stackpot.stackpot.feed.dto.FeedRequestDto;
import stackpot.stackpot.feed.dto.FeedResponseDto;
import stackpot.stackpot.feed.dto.FeedSearchResponseDto;
import stackpot.stackpot.feed.repository.FeedLikeRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Component
public class FeedConverter{

    private final FeedLikeRepository feedLikeRepository;


    public FeedResponseDto.FeedDto feedDto(Feed feed) {
        String roleName = feed.getUser().getRole() != null
                ? feed.getUser().getRole().name()
                : "멤버";
        String nicknameWithRole = feed.getUser().getNickname() + " " + RoleNameMapper.mapRoleName(roleName);

        return FeedResponseDto.FeedDto.builder()
                .feedId(feed.getFeedId())
                .writerId(feed.getUser().getId())
                .writer(nicknameWithRole)
                .writerRole(feed.getUser().getRole())
//                .category(feed.getCategory())
                .title(feed.getTitle())
                .content(feed.getContent())
                .likeCount(feed.getLikeCount())
                .createdAt(DateFormatter.koreanFormatter(feed.getCreatedAt()))
                .isOwner(null)
                .build();
    }

    public Feed toFeed(FeedRequestDto.createDto request) {
        return Feed.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .build();
    }

    public FeedSearchResponseDto toSearchDto(Feed feed) {
        String roleName = feed.getUser().getRole() != null
                ? feed.getUser().getRole().name()
                : "멤버";
        String nicknameWithRole = feed.getUser().getNickname() + " " + RoleNameMapper.mapRoleName(roleName);

        return FeedSearchResponseDto.builder()
                .userId(feed.getUser().getId())
                .feedId(feed.getFeedId())
                .title(feed.getTitle())
                .content(feed.getContent())
                .creatorNickname(nicknameWithRole) // 닉네임과 역할 포함
                .creatorRole(roleName)
                .createdAt(DateFormatter.koreanFormatter(feed.getCreatedAt()))
                .likeCount(feed.getLikeCount()) // 좋아요 개수 포함
                .build();
    }

    public FeedResponseDto.FeedDto toAuthorizedFeedDto(Feed feed, boolean isOwner){
        String roleName = feed.getUser().getRole() != null
                ? feed.getUser().getRole().name()
                : "멤버";
        String nicknameWithRole = feed.getUser().getNickname() + " " + RoleNameMapper.mapRoleName(roleName);

        return FeedResponseDto.FeedDto.builder()
                .feedId(feed.getFeedId())
                .writerId(feed.getUser().getId())
                .writer(nicknameWithRole)
                .writerRole(feed.getUser().getRole())
                .title(feed.getTitle())
                .content(feed.getContent())
                .likeCount(feed.getLikeCount())
                .createdAt(DateFormatter.koreanFormatter(feed.getCreatedAt()))
                .isOwner(isOwner)
                .build();
    }
}