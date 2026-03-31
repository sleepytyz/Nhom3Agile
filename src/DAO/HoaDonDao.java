/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Model.HoaDon;
import Model.HoaDonChiTiet;
import Service.DBConnect;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;

/**
 *
 * @author XPS
 */
public class HoaDonDao {

    public List<HoaDon> getHoaDonTheoThoiGian(String loai, LocalDate ngay) {
        List<HoaDon> ketQua = new ArrayList<>();
        for (HoaDon hd : getAll()) {
            LocalDate ngayTao = hd.getNgayTao().toLocalDate(); // nếu dùng Timestamp

            switch (loai.toUpperCase()) {
                case "NGAY":
                    if (ngayTao.isEqual(ngay)) {
                        ketQua.add(hd);
                    }
                    break;
                case "THANG":
                    if (ngayTao.getMonthValue() == ngay.getMonthValue()
                            && ngayTao.getYear() == ngay.getYear()) {
                        ketQua.add(hd);
                    }
                    break;
                case "NAM":
                    if (ngayTao.getYear() == ngay.getYear()) {
                        ketQua.add(hd);
                    }
                    break;
            }
        }
        return ketQua;
    }

    public Object[] getRow(HoaDon hd) {
        DecimalFormat df = new DecimalFormat("#,##0.##"); // Định dạng số đẹp

        String mahd = hd.getMahd();
        String manv = hd.getManv();
        String tenkh = hd.getTenkh();
        String sdt = hd.getSdt();
        String trangThai = hd.getTrangThai();
        Date ngayTao = hd.getNgayTao();
        double tongTien = hd.getTongTien();
        double tienTra = hd.getTienTra();
        double tienThua = hd.getTienThua();
        String thanhToan = hd.getThanhToan();
        String giaoHang = hd.getGiaoHang();
        String ghichu = hd.getGhiChu();

        Object[] row = new Object[]{
            mahd,
            manv,
            tenkh,
            sdt,
            trangThai,
            ngayTao,
            df.format(tongTien), // ✅ Định dạng để không bị 1E4
            df.format(tienTra),
            df.format(tienThua),
            thanhToan,
            giaoHang,
            ghichu
        };

        return row;
    }

    public List<HoaDon> getAll() {
        List<HoaDon> listHD = new ArrayList<>();
        String sql = "SELECT * FROM HoaDon \n"
                + "WHERE TrangThai LIKE '%thanh toán%' \n"
                + "   OR TrangThai LIKE '%giao hàng%' ";
        try {
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            while (rs.next()) {
                String mahd = rs.getString(1);
                String manv = rs.getString(2);
                String tenkh = rs.getString(3);
                String sdt = rs.getString(4);
                String trangThai = rs.getString(5);
                Date ngayTao = rs.getDate(6);
                double tongTien = rs.getDouble(7);
                double tienTra = rs.getDouble(8);
                double tienThua = rs.getDouble(9);
                String thanhToan = rs.getString(10);
                String giaoHang = rs.getString(11);
                String ghichu = rs.getString(12);
                HoaDon hd = new HoaDon(mahd, manv, tenkh, sdt, trangThai, ngayTao, tongTien, tienTra, tienThua, thanhToan, giaoHang, ghichu);
                listHD.add(hd);
            }
        } catch (Exception ex) {
        }

        return listHD;
    }

    public List<HoaDon> getAll123() {
        List<HoaDon> listHD = new ArrayList<>();
        String sql = "SELECT * FROM HoaDon";
        try {
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            while (rs.next()) {
                String mahd = rs.getString(1);
                String manv = rs.getString(2);
                String tenkh = rs.getString(3);
                String sdt = rs.getString(4);
                String trangThai = rs.getString(5);
                Date ngayTao = rs.getDate(6);
                double tongTien = rs.getDouble(7);
                double tienTra = rs.getDouble(8);
                double tienThua = rs.getDouble(9);
                String thanhToan = rs.getString(10);
                String giaoHang = rs.getString(11);
                String ghichu = rs.getString(12);
                HoaDon hd = new HoaDon(mahd, manv, tenkh, sdt, trangThai, ngayTao, tongTien, tienTra, tienThua, thanhToan, giaoHang, ghichu);
                listHD.add(hd);
            }
        } catch (Exception ex) {
        }

        return listHD;
    }

