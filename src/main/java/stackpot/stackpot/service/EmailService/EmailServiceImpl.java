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
    public void sendSupportNotification(String toEmail, String potName, String applicantName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("새로운 팟 지원 알림");
            message.setText(applicantName + " 님이 '" + potName + "' 팟에 지원했습니다!");
            mailSender.send(message);
        } catch (Exception e) {
            // 예외 처리: 이메일 전송 실패 시 로그를 출력
            System.err.println("이메일 전송 실패: " + e.getMessage());
        }
    }
}
