/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package View;

import DAO.KhuyenMaiDAO;
import DAO.SanPhamDAO;
import Model.KhuyenMai;
import Model.SanPham;
import Service.DBConnect;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.ParseException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

/**
 *
 * @author XPS
 */
public class QLKM extends javax.swing.JPanel {

    DefaultTableModel tableModelKM;  // cho jTable2 (Khuyến mãi)
    DefaultTableModel tableModelSP;  // cho jTable1 (Sản phẩm)

    KhuyenMaiDAO kmdao = new KhuyenMaiDAO();
    SanPhamDAO spdao = new SanPhamDAO();

    /**
     * Creates new form QLKM
     */
    public QLKM() {
        initComponents();
        TF_MaKM.setEditable(false);

        // Không cho chọn ngày trong quá khứ
        TF_NgayBD.setDateFormatString("yyyy/MM/dd");
        TF_NgayKT.setDateFormatString("yyyy/MM/dd");

// Không cho chọn ngày trong quá khứ
        TF_NgayBD.setMinSelectableDate(new Date());
        TF_NgayKT.setMinSelectableDate(new Date());

        // === Khởi tạo và đổ dữ liệu cho bảng khuyến mãi ===
        initTableKhuyenMai();      // Tạo cột
        fillTableKhuyenMai();      // Đổ dữ liệu

        // Gán mã KM tự động
        try {
            TF_MaKM.setText(kmdao.generateNewMaKM());
        } catch (Exception ex) {
            ex.printStackTrace();
            TF_MaKM.setText("KM1"); // Fallback nếu có lỗi
        }

        // === Lọc theo loại sản phẩm khi gõ ===
        txtApDungCho.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                locTheoKhoangGia();
            }
        });

        // Ẩn các thành phần không cần thiết
        jLabel4.setVisible(false);
        txtGiaMin.setVisible(false);
        jLabel13.setVisible(false);
        txtGiaMax.setVisible(false);

        // === Khởi tạo bảng sản phẩm có checkbox ở CUỐI ===
        DefaultTableModel modelSP = new DefaultTableModel(
                new Object[]{"Mã SP", "Tên SP", "Loại SP", "Giá", "Số Lượng", "Màu Sắc", "Kích Thước", "Chất Liệu", "Trạng Thái", ""}, 0
        ) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 3 ->
                        Double.class;
                    case 4 ->
                        Integer.class;
                    case 9 ->
                        Boolean.class;
                    default ->
                        String.class;
                };
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 9; // chỉ cho tick cột checkbox
            }
        };

        // Đổ dữ liệu sản phẩm
        for (SanPham sp : spdao.getAll()) {
            modelSP.addRow(new Object[]{
                sp.getMasp(), sp.getTensp(), sp.getLoaisp(), sp.getGia(),
                sp.getSluong(), sp.getMausac(), sp.getKichThuoc(),
                sp.getChatLieu(), sp.getTrangThai(), false
            });
        }

        jTable1.setModel(modelSP);

        // === Xử lý radio button "Tất cả sản phẩm" ===
        rdoTatCaSanPham.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isSelected = rdoTatCaSanPham.isSelected();
                jTable1.setEnabled(!isSelected);
                if (isSelected) {
                    for (int i = 0; i < jTable1.getRowCount(); i++) {
                        jTable1.setValueAt(false, i, 9);
                    }
                }
            }
        });
    }

    public void initTableKhuyenMai() {
        String[] cols = {
            "Mã KM", "Tên KM",
            "Ngày Bắt đầu", "Ngày Kết thúc", "Loại SP", "Giảm giá", "Trạng thái"
        };
        tableModelKM = new DefaultTableModel(cols, 0);
        jTable2.setModel(tableModelKM);
    }

    public void fillTableKhuyenMai() {
        tableModelKM.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        for (KhuyenMai km : kmdao.getAllKM()) {
            tableModelKM.addRow(new Object[]{
                km.getMaKM(),
                km.getTenKm(),
                sdf.format(km.getNgayBdau()),
                sdf.format(km.getNgayKthuc()),
                km.getLoaisp(),
                km.getGiamgia(),
                km.getTrangThai()
            });
        }
    }

    private void anhiengia(java.awt.event.ItemEvent evt) {
        String selected = cboHinhThuc.getSelectedItem().toString();

        if (selected.equals("Theo khoảng giá")) {
            jLabel4.setVisible(true);
            txtGiaMin.setVisible(true);
            jLabel13.setVisible(true);
            txtGiaMax.setVisible(true);
        } else {
            jLabel4.setVisible(false);
            txtGiaMin.setVisible(false);
            jLabel13.setVisible(false);
            txtGiaMax.setVisible(false);
        }
    }

    private boolean validateFormKhuyenMai() {
        String maKM = TF_MaKM.getText();
        Date ngayBD = TF_NgayBD.getDate();
        Date ngayKT = TF_NgayKT.getDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);

        // Mã KM
        if (maKM == null || !maKM.trim().startsWith("KM")) {
            JOptionPane.showMessageDialog(this, "Mã khuyến mãi phải bắt đầu bằng 'KM'.");
            TF_MaKM.requestFocus();
            return false;
        }

        // Tên KM
        if (TF_TenKM.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên khuyến mãi.");
            TF_TenKM.requestFocus();
            return false;
        }

        // Giảm giá: bắt buộc nhập và ở khoảng 10% - 70% (0.1 - 0.7)
        String txtGiam = TF_GiamGia.getText().trim();
        if (txtGiam.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập giảm giá.");
            TF_GiamGia.requestFocus();
            return false;
        }

        float giamHeSo;
        try {
            float raw = Float.parseFloat(txtGiam);
            // Nếu người dùng nhập 10 (nghĩa 10%) => chia 100
            if (raw > 1f) {
                giamHeSo = raw / 100f;
            } else {
                giamHeSo = raw;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Giảm giá phải là số hợp lệ (ví dụ 10 hoặc 0.1).");
            TF_GiamGia.requestFocus();
            return false;
        }

        // Giới hạn 10% - 70%
        if (giamHeSo < 0.1f || giamHeSo > 0.7f) {
            JOptionPane.showMessageDialog(this, "Giảm giá phải nằm trong khoảng 10% đến 70% (nhập 10 hoặc 0.1 → 10%).");
            TF_GiamGia.requestFocus();
            return false;
        }

        // Trạng thái
        if (jComboBox2.getSelectedItem() == null || jComboBox2.getSelectedItem().toString().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn trạng thái.");
            return false;
        }

        // Ngày
        if (ngayBD == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày bắt đầu.");
            return false;
        }
        if (ngayKT == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày kết thúc.");
            return false;
        }

        // So sánh ngày theo phần ngày (bỏ giờ)
        Date today;
        try {
            today = sdf.parse(sdf.format(new Date()));
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }

        if (ngayBD.before(today)) {
            JOptionPane.showMessageDialog(this, "Ngày bắt đầu không được ở trong quá khứ.");
            return false;
        }

        if (ngayKT.before(today)) {
            JOptionPane.showMessageDialog(this, "Ngày kết thúc không được ở trong quá khứ.");
            return false;
        }

        if (ngayKT.before(ngayBD)) {
            JOptionPane.showMessageDialog(this, "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.");
            return false;
        }

        // Nếu cần dùng giá trị hệ số giamgia sau này, bạn có thể lưu vào 1 biến thành viên ở đây
        // this.currentGiamHeSo = giamHeSo;
        return true;
    }

    public void showDetailKhuyenMai() {
        int i = jTable2.getSelectedRow();
        if (i >= 0) {
            // ✅ Lấy thông tin khuyến mãi
            KhuyenMai km = kmdao.getAllKM().get(i);

            TF_MaKM.setText(km.getMaKM());
            TF_TenKM.setText(km.getTenKm());
            TF_NgayBD.setDate(km.getNgayBdau());
            TF_NgayKT.setDate(km.getNgayKthuc());
            TF_GiamGia.setText(String.valueOf(km.getGiamgia()));
            jComboBox2.setSelectedItem(km.getTrangThai());

            // ✅ Bỏ tick toàn bộ trước khi set
            for (int row = 0; row < jTable1.getRowCount(); row++) {
                jTable1.setValueAt(false, row, 9); // cột checkbox
            }

            // ✅ Lấy danh sách sản phẩm đã thuộc KM
            List<String> dsSPKM = kmdao.getSanPhamTheoMaKM(km.getMaKM());

            // ✅ Tick lại sản phẩm có trong KM
            for (int row = 0; row < jTable1.getRowCount(); row++) {
                String maSP = jTable1.getValueAt(row, 0).toString();
                if (dsSPKM.contains(maSP)) {
                    jTable1.setValueAt(true, row, 9);
                }
            }
        }
    }

    public void capNhatKhuyenMai() {
        int selectedRow = jTable2.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khuyến mãi cần cập nhật.");
            return;
        }

        String maKM = jTable2.getValueAt(selectedRow, 0).toString().trim();
        String tenKm = TF_TenKM.getText().trim();
        String loaiSP = txtApDungCho.getText().trim();

        // Xử lý giảm giá
        float giamGiaInput;
        try {
            String txt = TF_GiamGia.getText().trim();
            giamGiaInput = txt.isEmpty() ? 0f : Float.parseFloat(txt);
            if (giamGiaInput > 1f) {
                giamGiaInput = giamGiaInput / 100f; // Chuyển từ % sang hệ số
            }
            if (giamGiaInput < 0f || giamGiaInput > 1f) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Giảm giá phải là số từ 0-100 (ví dụ: 10 hoặc 0.1).");
            return;
        }

        // Xử lý ngày
        Date ngayBd = TF_NgayBD.getDate();
        Date ngayKt = TF_NgayKT.getDate();
        if (ngayBd == null || ngayKt == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đầy đủ ngày bắt đầu và kết thúc.");
            return;
        }
        if (ngayKt.before(ngayBd)) {
            JOptionPane.showMessageDialog(this, "Ngày kết thúc phải sau ngày bắt đầu.");
            return;
        }

        // Xác định trạng thái tự động
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date today = cal.getTime();

        String trangThai;
        if (ngayBd.after(today)) {
            trangThai = "Sắp hoạt động";
        } else if (!today.before(ngayBd) && !today.after(ngayKt)) {
            trangThai = "Đang hoạt động";
        } else {
            trangThai = "Ngừng hoạt động";
        }

        // Lấy danh sách sản phẩm được chọn
        List<String> dsMaSP = new ArrayList<>();
        if (rdoTatCaSanPham.isSelected()) {
            // Lấy tất cả sản phẩm từ bảng
            for (int i = 0; i < jTable1.getRowCount(); i++) {
                Object val = jTable1.getValueAt(i, 0); // Cột mã SP
                if (val != null) {
                    dsMaSP.add(val.toString().trim());
                }
            }
        } else {
            // Lấy các sản phẩm được tick chọn
            for (int i = 0; i < jTable1.getRowCount(); i++) {
                Object checked = jTable1.getValueAt(i, 9); // Cột checkbox
                if (Boolean.TRUE.equals(checked)) {
                    Object val = jTable1.getValueAt(i, 0); // Cột mã SP
                    if (val != null) {
                        dsMaSP.add(val.toString().trim());
                    }
                }
            }
        }

        // Tạo đối tượng KhuyenMai
        KhuyenMai km = new KhuyenMai(maKM, tenKm, ngayBd, ngayKt, loaiSP, giamGiaInput, trangThai);

        try {
            // 1. Cập nhật thông tin khuyến mãi
            boolean updateKM = kmdao.capNhatKhuyenMai(km);

            // 2. Cập nhật danh sách sản phẩm áp dụng
            boolean updateCT = kmdao.replaceChiTietForMaKM(maKM, dsMaSP);

            if (updateKM && updateCT) {
                JOptionPane.showMessageDialog(this, "Cập nhật khuyến mãi thành công!");
                fillTableKhuyenMai(); // Cập nhật lại bảng hiển thị
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật khuyến mãi thất bại!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật: " + ex.getMessage());
        }
    }

    public void ngungHoatDongKhuyenMai() {
        int selectedRow = jTable2.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khuyến mãi để ngừng hoạt động.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn ngừng hoạt động khuyến mãi này?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String maKM = jTable2.getValueAt(selectedRow, 0).toString().trim();

        try {
            // Gọi DAO cập nhật trạng thái
            boolean result = kmdao.ngungHoatDongKhuyenMai(maKM);
            if (result) {
                JOptionPane.showMessageDialog(this, "Cập nhật trạng thái thành công!");
                fillTableKhuyenMai(); // Cập nhật lại bảng hiển thị
                lamMoiKhuyenMai();    // Reset form
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật trạng thái thất bại!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật trạng thái: " + ex.getMessage());
        }
    }

    public void luuKhuyenMai() {
        // Validate form
        if (!validateFormKhuyenMai()) {
            return;
        }

        // Tự động sinh mã KM nếu trường mã trống
        String maKM = TF_MaKM.getText().trim();
        if (maKM.isEmpty()) {
            try {
                System.out.println("[DEBUG] Đang sinh mã KM mới...");
                maKM = kmdao.generateNewMaKM();
                System.out.println("[DEBUG] Mã KM đã sinh: " + maKM);
                TF_MaKM.setText(maKM);
            } catch (Exception ex) {
                System.err.println("[ERROR] Lỗi khi sinh mã KM: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Lỗi khi sinh mã khuyến mãi: " + ex.getMessage());
                return;
            }
        }

        String tenKm = TF_TenKM.getText().trim();
        String loaiSP = txtApDungCho.getText().trim();

        // Parse giảm giá (giữ nguyên như cũ)
        float giamGiaInput;
        try {
            String txt = TF_GiamGia.getText().trim();
            if (txt.isEmpty()) {
                giamGiaInput = 0f;
            } else {
                giamGiaInput = Float.parseFloat(txt);
                if (giamGiaInput > 1f) {
                    giamGiaInput = giamGiaInput / 100f;
                }
            }
            if (giamGiaInput < 0f) {
                throw new NumberFormatException();
            }
            if (giamGiaInput > 1f) {
                giamGiaInput = 1f;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Giảm giá phải là số hợp lệ (ví dụ 10 hoặc 0.1).");
            return;
        }

        Date ngayBd = TF_NgayBD.getDate();
        Date ngayKt = TF_NgayKT.getDate();
        if (ngayBd == null || ngayKt == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày bắt đầu và ngày kết thúc.");
            return;
        }
        if (ngayBd.after(ngayKt)) {
            JOptionPane.showMessageDialog(this, "Ngày bắt đầu không được sau ngày kết thúc.");
            return;
        }

        // Xác định trạng thái (giữ nguyên như cũ)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date today = cal.getTime();

        String trangThai;
        if (ngayBd.after(today)) {
            trangThai = "Sắp hoạt động";
        } else if (!today.before(ngayBd) && !today.after(ngayKt)) {
            trangThai = "Đang hoạt động";
        } else {
            trangThai = "Ngừng hoạt động";
        }

        // Chuẩn bị danh sách MaSP (giữ nguyên như cũ)
        List<String> dsMaSP = new ArrayList<>();
        boolean allProducts = rdoTatCaSanPham.isSelected();
        if (allProducts) {
            for (int i = 0; i < jTable1.getRowCount(); i++) {
                Object val = jTable1.getValueAt(i, 0);
                if (val != null) {
                    dsMaSP.add(val.toString().trim());
                }
            }
        } else {
            for (int i = 0; i < jTable1.getRowCount(); i++) {
                Object checked = jTable1.getValueAt(i, 9);
                if (Boolean.TRUE.equals(checked)) {
                    Object val = jTable1.getValueAt(i, 0);
                    if (val != null) {
                        dsMaSP.add(val.toString().trim());
                    }
                }
            }
        }

        if (dsMaSP.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một sản phẩm hoặc bật 'Tất cả sản phẩm'.");
            return;
        }

        // Tạo object KhuyenMai
        KhuyenMai km = new KhuyenMai(maKM, tenKm, ngayBd, ngayKt, loaiSP, giamGiaInput, trangThai);

        try {
            // Kiểm tra nếu mã KM đã tồn tại (trường hợp người dùng nhập tay)
            boolean kmExists = kmdao.isMaKMExist(maKM);
            if (kmExists) {
                JOptionPane.showMessageDialog(this, "Mã khuyến mãi đã tồn tại. Vui lòng nhập mã khác hoặc để trống để tự sinh mã.");
                return;
            }

            // Lưu khuyến mãi mới
            boolean ok = kmdao.luuKhuyenMai(km, dsMaSP);
            if (ok) {
                fillTableKhuyenMai();
                JOptionPane.showMessageDialog(this, "Lưu khuyến mãi thành công. Mã: " + maKM);
            } else {
                JOptionPane.showMessageDialog(this, "Lưu khuyến mãi thất bại. Vui lòng kiểm tra log.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra: " + ex.getMessage());
        }
    }

    public void lamMoiKhuyenMai() {
        // Mã khuyến mãi mới
        try {
            TF_MaKM.setText(kmdao.generateNewMaKM());
        } catch (Exception ex) {
            ex.printStackTrace();
            TF_MaKM.setText("KM1");
        }

        // Reset thông tin KM
        TF_TenKM.setText("");
        TF_NgayBD.setDate(null);
        TF_NgayKT.setDate(null);
        TF_GiamGia.setText("");
        jComboBox2.setSelectedIndex(0); // Trạng thái

        // Reset phần lọc sản phẩm
        txtApDungCho.setText("");
        cboHinhThuc.setSelectedIndex(0); // Tất cả sản phẩm
        txtGiaMin.setText("");
        txtGiaMax.setText("");
        rdoTatCaSanPham.setSelected(false);

        // Bỏ chọn bảng KM
        jTable2.clearSelection();

        // Bỏ tick checkbox bảng sản phẩm
        for (int r = 0; r < jTable1.getRowCount(); r++) {
            jTable1.setValueAt(false, r, 9); // giả sử cột 9 là checkbox
        }
        jTable1.clearSelection();
    }

    private void locTheoKhoangGia() {
        String loai = txtApDungCho.getText().trim(); // Lọc theo loại sản phẩm
        String giaMinStr = txtGiaMin.getText().trim(); // Giá Min
        String giaMaxStr = txtGiaMax.getText().trim(); // Giá Max

        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0); // Xóa dữ liệu cũ

        try {
            Connection conn = DBConnect.getConnection();
            PreparedStatement pst;
            String sql;

            if (giaMinStr.isEmpty() || giaMaxStr.isEmpty()) {
                // Chỉ lọc theo loại sản phẩm
                sql = "SELECT * FROM SanPham WHERE LoaiSP LIKE ?";
                pst = conn.prepareStatement(sql);
                pst.setString(1, "%" + loai + "%");
            } else {
                // Lọc theo loại và khoảng giá
                double giaMin = Double.parseDouble(giaMinStr);
                double giaMax = Double.parseDouble(giaMaxStr);
                sql = "SELECT * FROM SanPham WHERE LoaiSP LIKE ? AND DonGia BETWEEN ? AND ?";
                pst = conn.prepareStatement(sql);
                pst.setString(1, "%" + loai + "%");
                pst.setDouble(2, giaMin);
                pst.setDouble(3, giaMax);
            }

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Object[] row = new Object[]{
                    rs.getString("MaSP"),
                    rs.getString("TenSP"),
                    rs.getString("LoaiSP"),
                    rs.getDouble("DonGia"),
                    rs.getInt("SoLuong"),
                    rs.getString("MauSac"),
                    rs.getString("KichThuoc"),
                    rs.getString("ChatLieu"),
                    rs.getString("TrangThai")
                };
                model.addRow(row);
            }

            rs.close();
            pst.close();
            conn.close();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đúng định dạng số cho giá.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi lọc sản phẩm: " + ex.getMessage());
        }
    }

    private void hienTatCaSanPham() {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0); // Xóa dữ liệu cũ

        try {
            Connection conn = DBConnect.getConnection();
            String sql = "SELECT * FROM SanPham";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[]{
                    rs.getString("MaSP"),
                    rs.getString("TenSP"),
                    rs.getString("LoaiSP"),
                    rs.getDouble("DonGia"),
                    rs.getInt("SoLuong"),
                    rs.getString("MauSac"),
                    rs.getString("KichThuoc"),
                    rs.getString("ChatLieu"),
                    rs.getString("TrangThai")
                };
                model.addRow(row);
            }

            rs.close();
            pst.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi hiển thị tất cả sản phẩm: " + e.getMessage());
        }
    }

    public String taoMaKhuyenMaiTiepTheo() {
        try {
            return kmdao.generateNewMaKM(); // Sử dụng phương thức từ DAO
        } catch (Exception ex) {
            ex.printStackTrace();
            return "KM1"; // Fallback nếu có lỗi
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        TF_LoaiSP2 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        txtApDungCho = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        cboHinhThuc = new javax.swing.JComboBox<>();
        txtGiaMin = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        txtGiaMax = new javax.swing.JTextField();
        rdoTatCaSanPham = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        TF_MaKM = new javax.swing.JTextField();
        TF_TenKM = new javax.swing.JTextField();
        jComboBox2 = new javax.swing.JComboBox<>();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        TF_GiamGia = new javax.swing.JTextField();
        TF_NgayBD = new com.toedter.calendar.JDateChooser();
        TF_NgayKT = new com.toedter.calendar.JDateChooser();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setText("Áp dụng cho :");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel1.setText("Khuyến mãi");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Danh sách sản phẩm"));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Áp dụng cho :");

        txtApDungCho.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtApDungChoKeyReleased(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setText("Hình thức :");

        cboHinhThuc.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Tất cả sản phẩm", "Theo khoảng giá" }));
        cboHinhThuc.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cboHinhThucItemStateChanged(evt);
            }
        });
        cboHinhThuc.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cboHinhThucMouseClicked(evt);
            }
        });
        cboHinhThuc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboHinhThucActionPerformed(evt);
            }
        });

        txtGiaMin.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtGiaMinKeyReleased(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Giá Min :");

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel13.setText("Giá Max :");

        txtGiaMax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtGiaMaxActionPerformed(evt);
            }
        });
        txtGiaMax.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtGiaMaxKeyReleased(evt);
            }
        });

        rdoTatCaSanPham.setText("All Sản Phẩm");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 815, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(rdoTatCaSanPham))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtApDungCho, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboHinhThuc, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, 68, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtGiaMin, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtGiaMax, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(61, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(txtApDungCho, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)
                            .addComponent(cboHinhThuc))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rdoTatCaSanPham)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(txtGiaMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtGiaMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Khuyến mãi"));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel6.setText("Tên khuyến mãi :");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel7.setText("Mã khuyến mãi :");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel8.setText("Ngày bắt đầu :");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel9.setText("Ngày kết thúc :");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel10.setText("Trạng thái  :");

        TF_MaKM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TF_MaKMActionPerformed(evt);
            }
        });
        TF_MaKM.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TF_MaKMKeyReleased(evt);
            }
        });

        TF_TenKM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TF_TenKMActionPerformed(evt);
            }
        });

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Đang hoạt động", "Ngừng hoạt động", "Sắp hoạt động" }));
        jComboBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox2ActionPerformed(evt);
            }
        });

        jButton1.setFont(new java.awt.Font("Segoe UI", 0, 36)); // NOI18N
        jButton1.setText("Lưu");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Segoe UI", 0, 36)); // NOI18N
        jButton2.setText("Cập nhật");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setFont(new java.awt.Font("Segoe UI", 0, 36)); // NOI18N
        jButton3.setText("Xoá");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setFont(new java.awt.Font("Segoe UI", 0, 36)); // NOI18N
        jButton4.setText("Làm mới");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel11.setText("Giảm giá :");

        TF_GiamGia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TF_GiamGiaActionPerformed(evt);
            }
        });

        TF_NgayBD.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TF_NgayBDMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(TF_MaKM, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(46, 46, 46))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 1, Short.MAX_VALUE))
                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addGap(18, 18, 18)
                        .addComponent(TF_GiamGia, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel9)
                            .addComponent(jLabel6)
                            .addComponent(jLabel8))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(TF_TenKM, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
                            .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TF_NgayBD, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(TF_NgayKT, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addGap(49, 49, 49)
                    .addComponent(jLabel7)
                    .addContainerGap(318, Short.MAX_VALUE)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(54, 54, 54)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(TF_MaKM, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(46, 46, 46)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(TF_TenKM, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(39, 39, 39)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                    .addComponent(TF_NgayBD, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(30, 30, 30)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TF_NgayKT, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TF_GiamGia, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(64, 64, 64))
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addGap(57, 57, 57)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(665, Short.MAX_VALUE)))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Danh sách khuyến mãi"));

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable2MouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jTable2);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 815, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(55, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(48, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(625, 625, 625)
                        .addComponent(jLabel1))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(58, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void TF_MaKMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TF_MaKMActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_MaKMActionPerformed

    private void TF_TenKMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TF_TenKMActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_TenKMActionPerformed

    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        lamMoiKhuyenMai();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void cboHinhThucActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboHinhThucActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_cboHinhThucActionPerformed

    private void TF_GiamGiaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TF_GiamGiaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_GiamGiaActionPerformed

    private void jTable2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable2MouseClicked
        // TODO add your handling code here:
        showDetailKhuyenMai();

    }//GEN-LAST:event_jTable2MouseClicked

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        if (validateFormKhuyenMai()) {
            capNhatKhuyenMai();
        }
        return;
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:

        ngungHoatDongKhuyenMai();

    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        if (validateFormKhuyenMai()) {
            luuKhuyenMai();
        }
        return;
    }//GEN-LAST:event_jButton1ActionPerformed

    private void txtGiaMaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtGiaMaxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtGiaMaxActionPerformed

    private void cboHinhThucMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cboHinhThucMouseClicked
        // TODO add your handling code here:

    }//GEN-LAST:event_cboHinhThucMouseClicked

    private void cboHinhThucItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboHinhThucItemStateChanged
        // TODO add your handling code here:

        String selected = cboHinhThuc.getSelectedItem().toString();
        if (selected.equals("Tất cả sản phẩm")) {
            hienTatCaSanPham(); // ✅ Gọi hàm để hiển thị tất cả
        } else if (selected.equals("Theo khoảng giá")) {
            locTheoKhoangGia(); // ✅ Gọi hàm lọc theo giá (bạn đã có)
        }

        if (selected.equals("Theo khoảng giá")) {
            jLabel4.setVisible(true);
            txtGiaMin.setVisible(true);
            jLabel13.setVisible(true);
            txtGiaMax.setVisible(true);
        } else {
            jLabel4.setVisible(false);
            txtGiaMin.setVisible(false);
            jLabel13.setVisible(false);
            txtGiaMax.setVisible(false);
        }

    }//GEN-LAST:event_cboHinhThucItemStateChanged

    private void txtGiaMinKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtGiaMinKeyReleased
        // TODO add your handling code here: 
        locTheoKhoangGia();
    }//GEN-LAST:event_txtGiaMinKeyReleased

    private void txtGiaMaxKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtGiaMaxKeyReleased
        // TODO add your handling code here:
        locTheoKhoangGia();
    }//GEN-LAST:event_txtGiaMaxKeyReleased

    private void txtApDungChoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtApDungChoKeyReleased

    }//GEN-LAST:event_txtApDungChoKeyReleased

    private void TF_MaKMKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TF_MaKMKeyReleased
        // TODO add your handling code here:


    }//GEN-LAST:event_TF_MaKMKeyReleased

    private void TF_NgayBDMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TF_NgayBDMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_NgayBDMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField TF_GiamGia;
    private javax.swing.JTextField TF_LoaiSP2;
    private javax.swing.JTextField TF_MaKM;
    private com.toedter.calendar.JDateChooser TF_NgayBD;
    private com.toedter.calendar.JDateChooser TF_NgayKT;
    private javax.swing.JTextField TF_TenKM;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.JComboBox<String> cboHinhThuc;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JRadioButton rdoTatCaSanPham;
    private javax.swing.JTextField txtApDungCho;
    private javax.swing.JTextField txtGiaMax;
    private javax.swing.JTextField txtGiaMin;
    // End of variables declaration//GEN-END:variables
}
