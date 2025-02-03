package stackpot.stackpot.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.EnumHandler;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.enums.TaskboardStatus;
import stackpot.stackpot.repository.PotRepository.PotRepository;
import stackpot.stackpot.service.MyPotService;
import stackpot.stackpot.service.PotService;
import stackpot.stackpot.web.dto.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "My Pot Management", description = "나의 팟 관리 API")
@RequestMapping("/my-pots")
public class MyPotController {

    private final MyPotService myPotService;
    private final PotService potService;
    private final PotRepository potRepository;

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
    @Operation(summary = "끓인 팟 상세 보기", description = "'끓인 팟 상세보기 모달'에 쓰이는 COMPLETED 상태인 팟의 상세 정보를 가져옵니다. 팟 멤버들의 userPotRole : num과 나의 역할도 함께 반환합니다.")
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
    @Operation(summary = "Todo 조회 API", description = "팟의 모든 멤버들의 todo 목록을 반환합니다. completed인 todo도 함께 반환하며, 새벽 3시에 자동 초기화됩니다. size 1 = user 1명이라고 생각하시면 됩니다. 현재 접속 중인 사용자가 맨 처음 요소로 반환됩니다.")
    @GetMapping("/{pot_id}/todos")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyTodo(
            @PathVariable("pot_id") Long potId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        // 페이지 번호 보정 (1부터 시작하도록)
        if (page < 1) {
            throw new EnumHandler(ErrorStatus.INVALID_PAGE);
        }
        int adjustedPage = page - 1;
        Pot pot = potRepository.findById(potId)
                .orElseThrow(()->new IllegalArgumentException("pot을 찾을 수 없습니다."));

        // 서비스 호출하여 데이터 조회
        Page<MyPotTodoResponseDTO> pagedTodos = myPotService.getTodo(potId, PageRequest.of(adjustedPage, size));
        // 응답 데이터 구성
        Map<String, Object> response = new HashMap<>();
        response.put("potName", pot.getPotName());
        response.put("todos", pagedTodos.getContent());
        response.put("totalPages", pagedTodos.getTotalPages());
        response.put("currentPage", pagedTodos.getNumber() + 1);
        response.put("totalElements", pagedTodos.getTotalElements());

        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(summary = "Todo 수정 API ", description = "사용자의 모든 투두의 내용을 한 번에 수정할 수 있습니다. 리스트를 통한 생성과 유사한 방식이지만 기존에 만들었던 todo의 경우 status를 유지해야 하기 때문에 todoId를 함께 보내주셔야 합니다. 새로 만드는 todo의 경우 todoId가 존재하지 않지만 자동으로 생성되기 때문에 아무 정수나 넣어주시면 됩니다. 사용자의 todo 중 존재하는 todoId를 보내실 경우 해당 todoId가 수정되므로 되도록 연관이 없는 1000 이상의 숫자를 넣어주시는 게 좋습니다.")
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

    @Operation(summary = "Task 조회 API")
    @GetMapping("/{pot_id}/tasks")
    public ResponseEntity<?> getPotTask(@PathVariable("pot_id") Long potId) {
        Map<TaskboardStatus, List<MyPotTaskPreViewResponseDto>> response = myPotService.preViewTask(potId);
        return ResponseEntity.ok(response);
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
