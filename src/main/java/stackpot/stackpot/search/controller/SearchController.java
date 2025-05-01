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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.search.service.SearchService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/search",produces = "application/json; charset=UTF-8")
@RequiredArgsConstructor
@Tag(name = "Search Management", description = "검색 API")
public class SearchController {

    private final SearchService searchService;

//    @GetMapping("/pots")
//    @Operation(summary = "팟 검색 API", description = "키워드로 팟 이름 및 내용을 검색합니다.",
//            parameters = {
//                    @Parameter(name = "keyword", description = "검색 키워드", example = "JAVA"),
//            })
//    public ResponseEntity<ApiResponse<Page<PotPreviewResponseDto>>> searchPots(
//            @RequestParam(required = false, defaultValue = "") String keyword,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "3") int size) {
//
//        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
//        // 서비스 호출 및 검색 수행
//        Page<PotPreviewResponseDto> response = searchService.searchPots(keyword, pageable);
//
//        return ResponseEntity.ok(ApiResponse.onSuccess(response));
//    }



//    @Operation(summary = "피드 검색 API", description = "키워드로 피드 제목 및 내용을 검색합니다.",
//            parameters = {
//                    @Parameter(name = "keyword", description = "검색 키워드", example = "Spring"),
//                    @Parameter(name = "page", description = "요청 페이지 번호 (0부터 시작)", example = "0"),
//                    @Parameter(name = "size", description = "한 페이지에 가져올 데이터 개수", example = "10")
//            })
//    @GetMapping("/feeds")
//    public ResponseEntity<ApiResponse<Map<String, Object>>> searchFeeds(
//            @RequestParam("keyword") String keyword,
//            @RequestParam(defaultValue = "0") Integer page,
//            @RequestParam(defaultValue = "10") Integer size) {
//
//        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
//
//        Page<FeedSearchResponseDto> feedPage = searchService.searchFeeds(keyword, pageable);
//
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("feeds", feedPage.getContent());
//        response.put("totalPages", feedPage.getTotalPages());
//        response.put("currentPage", feedPage.getNumber());
//        response.put("totalElements", feedPage.getTotalElements());
//
//        return ResponseEntity.ok(ApiResponse.onSuccess(response));
//    }
//

    @GetMapping
    @Operation(summary = "팟 or 피드 검색 API", description = "키워드를 기반으로 팟 또는 피드를 검색합니다.",
            parameters = {
                    @Parameter(name = "type", description = "검색 타입 (pot: 팟 검색, feed: 피드 검색)", example = "pot"),
                    @Parameter(name = "keyword", description = "검색 키워드", example = "JAVA"),
                    @Parameter(name = "page", description = "페이지 번호 (1부터 시작)", example = "1"),
                    @Parameter(name = "size", description = "페이지 크기", example = "10")
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
            throw new IllegalArgumentException("Invalid search type. Use 'pot' or 'feed'.");
        }

        // 응답 데이터 구성
        Map<String, Object> response = new HashMap<>();
        response.put("content", resultPage.getContent()); // 데이터 리스트
        response.put("totalPages", resultPage.getTotalPages()); // 전체 페이지 수
        response.put("totalElements", resultPage.getTotalElements()); // 전체 데이터 개수
        response.put("currentPage", page); // 현재 페이지 (1부터 시작)

        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }


}
