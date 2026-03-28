/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package View;

import javax.swing.table.DefaultTableModel;
import DAO.NhanVienDAO;
import Model.NhanVien;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.sql.Date;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

/**
 *
 * @author XPS
 */
public class QLNV extends javax.swing.JPanel {

    DefaultTableModel tableModel;
    NhanVienDAO nvDAO = new NhanVienDAO();
    int i = -1;
    private DefaultListModel<String> suggestionListModelNV;
    private JList<String> suggestionListNV;
    private JPopupMenu suggestionPopupNV;
    private List<NhanVien> danhSachNV; // danh sách toàn bộ nhân viên
    private List<NhanVien> currentSuggestionsNV;

    /**
     * Creates new form QLNV
     */
    public QLNV() {
        initComponents();
        initTable();
        initTableNghiViec();
        fillTable();
        loadNhanVienNghiViec();
        initComboBox();
        init();

        jComboBox1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                locTheoBoLoc();
            }
        });

        jComboBox2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                locTheoBoLoc();
            }
        });

        danhSachNV = nvDAO.getNhanVienByStatus(null);

        suggestionListModelNV = new DefaultListModel<>();
        suggestionListNV = new JList<>(suggestionListModelNV);
        suggestionListNV.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionListNV.setFocusable(false);

        suggestionPopupNV = new JPopupMenu();
        suggestionPopupNV.add(new JScrollPane(suggestionListNV));
        suggestionPopupNV.setFocusable(false);

        currentSuggestionsNV = new ArrayList<>();

// Gắn DocumentListener
        TF_Timsdtnv.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                showSuggestionsNV();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                showSuggestionsNV();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Không dùng với PlainDocument
            }
        });

        // KeyListener cho TF_SDT_NV
        TF_Timsdtnv.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (!suggestionListModelNV.isEmpty()) {
                        suggestionListNV.requestFocusInWindow();
                        suggestionListNV.setSelectedIndex(0);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (suggestionPopupNV.isVisible() && !suggestionListNV.isSelectionEmpty()) {
                        applySelectedSuggestionNV();
                        e.consume();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    suggestionPopupNV.setVisible(false);
                }
            }
        });

// KeyListener cho list
        suggestionListNV.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    applySelectedSuggestionNV();
                    suggestionPopupNV.setVisible(false);
                    TF_Timsdtnv.requestFocusInWindow();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    suggestionPopupNV.setVisible(false);
                    TF_Timsdtnv.requestFocusInWindow();
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    if (suggestionListNV.getSelectedIndex() == 0) {
                        TF_Timsdtnv.requestFocusInWindow();
                        suggestionListNV.clearSelection();
                        e.consume();
                    }
                }
            }
        });

