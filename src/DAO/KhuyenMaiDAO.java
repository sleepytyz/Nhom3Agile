/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Model.KhuyenMai;
import Model.SanPham;
import Service.DBConnect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author XPS
 */
public class KhuyenMaiDAO {

    public boolean capNhatKhuyenMai(KhuyenMai km) {
        String sql = "UPDATE KhuyenMai SET TenKM = ?, NgayBdau = ?, NgayKthuc = ?, LoaiSP = ?, GiamGia = ?, TrangThai = ? WHERE MaKM = ?";
        try (Connection con = DBConnect.getConnection(); PreparedStatement p = con.prepareStatement(sql)) {
            p.setString(1, km.getTenKm());
            p.setDate(2, new java.sql.Date(km.getNgayBdau().getTime()));
            p.setDate(3, new java.sql.Date(km.getNgayKthuc().getTime()));
            p.setString(4, km.getLoaisp());
            p.setFloat(5, km.getGiamgia());
            p.setString(6, km.getTrangThai());
            p.setString(7, km.getMaKM());
            return p.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean luuKhuyenMai(KhuyenMai km, List<String> dsMaSP) {
        String sqlKM = "INSERT INTO KhuyenMai (MaKM, TenKM, NgayBdau, NgayKthuc, LoaiSP, GiamGia, TrangThai) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlCT = "INSERT INTO KhuyenMaiChiTiet (MaKM, MaSP) VALUES (?, ?)";
        try (Connection con = DBConnect.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement pkm = con.prepareStatement(sqlKM)) {
                pkm.setString(1, km.getMaKM());
                pkm.setString(2, km.getTenKm());
                pkm.setDate(3, new java.sql.Date(km.getNgayBdau().getTime()));
                pkm.setDate(4, new java.sql.Date(km.getNgayKthuc().getTime()));
                pkm.setString(5, km.getLoaisp());
                pkm.setFloat(6, km.getGiamgia());
                pkm.setString(7, km.getTrangThai());
                pkm.executeUpdate();
            }

            if (dsMaSP != null && !dsMaSP.isEmpty()) {
                try (PreparedStatement pct = con.prepareStatement(sqlCT)) {
                    for (String maSP : dsMaSP) {
                        pct.setString(1, km.getMaKM());
                        pct.setString(2, maSP);
                        pct.addBatch();
                    }
                    pct.executeBatch();
                }
            }
            con.commit();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public Set<String> getExistingSPsForMaKM(String maKM) {
        Set<String> set = new HashSet<>();
        String sql = "SELECT MaSP FROM KhuyenMaiChiTiet WHERE MaKM = ?";
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKM);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    set.add(rs.getString("MaSP"));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return set;
    }

    public boolean appendChiTietForMaKM(String maKM, List<String> dsMaSP) {
        if (dsMaSP == null || dsMaSP.isEmpty()) {
            return true;
        }
        String sql = "INSERT INTO KhuyenMaiChiTiet (MaKM, MaSP) VALUES (?, ?)";
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            con.setAutoCommit(false);
            for (String maSP : dsMaSP) {
                ps.setString(1, maKM);
                ps.setString(2, maSP);
                ps.addBatch();
            }
            ps.executeBatch();
            con.commit();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean isMaKMExist(String maKM) {
        String sql = "SELECT COUNT(*) FROM KhuyenMai WHERE MaKM = ?";
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKM);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean ngungHoatDongKhuyenMai(String maKM) {
    String sqlUpdateKM = "UPDATE KhuyenMai SET TrangThai = N'Ngừng hoạt động' WHERE MaKM = ?";

    try (Connection con = DBConnect.getConnection();
         PreparedStatement ps = con.prepareStatement(sqlUpdateKM)) {
        
        ps.setString(1, maKM);
        int rows = ps.executeUpdate();
        return rows > 0; // thành công nếu có ít nhất 1 dòng được cập nhật
    } catch (Exception ex) {
        ex.printStackTrace();
        return false;
    }
}


    // Lấy tất cả khuyến mãi + update trạng thái theo ngày
   public List<KhuyenMai> getAllKM() {
    List<KhuyenMai> list = new ArrayList<>();

    // 1. Hết hạn thì ngừng hoạt động
    String updateStop = 
        "UPDATE KhuyenMai " +
        "SET TrangThai = N'Ngừng hoạt động' " +
        "WHERE CAST(GETDATE() AS date) > CAST(NgayKthuc AS date)";

    // 2. Trong thời gian áp dụng thì đang hoạt động (trừ khi đã tắt thủ công)
    String updateActive = 
        "UPDATE KhuyenMai " +
        "SET TrangThai = N'Đang hoạt động' " +
        "WHERE CAST(GETDATE() AS date) BETWEEN CAST(NgayBdau AS date) AND CAST(NgayKthuc AS date) " +
        "AND TrangThai NOT IN (N'Ngừng hoạt động')";

    // 3. Lấy toàn bộ danh sách khuyến mãi
    String select = "SELECT * FROM KhuyenMai ORDER BY MaKM";

    try (Connection con = DBConnect.getConnection(); 
         Statement st = con.createStatement()) {

        // Cập nhật trạng thái
        st.executeUpdate(updateStop);
        st.executeUpdate(updateActive);

        // Lấy danh sách
        try (ResultSet rs = st.executeQuery(select)) {
            while (rs.next()) {
                KhuyenMai km = new KhuyenMai(
                        rs.getString("MaKM"),
                        rs.getString("TenKM"),
                        rs.getDate("NgayBdau"),
                        rs.getDate("NgayKthuc"),
                        rs.getString("LoaiSP"),
                        rs.getFloat("GiamGia"),
                        rs.getString("TrangThai")
                );
                list.add(km);
            }
        }
    } catch (Exception ex) {
        ex.printStackTrace();
    }
    return list;
}


    public boolean replaceChiTietForMaKM(String maKM, List<String> dsMaSP) {
        String deleteSql = "DELETE FROM KhuyenMaiChiTiet WHERE MaKM = ?";
        String insertSql = "INSERT INTO KhuyenMaiChiTiet (MaKM, MaSP) VALUES (?, ?)";

        try (Connection con = DBConnect.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement pdel = con.prepareStatement(deleteSql)) {
                pdel.setString(1, maKM);
                pdel.executeUpdate();
            }

            if (dsMaSP != null && !dsMaSP.isEmpty()) {
                try (PreparedStatement pins = con.prepareStatement(insertSql)) {
                    for (String maSP : dsMaSP) {
                        pins.setString(1, maKM);
                        pins.setString(2, maSP);
                        pins.addBatch();
                    }
                    pins.executeBatch();
                }
            }

            con.commit();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            // nếu muốn, rollback thủ công (try-with-resources sẽ auto close)
            return false;
        }
    }

    public Object[] getRowKM(KhuyenMai km) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return new Object[]{
            km.getMaKM(),
            km.getTenKm(),
            sdf.format(km.getNgayBdau()),
            sdf.format(km.getNgayKthuc()),
            km.getLoaisp(),
            (km.getGiamgia() * 100) + "%", // nếu giamgia lưu 0.1 -> hiển thị 10%
            km.getTrangThai()
        };
    }

    private String getLastMaKM() throws Exception {
        String sql = "SELECT TOP 1 MaKM FROM KhuyenMai ORDER BY LEN(MaKM) DESC, MaKM DESC";
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString("MaKM");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception("Lỗi khi truy vấn mã KM cuối cùng: " + ex.getMessage());
        }
        return null;
    }

