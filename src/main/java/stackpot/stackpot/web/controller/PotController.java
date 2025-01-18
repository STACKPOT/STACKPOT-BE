package stackpot.stackpot.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.service.PotService;
import stackpot.stackpot.web.dto.PotRequestDto;
import stackpot.stackpot.web.dto.PotResponseDto;

@RestController
@RequestMapping("/pots")
@RequiredArgsConstructor
public class PotController {

    private final PotService potService;

    @PostMapping
    public ResponseEntity<PotResponseDto> createPot(@RequestBody @Valid PotRequestDto requestDto) {
        PotResponseDto responseDto = potService.createPotWithRecruitments(requestDto);
        return ResponseEntity.ok(responseDto);
    }
}