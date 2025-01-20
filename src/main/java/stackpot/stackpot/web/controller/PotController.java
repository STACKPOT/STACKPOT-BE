package stackpot.stackpot.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.service.PotServiceImpl;
import stackpot.stackpot.web.dto.PotRequestDto;
import stackpot.stackpot.web.dto.PotResponseDto;

@RestController
@RequestMapping("/pots")
@RequiredArgsConstructor
public class PotController {

    private final PotServiceImpl potService;

    @PostMapping
    public ResponseEntity<PotResponseDto> createPot(
            @RequestHeader(name = "Authorization", required = true) String token,
            @RequestBody @Valid PotRequestDto requestDto) {
        PotResponseDto responseDto = potService.createPotWithRecruitments(token, requestDto);
        return ResponseEntity.ok(responseDto);
    }
    @PatchMapping("/{pot_id}")
    public ResponseEntity<PotResponseDto> updatePot(
            @RequestHeader("Authorization") String token,
            @PathVariable("pot_id") Long potId, // @PathVariable에 매핑된 이름을 명시적으로 설정
            @RequestBody @Valid PotRequestDto requestDto) {
        PotResponseDto responseDto = potService.updatePotWithRecruitments(token, potId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{pot_id}")
    public ResponseEntity<Void> deletePot(
            @RequestHeader("Authorization") String token,
            @PathVariable("pot_id") Long potId) { // 동일하게 이름 매핑
        potService.deletePot(token, potId);
        return ResponseEntity.noContent().build();
    }


}