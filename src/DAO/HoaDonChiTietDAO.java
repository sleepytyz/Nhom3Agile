/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Model.HoaDonChiTiet;
import Service.DBConnect;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author XPS
 */
public class HoaDonChiTietDAO {

    public Object[] getRow1(HoaDonChiTiet hdct) {
    // Formatter: tiền và phần trăm riêng để hiển thị rõ ràng
    DecimalFormat dfMoney = new DecimalFormat("#,##0");   // ví dụ: 4,290,000
    DecimalFormat dfPercent = new DecimalFormat("#.##");  // ví dụ: 10 hoặc 15.5

    // Lấy dữ liệu
    String macthd = hdct.getMacthd();
    String mahd = hdct.getMahd();
    String masp = hdct.getMasp();
    String tensp = hdct.getTensp();
    int sluong = hdct.getSluong();
    float dongia = hdct.getDongia();
    float giamHeSo = hdct.getGiamGia(); // giả sử đây là hệ số: 0.1 = 10%
    float thanhTien = hdct.getThanhTien();
    String trangThaiTraHang = hdct.getTrangthai();
    String lyDo = hdct.getGchu();

    // Bảo vệ giá trị và tính phần trăm hiển thị
    if (Float.isNaN(giamHeSo) || giamHeSo < 0f) giamHeSo = 0f;
    if (giamHeSo > 1f) giamHeSo = 1f; // optional: giới hạn tối đa 100%

    float giamPT = giamHeSo * 100f; // 0.1 -> 10.0
    String giamHienThi = dfPercent.format(giamPT) + "%";

    return new Object[]{
        macthd,
        mahd,
        masp,
        tensp,
        sluong,
        dfMoney.format(dongia),
        giamHienThi,
        dfMoney.format(thanhTien),
        trangThaiTraHang,
        lyDo
    };
}


    public Object[] getRow12(HoaDonChiTiet hdct) {
        String macthd = hdct.getMacthd();
        String mahd = hdct.getMahd();
        String masp = hdct.getMasp();
        String tensp = hdct.getTensp();
        int sluong = hdct.getSluong();
        float dongia = hdct.getDongia();
        float giamGia = hdct.getGiamGia();
        float thanhTien = hdct.getThanhTien();
        String trangThaiTraHang = hdct.getTrangthai();
        String lydo = hdct.getGchu();

        return new Object[]{macthd, mahd, masp, tensp, sluong, dongia, giamGia, thanhTien, trangThaiTraHang, lydo};
    }

   public List<HoaDonChiTiet> getAll(String maHD) {
    List<HoaDonChiTiet> listHD = new ArrayList<>();

    String sql = new StringBuilder()
        .append("SELECT ")
        .append(" hdct.MaCTSP, hdct.MaHD, hdct.MaSP, hdct.TenSP, ")
        .append(" hdct.SoLuong, hdct.DonGia, ")
        .append(" ISNULL(km.TongGiamGia, 0) AS GiamGia, ")
        .append(" hdct.SoLuong * hdct.DonGia * (1 - ISNULL(km.TongGiamGia, 0)) AS ThanhTien ")
        .append("FROM HoaDonChiTiet hdct ")
        .append("LEFT JOIN ( ")
        .append("    SELECT ct.MaSP, MAX(k.GiamGia) AS TongGiamGia ")
        .append("    FROM KhuyenMaiChiTiet ct ")
        .append("    JOIN KhuyenMai k ON ct.MaKM = k.MaKM ")
        .append("    WHERE CAST(GETDATE() AS date) BETWEEN CAST(k.NgayBdau AS date) AND CAST(k.NgayKthuc AS date) ")
        .append("      AND k.TrangThai = N'Đang hoạt động' ")
        .append("    GROUP BY ct.MaSP ")
        .append(") km ON hdct.MaSP = km.MaSP ")
        .append("WHERE hdct.MaHD = ? ")
        .append("ORDER BY hdct.MaCTSP ASC")
        .toString();

    try (Connection con = DBConnect.getConnection();
         PreparedStatement stm = con.prepareStatement(sql)) {

        if (maHD == null || maHD.trim().isEmpty()) {
            return listHD;
        }
        stm.setString(1, maHD.trim());

        try (ResultSet rs = stm.executeQuery()) {
            while (rs.next()) {
                HoaDonChiTiet hd = new HoaDonChiTiet(
                    rs.getString("MaCTSP"),
                    rs.getString("MaHD"),
                    rs.getString("MaSP"),
                    rs.getString("TenSP"),
                    rs.getInt("SoLuong"),
                    rs.getFloat("DonGia"),
                    rs.getFloat("GiamGia"),
                    rs.getFloat("ThanhTien")
                    // Bỏ 2 trường TrangThaiTraHang và LyDoTraHang khỏi constructor hoặc thay đổi constructor phù hợp
                );
                listHD.add(hd);
            }
        }
    } catch (Exception ex) {
        System.err.println("Lỗi khi truy vấn hóa đơn chi tiết: " + ex.getMessage());
        throw new RuntimeException("Lỗi truy vấn dữ liệu", ex);
    }
    return listHD;
}


