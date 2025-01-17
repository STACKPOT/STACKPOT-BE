package stackpot.stackpot.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import stackpot.stackpot.service.UserCommandService;
import stackpot.stackpot.web.controller.MemberViewController;
import stackpot.stackpot.web.dto.UserRequestDTO;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberViewController.class)
class MemberViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserCommandService userCommandService;

    @Test
    void testJoinUser_Success() throws Exception {
        // Mock request data
        UserRequestDTO.JoinDto joinDto = new UserRequestDTO.JoinDto();
        joinDto.setEmail("test@example.com");
        joinDto.setNickname("TestUser");
        joinDto.setKakaoId("12345");

        doNothing().when(userCommandService).joinUser(any(UserRequestDTO.JoinDto.class));

        mockMvc.perform(post("/user/profile")
                        .flashAttr("UserJoinDto", joinDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void testJoinUser_ValidationError() throws Exception {
        mockMvc.perform(post("/user/profile")
                        .param("email", "") // 잘못된 데이터
                        .param("nickname", "")
                        .param("kakaoId", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeExists("error"));
    }
}