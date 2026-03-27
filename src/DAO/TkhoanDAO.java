/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Model.Tkhoan;
import Service.DBConnect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;

/**
 *
 * @author XPS
 */
public class TkhoanDAO {

    public Object[] getRow(Tkhoan tk) {
        String tenTK = tk.getTentk();
        String matKhau = tk.getMkhau();
        String vaitro = tk.getVtro();

        Object[] row = new Object[]{tenTK, matKhau, vaitro};
        return row;
    }

    public List<Tkhoan> getAll() {
        List<Tkhoan> listtk = new ArrayList<>();
        String sql = "SELECT * FROM Tkhoan";
        try {
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            while (rs.next()) {
                String tenTK = rs.getString(1);
                String matKhau = rs.getString(2);
                String vaitro = rs.getString(3);
                Tkhoan tk = new Tkhoan(tenTK, matKhau, vaitro);
                listtk.add(tk);
            }
        } catch (Exception ex) {
        }

        return listtk;
    }

    public Map<String, Object> checkUser(String tenTK, String matKhau) {
        String sql = "SELECT vaitro, MaNV FROM Tkhoan WHERE TenTK = ? AND matkhau = ?";
        Map<String, Object> result = new HashMap<>();

        try (Connection con = DBConnect.getConnection()) {
            if (con == null) {
                System.out.println("❌ Không thể kết nối CSDL (Connection null)");
                return null;
            }

            try (PreparedStatement pstm = con.prepareStatement(sql)) {
                pstm.setString(1, tenTK);
                pstm.setString(2, matKhau);

                try (ResultSet rs = pstm.executeQuery()) {
                    if (rs.next()) {
                        result.put("vaitro", rs.getString("vaitro"));
                        result.put("ma_nv", rs.getString("MaNV"));
                        return result;
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("❌ Lỗi trong checkUser:");
            ex.printStackTrace();
        }

        return null;
    }

    public byte[] getUserImage(String tenTK) {
        byte[] imageData = null;
        try {
            Connection conn = DBConnect.getConnection();
            String sql = "SELECT anh FROM Tkhoan WHERE TenTK = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, tenTK);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                imageData = rs.getBytes("anh");
            }

            rs.close();
            ps.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageData;
    }

    public boolean updateMatKhau(String tenTK, String matKhauMoi) {
        String sql = "UPDATE Tkhoan SET matkhau = ? WHERE TenTK = ?";
        try (
                Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, matKhauMoi);
            ps.setString(2, tenTK);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public String getMatKhauByTenTK(String tenTK) {
    String sql = "SELECT matkhau FROM Tkhoan WHERE TenTK = ?";
    try (
        Connection con = DBConnect.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)
    ) {
        ps.setString(1, tenTK);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getString("matkhau");
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}
    
    public boolean kiemTraTonTaiTaiKhoan(String maNV) {
    String sql = "SELECT * FROM Tkhoan WHERE MaNV = ?";
    try (
        Connection con = DBConnect.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)
    ) {
        ps.setString(1, maNV);
        ResultSet rs = ps.executeQuery();
        return rs.next(); // Nếu có dòng -> đã có tài khoản
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}   
    public boolean insert(Tkhoan tk) {
    String sql = "INSERT INTO Tkhoan (TenTK, MaNV, matkhau, vaitro, anh) VALUES (?, ?, ?, ?, ?)";
    try (
        Connection con = DBConnect.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)
    ) {
        ps.setString(1, tk.getTentk());
        ps.setString(2, tk.getMaNV());
        ps.setString(3, tk.getMkhau());
        ps.setString(4, tk.getVtro());
        ps.setBytes(5, tk.getAnh()); // lưu ảnh

        return ps.executeUpdate() > 0;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}

}
