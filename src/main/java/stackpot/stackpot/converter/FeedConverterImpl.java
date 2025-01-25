package stackpot.stackpot.converter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.domain.Feed;
import stackpot.stackpot.web.dto.FeedRequestDto;
import stackpot.stackpot.web.dto.FeedResponseDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.apache.tomcat.util.http.FastHttpDateFormat.formatDate;

@Component
public class FeedConverterImpl implements FeedConverter{

    @Override
    public FeedResponseDto.FeedDto feedDto(Feed feed, long popularity, long likeCount) {
        return FeedResponseDto.FeedDto.builder()
                .id(feed.getFeedId())
                .writer(feed.getUser().getNickname())
                .category(feed.getCategory())
                .title(feed.getTitle())
                .content(feed.getContent())
                .popularity(popularity)
                .likeCount(likeCount)
                .createdAt(formatLocalDateTime(feed.getCreatedAt()))
                .build();
    }

    @Override
    public Feed toFeed(FeedRequestDto.createDto request) {
        return Feed.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategor())
                .visibility(request.getVisibility())
                .build();
    }

    // 날짜 포맷 적용 메서드
    private String formatLocalDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 H:mm");
        return (dateTime != null) ? dateTime.format(formatter) : "날짜 없음";
    }

}
