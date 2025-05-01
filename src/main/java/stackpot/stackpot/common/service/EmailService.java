package stackpot.stackpot.common.service;

public interface EmailService {
    void sendSupportNotification(String toEmail, String potName, String applicantName, String applicantRoleName,String appliedRole,String applicantIntroduction);

    void sendPotDeleteNotification(String toEmail, String potName, String userName);
}
