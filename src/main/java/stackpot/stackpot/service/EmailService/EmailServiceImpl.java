package stackpot.stackpot.service.EmailService;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendSupportNotification(String toEmail, String potName, String applicantName, String applicantIntroduction) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("[STACKPOT] 새로운 지원자가 있습니다 - '" + potName + "'");

            // 이메일 본문 작성
            // 이메일 본문 작성
            String emailBody = String.format(
                    "[%s]에 새로운 지원자가 있습니다!\n\n" +
                            "안녕하세요, STACKPOT에서 알려드립니다.\n\n" +
                            "회원님이 생성하신 [%s]에 새로운 지원자가 지원했습니다. 아래는 지원자 정보와 관련된 세부 사항입니다:\n\n" +
                            "- 지원자 이름: %s\n" +
                            "- 한 줄 소개: %s\n\n" +
                            "STACKPOT과 함께 성공적인 프로젝트를 만들어가세요!\n\n" +
                            "감사합니다.\n\n" +
                            "STACKPOT 드림\n\n" +
                            "고객센터: stackpot.notice@gmail.com\n" +
                            "홈페이지: https://www.stackpot.co.kr",
                    potName, potName, applicantName, applicantIntroduction != null ? applicantIntroduction : "없음"
            );

            message.setText(emailBody);
            mailSender.send(message);
        } catch (Exception e) {
            // 예외 처리: 이메일 전송 실패 시 로그를 출력
            System.err.println("이메일 전송 실패: " + e.getMessage());
        }
    }

}
