/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author XPS
 */
public class test {
    public static void main(String[] args) {
        final String from = "minhthth06876@gmail.com";
        final String pass = "iqmxhbsbdhdaaijg"; // App password

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, pass);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse("nguoinhan@gmail.com")
            );
            message.setSubject("Test từ Java");
            message.setText("Chào bạn, đây là email test từ Java Swing!");

            Transport.send(message);

            System.out.println("Đã gửi thành công!");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}


