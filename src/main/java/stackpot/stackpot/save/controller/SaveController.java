package stackpot.stackpot.save.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.common.swagger.ApiErrorCodeExamples;
import stackpot.stackpot.save.service.SaveService;

import java.util.Map;

@RestController
@RequestMapping(value = "/saves",produces = "application/json; charset=UTF-8")
@RequiredArgsConstructor
@Tag(name = "Save Management", description = "저장 관리 API")
public class SaveController {
    private final SaveService saveService;

    @PostMapping("/feeds/{feed_id}")
    @Operation(
            summary = "Feed 저장 토글 API",
            description = "특정 Feed를 저장하거나 저장을 취소합니다. 이미 저장된 상태에서 다시 호출하면 저장이 해제됩니다.",
            parameters = {
                    @Parameter(name = "feed_id", description = "저장 또는 저장 해제할 Feed의 ID", required = true)
            }
    )
    @ApiErrorCodeExamples({
            ErrorStatus.FEED_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<String>> toggleFeedSave(@PathVariable Long feed_id) {
        String message = saveService.feedSave(feed_id);
        return ResponseEntity.ok(ApiResponse.onSuccess(message));
    }

    @PostMapping("/pots/{pot_id}")
    @Operation(
            summary = "Pot 저장 토글 API",
            description = "특정 Pot을 저장하거나 저장을 취소합니다. 이미 저장된 상태에서 다시 호출하면 저장이 해제됩니다.",
            parameters = {
                    @Parameter(name = "pot_id", description = "저장 또는 저장 해제할 Pot의 ID", required = true)
            }
    )
    @ApiErrorCodeExamples({
            ErrorStatus.POT_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<String>> togglePotSave(@PathVariable Long pot_id) {
        String message = saveService.potSave(pot_id);
        return ResponseEntity.ok(ApiResponse.onSuccess(message));
    }

    @GetMapping("/pots")
    @Operation(summary = "저장한 팟 조회", description = "현재 사용자가 저장한 Pot 리스트를 페이징으로 조회합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSavedPots(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Map<String, Object> result = saveService.getSavedPotsWithPaging(page, size);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }

    @GetMapping("/feeds")
    @Operation(summary = "저장한 피드 조회", description = "현재 사용자가 저장한 Feed 리스트를 페이징으로 조회합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSavedFeeds(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Map<String, Object> result = saveService.getSavedFeedsWithPaging(page, size);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }
}