   public List<Object[]> getAllWithMaKM(String maHD) {
    List<Object[]> list = new ArrayList<>();

    String sql = "SELECT "
               + "hdct.MaCTSP, hdct.MaHD, hdct.MaSP, hdct.TenSP, "
               + "hdct.SoLuong, hdct.DonGia, "
               + "ISNULL(km.TongGiamGia, 0) AS GiamGia, "
               + "ISNULL(km.MaKM, '') AS MaKM, "
               + "hdct.SoLuong * hdct.DonGia * (1 - ISNULL(km.TongGiamGia, 0)) AS ThanhTien "
               + "FROM HoaDonChiTiet hdct "
               + "LEFT JOIN ( "
               + "    SELECT ct.MaSP, MAX(k.GiamGia) AS TongGiamGia, MAX(k.MaKM) AS MaKM "
               + "    FROM KhuyenMaiChiTiet ct "
               + "    JOIN KhuyenMai k ON ct.MaKM = k.MaKM "
               + "    WHERE CAST(GETDATE() AS date) BETWEEN CAST(k.NgayBdau AS date) AND CAST(k.NgayKthuc AS date) "
               + "      AND k.TrangThai = N'Đang hoạt động' "
               + "    GROUP BY ct.MaSP "
               + ") km ON hdct.MaSP = km.MaSP "
               + "WHERE hdct.MaHD = ? "
               + "ORDER BY hdct.MaCTSP ASC";

    try (Connection con = DBConnect.getConnection();
         PreparedStatement stm = con.prepareStatement(sql)) {

        if (maHD == null || maHD.trim().isEmpty()) {
            return list;
        }
        stm.setString(1, maHD.trim());

        try (ResultSet rs = stm.executeQuery()) {
            while (rs.next()) {
                Object[] row = new Object[]{
                    rs.getString("MaCTSP"),
                    rs.getString("MaHD"),
                    rs.getString("MaSP"),
                    rs.getString("TenSP"),
                    rs.getInt("SoLuong"),
                    rs.getFloat("DonGia"),
                    rs.getFloat("GiamGia") * 100 + "%",  // Hiển thị phần trăm
                    rs.getString("MaKM"),  // Mã khuyến mãi thêm mới
                    rs.getFloat("ThanhTien"),
                };
                list.add(row);
            }
        }
    } catch (Exception ex) {
        ex.printStackTrace();
    }
    return list;
}