    public HoaDon timHDTheoma1(String mahd) {
        String sql = "SELECT * FROM HoaDon WHERE MaHD = ?";
        try {
            Connection con = DBConnect.getConnection();
            PreparedStatement pstm = con.prepareStatement(sql);
            pstm.setString(1, mahd);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                return new HoaDon(
                        rs.getString("MaHD"),
                        rs.getString("MaNV"),
                        rs.getString("TenKH"),
                        rs.getString("SDT"),
                        rs.getString("TrangThai"),
                        rs.getDate("NgayTao"),
                        rs.getInt("TongTien"),
                        rs.getInt("TienTra"),
                        rs.getInt("TienThua"),
                        rs.getString("ThanhToan"),
                        rs.getString("GiaoHang"),
                        rs.getString("GhiChu")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Không tìm thấy
    }

    public List<HoaDon> getAll12() {
        List<HoaDon> listHD = new ArrayList<>();
        String sql = "SELECT MaHD, MaNV, TrangThai, NgayTao, GhiChu "
                + "FROM HoaDon "
                + "WHERE TrangThai = N'Đã giao hàng'";

        try {
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            while (rs.next()) {
                String mahd = rs.getString(1);
                String manv = rs.getString(2);
                String trangThai = rs.getString(3);
                Date ngayTao = rs.getDate(4);
                String ghichu = rs.getString(5);
                HoaDon hd = new HoaDon(mahd, manv, trangThai, ngayTao, ghichu);
                listHD.add(hd);
            }
        } catch (Exception ex) {
        }
        return listHD;
    }

    public List<HoaDon> getAll1() {
        List<HoaDon> listHD = new ArrayList<>();
        String sql = "SELECT MaHD, MaNV, TrangThai, NgayTao, GhiChu "
                + "FROM HoaDon "
                + "WHERE TrangThai IN (N'Đang xử lý', N'Đang giao')";
        try {
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            while (rs.next()) {
                String mahd = rs.getString(1);
                String manv = rs.getString(2);
                String trangThai = rs.getString(3);
                Date ngayTao = rs.getDate(4);
                String ghichu = rs.getString(5);
                HoaDon hd = new HoaDon(mahd, manv, trangThai, ngayTao, ghichu);
                listHD.add(hd);
            }
        } catch (Exception ex) {
        }
        return listHD;
    }

    public Object[] getRow1(HoaDon hd) {
        String mahd = hd.getMahd();
        String manv = hd.getManv();
        String trangThai = hd.getTrangThai();
        Date ngayTao = hd.getNgayTao();
        String ghichu = hd.getGhiChu();

        Object[] row = new Object[]{mahd, manv, trangThai, ngayTao, ghichu};
        return row;
    }

    public boolean taoHoaDonBanDau(String maHD, String maNV, String tenKH,
            String sdtKH, String trangThai) {
        String sql = "INSERT INTO HoaDon(MaHD, MaNV, TenKH, SDT, TrangThai, NgayTao) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maHD);
            ps.setString(2, maNV);
            ps.setString(3, tenKH);
            ps.setString(4, sdtKH);
            ps.setString(5, trangThai);

            // Sử dụng ngày hiện tại
            ps.setDate(6, new java.sql.Date(System.currentTimeMillis()));

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean capNhatTrangThaiVaNhanVien(String maHD, String trangThai, String maNV) {
    String sql = "UPDATE HoaDon SET TrangThai = ?, MaNV = ? WHERE MaHD = ?";
    try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, trangThai);
        ps.setString(2, maNV);
        ps.setString(3, maHD);
        return ps.executeUpdate() > 0;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}


    public boolean capNhatThongTinGiaoHang(
            String maHD,
            float tongTien,
            float khachTra,
            float tienThua,
            String phuongThucGiaoHang,
            String phuongThucThanhToan,
            String trangThai,
            String maNV) {  // thêm tham số mã nhân viên

        String sql = "UPDATE HoaDon SET TongTien = ?, TienTra = ?, TienThua = ?, "
                + "ThanhToan = ?, GiaoHang = ?, TrangThai = ?, MaNV = ? WHERE MaHD = ?";

        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setFloat(1, tongTien);
            ps.setFloat(2, khachTra);
            ps.setFloat(3, tienThua);
            ps.setString(4, phuongThucThanhToan);
            ps.setString(5, phuongThucGiaoHang);
            ps.setString(6, trangThai);
            ps.setString(7, maNV);  // đặt mã nhân viên vào đây
            ps.setString(8, maHD);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean thanhToanHoaDon(
            String maHD,
            double tongTien,
            double tienKhachDua,
            double tienThua,
            String phuongThucTT,
            String phuongThucGH,
            String trangThaiMoi,
            String ghiChu,
            String maNV // thêm tham số mã nhân viên
    ) {
        String sql = "UPDATE HoaDon SET "
                + "TongTien = ?, "
                + "TienTra = ?, "
                + "TienThua = ?, "
                + "ThanhToan = ?, "
                + "GiaoHang = ?, "
                + "TrangThai = ?, "
                + "GhiChu = ?, "
                + "MaNV = ? " // cập nhật mã nhân viên
                + "WHERE MaHD = ?";

        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, tongTien);
            ps.setDouble(2, tienKhachDua);
            ps.setDouble(3, tienThua);
            ps.setString(4, phuongThucTT);
            ps.setString(5, phuongThucGH);
            ps.setString(6, trangThaiMoi);
            ps.setString(7, ghiChu);
            ps.setString(8, maNV);     // gán mã nhân viên
            ps.setString(9, maHD);

            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public String taoMaHoaDonMoi() {
        String maHD = "HD001"; // Mã mặc định nếu chưa có hóa đơn nào
        String sql = "SELECT TOP 1 MaHD FROM HoaDon ORDER BY MaHD DESC";

        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                String lastMaHD = rs.getString("MaHD");
                int number = Integer.parseInt(lastMaHD.substring(2)) + 1;
                maHD = String.format("HD%02d", number); // Định dạng HD001, HD002,...
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return maHD;
    }

    public int huyHoaDon(String maHoaDon, String ghiChu) {
        String sql = "UPDATE HoaDon SET TrangThai = ?, GhiChu = ? WHERE MaHD = ?";
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "Đã huỷ");
            ps.setString(2, ghiChu);
            ps.setString(3, maHoaDon);

            return ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

// Phương thức thêm hóa đơn mới (tạo cả hóa đơn và chi tiết hóa đơn)
    public boolean addHoaDon(HoaDon hoaDon, List<HoaDonChiTiet> danhSachChiTiet) throws SQLException {
        Connection con = null;
        PreparedStatement psHoaDon = null;
        PreparedStatement psChiTiet = null;
        boolean success = false;

        // SQL cho việc thêm HoaDon
        // Nếu bạn đã xóa TenKH khỏi DB, câu SQL này sẽ hoạt động.
        // Cần đảm bảo rằng các cột trong SQL khớp với các cột có trong bảng HoaDon
        String sqlHoaDon = "INSERT INTO HoaDon (MaHD, MaNV, SDT, TrangThai, NgayTao, TongTien, TienTra, TienThua, ThanhToan, GiaoHang, GhiChu) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlChiTiet = "INSERT INTO HoaDonChiTiet (MaHD, MaSP, TenSP, SoLuong, DonGia, GiamGia, ThanhTien) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            con = DBConnect.getConnection();
            con.setAutoCommit(false); // Bắt đầu transaction

            // 1. Thêm hóa đơn chính
            psHoaDon = con.prepareStatement(sqlHoaDon);
            psHoaDon.setString(1, hoaDon.getMahd());
            psHoaDon.setString(2, hoaDon.getManv());
            psHoaDon.setString(3, hoaDon.getSdt()); // LƯU SDT VÀO BẢNG HOADON (khóa ngoại)
            psHoaDon.setString(4, hoaDon.getTrangThai());

            // Chuyển đổi LocalDate sang java.sql.Date để lưu vào cột DATE
            psHoaDon.setDate(5, new java.sql.Date(hoaDon.getNgayTao().getTime()));

            psHoaDon.setDouble(6, hoaDon.getTongTien());
            psHoaDon.setDouble(7, hoaDon.getTienTra());
            psHoaDon.setDouble(8, hoaDon.getTienThua());
            psHoaDon.setString(9, hoaDon.getThanhToan());
            psHoaDon.setString(10, hoaDon.getGiaoHang());
            psHoaDon.setString(11, hoaDon.getGhiChu());
            psHoaDon.executeUpdate();

            // 2. Thêm các chi tiết hóa đơn
            psChiTiet = con.prepareStatement(sqlChiTiet);
            for (HoaDonChiTiet chiTiet : danhSachChiTiet) {
                psChiTiet.setString(1, chiTiet.getMahd());
                psChiTiet.setString(2, chiTiet.getMasp());
                psChiTiet.setString(3, chiTiet.getTensp());
                psChiTiet.setInt(4, chiTiet.getSluong());
                psChiTiet.setFloat(5, chiTiet.getDongia());
                psChiTiet.setFloat(6, chiTiet.getGiamGia());
                psChiTiet.setFloat(7, chiTiet.getThanhTien());
                psChiTiet.addBatch();
            }
            psChiTiet.executeBatch(); // Thực thi tất cả các lệnh thêm chi tiết

            con.commit(); // Hoàn thành transaction
            success = true;

        } catch (SQLException e) {
            if (con != null) {
                con.rollback(); // Rollback nếu có lỗi
            }
            e.printStackTrace();
            throw e; // Ném lỗi để xử lý ở tầng trên
        } finally {
            try {
                if (psHoaDon != null) {
                    psHoaDon.close();
                }
                if (psChiTiet != null) {
                    psChiTiet.close();
                }
                if (con != null) {
                    con.setAutoCommit(true); // Đặt lại auto-commit
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    // Phương thức lấy thông tin hóa đơn theo MaHD, kèm theo thông tin khách hàng
    public HoaDon getHoaDonById(String maHD) throws SQLException {
        HoaDon hd = null;
        // JOIN với KhachHang để lấy TenKH và Sdt cho model HoaDon
        String sql = "SELECT hd.MaHD, hd.MaNV, hd.SDT, kh.TenKH, hd.TrangThai, hd.NgayTao, hd.TongTien, hd.TienTra, hd.TienThua, hd.ThanhToan, hd.GiaoHang, hd.GhiChu "
                + "FROM HoaDon hd "
                + "JOIN KhachHang kh ON hd.SDT = kh.SDT "
                + // THAY ĐỔI JOIN TỪ MaKH SANG SDT
                "WHERE hd.MaHD = ?";

        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    hd = new HoaDon();
                    hd.setMahd(rs.getString("MaHD"));
                    hd.setManv(rs.getString("MaNV"));
                    hd.setSdt(rs.getString("SDT"));     // Set SDT
                    hd.setTenkh(rs.getString("TenKH")); // Set TenKH từ JOIN
                    hd.setTrangThai(rs.getString("TrangThai"));

                    // Đọc NgayTao từ DB (kiểu DATE) và chuyển đổi sang LocalDate
                    java.sql.Date sqlDate = rs.getDate("NgayTao");
                    hd.setNgayTao(sqlDate); // Không cần xử lý gì thêm

                    hd.setTongTien(rs.getDouble("TongTien"));
                    hd.setTienTra(rs.getDouble("TienTra"));
                    hd.setTienThua(rs.getDouble("TienThua"));
                    hd.setThanhToan(rs.getString("ThanhToan"));
                    hd.setGiaoHang(rs.getString("GiaoHang"));
                    hd.setGhiChu(rs.getString("GhiChu"));
                }
            }
        }
        return hd;
    }

    // Phương thức lấy chi tiết hóa đơn theo MaHD (giữ nguyên)
    public List<HoaDonChiTiet> getChiTietHoaDonByMaHD(String maHD) throws SQLException {
        List<HoaDonChiTiet> danhSachChiTiet = new ArrayList<>();
        String sql = "SELECT MaHD, MaSP, TenSP, SoLuong, DonGia, GiamGia, ThanhTien FROM HoaDonChiTiet WHERE MaHD = ?";
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HoaDonChiTiet hdct = new HoaDonChiTiet();
                    hdct.setMahd(rs.getString("MaHD"));
                    hdct.setMasp(rs.getString("MaSP"));
                    hdct.setTensp(rs.getString("TenSP"));
                    hdct.setSluong(rs.getInt("SoLuong"));
                    hdct.setDongia(rs.getFloat("DonGia"));
                    hdct.setGiamGia(rs.getFloat("GiamGia"));
                    hdct.setThanhTien(rs.getFloat("ThanhTien"));
                    danhSachChiTiet.add(hdct);
                }
            }
        }
        return danhSachChiTiet;
    }

