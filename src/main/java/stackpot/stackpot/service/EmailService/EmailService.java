package stackpot.stackpot.service.EmailService;

public interface EmailService {
    void sendSupportNotification(String toEmail, String potName, String applicantName, String applicantIntroduction);

    void sendPotDeleteNotification(String toEmail, String potName, String userName);
}
