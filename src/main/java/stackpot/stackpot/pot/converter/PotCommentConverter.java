package stackpot.stackpot.pot.converter;

import org.springframework.stereotype.Component;
import stackpot.stackpot.pot.dto.PotCommentDto;
import stackpot.stackpot.pot.dto.PotCommentResponseDto;
import stackpot.stackpot.user.entity.enums.Role;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

@Component
public class PotCommentConverter {

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

    public PotCommentResponseDto.AllPotCommentDto toAllPotCommentDto(PotCommentDto.PotCommentInfoDto dto) {
        return PotCommentResponseDto.AllPotCommentDto.builder()
                .userId(dto.getUserId())
                .userName(dto.getUserName())
                .role(dto.getRole())
                .isWriter(Objects.equals(dto.getWriterId(), dto.getUserId()))
                .commentId(dto.getCommentId())
                .comment(dto.getComment())
                .parentCommentId(dto.getParentCommentId())
                .createdAt(dto.getCreatedAt())
                .children(new ArrayList<>())
                .build();
    }
}
