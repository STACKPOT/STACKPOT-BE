package stackpot.stackpot.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.converter.FeedConverter;
import stackpot.stackpot.domain.Feed;
import stackpot.stackpot.domain.enums.Category;
import stackpot.stackpot.service.FeedService;
import stackpot.stackpot.web.dto.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/feeds")
@RequiredArgsConstructor
@Tag(name = "Feed Management", description = "피드 관리 API")
public class FeedController {

    private final FeedService feedService;
    private final FeedConverter feedConverter;


    @Operation(summary = "(수정 필요) feed 작성 api")
    @PostMapping("")
    public ResponseEntity<FeedResponseDto.FeedDto> createFeeds(@Valid @RequestBody FeedRequestDto.createDto requset) {
        // 정상 처리
        Feed feed = feedService.createFeed(requset);
        Long feedId = feed.getFeedId();
        Long saveCount = feedService.getSaveCount(feedId);
        Long likeCount = feedService.getLikeCount(feedId);

        FeedResponseDto.FeedDto response = feedConverter.feedDto(feed, likeCount+saveCount,likeCount);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "feed 미리보기 api ( 수정 필요 )")
    @GetMapping("")
    public ResponseEntity<FeedResponseDto.FeedPreviewList> getPreViewFeeds(
            @RequestParam(value = "category", required = false, defaultValue = "ALL") Category category,
            @RequestParam(value = "sort", required = false, defaultValue = "new") String sort,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        FeedResponseDto.FeedPreviewList response = feedService.getPreViewFeeds(category, sort, cursor, limit);
        return ResponseEntity.ok(response);
    }
    @Operation(summary = "feed 상세보기 api")
    @PostMapping("/{feedId}")
    public ResponseEntity<FeedResponseDto.FeedDto> getDetailFeed(@PathVariable Long feedId) {

        Feed feed = feedService.getFeed(feedId);
        Long saveCount = feedService.getSaveCount(feedId);
        Long likeCount = feedService.getLikeCount(feedId);

        FeedResponseDto.FeedDto response = feedConverter.feedDto(feed, likeCount+saveCount,likeCount);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "feed 수정 api (수정 필요")
    @PatchMapping("/{feedId}")
    public ResponseEntity<FeedResponseDto.FeedDto> modifyFeed(@PathVariable Long feedId, @Valid @RequestBody FeedRequestDto.createDto requset) {
        // 정상 처리
        Feed feed = feedService.modifyFeed(feedId, requset);
        Long saveCount = feedService.getSaveCount(feedId);
        Long likeCount = feedService.getLikeCount(feedId);

        FeedResponseDto.FeedDto response =feedConverter.feedDto(feed, likeCount+saveCount,likeCount);

        return ResponseEntity.ok(response);
    }
    @Operation(summary = "feed 좋아요 추가 api")
    @PostMapping("/{feedId}/like")
    public ResponseEntity<Map> toggleLike(@PathVariable Long feedId) {

        // 좋아요 토글
        boolean isLiked = feedService.toggleLike(feedId);
        return ResponseEntity.ok(Map.of(
                "liked", isLiked,
                "message", isLiked ? "좋아요를 눌렀습니다." : "좋아요를 취소했습니다."
        ));
    }

    @Operation(summary = "feed 저장하기 api")
    @PostMapping("/{feedId}/save")
    public ResponseEntity<Map> toggleSave(@PathVariable Long feedId) {

        // 좋아요 토글
        boolean isSaved = feedService.toggleSave(feedId);
        return ResponseEntity.ok(Map.of(
                "saved", isSaved,
                "message", isSaved ? "해당 피드를 저장했습니다." : "해당 피드 저장을 취소했습니다."
        ));
    }

    @Operation(summary = "사용자 별 feed 조회 API")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<FeedResponseDto.FeedPreviewList>> getFeedsByUserId(
            @PathVariable Long userId,
            @RequestParam(required = false) String nextCursor,
            @RequestParam(defaultValue = "10") int pageSize) {

        FeedResponseDto.FeedPreviewList feedPreviewList = feedService.getFeedsByUserId(userId, nextCursor, pageSize);
        return ResponseEntity.ok(ApiResponse.onSuccess(feedPreviewList));
    }
}
