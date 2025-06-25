package stackpot.stackpot.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.notification.dto.NotificationRequestDto;
import stackpot.stackpot.notification.dto.NotificationResponseDto;
import stackpot.stackpot.notification.service.NotificationCommandService;
import stackpot.stackpot.notification.service.NotificationQueryService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationQueryService notificationQueryService;
    private final NotificationCommandService notificationCommandService;

    @Operation(summary = "미확인 알림 조회 API")
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<NotificationResponseDto.UnReadNotificationDto>>> getAllUnReadNotifications() {
        return ResponseEntity.ok(ApiResponse.onSuccess(notificationQueryService.getAllUnReadNotifications()));
    }

    @Operation(summary = "알림 읽음 처리 API")
    @PatchMapping("/read")
    public ResponseEntity<ApiResponse<Void>> readNotification(@RequestBody NotificationRequestDto.ReadNotificationDto readNotificationDto) {
        notificationCommandService.readNotification(readNotificationDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }
}
