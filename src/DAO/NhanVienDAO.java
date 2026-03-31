/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Model.NhanVien;
import Service.DBConnect;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Date;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 *
 * @author XPS
 */
public class NhanVienDAO {

    public Object[] getRow(NhanVien nv) {
        String manv = nv.getManv();
        String tennv = nv.getTennv();
        String sdt = nv.getSdt();
        String vaitro = nv.getVaitro();
        String email = nv.getEmail();
        String diaChi = nv.getDiaChi();
        String gioiTinh = nv.getGioitinh();
        String trangThai = nv.getTrangThai();
        Date ngaySinh = nv.getNgaySinh();

        Object[] obj = new Object[]{manv, tennv, sdt, vaitro, email, diaChi, gioiTinh, trangThai, ngaySinh};
        return obj;
    }

    public NhanVien getNhanVienByMa(String maNV) {
        String sql = "SELECT * FROM NhanVien WHERE MaNV = ?";
        try (Connection con = DBConnect.getConnection(); PreparedStatement pstm = con.prepareStatement(sql)) {

            pstm.setString(1, maNV);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                return new NhanVien(
                        rs.getString("MaNV"),
                        rs.getString("TenNV"),
                        rs.getString("SĐT"),
                        rs.getString("VaiTro"),
                        rs.getString("Email"),
                        rs.getString("DiaChi"),
                        rs.getString("GioiTinh"),
                        rs.getString("TrangThai"),
                        rs.getDate("NgaySinh")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<NhanVien> getAllNV() {
        return getNhanVienByStatus(null);
    }

    // Phương thức chung để lọc theo trạng thái
    public  List<NhanVien> getNhanVienByStatus(String status) {
        List<NhanVien> list = new ArrayList<>();
        String sql = "SELECT * FROM NhanVien";

        if (status != null) {
            sql += " WHERE TrangThai = ?";
        }

        try (Connection con = DBConnect.getConnection(); PreparedStatement pstm = con.prepareStatement(sql)) {

            if (status != null) {
                pstm.setString(1, status);
            }

            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                NhanVien nv = new NhanVien(
                        rs.getString("MaNV"),
                        rs.getString("TenNV"),
                        rs.getString("SĐT"),
                        rs.getString("VaiTro"),
                        rs.getString("Email"),
                        rs.getString("DiaChi"),
                        rs.getString("GioiTinh"),
                        rs.getString("TrangThai"),
                        rs.getDate("NgaySinh")
                );
                list.add(nv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Lấy nhân viên đang làm việc
    public List<NhanVien> getNhanVienDangLam() {
        return getNhanVienByStatus("Đang làm");
    }

    // Lấy nhân viên tạm nghỉ
    public List<NhanVien> getNhanVienTamNghi() {
        return getNhanVienByStatus("Tạm nghỉ");
    }

    // Lấy nhân viên đã nghỉ việc
    public List<NhanVien> getNhanVienNghiViec() {
        return getNhanVienByStatus("Nghỉ việc");
    }

    // Lấy nhân viên đã khóa
    public List<NhanVien> getNhanVienDaKhoa() {
        return getNhanVienByStatus("Đã khóa");
    }

    public List<NhanVien> getAllNV1() {
        List<NhanVien> listnv = new ArrayList<>();
        try {
            String sql = "SELECT * FROM NHANVIEN where TrangThai like N'%Nghỉ việc%'";
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery(sql);

            while (rs.next()) {
                NhanVien nv = new NhanVien();
                nv.setManv(rs.getString(1));
                nv.setTennv(rs.getString(2));
                nv.setSdt(rs.getString(3));
                nv.setVaitro(rs.getString(4));
                nv.setEmail(rs.getString(5));
                nv.setDiaChi(rs.getString(6));
                nv.setGioitinh(rs.getString(7));
                nv.setTrangThai(rs.getString(8));
                nv.setNgaySinh(rs.getDate(9));

                listnv.add(nv);
            }
        } catch (Exception ex) {
        }
        return listnv;
    }
    
    public List<NhanVien> getAllNV12() {
        List<NhanVien> listnv = new ArrayList<>();
        try {
            String sql = "SELECT * FROM NHANVIEN where TrangThai like N'%Đang làm%'";
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery(sql);

            while (rs.next()) {
                NhanVien nv = new NhanVien();
                nv.setManv(rs.getString(1));
                nv.setTennv(rs.getString(2));
                nv.setSdt(rs.getString(3));
                nv.setVaitro(rs.getString(4));
                nv.setEmail(rs.getString(5));
                nv.setDiaChi(rs.getString(6));
                nv.setGioitinh(rs.getString(7));
                nv.setTrangThai(rs.getString(8));
                nv.setNgaySinh(rs.getDate(9));

                listnv.add(nv);
            }
        } catch (Exception ex) {
        }
        return listnv;
    }

    public int addNV(NhanVien nv) {
    String sql = "INSERT INTO NhanVien(MaNV,TenNV,SĐT,Vaitro,Email,DiaChi,GioiTinh,TrangThai,NgaySinh) VALUES (?,?,?,?,?,?,?,?,?)";
    try (Connection con = DBConnect.getConnection(); PreparedStatement pstm = con.prepareStatement(sql)) {

        pstm.setString(1, nv.getManv());
        pstm.setString(2, nv.getTennv());
        pstm.setString(3, nv.getSdt());
        pstm.setString(4, nv.getVaitro());
        pstm.setString(5, nv.getEmail());
        pstm.setString(6, nv.getDiaChi());
        pstm.setString(7, nv.getGioitinh());
        pstm.setString(8, "Đang làm"); // Mặc định luôn "Đang làm"
        pstm.setDate(9, nv.getNgaySinh());

        return pstm.executeUpdate();
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Mã nhân viên đã tồn tại!");
    }
    return 0;
}


    public int editNV(NhanVien nv, String manv) {
        String sql = "UPDATE NhanVien SET\n"
                + "TenNV = ?,\n"
                + "SĐT = ?,\n"
                + "Vaitro = ?,\n"
                + "Email = ?,\n"
                + "DiaChi = ?,\n"
                + "GioiTinh = ?,\n"
                + "TrangThai = ?,\n"
                + "NgaySinh = ?\n"
                + "WHERE MaNV = ?";
        try {
            Connection con = DBConnect.getConnection();
            PreparedStatement pstm = con.prepareStatement(sql);
            pstm.setString(1, nv.getTennv());
            pstm.setString(2, nv.getSdt());
            pstm.setString(3, nv.getVaitro());
            pstm.setString(4, nv.getEmail());
            pstm.setString(5, nv.getDiaChi());
            pstm.setString(6, nv.getGioitinh());
            pstm.setString(7, nv.getTrangThai());
            pstm.setDate(8, nv.getNgaySinh());
            pstm.setString(9, manv); // WHERE MaNV = ?

            return pstm.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public int deleteNV(String manv) {
        String sql = "UPDATE NhanVien SET TrangThai = N'Nghỉ việc' WHERE MaNV = ?";
        try (Connection con = DBConnect.getConnection(); PreparedStatement pstm = con.prepareStatement(sql)) {

            pstm.setString(1, manv);
            return pstm.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean kiemTraNhanVienDaBanHang(String maNV) {
        String sql = """
        SELECT COUNT(*) 
        FROM HoaDonChiTiet ct
        JOIN HoaDon hd ON ct.MaHD = hd.MaHD
        WHERE hd.MaNV = ?
    """;

        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNV);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public NhanVien timNVTheoSDT(String sdtnv) {
        String sql = "SELECT * FROM NhanVien WHERE SĐT = ?";
        try {
            Connection con = DBConnect.getConnection();
            PreparedStatement pstm = con.prepareStatement(sql);
            pstm.setString(1, sdtnv);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                return new NhanVien(
                        rs.getString("TenNV"),
                        rs.getString("MaNV"),
                        rs.getString("SĐT"),
                        rs.getString("VaiTro"),
                        rs.getString("Email"),
                        rs.getString("DiaChi"),
                        rs.getString("GioiTinh"),
                        rs.getString("TrangThai"),
                        rs.getDate("NgaySinh")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Không tìm thấy
    }

    public int updateTrangThai(String maNV, String trangThai) {
        String sql = "UPDATE NhanVien SET TrangThai = ? WHERE MaNV = ?";
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, trangThai);
            ps.setString(2, maNV);
            return ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public NhanVien selectByMaNV(String maNV) {
        String sql = "SELECT * FROM NhanVien WHERE MaNV = ?";
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                NhanVien nv = new NhanVien();
                nv.setManv(rs.getString("MaNV"));
                nv.setTennv(rs.getString("TenNV"));
                nv.setSdt(rs.getString("SĐT"));
                nv.setVaitro(rs.getString("Vaitro"));
                nv.setEmail(rs.getString("Email"));
                nv.setDiaChi(rs.getString("DiaChi"));
                nv.setGioitinh(rs.getString("GioiTinh"));
                nv.setTrangThai(rs.getString("TrangThai"));
                nv.setNgaySinh(rs.getDate("NgaySinh"));
                return nv;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isNhanVienDangLam(String maNV) {
        if (maNV == null || maNV.trim().isEmpty()) {
            System.out.println("[ERROR] Mã NV không hợp lệ");
            return false;
        }

        String sql = "SELECT TrangThai FROM NhanVien WHERE MaNV = ?";

        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maNV.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String trangThai = rs.getString("TrangThai");

                    if (trangThai == null) {
                        System.out.println("[DEBUG] Trạng thái NULL");
                        return false;
                    }

                    String tt = trangThai.trim().toLowerCase();

                    System.out.printf("[DEBUG] Mã NV: %s, Trạng thái gốc: '%s', Sau xử lý: '%s'%n",
                            maNV, trangThai, tt);

                    // Chấp nhận mọi biến thể chứa từ "đang" hoặc "hoạt"
                    return tt.contains("đang") || tt.contains("làm");
                } else {
                    System.out.println("[WARN] Không tìm thấy nhân viên có mã: " + maNV);
                    return false;
                }
            }

        } catch (Exception e) {
            System.out.println("[ERROR] Lỗi kiểm tra trạng thái nhân viên: " + e.getMessage());
            return false;
        }
    }

    // Thêm vào NhanVienDAO
    public String getTrangThai(String maNV) {
        String sql = "SELECT TrangThai FROM NhanVien WHERE MaNV = ?";
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("TrangThai") : null;
        } catch (Exception e) {
            return null;
        }
    }

    public String taoMaNVMoi() {
        String maNV = "NV001"; // Mã mặc định nếu chưa có hóa đơn nào
        String sql = "SELECT TOP 1 MaNV FROM NhanVien ORDER BY MaNV DESC";

        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                String lastMaNV = rs.getString("MaNV");
                int number = Integer.parseInt(lastMaNV.substring(2)) + 1;
                maNV = String.format("NV%03d", number);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return maNV;
    }

    public String layEmailTheoLienHe(String sdt) {
        String sql = "SELECT Email FROM NhanVien WHERE SĐT = ?";
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sdt);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("Email") : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean kiemTraSoDienThoaiTonTai(String sdt) {
        String sql = "SELECT 1 FROM NhanVien WHERE SĐT = ?";
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sdt);
            ResultSet rs = ps.executeQuery();
            return rs.next(); // nếu có bản ghi thì tồn tại
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean kiemTraEmailTonTai(String email) {
        String sql = "SELECT COUNT(*) FROM NhanVien WHERE Email = ?";
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
