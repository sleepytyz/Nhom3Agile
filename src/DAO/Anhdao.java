/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Service.DBConnect;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class Anhdao {
    public static void insertImage(String tenTK, File imageFile) {
        String sql = "UPDATE Tkhoan SET anh = ? WHERE TenTK = ?";
        try (
            Connection con = DBConnect.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            FileInputStream fis = new FileInputStream(imageFile)
        ) {
            ps.setBinaryStream(1, fis, (int) imageFile.length());
            ps.setString(2, tenTK);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Đã chèn ảnh cho tài khoản: " + tenTK);
            } else {
                System.out.println("⚠️ Không tìm thấy tài khoản: " + tenTK);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}




