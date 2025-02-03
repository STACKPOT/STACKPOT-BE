package stackpot.stackpot.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.converter.FeedConverter;
import stackpot.stackpot.domain.Feed;
import stackpot.stackpot.domain.enums.Category;
import stackpot.stackpot.service.FeedService;
import stackpot.stackpot.web.dto.*;

import java.util.Map;


@RestController
@RequestMapping("/feeds")
@RequiredArgsConstructor
@Tag(name = "Feed Management", description = "피드 관리 API")
public class FeedController {

    private final FeedService feedService;
    private final FeedConverter feedConverter;


    @PostMapping("")
    @Operation(summary = "Feed 생성 API", description = "새로운 Feed를 작성합니다.")
    public ResponseEntity<ApiResponse<FeedResponseDto.FeedDto>> createFeeds(@Valid @RequestBody FeedRequestDto.createDto requset) {
        // 정상 처리
        Feed feed = feedService.createFeed(requset);

        FeedResponseDto.FeedDto response = feedConverter.feedDto(feed);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @GetMapping("")
    @Operation(summary = "Feed 전체 조회 API", description = "category와 sort에 따라 정렬하여 Feed를 보여줍니다. 커서 기반 페이지페니션으로 응답합니다.",
        parameters = {
                @Parameter(name = "category", description = "ALL : 전체 보기, PLANNING/DESIGN/FRONTEND/BACKEND : 역할별로 보기 ", example = "BACKEND"),
                @Parameter(name = "sort", description = "new : 최신순, old : 오래된순, popular : 인기순(좋아요)", example = "old"),
                @Parameter(name = "cursor", description = "현재 페이지의 마지막 값"),
                @Parameter(name = "limit", description = "요청에 불러올 Feed 수", example = "10")
        })
    public ResponseEntity<ApiResponse<FeedResponseDto.FeedPreviewList>> getPreViewFeeds(
            @RequestParam(value = "category", required = false, defaultValue = "ALL") Category category,
            @RequestParam(value = "sort", required = false, defaultValue = "new") String sort,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        FeedResponseDto.FeedPreviewList response = feedService.getPreViewFeeds(String.valueOf(category), sort, cursor, limit);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
    @PostMapping("/{feedId}")
    @Operation(summary = "Feed 상세 조회 API", description = "요청된 FeedId의 Feed를 보여줍니다.",
        parameters = {
            @Parameter(name = "feedId", description = "상세 조회 feedId")
        })
    public ResponseEntity<ApiResponse<FeedResponseDto.FeedDto>> getDetailFeed(@PathVariable Long feedId) {

        Feed feed = feedService.getFeed(feedId);

        FeedResponseDto.FeedDto response = feedConverter.feedDto(feed);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @PatchMapping("/{feedId}")
    @Operation(summary = "Feed 수정 API", description = "요청된 feedId의 feed 내용을 수정합니다.",
        parameters = {
            @Parameter(name = "feedId", description = "수정 feedId")
        })
    public ResponseEntity<ApiResponse<FeedResponseDto.FeedDto>> modifyFeed(@PathVariable Long feedId, @Valid @RequestBody FeedRequestDto.createDto requset) {
        // 정상 처리
        Feed feed = feedService.modifyFeed(feedId, requset);
        Long likeCount = feedService.getLikeCount(feedId);

        FeedResponseDto.FeedDto response =feedConverter.feedDto(feed);

        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
    @PostMapping("/{feedId}/like")
    @Operation(summary = "Feed 좋아요 API", description = "feed 좋아요를 추가합니다.")
    public ResponseEntity<ApiResponse<Map>> toggleLike(@PathVariable Long feedId) {

        // 좋아요 토글
        boolean isLiked = feedService.toggleLike(feedId);
        return ResponseEntity.ok(ApiResponse.onSuccess(Map.of(
                "liked", isLiked,
                "message", isLiked ? "좋아요를 눌렀습니다." : "좋아요를 취소했습니다."
        )));
    }
    @Operation(summary = "사용자별 Feed 조회 API")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<FeedResponseDto.FeedPreviewList>> getFeedsByUserId(
            @PathVariable Long userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size) {

        FeedResponseDto.FeedPreviewList feedPreviewList = feedService.getFeedsByUserId(userId, cursor, size);
        return ResponseEntity.ok(ApiResponse.onSuccess(feedPreviewList));
    }

    @Operation(summary = "나의 Feed 조회 API")
    @GetMapping("/my-feeds")
    public ResponseEntity<ApiResponse<FeedResponseDto.FeedPreviewList>> getFeeds(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size) {

        FeedResponseDto.FeedPreviewList feedPreviewList = feedService.getFeeds(cursor, size);
        return ResponseEntity.ok(ApiResponse.onSuccess(feedPreviewList));
    }
}
