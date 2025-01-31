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
    @Operation(summary = "나의 진행 중인 팟 조회 API", description = "'나의 팟 첫 페이지'의 정보를 리턴합니다. 사용자가 생성했거나, 참여하고 있으며 진행 중(ONGOING)인 팟들 리스트를 조회합니다. \n")
    @GetMapping("")
    public ResponseEntity<ApiResponse<Map<String, List<MyPotResponseDTO.OngoingPotsDetail>>>> getMyOngoingPots() {
        Map<String, List<MyPotResponseDTO.OngoingPotsDetail>> response = myPotService.getMyOnGoingPots();
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @DeleteMapping("/{pot_id}/members")
    @Operation(summary = "팟 멤버 또는 팟 삭제 API", description = "생성자는 팟을 삭제하며, 생성자가 아니면 팟 멤버에서 본인을 삭제합니다.")
    public ResponseEntity<ApiResponse<String>> removePotOrMember(
            @PathVariable("pot_id") Long potId) {

        String responseMessage = potService.removePotOrMember(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(responseMessage));
    }

    @GetMapping("/{pot_id}/details")
    @Operation(summary = "끓인 팟 상세 보기", description = "'끓인 팟 상세보기 모달'에 쓰이는 COMPLETED 상태인 팟의 상세 정보를 가져옵니다. 팟 멤버들의 Role : num과 나의 역할도 함께 반환합니다.")
    public ResponseEntity<ApiResponse<CompletedPotDetailResponseDto>> getCompletedPotDetail(
            @PathVariable("pot_id") Long potId) {
        CompletedPotDetailResponseDto response = myPotService.getCompletedPotDetail(potId);
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
    @PostMapping("/{pot_id}/todos")
    public ResponseEntity<ApiResponse<List<MyPotTodoResponseDTO>>> postMyTodo(
            @PathVariable("pot_id") Long potId,
            @RequestBody MyPotTodoRequestDTO request) {

        List<MyPotTodoResponseDTO> response = myPotService.postTodo(potId, request);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    // 팟에서의 투두 조회
    @Operation(summary = "Todo 조회 API", description = "팟의 모든 멤버들의 todo 목록을 반환합니다. completed인 todo도 함께 반환하며, 새벽 3시에 자동 초기화됩니다.")
    @GetMapping("/{pot_id}/todos")
    public ResponseEntity<ApiResponse<List<MyPotTodoResponseDTO>>> getMyTodo(@PathVariable("pot_id") Long potId){
        List<MyPotTodoResponseDTO> response = myPotService.getTodo(potId);  // 수정된 부분
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(summary = "Todo 수정 API ", description = "사용자의 모든 투두의 내용을 한 번에 수정할 수 있습니다. 리스트를 통한 생성과 유사한 방식이지만 기존에 만들었던 todo의 경우 status를 유지해야 하기 때문에 todoId를 함께 보내주셔야 합니다. 새로 만드는 todo의 경우 todoId가 존재하지 않기 때문에 아무 정수나 넣어주시면 됩니다. 되도록 겹치지 않도록 1000이상으로 넣어주시면 좋을 것 같습니다")
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
