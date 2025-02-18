package stackpot.stackpot.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "My Pot Management", description = "나의 팟 관리 API")
@RequestMapping(value = "/my-pots",produces = "application/json; charset=UTF-8")
public class MyPotController {

    private final MyPotService myPotService;
    private final PotService potService;
    private final PotRepository potRepository;

    // 사용자가 만든 진행 중인 팟 조회
    @Operation(summary = "나의 팟 조회 API",
            description = "'나의 팟 첫 페이지' 정보를 반환하는 API입니다.\n\n" +
                    "- **사용자가 생성했거나 참여 중인 진행 중(`ONGOING`)인 팟을 조회**합니다.\n" +
                    "- **`isOwner` 값을 통해 사용자가 팟의 생성자인지 확인할 수 있습니다.**\n" +
                    "- **팟 생성일 기준 최신순으로 정렬**하여 반환됩니다.")
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<OngoingPotResponseDto>>> getMyPots() {
        List<OngoingPotResponseDto> response = myPotService.getMyPots();
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
    @Operation(summary = "마이페이지 끓인 팟 상세 보기 모달", description = "'끓인 팟 상세보기 모달'에 쓰이는 COMPLETED 상태인 팟의 상세 정보를 가져옵니다. 팟 멤버들의 userPotRole : num과 나의 역할도 함께 반환합니다.")
    public ResponseEntity<ApiResponse<CompletedPotDetailResponseDto>> getCompletedPotDetail(
            @PathVariable("pot_id") Long potId) {
        CompletedPotDetailResponseDto response = myPotService.getCompletedPotDetail(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @GetMapping("/badges")
    @Operation(summary = "나의 끓인 팟 조회 API (뱃지) - 마이페이지", description = "사용자가 참여한 potStatus가 COMPLETED 상태의 팟을 뱃지와 함께 반환합니다.")
    public ResponseEntity<ApiResponse<List<CompletedPotBadgeResponseDto>>> getCompletedPotsWithBadges() {
        List<CompletedPotBadgeResponseDto> response = myPotService.getCompletedPotsWithBadges();
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @GetMapping("/{user_id}/badges")
    @Operation(summary = "사용자별 끓인 팟 조회 API (뱃지) - 마이페이지", description = "userId를 통해 사용자별 참여한 potStatus가 COMPLETED 상태의 팟을 뱃지와 함께 반환합니다.")
    public ResponseEntity<ApiResponse<List<CompletedPotBadgeResponseDto>>> getUserCompletedPotsWithBadges(
            @PathVariable("user_id") Long userId
    ) {
        List<CompletedPotBadgeResponseDto> response = myPotService.getUserCompletedPotsWithBadges(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    /*// 팟에서의 투두 생성
    @Operation(
            summary = "Todo 생성 API",
            description = """
        - Status: NOT_STARTED / COMPLETED
        * 기본적으로 NOT_STARTED로 생성됩니다.
    """
    )
    @PostMapping("/{pot_id}/todos")
    public ResponseEntity<ApiResponse<List<MyPotTodoResponseDTO>>> postMyTodo(
            @PathVariable("pot_id") Long potId,
            @RequestBody MyPotTodoRequestDTO request) {

        List<MyPotTodoResponseDTO> response = myPotService.postTodo(potId, request);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }*/

    // 팟에서의 투두 조회
    @Operation(summary = "Todo 조회 API",
            description = "특정 팟에 속한 모든 멤버의 투두 목록을 반환하는 API입니다.\n\n" +
                    "- **완료된(`COMPLETED`) 투두도 함께 반환**됩니다.\n" +
                    "- **투두 목록은 매일 새벽 3시에 자동 초기화**됩니다.\n" +
                    "- **size=1은 한 명의 사용자를 의미**합니다.\n" +
                    "- **현재 접속 중인 사용자의 투두가 리스트의 첫 번째 요소로 반환**됩니다.")
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

    @PostMapping("/{pot_id}/tasks")
    @Operation(summary = "Task 생성 API", description = "Task를 생성합니다.")
    public ResponseEntity<ApiResponse<MyPotTaskResponseDto>> createPotTask(@PathVariable("pot_id") Long potId,
                                                                           @RequestBody @Valid MyPotTaskRequestDto.create request) {
        MyPotTaskResponseDto response = myPotService.creatTask(potId, request);

        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(summary = "Todo 생성 및 수정 API",
            description = "사용자의 모든 투두 내용을 한 번에 수정할 수 있는 API입니다. 이 API는 리스트를 통한 생성 방식과 유사하지만, 기존에 생성된 투두의 경우 " +
                    "status 값을 유지해야 하므로 todoId를 함께 보내야 합니다.\n\n" +
                    "- **기존 투두 수정**: `todoId`를 포함하여 요청해야 합니다. content만 수정 가능합니다.\n" +
                    "- **새로운 투두 생성**: `todoId`를 `null` 또는 `기존에 없었던` todoId로 보내면 새롭게 생성됩니다.\n" +
                    "- **status 필드** : Null, 아무값 처리 완료\n" +
                    "   - 기존 투두 : `기존의 값` 유지 \n" +
                    "   - 새로운 투두 : `NOT_STARTED`\n"+
                    "- **Example**: \"todoId\" : null")
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
    @GetMapping("/{pot_id}/tasks/{task_id}")
    @Operation(summary = "Task 상세 조회 API", description = "Task 상세 조회 API입니다. potId와 taskId를 통해 pot의 특정 task를 조회합니다.")
    public ResponseEntity<ApiResponse<MyPotTaskResponseDto>> getPotDetailTask(@PathVariable("pot_id") Long potId, @PathVariable("task_id") Long taskId) {
        MyPotTaskResponseDto response = myPotService.viewDetailTask(potId, taskId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @GetMapping("/{pot_id}/tasks")
    @Operation(summary = "Task 조회 API", description = "Task 전체 조회 API입니다. potId를 통해 pot의 전체 task를 조회합니다.")
    public ResponseEntity<ApiResponse<Map<TaskboardStatus, List<MyPotTaskPreViewResponseDto>>>> getPotTask(@PathVariable("pot_id") Long potId) {
        Map<TaskboardStatus, List<MyPotTaskPreViewResponseDto>> response = myPotService.preViewTask(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
    @GetMapping("/{pot_id}/tasks/calendar")
    @Operation(summary = "캘린더 Task 조회 API",
            description = "특정 날짜 이후의 Task들을 조회합니다. 각 Task의 참여자 목록이 포함됩니다.")
    public ResponseEntity<ApiResponse<List<MyPotTaskPreViewResponseDto>>> getPotTaskByDate(@PathVariable("pot_id") Long potId,
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        List<MyPotTaskPreViewResponseDto> response = myPotService.getTasksFromDate(potId, date);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @GetMapping("/{pot_id}/tasks/month")
    @Operation(summary = "월별 Task 조회 API",
            description = "특정 년/월의 모든 Task를 조회합니다. 현재 사용자가 참여중인 Task는 별도로 표시됩니다.")
    public ResponseEntity<ApiResponse<List<MonthlyTaskDto>>> getMonthlyTasks(
            @PathVariable("pot_id") Long potId,
            @RequestParam("year") int year,
            @RequestParam("month") int month) {

        List<MonthlyTaskDto> response = myPotService.getMonthlyTasks(potId, year, month);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @PatchMapping("/{pot_id}/tasks/{task_id}")
    @Operation(summary = "Task 수정 API", description = "Task 수정 API입니다. 수정사항을 입력해 주세요. 만약 기존 내용을 유지하고자 한다면 Null 처리 해주세요 ")
    public ResponseEntity<ApiResponse<MyPotTaskResponseDto>> modifyPotTask(@PathVariable("pot_id") Long potId,@PathVariable("task_id") Long taskId, @RequestBody MyPotTaskRequestDto.create request) {
        MyPotTaskResponseDto response = myPotService.modfiyTask(potId, taskId, request);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @DeleteMapping("/{pot_id}/tasks/{task_id}")
    @Operation(summary = "Task 삭제 API", description = "Task 삭제 API입니다. potId와 taskId를 pot의 특정 task를 삭제합니다.")
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

    @PatchMapping("/{pot_id}/tasks/{task_id}/status")
    @Operation(summary = "Task 상태 변경 API", description = "taskId와 todoStatus(OPEN / IN_PROGRESS / CLOSED) 값을 전달해주시면 해당 업무가 요청한 상태로 변경됩니다.")
    public ResponseEntity<ApiResponse<MyPotTaskStatusResponseDto>> modifyPotTask(@PathVariable("pot_id") Long potId, @PathVariable("task_id") Long taskId, TaskboardStatus status) {
        MyPotTaskStatusResponseDto response = myPotService.updateTaskStatus(potId, taskId, status);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
}
