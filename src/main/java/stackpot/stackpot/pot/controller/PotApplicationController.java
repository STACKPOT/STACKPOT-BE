package stackpot.stackpot.pot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.pot.dto.PotApplicationRequestDto;
import stackpot.stackpot.pot.dto.PotApplicationResponseDto;
import stackpot.stackpot.pot.service.PotApplicationCommandService;
import stackpot.stackpot.pot.service.PotApplicationQueryService;

import java.util.List;

@Tag(name = "Pot Application Management", description = "팟 지원 관리 API")
@RestController
@RequestMapping(value = "/pots/{pot_id}/applications",produces = "application/json; charset=UTF-8")
@RequiredArgsConstructor
public class PotApplicationController {

    private final PotApplicationCommandService potApplicationCommandService;
    private final PotApplicationQueryService potApplicationQueryService;
    @Operation(summary = "팟 지원 API")
    @PostMapping
    public ResponseEntity<ApiResponse<PotApplicationResponseDto>> applyToPot(
            @PathVariable("pot_id") Long potId,
            @RequestBody @Valid PotApplicationRequestDto requestDto) {

        // 팟 지원 로직 호출
        PotApplicationResponseDto responseDto = potApplicationCommandService.applyToPot(requestDto, potId);

        return ResponseEntity.ok(ApiResponse.onSuccess(responseDto)); // 성공 시 ApiResponse로 감싸서 응답 반환
    }
    @Operation(summary = "팟 지원 취소 API")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> cancelApplication(@PathVariable("pot_id") Long potId) {
        potApplicationCommandService.cancelApplication(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }


    @Operation(summary = "팟 지원자 조회 API")
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<PotApplicationResponseDto>>> getApplicants(
            @PathVariable("pot_id") Long potId) {
        // 서비스 호출
        List<PotApplicationResponseDto> applicants = potApplicationQueryService.getApplicantsByPotId(potId);

        return ResponseEntity.ok(ApiResponse.onSuccess(applicants)); // 성공 시 ApiResponse로 감싸서 응답 반환
    }

}