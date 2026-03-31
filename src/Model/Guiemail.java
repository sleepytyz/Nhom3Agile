/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package Model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Guiemail {
    private static final Logger logger = Logger.getLogger(Guiemail.class.getName());
    private static final String CONFIG_PATH = "config/email.properties";
    private static final String DEFAULT_EMAIL = "minhthth06876@gmail.com";
    private static final String DEFAULT_PASSWORD = "xnazzfkzgjxsqzot";
    
    private final String from;
    private final String pass;

    public Guiemail() {
        Properties config = loadConfig();
        this.from = config.getProperty("email.account", DEFAULT_EMAIL);
        this.pass = config.getProperty("email.password", DEFAULT_PASSWORD);
        
        if (this.from.equals(DEFAULT_EMAIL) || this.pass.equals(DEFAULT_PASSWORD)) {
            logger.warning("Đang sử dụng email/password mặc định");
        }
    }

    private Properties loadConfig() {
        Properties config = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_PATH)) {
            if (input != null) {
                config.load(input);
            } else {
                logger.log(Level.WARNING, "Không tìm thấy file cấu hình: {0}", CONFIG_PATH);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Lỗi đọc file cấu hình: " + e.getMessage(), e);
        }
        return config;
    }

     public void sendMaXacNhan(String toEmail, String maXacNhan) throws MessagingException {
        if (!isValidEmail(toEmail)) {
            throw new IllegalArgumentException("Email không hợp lệ: " + toEmail);
        }

        Message message = createEmailMessage(toEmail, maXacNhan);
        Transport.send(message);
        logger.log(Level.INFO, "Đã gửi mã {0} tới: {1}", new Object[]{maXacNhan, toEmail});
    }
     

    private String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private Message createEmailMessage(String toEmail, String verificationCode) throws MessagingException {
        Message message = new MimeMessage(createMailSession());
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Mã xác nhận khôi phục mật khẩu");
        
        // Nội dung email đẹp hơn với HTML
        String htmlContent = "<html><body>"
                + "<h3>Mã xác nhận khôi phục mật khẩu</h3>"
                + "<p>Mã xác nhận của bạn là: <strong>" + verificationCode + "</strong></p>"
                + "<p>Mã có hiệu lực trong 5 phút.</p>"
                + "</body></html>";
        
        message.setContent(htmlContent, "text/html; charset=utf-8");
        return message;
    }

    private Session createMailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, pass);
            }
        });
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}