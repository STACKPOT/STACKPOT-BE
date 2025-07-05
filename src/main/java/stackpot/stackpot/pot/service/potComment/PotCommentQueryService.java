package stackpot.stackpot.pot.service.potComment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.PotCommentHandler;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.pot.converter.PotCommentConverter;
import stackpot.stackpot.pot.dto.PotCommentDto;
import stackpot.stackpot.pot.dto.PotCommentResponseDto;
import stackpot.stackpot.pot.entity.mapping.PotComment;
import stackpot.stackpot.pot.repository.PotCommentRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PotCommentQueryService {

    private final PotCommentRepository potCommentRepository;
    private final PotCommentConverter potCommentConverter;
    private final AuthService authService;

    public PotComment selectPotCommentByCommentId(Long commentId) {
        return potCommentRepository.findByCommentId(commentId).orElseThrow(() -> new PotCommentHandler(ErrorStatus.POT_COMMENT_NOT_FOUND));
    }

    public List<PotCommentResponseDto.AllPotCommentDto> selectAllPotComments(Long potId) {
        Long userId = authService.getCurrentUserId();
        List<PotCommentDto.PotCommentInfoDto> dtos = potCommentRepository.findAllCommentInfoDtoByPotId(potId);
        dtos.sort(Comparator.comparing(PotCommentDto.PotCommentInfoDto::getCommentId));

        Map<Long, PotCommentResponseDto.AllPotCommentDto> map = new HashMap<>();
        List<PotCommentResponseDto.AllPotCommentDto> result = new ArrayList<>();

        // 계층구조로 변환하기
        for (PotCommentDto.PotCommentInfoDto dto : dtos) {
            map.put(dto.getCommentId(), potCommentConverter.toAllPotCommentDto(dto, userId));
        }
        for (PotCommentDto.PotCommentInfoDto dto : dtos) {
            PotCommentResponseDto.AllPotCommentDto current = map.get(dto.getCommentId());
            Long parentId = dto.getParentCommentId();

            if (parentId == null) {
                result.add(current);
                continue;
            }

            PotCommentResponseDto.AllPotCommentDto parent = map.get(parentId);
            if (parent != null) {
                parent.getChildren().add(current);
            }
        }

        sortChildrenRecursively(result);
        return result;
    }

    private void sortChildrenRecursively(List<PotCommentResponseDto.AllPotCommentDto> comments) {
        for (PotCommentResponseDto.AllPotCommentDto comment : comments) {
            comment.getChildren().sort(Comparator.comparing(PotCommentResponseDto.AllPotCommentDto::getCommentId));
            sortChildrenRecursively(comment.getChildren());
        }
    }
}
