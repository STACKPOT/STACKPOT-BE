package stackpot.stackpot.feed.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.FeedHandler;
import stackpot.stackpot.feed.entity.mapping.FeedLike;
import stackpot.stackpot.feed.repository.FeedLikeRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedLikeQueryService {

    private final FeedLikeRepository feedLikeRepository;

    public FeedLike getFeedLikeById(Long likeId) {
        return feedLikeRepository.findById(likeId)
                .orElseThrow(() -> new FeedHandler(ErrorStatus.FEED_LIKE_NOT_FOUND));
    }
}
