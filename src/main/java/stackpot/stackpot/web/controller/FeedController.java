package stackpot.stackpot.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.web.dto.PotRequestDto;
import stackpot.stackpot.web.dto.PotResponseDto;

@RestController
@RequestMapping("/feeds")
@RequiredArgsConstructor
public class FeedController {

    @GetMapping
    public ResponseEntity<PotResponseDto> feedPreView() {
//        return ResponseEntity.ok();
        return null;
    }
}
