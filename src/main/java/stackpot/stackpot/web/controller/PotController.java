package stackpot.stackpot.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
}
