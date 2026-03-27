/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package View;

import DAO.KhachHangDAO;
import DAO.HoaDonDao; // Cần import HoaDonDAO để lấy lịch sử giao dịch
import Model.KhachHang;
import Model.HoaDon; // Cần import HoaDon model để hiển thị lịch sử giao dịch
import java.sql.SQLException;
import java.time.format.DateTimeFormatter; // Để format ngày tháng cho lịch sử giao dịch
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JFrame; // Có thể không cần nếu QLKH là JPanel
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Dimension; // Import này để dùng Dimension
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class QLKH extends javax.swing.JPanel {

    DefaultTableModel modelKhachHang;
    DefaultTableModel modelLichSuGiaoDich; // Model cho bảng lịch sử giao dịch

    private JPopupMenu suggestionPopup;
    private JList<String> suggestionList;
    private DefaultListModel<String> suggestionListModel;
    // private KhachHangDAO khDAO; // Đảm bảo đã khai báo và khởi tạo ở đâu đó, nếu chưa thì khai báo ở đây
    private List<KhachHang> currentSuggestions; // Để lưu trữ đối tượng KhachHang tương ứng với gợi ý

    private KhachHangDAO khDAO = new KhachHangDAO();
    private HoaDonDao hdDAO = new HoaDonDao(); // Khởi tạo HoaDonDAO

    private List<KhachHang> currentKhachHangList; // List lưu trữ khách hàng hiện tại (sau lọc/tìm kiếm)

    // Định dạng ngày tháng UI cho lịch sử giao dịch (nếu cần)
    private static final DateTimeFormatter UI_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public QLKH() {
        initComponents(); // Khởi tạo các components của NetBeans
        setupTables();    // Cài đặt cấu trúc cột cho các bảng
        loadKhachHangData(); // Tải dữ liệu khách hàng ban đầu

        // Thêm Listener cho ComboBox lọc
        jComboBox3.addActionListener(e -> filter()); // Giới tính
        jComboBox4.addActionListener(e -> filter()); // Trạng thái
        rdokhachmoi.setSelected(true); // Mặc định khách mới
        disableRadioClick(rdokhachmoi);
        disableRadioClick(rdokhachquen);

        // Thêm listener cho TabbedPane để tải dữ liệu lịch sử giao dịch
        jTabbedPane1.addChangeListener(e -> {
            if (jTabbedPane1.getSelectedIndex() == 1) { // Tab "Lịch sử giao dịch"
                displayLichSuGiaoDich();
            }
        });

        if (khDAO == null) {
            khDAO = new KhachHangDAO();
        }

        suggestionListModel = new DefaultListModel<>();
        suggestionList = new JList<>(suggestionListModel);
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setFocusable(false); // Quan trọng: không lấy focus từ JTextField

        suggestionPopup = new JPopupMenu();
        suggestionPopup.add(new JScrollPane(suggestionList));
        suggestionPopup.setFocusable(false); // Quan trọng: không lấy focus từ JTextField
        // suggestionPopup.setOpaque(false); // Dòng này có thể gây vấn đề hiển thị trên một số LookAndFeel, có thể bỏ qua nếu không cần thiết.

        currentSuggestions = new ArrayList<>(); // Khởi tạo danh sách gợi ý hiện tại
// Thêm DocumentListener cho TF_SDT
        TF_MaKH1.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                showSuggestions();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                showSuggestions();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Không dùng cho PlainDocument
            }
        });

        // Thêm KeyListener cho TF_SDT để xử lý phím điều hướng
        TF_MaKH1.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (!suggestionListModel.isEmpty()) {
                        suggestionList.requestFocusInWindow(); // Chuyển focus sang JList
                        suggestionList.setSelectedIndex(0); // Chọn mục đầu tiên
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (suggestionPopup.isVisible() && !suggestionList.isSelectionEmpty()) {
                        applySelectedSuggestion();
                        e.consume(); // Ngăn không cho Enter xử lý tiếp
                    } else {
                        // Nếu không có gợi ý hoặc không chọn gì, bạn có thể thực hiện tìm kiếm khách hàng ở đây
                        // Ví dụ: gọi hàm tìm kiếm chính của bạn (nếu có)
                        // performSearchCustomer();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hideSuggestions();
                }
            }
        });

        // Thêm KeyListener cho JList để xử lý phím điều hướng (khi focus ở JList)
        suggestionList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    applySelectedSuggestion();
                    hideSuggestions();
                    TF_MaKH1.requestFocusInWindow(); // Trả focus về JTextField
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hideSuggestions();
                    TF_MaKH1.requestFocusInWindow();
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    if (suggestionList.getSelectedIndex() == 0) {
                        // Nếu đang ở đầu danh sách, chuyển focus về JTextField
                        TF_MaKH1.requestFocusInWindow();
                        suggestionList.clearSelection();
                        e.consume();
                    }
                }
            }
        });

        // Thêm MouseListener cho JList để xử lý click chuột
        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) { // Click chuột đơn
                    applySelectedSuggestion();
                    hideSuggestions();
                    TF_MaKH1.requestFocusInWindow();
                }
            }
        });

        // -------------------------------------------------------------------
        // Kết thúc phần code bạn sẽ thêm vào
        // -------------------------------------------------------------------
    }

    private void disableRadioClick(JRadioButton radio) {
        radio.setFocusable(false);               // Không focus khi Tab
        radio.setRequestFocusEnabled(false);     // Không nhận focus
        for (MouseListener ml : radio.getMouseListeners()) {
            radio.removeMouseListener(ml);       // Xóa toàn bộ listener click cũ
        }
        radio.addMouseListener(new MouseAdapter() {
        }); // Thêm listener rỗng để vô hiệu click
    }

    private void setupTables() {
        // --- SỬA Ở ĐÂY CHO modelKhachHang ---
        modelKhachHang = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Đặt tất cả các ô không thể chỉnh sửa trực tiếp trên bảng
            }
        };
        modelKhachHang.addColumn("Mã KH");
        modelKhachHang.addColumn("Tên KH");
        modelKhachHang.addColumn("Giới tính");
        modelKhachHang.addColumn("SĐT");
        modelKhachHang.addColumn("Trạng thái");
        modelKhachHang.addColumn("Địa chỉ");
        jTable2.setModel(modelKhachHang); // Gán model mới cho jTable2

        // --- Phần này của modelLichSuGiaoDich đã đúng (hoặc bạn có thể thêm lại nếu bị lỗi) ---
        modelLichSuGiaoDich = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        modelLichSuGiaoDich.addColumn("Mã HĐ");
        modelLichSuGiaoDich.addColumn("Ngày Tạo");
        modelLichSuGiaoDich.addColumn("Tổng Tiền");
        modelLichSuGiaoDich.addColumn("Trạng Thái HĐ"); // Lưu ý: trong code trước của tôi là modelLichSuGiaoDuyich, hãy sửa lại thành modelLichSuGiaoDich nếu bạn đã copy đúng.
        jTable3.setModel(modelLichSuGiaoDich);
    }

    public void fillToTable(List<KhachHang> list) {
        modelKhachHang.setRowCount(0); // Xóa dữ liệu cũ

        if (list == null || list.isEmpty()) {
            return;
        }

        for (KhachHang kh : list) {
            modelKhachHang.addRow(new Object[]{
                kh.getMakh(),
                kh.getTenkh(),
                kh.getGioiTinh(),
                kh.getSdt(),
                kh.getTrangThai(),
                kh.getDiaChi()
            });
        }
        TF_MaKH.setEditable(false);
    }

    // Phương thức lấy dữ liệu từ Form để tạo đối tượng KhachHang
    private KhachHang getForm() {
        String ma = TF_MaKH.getText().trim();
        String ten = TF_TenKH.getText().trim();
        String sdt = TF_SDT.getText().trim();
        String diachi = TA_diachi.getText().trim();
        String gt = rdonam.isSelected() ? "Nam" : "Nữ";
        String trangthai = rdokhachquen.isSelected() ? "Khách quen" : (rdokhachmoi.isSelected() ? "Khách mới" : "");
        return new KhachHang(ma, ten, gt, sdt, trangthai, diachi);
    }

    // Phương thức đặt dữ liệu từ đối tượng KhachHang lên Form
    private void setForm(KhachHang kh) {
        TF_MaKH.setText(kh.getMakh());
        TF_TenKH.setText(kh.getTenkh());
        TF_SDT.setText(kh.getSdt());
        TA_diachi.setText(kh.getDiaChi());

        // Giới tính
        if (kh.getGioiTinh() != null) {
            if (kh.getGioiTinh().equalsIgnoreCase("Nam")) {
                rdonam.setSelected(true);
            } else if (kh.getGioiTinh().equalsIgnoreCase("Nữ")) {
                rdonu.setSelected(true);
            } else {
                buttonGroup1.clearSelection();
            }
        } else {
            buttonGroup1.clearSelection();
        }

        // Trạng thái
        if (kh.getTrangThai() != null) {
            if (kh.getTrangThai().equalsIgnoreCase("Khách quen")) {
                rdokhachquen.setSelected(true);
            } else if (kh.getTrangThai().equalsIgnoreCase("Khách mới")) {
                rdokhachmoi.setSelected(true);
            } else {
                buttonGroup2.clearSelection();
            }
        } else {
            buttonGroup2.clearSelection();
        }

        // 🚫 Khóa không cho đổi trạng thái nhưng vẫn hiển thị bình thường
        rdokhachmoi.setFocusable(false);
        rdokhachquen.setFocusable(false);
        rdokhachmoi.setRequestFocusEnabled(false);
        rdokhachquen.setRequestFocusEnabled(false);

        // Hoặc cách chắc chắn hơn: chặn sự kiện click
        rdokhachmoi.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                evt.consume();
            }
        });
        rdokhachquen.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                evt.consume();
            }
        });

        // Xử lý khóa/mở form theo trạng thái "Khóa"
        if (kh.getTrangThai() != null && kh.getTrangThai().equalsIgnoreCase("Khóa")) {
            toggleInputFields(false);
            jButton17.setEnabled(false);
            jButton18.setEnabled(true);

            BT_timkh.setEnabled(true);
            TF_MaKH1.setEnabled(true);
            jButton14.setEnabled(true);
            jComboBox3.setEnabled(true);
            jComboBox4.setEnabled(true);

        } else {
            toggleInputFields(true);
            jButton17.setEnabled(true);
            jButton18.setEnabled(false);

            BT_timkh.setEnabled(true);
            TF_MaKH1.setEnabled(true);
            jButton14.setEnabled(true);
            jComboBox3.setEnabled(true);
            jComboBox4.setEnabled(true);
        }
    }

    private void clearForm() {
        TF_MaKH.setText(generateNewMaKH()); // Tạo mã KH mới khi làm sạch form
        TF_TenKH.setText("");
        TF_SDT.setText("");
        TA_diachi.setText("");
        buttonGroup1.clearSelection(); // Xóa lựa chọn radio button giới tính
        buttonGroup2.clearSelection(); // Xóa lựa chọn radio button trạng thái

        // Mặc định chọn "Khách mới" khi thêm mới
        rdokhachmoi.setSelected(true);

        // Cho phép nhập liệu
        toggleInputFields(true);
        jButton17.setEnabled(true);  // Kích hoạt nút Khóa
        jButton18.setEnabled(false); // Vô hiệu hóa nút Mở khóa

        // Khóa radio nhưng vẫn hiển thị rõ nét
        lockRadioButtons();

        // Clear selection on table
        jTable2.clearSelection();

        // Clear history table nếu có
        if (modelLichSuGiaoDich != null) {
            modelLichSuGiaoDich.setRowCount(0);
        }
    }

    private void lockRadioButtons() {
        // Xóa tất cả MouseListener cũ để tránh chồng sự kiện
        for (MouseListener ml : rdokhachmoi.getMouseListeners()) {
            rdokhachmoi.removeMouseListener(ml);
        }
        for (MouseListener ml : rdokhachquen.getMouseListeners()) {
            rdokhachquen.removeMouseListener(ml);
        }

        // Thêm chặn click mới
        rdokhachmoi.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                evt.consume();
            }
        });
        rdokhachquen.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                evt.consume();
            }
        });

        // Không cho focus
        rdokhachmoi.setFocusable(false);
        rdokhachquen.setFocusable(false);
    }

    private void toggleInputFields(boolean enable) {
        TF_MaKH.setEnabled(enable);
        TF_TenKH.setEnabled(enable);
        TF_SDT.setEnabled(enable);
        TA_diachi.setEnabled(enable); // Đảm bảo TA_diachi là tên biến của JTextArea Địa chỉ
        rdonam.setEnabled(enable); // Đảm bảo rdonam là tên biến của JRadioButton Nam
        rdonu.setEnabled(enable);   // Đảm bảo rdonu là tên biến của JRadioButton Nữ
        rdokhachquen.setEnabled(enable); // Đảm bảo rdokhachquen là tên biến của JRadioButton Khách quen
        rdokhachmoi.setEnabled(enable);  // Đảm bảo rdokhachmoi là tên biến của JRadioButton Khách mới

        jButton13.setEnabled(enable); // Nút "Sửa"
        jButton15.setEnabled(enable); // Nút "Thêm"
        // Nút "Khóa" (jButton17) và "Mở khóa" (jButton18) sẽ được xử lý riêng trong setForm
        // Các nút tìm kiếm, làm mới, lọc sẽ luôn hoạt động
        BT_timkh.setEnabled(true); // Nút Tìm kiếm (theo SĐT ở trên)
        TF_MaKH1.setEnabled(true); // Trường SĐT cho tìm kiếm
        jButton14.setEnabled(true); // Nút "Làm mới" (hoặc tên tương tự)
        jComboBox3.setEnabled(true); // Lọc Giới tính
        jComboBox4.setEnabled(true); // Lọc Trạng thái
    }

    private boolean validateForm(boolean isAddingNew) {
        // 1. Validate Tên Khách hàng
        String tenKH = TF_TenKH.getText().trim();
        if (tenKH.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên Khách Hàng không được để trống.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!tenKH.matches("^[\\p{L}\\s.'-]+$")) {
            JOptionPane.showMessageDialog(this, "Tên Khách Hàng chỉ chấp nhận chữ cái và dấu cách.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // 2. Validate Số điện thoại
        String sdt = TF_SDT.getText().trim();
        if (sdt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Số Điện Thoại không được để trống.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!sdt.matches("^0\\d{9}$")) { // Bắt đầu bằng 0 + 9 số tiếp theo
            JOptionPane.showMessageDialog(this, "Số Điện Thoại phải gồm 10 chữ số và bắt đầu bằng số 0.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (isAddingNew) {
            try {
                KhachHang existingKh = khDAO.getKhachHangBySdt(sdt);
                if (existingKh != null) {
                    JOptionPane.showMessageDialog(this, "Số Điện Thoại này đã tồn tại trong hệ thống.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi kiểm tra trùng Số Điện Thoại: " + e.getMessage(), "Lỗi DB", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                return false;
            }
        }

        // 3. Validate Giới tính
        if (!rdonam.isSelected() && !rdonu.isSelected()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Giới Tính (Nam hoặc Nữ).", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // 4. Validate Địa chỉ
        String diaChi = TA_diachi.getText().trim();
        if (diaChi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Địa Chỉ không được để trống.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (diaChi.length() < 5) {
            JOptionPane.showMessageDialog(this, "Địa Chỉ phải có ít nhất 5 ký tự.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    // Phương thức tải dữ liệu khách hàng từ DAO và đổ vào bảng
    private void loadKhachHangData() {
        try {
            currentKhachHangList = khDAO.getAllKhachHang();
            fillToTable(currentKhachHangList);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu khách hàng: " + e.getMessage(), "Lỗi DB", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Phương thức lọc khách hàng theo giới tính và trạng thái
    private void filter() {
        String gt = jComboBox3.getSelectedItem().toString().trim();
        String tt = jComboBox4.getSelectedItem().toString().trim();

        try {
            List<KhachHang> allCustomers = khDAO.getAllKhachHang(); // Lấy lại danh sách đầy đủ
            List<KhachHang> filteredList = allCustomers.stream()
                    .filter(kh -> (gt.equals("ALL") || (kh.getGioiTinh() != null && kh.getGioiTinh().equalsIgnoreCase(gt))))
                    .filter(kh -> (tt.equals("ALL") || (kh.getTrangThai() != null && kh.getTrangThai().equalsIgnoreCase(tt))))
                    .collect(Collectors.toList());
            fillToTable(filteredList);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lọc dữ liệu khách hàng: " + e.getMessage(), "Lỗi DB", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Phương thức hiển thị lịch sử giao dịch của khách hàng được chọn
    private void displayLichSuGiaoDich() {
        modelLichSuGiaoDich.setRowCount(0); // Xóa dữ liệu cũ
        int selectedRow = jTable2.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một khách hàng để xem lịch sử giao dịch.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String sdtKhachHang = (String) modelKhachHang.getValueAt(selectedRow, 3); // Lấy SDT từ bảng khách hàng
        if (sdtKhachHang == null || sdtKhachHang.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy số điện thoại của khách hàng được chọn.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            List<HoaDon> hoaDonList = hdDAO.getHoaDonsBySdt(sdtKhachHang);
            if (hoaDonList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Khách hàng này chưa có giao dịch nào.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Định dạng ngày tháng
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                for (HoaDon hd : hoaDonList) {
                    String ngayTaoStr = "";
                    if (hd.getNgayTao() != null) {
                        ngayTaoStr = sdf.format(hd.getNgayTao());
                    }

                    modelLichSuGiaoDich.addRow(new Object[]{
                        hd.getMahd(),
                        ngayTaoStr,
                        String.format("%,.0f", hd.getTongTien()), // Format tiền tệ
                        hd.getTrangThai()
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải lịch sử giao dịch: " + e.getMessage(), "Lỗi DB", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private int findRowBySdt(String sdt) {
        for (int i = 0; i < modelKhachHang.getRowCount(); i++) {
            if (sdt.equals(modelKhachHang.getValueAt(i, 3))) { // Cột 3 là SĐT, nếu cột SĐT của bạn ở vị trí khác, hãy đổi số 3
                return i;
            }
        }
        return -1;
    }

    private void performStatusCheckForSelectedCustomer() {
        int selectedRow = jTable2.getSelectedRow();
        if (selectedRow < 0) { // Nếu không có hàng nào được chọn
            return;
        }

        String sdt = (String) modelKhachHang.getValueAt(selectedRow, 3); // Lấy SĐT từ bảng
        if (sdt == null || sdt.isEmpty()) {
            return;
        }

        try {
            KhachHang kh = khDAO.getKhachHangBySdt(sdt); // Lấy thông tin khách hàng từ DB
            if (kh != null && kh.getTrangThai().equalsIgnoreCase("Khách mới")) {
                int soLuongHoaDon = khDAO.getSoLuongHoaDonBySdt(sdt); // Đếm số hóa đơn của KH
                if (soLuongHoaDon >= 5) {
                    boolean updated = khDAO.updateTrangThaiKhachHang(sdt, "Khách quen");
                    if (updated) {
                        JOptionPane.showMessageDialog(this, "Khách hàng " + kh.getTenkh() + " (" + sdt + ") đã trở thành Khách quen!", "Cập nhật trạng thái", JOptionPane.INFORMATION_MESSAGE);
                        loadKhachHangData(); // Tải lại bảng để hiển thị trạng thái mới
                        // Chọn lại hàng và cập nhật form để hiển thị trạng thái mới ngay lập tức
                        int newSelectedRow = findRowBySdt(sdt);
                        if (newSelectedRow != -1) {
                            jTable2.setRowSelectionInterval(newSelectedRow, newSelectedRow);
                            KhachHang updatedKh = khDAO.getKhachHangBySdt(sdt);
                            if (updatedKh != null) {
                                setForm(updatedKh); // Cập nhật form
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi kiểm tra và cập nhật trạng thái khách hàng: " + e.getMessage(), "Lỗi DB", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void showSuggestions() {
        String input = TF_MaKH1.getText().trim();
        if (input.isEmpty()) {
            hideSuggestions();
            return;
        }

        try {
            currentSuggestions = khDAO.searchKhachHangBySdtOrName(input);
            suggestionListModel.clear();

            if (currentSuggestions.isEmpty()) {
                hideSuggestions();
                return;
            }

            for (KhachHang kh : currentSuggestions) {
                suggestionListModel.addElement(kh.getSdt() + " - " + kh.getTenkh());
            }

            // Đặt kích thước popup bằng kích thước của JList
            int listWidth = Math.max(TF_MaKH1.getWidth(), suggestionList.getPreferredSize().width + 20); // +20 cho padding/scrollbar
            int listHeight = Math.min(suggestionList.getPreferredSize().height, 200); // Giới hạn chiều cao popup

            suggestionPopup.setPreferredSize(new Dimension(listWidth, listHeight));
            suggestionPopup.setPopupSize(new Dimension(listWidth, listHeight));

            suggestionPopup.show(TF_MaKH1, 0, TF_MaKH1.getHeight()); // Hiển thị dưới JTextField
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm kiếm gợi ý: " + e.getMessage());
            // Không hiển thị JOptionPane cho lỗi này để tránh làm phiền người dùng khi gõ
            hideSuggestions();
        }
    }

    private void hideSuggestions() {
        if (suggestionPopup.isVisible()) {
            suggestionPopup.setVisible(false);
        }
    }

    private void applySelectedSuggestion() {
        if (!suggestionList.isSelectionEmpty()) {
            int selectedIndex = suggestionList.getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < currentSuggestions.size()) {
                KhachHang selectedKh = currentSuggestions.get(selectedIndex);
                TF_MaKH1.setText(selectedKh.getSdt()); // Đặt SĐT vào trường
                // Tự động tải thông tin khách hàng lên form sau khi chọn gợi ý
                try {
                    KhachHang fullKh = khDAO.getKhachHangBySdt(selectedKh.getSdt());
                    if (fullKh != null) {
                        setForm(fullKh);
                        // Sau khi setForm, kiểm tra lại trạng thái khách hàng (nếu cần)
                        performStatusCheckForSelectedCustomer();
                    }
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi tải thông tin khách hàng từ gợi ý: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        }
        hideSuggestions(); // Luôn ẩn popup sau khi áp dụng
    }

    private String generateNewMaKH() {
        try {
            String lastMaKH = khDAO.getLastMaKH();

            if (lastMaKH != null && lastMaKH.startsWith("KH")) {
                // Tách phần số từ mã KH (xử lý cả KH1, KH01, KH001)
                String numberPart = lastMaKH.substring(2);

                // Chuyển phần số thành giá trị int và tăng lên 1
                int nextNumber = Integer.parseInt(numberPart) + 1;

                // Giữ nguyên format của mã cũ (KH1 -> KH2, KH01 -> KH02, KH001 -> KH002)
                if (numberPart.matches("0\\d+")) {
                    // Nếu có số 0 đứng đầu, giữ nguyên số chữ số
                    return String.format("KH%0" + numberPart.length() + "d", nextNumber);
                } else {
                    // Không có số 0 đứng đầu, chỉ nối số đơn giản
                    return "KH" + nextNumber;
                }
            }

            // Trường hợp không có KH nào trong database
            return "KH1"; // Hoặc "KH01"/"KH001" tuỳ theo mong muốn ban đầu

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi truy vấn database: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return "KH1";
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
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        TF_TenKH = new javax.swing.JTextField();
        TF_MaKH = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        TF_SDT = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        rdonam = new javax.swing.JRadioButton();
        rdonu = new javax.swing.JRadioButton();
        rdokhachquen = new javax.swing.JRadioButton();
        rdokhachmoi = new javax.swing.JRadioButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        TA_diachi = new javax.swing.JTextArea();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jButton15 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jButton17 = new javax.swing.JButton();
        jButton18 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jComboBox3 = new javax.swing.JComboBox<>();
        jComboBox4 = new javax.swing.JComboBox<>();
        jPanel6 = new javax.swing.JPanel();
        TF_MaKH1 = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        BT_timkh = new javax.swing.JButton();

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel1.setText("Quản lý khách hàng");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Quản lý khách hàng"));
        jPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel1MouseClicked(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel13.setText("Mã khách hàng :");

        TF_TenKH.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TF_TenKHActionPerformed(evt);
            }
        });

        TF_MaKH.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TF_MaKHActionPerformed(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel14.setText("Số điện thoại :");

        jLabel21.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel21.setText("Tên khách hàng :");

        TF_SDT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TF_SDTActionPerformed(evt);
            }
        });

        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel22.setText("Địa chỉ :");

        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel23.setText("Giới tính :");

        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel24.setText("Trạng thái :");

        buttonGroup1.add(rdonam);
        rdonam.setText("Nam");
        rdonam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdonamActionPerformed(evt);
            }
        });

        buttonGroup1.add(rdonu);
        rdonu.setText("Nữ");

        buttonGroup2.add(rdokhachquen);
        rdokhachquen.setText("Khách quen");

        buttonGroup2.add(rdokhachmoi);
        rdokhachmoi.setText("Khách mới");

        TA_diachi.setColumns(20);
        TA_diachi.setRows(5);
        jScrollPane2.setViewportView(TA_diachi);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(100, 100, 100)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel21)
                    .addComponent(jLabel13)
                    .addComponent(jLabel14))
                .addGap(74, 74, 74)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(TF_TenKH, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TF_SDT, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TF_MaKH, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel22)
                    .addComponent(jLabel24)
                    .addComponent(jLabel23))
                .addGap(98, 98, 98)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rdonam)
                            .addComponent(rdokhachquen))
                        .addGap(97, 97, 97)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rdonu)
                            .addComponent(rdokhachmoi)))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(130, 130, 130))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(TF_MaKH, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23)
                    .addComponent(rdonam)
                    .addComponent(rdonu))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(46, 46, 46)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel21)
                            .addComponent(TF_TenKH, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel24)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(rdokhachmoi)
                            .addComponent(rdokhachquen))
                        .addGap(14, 14, 14)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(46, 46, 46)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel14)
                                    .addComponent(TF_SDT, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(40, 40, 40)
                                .addComponent(jLabel22)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(37, 37, 37))))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

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
        jScrollPane3.setViewportView(jTable2);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 1055, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(47, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Xem thông tin", jPanel8);

        jPanel9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
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
        jTable3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable3MouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(jTable3);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 1055, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(47, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Lịch sử giao dịch", jPanel9);

        jButton15.setText("Thêm");
        jButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton15ActionPerformed(evt);
            }
        });

        jButton13.setText("Sửa");
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });

        jButton14.setText("Làm mới");
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });

        jButton17.setText("Khoá");
        jButton17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton17ActionPerformed(evt);
            }
        });

        jButton18.setText("Mở khoá");
        jButton18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton18ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton18, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jButton15, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton17, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jButton14, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton17, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton15, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 48, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton18, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addComponent(jButton14, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(54, 54, 54))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Lọc"));

        jLabel26.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel26.setText("Lọc theo trạng thái :");

        jLabel27.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel27.setText("Lọc theo giới tính :");

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ALL", "Nam ", "Nữ" }));

        jComboBox4.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ALL", "Khách quen", "Khách mới", " " }));

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel26)
                .addGap(18, 18, 18)
                .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 136, Short.MAX_VALUE)
                .addComponent(jLabel27)
                .addGap(26, 26, 26)
                .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(96, 96, 96))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel27)
                    .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel26))
                .addContainerGap(22, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Tìm kiếm"));

        TF_MaKH1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TF_MaKH1ActionPerformed(evt);
            }
        });

        jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel25.setText("Số điện thoại :");

        BT_timkh.setText("Tìm");
        BT_timkh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BT_timkhActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel25)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(TF_MaKH1, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(BT_timkh, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(71, 71, 71))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(TF_MaKH1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addComponent(BT_timkh)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(852, 852, 852))
            .addGroup(layout.createSequentialGroup()
                .addGap(53, 53, 53)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1148, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(70, 70, 70))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(76, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void TF_TenKHActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TF_TenKHActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_TenKHActionPerformed

    private void TF_MaKHActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TF_MaKHActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_MaKHActionPerformed

    private void TF_SDTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TF_SDTActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_SDTActionPerformed

    private void rdonamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdonamActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rdonamActionPerformed

    private void TF_MaKH1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TF_MaKH1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_MaKH1ActionPerformed

    private void BT_timkhActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BT_timkhActionPerformed
        // TODO add your handling code here:
        String sdt = TF_MaKH1.getText().trim();
        if (sdt.isEmpty()) {
            loadKhachHangData(); // Nếu ô tìm kiếm trống, hiển thị tất cả
            return;
        }
        try {
            List<KhachHang> list = khDAO.searchKhachHangBySDT(sdt);
            fillToTable(list);
            if (list.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy khách hàng nào với SĐT này.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tìm kiếm khách hàng: " + e.getMessage(), "Lỗi DB", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_BT_timkhActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        // TODO add your handling code here:                                      
        int selectedRow = jTable2.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần sửa.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

// Lấy thông tin hiện tại từ bảng
        String currentSdt = (String) modelKhachHang.getValueAt(selectedRow, 3);
        String currentStatus = (String) modelKhachHang.getValueAt(selectedRow, 4);
        String currentName = (String) modelKhachHang.getValueAt(selectedRow, 2); // Tên cũ

// Kiểm tra khách hàng có bị khóa không
        if ("Khóa".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this, "Không thể sửa thông tin khách hàng đang bị khóa!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

// Validate form
        if (!validateForm(false)) {
            return;
        }

// Lấy thông tin từ form
        String maKH = TF_MaKH.getText().trim();
        String newSdt = TF_SDT.getText().trim();
        String newName = TF_TenKH.getText().trim(); // Tên mới

// Kiểm tra SĐT mới có trùng không (nếu bị thay đổi)
        if (!newSdt.equals(currentSdt)) {
            try {
                if (khDAO.isSdtExist(newSdt)) {
                    JOptionPane.showMessageDialog(this, "Số điện thoại này đã thuộc về khách hàng khác!",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi kiểm tra số điện thoại!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

// ✅ KHÔNG CHO SỬA TÊN/SDT nếu đã có hóa đơn
        // ✅ KHÔNG CHO SỬA TÊN/SDT/Trạng thái nếu đã có hóa đơn
        try {
            if (khDAO.hasHoaDonBySDT(currentSdt)) {
                String newStatus = rdokhachquen.isSelected() ? "Khách quen" : "Khách mới"; // Trạng thái mới

                if (!newSdt.equals(currentSdt)
                        || !newName.equals(currentName)
                        || !newStatus.equals(currentStatus)) {
                    JOptionPane.showMessageDialog(this,
                            "Không thể sửa Tên, SĐT vì khách hàng đã phát sinh hóa đơn!",
                            "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi kiểm tra lịch sử hóa đơn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

// Tạo đối tượng khách hàng mới
        KhachHang kh = new KhachHang(
                maKH,
                newName,
                rdonam.isSelected() ? "Nam" : "Nữ",
                newSdt,
                rdokhachquen.isSelected() ? "Khách quen" : "Khách mới",
                TA_diachi.getText().trim()
        );

// Thực hiện update
        try {
            boolean success = khDAO.updateKhachHang(kh, currentSdt);
            if (success) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                loadKhachHangData();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại! Vui lòng thử lại.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + e.getMessage(),
                    "Lỗi nghiêm trọng", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }


    }//GEN-LAST:event_jButton13ActionPerformed

    private void jButton18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton18ActionPerformed
        // TODO add your handling code here:
        int selectedRow = jTable2.getSelectedRow();
    if (selectedRow < 0) {
        JOptionPane.showMessageDialog(this, 
            "Vui lòng chọn khách hàng cần mở khóa.", 
            "Thông báo", JOptionPane.WARNING_MESSAGE);
        return;
    }

    String sdtToUnlock = (String) modelKhachHang.getValueAt(selectedRow, 3);
    int confirm = JOptionPane.showConfirmDialog(this, 
        "Bạn có chắc chắn muốn mở khóa khách hàng có SĐT " + sdtToUnlock + " không?", 
        "Xác nhận mở khóa", JOptionPane.YES_NO_OPTION);

    if (confirm == JOptionPane.YES_OPTION) {
        try {
            if (khDAO.updateTrangThaiKhachHang(sdtToUnlock, "Khách quen")) {
                JOptionPane.showMessageDialog(this, "Mở khóa khách hàng thành công!");
                loadKhachHangData();
                
                // Cập nhật UI
                int newSelectedRow = findRowBySdt(sdtToUnlock);
                if (newSelectedRow != -1) {
                    jTable2.setRowSelectionInterval(newSelectedRow, newSelectedRow);
                    KhachHang khUnlocked = khDAO.getKhachHangBySdt(sdtToUnlock);
                    if (khUnlocked != null) {
                        setForm(khUnlocked);
                    }
                } else {
                    clearForm();
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Mở khóa khách hàng thất bại! Khách hàng không tồn tại hoặc trạng thái không thể thay đổi.", 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Lỗi mở khóa khách hàng: " + e.getMessage(), 
                "Lỗi DB", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    }//GEN-LAST:event_jButton18ActionPerformed

    private void jTable2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable2MouseClicked
        // TODO add your handling code here:
        int row = jTable2.getSelectedRow();
        if (row >= 0) {
            String sdt = (String) modelKhachHang.getValueAt(row, 3);
            try {
                KhachHang kh = khDAO.getKhachHangBySdt(sdt);
                if (kh != null) {
                    setForm(kh);
                    // --- Gọi phương thức kiểm tra trạng thái ở đây ---
                    lockRadioButtons();

                    performStatusCheckForSelectedCustomer(); // Kiểm tra và cập nhật trạng thái
                    // --- Kết thúc ---

                    if (jTabbedPane1.getSelectedIndex() == 1) {
                        displayLichSuGiaoDich();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin khách hàng chi tiết.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi tải thông tin khách hàng: " + e.getMessage(), "Lỗi DB", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_jTable2MouseClicked

    private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton15ActionPerformed
        // TODO add your handling code here:
        if (!validateForm(true)) { // isAddingNew = true
            return; // Nếu validate thất bại, dừng lại
        }

        try {
            // Tạo mã KH tự động
            String maKH = generateNewMaKH();
            TF_MaKH.setText(maKH); // Hiển thị mã KH lên form

            // Xử lý giới tính
            String gioitinh = "";
            if (rdonam.isSelected()) {
                gioitinh = "Nam";
            } else if (rdonu.isSelected()) {
                gioitinh = "Nữ";
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn giới tính!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Xử lý trạng thái (mặc định là Khách mới khi thêm)
            String trangthai = rdokhachquen.isSelected() ? "Khách quen" : "Khách mới";

            // Validate số điện thoại
            String sdt = TF_SDT.getText().trim();
            if (!sdt.matches("\\d{10,11}")) {
                JOptionPane.showMessageDialog(this, "Số điện thoại phải có 10-11 chữ số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                TF_SDT.requestFocus();
                return;
            }

            // Tạo đối tượng khách hàng
            // Tạo đối tượng khách hàng theo đúng thứ tự: MaKH, TenKH, GioiTinh, SDT, TrangThai, DiaChi
            KhachHang kh = new KhachHang(
                    maKH,
                    TF_TenKH.getText().trim(),
                    gioitinh,
                    sdt,
                    trangthai,
                    TA_diachi.getText().trim()
            );

            // Thực hiện thêm vào database
            khDAO.insert(kh);

            JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công!");
            loadKhachHangData();
            clearForm();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi database: " + e.getMessage(), "Lỗi nghiêm trọng", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();

        }
    }//GEN-LAST:event_jButton15ActionPerformed

    private void jButton17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton17ActionPerformed
        // TODO add your handling code here:
         int selectedRow = jTable2.getSelectedRow();
    if (selectedRow < 0) {
        JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần khóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
        return;
    }

    String sdtToLock = (String) modelKhachHang.getValueAt(selectedRow, 3);
    int confirm = JOptionPane.showConfirmDialog(this, 
        "Bạn có chắc chắn muốn khóa khách hàng có SĐT " + sdtToLock + " không?", 
        "Xác nhận khóa", JOptionPane.YES_NO_OPTION);

    if (confirm == JOptionPane.YES_OPTION) {
        try {
            if (khDAO.updateTrangThaiKhachHang(sdtToLock, "Khóa")) {
                JOptionPane.showMessageDialog(this, "Khóa khách hàng thành công!");
                loadKhachHangData();
                
                // Cập nhật UI
                int newSelectedRow = findRowBySdt(sdtToLock);
                if (newSelectedRow != -1) {
                    jTable2.setRowSelectionInterval(newSelectedRow, newSelectedRow);
                    KhachHang khLocked = khDAO.getKhachHangBySdt(sdtToLock);
                    if (khLocked != null) {
                        setForm(khLocked);
                    }
                } else {
                    clearForm();
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Khóa khách hàng thất bại! Khách hàng không tồn tại.", 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Lỗi khóa khách hàng: " + e.getMessage(), 
                "Lỗi DB", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    }//GEN-LAST:event_jButton17ActionPerformed

    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
        // TODO add your handling code here:
        clearForm();
        loadKhachHangData();
        // Sau khi làm mới, nếu đang ở tab lịch sử giao dịch thì cũng cần xóa dữ liệu cũ đi
        modelLichSuGiaoDich.setRowCount(0);
    }//GEN-LAST:event_jButton14ActionPerformed

    private void jPanel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jPanel1MouseClicked

    private void jTable3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable3MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jTable3MouseClicked
//
//    public static void main(String args[]) {
//        /* Set the Nimbus look and feel */
//        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
//        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
//     * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
//         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(QLKH.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(QLKH.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(QLKH.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(QLKH.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        //</editor-fold>
//
//        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                // Tạo một JFrame để chứa QLKH JPanel
//                JFrame frame = new JFrame("Quản lý Khách hàng - Test");
//                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//                frame.add(new QLKH()); // Thêm JPanel QLKH vào frame
//                frame.pack();
//                frame.setLocationRelativeTo(null);
//                frame.setVisible(true);
//            }
//        });
//    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BT_timkh;
    private javax.swing.JTextArea TA_diachi;
    private javax.swing.JTextField TF_MaKH;
    private javax.swing.JTextField TF_MaKH1;
    private javax.swing.JTextField TF_SDT;
    private javax.swing.JTextField TF_TenKH;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton18;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JComboBox<String> jComboBox4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JRadioButton rdokhachmoi;
    private javax.swing.JRadioButton rdokhachquen;
    private javax.swing.JRadioButton rdonam;
    private javax.swing.JRadioButton rdonu;
    // End of variables declaration//GEN-END:variables
}
