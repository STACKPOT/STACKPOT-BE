package stackpot.stackpot.task.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.common.swagger.ApiErrorCodeExamples;
import stackpot.stackpot.task.dto.*;
import stackpot.stackpot.task.entity.enums.TaskboardStatus;
import stackpot.stackpot.task.service.TaskCommandService;
import stackpot.stackpot.task.service.TaskQueryService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Task Management", description = "Task 관리 API")
@RequestMapping(value = "/my-pots",produces = "application/json; charset=UTF-8")
public class TaskController {

    private final TaskCommandService taskCommandService;
    private final TaskQueryService taskQueryService;

    @PostMapping("/{pot_id}/tasks")
    @Operation(summary = "Task 생성 API",
            description = "participants는 참여자의 memberID를 ,로 구분하여 적어주시면 됩니다\n" +
                    "memberID는 [Pot Member Management] > [/pots/{pot_id}/members]를 통해 확인하실 수 있습니다."+
                    "ex) \"participants\": [1, 2]")
    @ApiErrorCodeExamples({
            ErrorStatus.POT_NOT_FOUND,
            ErrorStatus.POT_MEMBER_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<MyPotTaskResponseDto>> createPotTask(@PathVariable("pot_id") Long potId, @RequestBody @Valid MyPotTaskRequestDto.create request) {
        MyPotTaskResponseDto response = taskCommandService.createTask(potId, request);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }


    @GetMapping("/{pot_id}/tasks/{task_id}")
    @Operation(summary = "Task 상세 조회 API", description = "Task 상세 조회 API입니다. potId와 taskId를 통해 pot의 특정 task를 조회합니다.")
    @ApiErrorCodeExamples({
            ErrorStatus.POT_NOT_FOUND,
            ErrorStatus.POT_MEMBER_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<MyPotTaskResponseDto>> getPotDetailTask(@PathVariable("pot_id") Long potId, @PathVariable("task_id") Long taskId) {
        MyPotTaskResponseDto response = taskQueryService.viewDetailTask(potId, taskId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @GetMapping("/{pot_id}/tasks")
    @Operation(summary = "Task 조회 API",
            description = "Task 전체 조회 API입니다. potId를 통해 pot의 전체 task를 조회합니다.\n" +
                    " \"category는 참여한 멤버의 role로 자동으로 결정됩니다.\"")
    @ApiErrorCodeExamples({
            ErrorStatus.POT_NOT_FOUND,
            ErrorStatus.POT_MEMBER_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<Map<TaskboardStatus, List<MyPotTaskPreViewResponseDto>>>> getPotTask(@PathVariable("pot_id") Long potId) {
        Map<TaskboardStatus, List<MyPotTaskPreViewResponseDto>> response = taskQueryService.preViewTask(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @GetMapping("/{pot_id}/tasks/calendar")
    @Operation(summary = "캘린더 Task 조회 API",
            description = "특정 날짜 D-DAY Task들을 조회합니다. 각 Task의 참여자 목록이 포함됩니다.",
            parameters = {
                    @Parameter(name = "date", description = "yyyy-MM-dd 형식으로 작성해주세요.")
            })
    @ApiErrorCodeExamples({
            ErrorStatus.POT_NOT_FOUND,
            ErrorStatus.POT_MEMBER_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<List<MyPotTaskPreViewResponseDto>>> getPotTaskByDate(@PathVariable("pot_id") Long potId, @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        List<MyPotTaskPreViewResponseDto> response = taskQueryService.getTasksFromDate(potId, date);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @GetMapping("/{pot_id}/tasks/month")
    @Operation(summary = "월별 Task 조회 API",
            description = "특정 년/월의 모든 Task를 조회합니다. 현재 사용자가 참여중인 Task는 별도로 표시됩니다.")
    @ApiErrorCodeExamples({
            ErrorStatus.POT_NOT_FOUND,
            ErrorStatus.POT_MEMBER_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<List<MonthlyTaskDto>>> getMonthlyTasks(
            @PathVariable("pot_id") Long potId,
            @RequestParam("year") int year,
            @RequestParam("month") int month) {
        List<MonthlyTaskDto> response = taskQueryService.getMonthlyTasks(potId, year, month);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @PatchMapping("/{pot_id}/tasks/{task_id}")
    @Operation(summary = "Task 수정 API", description = "Task 수정 API입니다. 수정사항을 입력해 주세요. 만약 기존 내용을 유지하고자 한다면 Null 처리 해주세요 ")
    @ApiErrorCodeExamples({
            ErrorStatus.POT_NOT_FOUND,
            ErrorStatus.POT_MEMBER_NOT_FOUND,
            ErrorStatus.TASKBOARD_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<MyPotTaskResponseDto>> modifyPotTask(@PathVariable("pot_id") Long potId,@PathVariable("task_id") Long taskId, @RequestBody MyPotTaskRequestDto.create request) {
        MyPotTaskResponseDto response = taskCommandService.modifyTask(potId, taskId, request);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @DeleteMapping("/{pot_id}/tasks/{task_id}")
    @Operation(summary = "Task 삭제 API", description = "Task 삭제 API입니다. potId와 taskId를 pot의 특정 task를 삭제합니다.")
    @ApiErrorCodeExamples({
            ErrorStatus.POT_NOT_FOUND,
            ErrorStatus.POT_MEMBER_NOT_FOUND,
            ErrorStatus.TASKBOARD_NOT_FOUND
    })
    public ResponseEntity<?> deletetPotTask(@PathVariable("pot_id") Long potId, @PathVariable("task_id") Long taskId) {
        taskCommandService.deleteTaskBoard(potId, taskId);
        return ResponseEntity.ok(ApiResponse.onSuccess("할일이 삭제되었습니다."));
    }

    @PatchMapping("/{pot_id}/tasks/{task_id}/status")
    @Operation(summary = "Task 상태 변경 API", description = "taskId와 todoStatus(OPEN / IN_PROGRESS / CLOSED) 값을 전달해주시면 해당 업무가 요청한 상태로 변경됩니다.")
    @ApiErrorCodeExamples({
            ErrorStatus.POT_NOT_FOUND,
            ErrorStatus.POT_MEMBER_NOT_FOUND,
            ErrorStatus.TASKBOARD_NOT_FOUND,
            ErrorStatus.TASKBOARD_POT_MISMATCH
    })
    public ResponseEntity<ApiResponse<MyPotTaskStatusResponseDto>> modifyPotTask(@PathVariable("pot_id") Long potId, @PathVariable("task_id") Long taskId, TaskboardStatus status) {
        MyPotTaskStatusResponseDto response = taskCommandService.updateTaskStatus(potId, taskId, status);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
}
