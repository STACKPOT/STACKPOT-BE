package stackpot.stackpot.web.controller;

import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "팟 생성하기")
    @PostMapping
    public ResponseEntity<PotResponseDto> createPot(

            @RequestBody @Valid PotRequestDto requestDto) {
        // 팟 생성 로직 호출
        PotResponseDto responseDto = potService.createPotWithRecruitments(requestDto);

        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "팟 수정하기")
    @PatchMapping("/{pot_id}")
    public ResponseEntity<PotResponseDto> updatePot(
            @PathVariable("pot_id") Long potId,
            @RequestBody @Valid PotRequestDto requestDto) {
        // 팟 수정 로직 호출
        PotResponseDto responseDto = potService.updatePotWithRecruitments(potId, requestDto);

        return ResponseEntity.ok(responseDto); // 수정된 팟 정보 반환
    }

    @Operation(summary = "팟 삭제하기")
    @DeleteMapping("/{pot_id}")
    public ResponseEntity<Void> deletePot(@PathVariable("pot_id") Long potId) {
        // 팟 삭제 로직 호출
        potService.deletePot(potId);

        return ResponseEntity.noContent().build();
    }


}