package stackpot.stackpot.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.service.PotApplicationService.PotApplicationService;
import stackpot.stackpot.web.dto.ApplicationRequestDto;
import stackpot.stackpot.web.dto.ApplicationResponseDto;

import java.util.List;

@RestController
@RequestMapping("/pots/{pot_id}/applications")
@RequiredArgsConstructor
public class PotApplicationController {

    private final PotApplicationService potApplicationService;

    @PostMapping
    public ResponseEntity<ApplicationResponseDto> applyToPot(
            @RequestHeader("Authorization") String token,
            @PathVariable("pot_id") Long potId, // PathVariable 이름 변경
            @RequestBody @Valid ApplicationRequestDto requestDto) {
        String parsedToken = token.replace("Bearer ", "");
        ApplicationResponseDto responseDto = potApplicationService.applyToPot(parsedToken, potId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponseDto>> getApplications(@PathVariable("pot_id") Long potId) {
        List<ApplicationResponseDto> applications = potApplicationService.getApplicationsByPot(potId);
        return ResponseEntity.ok(applications);
    }
}