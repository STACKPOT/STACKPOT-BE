package stackpot.stackpot.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.service.PotApplicationService.PotApplicationService;
import stackpot.stackpot.web.dto.PotApplicationRequestDto;
import stackpot.stackpot.web.dto.PotApplicationResponseDto;

import java.util.List;

@Tag(name = "Pot Application Management", description = "팟 지원 관리 API")
@RestController
@RequestMapping("/pots/{pot_id}/applications")
@RequiredArgsConstructor
public class PotApplicationController {

    private final PotApplicationService potApplicationService;
    @Operation(summary = "팟 지원 API")
    @PostMapping
    public ResponseEntity<PotApplicationResponseDto> applyToPot(
            @PathVariable("pot_id") Long potId,
            @RequestBody @Valid PotApplicationRequestDto requestDto) {

        // 팟 지원 로직 호출
        PotApplicationResponseDto responseDto = potApplicationService.applyToPot(requestDto, potId);

        return ResponseEntity.ok(responseDto); // 성공 시 응답 반환
    }

    @Operation(summary = "팟 지원자 조회 API")
    @GetMapping("")
    public ResponseEntity<List<PotApplicationResponseDto>> getApplicants(
            @PathVariable("pot_id") Long potId) {
        // 서비스 호출
        List<PotApplicationResponseDto> applicants = potApplicationService.getApplicantsByPotId(potId);

        return ResponseEntity.ok(applicants);
    }

}