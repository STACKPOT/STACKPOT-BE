package stackpot.stackpot.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stackpot.stackpot.apiPayload.ApiResponse;
import stackpot.stackpot.service.PotBadgeMemberService;
import stackpot.stackpot.service.UserTodoService;
import stackpot.stackpot.web.dto.PotBadgeMemberDto;

import java.util.List;

@RestController
@RequestMapping("/badges")
@RequiredArgsConstructor
@Tag(name = "Badge Management", description = "뱃지 관리 API")
public class PotBadgeMemberController {

    private final PotBadgeMemberService potBadgeMemberService;
    private final UserTodoService userTodoService;

    @Operation(summary = "특정 팟에서 뱃지를 받은 멤버 조회 API")
    @GetMapping("/pots/{pot_id}")
    public ResponseEntity<ApiResponse<List<PotBadgeMemberDto>>> getBadgeMembersByPotId(
            @PathVariable("pot_id") Long potId) {

        List<PotBadgeMemberDto> badgeMembers = potBadgeMemberService.getBadgeMembersByPotId(potId);
        return ResponseEntity.ok(ApiResponse.onSuccess(badgeMembers));
    }


//    @Operation(summary = "팟에서 가장 많은 투두를 완료한 멤버에게 뱃지 부여")
//    @PostMapping("/assign-badge/{pot_id}")
//    public ResponseEntity<ApiResponse<Void>> assignBadgeToTopMembers(
//            @PathVariable("pot_id") Long potId) {
//
//        userTodoService.assignBadgeToTopMembers(potId);
//        return ResponseEntity.ok(ApiResponse.onSuccess(null));
//    }
}

