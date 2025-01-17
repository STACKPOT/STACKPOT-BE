package stackpot.stackpot.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import stackpot.stackpot.service.UserCommandService;
import stackpot.stackpot.web.dto.UserRequestDTO;

@Controller
@RequiredArgsConstructor
public class MemberViewController {

    private final UserCommandService userCommandService;

    @PostMapping("/user/profile")
    public String joinUser(@ModelAttribute("UserJoinDto") UserRequestDTO.JoinDto request,
                           BindingResult bindingResult,
                           Model model) {
        if (bindingResult.hasErrors()) {
            return "signup";
        }

        try {
            userCommandService.joinUser(request);
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "signup";
        }
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/user/profile")
    public String signupPage(@RequestParam(required = false) String email, Model model) {
        model.addAttribute("email", email); // 카카오 이메일 전달
        return "signup";
    }


}
