package stackpot.stackpot.pot.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.pot.dto.PotCommentRequestDto;
import stackpot.stackpot.pot.dto.PotCommentResponseDto;
import stackpot.stackpot.pot.service.potComment.PotCommentCommandService;
import stackpot.stackpot.pot.service.potComment.PotCommentQueryService;

import java.util.List;

@RestController
@RequestMapping("/pot-comments")
@RequiredArgsConstructor
public class PotCommentController {

    private final PotCommentCommandService potCommentCommandService;
    private final PotCommentQueryService potCommentQueryService;

    @Operation(summary = "팟의 모든 댓글 조회 API")
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<PotCommentResponseDto.AllPotCommentDto>>> selectPotComments(@RequestParam("potId") Long potId) {
        List<PotCommentResponseDto.AllPotCommentDto> result = potCommentQueryService.selectAllPotComments(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }

    @Operation(summary = "팟 댓글 달기 API")
    @PostMapping("")
    public ResponseEntity<ApiResponse<PotCommentResponseDto.PotCommentCreateDto>> createPotComment(@RequestBody PotCommentRequestDto.PotCommentCreateDto potCommentCreateDto) {
        PotCommentResponseDto.PotCommentCreateDto result = potCommentCommandService.createPotComment(potCommentCreateDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }

    @Operation(summary = "팟 대댓글 달기 API")
    @PostMapping("/{commentId}/replies")
    public ResponseEntity<ApiResponse<PotCommentResponseDto.PotReplyCommentCreateDto>> createPotReplyComment(@PathVariable("commentId") Long commentId,
                                                                                                             @RequestBody PotCommentRequestDto.PotCommentCreateDto potCommentCreateDto) {
        PotCommentResponseDto.PotReplyCommentCreateDto result = potCommentCommandService.createPotReplyComment(commentId, potCommentCreateDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }

    @Operation(summary = "팟 댓글/대댓글 수정 API")
    @PatchMapping("/{commentId}")
    public ResponseEntity<ApiResponse<PotCommentResponseDto.PotCommentUpdateDto>> updatePotComment(@PathVariable("commentId") Long commentId,
                                                                                                   @RequestBody PotCommentRequestDto.PotCommentUpdateDto potCommentUpdateDto) {
        PotCommentResponseDto.PotCommentUpdateDto result = potCommentCommandService.updatePotComment(commentId, potCommentUpdateDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }

    @Operation(summary = "팟 댓글/대댓글 삭제 API")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deletePotComment(@PathVariable("commentId") Long commentId) {
        potCommentCommandService.deletePotComment(commentId);
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }
}
