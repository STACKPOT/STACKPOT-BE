package stackpot.stackpot.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.service.FeedService;
import stackpot.stackpot.web.dto.FeedResponseDto;
import stackpot.stackpot.web.dto.PotRequestDto;
import stackpot.stackpot.web.dto.PotResponseDto;

@RestController
@RequestMapping("/feeds")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @Operation(summary = "피드 미리보기 api")
    @GetMapping("")
    public ResponseEntity<FeedResponseDto.FeedResponse> getPreViewFeeds(
            @RequestParam(value = "category", required = false, defaultValue = "전체") String category,
            @RequestParam(value = "sort", required = false, defaultValue = "new") String sort,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        FeedResponseDto.FeedResponse response = feedService.getPreViewFeeds(category, sort, cursor, limit);
        return ResponseEntity.ok(response);
    }
}