// MouseListener
        suggestionListNV.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    applySelectedSuggestionNV();
                    suggestionPopupNV.setVisible(false);
                    TF_Timsdtnv.requestFocusInWindow();
                }
            }
        });

    }

    private void showSuggestionsNV() {
        String text = TF_Timsdtnv.getText().trim();
        suggestionListModelNV.clear();
        currentSuggestionsNV.clear();

        if (text.isEmpty()) {
            suggestionPopupNV.setVisible(false);
            return;
        }

        for (NhanVien nv : danhSachNV) {
            if (nv.getSdt().startsWith(text)) {
                String display = nv.getSdt() + " - " + nv.getTennv();
                suggestionListModelNV.addElement(display);
                currentSuggestionsNV.add(nv);
            }
        }

        if (!suggestionListModelNV.isEmpty()) {
            suggestionListNV.setSelectedIndex(0);
            suggestionPopupNV.show(TF_Timsdtnv, 0, TF_Timsdtnv.getHeight());
            TF_Timsdtnv.requestFocus();
        } else {
            suggestionPopupNV.setVisible(false);
        }
    }

    private void applySelectedSuggestionNV() {
        int index = suggestionListNV.getSelectedIndex();
        if (index >= 0 && index < currentSuggestionsNV.size()) {
            NhanVien selected = currentSuggestionsNV.get(index);
            TF_Timsdtnv.setText(selected.getSdt());
            // Bạn có thể tự động điền tên, mã, v.v. nếu muốn:
            // TF_TenNV.setText(selected.getTenNV());
            fillTableByNhanVien(selected);
        }
    }

    public void initTable() {
        tableModel = new DefaultTableModel();
        String[] cols = new String[]{"MÃ NV", "TÊN NV", "SDT", "VAI TRÒ", "EMAIL", "ĐỊA CHỈ", "GIỚI TÍNH", "TRẠNG THÁI", "NGÀY SINH"};
        tableModel.setColumnIdentifiers(cols);
        tblQLNV.setModel(tableModel);
    }

    public void fillTableByNhanVien(NhanVien nv) {
        tableModel.setRowCount(0); // Xóa bảng

        if (nv != null) {
            tableModel.addRow(new Object[]{
                nv.getManv(),
                nv.getTennv(),
                nv.getSdt(),
                nv.getVaitro(),
                nv.getEmail(),
                nv.getDiaChi(),
                nv.getGioitinh(),
                nv.getTrangThai(),
                nv.getNgaySinh()
            });
        }
    }

    public void fillTable() {
        tableModel.setRowCount(0);
        for (NhanVien nv : nvDAO.getNhanVienDangLam()) {  // Chỉ lấy nhân viên đang làm
            tableModel.addRow(new Object[]{
                nv.getManv(),
                nv.getTennv(),
                nv.getSdt(),
                nv.getVaitro(),
                nv.getEmail(),
                nv.getDiaChi(),
                nv.getGioitinh(),
                nv.getTrangThai(),
                nv.getNgaySinh()
            });
        }
    }

    public void initTableNghiViec() {
        DefaultTableModel model = new DefaultTableModel();
        String[] cols = new String[]{"MÃ NV", "TÊN NV", "SDT", "VAI TRÒ", "EMAIL", "ĐỊA CHỈ", "GIỚI TÍNH", "TRẠNG THÁI", "NGÀY SINH"};
        model.setColumnIdentifiers(cols);
        tblNghiViec.setModel(model);
    }

    private void initComboBox() {
        cboVaiTro.removeAllItems();
        cboVaiTro.addItem("-- Chọn vai trò --");
        cboVaiTro.addItem("Nhân viên");
        cboVaiTro.addItem("Quản lý");

        cboTrangThai.removeAllItems();
        cboTrangThai.addItem("-- Chọn trạng thái --");
        cboTrangThai.addItem("Đang làm");
        cboTrangThai.addItem("Tạm nghỉ");
        cboTrangThai.addItem("Nghỉ việc");
        cboTrangThai.addItem("Đã khóa");

        // Thêm ActionListener để ngăn chọn lại giá trị mặc định
        cboVaiTro.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cboVaiTro.getSelectedIndex() == -1 && cboVaiTro.getItemCount() > 0) {
                    cboVaiTro.setSelectedIndex(0);
                }
            }
        });

        cboTrangThai.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cboTrangThai.getSelectedIndex() == -1 && cboTrangThai.getItemCount() > 0) {
                    cboTrangThai.setSelectedIndex(0);
                }
            }
        });

        // Đặt mặc định về phần tử đầu tiên
        cboVaiTro.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);
    }

    public void init() {
        TF_MaNV.setEditable(false);
        tblNghiViec.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblNghiViecMouseClicked(evt);
            }
        });

        tblQLNV.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblQLNVMouseClicked(evt);
            }
        });

        NghiViec.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int index = NghiViec.getSelectedIndex();
                if (index == 0) { // Tab "Đang làm"
                    fillTable(); // Load nhân viên "Đang làm"
                } else if (index == 1) { // Tab "Nghỉ việc/Tạm nghỉ"
                    loadNhanVienNghiViec(); // Load cả "Tạm nghỉ" và "Nghỉ việc"
                }
            }
        });

        tblNghiViec.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                showDetails(tblNghiViec); // Truyền tblNghiViec thay vì jTable1
            }
        });

        tblQLNV.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                showDetails(tblQLNV); // Truyền tblQLNV thay vì jTable1
            }
        });

    } // <-- Đóng ngoặc ở đây!!!

    public void lamMoi() {
        TF_MaNV.setText("");
        TF_TenNV.setText("");
        TF_SDTNV.setText("");
        TF_EmailNV.setText("");
        TF_DiaChiNV.setText("");
        TF_Timsdtnv.setText("");
        jComboBox1.setSelectedIndex(0);
        jComboBox2.setSelectedIndex(0);
        TF_NgaySinhNV.setText("");
        buttonGroup1.clearSelection();

        // Reset combo box về "-- Chọn..."
        cboVaiTro.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);

        tblQLNV.clearSelection();
        i = -1;
    }

    public void showDetails(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            // Xác định model của bảng đang được chọn
            DefaultTableModel model = (DefaultTableModel) table.getModel();

            // Lấy mã NV từ cột đầu tiên của dòng được chọn
            String maNV = model.getValueAt(selectedRow, 0).toString();
            NhanVien nv = nvDAO.getNhanVienByMa(maNV);

            if (nv != null) {
                // Hiển thị thông tin cơ bản
                TF_MaNV.setText(nv.getManv());
                TF_TenNV.setText(nv.getTennv());
                TF_SDTNV.setText(nv.getSdt());
                TF_EmailNV.setText(nv.getEmail());
                TF_DiaChiNV.setText(nv.getDiaChi());
                TF_NgaySinhNV.setText(nv.getNgaySinh() != null ? nv.getNgaySinh().toString() : "");

                // Xử lý giới tính
                buttonGroup1.clearSelection();
                if ("Nam".equalsIgnoreCase(nv.getGioitinh())) {
                    rdoNam.setSelected(true);
                } else if ("Nữ".equalsIgnoreCase(nv.getGioitinh())) {
                    rdoNu.setSelected(true);
                }

                // Xử lý combo box vai trò
                for (int i = 0; i < cboVaiTro.getItemCount(); i++) {
                    if (cboVaiTro.getItemAt(i).toString().equalsIgnoreCase(nv.getVaitro())) {
                        cboVaiTro.setSelectedIndex(i);
                        break;
                    }
                }

                // Xử lý combo box trạng thái
                for (int i = 0; i < cboTrangThai.getItemCount(); i++) {
                    if (cboTrangThai.getItemAt(i).toString().equalsIgnoreCase(nv.getTrangThai())) {
                        cboTrangThai.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }

    // validateForm theo thứ tự: tên -> sdt -> giới tính -> vai trò -> trạng thái -> ngày sinh -> địa chỉ -> email
    private boolean validateForm() {
        // Tạo mã nếu chưa có
        if (TF_MaNV.getText().trim().isEmpty()) {
            TF_MaNV.setText(nvDAO.taoMaNVMoi());
        }

        String tenNV = TF_TenNV.getText().trim();
        String sdt = TF_SDTNV.getText().trim();
        String email = TF_EmailNV.getText().trim();
        String diaChi = TF_DiaChiNV.getText().trim();
        String ngaySinhStr = TF_NgaySinhNV.getText().trim();
        int selectedRow = tblQLNV.getSelectedRow();

        // 1. Tên
        if (tenNV.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên nhân viên.");
            TF_TenNV.requestFocus();
            return false;
        }
        if (!tenNV.matches("^[\\p{L} ]+$")) {
            JOptionPane.showMessageDialog(this, "Tên nhân viên chỉ được chứa chữ cái và khoảng trắng.");
            TF_TenNV.requestFocus();
            return false;
        }
        for (String word : tenNV.split("\\s+")) {
            if (!word.isEmpty() && !Character.isUpperCase(word.charAt(0))) {
                JOptionPane.showMessageDialog(this, "Vui lòng viết hoa chữ cái đầu mỗi từ trong tên nhân viên.");
                TF_TenNV.requestFocus();
                return false;
            }
        }

        // 2. Số điện thoại
        if (sdt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số điện thoại.");
            TF_SDTNV.requestFocus();
            return false;
        }
        if (!sdt.matches("^0\\d{9}$")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ. Định dạng: 0xxxxxxxxx (10 số).");
            TF_SDTNV.requestFocus();
            return false;
        }
        // Kiểm tra trùng SĐT trong bảng (bỏ qua selectedRow)
        for (int i = 0; i < tblQLNV.getRowCount(); i++) {
            if (i == selectedRow) {
                continue;
            }
            Object val = tblQLNV.getValueAt(i, 2); // cột SĐT (theo code trước là index 2)
            if (val != null && sdt.equals(val.toString().trim())) {
                JOptionPane.showMessageDialog(this, "Số điện thoại đã tồn tại.");
                TF_SDTNV.requestFocus();
                return false;
            }
        }

        // 3. Giới tính
        if (!rdoNam.isSelected() && !rdoNu.isSelected()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn giới tính.");
            return false;
        }

        // 4. Vai trò
        String vaiTro = cboVaiTro.getSelectedItem() != null ? cboVaiTro.getSelectedItem().toString() : "";
        if (vaiTro.equals("-- Chọn vai trò --") || cboVaiTro.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn vai trò hợp lệ.");
            cboVaiTro.requestFocus();
            return false;
        }

        // 5. Trạng thái
        String trangThai = cboTrangThai.getSelectedItem() != null ? cboTrangThai.getSelectedItem().toString() : "";
        if (trangThai.equals("-- Chọn trạng thái --") || cboTrangThai.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn trạng thái hợp lệ.");
            cboTrangThai.requestFocus();
            return false;
        }

        // 6. Ngày sinh (và tuổi 18-50) — dùng hàm isTuoiHopLe
        if (ngaySinhStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập ngày sinh.");
            TF_NgaySinhNV.requestFocus();
            return false;
        }
        if (!isTuoiHopLe(ngaySinhStr)) { // nếu bạn vẫn dùng tên cũ isDu18Tuoi thì đổi gọi tương ứng
            TF_NgaySinhNV.requestFocus();
            return false;
        }

        // 7. Địa chỉ
        if (diaChi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập địa chỉ.");
            TF_DiaChiNV.requestFocus();
            return false;
        }

        // 8. Email
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập email.");
            TF_EmailNV.requestFocus();
            return false;
        }
        // Nếu bạn muốn bắt buộc @gmail.com, dùng regex bên dưới; nếu muốn cho mọi domain, thay pattern tương ứng.
        if (!email.matches("^[a-zA-Z0-9._%+-]+@gmail\\.com$")) {
            JOptionPane.showMessageDialog(this, "Email không hợp lệ. Phải kết thúc bằng @gmail.com");
            TF_EmailNV.requestFocus();
            return false;
        }
        // Kiểm tra trùng Email trong bảng
        for (int i = 0; i < tblQLNV.getRowCount(); i++) {
            if (i == selectedRow) {
                continue;
            }
            Object val = tblQLNV.getValueAt(i, 4); // cột Email (theo code trước là index 4)
            if (val != null && email.equalsIgnoreCase(val.toString().trim())) {
                JOptionPane.showMessageDialog(this, "Email đã tồn tại.");
                TF_EmailNV.requestFocus();
                return false;
            }
        }

        return true;
    }

    public void addNV() {
        if (!validateForm()) {
            return;
        }

        String manv = TF_MaNV.getText();
        String tenv = TF_TenNV.getText();
        String sdt = TF_SDTNV.getText();
        String email = TF_EmailNV.getText();
        String diaChi = TF_DiaChiNV.getText();
        String gioitinh = rdoNam.isSelected() ? "Nam" : "Nữ";
        String vaitro = cboVaiTro.getSelectedItem().toString();

        // Mặc định trạng thái = "Đang làm" khi thêm
        String trangthai = "Đang làm";
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate = LocalDate.parse(TF_NgaySinhNV.getText().trim(), formatter);
            Date ngaySinh = Date.valueOf(localDate);

            NhanVien nv = new NhanVien(manv, tenv, sdt, vaitro, email, diaChi, gioitinh, trangthai, ngaySinh);
            int result = nvDAO.addNV(nv);
            if (result == 1) {
                fillTable();
                JOptionPane.showMessageDialog(this, "Thêm NV thành công!");
                lamMoi();
            } else {
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra!");
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Ngày sinh không đúng định dạng yyyy-MM-dd");
        }
    }

    public void deleteNV() {
    int tabIndex = NghiViec.getSelectedIndex(); // 0: đang làm, 1: nghỉ việc
    JTable currentTable = (tabIndex == 0) ? tblQLNV : tblNghiViec;
    int selectedRow = currentTable.getSelectedRow();

    if (selectedRow >= 0) {
        String maNV = currentTable.getValueAt(selectedRow, 0).toString();
        String trangThai = currentTable.getValueAt(selectedRow, 5).toString(); // Thay [cột chứa trạng thái] bằng chỉ số cột thực tế
        
        // Kiểm tra nếu nhân viên đã nghỉ việc
        if (trangThai.equalsIgnoreCase("Nghỉ việc")) {
            JOptionPane.showMessageDialog(this, "Nhân viên này đã nghỉ việc rồi!");
            return;
        }

        String vaiTro = currentTable.getValueAt(selectedRow, 3).toString();
        
        if (!vaiTro.equalsIgnoreCase("Nhân viên")) {
            JOptionPane.showMessageDialog(this, "Chỉ có thể chuyển trạng thái Nhân viên sang nghỉ việc.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc chắn muốn chuyển nhân viên này sang trạng thái nghỉ việc?", 
            "Xác nhận", JOptionPane.YES_NO_OPTION);
            
        if (choice == JOptionPane.YES_OPTION) {
            int result = nvDAO.deleteNV(maNV);
            if (result == 1) {
                fillTable();
                loadNhanVienNghiViec();
                JOptionPane.showMessageDialog(this, "Chuyển trạng thái nhân viên thành công!");
            } else {
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi cập nhật trạng thái.");
            }
        }
    } else {
        JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần thay đổi trạng thái.");
    }
}
    public void edit() {
         int tabIndex = NghiViec.getSelectedIndex();
    JTable currentTable = (tabIndex == 0) ? tblQLNV : tblNghiViec;
    int selectedRow = currentTable.getSelectedRow();
    
    if (selectedRow < 0) {
        JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần sửa trước!");
        return; // chưa chọn thì thoát luôn
    }
    
    // Đã chọn rồi mới validate form
    if (!validateForm()) {
        return; 
    }
        
        if (selectedRow >= 0) {
            int choice = JOptionPane.showConfirmDialog(this, "Bạn có thực sự muốn sửa?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                String maCu = currentTable.getValueAt(selectedRow, 0).toString();

                String manv = TF_MaNV.getText().trim();     // Nên để không sửa mã
                String tenv = TF_TenNV.getText().trim();
                String sdt = TF_SDTNV.getText().trim();
                String email = TF_EmailNV.getText().trim();
                String diaChi = TF_DiaChiNV.getText().trim();
                String gioitinh = rdoNam.isSelected() ? "Nam" : "Nữ";
                String vaitro = cboVaiTro.getSelectedItem().toString();
                String trangThai = cboTrangThai.getSelectedItem().toString();

                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate localDate = LocalDate.parse(TF_NgaySinhNV.getText().trim(), formatter);
                    Date ngaySinh = Date.valueOf(localDate);

                    NhanVien nvMoi = new NhanVien(manv, tenv, sdt, vaitro, email, diaChi, gioitinh, trangThai, ngaySinh);

                    int result = nvDAO.editNV(nvMoi, maCu);
                    if (result == 1) {
                        fillTable();
                        loadNhanVienNghiViec(); // Cập nhật lại bảng nếu cần
                        JOptionPane.showMessageDialog(this, "Sửa nhân viên/quản lý thành công!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi sửa!");
                    }
                } catch (DateTimeParseException e) {
                    JOptionPane.showMessageDialog(this, "Ngày sinh không đúng định dạng yyyy-MM-dd");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên/quản lý để sửa!");
        }
    }

    public void timNVTheoSDT() {
        String sdtNV = TF_Timsdtnv.getText().trim(); // Ô nhập mã trong phần Tìm kiếm

        if (sdtNV.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số điện thoại cần tìm !");
            return;
        }

        NhanVien nv = nvDAO.timNVTheoSDT(sdtNV);
        tableModel.setRowCount(0); // Xoá bảng hiện tại để hiển thị kết quả mới

        if (nv != null) {
            Object[] row = new Object[]{
                nv.getManv(), nv.getTennv(), nv.getSdt(),
                nv.getVaitro(), nv.getEmail(), nv.getDiaChi(),
                nv.getGioitinh(), nv.getTrangThai(), nv.getNgaySinh()
            };
            tableModel.addRow(row);

            // Làm mới form trước khi hiển thị thông tin mới
            lamMoi();

            // Hiển thị thông tin nhân viên tìm được lên form
            TF_MaNV.setText(nv.getManv());
            TF_TenNV.setText(nv.getTennv());
            TF_SDTNV.setText(nv.getSdt());
            TF_EmailNV.setText(nv.getEmail());
            TF_DiaChiNV.setText(nv.getDiaChi());
            TF_NgaySinhNV.setText(String.valueOf(nv.getNgaySinh()));

            if (nv.getGioitinh().equalsIgnoreCase("Nam")) {
                rdoNam.setSelected(true);
            } else {
                rdoNu.setSelected(true);
            }

            // Cập nhật vai trò
            for (int j = 0; j < cboVaiTro.getItemCount(); j++) {
                if (cboVaiTro.getItemAt(j).toString().trim().equalsIgnoreCase(nv.getVaitro().trim())) {
                    cboVaiTro.setSelectedIndex(j);
                    break;
                }
            }

            // Cập nhật trạng thái
            for (int j = 0; j < cboTrangThai.getItemCount(); j++) {
                if (cboTrangThai.getItemAt(j).toString().trim().equalsIgnoreCase(nv.getTrangThai().trim())) {
                    cboTrangThai.setSelectedIndex(j);
                    break;
                }
            }

            tblQLNV.setRowSelectionInterval(0, 0); // chọn dòng đầu tiên
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm thấy nhân viên với số điện thoại này!");
            lamMoi(); // Làm mới form nếu không tìm thấy
        }
    }

    public void khoaNhanVien() {
        int selectedTab = NghiViec.getSelectedIndex();
        JTable currentTable;

        // Xác định bảng đang được chọn
        if (selectedTab == 0) {
            currentTable = tblQLNV;
        } else if (selectedTab == 1) {
            currentTable = tblNghiViec;
        } else {
            JOptionPane.showMessageDialog(this, "Không xác định được tab hiện tại.");
            return;
        }

        int row = currentTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần khóa.");
            return;
        }

        String maNV = currentTable.getValueAt(row, 0).toString();
        String vaiTro = currentTable.getValueAt(row, 3).toString();     // cột 3: Vai trò
        String trangThai = currentTable.getValueAt(row, 7).toString();  // cột 7: Trạng thái

        // Kiểm tra vai trò
        if (!vaiTro.equalsIgnoreCase("Nhân viên")) {
            JOptionPane.showMessageDialog(this, "Chỉ có thể khóa Nhân viên");
            return;
        }

        // Kiểm tra trạng thái
        if (!trangThai.equalsIgnoreCase("Tạm nghỉ") && !trangThai.equalsIgnoreCase("Nghỉ việc")) {
            JOptionPane.showMessageDialog(this, "Chỉ có thể khóa nhân viên có trạng thái 'Tạm nghỉ' hoặc 'Nghỉ việc'.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn KHÓA nhân viên này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            int result = nvDAO.updateTrangThai(maNV, "Đã khóa");
            if (result == 1) {
                JOptionPane.showMessageDialog(this, "Đã khóa nhân viên thành công!");

                // Load lại bảng tương ứng
                if (selectedTab == 0) {
                    fillTable(); // nếu là tab Đang làm
                } else {
                    loadNhanVienNghiViec(); // nếu là tab Nghỉ việc
                }
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật trạng thái.");
            }
        }
    }

    public void moKhoaNhanVien() {
        int selectedTab = NghiViec.getSelectedIndex();
        JTable currentTable;

        // Xác định bảng hiện tại
        if (selectedTab == 0) {
            currentTable = tblQLNV;
        } else if (selectedTab == 1) {
            currentTable = tblNghiViec;
        } else {
            JOptionPane.showMessageDialog(this, "Không xác định được tab hiện tại.");
            return;
        }

        int row = currentTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần mở khóa.");
            return;
        }

        String maNV = currentTable.getValueAt(row, 0).toString();
        String trangThaiHienTai = currentTable.getValueAt(row, 7).toString().trim();

        if (!trangThaiHienTai.equalsIgnoreCase("Đã khóa")) {
            JOptionPane.showMessageDialog(this, "Nhân viên chưa bị khóa!");
            return;
        }

        // Các trạng thái hợp lệ sau khi mở khóa
        String[] trangThaiHopLe = {"Đang làm", "Tạm nghỉ", "Nghỉ việc"};
        JComboBox<String> cboTrangThai = new JComboBox<>(trangThaiHopLe);

        int chon = JOptionPane.showConfirmDialog(this, cboTrangThai,
                "Chọn trạng thái mới cho nhân viên:", JOptionPane.OK_CANCEL_OPTION);

        if (chon != JOptionPane.OK_OPTION) {
            return;
        }

        String trangThaiMoi = cboTrangThai.getSelectedItem().toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn MỞ KHÓA nhân viên này với trạng thái '" + trangThaiMoi + "'?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int result = nvDAO.updateTrangThai(maNV, trangThaiMoi);
            if (result == 1) {
                // Nếu đang ở tab 0, mở khóa các trường nhập liệu
                if (selectedTab == 0) {
                    TF_MaNV.setEnabled(true);
                    TF_TenNV.setEnabled(true);
                    TF_SDTNV.setEnabled(true);
                    TF_EmailNV.setEnabled(true);
                    TF_DiaChiNV.setEnabled(true);
                    TF_NgaySinhNV.setEnabled(true);
                    rdoNam.setEnabled(true);
                    rdoNu.setEnabled(true);
                    cboVaiTro.setEnabled(true);
                    cboTrangThai.setEnabled(true);
                }

                JOptionPane.showMessageDialog(this, "Đã mở khóa nhân viên thành công!");

                // Load lại bảng tương ứng
                if (selectedTab == 0) {
                    fillTable();
                } else {
                    loadNhanVienNghiViec();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật trạng thái.");
            }
        }
    }

    public void loadNhanVienNghiViec() {
        DefaultTableModel model = (DefaultTableModel) tblNghiViec.getModel();
        model.setRowCount(0);

        // Lấy tất cả nhân viên có trạng thái cần hiển thị
        List<NhanVien> listTamNghi = nvDAO.getNhanVienTamNghi();
        List<NhanVien> listNghiViec = nvDAO.getNhanVienNghiViec();
        List<NhanVien> listDaKhoa = nvDAO.getNhanVienDaKhoa(); // ← THÊM DÒNG NÀY

        for (NhanVien nv : listTamNghi) {
            model.addRow(nvDAO.getRow(nv));
        }
        for (NhanVien nv : listNghiViec) {
            model.addRow(nvDAO.getRow(nv));
        }
        for (NhanVien nv : listDaKhoa) { // ← THÊM DÒNG NÀY
            model.addRow(nvDAO.getRow(nv));
        }
    }

    public void locTheoGioiTinh(String gioiTinh) {
        tableModel.setRowCount(0); // Xóa dữ liệu bảng cũ

        List<NhanVien> ds = nvDAO.getAllNV(); // Lấy danh sách nhân viên

        for (NhanVien nv : ds) {
            String gt = nv.getGioitinh(); // Kiểu String, ví dụ "Nam" hoặc "Nữ"
            if (gt != null && gt.equalsIgnoreCase(gioiTinh)) {
                tableModel.addRow(new Object[]{
                    nv.getManv(),
                    nv.getTennv(),
                    nv.getSdt(),
                    nv.getVaitro(),
                    nv.getEmail(),
                    nv.getDiaChi(),
                    nv.getGioitinh(),
                    nv.getTrangThai(),
                    nv.getNgaySinh()
                }
                );
            }
        }
    }

    public void locTheoVaiTro(String vaiTro) {
        if (vaiTro.equals("-- Tất cả --")) {
            fillTable(); // Hiển thị tất cả
            return;
        }

        tableModel.setRowCount(0); // Xoá dữ liệu cũ

        List<NhanVien> ds = nvDAO.getAllNV(); // Lấy danh sách nhân viên
        for (NhanVien nv : ds) {
            if (nv.getVaitro() != null && nv.getVaitro().equalsIgnoreCase(vaiTro)) {
                tableModel.addRow(new Object[]{
                    nv.getManv(),
                    nv.getTennv(),
                    nv.getSdt(),
                    nv.getVaitro(),
                    nv.getEmail(),
                    nv.getDiaChi(),
                    nv.getGioitinh(),
                    nv.getTrangThai(),
                    nv.getNgaySinh()
                }
                );
            }
        }
    }

    public void locTheoGioiTinhVaVaiTro(String gioiTinh, String vaiTro) {
        System.out.println("Lọc theo: " + gioiTinh + " - " + vaiTro); // debug

        tableModel.setRowCount(0); // Xóa bảng cũ

        List<NhanVien> ds = nvDAO.getAllNV(); // Lấy danh sách NV
        for (NhanVien nv : ds) {
            String gt = nv.getGioitinh() != null ? nv.getGioitinh().trim() : "";
            String vt = nv.getVaitro() != null ? nv.getVaitro().trim() : "";

            boolean matchGioiTinh = gioiTinh.isEmpty() || gt.equalsIgnoreCase(gioiTinh.trim());
            boolean matchVaiTro = vaiTro.isEmpty() || vt.equalsIgnoreCase(vaiTro.trim());

            if (matchGioiTinh && matchVaiTro) {
                tableModel.addRow(new Object[]{
                    nv.getManv(),
                    nv.getTennv(),
                    nv.getSdt(),
                    nv.getVaitro(),
                    nv.getEmail(),
                    nv.getDiaChi(),
                    nv.getGioitinh(),
                    nv.getTrangThai(),
                    nv.getNgaySinh()
                });
            }
        }
    }

    private void locTheoBoLoc() {
        String gioiTinh = jComboBox1.getSelectedItem().toString().trim();
        String vaiTro = jComboBox2.getSelectedItem().toString().trim();

        boolean chonGioiTinh = !gioiTinh.equals("-- Tất cả --");
        boolean chonVaiTro = !vaiTro.equals("-- Chọn vai trò --");

        if (chonGioiTinh && chonVaiTro) {
            locTheoGioiTinhVaVaiTro(gioiTinh, vaiTro);
        } else if (chonGioiTinh) {
            locTheoGioiTinh(gioiTinh);
        } else if (chonVaiTro) {
            locTheoVaiTro(vaiTro);
        } else {
            fillTable(); // hiển thị tất cả
        }
    }

    public boolean isTuoiHopLe(String strNgaySinh) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate ngaySinh = LocalDate.parse(strNgaySinh, formatter);
            LocalDate homNay = LocalDate.now();
            Period tuoi = Period.between(ngaySinh, homNay);

            // Kiểm tra ngày sinh không lớn hơn hôm nay
            if (ngaySinh.isAfter(homNay)) {
                JOptionPane.showMessageDialog(this, "Ngày sinh không được lớn hơn hôm nay.");
                return false;
            }

            // Kiểm tra tuổi từ 18 đến 50
            if (tuoi.getYears() < 18 || tuoi.getYears() > 50) {
                JOptionPane.showMessageDialog(this, "Tuổi nhân viên phải từ 18 đến 50.");
                return false;
            }

        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Ngày sinh không đúng định dạng yyyy-MM-dd.");
            return false;
        }
        return true;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        TF_TenNV = new javax.swing.JTextField();
        TF_SDTNV = new javax.swing.JTextField();
        TF_MaNV = new javax.swing.JTextField();
        TF_NgaySinhNV = new javax.swing.JTextField();
        rdoNam = new javax.swing.JRadioButton();
        rdoNu = new javax.swing.JRadioButton();
        jLabel9 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        TF_Timsdtnv = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        TF_EmailNV = new javax.swing.JTextField();
        TF_DiaChiNV = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        cboTrangThai = new javax.swing.JComboBox<>();
        cboVaiTro = new javax.swing.JComboBox<>();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        NghiViec = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblQLNV = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblNghiViec = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        btnKhoa = new javax.swing.JButton();
        btnXoa = new javax.swing.JButton();
        btnSua = new javax.swing.JButton();
        btnMoKhoa = new javax.swing.JButton();
        btnThem = new javax.swing.JButton();
        btnLamMoi = new javax.swing.JButton();
        jComboBox1 = new javax.swing.JComboBox<>();
        jComboBox2 = new javax.swing.JComboBox<>();

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

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel1.setText("Quản lý nhân viên");

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Quản lý nhân viên"));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel3.setText("Mã nhân viên :");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel4.setText("Tên nhân viên :");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel5.setText("Số điện thoại :");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel6.setText("Giới tính :");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel7.setText("Trạng thái :");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel8.setText("Ngày sinh :");

        TF_TenNV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TF_TenNVActionPerformed(evt);
            }
        });

        TF_SDTNV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TF_SDTNVActionPerformed(evt);
            }
        });

        TF_MaNV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TF_MaNVActionPerformed(evt);
            }
        });

        TF_NgaySinhNV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TF_NgaySinhNVActionPerformed(evt);
            }
        });

        buttonGroup1.add(rdoNam);
        rdoNam.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        rdoNam.setText("Nam");
        rdoNam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdoNamActionPerformed(evt);
            }
        });

        buttonGroup1.add(rdoNu);
        rdoNu.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        rdoNu.setText("Nữ");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel9.setText("Vai trò :");

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Tìm kiếm"));

        TF_Timsdtnv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TF_TimsdtnvActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel10.setText("Số điện thoại :");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(TF_Timsdtnv, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(180, 180, 180))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_Timsdtnv, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addContainerGap(37, Short.MAX_VALUE))
        );

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel11.setText("Địa chỉ :");

        TF_EmailNV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TF_EmailNVActionPerformed(evt);
            }
        });

        TF_DiaChiNV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TF_DiaChiNVActionPerformed(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel12.setText("Email :");

        cboTrangThai.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--Chọn trạng thái--", "Đang làm", "Tạm nghỉ", "Nghỉ việc" }));

        cboVaiTro.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--Chọn vai trò--", "Nhân viên", "Quản lý" }));
        cboVaiTro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboVaiTroActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3)
                    .addComponent(jLabel6)
                    .addComponent(jLabel9))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(rdoNam)
                        .addGap(41, 41, 41)
                        .addComponent(rdoNu, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(TF_SDTNV, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(TF_TenNV, javax.swing.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
                        .addComponent(TF_MaNV, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(cboVaiTro, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(124, 124, 124)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(jLabel7))
                        .addGap(64, 64, 64)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(TF_DiaChiNV, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TF_NgaySinhNV, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TF_EmailNV, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cboTrangThai, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(58, 58, 58)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 348, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel11)
                            .addComponent(jLabel12))))
                .addContainerGap(36, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(135, 135, 135)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(TF_SDTNV, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(17, 17, 17)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel3)
                                    .addComponent(TF_MaNV, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel7)
                                    .addComponent(cboTrangThai, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(30, 30, 30)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel4)
                                    .addComponent(TF_TenNV, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel8)
                                    .addComponent(TF_NgaySinhNV, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(TF_DiaChiNV, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11))))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(rdoNam)
                            .addComponent(rdoNu)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(TF_EmailNV, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboVaiTro, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addContainerGap(43, Short.MAX_VALUE))
        );

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel13.setText("Lọc theo vai trò :");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel14.setText("Lọc theo giới tính :");

        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        tblQLNV.setModel(new javax.swing.table.DefaultTableModel(
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
        tblQLNV.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblQLNVMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tblQLNV);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 1099, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(42, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(28, Short.MAX_VALUE))
        );

        NghiViec.addTab("Xem thông tin", jPanel4);

        jPanel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        tblNghiViec.setModel(new javax.swing.table.DefaultTableModel(
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
        tblNghiViec.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblNghiViecMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(tblNghiViec);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 1099, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(42, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(28, Short.MAX_VALUE))
        );

        NghiViec.addTab("Nghỉ việc", jPanel5);

        btnKhoa.setText("Khoá");
        btnKhoa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKhoaActionPerformed(evt);
            }
        });

        btnXoa.setIcon(new javax.swing.ImageIcon("D:\\MOB1014\\icons\\delete.png")); // NOI18N
        btnXoa.setText("Nghỉ việc");
        btnXoa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnXoaActionPerformed(evt);
            }
        });

        btnSua.setIcon(new javax.swing.ImageIcon("D:\\MOB1014\\icons\\edit.png")); // NOI18N
        btnSua.setText("Sửa");
        btnSua.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSuaActionPerformed(evt);
            }
        });

        btnMoKhoa.setText("Mở khoá");
        btnMoKhoa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoKhoaActionPerformed(evt);
            }
        });

        btnThem.setIcon(new javax.swing.ImageIcon("D:\\MOB1014\\icons\\add.png")); // NOI18N
        btnThem.setText("Thêm");
        btnThem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThemActionPerformed(evt);
            }
        });

        btnLamMoi.setText("Làm mới");
        btnLamMoi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLamMoiActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(btnThem, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(btnKhoa, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(btnSua, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnXoa, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnMoKhoa, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnLamMoi, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnKhoa, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnThem, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(67, 67, 67)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnXoa, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnMoKhoa, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 68, Short.MAX_VALUE)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSua, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLamMoi, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(40, 40, 40))
        );

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-- Tất cả --", "Nam ", "Nữ" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-- Tất cả --", "Nhân viên", "Quản lý" }));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(149, 149, 149)
                                .addComponent(jLabel14)
                                .addGap(18, 18, 18)
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(92, 92, 92)
                                .addComponent(jLabel13)
                                .addGap(18, 18, 18)
                                .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(48, 48, 48)
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(NghiViec, javax.swing.GroupLayout.PREFERRED_SIZE, 1157, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(615, 615, 615))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(43, 43, 43)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(NghiViec, javax.swing.GroupLayout.PREFERRED_SIZE, 354, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 35, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void TF_TenNVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TF_TenNVActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_TenNVActionPerformed

    private void TF_SDTNVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TF_SDTNVActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_SDTNVActionPerformed

    private void TF_MaNVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TF_MaNVActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_MaNVActionPerformed

    private void TF_NgaySinhNVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TF_NgaySinhNVActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_NgaySinhNVActionPerformed

    private void rdoNamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdoNamActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rdoNamActionPerformed

    private void btnSuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSuaActionPerformed
        // TODO add your handling code here:
        edit();

    }//GEN-LAST:event_btnSuaActionPerformed

    private void TF_TimsdtnvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TF_TimsdtnvActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_TimsdtnvActionPerformed

    private void TF_EmailNVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TF_EmailNVActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_EmailNVActionPerformed

    private void TF_DiaChiNVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TF_DiaChiNVActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_DiaChiNVActionPerformed

    private void btnKhoaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKhoaActionPerformed
        // TODO add your handling code here:

        khoaNhanVien();

    }//GEN-LAST:event_btnKhoaActionPerformed

    private void cboVaiTroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboVaiTroActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cboVaiTroActionPerformed

    private void btnMoKhoaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoKhoaActionPerformed
        // TODO add your handling code here:

        moKhoaNhanVien();

    }//GEN-LAST:event_btnMoKhoaActionPerformed

    private void btnThemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThemActionPerformed
        // TODO add your handling code here:
        // đảm bảo combo có item "Đang làm" (tránh lỗi nếu item bị xóa ở đâu đó)
        boolean found = false;
        for (int i = 0; i < cboTrangThai.getItemCount(); i++) {
            Object it = cboTrangThai.getItemAt(i);
            if ("Đang làm".equals(it != null ? it.toString() : null)) {
                found = true;
                break;
            }
        }
        if (!found) {
            cboTrangThai.addItem("Đang làm");
        }

