package stackpot.stackpot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import stackpot.stackpot.converter.FeedConverter;
import stackpot.stackpot.converter.FeedConverterImpl;
import stackpot.stackpot.domain.Feed;
import stackpot.stackpot.repository.FeedRepository.FeedRepository;
import stackpot.stackpot.web.dto.FeedResponseDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final FeedRepository feedRepository;
    private final FeedConverter feedConverter;

    @Override
    public FeedResponseDto.FeedResponse getPreViewFeeds(String mainPart, String sort, String cursor, int limit) {
        // 커서가 없으면 현재 시간 사용
        LocalDateTime lastCreatedAt = cursor != null
                ? LocalDateTime.parse(cursor)
                : LocalDateTime.now();

        // Pageable 생성
        Pageable pageable = PageRequest.of(0, limit);

        // 데이터 조회
        List<Object[]> feedResults = feedRepository.findFeeds(mainPart, sort, lastCreatedAt, pageable);

        // Feed와 인기 점수를 DTO로 변환
        List<FeedResponseDto.FeedDto> feedDtoList = feedResults.stream()
                .map(result -> {
                    Feed feed = (Feed) result[0];
                    int popularity = (int) result[1];
                    int likeCount = (int) result[2];

                    return feedConverter.feedDto(feed, popularity, likeCount);
                })
                .collect(Collectors.toList());

        // 다음 커서 계산
        String nextCursor = feedResults.isEmpty()
                ? null
                : ((Feed) feedResults.get(feedResults.size() - 1)[0]).getCreatedAt().toString();

        return new FeedResponseDto.FeedResponse(feedDtoList, nextCursor);
    }
}