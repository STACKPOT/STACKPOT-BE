package stackpot.stackpot.service.EmailService;

public interface EmailService {
    void sendSupportNotification(String toEmail, String potName, String applicantName);
}
