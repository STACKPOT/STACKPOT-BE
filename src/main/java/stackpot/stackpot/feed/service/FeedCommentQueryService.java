package stackpot.stackpot.feed.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.FeedCommentHandler;
import stackpot.stackpot.apiPayload.exception.handler.FeedHandler;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.feed.converter.FeedCommentConverter;
import stackpot.stackpot.feed.dto.FeedCommentDto;
import stackpot.stackpot.feed.dto.FeedCommentResponseDto;
import stackpot.stackpot.feed.entity.mapping.FeedComment;
import stackpot.stackpot.feed.repository.FeedCommentRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedCommentQueryService {

    private final FeedCommentRepository feedCommentRepository;
    private final FeedCommentConverter feedCommentConverter;
    private final AuthService authService;

    public FeedComment selectFeedCommentByCommentId(Long commentId) {
        return feedCommentRepository.findByCommentId(commentId).orElseThrow(() -> new FeedCommentHandler(ErrorStatus.FEED_COMMENT_NOT_FOUND));
    }

    public List<FeedCommentResponseDto.AllFeedCommentDto> selectAllFeedComments(Long feedId) {
        Long userId = authService.getCurrentUserId();
        List<FeedCommentDto.FeedCommentInfoDto> dtos = feedCommentRepository.findAllCommentInfoDtoByFeedId(feedId);
        dtos.sort(Comparator.comparing(FeedCommentDto.FeedCommentInfoDto::getCommentId));

        Map<Long, FeedCommentResponseDto.AllFeedCommentDto> map = new HashMap<>();
        List<FeedCommentResponseDto.AllFeedCommentDto> result = new ArrayList<>();

        for (FeedCommentDto.FeedCommentInfoDto dto : dtos) {
            map.put(dto.getCommentId(), feedCommentConverter.toAllFeedCommentDto(dto, userId));
        }
        for (FeedCommentDto.FeedCommentInfoDto dto : dtos) {
            FeedCommentResponseDto.AllFeedCommentDto current = map.get(dto.getCommentId());
            Long parentId = current.getParentCommentId();

            if (parentId == null) {
                result.add(current);
                continue;
            }

            FeedCommentResponseDto.AllFeedCommentDto parent = map.get(parentId);
            if (parent != null) {
                parent.getChildren().add(current);
            }
        }

        sortChildrenRecursively(result);
        return result;
    }

    private void sortChildrenRecursively(List<FeedCommentResponseDto.AllFeedCommentDto> comments) {
        for (FeedCommentResponseDto.AllFeedCommentDto comment : comments) {
            comment.getChildren().sort(Comparator.comparing(FeedCommentResponseDto.AllFeedCommentDto::getCommentId));
            sortChildrenRecursively(comment.getChildren());
        }
    }
}
