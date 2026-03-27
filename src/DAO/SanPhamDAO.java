/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Model.KhuyenMai;
import Model.SanPham;
import Model.SanPhamGoiY;
import Model.StringUtils;
import Service.DBConnect;
import java.awt.Image;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.*;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 *
 * @author XPS
 */
public class SanPhamDAO {

    public Object[] getRow(SanPham sp) {
        DecimalFormat df = new DecimalFormat("#.##");

        ImageIcon icon = null;
        if (sp.getHinhAnh() != null) {
            Image img = new ImageIcon(sp.getHinhAnh()).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            icon = new ImageIcon(img);
        }

        return new Object[]{
            sp.getMasp(),
            sp.getTensp(),
            sp.getLoaisp(),
            df.format(sp.getGia()),
            sp.getSluong(),
            sp.getMausac(),
            sp.getKichThuoc(),
            sp.getChatLieu(),
            sp.getTrangThai(),
            icon // ✅ Cột cuối là ảnh dạng ImageIcon
        };
    }

   // Trong file SanPhamDAO.java
public List<SanPhamGoiY> goiYTenSanPham(String keyword) {
    List<SanPhamGoiY> list = new ArrayList<>();
    String tuKhoaTimKiem = keyword.toLowerCase();
    
    // Câu lệnh SQL đã được tối ưu để tìm kiếm không dấu và không phân biệt chữ hoa/thường
    String sql = "SELECT TenSP, HinhAnh FROM SanPham WHERE LOWER(TenSP) LIKE ? COLLATE SQL_Latin1_General_CP1_CI_AI ORDER BY CAST(SUBSTRING(MaSP, 3, LEN(MaSP)) AS INT)";
    
    System.out.println("Từ khóa tìm kiếm (không dấu): " + tuKhoaTimKiem);
    
    try (Connection con = DBConnect.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
        
        String param = "%" + tuKhoaTimKiem + "%";
        System.out.println("Tham số truy vấn: " + param);
        
        ps.setString(1, param);
        
        ResultSet rs = ps.executeQuery();
        
        int count = 0;
        while (rs.next()) {
            count++;
            list.add(new SanPhamGoiY(
                    rs.getString("TenSP"),
                    rs.getBytes("HinhAnh")
            ));
        }
        System.out.println("Số lượng kết quả từ database: " + count);
        
    } catch (Exception e) {
        System.err.println("Có lỗi xảy ra khi truy vấn database:");
        e.printStackTrace();
    }
    return list;
}

