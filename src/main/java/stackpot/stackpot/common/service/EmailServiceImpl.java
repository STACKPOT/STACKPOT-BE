package stackpot.stackpot.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendSupportNotification(String toEmail, String potName,String applicantName,String appliedRoleName, String appliedRole, String applicantIntroduction) {

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("[STACKPOT] 새로운 지원자가 있습니다 -'" + potName + "'");

            String emailBody = String.format(
                    "[%s]에 새로운 지원자가 있습니다!\n\n" +
                            "안녕하세요, STACKPOT에서 알려드립니다.\n\n" +
                            "회원님이 생성하신 [%s]에 새로운 지원자가 지원했습니다. 아래는 지원자 정보와 관련된 세부 사항입니다:\n\n" +
                            "- 지원자 이름: %s\n" +
                            "- 지원 파트: %s(%s)\n" +
                            "- 한 줄 소개: %s\n\n" +
                            "STACKPOT과 함께 성공적인 프로젝트를 만들어가세요!\n\n" +
                            "감사합니다.\n\n" +
                            "STACKPOT 드림\n\n" +
                            "고객센터: stackpot.notice@gmail.com\n" +
                            "홈페이지: https://www.stackpot.co.kr",
                    potName, potName, applicantName,appliedRoleName, appliedRole, applicantIntroduction != null ? applicantIntroduction : "없음"
            );

            message.setText(emailBody);
            mailSender.send(message);

    }

    @Override
    public void sendPotDeleteNotification(String toEmail, String potName, String userName){

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("[STACKPOT] 팟이 삭제되었음을 알려드립니다.");

            // 이메일 본문 작성
            String emailBody = String.format(
                    "안녕하세요, %s님.\n\n" +
                            "참여 중이던 ”[%s]” 프로젝트가 삭제되었습니다.\n" +
                            "해당 프로젝트는 팀장의 탈퇴로 인해 자동 종료되었으며, 관련된 모든 정보가 정리되었습니다.\n\n\n" +

                            "- 프로젝트 종료 사유\n" +
                            "해당 프로젝트는 팀장의 탈퇴로 인해 더 이상 운영이 어려워 종료되었습니다.\n\n" +

                            "- 추가 안내\n" +
                            "프로젝트에 대한 궁금한 사항이 있다면, 함께했던 팀원들과 논의해보시길 바랍니다.\n" +
                            "기존에 등록된 데이터(게시물, 작업 등)는 더 이상 접근할 수 없습니다.\n\n" +

                            "❗새로운 프로젝트에 도전해보세요 \n" +
                            "다양한 프로젝트가 진행 중이니, 새로운 기회를 찾아보세요! 😊\n" +
                            "감사합니다.\n\n" +

                            "STACKPOT 드림\n\n" +
                            "고객센터: stackpot.notice@gmail.com\n" +
                            "홈페이지: https://www.stackpot.co.kr",
                    userName, potName
            );



            message.setText(emailBody);
            mailSender.send(message);




    }

}