    public List<HoaDonChiTiet> timHDTheoma2(String mahd) {
        List<HoaDonChiTiet> ds = new ArrayList<>();
        String sql = "SELECT * FROM HoaDonChiTiet WHERE MaHD = ?";

        try {
            Connection con = DBConnect.getConnection();
            PreparedStatement pstm = con.prepareStatement(sql);
            pstm.setString(1, mahd);
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                HoaDonChiTiet hdct = new HoaDonChiTiet(
                        rs.getString("MaCTSP"),
                        rs.getString("MaHD"),
                        rs.getString("MaSP"),
                        rs.getString("TenSP"),
                        rs.getInt("SoLuong"),
                        rs.getFloat("DonGia"),
                        rs.getFloat("GiamGia"),
                        rs.getFloat("ThanhTien")
                );
                ds.add(hdct);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ds;
    }

   public boolean themChiTiet(String maHD, String maSP, int soLuong) {
    String sqlCheck = "SELECT SoLuong, DonGia, GiamGia FROM HoaDonChiTiet WHERE MaHD = ? AND MaSP = ?";
    String sqlUpdateCT = "UPDATE HoaDonChiTiet "
            + "SET SoLuong = SoLuong + ?, ThanhTien = (SoLuong + ?) * ?, GiamGia = ? "
            + "WHERE MaHD = ? AND MaSP = ?";
    String sqlInsert = "INSERT INTO HoaDonChiTiet (MaHD, MaSP, TenSP, SoLuong, DonGia, GiamGia, ThanhTien) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
    String sqlSP = "SELECT TenSP, DonGia, SoLuong FROM SanPham WHERE MaSP = ?";
    // sửa: lấy từ bảng chi tiết nối KhuyenMai
    String sqlKM = "SELECT ISNULL(SUM(k.GiamGia), 0) AS TongGiamGia "
            + "FROM KhuyenMaiChiTiet ct "
            + "JOIN KhuyenMai k ON ct.MaKM = k.MaKM "
            + "WHERE ct.MaSP = ? "
            + "  AND CAST(GETDATE() AS date) BETWEEN CAST(k.NgayBdau AS date) AND CAST(k.NgayKthuc AS date) "
            + "  AND k.TrangThai = N'Đang hoạt động'";

    String sqlUpdateSL = "UPDATE SanPham SET SoLuong = SoLuong - ? WHERE MaSP = ?";

    try (Connection con = DBConnect.getConnection();
         PreparedStatement psSP = con.prepareStatement(sqlSP);
         PreparedStatement psKM = con.prepareStatement(sqlKM);
         PreparedStatement psCheck = con.prepareStatement(sqlCheck)) {

        // 1) Lấy thông tin sản phẩm
        psSP.setString(1, maSP);
        try (ResultSet rsSP = psSP.executeQuery()) {
            if (!rsSP.next()) {
                JOptionPane.showMessageDialog(null, "Không tìm thấy sản phẩm!");
                return false;
            }
            String tenSP = rsSP.getString("TenSP");
            double donGia = rsSP.getDouble("DonGia");
            int tonKho = rsSP.getInt("SoLuong");

            if (soLuong > tonKho) {
                JOptionPane.showMessageDialog(null, "Số lượng tồn kho không đủ!");
                return false;
            }

            // 2) Lấy tổng hệ số giảm giá (hệ số như 0.1)
            psKM.setString(1, maSP);
            double giamHeSo = 0.0;
            try (ResultSet rsKM = psKM.executeQuery()) {
                if (rsKM.next()) {
                    giamHeSo = rsKM.getDouble("TongGiamGia");
                }
            }
            if (giamHeSo < 0) giamHeSo = 0;
            if (giamHeSo > 1) giamHeSo = 1; // caps at 100%

            // Đơn giá sau giảm (theo hệ số)
            double donGiaSauGiam = donGia * (1.0 - giamHeSo);

            // 3) Kiểm tra tồn tại item trong hóa đơn theo MaHD + MaSP
            psCheck.setString(1, maHD);
            psCheck.setString(2, maSP);
            try (ResultSet rsCheck = psCheck.executeQuery()) {
                if (rsCheck.next()) {
                    // Cập nhật: tăng SoLuong và tính lại ThanhTien theo đơn giá sau giảm hiện tại
                    try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdateCT)) {
                        psUpdate.setInt(1, soLuong); // tăng số lượng
                        psUpdate.setInt(2, soLuong); // dùng lại trong phép tính
                        psUpdate.setDouble(3, donGiaSauGiam); // đơn giá sau giảm
                        psUpdate.setDouble(4, giamHeSo); // cập nhật cột GiamGia (hệ số)
                        psUpdate.setString(5, maHD);
                        psUpdate.setString(6, maSP);
                        psUpdate.executeUpdate();
                    }
                } else {
                    // Insert mới: lưu GiamGia là hệ số, ThanhTien = soLuong * donGiaSauGiam
                    double thanhTien = soLuong * donGiaSauGiam;
                    try (PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {
                        psInsert.setString(1, maHD);
                        psInsert.setString(2, maSP);
                        psInsert.setString(3, tenSP);
                        psInsert.setInt(4, soLuong);
                        psInsert.setDouble(5, donGia);
                        psInsert.setDouble(6, giamHeSo);
                        psInsert.setDouble(7, thanhTien);
                        psInsert.executeUpdate();
                    }
                }
            }

            // 4) Trừ tồn kho sản phẩm
            try (PreparedStatement psSL = con.prepareStatement(sqlUpdateSL)) {
                psSL.setInt(1, soLuong);
                psSL.setString(2, maSP);
                psSL.executeUpdate();
            }

            return true;
        }

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Lỗi thêm sản phẩm: " + e.getMessage());
        return false;
    }
}


    public void xoaMotSanPham(String maHD, String maSP) {
        String sql = "DELETE FROM HoaDonChiTiet WHERE MaHD = ? AND MaSP = ?";
        try {
            Connection conn = DBConnect.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, maHD);
            ps.setString(2, maSP);
            int kq = ps.executeUpdate();
            if (kq > 0) {
                System.out.println("Đã xoá sản phẩm " + maSP + " khỏi hoá đơn " + maHD);
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void xoaToanBoSanPham(String maHD) {
        String sql = "DELETE FROM HoaDonChiTiet WHERE MaHD = ?";
        try {
            Connection conn = DBConnect.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, maHD);
            int kq = ps.executeUpdate();
            if (kq > 0) {
                System.out.println("Đã xoá toàn bộ sản phẩm khỏi hoá đơn " + maHD);
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
