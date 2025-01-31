package stackpot.stackpot.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.service.UserTodoService;

@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class UserTodoController {

    private final UserTodoService userTodoService;

    @Operation(summary = "팟에서 가장 많은 투두를 완료한 멤버에게 뱃지 부여")
    @PostMapping("/assign-badge/{potId}")
    public ResponseEntity<ApiResponse<Void>> assignBadgeToTopMembers(
            @PathVariable Long potId) {

        userTodoService.assignBadgeToTopMembers(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }



}
