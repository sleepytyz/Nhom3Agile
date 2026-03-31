/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author XPS
 */
import Service.DBConnect;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class TestInsertImage {

    public static void main(String[] args) {
        String maSP = "SP0012"; // 🟦 Thay mã sản phẩm cần cập nhật
        String imagePath = "C:\\Users\\XPS\\Downloads\\anhspham\\anh6.jpg";

        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            System.out.println("❌ File ảnh không tồn tại: " + imagePath);
            return;
        }

        try (
                FileInputStream fis = new FileInputStream(imageFile); Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "UPDATE SanPham SET HinhAnh = ? WHERE MaSP = ?"
        )) {
            ps.setBinaryStream(1, fis, (int) imageFile.length());
            ps.setString(2, maSP);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Đã cập nhật ảnh cho sản phẩm: " + maSP);
            } else {
                System.out.println("❌ Không tìm thấy sản phẩm với mã: " + maSP);
            }

        } catch (Exception e) {
            System.out.println("❌ Lỗi khi cập nhật ảnh:");
            e.printStackTrace();
        }
    }
}
