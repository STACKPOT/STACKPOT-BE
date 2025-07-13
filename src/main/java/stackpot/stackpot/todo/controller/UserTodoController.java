package stackpot.stackpot.todo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.EnumHandler;
import stackpot.stackpot.common.swagger.ApiErrorCodeExamples;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.pot.repository.PotRepository;
import stackpot.stackpot.todo.dto.MyPotTodoResponseDTO;
import stackpot.stackpot.todo.dto.MyPotTodoUpdateRequestDTO;
import stackpot.stackpot.todo.service.UserTodoService;


import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@Tag(name = "Todo Management", description = "Todo 관리 API")
@RequestMapping("/my-pots")
public class UserTodoController {

    private final UserTodoService userTodoService;
    private final PotRepository potRepository;

    @Operation(summary = "Todo 생성 및 수정 API",
            description = "사용자의 모든 투두 내용을 한 번에 수정할 수 있는 API입니다. 이 API는 리스트를 통한 생성 방식과 유사하지만, 기존에 생성된 투두의 경우 " +
                    "status 값을 유지해야 하므로 todoId를 함께 보내야 합니다.\n\n" +
                    "- **기존 투두 수정**: `todoId`를 포함하여 요청해야 합니다. content만 수정 가능합니다.\n" +
                    "- **새로운 투두 생성**: `todoId`를 `null` 또는 `기존에 없었던` todoId로 보내면 새롭게 생성됩니다.\n" +
                    "- **status 필드** : Null, 아무값 처리 완료\n" +
                    "   - 기존 투두 : `기존의 값` 유지 \n" +
                    "   - 새로운 투두 : `NOT_STARTED`\n"+
                    "- **Example**: \"todoId\" : null")
    @ApiErrorCodeExamples({
            ErrorStatus.POT_NOT_FOUND
    })
    @PatchMapping("/{pot_id}/todos")
    public ResponseEntity<ApiResponse<List<MyPotTodoResponseDTO>>> updateMyTodos(
            @PathVariable("pot_id") Long potId,
            @RequestBody List<MyPotTodoUpdateRequestDTO> requestList) {

        List<MyPotTodoResponseDTO> response = userTodoService.updateTodos(potId, requestList);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(summary = "Todo 완료 / 해제 API", description = "todo의 status를 토글 형식으로 COMPLETED / NOT_STARTED로 변경합니다.")
    @ApiErrorCodeExamples({
            ErrorStatus.POT_NOT_FOUND,
    })
    @PatchMapping("/{pot_id}/todos/{todo_id}")
    public ResponseEntity<ApiResponse<List<MyPotTodoResponseDTO>>> completeTodo(
            @PathVariable("pot_id") Long potId,
            @PathVariable("todo_id") Long todoId) {

        List<MyPotTodoResponseDTO> response = userTodoService.completeTodo(potId, todoId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    // 팟에서의 투두 조회
    @Operation(summary = "Todo 조회 API",
            description = "특정 팟에 속한 모든 멤버의 투두 목록을 반환하는 API입니다.\n\n" +
                    "- **완료된(`COMPLETED`) 투두도 함께 반환**됩니다.\n" +
                    "- **투두 목록은 매일 새벽 3시에 자동 초기화**됩니다.\n" +
                    "- **size=1은 한 명의 사용자를 의미**합니다.\n" +
                    "- **현재 접속 중인 사용자의 투두가 리스트의 첫 번째 요소로 반환**됩니다.")
    @ApiErrorCodeExamples({
            ErrorStatus.POT_NOT_FOUND,
            ErrorStatus.POT_FORBIDDEN
    })
    @GetMapping("/{pot_id}/todos")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyTodo(
            @PathVariable("pot_id") Long potId,
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name="size", defaultValue = "10") Integer size) {

        // 페이지 번호 보정 (1부터 시작하도록)
        if (page < 1) {
            throw new EnumHandler(ErrorStatus.INVALID_PAGE);
        }
        int adjustedPage = page - 1;
        Pot pot = potRepository.findById(potId)
                .orElseThrow(()->new IllegalArgumentException("pot을 찾을 수 없습니다."));

        // 서비스 호출하여 데이터 조회
        Page<MyPotTodoResponseDTO> pagedTodos = userTodoService.getTodo(potId, PageRequest.of(adjustedPage, size));
        // 응답 데이터 구성
        Map<String, Object> response = new HashMap<>();
        response.put("potName", pot.getPotName());
        response.put("todos", pagedTodos.getContent());
        response.put("totalPages", pagedTodos.getTotalPages());
        response.put("currentPage", pagedTodos.getNumber() + 1);
        response.put("totalElements", pagedTodos.getTotalElements());

        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
}