    // Phương thức lấy hóa đơn theo SDT khách hàng (dùng cho "Lịch sử giao dịch" của QLKH)
    public List<HoaDon> getHoaDonsBySdt(String sdt) throws SQLException {
        List<HoaDon> danhSachHoaDon = new ArrayList<>();
        String sql = "SELECT hd.MaHD, hd.MaNV, hd.SDT, kh.TenKH, hd.TrangThai, hd.NgayTao, hd.TongTien, hd.TienTra, hd.TienThua, hd.ThanhToan, hd.GiaoHang, hd.GhiChu "
                + "FROM HoaDon hd "
                + "JOIN KhachHang kh ON hd.SDT = kh.SDT "
                + "WHERE hd.SDT = ? ORDER BY hd.NgayTao DESC";

        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sdt);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HoaDon hd = new HoaDon();
                    hd.setMahd(rs.getString("MaHD"));
                    hd.setManv(rs.getString("MaNV"));
                    hd.setSdt(rs.getString("SDT"));
                    hd.setTenkh(rs.getString("TenKH"));
                    hd.setTrangThai(rs.getString("TrangThai"));

                    java.sql.Date sqlDate = rs.getDate("NgayTao");
                    hd.setNgayTao(sqlDate); // Không cần xử lý gì thêm

