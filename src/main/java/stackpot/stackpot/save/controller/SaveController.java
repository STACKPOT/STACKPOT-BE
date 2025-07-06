package stackpot.stackpot.save.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.common.swagger.ApiErrorCodeExamples;
import stackpot.stackpot.save.service.SaveService;

@RestController
@RequestMapping(value = "/saves",produces = "application/json; charset=UTF-8")
@RequiredArgsConstructor
@Tag(name = "Save Management", description = "저장 관리 API")
public class SaveController {
    private final SaveService saveService;

    @PostMapping("/feed/{feed_id}")
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

    @PostMapping("/pot/{pot_id}")
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
}
