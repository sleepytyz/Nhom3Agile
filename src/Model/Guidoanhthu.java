/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class Guidoanhthu {
    private final String from = "minhthth06876@gmail.com"; // Gmail gửi
    private final String password = "xnazzfkzgjxsqzot";     // Mật khẩu ứng dụng Gmail

    // Tạo tiêu đề báo cáo theo kiểu thống kê
    public String taoTieuDeBaoCao(String kieu, LocalDate ngay, int thang, int nam) {
        switch (kieu) {
            case "ngay":
                return "📅 BÁO CÁO DOANH THU NGÀY " + ngay.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            case "thang":
                return "📅 BÁO CÁO DOANH THU THÁNG " + thang + "/" + nam;
            case "nam":
                return "📅 BÁO CÁO DOANH THU NĂM " + nam;
            default:
                return "📅 BÁO CÁO DOANH THU";
        }
    }

    // Tạo nội dung HTML báo cáo doanh thu
    public String taoNoiDungBaoCao(List<HoaDon> danhSach, double tongDoanhThu, String thoiGian) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h2>📊 BÁO CÁO DOANH THU ").append(thoiGian).append("</h2>");
        sb.append("<p><strong>Số hóa đơn:</strong> ").append(danhSach.size()).append("</p>");
        sb.append("<p><strong>Tổng doanh thu:</strong> ")
          .append(String.format("%,.0f", tongDoanhThu)).append(" VND</p>");

        sb.append("<br><table border='1' cellspacing='0' cellpadding='5'>");
        sb.append("<tr><th>Mã HĐ</th><th>Ngày tạo</th><th>Tổng tiền (VND)</th></tr>");
        for (HoaDon hd : danhSach) {
            sb.append("<tr>")
              .append("<td>").append(hd.getMahd()).append("</td>")
              .append("<td>").append(hd.getNgayTao()).append("</td>")
              .append("<td>").append(String.format("%,.0f", hd.getTongTien())).append("</td>")
              .append("</tr>");
        }
        sb.append("</table>");
        sb.append("<p>⏰ Thời gian gửi: ")
          .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))).append("</p>");
        return sb.toString();
    }

    // Hàm gửi báo cáo qua email
    public void guiBaoCaoEmail(String toEmail, String subject, String noiDungHTML) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setContent(noiDungHTML, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("✅ Gửi báo cáo thành công tới: " + toEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("❌ Gửi báo cáo thất bại! Kiểm tra địa chỉ email và kết nối mạng.");
        }
    }
}



