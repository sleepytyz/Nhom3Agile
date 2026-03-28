/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Model.HoaDon;
import Model.SanPham;
import Model.TopSanPham;
import Service.DBConnect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author XPS
 */
public class ThongKeDAO {

    public List<SanPham> getSanPhamCanhBaoSoLuong(int nguong) {
    List<SanPham> list = new ArrayList<>();
    String sql = "SELECT * FROM SanPham WHERE SoLuong <= ?";

    try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, nguong);
        ResultSet rs = ps.executeQuery();
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
            sp.setHinhAnh(rs.getBytes("HinhAnh")); // Nếu cần xử lý ảnh sau
            list.add(sp);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return list;
}



    public List<HoaDon> getDanhSachHoaDonByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
    List<HoaDon> list = new ArrayList<>();
    String sql = "SELECT MaHD, MaNV, TenKH, SDT, TrangThai, NgayTao, TongTien, TienTra, TienThua, ThanhToan, GiaoHang, GhiChu "
               + "FROM HoaDon "
               + "WHERE NgayTao BETWEEN ? AND ? "
               + "AND TrangThai IN (N'Đã thanh toán', N'Đã giao hàng')";
    
    try (Connection con = DBConnect.getConnection(); 
         PreparedStatement pst = con.prepareStatement(sql)) {
        
        pst.setObject(1, startDate);
        pst.setObject(2, endDate);
        
        try (ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                HoaDon hd = new HoaDon();
                hd.setMahd(rs.getString("MaHD"));
                hd.setManv(rs.getString("MaNV"));

                String tenKH = rs.getString("TenKH");
                hd.setTenkh(tenKH != null ? tenKH : "Khách vãng lai");

                String sdt = rs.getString("SDT");
                hd.setSdt(sdt != null ? sdt : "");

                hd.setTrangThai(rs.getString("TrangThai"));
                hd.setNgayTao(rs.getDate("NgayTao"));
                hd.setTongTien(rs.getDouble("TongTien"));
                hd.setTienTra(rs.getDouble("TienTra"));
                hd.setTienThua(rs.getDouble("TienThua"));
                hd.setThanhToan(rs.getString("ThanhToan"));
                hd.setGiaoHang(rs.getString("GiaoHang"));
                hd.setGhiChu(rs.getString("GhiChu"));
                
                list.add(hd);
            }
        }
    }
    return list;
}


   public List<TopSanPham> getTop5SanPhamByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
    List<TopSanPham> list = new ArrayList<>();
    LocalDate adjustedEndDate = endDate.plusDays(1); // để bao gồm cả ngày kết thúc

    String sql = "SELECT TOP 5 " +
                 "SP.MaSP, SP.TenSP, SUM(CTHD.SoLuong) AS TongSoLuongBan " +
                 "FROM HoaDonChiTiet CTHD " +
                 "JOIN SanPham SP ON CTHD.MaSP = SP.MaSP " +
                 "JOIN HoaDon HD ON CTHD.MaHD = HD.MaHD " +
                 "WHERE HD.NgayTao >= ? AND HD.NgayTao < ? " +
                 "AND HD.TrangThai IN (N'Đã thanh toán', N'Đã giao hàng') " +
                 "GROUP BY SP.MaSP, SP.TenSP " +
                 "ORDER BY TongSoLuongBan DESC";

    try (Connection con = DBConnect.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
        pst.setObject(1, startDate);
        pst.setObject(2, adjustedEndDate);

        try (ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                TopSanPham sp = new TopSanPham();
                sp.setMaSanPham(rs.getString("MaSP"));
                sp.setTenSanPham(rs.getString("TenSP"));
                sp.setTongSoLuongBan(rs.getInt("TongSoLuongBan"));
                list.add(sp);
            }
        }
    }

    return list;
}


    // Phương thức mới: Lấy tổng doanh thu theo khoảng ngày
    public double getTongDoanhThuByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        double totalRevenue = 0.0;
        String sql = "SELECT SUM(TongTien) AS TotalRevenue FROM HoaDon WHERE NgayTao BETWEEN ? AND ?";
        try (Connection con = DBConnect.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setObject(1, startDate);
            pst.setObject(2, endDate);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    totalRevenue = rs.getDouble("TotalRevenue");
                }
            }
        }
        return totalRevenue;
    }

    // Phương thức mới: Lấy tổng số đơn hàng theo khoảng ngày
    public int getTongDonHangByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        int totalOrders = 0;
        String sql = "SELECT COUNT(MaHD) AS TotalOrders FROM HoaDon WHERE NgayTao BETWEEN ? AND ?";
        try (Connection con = DBConnect.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setObject(1, startDate);
            pst.setObject(2, endDate);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    totalOrders = rs.getInt("TotalOrders");
                }
            }
        }
        return totalOrders;
    }

    public int getSoDonHangTheoThang(int thang, int nam) {
        String sql = "SELECT COUNT(*) FROM HoaDon WHERE MONTH(NgayTao) = ? AND YEAR(NgayTao) = ? AND TrangThai IN (N'Đã thanh toán', N'Đã giao hàng')";
        try (Connection cn = DBConnect.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, thang);
            ps.setInt(2, nam);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return 0;
    }

    public List<HoaDon> getHoaDonTheoThoiGian(String kieuLoc, LocalDate now) throws SQLException {
        List<HoaDon> list = new ArrayList<>();
        String sql = "SELECT * FROM HoaDon WHERE 1=1";

        switch (kieuLoc) {
            case "NGAY":
                sql += " AND CONVERT(date, NgayTao) = ?";
                break;
            case "THANG":
                sql += " AND MONTH(NgayTao) = ? AND YEAR(NgayTao) = ?";
                break;
            case "NAM":
                sql += " AND YEAR(NgayTao) = ?";
                break;
        }

        try (Connection con = DBConnect.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            switch (kieuLoc) {
                case "NGAY":
                    pst.setDate(1, java.sql.Date.valueOf(now));
                    break;
                case "THANG":
                    pst.setInt(1, now.getMonthValue());
                    pst.setInt(2, now.getYear());
                    break;
                case "NAM":
                    pst.setInt(1, now.getYear());
                    break;
            }

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                HoaDon hd = new HoaDon();
                hd.setMahd(rs.getString("MaHD"));
                hd.setNgayTao(rs.getDate("NgayTao"));
                hd.setTongTien(rs.getDouble("TongTien"));
                // ... lấy các trường khác nếu cần
                list.add(hd);
            }
        }
        return list;
    }

    public List<HoaDon> getHoaDonTheoThang(int thang, int nam) {
        List<HoaDon> list = new ArrayList<>();
        String sql = "SELECT MaHD, MaNV, TenKH, SDT, TrangThai, NgayTao, TongTien, TienTra, TienThua, ThanhToan, GiaoHang, GhiChu "
                + "FROM HoaDon "
                + "WHERE MONTH(NgayTao) = ? AND YEAR(NgayTao) = ? "
                + "AND (TrangThai = N'Đã thanh toán' OR TrangThai = N'Đã giao hàng')";
        try (Connection cn = DBConnect.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, thang);
            ps.setInt(2, nam);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                HoaDon hd = new HoaDon();
                hd.setMahd(rs.getString("MaHD"));
                hd.setManv(rs.getString("MaNV"));
                hd.setTenkh(rs.getString("TenKH"));
                hd.setSdt(rs.getString("SDT"));
                hd.setTrangThai(rs.getString("TrangThai"));
                hd.setNgayTao(rs.getDate("NgayTao"));
                hd.setTongTien(rs.getDouble("TongTien"));
                hd.setTienTra(rs.getDouble("TienTra"));
                hd.setTienThua(rs.getDouble("TienThua"));
                hd.setThanhToan(rs.getString("ThanhToan"));
                hd.setGiaoHang(rs.getString("GiaoHang"));
                hd.setGhiChu(rs.getString("GhiChu"));
                list.add(hd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int getTongSoLuongTonKho() {
        int totalStock = 0;
        String sql = "SELECT SUM(SoLuong) AS TongSoLuongTonKho FROM SanPham";
        try (Connection con = DBConnect.getConnection(); PreparedStatement pst = con.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                totalStock = rs.getInt("TongSoLuongTonKho");
                if (rs.wasNull()) {
                    totalStock = 0;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace(System.out);
        }
        return totalStock;
    }

    public double getDoanhThuTheoNgay(Date ngay) {
        String sql = "SELECT SUM(TongTien) FROM HoaDon WHERE CONVERT(DATE, NgayTao) = ? AND TrangThai IN (N'Đã thanh toán', N'Đã giao hàng')";
        try (Connection cn = DBConnect.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(ngay.getTime()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return 0;
    }

    public List<HoaDon> getHoaDonTheoNgay(Date ngay) {
        List<HoaDon> list = new ArrayList<>();
        String sql = "SELECT MaHD, MaNV, TenKH, SDT, TrangThai, NgayTao, TongTien, TienTra, TienThua, ThanhToan, GiaoHang, GhiChu "
                + "FROM HoaDon "
                + "WHERE CONVERT(DATE, NgayTao) = ? "
                + "AND (TrangThai = N'Đã thanh toán' OR TrangThai = N'Đã giao hàng')";
        try (Connection cn = DBConnect.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(ngay.getTime()));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                HoaDon hd = new HoaDon();
                hd.setMahd(rs.getString("MaHD"));
                hd.setManv(rs.getString("MaNV"));
                hd.setTenkh(rs.getString("TenKH"));
                hd.setSdt(rs.getString("SDT"));
                hd.setTrangThai(rs.getString("TrangThai"));
                hd.setNgayTao(rs.getDate("NgayTao"));
                hd.setTongTien(rs.getDouble("TongTien"));
                hd.setTienTra(rs.getDouble("TienTra"));
                hd.setTienThua(rs.getDouble("TienThua"));
                hd.setThanhToan(rs.getString("ThanhToan"));
                hd.setGiaoHang(rs.getString("GiaoHang"));
                hd.setGhiChu(rs.getString("GhiChu"));
                list.add(hd);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return list;
    }

    public int getSoDonHangTheoNgay(Date ngay) {
        String sql = "SELECT COUNT(*) FROM HoaDon WHERE CONVERT(DATE, NgayTao) = ? AND TrangThai IN (N'Đã thanh toán', N'Đã giao hàng')";
        try (Connection cn = DBConnect.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(ngay.getTime()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return 0;
    }

    public double getDoanhThuTheoThang(int thang, int nam) {
        String sql = "SELECT SUM(TongTien) FROM HoaDon WHERE MONTH(NgayTao) = ? AND YEAR(NgayTao) = ? AND TrangThai IN (N'Đã thanh toán', N'Đã giao hàng')";
        try (Connection cn = DBConnect.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, thang);
            ps.setInt(2, nam);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return 0;
    }

    public double getDoanhThuTheoNam(int nam) {
        String sql = "SELECT SUM(TongTien) FROM HoaDon WHERE YEAR(NgayTao) = ? AND TrangThai IN (N'Đã thanh toán', N'Đã giao hàng')";
        try (Connection cn = DBConnect.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, nam);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return 0;
    }

    public int getSoDonHangTheoNam(int nam) {
        String sql = "SELECT COUNT(*) FROM HoaDon WHERE YEAR(NgayTao) = ? AND TrangThai IN (N'Đã thanh toán', N'Đã giao hàng')";
        try (Connection cn = DBConnect.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, nam);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return 0;
    }

    public List<HoaDon> getHoaDonTheoNam(int nam) {
        List<HoaDon> list = new ArrayList<>();
        String sql = "SELECT MaHD, MaNV, TenKH, SDT, TrangThai, NgayTao, TongTien, TienTra, TienThua, ThanhToan, GiaoHang, GhiChu "
                + "FROM HoaDon "
                + "WHERE YEAR(NgayTao) = ? "
                + "AND (TrangThai = N'Đã thanh toán' OR TrangThai = N'Đã giao hàng')";
        try (Connection cn = DBConnect.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, nam);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                HoaDon hd = new HoaDon();
                hd.setMahd(rs.getString("MaHD"));
                hd.setManv(rs.getString("MaNV"));
                hd.setTenkh(rs.getString("TenKH"));
                hd.setSdt(rs.getString("SDT"));
                hd.setTrangThai(rs.getString("TrangThai"));
                hd.setNgayTao(rs.getDate("NgayTao"));
                hd.setTongTien(rs.getDouble("TongTien"));
                hd.setTienTra(rs.getDouble("TienTra"));
                hd.setTienThua(rs.getDouble("TienThua"));
                hd.setThanhToan(rs.getString("ThanhToan"));
                hd.setGiaoHang(rs.getString("GiaoHang"));
                hd.setGhiChu(rs.getString("GhiChu"));
                list.add(hd);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return list;
    }

}