                    hd.setTongTien(rs.getDouble("TongTien"));
                    hd.setTienTra(rs.getDouble("TienTra"));
                    hd.setTienThua(rs.getDouble("TienThua"));
                    hd.setThanhToan(rs.getString("ThanhToan"));
                    hd.setGiaoHang(rs.getString("GiaoHang"));
                    hd.setGhiChu(rs.getString("GhiChu"));
                    danhSachHoaDon.add(hd);
                }
            }
        }
        return danhSachHoaDon;
    }

    public HoaDon getHoaDonByMa(String maHD) {
        String sql = "SELECT * FROM HoaDon WHERE MaHD = ?";
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
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
                hd.setGhiChu(rs.getString("GhiChu"));

                // Thêm 2 trường mới
                hd.setThanhToan(rs.getString("ThanhToan"));
                hd.setGiaoHang(rs.getString("GiaoHang"));

                return hd;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean capNhatTongTien(String maHD, double tongTien) {
        String sql = "UPDATE HoaDon SET TongTien = ? WHERE MaHD = ?";
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, tongTien);
            ps.setString(2, maHD);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<HoaDon> getHoaDonDangGiao() {
        List<HoaDon> listHD = new ArrayList<>();
        String sql = "SELECT MaHD, MaNV, TrangThai, NgayTao, GhiChu "
                + "FROM HoaDon WHERE TrangThai = N'Đang giao'";
        try {
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            while (rs.next()) {
                String mahd = rs.getString(1);
                String manv = rs.getString(2);
                String trangThai = rs.getString(3);
                Date ngayTao = rs.getDate(4);
                String ghichu = rs.getString(5);
                HoaDon hd = new HoaDon(mahd, manv, trangThai, ngayTao, ghichu);
                listHD.add(hd);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return listHD;
    }

    public String getTrangThaiByMaHD(String maHD) {
        String sql = "SELECT TrangThai FROM HoaDon WHERE MaHD = ?";
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("TrangThai");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
