package stackpot.stackpot.feed.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.common.swagger.ApiErrorCodeExamples;
import stackpot.stackpot.feed.dto.FeedRequestDto;
import stackpot.stackpot.feed.dto.FeedResponseDto;
import stackpot.stackpot.feed.dto.SeriesRequestDto;
import stackpot.stackpot.feed.entity.Feed;
import stackpot.stackpot.feed.entity.enums.Category;
import stackpot.stackpot.feed.service.FeedService;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping(value = "/feeds",produces = "application/json; charset=UTF-8")
@RequiredArgsConstructor
@Tag(name = "Feed Management", description = "피드 관리 API")
public class FeedController {

    private final FeedService feedService;



    @PostMapping("")
    @Operation(summary = "Feed 생성 API",
            description = "Feed를 생성하는 API입니다.\n" +
                    "- categories: 다중 선택 가능하며 enum입니다. [ALL, BACKEND, FRONTEND, DESIGN, PLANNING] \n" +
                    "- interests: 다중 선택 가능하며 enum입니다. [SIDE_PROJECT(사이드 프로젝트), SOLO_DEVELOPMENT(1인 개발), COMPETITION(공모전), STARTUP(창업), NETWORKING(네트워킹 행사)]\n" +
                    "- seriesId: 저장할 시리즈의 Id를 입력해 주시면 됩니다. 선택하지 않을 경우 null을 보내주세요. \n")
    @ApiErrorCodeExamples({
            ErrorStatus.USER_NOT_FOUND,
            ErrorStatus.SERIES_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<FeedResponseDto.CreatedFeedDto>> createFeeds(
            @Valid @RequestBody FeedRequestDto.createDto request) {

        FeedResponseDto.CreatedFeedDto response = feedService.createFeed(request);
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
    @ApiErrorCodeExamples({
            ErrorStatus.USER_NOT_FOUND,
            ErrorStatus.FEED_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<FeedResponseDto.FeedPreviewList>> getPreViewFeeds(
            @RequestParam(value = "category", required = false, defaultValue = "ALL") Category category,
            @RequestParam(value = "sort", required = false, defaultValue = "new") String sort,
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        FeedResponseDto.FeedPreviewList response = feedService.getPreViewFeeds(String.valueOf(category), sort, cursor, limit);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @GetMapping("/{feedId}/detail")
    @Operation(summary = "Feed 상세 조회 API", description = "요청된 Feed를 보여줍니다.",
            parameters = {
                    @Parameter(name = "feedId", description = "상세 조회 feedId")
            })
    @ApiErrorCodeExamples({
            ErrorStatus.USER_NOT_FOUND,
            ErrorStatus.FEED_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<FeedResponseDto.AuthorizedFeedDto>> getDetailFeed(@PathVariable Long feedId) {
        FeedResponseDto.AuthorizedFeedDto response = feedService.getFeed(feedId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }


    @PatchMapping("/{feedId}")
    @Operation(
            summary = "Feed 수정 API",
            description = "요청된 feedId의 feed 내용을 수정합니다. 수정 사항이 없다면 null 값을 넣어주세요\n" +
                    "- categories: 다중 선택 가능하며 enum입니다. [ALL, BACKEND, FRONTEND, DESIGN, PLANNING] \n" +
                    "- interests: 다중 선택 가능하며 enum입니다. [SIDE_PROJECT(사이드 프로젝트), SOLO_DEVELOPMENT(1인 개발), COMPETITION(공모전), STARTUP(창업), NETWORKING(네트워킹 행사)]\n" +
                    "- seriesId: 저장할 시리즈의 Id를 입력해 주시면 됩니다. 선택하지 않을 경우 null을 보내주세요.\n",
            parameters = {
                    @Parameter(name = "feedId", description = "수정할 Feed의 ID")
            }
    )
    @ApiErrorCodeExamples({
            ErrorStatus.FEED_UNAUTHORIZED,
            ErrorStatus.FEED_NOT_FOUND,
            ErrorStatus.SERIES_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<FeedResponseDto.CreatedFeedDto>> modifyFeed(
            @PathVariable Long feedId,
            @Valid @RequestBody FeedRequestDto.createDto request) {

        FeedResponseDto.CreatedFeedDto response = feedService.modifyFeed(feedId, request);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @DeleteMapping("/{feedId}")
    @Operation(summary = "Feed 삭제 API", description = "요청된 feedId의 feed 내용을 수정합니다.",
            parameters = {
                    @Parameter(name = "feedId", description = "삭제 feedId")
            })
    @ApiErrorCodeExamples({
            ErrorStatus.FEED_UNAUTHORIZED,
            ErrorStatus.FEED_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<String>> deleteFeed(@PathVariable Long feedId) {
        String response = feedService.deleteFeed(feedId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @PostMapping("/{feedId}/like")
    @Operation(summary = "Feed 좋아요 API", description = "feed 좋아요를 추가합니다.",
            parameters = {
                    @Parameter(name = "feedId", description = "좋아요를 누를 Feed의 ID", required = true)
            })
    @ApiErrorCodeExamples({
            ErrorStatus.USER_NOT_FOUND,
            ErrorStatus.FEED_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<Map>> toggleLike(@PathVariable Long feedId) {
        boolean isLiked = feedService.toggleLike(feedId);
        return ResponseEntity.ok(ApiResponse.onSuccess(Map.of(
                "liked", isLiked,
                "message", isLiked ? "좋아요를 눌렀습니다." : "좋아요를 취소했습니다."
        )));
    }
    @GetMapping("/{userId}")
    @Operation(
            summary = "사용자별 Feed 조회 API",
            description = "사용자의 feed를 조회합니다."
    )
    public ResponseEntity<ApiResponse<FeedResponseDto.FeedPreviewList>> getFeedsByUserId(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable("userId") Long userId,

            @Parameter(description = "커서", example = "100", required = false)
            @RequestParam(value = "cursor", required = false) Long cursor,

            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        FeedResponseDto.FeedPreviewList feedPreviewList = feedService.getFeedsByUserId(userId, cursor, size);
        return ResponseEntity.ok(ApiResponse.onSuccess(feedPreviewList));
    }

    @Operation(summary = "나의 Feed 조회 API")
    @GetMapping("/my-feeds")
    @ApiErrorCodeExamples({
            ErrorStatus.USER_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<FeedResponseDto.FeedPreviewList>> getFeeds(
            @RequestParam(name = "cursor", required = false) Long cursor,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        FeedResponseDto.FeedPreviewList feedPreviewList = feedService.getFeeds(cursor, size);
        return ResponseEntity.ok(ApiResponse.onSuccess(feedPreviewList));
    }

    @PostMapping("/series")
    @Operation(summary = "Series 생성 API", description = "새로운 Series를 리스트로 넣어주시면 됩니다.")
    @ApiErrorCodeExamples({
            ErrorStatus.SERIES_BAD_REQUEST
    })
    public ResponseEntity<ApiResponse<Map<Long, String>>> createSeries(
            @Valid @RequestBody SeriesRequestDto requestDto) {

        Map<Long, String> seriesMap = feedService.createSeries(requestDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(seriesMap));
    }

    @GetMapping("/series")
    @Operation(summary = "Series 조회 API", description = "본인의 Series List를 조회합니다.")
    @ApiErrorCodeExamples({
            ErrorStatus.USER_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<Map<Long, String>>> getSeries() {
        Map<Long, String> seriesMap = feedService.getMySeries();
        return ResponseEntity.ok(ApiResponse.onSuccess(seriesMap));
    }
}
