package stackpot.stackpot.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import stackpot.stackpot.converter.UserConverter;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.service.UserCommandService;
import stackpot.stackpot.web.dto.UserRequestDto;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserCommandService userCommandService;
    @Operation(summary = "토큰 test api")
    @GetMapping("/login/token")
    public ResponseEntity<String> testEndpoint(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }
        return ResponseEntity.ok("Authenticated user: " + authentication.getName());
    }

    @Operation(summary = "회원가입 api")
    @PatchMapping("/users/profile")
    public ResponseEntity<?> signup(@RequestBody UserRequestDto.JoinDto request,
                                    BindingResult bindingResult) {
        // 유효성 검사 실패 처리
        if (bindingResult.hasErrors()) {
            // 에러 메시지 수집
            List<String> errors = bindingResult.getAllErrors()
                    .stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }

        System.out.println("uuuuuds : " + request.getKakaoId());
        // 정상 처리
        User user = userCommandService.joinUser(request);


        return ResponseEntity.status(HttpStatus.CREATED).body(UserConverter.toDto(user));
    }

}
