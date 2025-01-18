package stackpot.stackpot.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import stackpot.stackpot.config.security.JwtTokenProvider;
import stackpot.stackpot.service.UserCommandService;
import stackpot.stackpot.web.dto.UserRequestDTO;

@Controller
@RequiredArgsConstructor
public class MemberViewController {

    private final UserCommandService userCommandService;
    private final JwtTokenProvider jwtTokenProvider;

    // 로그인 페이지
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

//     회원가입 페이지 랜더링
    @GetMapping("/signup")
    public String signupPage() {
        // 단순히 회원가입 페이지를 렌더링
        return "signup";
    }

    // 회원가입 처리
    @PostMapping("/user/signup")
    public String joinUser(@Valid @ModelAttribute("UserJoinDTO") UserRequestDTO.JoinDto request,
                           BindingResult bindingResult,
                           @RequestHeader(value = "Authorization", required = false) String token,
                           Model model) {
        if (bindingResult.hasErrors()) {
            return "signup"; // 유효성 검사 실패 시 다시 회원가입 페이지로
        }

        try {
            if (token == null || !token.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Authorization header is missing or invalid.");
            }

            // JWT에서 이메일 추출
            String email = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
            request.setEmail(email); // DTO에 이메일 설정
            System.out.println("Extracted Email: " + email);


            // 회원가입 처리
            userCommandService.joinUser(request);
            return "redirect:/home"; // 성공 시 홈 페이지로 리다이렉트
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "signup"; // 에러 발생 시 다시 회원가입 페이지로
        }
    }
}