    // Phương thức sinh mã KM mới
    public String generateNewMaKM() throws Exception {
        String lastMaKM = getLastMaKM();
        System.out.println("[DEBUG] Last MaKM from DB: " + lastMaKM); // Thêm dòng này

        if (lastMaKM != null && lastMaKM.matches("KM\\d+")) {
            try {
                int lastNumber = Integer.parseInt(lastMaKM.substring(2));
                System.out.println("[DEBUG] Last number: " + lastNumber); // Thêm dòng này
                return String.format("KM%d", lastNumber + 1);
            } catch (NumberFormatException e) {
                System.err.println("Lỗi khi chuyển đổi số từ mã KM: " + e.getMessage());
                return "KM1";
            }
        }
        return "KM1";
    }

    public List<String> getSanPhamTheoMaKM(String maKM) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT MaSP FROM KhuyenMaiChiTiet WHERE MaKM = ?";
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKM);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("MaSP"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean isKhuyenMaiExistForProduct(String maSP, Date ngayBd, Date ngayKt) {
        String sql = "SELECT COUNT(*) FROM KhuyenMaiChiTiet ct JOIN KhuyenMai km ON ct.MaKM = km.MaKM "
                + "WHERE ct.MaSP = ? AND NOT (CAST(km.NgayKthuc AS date) < CAST(? AS date) OR CAST(km.NgayBdau AS date) > CAST(? AS date))";
        try (Connection con = DBConnect.getConnection(); PreparedStatement p = con.prepareStatement(sql)) {
            p.setString(1, maSP);
            p.setDate(2, new java.sql.Date(ngayBd.getTime()));
            p.setDate(3, new java.sql.Date(ngayKt.getTime()));
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public List<Model.SanPham> getProductDetailsByMaKM(String maKM) {
        List<Model.SanPham> list = new ArrayList<>();
        String sql = "SELECT sp.* FROM SanPham sp JOIN KhuyenMaiChiTiet ct ON sp.MaSP = ct.MaSP WHERE ct.MaKM = ?";
        try (Connection con = DBConnect.getConnection(); PreparedStatement p = con.prepareStatement(sql)) {
            p.setString(1, maKM);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    // Giả sử lớp SanPham có constructor phù hợp. Thay đổi tuỳ model của bạn
                    Model.SanPham sp = new Model.SanPham(
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public List<String> getProductsByMaKM(String maKM) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT MaSP FROM KhuyenMaiChiTiet WHERE MaKM = ?";
        try (Connection con = DBConnect.getConnection(); PreparedStatement p = con.prepareStatement(sql)) {
            p.setString(1, maKM);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("MaSP"));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }
}
