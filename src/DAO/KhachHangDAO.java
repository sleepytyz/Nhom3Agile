/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Model.KhachHang;
import Service.DBConnect;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;

/**
 *
 * @author XPS
 */
public class KhachHangDAO {

    public List<KhachHang> getAllKhachHangg() throws SQLException {
        List<KhachHang> list = new ArrayList<>();
        String sql = "SELECT * FROM KhachHang WHERE TrangThai = N'Khách mới' OR TrangThai = N'Khách quen' ORDER BY MaKH ASC;;";

        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                KhachHang kh = new KhachHang(
                        rs.getString("MaKH"),
                        rs.getString("TenKH"),
                        rs.getString("GioiTinh"),
                        rs.getString("SDT"),
                        rs.getString("TrangThai"),
                        rs.getString("DiaChi")
                );
                list.add(kh);
            }
        }
        return list;
    }

    public void insert(KhachHang kh) {
        String sql = "INSERT INTO KhachHang (MaKH, TenKH, GioiTinh, SDT, TrangThai, DiaChi) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (
                Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql);) {
            ps.setString(1, kh.getMakh());
            ps.setString(2, kh.getTenkh());
            ps.setString(3, kh.getGioiTinh());
            ps.setString(4, kh.getSdt());
            ps.setString(5, kh.getTrangThai());
            ps.setString(6, kh.getDiaChi());

            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi thêm khách hàng!", e);
        }
    }

    public List<KhachHang> getAllKhachHang() throws SQLException {
        List<KhachHang> list = new ArrayList<>();
        // Sắp xếp dựa trên số sau 'KH'
        String sql = "SELECT * FROM KhachHang "
                + "ORDER BY CAST(SUBSTRING(MaKH, 3, LEN(MaKH)) AS INT) ASC";

        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                KhachHang kh = new KhachHang(
                        rs.getString("MaKH"),
                        rs.getString("TenKH"),
                        rs.getString("GioiTinh"),
                        rs.getString("SDT"),
                        rs.getString("TrangThai"),
                        rs.getString("DiaChi")
                );
                list.add(kh);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return list;
    }

    public boolean insertKhachHang(KhachHang kh) throws SQLException {
        String sql = "INSERT INTO KhachHang (MaKH, TenKH, GioiTinh, SDT, TrangThai, DiaChi) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, kh.getMakh());
            ps.setString(2, kh.getTenkh());
            ps.setString(3, kh.getGioiTinh());
            ps.setString(4, kh.getSdt()); // SDT là khóa chính
            ps.setString(5, kh.getTrangThai());
            ps.setString(6, kh.getDiaChi());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            // Xử lý lỗi trùng khóa chính (duplicate primary key) nếu cần thiết
            if (e.getMessage().contains("Violation of PRIMARY KEY constraint")) {
                throw new SQLException("Số điện thoại " + kh.getSdt() + " đã tồn tại.", e);
            }
            e.printStackTrace();
            throw e;
        }
    }

    public String getLastMaKH() throws SQLException {
        String lastMaKH = null;
        // Ép phần số sau 'KH' thành INT để sắp xếp đúng
        String sql = "SELECT TOP 1 MaKH FROM KhachHang "
                + "ORDER BY CAST(SUBSTRING(MaKH, 3, LEN(MaKH)) AS INT) DESC";

        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                lastMaKH = rs.getString("MaKH");
            }
        }
        return lastMaKH;
    }

    public boolean isMaKHExist(String maKH) throws SQLException {
        String sql = "SELECT COUNT(*) FROM KhachHang WHERE MaKH = ?";

        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maKH);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // Trả về true nếu tìm thấy bản ghi
                }
            }
        }
        return false;
    }

    public boolean hasHoaDonBySDT(String sdt) {
        String sql = "SELECT COUNT(*) FROM HoaDon WHERE SDT = ?";
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sdt);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (Exception e) {
            System.out.println("Lỗi kiểm tra hóa đơn: " + e.getMessage());
            return false;
        }
    }

    public boolean updateKhachHang(KhachHang kh, String currentSdt) throws SQLException {
        String sql = "UPDATE KhachHang SET MaKH=?, TenKH=?, GioiTinh=?, TrangThai=?, DiaChi=?, SDT=? WHERE SDT=?";
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, kh.getMakh());
            ps.setString(2, kh.getTenkh());
            ps.setString(3, kh.getGioiTinh());
            ps.setString(4, kh.getTrangThai());
            ps.setString(5, kh.getDiaChi());
            ps.setString(6, kh.getSdt()); // SDT mới
            ps.setString(7, currentSdt); // SDT cũ (điều kiện WHERE)

            return ps.executeUpdate() > 0;
        }
    }

    public boolean isSdtExist(String sdt) throws SQLException {
        String sql = "SELECT COUNT(*) FROM KhachHang WHERE SDT=?";
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sdt);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // Phương thức cập nhật trạng thái khách hàng riêng biệt (dùng cho Khóa/Mở khóa)
    public boolean updateTrangThaiKhachHang(String sdt, String newStatus) throws SQLException {
        String sql = "UPDATE KhachHang SET TrangThai = ? WHERE SDT = ?";
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, sdt);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public boolean deleteKhachHang(String sdt) throws SQLException {
        String sql = "DELETE FROM KhachHang WHERE SDT = ?";
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, sdt);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            // Xử lý lỗi ràng buộc khóa ngoại nếu có hóa đơn liên quan
            if (e.getMessage().contains("The DELETE statement conflicted with the REFERENCE constraint")) {
                throw new SQLException("Không thể xóa khách hàng này vì có hóa đơn liên quan. Vui lòng xóa hóa đơn trước hoặc cập nhật khách hàng của hóa đơn.", e);
            }
            e.printStackTrace();
            throw e;
        }
    }

    public List<KhachHang> searchKhachHangBySDT(String sdtKeyword) throws SQLException {
        List<KhachHang> danhSach = new ArrayList<>();
        String sql = "SELECT MaKH, TenKH, GioiTinh, SDT, TrangThai, DiaChi FROM KhachHang WHERE SDT LIKE ?";

        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, sdtKeyword + "%"); // Tìm SDT bắt đầu bằng từ khoá

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    KhachHang kh = new KhachHang();
                    kh.setMakh(rs.getString("MaKH"));
                    kh.setTenkh(rs.getString("TenKH"));
                    kh.setGioiTinh(rs.getString("GioiTinh"));
                    kh.setSdt(rs.getString("SDT"));
                    kh.setTrangThai(rs.getString("TrangThai"));
                    kh.setDiaChi(rs.getString("DiaChi"));

                    danhSach.add(kh);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn khách hàng theo SDT: " + e.getMessage());
            throw e;
        }

        return danhSach;
    }

    // Phương thức tìm khách hàng theo SDT (để lấy chi tiết một khách hàng)
    public KhachHang getKhachHangBySdt(String sdt) throws SQLException {
        String sql = "SELECT MaKH, TenKH, GioiTinh, SDT, TrangThai, DiaChi FROM KhachHang WHERE SDT = ?";
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sdt);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new KhachHang(
                            rs.getString("MaKH"),
                            rs.getString("TenKH"),
                            rs.getString("GioiTinh"),
                            rs.getString("SDT"),
                            rs.getString("TrangThai"),
                            rs.getString("DiaChi")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return null;
    }

    public int getSoLuongHoaDonBySdt(String sdt) throws SQLException {
        int count = 0;
        String sql = "SELECT COUNT(mahd) FROM hoadon WHERE sdt = ?";
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sdt);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        }
        return count;
    }

    public List<KhachHang> searchKhachHangBySdtOrName(String searchTerm) throws SQLException {
        List<KhachHang> suggestions = new ArrayList<>();
        // Nếu bạn đang dùng SQL Server:
        String sql = "SELECT TOP 10 makh, tenkh, sdt, gioitinh, trangthai, diachi FROM khachhang WHERE sdt LIKE ? OR tenkh LIKE ?";

// Hoặc, nếu bạn không chắc chắn và muốn hỗ trợ cả hai, bạn có thể cân nhắc một biến hoặc cấu hình
// Nhưng hãy ưu tiên dùng đúng với DB bạn đang dùng. Giả định bạn dùng SQL Server với lỗi này.
        try (Connection conn = DBConnect.getConnection(); // Sử dụng DBConnect của bạn
                 PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + searchTerm + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern); // Tìm theo cả SĐT và Tên
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    KhachHang kh = new KhachHang();
                    kh.setMakh(rs.getString("makh"));
                    kh.setTenkh(rs.getString("tenkh"));
                    kh.setSdt(rs.getString("sdt"));
                    kh.setGioiTinh(rs.getString("gioitinh"));
                    kh.setTrangThai(rs.getString("trangthai"));
                    kh.setDiaChi(rs.getString("diachi"));
                    suggestions.add(kh);
                }
            }
        }
        return suggestions;
    }

}
