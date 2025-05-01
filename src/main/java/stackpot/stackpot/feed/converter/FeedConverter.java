package stackpot.stackpot.feed.converter;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import stackpot.stackpot.feed.entity.Feed;
import stackpot.stackpot.feed.dto.FeedRequestDto;
import stackpot.stackpot.feed.dto.FeedResponseDto;
import stackpot.stackpot.feed.dto.FeedSearchResponseDto;
import stackpot.stackpot.feed.repository.FeedLikeRepository;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Component
public class FeedConverter{

    private final FeedLikeRepository feedLikeRepository;

    public FeedResponseDto.FeedDto feedDto(Feed feed) {
        return FeedResponseDto.FeedDto.builder()
                .feedId(feed.getFeedId())
                .writerId(feed.getUser().getId())
                .writer(feed.getUser().getNickname()+""+mapRoleName(String.valueOf(feed.getUser().getRole())))
                .writerRole(feed.getUser().getRole())
//                .category(feed.getCategory())
                .title(feed.getTitle())
                .content(feed.getContent())
                .likeCount(feed.getLikeCount())
                .createdAt(formatLocalDateTime(feed.getCreatedAt()))
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

    // 날짜 포맷 적용 메서드
    private String formatLocalDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 H:mm");
        return (dateTime != null) ? dateTime.format(formatter) : "날짜 없음";
    }


    public FeedSearchResponseDto toSearchDto(Feed feed) {
        // 역할 이름 매핑 (유효한 역할만 처리)
        String roleName = feed.getUser().getRole() != null ? feed.getUser().getRole().name() : "멤버";
        String nicknameWithRole = feed.getUser().getNickname() + mapRoleName(roleName) ;

        return FeedSearchResponseDto.builder()
                .userId(feed.getUser().getId())
                .feedId(feed.getFeedId())
                .title(feed.getTitle())
                .content(feed.getContent())
                .creatorNickname(nicknameWithRole) // 닉네임과 역할 포함
                .creatorRole(roleName)
                .createdAt(formatLocalDateTime(feed.getCreatedAt())) // 시간 포맷 적용
                .likeCount(feed.getLikeCount()) // 좋아요 개수 포함
                .build();
    }

    public FeedResponseDto.FeedDto toAuthorizedFeedDto(Feed feed, boolean isOwner){
        return FeedResponseDto.FeedDto.builder()
                .feedId(feed.getFeedId())
                .writerId(feed.getUser().getId())
                .writer(feed.getUser().getNickname()+""+mapRoleName(String.valueOf(feed.getUser().getRole())))
                .writerRole(feed.getUser().getRole())
                .title(feed.getTitle())
                .content(feed.getContent())
                .likeCount(feed.getLikeCount())
                .createdAt(formatLocalDateTime(feed.getCreatedAt()))
                .isOwner(isOwner)
                .build();
    }

    private String mapRoleName(String roleName) {
        return switch (roleName) {
            case "BACKEND" -> " 양파";
            case "FRONTEND" -> " 버섯";
            case "DESIGN" -> " 브로콜리";
            case "PLANNING" -> " 당근";
            default -> "멤버";
        };
    }
}