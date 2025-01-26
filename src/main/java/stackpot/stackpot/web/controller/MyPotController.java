package stackpot.stackpot.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.service.MyPotService;
import stackpot.stackpot.service.PotService;
import stackpot.stackpot.web.dto.MyPotResponseDTO;
import stackpot.stackpot.web.dto.MyPotTodoRequestDTO;
import stackpot.stackpot.web.dto.MyPotTodoResponseDTO;
import stackpot.stackpot.web.dto.MyPotTodoUpdateRequestDTO;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "My Pot Management", description = "나의 팟 관리 API")
public class MyPotController {

    private final MyPotService myPotService;
    private final PotService potService;

    // 사용자가 만든 진행 중인 팟 조회
    @Operation(summary = "사용자의 팟 목록 조회 API", description = "사용자가 생성했거나, 참여하고 있으며 진행 중(ONGOING)인 팟들 리스트를 조회합니다. \n")
    @GetMapping("/mypots/ongoing")
    public ResponseEntity<ApiResponse<Map<String, List<MyPotResponseDTO.OngoingPotsDetail>>>> getMyOngoingPots() {
        Map<String, List<MyPotResponseDTO.OngoingPotsDetail>> response = myPotService.getMyOnGoingPots();
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
//    @DeleteMapping("/{pot_id}/members/{user_id}")
//    @Operation(summary = "팟에서 멤버 삭제", description = "팟 멤버가 본인의 팟을 삭제하면 팟 멤버에서 해당 사용자가 제거됩니다.")
//    public ResponseEntity<ApiResponse<String>> removePotMember(
//            @PathVariable("pot_id") Long potId,
//            @PathVariable("user_id") Long userId) {
//
//        potService.removeMemberFromPot(potId, userId);
//        return ResponseEntity.ok(ApiResponse.onSuccess("팟 멤버가 성공적으로 삭제되었습니다."));
//    }

    // 팟에서의 투두 생성
    @Operation(
            summary = "Todo 생성 API",
            description = """
        - Status: NOT_STARTED / COMPLETED
        * 생성의 경우 NOT_STARTED로 전달해 주시면 됩니다.
    """
    )
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

    @Operation(summary = "Todo 완료 API", description = "todo의 status를 COMPLETED로 변경합니다.")
    @PatchMapping("/my-pots/{pot_id}/todos/{todo_id}")
    public ResponseEntity<ApiResponse<List<MyPotTodoResponseDTO>>> completeTodo(
            @PathVariable("pot_id") Long potId,
            @PathVariable("todo_id") Long todoId) {

        List<MyPotTodoResponseDTO> response = myPotService.completeTodo(potId, todoId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

}
