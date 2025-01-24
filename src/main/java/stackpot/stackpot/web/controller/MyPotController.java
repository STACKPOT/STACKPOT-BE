package stackpot.stackpot.web.controller;

import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "사용자가 만든 진행 중인 팟 조회 API", description = "pot 상태는 다음과 같이 구분됩니다. recruiting / ongoing / completed\n")
    @GetMapping("/my-pots")
    public ResponseEntity<ApiResponse<List<MyPotResponseDTO>>> getMyOnGoingPots() {
        List<MyPotResponseDTO> myOngoingPots = myPotService.getMyOnGoingPots();
        return ResponseEntity.ok(ApiResponse.onSuccess(myOngoingPots));
    }

    // 팟에서의 투두 생성
    @Operation(summary = "Todo 생성 API", description = "status는 NOT_STARTED와 COMPLETED로 구분되며, 생성의 경우 NOT_STARTED로 전달해 주시면 됩니다.")
    @PostMapping("/my-pots/{pot_id}/todos")
    public ResponseEntity<ApiResponse<List<MyPotTodoResponseDTO>>> postMyTodo(
            @PathVariable("pot_id") Long potId,
            @RequestBody MyPotTodoRequestDTO request) {

        List<MyPotTodoResponseDTO> response = myPotService.postTodo(potId, request);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    // 팟에서의 투두 조회
    @Operation(summary = "Todo 조회 API")
    @GetMapping("/my-pots/{pot_id}/todos")
    public ResponseEntity<ApiResponse<List<MyPotTodoResponseDTO>>> getMyTodo(@PathVariable("pot_id") Long potId){
        List<MyPotTodoResponseDTO> response = myPotService.getTodo(potId);  // 수정된 부분
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(summary = "Todo 내용 일괄 수정 API", description = "사용자의 모든 투두의 내용을 한 번에 수정할 수 있습니다. 리스트 사이에 ,로 구분해서 전달해 주셔야 합니다!")
    @PatchMapping("/my-pots/{pot_id}/todos")
    public ResponseEntity<ApiResponse<List<MyPotTodoResponseDTO>>> updateMyTodos(
            @PathVariable("pot_id") Long potId,
            @RequestBody List<MyPotTodoUpdateRequestDTO> requestList) {

        List<MyPotTodoResponseDTO> response = myPotService.updateTodos(potId, requestList);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

}
