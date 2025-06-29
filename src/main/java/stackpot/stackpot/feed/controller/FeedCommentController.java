package stackpot.stackpot.feed.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.common.swagger.ApiErrorCodeExamples;
import stackpot.stackpot.feed.dto.FeedCommentRequestDto;
import stackpot.stackpot.feed.dto.FeedCommentResponseDto;
import stackpot.stackpot.feed.service.FeedCommentCommandService;
import stackpot.stackpot.feed.service.FeedCommentQueryService;

import java.util.List;

@RestController
@RequestMapping("/feed-comments")
@RequiredArgsConstructor
public class FeedCommentController {

    private final FeedCommentCommandService feedCommentCommandService;
    private final FeedCommentQueryService feedCommentQueryService;

    @Operation(summary = "피드의 모든 댓글 조회 API")
    @ApiErrorCodeExamples({
    })
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<FeedCommentResponseDto.AllFeedCommentDto>>> selectFeedComments(@RequestParam("feedId") Long feedId) {
        List<FeedCommentResponseDto.AllFeedCommentDto> result = feedCommentQueryService.selectAllFeedComments(feedId);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }

    @Operation(summary = "피드 댓글 달기 API")
    @ApiErrorCodeExamples({
            ErrorStatus.AUTHENTICATION_FAILED,
            ErrorStatus.USER_NOT_FOUND,
            ErrorStatus.FEED_NOT_FOUND,
    })
    @PostMapping("")
    public ResponseEntity<ApiResponse<FeedCommentResponseDto.FeedCommentCreateDto>> createFeedComment(@RequestBody FeedCommentRequestDto.FeedCommentCreateDto feedCommentCreateDto) {
        FeedCommentResponseDto.FeedCommentCreateDto result = feedCommentCommandService.createFeedComment(feedCommentCreateDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }

    @Operation(summary = "피드 대댓글 달기 API")
    @ApiErrorCodeExamples({
            ErrorStatus.AUTHENTICATION_FAILED,
            ErrorStatus.USER_NOT_FOUND,
            ErrorStatus.FEED_NOT_FOUND,
            ErrorStatus.FEED_COMMENT_NOT_FOUND
    })
    @PostMapping("/{parentCommentId}/replies")
    public ResponseEntity<ApiResponse<FeedCommentResponseDto.FeedReplyCommentCreateDto>> createFeedReplyComment(@PathVariable("parentCommentId") Long commentId,
                                                                                                                @RequestBody FeedCommentRequestDto.FeedCommentCreateDto feedCommentCreateDto) {
        FeedCommentResponseDto.FeedReplyCommentCreateDto result = feedCommentCommandService.createFeedReplyComment(commentId, feedCommentCreateDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }

    @Operation(summary = "피드 댓글/대댓글 수정 API")
    @ApiErrorCodeExamples({
            ErrorStatus.FEED_COMMENT_NOT_FOUND
    })
    @PatchMapping("/{commentId}")
    public ResponseEntity<ApiResponse<FeedCommentResponseDto.FeedCommentUpdateDto>> updateFeedComment(@PathVariable("commentId") Long commentId,
                                                                                                      @RequestBody FeedCommentRequestDto.FeedCommentUpdateDto feedCommentUpdateDto) {
        FeedCommentResponseDto.FeedCommentUpdateDto result = feedCommentCommandService.updateFeedComment(commentId, feedCommentUpdateDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }

    @Operation(summary = "피드 댓글/대댓글 삭제 API")
    @ApiErrorCodeExamples({
            ErrorStatus.FEED_COMMENT_NOT_FOUND
    })
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteFeedComment(@PathVariable("commentId") Long commentId) {
        feedCommentCommandService.deleteFeedComment(commentId);
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }
}
