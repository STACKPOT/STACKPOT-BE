package stackpot.stackpot.search.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.PotHandler;
import stackpot.stackpot.common.swagger.ApiErrorCodeExamples;
import stackpot.stackpot.feed.dto.FeedResponseDto;
import stackpot.stackpot.feed.service.FeedQueryService;
import stackpot.stackpot.search.service.SearchService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/search",produces = "application/json; charset=UTF-8")
@RequiredArgsConstructor
@Tag(name = "Search Management", description = "검색 API")
public class SearchController {

    private final SearchService searchService;
    private final FeedQueryService feedQueryService;

    @GetMapping
    @Operation(summary = "팟 or 피드 검색 API", description = "키워드를 기반으로 팟 또는 피드를 검색합니다.",
            parameters = {
                    @Parameter(name = "type", description = "검색 타입 (pot: 팟 검색, feed: 피드 검색)", example = "pot"),
                    @Parameter(name = "keyword", description = "검색 키워드", example = "JAVA"),
                    @Parameter(name = "page", description = "페이지 번호 (1부터 시작)", example = "1"),
                    @Parameter(name = "size", description = "페이지 크기", example = "10")
            })
    @ApiErrorCodeExamples({
            ErrorStatus.INVALID_SEARCH_TYPE
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> search(
            @RequestParam String type,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page, // 기본값 1
            @RequestParam(defaultValue = "10") Integer size) {

        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<?> resultPage;
        if ("pot".equalsIgnoreCase(type)) {
            resultPage = searchService.searchPots(keyword, pageable);
        } else if ("feed".equalsIgnoreCase(type)) {
            resultPage = searchService.searchFeeds(keyword, pageable);
        } else {
            throw new PotHandler(ErrorStatus.INVALID_SEARCH_TYPE);
        }

        // 응답 데이터 구성
        Map<String, Object> response = new HashMap<>();
        response.put("content", resultPage.getContent()); // 데이터 리스트
        response.put("totalPages", resultPage.getTotalPages()); // 전체 페이지 수
        response.put("totalElements", resultPage.getTotalElements()); // 전체 데이터 개수
        response.put("currentPage", page); // 현재 페이지 (1부터 시작)

        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @GetMapping("/feeds/users/{user_id}")
    @Operation(
            summary = "다른 사용자 피드 검색 API",
            description = "특정 사용자의 피드 중에서 키워드(제목/내용)를 포함하는 피드 목록을 검색합니다."
    )
    @ApiErrorCodeExamples({
            ErrorStatus.USER_NOT_FOUND,
            ErrorStatus.FEED_NOT_FOUND,
            ErrorStatus._INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<ApiResponse<FeedResponseDto.FeedPreviewList>> searchByUserId(
            @Parameter(
                    description = "해당 프로필의 사용자 ID",
                    example = "6"
            )
            @PathVariable Long user_id,

            @Parameter(
                    description = "이전 페이지 마지막 피드 ID"

            )
            @RequestParam(required = false) Long nextCursor,

            @Parameter(
                    description = "페이지 크기",
                    example = "5"
            )
            @RequestParam(defaultValue = "5") int pageSize,

            @Parameter(
                    description = "검색 키워드 (제목 또는 내용)",
                    example = "폭우"
            )
            @RequestParam(required = false) String keyword
    ) {
        FeedResponseDto.FeedPreviewList response = feedQueryService.searchByUserIdByKeyword(user_id, nextCursor, pageSize, keyword);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    //  키워드 기반 피드 검색 (로그인 유저 기준)
    @Operation(summary = "나의 피드 키워드 검색", description = "로그인한 사용자의 피드 중 키워드(title/content)로 검색합니다.")
    @GetMapping("/my-feeds")
    public ResponseEntity<ApiResponse<FeedResponseDto.FeedPreviewList>> searchMyFeedsByKeyword(
            @RequestParam(required = false) Long nextCursor,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        FeedResponseDto.FeedPreviewList result = feedQueryService.searchMyFeedsByKeyword(nextCursor, size, keyword);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }

}
