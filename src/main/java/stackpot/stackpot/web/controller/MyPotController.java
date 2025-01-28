package stackpot.stackpot.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.service.MyPotService;
import stackpot.stackpot.service.PotService;
import stackpot.stackpot.web.dto.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "My Pot Management", description = "나의 팟 관리 API")
@RequestMapping("/my-pots")
public class MyPotController {

    private final MyPotService myPotService;
    private final PotService potService;

    // 사용자가 만든 진행 중인 팟 조회
    @Operation(summary = "나의 진행 중인 팟 조회 API", description = "사용자가 생성했거나, 참여하고 있으며 진행 중(ONGOING)인 팟들 리스트를 조회합니다. \n")
    @GetMapping("")
    public ResponseEntity<ApiResponse<Map<String, List<MyPotResponseDTO.OngoingPotsDetail>>>> getMyOngoingPots() {
        Map<String, List<MyPotResponseDTO.OngoingPotsDetail>> response = myPotService.getMyOnGoingPots();
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
//    @DeleteMapping("/{pot_id}/members")
//    @Operation(summary = "팟에서 본인 삭제", description = "현재 로그인한 팟 멤버가 본인의 팟을 삭제하면 팟 멤버에서 자신이 제거됩니다.")
//    public ResponseEntity<ApiResponse<String>> removePotMember(
//            @PathVariable("pot_id") Long potId) {
//
//        potService.removeMemberFromPot(potId);
//        return ResponseEntity.ok(ApiResponse.onSuccess("팟 멤버가 성공적으로 삭제되었습니다."));
//    }
    @DeleteMapping("/{pot_id}/members")
    @Operation(summary = "팟 멤버 또는 팟 삭제 API", description = "생성자는 팟을 삭제하며, 생성자가 아니면 팟 멤버에서 본인을 삭제합니다.")
    public ResponseEntity<ApiResponse<String>> removePotOrMember(
            @PathVariable("pot_id") Long potId) {

        String responseMessage = potService.removePotOrMember(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(responseMessage));
    }

    // 팟에서의 투두 생성
    @Operation(
            summary = "Todo 생성 API",
            description = """
        - Status: NOT_STARTED / COMPLETED
        * 생성의 경우 NOT_STARTED로 전달해 주시면 됩니다.
    """
    )
    @PostMapping("/{pot_id}/todos")
    public ResponseEntity<ApiResponse<List<MyPotTodoResponseDTO>>> postMyTodo(
            @PathVariable("pot_id") Long potId,
            @RequestBody MyPotTodoRequestDTO request) {

        List<MyPotTodoResponseDTO> response = myPotService.postTodo(potId, request);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    // 팟에서의 투두 조회
    @Operation(summary = "Todo 조회 API")
    @GetMapping("/{pot_id}/todos")
    public ResponseEntity<ApiResponse<List<MyPotTodoResponseDTO>>> getMyTodo(@PathVariable("pot_id") Long potId){
        List<MyPotTodoResponseDTO> response = myPotService.getTodo(potId);  // 수정된 부분
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(summary = "Todo 수정 API", description = "사용자의 모든 투두의 내용을 한 번에 수정할 수 있습니다. 리스트 사이에 ,로 구분해서 전달해 주셔야 합니다!")
    @PatchMapping("/{pot_id}/todos")
    public ResponseEntity<ApiResponse<List<MyPotTodoResponseDTO>>> updateMyTodos(
            @PathVariable("pot_id") Long potId,
            @RequestBody List<MyPotTodoUpdateRequestDTO> requestList) {

        List<MyPotTodoResponseDTO> response = myPotService.updateTodos(potId, requestList);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(summary = "Todo 완료 API", description = "todo의 status를 COMPLETED로 변경합니다.")
    @PatchMapping("/{pot_id}/todos/{todo_id}")
    public ResponseEntity<ApiResponse<List<MyPotTodoResponseDTO>>> completeTodo(
            @PathVariable("pot_id") Long potId,
            @PathVariable("todo_id") Long todoId) {

        List<MyPotTodoResponseDTO> response = myPotService.completeTodo(potId, todoId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(summary = "Task 생성 API")
    @PostMapping("/{pot_id}/tasks")
    public ResponseEntity<ApiResponse<MyPotTaskResponseDto>> createPotTask(@PathVariable("pot_id") Long potId,
                                                                           @RequestBody @Valid MyPotTaskRequestDto.create request) {
        MyPotTaskResponseDto response = myPotService.creatTask(potId, request);

        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
    @Operation(summary = "Task 상세 조회 API")
    @GetMapping("/{pot_id}/tasks/{task_id}")
    public ResponseEntity<ApiResponse<MyPotTaskResponseDto>> getPotDetailTask(@PathVariable("pot_id") Long potId, @PathVariable("task_id") Long taskId) {

        MyPotTaskResponseDto response = myPotService.viewDetailTask(taskId);

        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

        @Operation(summary = "[미완성] Task 조회 API")
        @GetMapping("/{pot_id}/tasks")
        public ResponseEntity<?> getPotTask(@PathVariable("pot_id") Long potId) {

            return null;
        }

    @Operation(summary = "Task 수정 API")
    @PatchMapping("/{pot_id}/tasks/{task_id}")
    public ResponseEntity<ApiResponse<MyPotTaskResponseDto>> modifyPotTask(@PathVariable("task_id") Long taskId, @RequestBody @Valid MyPotTaskRequestDto.create request) {
        MyPotTaskResponseDto response = myPotService.modfiyTask(taskId, request);

        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(summary = "Task 삭제 API")
    @DeleteMapping("/{pot_id}/tasks/{task_id}")
    public ResponseEntity<?> deletetPotTask(@PathVariable("pot_id") Long potId, @PathVariable("task_id") Long taskId) {
        try {
            myPotService.deleteTaskboard(potId, taskId);
            return ResponseEntity.ok(ApiResponse.onSuccess("할일이 삭제되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while deleting the taskboard and associated tasks.");
        }
    }
}
