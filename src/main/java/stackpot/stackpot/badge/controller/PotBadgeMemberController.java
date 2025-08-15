package stackpot.stackpot.badge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.badge.service.BadgeService;
import stackpot.stackpot.badge.service.PotBadgeMemberService;
import stackpot.stackpot.common.swagger.ApiErrorCodeExamples;
import stackpot.stackpot.todo.dto.MyPotTodoResponseDTO;
import stackpot.stackpot.todo.dto.MyPotTodoUpdateRequestDTO;
import stackpot.stackpot.todo.service.UserTodoService;
import stackpot.stackpot.badge.dto.PotBadgeMemberDto;

import java.util.List;

@RestController
@RequestMapping("/badges")
@RequiredArgsConstructor
@Tag(name = "Badge Management", description = "뱃지 관리 API")
public class PotBadgeMemberController {

    private final PotBadgeMemberService potBadgeMemberService;
    private final BadgeService badgeService;

    @Operation(summary = "특정 팟에서 뱃지를 받은 멤버 조회 API")
    @GetMapping("/pots/{pot_id}")
    public ResponseEntity<ApiResponse<List<PotBadgeMemberDto>>> getBadgeMembersByPotId(
            @PathVariable("pot_id") Long potId) {
        List<PotBadgeMemberDto> badgeMembers = potBadgeMemberService.getBadgeMembersByPotId(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(badgeMembers));
    }

    @Operation(summary = "팟에서 가장 많은 `투두를 완료한' 멤버에게 '할 일 정복자' 뱃지 부여")
    @PostMapping("/{potId}")
    @ApiErrorCodeExamples({
            ErrorStatus.BADGE_INSUFFICIENT_TODO_COUNTS,
            ErrorStatus.POT_MEMBER_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<Void>> assignBadgeToTopMembers(@PathVariable Long potId) {
        badgeService.assignBadgeToTopMembers(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }

    @Operation(summary = "전체 프로젝트 업무 수 대비 개인이 담당한 업무 수 비율이 큰 사람에게 '없어서는 안 될 능력자' 뱃지 부여")
    @PostMapping("/{potId}/task-badge")
    @ApiErrorCodeExamples({
            ErrorStatus.BADGE_NOT_FOUND,
            ErrorStatus.BADGE_INSUFFICIENT_TOP_MEMBERS,
            ErrorStatus.POT_MEMBER_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<Void>> assignTaskBadgeToTopMembers(@PathVariable Long potId) {
        badgeService.assignTaskBadgeToTopMembers(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }
}

