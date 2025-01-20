package stackpot.stackpot.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.service.MyPotService;
import stackpot.stackpot.service.PotService;
import stackpot.stackpot.web.dto.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MyPotController {

    private final MyPotService myPotService;


    // 사용자가 만든 진행 중인 팟 조회
    @GetMapping("/my-pots")
    public ResponseEntity<List<MyPotResponseDTO>> getMyOnGoingPots() {
        return ResponseEntity.ok(myPotService.getMyOnGoingPots());
    }


}
