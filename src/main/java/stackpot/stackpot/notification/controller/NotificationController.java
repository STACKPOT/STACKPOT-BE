package stackpot.stackpot.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.common.swagger.ApiErrorCodeExamples;
import stackpot.stackpot.notification.dto.NotificationRequestDto;
import stackpot.stackpot.notification.dto.NotificationResponseDto;
import stackpot.stackpot.notification.service.NotificationCommandService;
import stackpot.stackpot.notification.service.NotificationQueryService;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationQueryService notificationQueryService;
    private final NotificationCommandService notificationCommandService;

    @Operation(summary = "미확인 알림 조회 API")
    @ApiErrorCodeExamples({
            ErrorStatus.AUTHENTICATION_FAILED
    })
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<NotificationResponseDto.UnReadNotificationDto>>> getAllUnReadNotifications() {
        return ResponseEntity.ok(ApiResponse.onSuccess(notificationQueryService.getAllUnReadNotifications()));
    }

    @Operation(summary = "알림 읽음 처리 API")
    @ApiErrorCodeExamples({
            ErrorStatus.NOTIFICATION_NOT_FOUND,
            ErrorStatus.INVALID_NOTIFICATION_TYPE
    })
    @PatchMapping("/read")
    public ResponseEntity<ApiResponse<Void>> readNotification(@RequestBody NotificationRequestDto.ReadNotificationDto readNotificationDto) {
        notificationCommandService.readNotification(readNotificationDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }
}
