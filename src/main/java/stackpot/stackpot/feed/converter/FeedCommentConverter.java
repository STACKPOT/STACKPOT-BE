package stackpot.stackpot.feed.converter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.feed.dto.FeedCommentDto;
import stackpot.stackpot.feed.dto.FeedCommentResponseDto;
import stackpot.stackpot.user.entity.enums.Role;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

@Component
public class FeedCommentConverter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 M월 d일 H:mm");

    public FeedCommentResponseDto.AllFeedCommentDto toAllFeedCommentDto(FeedCommentDto.FeedCommentInfoDto dto, Long currentUserId) {
        return FeedCommentResponseDto.AllFeedCommentDto.builder()
                .userId(dto.getUserId())
                .userName(dto.getUserName() + " 새싹")
                .isCommentWriter(Objects.equals(dto.getUserId(), currentUserId))
                .isFeedWriter(Objects.equals(dto.getFeedWriterId(), dto.getUserId()))
                .commentId(dto.getCommentId())
                .comment(dto.getComment())
                .parentCommentId(dto.getParentCommentId())
                .createdAt(dto.getCreatedAt().format(DATE_FORMATTER))
                .children(new ArrayList<>())
                .build();
    }

    public FeedCommentResponseDto.FeedCommentCreateDto toFeedCommentCreateDto(Long userId, String userName, Boolean isWriter,
                                                                              Long commentId, String comment, LocalDateTime createdAt) {
        return FeedCommentResponseDto.FeedCommentCreateDto.builder()
                .userId(userId)
                .userName(userName + " 새싹")
                .isWriter(isWriter)
                .commentId(commentId)
                .comment(comment)
                .createdAt(createdAt)
                .build();
    }

    public FeedCommentResponseDto.FeedReplyCommentCreateDto toFeedReplyCommentCreateDto(Long userId, String userName, Boolean isWriter,
                                                                                        Long commentId, String comment, Long parentCommentId,
                                                                                        LocalDateTime createdAt) {
        return FeedCommentResponseDto.FeedReplyCommentCreateDto.builder()
                .userId(userId)
                .userName(userName + " 새싹")
                .isWriter(isWriter)
                .commentId(commentId)
                .comment(comment)
                .parentCommentId(parentCommentId)
                .createdAt(createdAt)
                .build();
    }

    public FeedCommentResponseDto.FeedCommentUpdateDto toFeedCommentUpdateDto(String comment) {
        return FeedCommentResponseDto.FeedCommentUpdateDto.builder()
                .comment(comment)
                .build();
    }
}
