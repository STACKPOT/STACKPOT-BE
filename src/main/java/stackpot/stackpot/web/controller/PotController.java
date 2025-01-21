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
            @RequestHeader("Authorization") String token,
            @RequestBody @Valid PotRequestDto requestDto) {
        // Bearer 제거 후 토큰 전달
        String parsedToken = token.replace("Bearer ", "");
        PotResponseDto responseDto = potService.createPotWithRecruitments(parsedToken, requestDto);
        return ResponseEntity.ok(responseDto);
    }
    @PatchMapping("/{pot_id}")
    public ResponseEntity<PotResponseDto> updatePot(
            @RequestHeader("Authorization") String token,
            @PathVariable("pot_id") Long potId, // 동일하게 이름 매핑
            @RequestBody @Valid PotRequestDto requestDto) { // 요청 DTO 검증
        // Bearer 제거
        String parsedToken = token.replace("Bearer ", "");

        // 팟 수정 로직 호출
        PotResponseDto responseDto = potService.updatePotWithRecruitments(parsedToken, potId, requestDto);

        return ResponseEntity.ok(responseDto); // 수정된 팟 정보 반환
    }


    @DeleteMapping("/{pot_id}")
    public ResponseEntity<Void> deletePot(
            @RequestHeader("Authorization") String token,
            @PathVariable("pot_id") Long potId) { // 동일하게 이름 매핑
        // Bearer 제거
        String parsedToken = token.replace("Bearer ", "");

        // 팟 삭제 로직 호출
        potService.deletePot(parsedToken, potId);

        return ResponseEntity.noContent().build();
    }


}