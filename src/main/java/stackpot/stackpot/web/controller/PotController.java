package stackpot.stackpot.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import stackpot.stackpot.service.PotService;
import stackpot.stackpot.web.dto.PotResponseDTO;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PotController {

    private final PotService potService;

    @GetMapping("/pots")
    public List<PotResponseDTO> getPots(@RequestParam(required = false) String recruitmentRole) {
        return potService.getAllPots(recruitmentRole);
    }

    @GetMapping("/pots/{potId}")
    public PotResponseDTO getPotDetails(@PathVariable Long potId) {
        return potService.getPotDetails(potId);
    }
}
