package stackpot.stackpot.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.service.PotApplicationService.PotApplicationService;
import stackpot.stackpot.web.dto.PotApplicationRequestDto;
import stackpot.stackpot.web.dto.PotApplicationResponseDto;

import java.util.List;

@RestController
@RequestMapping("/pots/{pot_id}/applications")
@RequiredArgsConstructor
public class PotApplicationController {

    private final PotApplicationService potApplicationService;

    @PostMapping
    public ResponseEntity<PotApplicationResponseDto> applyToPot(
            @PathVariable("pot_id") Long potId,
            @RequestBody @Valid PotApplicationRequestDto requestDto) {

        // 팟 지원 로직 호출
        PotApplicationResponseDto responseDto = potApplicationService.applyToPot(requestDto, potId);

        return ResponseEntity.ok(responseDto); // 성공 시 응답 반환
    }


    @GetMapping
    public ResponseEntity<List<PotApplicationResponseDto>> getApplications(@PathVariable("pot_id") Long potId) {
        List<PotApplicationResponseDto> applications = potApplicationService.getApplicationsByPot(potId);
        return ResponseEntity.ok(applications);
    }
}