    public List<SanPham> getByLoai(String maLoai) {
        List<SanPham> list = new ArrayList<>();
        String sql = "SELECT * FROM SanPham WHERE LoaiSP = ?";

        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maLoai);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SanPham sp = new SanPham(
                            rs.getString("MaSP"),
                            rs.getString("TenSP"),
                            rs.getString("LoaiSP"),
                            rs.getFloat("DonGia"),
                            rs.getInt("SoLuong"),
                            rs.getString("MauSac"),
                            rs.getString("KichThuoc"),
                            rs.getString("ChatLieu"),
                            rs.getString("TrangThai"),
                            rs.getBytes("HinhAnh")
                    );
                    list.add(sp);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi lấy dữ liệu sản phẩm theo loại: " + e.getMessage());
        }
        return list;
    }

    public ArrayList<SanPham> getAll1() {
        ArrayList<SanPham> list = new ArrayList<>();
        String sql = "SELECT * FROM SanPham\n"
                + "ORDER BY CAST(SUBSTRING(MaSP, 3, LEN(MaSP)) AS INT)";
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SanPham sp = new SanPham(
                        rs.getString("MaSP"),
                        rs.getString("TenSP"),
                        rs.getFloat("DonGia"),
                        rs.getInt("SoLuong"),
                        rs.getBytes("HinhAnh")
                );
                list.add(sp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<SanPham> getSanPhamDangHoatDong() {
        List<SanPham> ds = new ArrayList<>();
        String sql = "SELECT * FROM SanPham WHERE TrangThai != N'Đã khóa'";
        try (
                Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery();) {
            while (rs.next()) {
                SanPham sp = new SanPham();
                sp.setMasp(rs.getString("MaSP"));
                sp.setTensp(rs.getString("TenSP"));
                sp.setLoaisp(rs.getString("LoaiSP"));
                sp.setGia(rs.getFloat("DonGia"));
                sp.setSluong(rs.getInt("SoLuong"));
                sp.setMausac(rs.getString("MauSac"));
                sp.setKichThuoc(rs.getString("KichThuoc"));
                sp.setChatLieu(rs.getString("ChatLieu"));
                sp.setTrangThai(rs.getString("TrangThai"));
                sp.setHinhAnh(rs.getBytes("HinhAnh"));
                ds.add(sp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    public List<Object[]> getAllWithDiscount() {
    List<Object[]> list = new ArrayList<>();
    String sql
            = "SELECT sp.MaSP, sp.TenSP, sp.LoaiSP, sp.DonGia, sp.SoLuong, "
            + "       ISNULL(km_max.MaxGiamGia, 0) * 100 AS TongGiamGiaPT, "
            + "       ISNULL(km_max.MaKM, '') AS MaKM, "  // thêm lấy mã KM
            + "       sp.MauSac, sp.KichThuoc, sp.ChatLieu, sp.TrangThai, sp.HinhAnh "
            + "FROM SanPham sp "
            + "LEFT JOIN ( "
            + "    SELECT ct.MaSP, MAX(k.GiamGia) AS MaxGiamGia, MAX(k.MaKM) AS MaKM "  // thêm lấy MaKM
            + "    FROM KhuyenMaiChiTiet ct "
            + "    JOIN KhuyenMai k ON ct.MaKM = k.MaKM "
            + "    WHERE CAST(GETDATE() AS date) BETWEEN CAST(k.NgayBdau AS date) AND CAST(k.NgayKthuc AS date) "
            + "      AND k.TrangThai = N'Đang hoạt động' "
            + "    GROUP BY ct.MaSP "
            + ") km_max ON sp.MaSP = km_max.MaSP "
            + "ORDER BY CAST(RIGHT(sp.MaSP, LEN(sp.MaSP) - 2) AS INT) ASC;";

    try (Connection con = DBConnect.getConnection(); Statement stm = con.createStatement(); ResultSet rs = stm.executeQuery(sql)) {

        while (rs.next()) {
            byte[] hinhAnh = null;
            try {
                hinhAnh = rs.getBytes("HinhAnh");
            } catch (SQLException ex) {
                // nếu không có cột HinhAnh hoặc null thì bỏ qua
            }

            float tongGiamPT = rs.getFloat("TongGiamGiaPT"); // ví dụ 10.0
            list.add(new Object[]{
                rs.getString("MaSP"),
                rs.getString("TenSP"),
                rs.getString("LoaiSP"),
                rs.getFloat("DonGia"),
                rs.getInt("SoLuong"),
                tongGiamPT + "%",     // hiển thị 10%
                rs.getString("MaKM"), // cột Mã KM mới
                rs.getString("MauSac"),
                rs.getString("KichThuoc"),
                rs.getString("ChatLieu"),
                rs.getString("TrangThai"),
                hinhAnh
            });
        }
    } catch (Exception ex) {
        ex.printStackTrace();
    }
    return list;
}


    public List<SanPham> getAll() {
        List<SanPham> listSP = new ArrayList<>();
        String sql = "SELECT * FROM SanPham ORDER BY CAST(RIGHT(MaSP, LEN(MaSP)-2) AS INT) ASC;";
        try (
                Connection con = DBConnect.getConnection(); Statement stm = con.createStatement(); ResultSet rs = stm.executeQuery(sql)) {

            while (rs.next()) {
                String masp = rs.getString("MaSP");
                String tensp = rs.getString("TenSP");
                String loaisp = rs.getString("LoaiSP");
                float gia = rs.getFloat("DonGia");
                int sluong = rs.getInt("SoLuong");
                String mausac = rs.getString("MauSac");
                String kichThuoc = rs.getString("KichThuoc");
                String chatLieu = rs.getString("ChatLieu");

                // Gán trạng thái: nếu hết hàng thì ghi rõ
                String trangThai;
                if (sluong == 0) {
                    trangThai = "Hết hàng";
                } else {
                    trangThai = rs.getString("TrangThai");
                }

                byte[] hinhAnh = rs.getBytes("HinhAnh");

                SanPham sp = new SanPham(masp, tensp, loaisp, gia, sluong, mausac, kichThuoc, chatLieu, trangThai, hinhAnh);
                listSP.add(sp);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return listSP;
    }

    public List<SanPham> getAll2() {
        List<SanPham> listSP = new ArrayList<>();
        String sql = "SELECT * FROM SanPham ORDER BY CAST(RIGHT(MaSP, LEN(MaSP)-2) AS INT) ASC;";
        try {
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            while (rs.next()) {
                String masp = rs.getString("MaSP");
                String tensp = rs.getString("TenSP");
                String loaisp = rs.getString("LoaiSP");
                float gia = rs.getFloat("DonGia");
                int sluong = rs.getInt("SoLuong");
                String mausac = rs.getString("MauSac");
                String kichThuoc = rs.getString("KichThuoc");
                String chatLieu = rs.getString("ChatLieu");
                String trangThai = rs.getString("TrangThai");

                // 👇 Lấy ảnh dạng byte[]
                byte[] hinhAnh = rs.getBytes("HinhAnh");

                // 👇 Khởi tạo SanPham có thêm ảnh
                SanPham sp = new SanPham(masp, tensp, loaisp, gia, sluong, mausac, kichThuoc, chatLieu, trangThai, hinhAnh);
                listSP.add(sp);
            }
        } catch (Exception ex) {
            ex.printStackTrace(); // nên in lỗi để dễ debug
        }

        return listSP;
    }

    public void capNhatTonKho(String maSP, int soLuongCongThem) {
        String sql = "UPDATE SanPham SET Soluong = Soluong + ? WHERE MaSP = ?";
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, soLuongCongThem);
            ps.setString(2, maSP);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int themSP(SanPham sp, long soLuong) {
        String sql = "INSERT INTO SanPham (MaSP, TenSP, LoaiSP, DonGia, SoLuong, MauSac, KichThuoc, ChatLieu, TrangThai, HinhAnh) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection con = DBConnect.getConnection();
            PreparedStatement pstm = con.prepareStatement(sql);
            pstm.setString(1, sp.getMasp());
            pstm.setString(2, sp.getTensp());
            pstm.setString(3, sp.getLoaisp());
            pstm.setBigDecimal(4, BigDecimal.valueOf(sp.getGia()));
            pstm.setLong(5, soLuong); // ✅ dùng long thay vì int
            pstm.setString(6, sp.getMausac());
            pstm.setString(7, sp.getKichThuoc());
            pstm.setString(8, sp.getChatLieu());
            pstm.setString(9, sp.getTrangThai());
            pstm.setBytes(10, sp.getHinhAnh()); // truyền ảnh dưới dạng byte[]

            if (pstm.executeUpdate() > 0) {
                return 1;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public int suaSP(SanPham sp, String maSP, long soLuong) {
        String sql = "UPDATE SanPham SET "
                + "MaSP = ?, "
                + "TenSP = ?, "
                + "LoaiSP = ?, "
                + "DonGia = ?, "
                + "SoLuong = ?, "
                + "MauSac = ?, "
                + "KichThuoc = ?, "
                + "ChatLieu = ?, "
                + "TrangThai = ?, "
                + "HinhAnh = ? "
                + "WHERE MaSP = ?";
        try {
            Connection con = DBConnect.getConnection();
            PreparedStatement pstm = con.prepareStatement(sql);
            pstm.setString(1, sp.getMasp());
            pstm.setString(2, sp.getTensp());
            pstm.setString(3, sp.getLoaisp());
            pstm.setBigDecimal(4, BigDecimal.valueOf(sp.getGia()));
            pstm.setLong(5, soLuong); // ✅ dùng long thay vì int
            pstm.setString(6, sp.getMausac());
            pstm.setString(7, sp.getKichThuoc());
            pstm.setString(8, sp.getChatLieu());
            pstm.setString(9, sp.getTrangThai());
            pstm.setBytes(10, sp.getHinhAnh());
            pstm.setString(11, maSP);

            if (pstm.executeUpdate() > 0) {
                return 1;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public int xoaSP(String maSP) {
        String sql = "DELETE FROM SanPham WHERE MaSP = ? AND MaSP NOT IN (SELECT MaSP FROM HoaDonChiTiet)";

        try {
            Connection con = DBConnect.getConnection();
            PreparedStatement pstm = con.prepareStatement(sql);
            pstm.setString(1, maSP);

            if (pstm.executeUpdate() > 0) {
                return 1;
            }
        } catch (Exception ex) {
            ex.printStackTrace(); // Nên log lỗi để kiểm tra
        }

        return 0; // Không xóa được
    }

    public List<SanPham> timSPTheoTen(String tenSP) {
    List<SanPham> ds = new ArrayList<>();
    String sql = "SELECT * FROM SanPham WHERE TenSP LIKE ?";  // sửa từ = thành LIKE
    
    try (Connection conn = DBConnect.getConnection();
         PreparedStatement st = conn.prepareStatement(sql)) {
        
        String searchTerm = "%" + tenSP + "%";  // Giữ nguyên dấu, không xóa khoảng trắng
        st.setString(1, searchTerm);
        
        ResultSet rs = st.executeQuery();
        
        while (rs.next()) {
            SanPham sp = new SanPham();
            sp.setMasp(rs.getString("MaSP"));
            sp.setTensp(rs.getString("TenSP"));
            sp.setLoaisp(rs.getString("LoaiSP"));
            sp.setGia(rs.getFloat("DonGia"));
            sp.setSluong(rs.getInt("SoLuong"));
            sp.setMausac(rs.getString("MauSac"));
            sp.setKichThuoc(rs.getString("KichThuoc"));
            sp.setChatLieu(rs.getString("ChatLieu"));
            sp.setTrangThai(rs.getString("TrangThai"));
            sp.setHinhAnh(rs.getBytes("HinhAnh"));
            
            ds.add(sp);
        }
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Lỗi khi tìm sản phẩm: " + e.getMessage(), 
            "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
    
    return ds;
}


    public int khoaSP(String maSP) {
        String sql = "UPDATE SanPham SET TrangThai = N'Đã khóa' WHERE MaSP = ?";

        try {
            Connection con = DBConnect.getConnection();
            PreparedStatement pstm = con.prepareStatement(sql);
            pstm.setString(1, maSP);

            if (pstm.executeUpdate() > 0) {
                return 1; // Khóa thành công
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0; // Khóa thất bại
    }

    public int moKhoaSP(String maSP, String trangThaiCu) {
        String sql = "UPDATE SanPham SET TrangThai = ? WHERE MaSP = ?";

        try {
            Connection con = DBConnect.getConnection();
            PreparedStatement pstm = con.prepareStatement(sql);
            pstm.setString(1, trangThaiCu); // Trạng thái trước khi bị khoá
            pstm.setString(2, maSP);

            if (pstm.executeUpdate() > 0) {
                return 1;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public SanPham timSPTheoMa(String maSP) {
        String sql = "SELECT * FROM SanPham WHERE MaSP = ?";
        try (Connection con = DBConnect.getConnection(); PreparedStatement pstm = con.prepareStatement(sql)) {

            pstm.setString(1, maSP);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                return new SanPham(
                        rs.getString("MaSP"),
                        rs.getString("TenSP"),
                        rs.getString("LoaiSP"),
                        rs.getFloat("DonGia"),
                        rs.getInt("SoLuong"),
                        rs.getString("MauSac"),
                        rs.getString("KichThuoc"),
                        rs.getString("ChatLieu"),
                        rs.getString("TrangThai"),
                        rs.getBytes("HinhAnh") // Đọc ảnh
                );
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public List<SanPham> LocSPTheoGia(float giaMin, float giaMax) {
        List<SanPham> list = new ArrayList<>();
        String sql = "SELECT * FROM SanPham WHERE DonGia BETWEEN ? AND ?";
        try {
            Connection con = DBConnect.getConnection();
            PreparedStatement pstm = con.prepareStatement(sql);
            pstm.setFloat(1, giaMin);
            pstm.setFloat(2, giaMax);
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                SanPham sp = new SanPham(
                        rs.getString("MaSP"),
                        rs.getString("TenSP"),
                        rs.getString("LoaiSP"),
                        rs.getFloat("DonGia"),
                        rs.getInt("SoLuong"),
                        rs.getString("MauSac"),
                        rs.getString("KichThuoc"),
                        rs.getString("ChatLieu"),
                        rs.getString("TrangThai")
                );

                // ✅ Đọc ảnh từ DB (cột có kiểu VARBINARY hoặc IMAGE)
                sp.setHinhAnh(rs.getBytes("HinhAnh")); // tên cột ảnh trong DB

                list.add(sp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

// Đây là danh sách mới mỗi lần gọi hàm. Nó chỉ tồn tại tạm thời 
    public List<SanPham> dsSPKhoa(String trangThai) {
        List<SanPham> list = new ArrayList<>();
        String sql = "SELECT * FROM SanPham WHERE TrangThai = ?";
        try (
                Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, trangThai);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                SanPham sp = new SanPham(
                        rs.getString("MaSP"),
                        rs.getString("TenSP"),
                        rs.getString("LoaiSP"),
                        rs.getFloat("DonGia"),
                        rs.getInt("SoLuong"),
                        rs.getString("MauSac"),
                        rs.getString("KichThuoc"),
                        rs.getString("ChatLieu"),
                        rs.getString("TrangThai")
                );

                // ✅ Thêm ảnh từ DB
                sp.setHinhAnh(rs.getBytes("HinhAnh"));

                list.add(sp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<SanPham> locSPTheoLoai(String loai) {
        List<SanPham> list = new ArrayList<>();
        String sql = "SELECT * FROM SanPham WHERE LoaiSP LIKE ? OR TenSP LIKE ? ORDER BY MaSP";

        try (
                Connection con = DBConnect.getConnection(); PreparedStatement pstm = con.prepareStatement(sql)) {

            String loaiLike = "%" + loai.trim() + "%";
            pstm.setString(1, loaiLike);
            pstm.setString(2, loaiLike);

            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                SanPham sp = new SanPham(
                        rs.getString("MaSP"),
                        rs.getString("TenSP"),
                        rs.getString("LoaiSP"),
                        rs.getFloat("DonGia"),
                        rs.getInt("SoLuong"),
                        rs.getString("MauSac"),
                        rs.getString("KichThuoc"),
                        rs.getString("ChatLieu"),
                        rs.getString("TrangThai")
                );

                // ✅ Thêm ảnh từ DB
                sp.setHinhAnh(rs.getBytes("HinhAnh"));

                list.add(sp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public String taoMaSPMoi() {
        String maSP = "SP001"; // Mặc định nếu chưa có SP nào
        String sql = "SELECT MaSP FROM SanPham";

        try (
                Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            int max = 0;
            while (rs.next()) {
                String ma = rs.getString("MaSP");
                if (ma != null && ma.startsWith("SP")) {
                    try {
                        int so = Integer.parseInt(ma.substring(2));
                        if (so > max) {
                            max = so;
                        }
                    } catch (NumberFormatException e) {
                        // Bỏ qua mã không đúng định dạng SPxxx
                    }
                }
            }
            maSP = String.format("SP%04d", max + 1); // Ví dụ: SP001 → SP002 → SP010
        } catch (Exception e) {
            e.printStackTrace();
        }

        return maSP;
    }

}
