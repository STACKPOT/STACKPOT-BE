package stackpot.stackpot.pot.converter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.pot.dto.PotCommentDto;
import stackpot.stackpot.pot.dto.PotCommentResponseDto;
import stackpot.stackpot.user.entity.enums.Role;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

@Component
public class PotCommentConverter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 M월 d일 H:mm");

    public PotCommentResponseDto.PotCommentCreateDto toPotCommentCreateDto(Long userId, String userName, Role role, Boolean isWriter,
                                                                           Long commentId, String comment, LocalDateTime createdAt) {
        return PotCommentResponseDto.PotCommentCreateDto.builder()
                .userId(userId)
                .userName(userName)
                .role(role)
                .isWriter(isWriter)
                .commentId(commentId)
                .comment(comment)
                .createdAt(createdAt)
                .build();
    }

    public PotCommentResponseDto.PotReplyCommentCreateDto toPotReplyCommentCreateDto(Long userId, String userName, Role role, Boolean isWriter,
                                                                                     Long commentId, String comment, Long parentCommentId,
                                                                                     LocalDateTime createdAt) {
        return PotCommentResponseDto.PotReplyCommentCreateDto.builder()
                .userId(userId)
                .userName(userName)
                .role(role)
                .isWriter(isWriter)
                .commentId(commentId)
                .comment(comment)
                .parentCommentId(parentCommentId)
                .createdAt(createdAt)
                .build();
    }

    public PotCommentResponseDto.PotCommentUpdateDto toPotCommentUpdateDto(String comment) {
        return PotCommentResponseDto.PotCommentUpdateDto.builder()
                .comment(comment)
                .build();
    }

    public PotCommentResponseDto.AllPotCommentDto toAllPotCommentDto(PotCommentDto.PotCommentInfoDto dto, Long currentUserId) {
        return PotCommentResponseDto.AllPotCommentDto.builder()
                .userId(dto.getUserId())
                .userName(dto.getUserName())
                .role(dto.getRole())
                .isCommentWriter(Objects.equals(dto.getUserId(), currentUserId))
                .isPotWriter(Objects.equals(dto.getPotWriterId(), dto.getUserId()))
                .commentId(dto.getCommentId())
                .comment(dto.getComment())
                .parentCommentId(dto.getParentCommentId())
                .createdAt(dto.getCreatedAt().format(DATE_FORMATTER))
                .children(new ArrayList<>())
                .build();
    }
}
