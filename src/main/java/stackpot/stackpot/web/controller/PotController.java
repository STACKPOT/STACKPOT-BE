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

            @RequestBody @Valid PotRequestDto requestDto) {
        // 팟 생성 로직 호출
        PotResponseDto responseDto = potService.createPotWithRecruitments(requestDto);

        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping("/{pot_id}")
    public ResponseEntity<PotResponseDto> updatePot(
            @PathVariable("pot_id") Long potId,
            @RequestBody @Valid PotRequestDto requestDto) {
        // 팟 수정 로직 호출
        PotResponseDto responseDto = potService.updatePotWithRecruitments(potId, requestDto);

        return ResponseEntity.ok(responseDto); // 수정된 팟 정보 반환
    }


    @DeleteMapping("/{pot_id}")
    public ResponseEntity<Void> deletePot(@PathVariable("pot_id") Long potId) {
        // 팟 삭제 로직 호출
        potService.deletePot(potId);

        return ResponseEntity.noContent().build();
    }


}