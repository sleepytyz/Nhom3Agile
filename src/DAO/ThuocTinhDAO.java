/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Model.ThuocTinh;
import Service.DBConnect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author win10
 */
public class ThuocTinhDAO {

    public boolean them(ThuocTinh tt) {
        // Kiểm tra tên đã tồn tại chưa
        String sqlCheck = "SELECT COUNT(*) FROM ThuocTinh WHERE TenThuocTinh = ?";
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sqlCheck)) {
            ps.setString(1, tt.getTenTT());
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                // Tên đã tồn tại
                JOptionPane.showMessageDialog(null, "Tên thuộc tính đã tồn tại.");
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // Nếu chưa tồn tại → thêm vào DB
        String sql = "INSERT INTO ThuocTinh (MaThuocTinh, TenThuocTinh, LoaiThuocTinh) VALUES (?, ?, ?)";
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, tt.getMaTT());
            ps.setString(2, tt.getTenTT());
            ps.setString(3, tt.getLoaiTT());

            int kq = ps.executeUpdate();
            return kq > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int suaTT(ThuocTinh tt) {
        String sql = "update ThuocTinh set\n"
                + "TenThuocTinh = ?, "
                + "LoaiThuocTinh = ?\n"
                + "WHERE MaThuocTinh = ?";    // Thêm khoảng trắng trước WHERE

        try {
            Connection con = DBConnect.getConnection();
            PreparedStatement pstm = con.prepareStatement(sql);
            pstm.setString(1, tt.getTenTT());
            pstm.setString(2, tt.getLoaiTT());
            pstm.setString(3, tt.getMaTT());

            if (pstm.executeUpdate() > 0) {
                return 1; // cập nhật thành công
            }
        } catch (Exception ex) {
            ex.printStackTrace(); // hiển thị lỗi nếu có
        }
        return 0; // cập nhật thất bại
    }

    public Object[] getRow(ThuocTinh tt) {
        String matt = tt.getMaTT();
        String tentt = tt.getTenTT();
        String loaitt = tt.getLoaiTT();

        Object[] row = new Object[]{matt, tentt, loaitt};
        return row;
    }

    public List<ThuocTinh> getAllTT() {
        List<ThuocTinh> listtt = new ArrayList<>();
        String sql = "SELECT * FROM ThuocTinh ORDER BY MaThuocTinh ASC"; // ← sắp xếp tăng dần
        try {
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            while (rs.next()) {
                String matt = rs.getString(1);
                String tentt = rs.getString(2);
                String loaitt = rs.getString(3);

                ThuocTinh tt = new ThuocTinh(matt, tentt, loaitt);
                listtt.add(tt);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return listtt;
    }

    public List<String> getTTTheoloai(String loaiThuocTinh) {
    List<String> list = new ArrayList<>();
    String sql = "SELECT TenThuocTinh FROM ThuocTinh WHERE LoaiThuocTinh = ? ORDER BY MaThuocTinh ASC";
    
    try (Connection conn = DBConnect.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        
        // Thiết lập tham số cho placeholder
        ps.setString(1, loaiThuocTinh);  // <-- Dòng 117
        
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("TenThuocTinh"));
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return list;
}

    public List<String> getAllChatLieu() {
        return getTTTheoloai("Chất liệu");
    }

    public List<String> getAllKichThuoc() {
        return getTTTheoloai("Kích thước");
    }

    public List<String> getAllLoaiSP() {
        return getTTTheoloai("Loại sản phẩm");
    }

    public List<String> getAllMauSac() {
        return getTTTheoloai("Màu sắc");
    }

    public String taoMaKT() {
        String maKT = "KT001"; // Mặc định nếu chưa có mã nào
        String sql = "SELECT MaThuocTinh FROM ThuocTinh WHERE MaThuocTinh LIKE 'KT%'";

        try (
                Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            int max = 0;
            while (rs.next()) {
                String makt = rs.getString("MaThuocTinh");
                if (makt != null && makt.startsWith("KT")) {
                    try {
                        int so = Integer.parseInt(makt.substring(2));
                        if (so > max) {
                            max = so;
                        }
                    } catch (NumberFormatException e) {
                        // Bỏ qua mã không đúng định dạng SPxxx
                    }
                }
            }
            maKT = String.format("KT%03d", max + 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return maKT;
    }

    public String taoMaMS() {
        String maMS = "MS001"; // Mặc định nếu chưa có mã nào
        String sql = "SELECT MaThuocTinh FROM ThuocTinh WHERE MaThuocTinh LIKE 'MS%'";

        try (
                Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            int max = 0;
            while (rs.next()) {
                String mams = rs.getString("MaThuocTinh");
                if (mams != null && mams.startsWith("MS")) {
                    try {
                        int so = Integer.parseInt(mams.substring(2));
                        if (so > max) {
                            max = so;
                        }
                    } catch (NumberFormatException e) {
                        // Bỏ qua mã không đúng định dạng MSxxx
                    }
                }
            }
            maMS = String.format("MS%03d", max + 1); // 
        } catch (Exception e) {
            e.printStackTrace();
        }

        return maMS;
    }

    public String taoMaLSP() {
        String maLSP = "LSP001"; // Mặc định nếu chưa có mã nào
        String sql = "SELECT MaThuocTinh FROM ThuocTinh WHERE MaThuocTinh LIKE 'LSP%'";

        try (
                Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            int max = 0;
            while (rs.next()) {
                String malsp = rs.getString("MaThuocTinh");
                if (malsp != null && malsp.startsWith("LSP")) {
                    try {
                        int so = Integer.parseInt(malsp.substring(3));
                        if (so > max) {
                            max = so;
                        }
                    } catch (NumberFormatException e) {

                    }
                }
            }
            maLSP = String.format("LSP%03d", max + 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return maLSP;
    }

    public String taoMaCL() {
        String maCL = "CL001"; // Mặc định nếu chưa có mã nào
        String sql = "SELECT MaThuocTinh FROM ThuocTinh WHERE MaThuocTinh LIKE 'CL%'";

        try (
                Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            int max = 0;
            while (rs.next()) {
                String macl = rs.getString("MaThuocTinh");
                if (macl != null && macl.startsWith("CL")) {
                    try {
                        int so = Integer.parseInt(macl.substring(2));
                        if (so > max) {
                            max = so;
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
            maCL = String.format("CL%03d", max + 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return maCL;
    }

    public List<ThuocTinh> locTT(String loai) {
        List<ThuocTinh> ds = new ArrayList<>();
        try {
            String sql = "SELECT * FROM ThuocTinh WHERE LoaiThuocTinh = ?";
            Connection con = DBConnect.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, loai);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ThuocTinh tt = new ThuocTinh();
                tt.setMaTT(rs.getString("MaThuocTinh"));
                tt.setTenTT(rs.getString("TenThuocTinh"));
                tt.setLoaiTT(rs.getString("LoaiThuocTinh"));
                ds.add(tt);
            }
            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }
    
    
    public List<ThuocTinh> getAllLoaiSanPham() {
    List<ThuocTinh> list = new ArrayList<>();
    String sql = "SELECT * FROM ThuocTinh WHERE LoaiThuocTinh = N'Loại sản phẩm'";

    try (Connection conn = DBConnect.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            String maTT = rs.getString("MaThuocTinh");
            String tenTT = rs.getString("TenThuocTinh");
            String loaiTT = rs.getString("LoaiThuocTinh");
            list.add(new ThuocTinh(maTT, tenTT, loaiTT));
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
    return list;
}



}
