package stackpot.stackpot.feed.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.feed.converter.FeedCommentConverter;
import stackpot.stackpot.feed.dto.FeedCommentRequestDto;
import stackpot.stackpot.feed.dto.FeedCommentResponseDto;
import stackpot.stackpot.feed.entity.Feed;
import stackpot.stackpot.feed.entity.mapping.FeedComment;
import stackpot.stackpot.feed.repository.FeedCommentRepository;
import stackpot.stackpot.user.entity.User;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedCommentCommandService {

    private final FeedService feedService;
    private final FeedCommentQueryService feedCommentQueryService;
    private final FeedCommentRepository feedCommentRepository;
    private final FeedCommentConverter feedCommentConverter;
    private final AuthService authService;

    @Transactional
    public FeedCommentResponseDto.FeedCommentCreateDto createFeedComment(FeedCommentRequestDto.FeedCommentCreateDto feedCommentCreateDto) {
        User user = authService.getCurrentUser();
        Long feedId = feedCommentCreateDto.getFeedId();
        Feed feed = feedService.getFeedByFeedId(feedId);
        String comment = feedCommentCreateDto.getComment();

        FeedComment feedComment = feedCommentRepository.save(FeedComment.builder()
                .comment(comment)
                .user(user)
                .feed(feed)
                .parent(null)
                .build());
        Boolean isWriter = Objects.equals(user.getId(), feed.getUser().getUserId());
        return feedCommentConverter.toFeedCommentCreateDto(user.getUserId(), user.getNickname(), user.getRole(), isWriter,
                feedComment.getId(), comment, feedComment.getCreatedAt());
    }

    @Transactional
    public FeedCommentResponseDto.FeedReplyCommentCreateDto createFeedReplyComment(Long parentCommentId, FeedCommentRequestDto.FeedCommentCreateDto feedCommentCreateDto) {
        User user = authService.getCurrentUser();
        Long feedId = feedCommentCreateDto.getFeedId();
        Feed feed = feedService.getFeedByFeedId(feedId);
        String comment = feedCommentCreateDto.getComment();
        FeedComment parent = feedCommentQueryService.selectFeedCommentByCommentId(parentCommentId);

        FeedComment feedComment = feedCommentRepository.save(FeedComment.builder()
                .comment(comment)
                .user(user)
                .feed(feed)
                .parent(parent)
                .build());
        Boolean isWriter = Objects.equals(user.getId(), feed.getUser().getUserId());
        return feedCommentConverter.toFeedReplyCommentCreateDto(user.getUserId(), user.getNickname(), user.getRole(), isWriter,
                feedComment.getId(), comment, parent.getId(), feedComment.getCreatedAt());
    }

    @Transactional
    public FeedCommentResponseDto.FeedCommentUpdateDto updateFeedComment(Long commentId, FeedCommentRequestDto.FeedCommentUpdateDto feedCommentUpdateDto) {
        FeedComment feedComment = feedCommentQueryService.selectFeedCommentByCommentId(commentId);
        feedComment.updateComment(feedCommentUpdateDto.getComment());
        return feedCommentConverter.toFeedCommentUpdateDto(feedCommentUpdateDto.getComment());
    }

    @Transactional
    public void deleteFeedComment(Long commentId) {
        FeedComment feedComment = feedCommentQueryService.selectFeedCommentByCommentId(commentId);
        feedCommentRepository.delete(feedComment);
    }
}
