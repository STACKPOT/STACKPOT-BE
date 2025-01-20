package stackpot.stackpot.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.apiPayload.ApiResponse;
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
    public ResponseEntity<ApiResponse<List<MyPotResponseDTO>>> getMyOnGoingPots() {
        List<MyPotResponseDTO> myOngoingPots = myPotService.getMyOnGoingPots();
        return ResponseEntity.ok(ApiResponse.onSuccess(myOngoingPots));
    }

    @PostMapping("/my-pots/{pot_id}/todos")
    public ResponseEntity<ApiResponse<MyPotTodoResponseDTO>> postMyTodo(
            @PathVariable("pot_id") Long potId,
            @RequestBody  MyPotTodoRequestDTO request){
        MyPotTodoResponseDTO response = myPotService.postTodo(potId, request);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
}