// CHỌN "Đang làm" TRƯỚC KHI VALIDATE
        cboTrangThai.setSelectedItem("Đang làm");

// Bây giờ validate sẽ thấy "Đang làm" và không bắt chọn nữa
        if (!validateForm()) {
            return;
        }
        addNV();

    }//GEN-LAST:event_btnThemActionPerformed

    private void btnXoaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXoaActionPerformed
        // TODO add your handling code here:

        deleteNV();

    }//GEN-LAST:event_btnXoaActionPerformed

    private void btnLamMoiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLamMoiActionPerformed
        // TODO add your handling code here:

        lamMoi();
    }//GEN-LAST:event_btnLamMoiActionPerformed

    private void tblQLNVMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblQLNVMouseClicked
        // TODO add your handling code here:
        showDetails(jTable1);
    }//GEN-LAST:event_tblQLNVMouseClicked

    private void tblNghiViecMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblNghiViecMouseClicked
        // TODO add your handling code here:
        showDetails(jTable1);
    }//GEN-LAST:event_tblNghiViecMouseClicked

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_jComboBox1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane NghiViec;
    private javax.swing.JTextField TF_DiaChiNV;
    private javax.swing.JTextField TF_EmailNV;
    private javax.swing.JTextField TF_MaNV;
    private javax.swing.JTextField TF_NgaySinhNV;
    private javax.swing.JTextField TF_SDTNV;
    private javax.swing.JTextField TF_TenNV;
    private javax.swing.JTextField TF_Timsdtnv;
    private javax.swing.JButton btnKhoa;
    private javax.swing.JButton btnLamMoi;
    private javax.swing.JButton btnMoKhoa;
    private javax.swing.JButton btnSua;
    private javax.swing.JButton btnThem;
    private javax.swing.JButton btnXoa;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> cboTrangThai;
    private javax.swing.JComboBox<String> cboVaiTro;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
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
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JRadioButton rdoNam;
    private javax.swing.JRadioButton rdoNu;
    private javax.swing.JTable tblNghiViec;
    private javax.swing.JTable tblQLNV;
    // End of variables declaration//GEN-END:variables
}
