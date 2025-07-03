package stackpot.stackpot.pot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.common.swagger.ApiErrorCodeExamples;
import stackpot.stackpot.pot.dto.PotApplicationRequestDto;
import stackpot.stackpot.pot.dto.PotApplicationResponseDto;
import stackpot.stackpot.pot.service.potApplication.PotApplicationCommandService;
import stackpot.stackpot.pot.service.potApplication.PotApplicationQueryService;

import java.util.List;

@Tag(name = "Pot Application Management", description = "팟 지원 관리 API")
@RestController
@RequestMapping(value = "/pots/{pot_id}/applications",produces = "application/json; charset=UTF-8")
@RequiredArgsConstructor
public class PotApplicationController {

    private final PotApplicationCommandService potApplicationCommandService;
    private final PotApplicationQueryService potApplicationQueryService;
    @Operation(summary = "팟 지원 API")
    @ApiErrorCodeExamples({
            ErrorStatus.POT_NOT_FOUND,
            ErrorStatus.DUPLICATE_APPLICATION
    })
    @PostMapping
    public ResponseEntity<ApiResponse<PotApplicationResponseDto>> applyToPot(
            @PathVariable("pot_id") Long potId,
            @RequestBody @Valid PotApplicationRequestDto requestDto) {
        PotApplicationResponseDto responseDto = potApplicationCommandService.applyToPot(requestDto, potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(responseDto));
    }

    @Operation(summary = "팟 지원 취소 API")
    @ApiErrorCodeExamples({
            ErrorStatus.APPLICATION_NOT_FOUND
    })
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> cancelApplication(@PathVariable("pot_id") Long potId) {
        potApplicationCommandService.cancelApplication(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }


    @Operation(summary = "팟 지원자 조회 API")
    @ApiErrorCodeExamples({
            ErrorStatus.UNAUTHORIZED_ACCESS
    })
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<PotApplicationResponseDto>>> getApplicants(
            @PathVariable("pot_id") Long potId) {

        List<PotApplicationResponseDto> applicants = potApplicationQueryService.getApplicantsByPotId(potId);

        return ResponseEntity.ok(ApiResponse.onSuccess(applicants));
    }

}