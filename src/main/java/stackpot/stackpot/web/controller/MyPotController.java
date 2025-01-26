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
public class MyPotController {

    private final MyPotService myPotService;


    // 사용자가 만든 진행 중인 팟 조회
    @Operation(summary = "사용자의 팟 목록 조회 API", description = "사용자가 생성했거나, 참여하고 있으며 진행 중(ONGOING)인 팟들 리스트를 조회합니다. \n")
    @GetMapping("/mypots/ongoing")
    public ResponseEntity<ApiResponse<Map<String, List<MyPotResponseDTO.OngoingPotsDetail>>>> getMyOngoingPots() {
        Map<String, List<MyPotResponseDTO.OngoingPotsDetail>> response = myPotService.getMyOnGoingPots();
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

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

    @Operation(summary = "mypotTask 생성 API")
    @PostMapping("/my-pots/{pot_id}/tasks")
    public ResponseEntity<ApiResponse<MyPotTaskResponseDto>> createPotTask(@PathVariable("pot_id") Long potId,
                                                                           @RequestBody @Valid MyPotTaskRequestDto.create request) {
        MyPotTaskResponseDto response = myPotService.creatTask(potId, request);

        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
    @Operation(summary = "mypotTask 상세보기 API")
    @GetMapping("/my-pots/{pot_id}/tasks/{task_id}")
    public ResponseEntity<ApiResponse<MyPotTaskResponseDto>> getPotDetailTask(@PathVariable("pot_id") Long potId, @PathVariable("task_id") Long taskId) {

        MyPotTaskResponseDto response = myPotService.viewDetailTask(taskId);

        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

        @Operation(summary = "[미완성] mypotTask 불러오기 API")
        @GetMapping("/my-pots/{pot_id}/tasks")
        public ResponseEntity<?> getPotTask(@PathVariable("pot_id") Long potId) {

            return null;
        }

    @Operation(summary = "mypotTask 수정 API")
    @PatchMapping("/my-pots/{pot_id}/tasks/{task_id}")
    public ResponseEntity<ApiResponse<MyPotTaskResponseDto>> modifyPotTask(@PathVariable("task_id") Long taskId, @RequestBody @Valid MyPotTaskRequestDto.create request) {
        MyPotTaskResponseDto response = myPotService.modfiyTask(taskId, request);

        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(summary = "mypotTask 삭제 API")
    @DeleteMapping("/my-pots/{pot_id}/tasks/{task_id}")
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
    @Operation(summary = "Todo 완료 API", description = "todo의 status를 COMPLETED로 변경합니다.")
    @PatchMapping("/my-pots/{pot_id}/todos/{todo_id}")
    public ResponseEntity<ApiResponse<List<MyPotTodoResponseDTO>>> completeTodo(
            @PathVariable("pot_id") Long potId,
            @PathVariable("todo_id") Long todoId) {

        List<MyPotTodoResponseDTO> response = myPotService.completeTodo(potId, todoId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

}
