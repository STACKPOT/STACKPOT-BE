package stackpot.stackpot.feed.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.common.swagger.ApiErrorCodeExamples;
import stackpot.stackpot.feed.dto.FeedRequestDto;
import stackpot.stackpot.feed.dto.FeedResponseDto;
import stackpot.stackpot.feed.dto.SeriesRequestDto;
import stackpot.stackpot.feed.entity.enums.Category;
import stackpot.stackpot.feed.service.FeedCommandService;
import stackpot.stackpot.feed.service.FeedQueryService;


@RestController
@RequestMapping(value = "/feeds",produces = "application/json; charset=UTF-8")
@RequiredArgsConstructor
@Tag(name = "Feed Management", description = "피드 관리 API")
public class FeedController {

    private final FeedQueryService feedQueryService;
    private final FeedCommandService feedCommandService;

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

        FeedResponseDto.CreatedFeedDto response = feedCommandService.createFeed(request);
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

        FeedResponseDto.FeedPreviewList response = feedQueryService.getPreViewFeeds(String.valueOf(category), sort, cursor, limit);
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
        FeedResponseDto.AuthorizedFeedDto response = feedQueryService.getFeed(feedId);
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

        FeedResponseDto.CreatedFeedDto response = feedCommandService.modifyFeed(feedId, request);
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
        String response = feedCommandService.deleteFeed(feedId);
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
        boolean isLiked = feedCommandService.toggleLike(feedId);
        return ResponseEntity.ok(ApiResponse.onSuccess(Map.of(
                "liked", isLiked,
                "message", isLiked ? "좋아요를 눌렀습니다." : "좋아요를 취소했습니다."
        )));
    }


    @PostMapping("/series")
    @Operation(summary = "시리즈 생성/삭제 동기화 API",
            description = "현재 화면에 표시되는 시리즈 이름 전체를 문자열 리스트로 전달해 주세요.\n" +
                    "서버에서는 해당 목록을 기준으로 새로운 시리즈는 생성하고,\n" +
                    "기존에 있던 시리즈 중 목록에 없는 항목은 삭제 처리합니다.\n" +
                    "기존 시리즈와 신규 시리즈를 따로 구분하지 않으셔도 되며,\n" +
                    "전체 목록만 보내주시면 됩니다.\n\n"+
                    "다 삭제하는 경우, 빈 리스트를 보내주시면 됩니다! {\"comments\": []}\n" )
    @ApiErrorCodeExamples({
            ErrorStatus.SERIES_BAD_REQUEST
    })
    public ResponseEntity<ApiResponse<Map<Long, String>>> createSeries(
            @Valid @RequestBody SeriesRequestDto requestDto) {

        Map<Long, String> seriesMap = feedCommandService.createSeries(requestDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(seriesMap));
    }

    @GetMapping("/series")
    @Operation(summary = "Series 조회 API", description = "본인의 Series List를 조회합니다.")
    @ApiErrorCodeExamples({
            ErrorStatus.USER_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<Map<Long, String>>> getSeries() {
        Map<Long, String> seriesMap = feedQueryService.getMySeries();
        return ResponseEntity.ok(ApiResponse.onSuccess(seriesMap));
    }

    @GetMapping("/series/{user_id}")
    @Operation(summary = "다른 사람 Series 조회 API", description = "다른 사람의 Series List를 조회합니다.")
    @ApiErrorCodeExamples({
            ErrorStatus.USER_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<Map<Long, String>>> getSeries(@PathVariable("user_id") Long userId) {
        Map<Long, String> seriesMap = feedQueryService.getOtherSeries(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(seriesMap));
    }

    @GetMapping("/likes")
    @Operation(summary = "좋아요(공감)한 피드 조회", description = "현재 사용자가 공감한 Feed 리스트를 페이징으로 조회합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSavedFeeds(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Map<String, Object> result = feedQueryService.getLikedFeedsWithPaging(page, size);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }
}
