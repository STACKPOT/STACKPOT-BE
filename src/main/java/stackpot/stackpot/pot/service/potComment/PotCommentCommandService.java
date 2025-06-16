package stackpot.stackpot.pot.service.potComment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.pot.converter.PotCommentConverter;
import stackpot.stackpot.pot.dto.PotCommentRequestDto;
import stackpot.stackpot.pot.dto.PotCommentResponseDto;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.pot.entity.mapping.PotComment;
import stackpot.stackpot.pot.repository.PotCommentRepository;
import stackpot.stackpot.pot.service.pot.PotQueryService;
import stackpot.stackpot.user.entity.User;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PotCommentCommandService {

    private final PotQueryService potQueryService;
    private final PotCommentQueryService potCommentQueryService;
    private final PotCommentRepository potCommentRepository;
    private final PotCommentConverter potCommentConverter;
    private final AuthService authService;

    @Transactional
    public PotCommentResponseDto.PotCommentCreateDto createPotComment(PotCommentRequestDto.PotCommentCreateDto potCommentCreateDto) {
        User user = authService.getCurrentUser();
        Long potId = potCommentCreateDto.getPotId();
        Pot pot = potQueryService.getPotByPotId(potId);
        String comment = potCommentCreateDto.getComment();

        PotComment potComment = potCommentRepository.save(PotComment.builder()
                .comment(comment)
                .user(user)
                .pot(pot)
                .parent(null)
                .build());
        Boolean isWriter = Objects.equals(user.getId(), pot.getUser().getUserId());
        return potCommentConverter.toPotCommentCreateDto(user.getUserId(), user.getNickname(), user.getRole(), isWriter,
                potComment.getId(), comment, potComment.getCreatedAt());
    }

    @Transactional
    public PotCommentResponseDto.PotReplyCommentCreateDto createPotReplyComment(Long parentCommentId, PotCommentRequestDto.PotCommentCreateDto potCommentCreateDto) {
        User user = authService.getCurrentUser();
        Long potId = potCommentCreateDto.getPotId();
        Pot pot = potQueryService.getPotByPotId(potId);
        String comment = potCommentCreateDto.getComment();
        PotComment parent = potCommentQueryService.selectPotCommentByCommentId(parentCommentId);

        PotComment potComment = potCommentRepository.save(PotComment.builder()
                .comment(comment)
                .user(user)
                .pot(pot)
                .parent(parent)
                .build());
        Boolean isWriter = Objects.equals(user.getId(), pot.getUser().getUserId());
        return potCommentConverter.toPotReplyCommentCreateDto(user.getUserId(), user.getNickname(), user.getRole(), isWriter,
                potComment.getId(), comment, parent.getId(), potComment.getCreatedAt());
    }

    @Transactional
    public PotCommentResponseDto.PotCommentUpdateDto updatePotComment(Long commentId, PotCommentRequestDto.PotCommentUpdateDto potCommentUpdateDto) {
        PotComment potComment = potCommentQueryService.selectPotCommentByCommentId(commentId);
        potComment.updateComment(potCommentUpdateDto.getComment());
        return potCommentConverter.toPotCommentUpdateDto(potCommentUpdateDto.getComment());
    }

    @Transactional
    public void deletePotComment(Long commentId) {
        PotComment potComment = potCommentQueryService.selectPotCommentByCommentId(commentId);
        potCommentRepository.delete(potComment);
    }
}
