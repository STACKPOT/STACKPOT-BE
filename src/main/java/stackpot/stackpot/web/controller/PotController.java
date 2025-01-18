package stackpot.stackpot.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import stackpot.stackpot.service.PotService;
import stackpot.stackpot.web.dto.PotResponseDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PotController {

    private final PotService potService;

    @GetMapping("/pots")
    public List<PotResponseDto> getPots(@RequestParam(required = false) String role) {
        return potService.getAllPots(role);
    }

    // 특정 Pot의 상세 정보 조회
    @GetMapping("/pots/{potId}")
    public PotResponseDto getPotDetails(@PathVariable Long potId) {
        return potService.getPotDetails(potId);
    }
}
