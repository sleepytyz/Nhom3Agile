/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package View;

import Model.SanPham;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import DAO.SanPhamDAO;
import DAO.ThuocTinhDAO;
import Model.SanPhamGoiY;
import Model.SanPhamGoiYRenderer;
import Model.ThuocTinh;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableRowSorter;
import java.text.DecimalFormat;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author
 */
public class QLSP extends javax.swing.JPanel {

    private byte[] imageBytes = null;
    DefaultTableModel tableModel;
    DefaultTableModel model_XemTT;
    DefaultTableModel model_SPKhoa;
    DecimalFormat df = new DecimalFormat("#.##");

    SanPhamDAO spdao = new SanPhamDAO();
    ThuocTinhDAO ttdao = new ThuocTinhDAO();

    private JPopupMenu suggestionPopup;
    private JList<String> suggestionList;
    private DefaultListModel<String> suggestionListModel;
    // private KhachHangDAO khDAO; // Đảm bảo đã khai báo và khởi tạo ở đâu đó, nếu chưa thì khai báo ở đây
    private List<SanPham> currentSuggestions; // Để lưu trữ đối tượng KhachHang tương ứng với gợi ý

    private List<SanPhamGoiY> currentGoiYSP = new ArrayList<>();
    private DefaultListModel<SanPhamGoiY> modelGoiYSP = new DefaultListModel<>();
    private JList<SanPhamGoiY> listGoiYSP = new JList<>(modelGoiYSP);
    private JPopupMenu popupGoiYSP = new JPopupMenu();

    /**
     * Creates new form QLSP
     */
    public QLSP() {
        initComponents();

        listGoiYSP.setCellRenderer(new SanPhamGoiYRenderer());
        listGoiYSP.setFixedCellHeight(50); // Chiều cao mỗi item
        popupGoiYSP.add(new JScrollPane(listGoiYSP));
        popupGoiYSP.setFocusable(false);

        btn_Tim.addActionListener(e -> {
            int tabIndex = tab_sp.getSelectedIndex();
            if (tabIndex == 0) {
                timSPTheoTen(tbl_XemTT);
            } else {
                timSPTheoTen(tbl_SPkhoa);
            }
        });

        // Sự kiện khi nhập text
        TF_TenSP1.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                hienThiGoiYSP();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                hienThiGoiYSP();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        // Sự kiện bàn phím
        TF_TenSP1.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN && !modelGoiYSP.isEmpty()) {
                    listGoiYSP.requestFocusInWindow();
                    listGoiYSP.setSelectedIndex(0);
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    timSPTheoTen(tbl_XemTT); // Tìm khi nhấn Enter
                }
            }
        });

        // Sự kiện chọn từ danh sách gợi ý
        listGoiYSP.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    TF_TenSP1.setText(listGoiYSP.getSelectedValue().getTen());
                    popupGoiYSP.setVisible(false);
                }
            }
        });

        initTable();
        fillTable();
        tbl_XemTT.setName("tbl_XemTT");

        initTableSPKhoa();
        fillTableSPKhoa();
        tbl_SPkhoa.setName("tbl_SPKhoa");

        initTableTT();
        fillTableTT();

        initComboBox();
        addRDO();

        TF_MaSP.setEditable(false);

        TF_SoLuong.addActionListener(e -> {
            try {
                int sl = Integer.parseInt(TF_SoLuong.getText().trim());
                if (sl == 0) {
                    cbb_TrangThai.setSelectedItem("Hết hàng");
                } else if (cbb_TrangThai.getSelectedItem().toString().equals("Hết hàng")) {
                    cbb_TrangThai.setSelectedItem("Còn hàng");
                }
            } catch (NumberFormatException ex) {
                // Bỏ qua nếu nhập không phải số
            }
        });

        tab_sp.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                handleTabChange();
            }
        });

        // Xử lý trạng thái ban đầu
        handleTabChange();
    }
    private List<SanPham> danhSachSanPham = new ArrayList<>();
    private List<SanPham> danhSachSPKhoa = new ArrayList<>();

    private void hienThiGoiYSP() {
    String input = TF_TenSP1.getText().trim();
    modelGoiYSP.clear();

    if (!input.isEmpty()) {
        currentGoiYSP = spdao.goiYTenSanPham(input);
        for (SanPhamGoiY sp : currentGoiYSP) {
            modelGoiYSP.addElement(sp);
        }

        if (!modelGoiYSP.isEmpty()) {
            if (!popupGoiYSP.isVisible()) {
                // Hiển thị popup bên phải và dưới TF_TenSP1
                Point location = new Point(TF_TenSP1.getWidth(), TF_TenSP1.getHeight());
                popupGoiYSP.show(TF_TenSP1, location.x, location.y);
                listGoiYSP.setSelectedIndex(0);
            }
        } else {
            popupGoiYSP.setVisible(false);
        }
    } else {
        popupGoiYSP.setVisible(false);
    }
}



    private void handleTabChange() {
        int index = tab_sp.getSelectedIndex();

        // Chỉ reset dữ liệu nếu chuyển từ tab khác (không reset khi khởi tạo)
        if (currentTab != index) {
            lamMoi();
        }
        currentTab = index;

        resetAllComponents();

        switch (index) {
            case 0:
                setupViewInfoTab();
                break;
            case 1:
                setupLockedProductsTab();
                break;
            case 2:
                setupAttributesTab();
                break;
        }
    }

    private int currentTab = -1; // Thêm biến theo dõi tab hiện tại

// Reset tất cả components về trạng thái mặc định
    private void resetAllComponents() {
        // Các field nhập liệu
        TF_TenSP.setEditable(true);
        TF_Gia.setEditable(true);
        TF_SoLuong.setEditable(true);
        TF_TenSP1.setEditable(true);
        TF_GiaMin.setEditable(true);
        TF_GiaMax.setEditable(true);

        // Các combobox
        cbb_MauSac.setEnabled(true);
        cbb_KichThuoc.setEnabled(true);
        cbb_LoaiSP.setEnabled(true);
        cbb_ChatLieu.setEnabled(true);
        cbb_TrangThai.setEnabled(true);
        cbb_LTL.setEnabled(true);

        // Các nút chức năng chính
        btn_Them.setEnabled(true);
        btn_Sua.setEnabled(true);
        btn_Khoa.setEnabled(true);
        btn_MoKhoa.setEnabled(true);
        btn_LamMoi.setEnabled(true);
        btn_LamMoi1.setEnabled(true);

        // Các nút tìm kiếm
        btn_Tim.setEnabled(true);
        btn_Loc2.setEnabled(true);

        // Các nút thuộc tính
        btn_ThemTT.setEnabled(true);
        btn_SuaTT.setEnabled(true);
        btn_LamMoiTT.setEnabled(true);
    }

// Cấu hình cho tab "Xem thông tin"
    private void setupViewInfoTab() {
        // Enable các nút chính
        btn_Them.setEnabled(true);
        btn_Sua.setEnabled(true);
        btn_Khoa.setEnabled(true);

        // Disable nút mở khóa (vì đang ở tab thường)
        btn_MoKhoa.setEnabled(false);

        // Enable các chức năng tìm kiếm
        btn_Tim.setEnabled(true);
        btn_Loc2.setEnabled(true);

        // Disable các nút thuộc tính (vì không ở tab đó)
        btn_ThemTT.setEnabled(false);
        btn_SuaTT.setEnabled(false);
        btn_LamMoiTT.setEnabled(false);
    }

// Cấu hình cho tab "Sản phẩm khóa"
    private void setupLockedProductsTab() {
        // Chỉ cho phép xem, không cho chỉnh sửa
        TF_TenSP.setEditable(false);
        TF_Gia.setEditable(false);
        TF_SoLuong.setEditable(false);
        cbb_MauSac.setEnabled(false);
        cbb_KichThuoc.setEnabled(false);
        cbb_LoaiSP.setEnabled(false);
        cbb_ChatLieu.setEnabled(false);
        cbb_TrangThai.setEnabled(false);
        btn_LamMoi1.setEnabled(false);

        // Disable các nút chính trừ nút mở khóa
        btn_Them.setEnabled(false);
        btn_Sua.setEnabled(false);
        btn_Khoa.setEnabled(false);
        btn_MoKhoa.setEnabled(true);

        // Disable các nút thuộc tính
        btn_ThemTT.setEnabled(false);
        btn_SuaTT.setEnabled(false);
        btn_LamMoiTT.setEnabled(false);
    }

// Cấu hình cho tab "Thuộc tính sản phẩm"
    private void setupAttributesTab() {
        // Disable tất cả các nút chính
        btn_Them.setEnabled(false);
        btn_Sua.setEnabled(false);
        btn_Khoa.setEnabled(false);
        btn_MoKhoa.setEnabled(false);

        // Disable các chức năng tìm kiếm
        btn_Tim.setEnabled(false);
        btn_Loc2.setEnabled(false);
        TF_TenSP1.setEditable(false);
        TF_GiaMin.setEditable(false);
        TF_GiaMax.setEditable(false);
        cbb_LTL.setEnabled(false);

        // Chỉ enable các nút thuộc tính
        btn_ThemTT.setEnabled(true);
        btn_SuaTT.setEnabled(true);
        btn_LamMoiTT.setEnabled(true);

        // Disable các field không liên quan
        TF_TenSP.setEditable(false);
        TF_Gia.setEditable(false);
        TF_SoLuong.setEditable(false);
        cbb_MauSac.setEnabled(false);
        cbb_KichThuoc.setEnabled(false);
        cbb_LoaiSP.setEnabled(false);
        cbb_ChatLieu.setEnabled(false);
        cbb_TrangThai.setEnabled(false);
        btn_LamMoi1.setEnabled(false);
    }

    private String vietHoa(String input) {
        input = input.trim().toLowerCase();
        if (input.isEmpty()) {
            return "";
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public void initTable() {
        String[] cols = new String[]{"Mã SP", "Tên SP", "Loại SP", "Giá", "Số Lượng", "Màu Sắc", "Kích Thước", "Chất Liệu", "Trạng Thái", "Hình ảnh"};
        model_XemTT = new DefaultTableModel();
        model_XemTT.setColumnIdentifiers(cols);
        tbl_XemTT.setModel(model_XemTT);

    }

    public void fillTable() {
        model_XemTT.setRowCount(0);
        for (SanPham sp : spdao.getAll()) {
            if (!sp.getTrangThai().equalsIgnoreCase("Đã khóa")) {
                model_XemTT.addRow(spdao.getRow(sp));
            }
        }
        System.out.println("Tên bảng đang fill: " + tbl_XemTT.getName());
        System.out.println("Số dòng trong model trước khi fill: " + model_XemTT.getRowCount());

    }

    public void initTableSPKhoa() {
        String[] cols = new String[]{"Mã SP", "Tên SP", "Loại SP", "Giá", "Số Lượng", "Màu Sắc", "Kích Thước", "Chất Liệu", "Trạng Thái"};
//        DefaultTableModel modelSPKhoa = new DefaultTableModel();
        model_SPKhoa = new DefaultTableModel();
        model_SPKhoa.setColumnIdentifiers(cols);
        tbl_SPkhoa.setModel(model_SPKhoa);
    }

    public void fillTableSPKhoa() {
        DefaultTableModel model = (DefaultTableModel) tbl_SPkhoa.getModel();
        model.setRowCount(0);

        danhSachSPKhoa = spdao.dsSPKhoa("Đã khóa"); // lọc theo trạng thái Khóa

        for (SanPham sp : danhSachSPKhoa) {
            model.addRow(new Object[]{
                sp.getMasp(), sp.getTensp(), sp.getLoaisp(), sp.getGia(),
                sp.getSluong(), sp.getMausac(), sp.getKichThuoc(),
                sp.getChatLieu(), sp.getTrangThai()
            });
        }
    }

    private void addRDO() {
        // Thêm ActionListener cho mỗi radio button
        rdo_KichThuoc.addActionListener(e -> {
            locTT("Kích thước");
            sapXepThuocTinh("ASC"); // Mặc định sắp xếp tăng dần
        });

        rdo_MauSac.addActionListener(e -> {
            locTT("Màu sắc");
            sapXepThuocTinh("ASC");
        });

        rdo_ChatLieu.addActionListener(e -> {
            locTT("Chất liệu");
            sapXepThuocTinh("ASC");
        });

        rdo_LoaiSP.addActionListener(e -> {
            locTT("Loại sản phẩm");
            sapXepThuocTinh("ASC");
        });
    }

    private void sapXepThuocTinh(String sortOrder) {
        String loaiTT = "";
        if (rdo_KichThuoc.isSelected()) {
            loaiTT = "Kích thước";
        } else if (rdo_MauSac.isSelected()) {
            loaiTT = "Màu sắc";
        } else if (rdo_ChatLieu.isSelected()) {
            loaiTT = "Chất liệu";
        } else if (rdo_LoaiSP.isSelected()) {
            loaiTT = "Loại sản phẩm";
        }

        if (!loaiTT.isEmpty()) {
            DefaultTableModel model = (DefaultTableModel) tbl_ThuocTinh.getModel();
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            tbl_ThuocTinh.setRowSorter(sorter);

            // Sắp xếp theo cột Tên thuộc tính (cột 1)
            List<RowSorter.SortKey> sortKeys = new ArrayList<>();
            sortKeys.add(new RowSorter.SortKey(0, sortOrder.equals("ASC") ? SortOrder.ASCENDING : SortOrder.DESCENDING));
            sorter.setSortKeys(sortKeys);
        }
    }

    public void initTableTT() {
        String[] cols = new String[]{"Mã thuộc tính", "Tên thuộc tính", "Loại thuộc tính"};
        tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(cols);
        tbl_ThuocTinh.setModel(tableModel);
    }

    public void fillTableTT() {
        DefaultTableModel model = (DefaultTableModel) tbl_ThuocTinh.getModel();
        model.setRowCount(0);

        for (ThuocTinh tt : ttdao.getAllTT()) {
            model.addRow(ttdao.getRow(tt));
        }
    }

    private void initComboBox() {
        // Load danh sách từ DAO
        cbb_TrangThai.removeAllItems();
        cbb_TrangThai.addItem("--Chọn trạng thái--");
        cbb_TrangThai.addItem("Còn hàng");
        cbb_TrangThai.addItem("Hết hàng");
        cbb_TrangThai.addItem("Đã khóa");

        List<String> kT = ttdao.getTTTheoloai("Kích thước");
        List<String> mS = ttdao.getTTTheoloai("Màu sắc");
        List<String> cL = ttdao.getTTTheoloai("Chất liệu");
        List<String> lSP = ttdao.getTTTheoloai("Loại sản phẩm");

        // Xóa dữ liệu cũ
        cbb_KichThuoc.removeAllItems();
        cbb_KichThuoc.addItem("--Chọn kích thước--");

        cbb_MauSac.removeAllItems();
        cbb_MauSac.addItem("--Chọn màu sắc--");

        cbb_ChatLieu.removeAllItems();
        cbb_ChatLieu.addItem("--Chọn chất liệu--");

        cbb_LoaiSP.removeAllItems();
        cbb_LoaiSP.addItem("--Chọn loại sản phẩm--");

        cbb_LTL.removeAllItems();
        cbb_LTL.addItem("--Chọn loại cần lọc--");

        // Thêm dữ liệu mới
        for (String kt : kT) {
            cbb_KichThuoc.addItem(kt);
        }

        for (String ms : mS) {
            cbb_MauSac.addItem(ms);
        }

        for (String cl : cL) {
            cbb_ChatLieu.addItem(cl);
        }

        for (String lsp : lSP) {
            cbb_LoaiSP.addItem(lsp);
        }
        for (String ltl : lSP) {
            cbb_LTL.addItem(ltl);
        }
        cbb_LTL.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String loai = (String) cbb_LTL.getSelectedItem();

                // Xác định bảng đang hiển thị
                JTable currentTable = tab_sp.getSelectedIndex() == 0 ? tbl_XemTT : tbl_SPkhoa;

                if (loai == null || loai.equals("--Chọn loại cần lọc--")) {
                    // Nếu chọn "--Chọn loại cần lọc--" hoặc null, hiển thị tất cả
                    if (tab_sp.getSelectedIndex() == 0) {
                        fillTable(); // Load lại toàn bộ dữ liệu cho tab thường
                    } else {
                        fillTableSPKhoa(); // Load lại toàn bộ dữ liệu cho tab khóa
                    }
                } else {
                    // Nếu chọn một loại cụ thể, thực hiện lọc
                    locSPTheoLoai(currentTable, loai);
                }
            }
        });

    }

    private boolean validateForm() {

        String tenSP = TF_TenSP.getText().trim();
        if (tenSP.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên sản phẩm.");
            TF_TenSP.requestFocus(); // Đưa con trỏ về ô Tên sản phẩm
            return false;
        }
        if (tenSP.matches(".*\\d.*")) {
            JOptionPane.showMessageDialog(this, "Tên sản phẩm không được chứa số.");
            TF_TenSP.requestFocus(); // Đưa con trỏ về ô Tên sản phẩm
            return false;
        }
        if (tenSP.length() > 50) {
            JOptionPane.showMessageDialog(this, "Tên sản phẩm không vượt quá 50 ký tự!");
            TF_TenSP.requestFocus();// Đưa con trỏ về ô TenSP để nhập lại
            return false;
        }

        if (cbb_LoaiSP.getSelectedIndex() == 0 || cbb_LoaiSP.getSelectedItem().toString().equals("-- Chọn loại sản phẩm --")) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn loại sản phẩm.");
            cbb_LoaiSP.requestFocus();  // Đưa con trỏ về ô loaisp
            return false;
        }

        if (cbb_ChatLieu.getSelectedIndex() == 0 || cbb_ChatLieu.getSelectedItem().toString().equals("-- Chọn chất liệu --")) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn chất liệu sản phẩm.");
            cbb_ChatLieu.requestFocus();  // Đưa con trỏ về ô chatlieu
            return false;
        }

        if (cbb_TrangThai.getSelectedIndex() == 0 || cbb_TrangThai.getSelectedItem().toString().equals("-- Chọn trạng thái --")) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn trạng thái sản phẩm.");
            cbb_TrangThai.requestFocus();  // Đưa con trỏ về ô trangth
            return false;
        }

        String giaStr = TF_Gia.getText().trim();
        if (giaStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập giá sản phẩm.");
            TF_Gia.requestFocus();  // Đưa con trỏ về ô giá
            return false;
        }
        try {
            float gia = Float.parseFloat(giaStr);
            if (gia <= 0) {
                JOptionPane.showMessageDialog(this, "Giá phải là số dương (lớn hơn 0).");
                TF_Gia.requestFocus();  // Đưa con trỏ về ô giá
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá phải là số hợp lệ.");
            TF_Gia.requestFocus();  // Đưa con trỏ về ô giá
            return false;
        }

        String soLuongStr = TF_SoLuong.getText().trim();
        if (soLuongStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số lượng sản phẩm.");
            TF_SoLuong.requestFocus();
            return false;
        }
        try {
            int sl = Integer.parseInt(soLuongStr);
            if (sl < 0) {
                JOptionPane.showMessageDialog(this, "Số lượng phải là số nguyên dương.");
                TF_SoLuong.requestFocus();
                return false;
            }

            // Tự động cập nhật trạng thái theo số lượng
            if (sl == 0) {
                cbb_TrangThai.setSelectedItem("Hết hàng");
            } else if (cbb_TrangThai.getSelectedItem().toString().equals("Hết hàng")) {
                cbb_TrangThai.setSelectedItem("Còn hàng");
            }
            String trangThai = cbb_TrangThai.getSelectedItem().toString();
            if (sl > 0 && trangThai.equals("Hết hàng")) {
                JOptionPane.showMessageDialog(this,
                        "Không thể đặt trạng thái 'Hết hàng' khi số lượng > 0");
                cbb_TrangThai.requestFocus();
                return false;
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số lượng phải là số nguyên hợp lệ.");
            TF_SoLuong.requestFocus();
            return false;
        }

        if (cbb_MauSac.getSelectedIndex() == 0 || cbb_MauSac.getSelectedItem().toString().equals("-- Chọn màu sắc --")) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn màu sắc sản phẩm.");
            cbb_MauSac.requestFocus();  // Đưa con trỏ về ô màu
            return false;
        }

        if (cbb_KichThuoc.getSelectedIndex() == 0 || cbb_KichThuoc.getSelectedItem().toString().equals("-- Chọn kích thước --")) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn kích thước sản phẩm.");
            cbb_KichThuoc.requestFocus();  // Đưa con trỏ về ô màu
            return false;
        }

        return true;
    }

    public void lamMoi() {
        TF_MaSP.setText("");
        TF_TenSP.setText("");
        TF_Gia.setText("");
        TF_SoLuong.setText("");
        TF_TenSP1.setText("");
        TF_GiaMax.setText("");
        TF_GiaMin.setText("");

        // Đưa ảnh về null
        imageBytes = null; // ✅ xóa dữ liệu ảnh đã chọn
        jLabel_Anh.setIcon(null); // ✅ xóa ảnh hiển thị
        jLabel_Anh.setText("");   // tùy ý, có thể để lại chữ nếu muốn

        cbb_LTL.setSelectedIndex(0);
        cbb_TrangThai.setSelectedIndex(0);
        cbb_LoaiSP.setSelectedIndex(0);
        cbb_ChatLieu.setSelectedIndex(0);
        cbb_KichThuoc.setSelectedIndex(0);
        cbb_MauSac.setSelectedIndex(0);

        tbl_XemTT.clearSelection(); // bỏ chọn dòng đang chọn
        fillTable();

        tbl_SPkhoa.clearSelection(); // bỏ chọn dòng đang chọn
        fillTableSPKhoa(); // load lại bảng nếu muốn cập nhật
    }

    public void lamMoiTT() {
        TF_TT.setText("");
        buttonGroup3.clearSelection();
        tbl_ThuocTinh.clearSelection(); // bỏ chọn dòng đang chọn
        fillTableTT();
    }

    public void showDetail(JTable table) {
        int i = table.getSelectedRow();
        if (i >= 0) {
            String maSP = table.getValueAt(i, 0).toString();
            SanPham sp = spdao.timSPTheoMa(maSP);

            TF_MaSP.setText(sp.getMasp());
            TF_TenSP.setText(sp.getTensp());

            // ✅ Định dạng lại giá để tránh E (khoa học)
            DecimalFormat df = new DecimalFormat("#.##");
            TF_Gia.setText(df.format(sp.getGia()));

            TF_SoLuong.setText(String.valueOf(sp.getSluong()));

            for (int j = 0; j < cbb_ChatLieu.getItemCount(); j++) {
                if (cbb_ChatLieu.getItemAt(j).toString().trim().equalsIgnoreCase(sp.getChatLieu().trim())) {
                    cbb_ChatLieu.setSelectedIndex(j);
                    break;
                }
            }

            for (int j = 0; j < cbb_KichThuoc.getItemCount(); j++) {
                if (cbb_KichThuoc.getItemAt(j).toString().trim().equalsIgnoreCase(sp.getKichThuoc().trim())) {
                    cbb_KichThuoc.setSelectedIndex(j);
                    break;
                }
            }

            for (int j = 0; j < cbb_LoaiSP.getItemCount(); j++) {
                if (cbb_LoaiSP.getItemAt(j).toString().trim().equalsIgnoreCase(sp.getLoaisp().trim())) {
                    cbb_LoaiSP.setSelectedIndex(j);
                    break;
                }
            }

            for (int j = 0; j < cbb_MauSac.getItemCount(); j++) {
                if (cbb_MauSac.getItemAt(j).toString().trim().equalsIgnoreCase(sp.getMausac().trim())) {
                    cbb_MauSac.setSelectedIndex(j);
                    break;
                }
            }

            for (int j = 0; j < cbb_TrangThai.getItemCount(); j++) {
                if (cbb_TrangThai.getItemAt(j).toString().trim().equalsIgnoreCase(sp.getTrangThai().trim())) {
                    cbb_TrangThai.setSelectedIndex(j);
                    break;
                }
            }

            byte[] imgBytes = sp.getHinhAnh();
            if (imgBytes != null) {
                ImageIcon icon = new ImageIcon(imgBytes);
                Image img = icon.getImage().getScaledInstance(jLabel_Anh.getWidth(), jLabel_Anh.getHeight(), Image.SCALE_SMOOTH);
                jLabel_Anh.setIcon(new ImageIcon(img));
            } else {
                jLabel_Anh.setIcon(null); // hoặc ảnh mặc định
            }

        }
    }

    public void showDetailTT() {
        int i = tbl_ThuocTinh.getSelectedRow();

        if (i != -1) {
            String tenTT = tbl_ThuocTinh.getValueAt(i, 1).toString();
            String loaiTT = tbl_ThuocTinh.getValueAt(i, 2).toString();

            TF_TT.setText(tenTT);
            if (loaiTT.equalsIgnoreCase("Kích thước")) {
                rdo_KichThuoc.setSelected(true);
            } else if (loaiTT.equalsIgnoreCase("Màu sắc")) {
                rdo_MauSac.setSelected(true);
            } else if (loaiTT.equalsIgnoreCase("Chất liệu")) {
                rdo_ChatLieu.setSelected(true);
            } else if (loaiTT.equalsIgnoreCase("Loại sản phẩm")) {
                rdo_LoaiSP.setSelected(true);
            }
        }

    }

    public void them() {
        DecimalFormat df = new DecimalFormat("#.##");

        String masp = TF_MaSP.getText();
        String tensp = vietHoa(TF_TenSP.getText().trim().toLowerCase());
        String loaisp = cbb_LoaiSP.getSelectedItem().toString().toLowerCase();
        String chuDau_TenSP = tensp.split(" ")[0];

        if (!chuDau_TenSP.equalsIgnoreCase(loaisp)) {
            JOptionPane.showMessageDialog(this,
                    "Tên sản phẩm là: '" + chuDau_TenSP
                    + "' nhưng bạn chọn loại: '" + loaisp + "'.\nVui lòng kiểm tra lại.");
            return;
        }

        try {
            float gia = Float.parseFloat(TF_Gia.getText());
            long sluongLong = Long.parseLong(TF_SoLuong.getText());
            String mauSac = cbb_MauSac.getSelectedItem().toString();
            String kichThuoc = cbb_KichThuoc.getSelectedItem().toString().toUpperCase();
            String chatLieu = cbb_ChatLieu.getSelectedItem().toString();

            // Tự động cập nhật trạng thái theo số lượng
            String trangThai = (sluongLong == 0) ? "Hết hàng" : cbb_TrangThai.getSelectedItem().toString();

            SanPham sp = new SanPham(masp, tensp, loaisp, gia, 0, mauSac, kichThuoc, chatLieu, trangThai);
            sp.setHinhAnh(imageBytes);
            int Result = spdao.themSP(sp, sluongLong);

            if (Result == 1) {
                fillTable();
                TF_Gia.setText(df.format(gia));
                TF_SoLuong.setText(String.valueOf(sluongLong));
                JOptionPane.showMessageDialog(this, "Thêm thành công");
            } else {
                JOptionPane.showMessageDialog(this, "Kích thước không được vượt quá 10 ký tự.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá hoặc số lượng không hợp lệ!");
        }
    }

    public String getBTG(ButtonGroup group) {
        for (Enumeration<AbstractButton> buttons = group.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();
            if (button.isSelected()) {
                return button.getText(); // Lấy đúng text hiển thị trên giao diện (VD: "Kích thước")
            }
        }
        return null; // Nếu không chọn gì
    }

    public void themTT() {
        String tenTT = vietHoa(TF_TT.getText().trim());
        if (tenTT == null || tenTT.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên thuộc tính không được để trống.");
            return;
        }
        String loaiTT = "";
        if (rdo_KichThuoc.isSelected()) {
            loaiTT = "Kích thước";
            tenTT = tenTT.toUpperCase();
        } else if (rdo_MauSac.isSelected()) {
            loaiTT = "Màu sắc";
        } else if (rdo_ChatLieu.isSelected()) {
            loaiTT = "Chất liệu";
        } else if (rdo_LoaiSP.isSelected()) {
            loaiTT = "Loại sản phẩm";
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn loại thuộc tính.");
            return;
        }

        if (buttonGroup3 == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn loại thuộc tính.");
            return;
        }

        List<ThuocTinh> ds = ttdao.getAllTT();
        for (ThuocTinh tt : ds) {
            if (tt.getTenTT().equalsIgnoreCase(tenTT) && tt.getLoaiTT().equalsIgnoreCase(loaiTT)) {
                JOptionPane.showMessageDialog(this, "Tên thuộc tính đã tồn tại!");
                return;
            }
        }
        if (loaiTT.equalsIgnoreCase("Kích thước") && tenTT.length() > 10) {
            JOptionPane.showMessageDialog(this, "Không thể thêm kích thước quá 10 ký tự!");
            return;
        }
        // Sinh mã tự động
        String maTT = "";
        switch (loaiTT) {
            case "Kích thước":
                maTT = ttdao.taoMaKT();
                break;
            case "Màu sắc":
                maTT = ttdao.taoMaMS();
                break;
            case "Chất liệu":
                maTT = ttdao.taoMaCL();
                break;
            case "Loại sản phẩm":
                maTT = ttdao.taoMaLSP();
                break;
        }
        ThuocTinh tt = new ThuocTinh(maTT, tenTT, loaiTT);
        boolean themOK = ttdao.them(tt);

        if (themOK) {
            JOptionPane.showMessageDialog(this, "Thêm thành công");
            fillTableTT();
            initComboBox();// cập nhật combobox các tab còn lại
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm thuộc tính");
        }
    }
//   

    public void sua() {
        DecimalFormat df = new DecimalFormat("#.##");

        int i = tbl_XemTT.getSelectedRow();
        if (i >= 0) {
            if (!validateForm()) {
                return;
            }

            // Lấy số lượng từ form
            int sluong = 0;
            try {
                sluong = Integer.parseInt(TF_SoLuong.getText().trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Số lượng không hợp lệ!");
                return;
            }

            // Kiểm tra nếu số lượng > 0 mà trạng thái là "Hết hàng"
            String trangThai = vietHoa(cbb_TrangThai.getSelectedItem().toString());
            if (sluong > 0 && trangThai.equalsIgnoreCase("Hết hàng")) {
                JOptionPane.showMessageDialog(this,
                        "Không thể đặt trạng thái 'Hết hàng' khi số lượng > 0.\n"
                        + "Vui lòng điều chỉnh số lượng hoặc chọn trạng thái khác.");
                return;
            }

            int chon = JOptionPane.showConfirmDialog(this, "Bạn có thực sự muốn sửa ? ",
                    "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (chon == JOptionPane.YES_OPTION) {
                String macu = tbl_XemTT.getValueAt(i, 0).toString();
                String masp = TF_MaSP.getText();
                String tensp = vietHoa(TF_TenSP.getText());
                String loaisp = vietHoa(cbb_LoaiSP.getSelectedItem().toString());
                String chuDau_TenSP = TF_TenSP.getText().trim().split(" ")[0].toLowerCase();
                String loaiSP_Lower = loaisp.toLowerCase();

                if (!chuDau_TenSP.equalsIgnoreCase(loaiSP_Lower)) {
                    JOptionPane.showMessageDialog(this, "Tên sản phẩm là: \"" + chuDau_TenSP
                            + "\" nhưng bạn chọn loại: \"" + loaiSP_Lower
                            + "\".\nVui lòng chọn đúng loại sản phẩm khớp với tên.");
                    return;
                }

                try {
                    float gia = Float.parseFloat(TF_Gia.getText());
                    long sluongLong = Long.parseLong(TF_SoLuong.getText());
                    String mauSac = vietHoa(cbb_MauSac.getSelectedItem().toString());
                    String kichThuoc = cbb_KichThuoc.getSelectedItem().toString().toUpperCase();
                    String chatLieu = vietHoa(cbb_ChatLieu.getSelectedItem().toString());

                    SanPham spmoi = new SanPham(masp, tensp, loaisp, gia, 0, mauSac, kichThuoc, chatLieu, trangThai);

                    // Kiểm tra nếu không chọn ảnh mới thì giữ ảnh cũ
                    if (imageBytes == null) {
                        SanPham spCu = spdao.timSPTheoMa(macu);
                        spmoi.setHinhAnh(spCu.getHinhAnh());
                    } else {
                        spmoi.setHinhAnh(imageBytes);
                    }

                    int result = spdao.suaSP(spmoi, macu, sluongLong);

                    if (result == 1) {
                        danhSachSanPham = spdao.getAll();
                        fillTable();
                        fillTableSPKhoa();
                        TF_Gia.setText(df.format(gia));
                        TF_SoLuong.setText(String.valueOf(sluongLong));
                        JOptionPane.showMessageDialog(this, "Sửa thành công");
                    } else {
                        JOptionPane.showMessageDialog(this, "Sửa thất bại");
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Giá hoặc số lượng không hợp lệ!");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Chọn sản phẩm để sửa");
        }
    }

    public void suaTT() {
        int i = tbl_ThuocTinh.getSelectedRow();
        if (i == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để sửa.");
            return;
        }
        int chon = JOptionPane.showConfirmDialog(this, "Bạn có thực sự muốn sửa ? ",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (chon == JOptionPane.YES_OPTION) {
            String ma = tbl_ThuocTinh.getValueAt(i, 0).toString(); // Cột 0: MaThuocTinh
            String ten = vietHoa(TF_TT.getText().trim());
            String loai = "";
            if (loai.equals("Kích thước")) {
                ten = ten.toUpperCase(); // Ghi đè bằng in hoa toàn bộ nếu là Kích thước
            } else if (rdo_MauSac.isSelected()) {
                loai = "Màu sắc";
            } else if (rdo_ChatLieu.isSelected()) {
                loai = "Chất liệu";
            } else if (rdo_LoaiSP.isSelected()) {
                loai = "Loại sản phẩm";
            }

            for (int a = 0; a < tbl_ThuocTinh.getRowCount(); a++) {
                String tenTrongBang = tbl_ThuocTinh.getValueAt(a, 1).toString();
                String maTrongBang = tbl_ThuocTinh.getValueAt(a, 0).toString();

                if (ten.equalsIgnoreCase(tenTrongBang) && !ma.equals(maTrongBang)) {
                    JOptionPane.showMessageDialog(this, "Tên thuộc tính đã tồn tại.");
                    return;
                }
            }
            ThuocTinh tt = new ThuocTinh(ma, ten, loai);
            int result = ttdao.suaTT(tt);

            if (result > 0) {
                JOptionPane.showMessageDialog(this, "Sửa thành công");
                fillTableTT();
                initComboBox();// cập nhật combobox các tab còn lại
            } else {
                JOptionPane.showMessageDialog(this, "Sửa thất bại");
            }
        }
    }

    private void chonAnh() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Hình ảnh", "jpg", "png", "jpeg");
        chooser.setFileFilter(filter);
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                imageBytes = Files.readAllBytes(file.toPath()); // đọc ảnh thành mảng byte
                // Nếu bạn có JLabel để hiển thị ảnh thì:
                ImageIcon icon = new ImageIcon(imageBytes);
                Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                jLabel_Anh.setIcon(new ImageIcon(img));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void khoa() {
        int i = tbl_XemTT.getSelectedRow();
        if (i >= 0) {
            String maSP = tbl_XemTT.getValueAt(i, 0).toString();
            SanPham sp = spdao.timSPTheoMa(maSP);
//            SanPham sp = spdao.getAll().get(i);
//            if (sp.getTrangThai().equalsIgnoreCase("Đã khóa")) {
//                JOptionPane.showMessageDialog(this, "Sản phẩm đã bị khóa rồi!");
//                return;
//            }
            int chon = JOptionPane.showConfirmDialog(this, "Bạn muốn khóa sản phẩm này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (chon == JOptionPane.YES_OPTION) {
                int result = spdao.khoaSP(sp.getMasp());
                if (result == 1) {
                    JOptionPane.showMessageDialog(this, "Khóa sản phẩm thành công !");
                    danhSachSanPham = spdao.getAll();
                    danhSachSPKhoa = spdao.dsSPKhoa("Đã khóa");
                    fillTable();
                    fillTableSPKhoa();
                } else {
                    JOptionPane.showMessageDialog(this, "Khóa sản phẩm thất bại!");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Chọn sản phẩm cần khóa.");
        }
    }

    public void moKhoa() {
        int i = tbl_SPkhoa.getSelectedRow();
        if (i >= 0) {
            String maSP = tbl_SPkhoa.getValueAt(i, 0).toString();

            int chon = JOptionPane.showConfirmDialog(this, "Bạn muốn mở khóa sản phẩm này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (chon == JOptionPane.YES_OPTION) {

                // Hỏi người dùng nhập trạng thái mới sau khi mở khóa
                String[] trangThai = {"Còn hàng", "Hết hàng"};
                JComboBox<String> cboTrangThai = new JComboBox<>(trangThai);

                int result = JOptionPane.showConfirmDialog(
                        this,
                        cboTrangThai,
                        "Chọn trạng thái sản phẩm",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );

                if (result == JOptionPane.OK_OPTION) {
                    String trangThaiMoi = cboTrangThai.getSelectedItem().toString();

                    int kq = spdao.moKhoaSP(maSP, trangThaiMoi);
                    if (kq == 1) {
                        JOptionPane.showMessageDialog(this, "Mở khóa sản phẩm thành công.");
                        danhSachSanPham = spdao.getAll();
                        danhSachSPKhoa = spdao.dsSPKhoa("Đã khóa");
                        fillTable();
                        fillTableSPKhoa();
                    } else {
                        JOptionPane.showMessageDialog(this, "Mở khóa sản phẩm thất bại.");
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm để mở khóa.");
        }
    }

    public void timSPTheoTen(JTable table) {
        String tenSP = TF_TenSP1.getText().trim();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0); // Xóa dữ liệu cũ

        if (tenSP.isEmpty()) {
            // Nếu ô trống, hiển thị tất cả
            if (table == tbl_XemTT) {
                fillTable();
            } else {
                fillTableSPKhoa();
            }
            return;
        }

        // Debug: In ra tên SP đang tìm
        System.out.println("Đang tìm sản phẩm: " + tenSP);

        List<SanPham> ds = spdao.timSPTheoTen(tenSP);

        // Debug: In ra số lượng kết quả tìm được
        System.out.println("Số lượng kết quả: " + ds.size());

        if (!ds.isEmpty()) {
            boolean found = false;
            for (SanPham sp : ds) {
                // Debug: In thông tin từng sản phẩm
                System.out.println("SP: " + sp.getTensp() + " - Trạng thái: " + sp.getTrangThai());

                boolean isLocked = sp.getTrangThai().equalsIgnoreCase("Đã khóa");

                if (table == tbl_XemTT && !isLocked) {
                    model.addRow(spdao.getRow(sp));
                    found = true;
                } else if (table == tbl_SPkhoa && isLocked) {
                    model.addRow(spdao.getRow(sp));
                    found = true;
                }
            }

            if (!found) {
                String message = table == tbl_XemTT
                        ? "Sản phẩm '" + tenSP + "' đã bị khóa. Vui lòng kiểm tra tab Sản phẩm khóa."
                        : "Sản phẩm '" + tenSP + "' không bị khóa. Vui lòng kiểm tra tab Xem thông tin.";
                JOptionPane.showMessageDialog(this, message);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm thấy sản phẩm: " + tenSP);
        }
    }

    public void locTheoKhoangGia(JTable table) {
        try {
            String min = TF_GiaMin.getText().trim();
            String max = TF_GiaMax.getText().trim();

            // Kiểm tra ô nhập trống
            if (min.isEmpty() || max.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ cả giá thấp nhất và giá cao nhất!");
                return;
            }

            float giaMin = Float.parseFloat(TF_GiaMin.getText());
            float giaMax = Float.parseFloat(TF_GiaMax.getText());

            if (giaMin <= 0) {  // Giá không thể âm
                JOptionPane.showMessageDialog(this, "Đơn giá thấp nhất phải là số hợp lệ \n không được âm!");
                TF_GiaMin.requestFocus();
                return;
            }
            if (giaMax <= 0) {  // Giá không thể âm
                JOptionPane.showMessageDialog(this, "Đơn giá cao nhất phải là số hợp lệ \n không được âm!");
                TF_GiaMax.requestFocus();
                return;
            }
            if (giaMin > giaMax) {
                JOptionPane.showMessageDialog(this, "Giá thấp nhất phải nhỏ hơn giá lớn nhất");
                TF_GiaMin.requestFocus();
                return;
            }

            List<SanPham> ds = spdao.LocSPTheoGia(giaMin, giaMax);
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0); // Xóa dữ liệu cũ

            for (SanPham sp : ds) {
                if (table == tbl_XemTT && !sp.getTrangThai().equalsIgnoreCase("Đã khóa")) {
                    model.addRow(spdao.getRow(sp));
                } else if (table == tbl_SPkhoa && sp.getTrangThai().equalsIgnoreCase("Đã khóa")) {
                    model.addRow(spdao.getRow(sp));
                }
            }
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Không có sản phẩm nào trong khoảng giá đã chọn!");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ!");
        }
    }

    public void locSPTheoLoai(JTable table, String loai) {
        try {
            if (loai == null || loai.trim().isEmpty() || loai.equals("--Chọn loại cần lọc--")) {
                if (table == tbl_XemTT) {
                    fillTable(); // Load lại toàn bộ dữ liệu cho tab thường
                } else if (table == tbl_SPkhoa) {
                    fillTableSPKhoa(); // Load lại toàn bộ dữ liệu cho tab khóa
                }
                return;
            }

            List<SanPham> ds = spdao.locSPTheoLoai(loai);
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0); // Xóa dữ liệu cũ

            for (SanPham sp : ds) {
                if (table == tbl_XemTT && !sp.getTrangThai().equalsIgnoreCase("Đã khóa")) {
                    model.addRow(spdao.getRow(sp));
                } else if (table == tbl_SPkhoa && sp.getTrangThai().equalsIgnoreCase("Đã khóa")) {
                    model.addRow(spdao.getRow(sp));
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi khi lọc theo loại!");
            e.printStackTrace();
        }
    }

    public void locTT(String loai) {
        DefaultTableModel model = (DefaultTableModel) tbl_ThuocTinh.getModel();
        model.setRowCount(0); // Xóa dữ liệu cũ

        List<ThuocTinh> ds = ttdao.locTT(loai);
        for (ThuocTinh tt : ds) {
            model.addRow(new Object[]{
                tt.getMaTT(),
                tt.getTenTT(),
                tt.getLoaiTT()
            });
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

        TF_MaKH4 = new javax.swing.JTextField();
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        TF_TenSP = new javax.swing.JTextField();
        TF_MaSP = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel25 = new javax.swing.JLabel();
        btn_Tim = new javax.swing.JButton();
        TF_TenSP1 = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        TF_Gia = new javax.swing.JTextField();
        TF_SoLuong = new javax.swing.JTextField();
        cbb_MauSac = new javax.swing.JComboBox<>();
        cbb_KichThuoc = new javax.swing.JComboBox<>();
        cbb_LoaiSP = new javax.swing.JComboBox<>();
        cbb_ChatLieu = new javax.swing.JComboBox<>();
        cbb_TrangThai = new javax.swing.JComboBox<>();
        btn_LamMoi1 = new javax.swing.JButton();
        jLabel_Anh = new javax.swing.JLabel();
        tab_sp = new javax.swing.JTabbedPane();
        pnl_XemTT = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tbl_XemTT = new javax.swing.JTable();
        pnl_SPK = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tbl_SPkhoa = new javax.swing.JTable();
        pnl_ThuocTinh = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tbl_ThuocTinh = new javax.swing.JTable();
        lbl_GiaMin3 = new javax.swing.JLabel();
        TF_TT = new javax.swing.JTextField();
        rdo_KichThuoc = new javax.swing.JRadioButton();
        rdo_MauSac = new javax.swing.JRadioButton();
        rdo_LoaiSP = new javax.swing.JRadioButton();
        btn_ThemTT = new javax.swing.JButton();
        btn_SuaTT = new javax.swing.JButton();
        rdo_ChatLieu = new javax.swing.JRadioButton();
        btn_LamMoiTT = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        TF_GiaMin = new javax.swing.JTextField();
        jLabel35 = new javax.swing.JLabel();
        lbl_GiaMin2 = new javax.swing.JLabel();
        TF_GiaMax = new javax.swing.JTextField();
        lbl_GiaMax2 = new javax.swing.JLabel();
        btn_Loc2 = new javax.swing.JButton();
        cbb_LTL = new javax.swing.JComboBox<>();
        jPanel2 = new javax.swing.JPanel();
        btn_Them = new javax.swing.JButton();
        btn_Sua = new javax.swing.JButton();
        btn_MoKhoa = new javax.swing.JButton();
        btn_Khoa = new javax.swing.JButton();
        btn_LamMoi = new javax.swing.JButton();

        TF_MaKH4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TF_MaKH4ActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Quản lý khách hàng"));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel13.setText("Mã sản phẩm :");

        TF_TenSP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TF_TenSPActionPerformed(evt);
            }
        });

        TF_MaSP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TF_MaSPActionPerformed(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel14.setText("Loại sản phẩm :");

        jLabel21.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel21.setText("Tên sản phẩm :");

        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel22.setText("Màu sắc :");

        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel23.setText("Đơn giá :");

        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel24.setText("Số lượng :");

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Tìm kiếm"));

        jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel25.setText("Tên sản phẩm :");

        btn_Tim.setIcon(new javax.swing.ImageIcon("D:\\MOB1014\\icons\\search.png")); // NOI18N
        btn_Tim.setText("Tìm");
        btn_Tim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_TimActionPerformed(evt);
            }
        });

        TF_TenSP1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TF_TenSP1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel25)
                        .addGap(18, 18, 18)
                        .addComponent(TF_TenSP1, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(164, 164, 164)
                        .addComponent(btn_Tim, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(71, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(TF_TenSP1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btn_Tim, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(29, Short.MAX_VALUE))
        );

        jLabel28.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel28.setText("Kích thước :");

        jLabel29.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel29.setText("Chất liệu :");

        jLabel30.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel30.setText("Trạng thái :");

        TF_Gia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TF_GiaActionPerformed(evt);
            }
        });

        TF_SoLuong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TF_SoLuongActionPerformed(evt);
            }
        });

        cbb_MauSac.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        cbb_KichThuoc.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        cbb_LoaiSP.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        cbb_ChatLieu.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        cbb_TrangThai.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btn_LamMoi1.setText("Chọn ảnh");
        btn_LamMoi1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_LamMoi1ActionPerformed(evt);
            }
        });

        jLabel_Anh.setText("...");
        jLabel_Anh.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel_AnhMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(55, 55, 55)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel29)
                    .addComponent(jLabel14)
                    .addComponent(jLabel30)
                    .addComponent(jLabel13)
                    .addComponent(jLabel21, javax.swing.GroupLayout.Alignment.LEADING))
                .addGap(48, 48, 48)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbb_TrangThai, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(TF_MaSP, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(TF_TenSP, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cbb_LoaiSP, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cbb_ChatLieu, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(44, 44, 44)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel23)
                    .addComponent(jLabel24)
                    .addComponent(jLabel28)
                    .addComponent(jLabel22))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(cbb_MauSac, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(106, 106, 106)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(TF_SoLuong, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(TF_Gia, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(315, 315, 315))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(cbb_KichThuoc, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel_Anh, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)))
                        .addComponent(btn_LamMoi1, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(11, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13)
                            .addComponent(TF_MaSP, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TF_Gia, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel23))
                        .addGap(33, 33, 33)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel21)
                            .addComponent(TF_TenSP, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TF_SoLuong, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel24))
                        .addGap(33, 33, 33)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel14)
                            .addComponent(jLabel22)
                            .addComponent(cbb_MauSac, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbb_LoaiSP, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(34, 34, 34)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel29)
                            .addComponent(jLabel28)
                            .addComponent(cbb_KichThuoc, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbb_ChatLieu, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(2, 2, 2)
                                .addComponent(cbb_TrangThai, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel30)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btn_LamMoi1, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel_Anh, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(9, 9, 9))
        );

        tab_sp.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tab_spMouseClicked(evt);
            }
        });

        pnl_XemTT.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        tbl_XemTT.setModel(new javax.swing.table.DefaultTableModel(
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
        tbl_XemTT.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbl_XemTTMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tbl_XemTT);

        javax.swing.GroupLayout pnl_XemTTLayout = new javax.swing.GroupLayout(pnl_XemTT);
        pnl_XemTT.setLayout(pnl_XemTTLayout);
        pnl_XemTTLayout.setHorizontalGroup(
            pnl_XemTTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_XemTTLayout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 1094, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(34, Short.MAX_VALUE))
        );
        pnl_XemTTLayout.setVerticalGroup(
            pnl_XemTTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_XemTTLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        tab_sp.addTab("Xem thông tin", pnl_XemTT);

        pnl_SPK.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        tbl_SPkhoa.setModel(new javax.swing.table.DefaultTableModel(
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
        tbl_SPkhoa.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbl_SPkhoaMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(tbl_SPkhoa);

        javax.swing.GroupLayout pnl_SPKLayout = new javax.swing.GroupLayout(pnl_SPK);
        pnl_SPK.setLayout(pnl_SPKLayout);
        pnl_SPKLayout.setHorizontalGroup(
            pnl_SPKLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_SPKLayout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 1095, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(33, Short.MAX_VALUE))
        );
        pnl_SPKLayout.setVerticalGroup(
            pnl_SPKLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_SPKLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        tab_sp.addTab("Sản phẩm khoá", pnl_SPK);

        pnl_ThuocTinh.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        tbl_ThuocTinh.setModel(new javax.swing.table.DefaultTableModel(
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
        tbl_ThuocTinh.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbl_ThuocTinhMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(tbl_ThuocTinh);

        lbl_GiaMin3.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lbl_GiaMin3.setText("Thuộc tính sản phẩm :");

        buttonGroup3.add(rdo_KichThuoc);
        rdo_KichThuoc.setText("Kích thước");

        buttonGroup3.add(rdo_MauSac);
        rdo_MauSac.setText("Màu sắc");

        buttonGroup3.add(rdo_LoaiSP);
        rdo_LoaiSP.setText("Loại sản phẩm");

        btn_ThemTT.setIcon(new javax.swing.ImageIcon("D:\\MOB1014\\icons\\add.png")); // NOI18N
        btn_ThemTT.setText("Thêm");
        btn_ThemTT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_ThemTTActionPerformed(evt);
            }
        });

        btn_SuaTT.setIcon(new javax.swing.ImageIcon("D:\\MOB1014\\icons\\edit.png")); // NOI18N
        btn_SuaTT.setText("Sửa");
        btn_SuaTT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_SuaTTActionPerformed(evt);
            }
        });

        buttonGroup3.add(rdo_ChatLieu);
        rdo_ChatLieu.setText("Chất liệu");
        rdo_ChatLieu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdo_ChatLieuActionPerformed(evt);
            }
        });

        btn_LamMoiTT.setText("Làm mới");
        btn_LamMoiTT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_LamMoiTTActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnl_ThuocTinhLayout = new javax.swing.GroupLayout(pnl_ThuocTinh);
        pnl_ThuocTinh.setLayout(pnl_ThuocTinhLayout);
        pnl_ThuocTinhLayout.setHorizontalGroup(
            pnl_ThuocTinhLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_ThuocTinhLayout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addGroup(pnl_ThuocTinhLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnl_ThuocTinhLayout.createSequentialGroup()
                        .addGroup(pnl_ThuocTinhLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btn_ThemTT, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lbl_GiaMin3))
                        .addGroup(pnl_ThuocTinhLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnl_ThuocTinhLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(TF_TT, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(51, 51, 51)
                                .addComponent(rdo_KichThuoc)
                                .addGap(52, 52, 52)
                                .addComponent(rdo_MauSac)
                                .addGap(62, 62, 62)
                                .addComponent(rdo_LoaiSP)
                                .addGap(64, 64, 64)
                                .addComponent(rdo_ChatLieu))
                            .addGroup(pnl_ThuocTinhLayout.createSequentialGroup()
                                .addGap(29, 29, 29)
                                .addComponent(btn_SuaTT, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(37, 37, 37)
                                .addComponent(btn_LamMoiTT, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 1057, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(52, Short.MAX_VALUE))
        );
        pnl_ThuocTinhLayout.setVerticalGroup(
            pnl_ThuocTinhLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_ThuocTinhLayout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(pnl_ThuocTinhLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbl_GiaMin3)
                    .addComponent(TF_TT, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rdo_KichThuoc)
                    .addComponent(rdo_LoaiSP)
                    .addComponent(rdo_ChatLieu)
                    .addComponent(rdo_MauSac))
                .addGap(14, 14, 14)
                .addGroup(pnl_ThuocTinhLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btn_ThemTT, javax.swing.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
                    .addComponent(btn_SuaTT, javax.swing.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
                    .addComponent(btn_LamMoiTT, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        tab_sp.addTab("Thuộc tính sản phẩm", pnl_ThuocTinh);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel1.setText("Quản lý sản phẩm");

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Lọc"));

        TF_GiaMin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TF_GiaMinActionPerformed(evt);
            }
        });

        jLabel35.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel35.setText("Lọc theo loại :");

        lbl_GiaMin2.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lbl_GiaMin2.setText("Đơn giá thấp nhất:");

        TF_GiaMax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TF_GiaMaxActionPerformed(evt);
            }
        });

        lbl_GiaMax2.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lbl_GiaMax2.setText("Đơn giá lớn nhất:");

        btn_Loc2.setText("Tìm");
        btn_Loc2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_LocActionPerformed(evt);
            }
        });

        cbb_LTL.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(lbl_GiaMin2)
                .addGap(18, 18, 18)
                .addComponent(TF_GiaMin, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(43, 43, 43)
                .addComponent(lbl_GiaMax2)
                .addGap(18, 18, 18)
                .addComponent(TF_GiaMax, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btn_Loc2, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(jLabel35)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(cbb_LTL, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(120, 120, 120))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbb_LTL, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel35)
                    .addComponent(btn_Loc2, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TF_GiaMax, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbl_GiaMax2)
                    .addComponent(TF_GiaMin, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbl_GiaMin2))
                .addContainerGap(22, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        btn_Them.setIcon(new javax.swing.ImageIcon("D:\\MOB1014\\icons\\add.png")); // NOI18N
        btn_Them.setText("Thêm");
        btn_Them.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_ThemActionPerformed(evt);
            }
        });

        btn_Sua.setIcon(new javax.swing.ImageIcon("D:\\MOB1014\\icons\\edit.png")); // NOI18N
        btn_Sua.setText("Sửa");
        btn_Sua.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_SuaActionPerformed(evt);
            }
        });

        btn_MoKhoa.setText("Mở khoá");
        btn_MoKhoa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_MoKhoaActionPerformed(evt);
            }
        });

        btn_Khoa.setText("Khoá");
        btn_Khoa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_KhoaActionPerformed(evt);
            }
        });

        btn_LamMoi.setText("Làm mới");
        btn_LamMoi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_LamMoiActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btn_Sua, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                        .addComponent(btn_MoKhoa, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btn_Them, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btn_Khoa, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btn_LamMoi, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_Them, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_Khoa, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(39, 39, 39)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_Sua, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_MoKhoa, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addComponent(btn_LamMoi, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(48, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(568, 568, 568))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(62, 62, 62)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 7, Short.MAX_VALUE)
                        .addComponent(tab_sp, javax.swing.GroupLayout.PREFERRED_SIZE, 1174, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(26, 26, 26))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(tab_sp, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void TF_MaKH4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TF_MaKH4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_MaKH4ActionPerformed

    private void tbl_SPkhoaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbl_SPkhoaMouseClicked
        // TODO add your handling code here:
        showDetail(tbl_SPkhoa);
    }//GEN-LAST:event_tbl_SPkhoaMouseClicked

    private void tab_spMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tab_spMouseClicked
        // TODO add your handling code here:

    }//GEN-LAST:event_tab_spMouseClicked

    private void tbl_XemTTMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbl_XemTTMouseClicked
        // TODO add your handling code here:
        showDetail(tbl_XemTT);

    }//GEN-LAST:event_tbl_XemTTMouseClicked

    private void tbl_ThuocTinhMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbl_ThuocTinhMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tbl_ThuocTinhMouseClicked

    private void btn_ThemTTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_ThemTTActionPerformed
        // TODO add your handling code here:
        themTT();
    }//GEN-LAST:event_btn_ThemTTActionPerformed

    private void btn_SuaTTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_SuaTTActionPerformed
        // TODO add your handling code here:
        suaTT();
    }//GEN-LAST:event_btn_SuaTTActionPerformed

    private void rdo_ChatLieuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdo_ChatLieuActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rdo_ChatLieuActionPerformed

    private void btn_MoKhoaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_MoKhoaActionPerformed
        // TODO add your handling code here:
        moKhoa();
    }//GEN-LAST:event_btn_MoKhoaActionPerformed

    private void TF_SoLuongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TF_SoLuongActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_SoLuongActionPerformed

    private void TF_GiaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TF_GiaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_GiaActionPerformed

    private void btn_KhoaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_KhoaActionPerformed
        // TODO add your handling code here:
        khoa();
    }//GEN-LAST:event_btn_KhoaActionPerformed

    private void btn_ThemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_ThemActionPerformed
        // TODO add your handling code here:
        if (TF_MaSP.getText().trim().isEmpty()) {
            TF_MaSP.setText(spdao.taoMaSPMoi());
        }
        if (validateForm()) {
            them();
        }
    }//GEN-LAST:event_btn_ThemActionPerformed

    private void btn_LamMoiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_LamMoiActionPerformed
        // TODO add your handling code here:
        lamMoi();
    }//GEN-LAST:event_btn_LamMoiActionPerformed

    private void btn_SuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_SuaActionPerformed
        // TODO add your handling code here:
        sua();
    }//GEN-LAST:event_btn_SuaActionPerformed

    private void btn_TimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_TimActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btn_TimActionPerformed

    private void TF_MaSPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TF_MaSPActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_MaSPActionPerformed

    private void TF_TenSPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TF_TenSPActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_TenSPActionPerformed

    private void btn_LocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_LocActionPerformed
        // TODO add your handling code here:

        if (pnl_XemTT.isVisible()) {
            locTheoKhoangGia(tbl_XemTT);
        } else if (pnl_SPK.isVisible()) {
            locTheoKhoangGia(tbl_SPkhoa);
        }
    }//GEN-LAST:event_btn_LocActionPerformed

    private void TF_GiaMaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TF_GiaMaxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_GiaMaxActionPerformed

    private void TF_GiaMinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TF_GiaMinActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_GiaMinActionPerformed

    private void btn_LamMoiTTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_LamMoiTTActionPerformed
        // TODO add your handling code here:
        lamMoiTT();
    }//GEN-LAST:event_btn_LamMoiTTActionPerformed

    private void TF_TenSP1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TF_TenSP1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_TenSP1ActionPerformed

    private void btn_LamMoi1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_LamMoi1ActionPerformed
        // TODO add your handling code here:
        chonAnh();
    }//GEN-LAST:event_btn_LamMoi1ActionPerformed

    private void jLabel_AnhMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel_AnhMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel_AnhMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField TF_Gia;
    private javax.swing.JTextField TF_GiaMax;
    private javax.swing.JTextField TF_GiaMin;
    private javax.swing.JTextField TF_MaKH4;
    private javax.swing.JTextField TF_MaSP;
    private javax.swing.JTextField TF_SoLuong;
    private javax.swing.JTextField TF_TT;
    private javax.swing.JTextField TF_TenSP;
    private javax.swing.JTextField TF_TenSP1;
    private javax.swing.JButton btn_Khoa;
    private javax.swing.JButton btn_LamMoi;
    private javax.swing.JButton btn_LamMoi1;
    private javax.swing.JButton btn_LamMoiTT;
    private javax.swing.JButton btn_Loc2;
    private javax.swing.JButton btn_MoKhoa;
    private javax.swing.JButton btn_Sua;
    private javax.swing.JButton btn_SuaTT;
    private javax.swing.JButton btn_Them;
    private javax.swing.JButton btn_ThemTT;
    private javax.swing.JButton btn_Tim;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JComboBox<String> cbb_ChatLieu;
    private javax.swing.JComboBox<String> cbb_KichThuoc;
    private javax.swing.JComboBox<String> cbb_LTL;
    private javax.swing.JComboBox<String> cbb_LoaiSP;
    private javax.swing.JComboBox<String> cbb_MauSac;
    private javax.swing.JComboBox<String> cbb_TrangThai;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel_Anh;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JLabel lbl_GiaMax2;
    private javax.swing.JLabel lbl_GiaMin2;
    private javax.swing.JLabel lbl_GiaMin3;
    private javax.swing.JPanel pnl_SPK;
    private javax.swing.JPanel pnl_ThuocTinh;
    private javax.swing.JPanel pnl_XemTT;
    private javax.swing.JRadioButton rdo_ChatLieu;
    private javax.swing.JRadioButton rdo_KichThuoc;
    private javax.swing.JRadioButton rdo_LoaiSP;
    private javax.swing.JRadioButton rdo_MauSac;
    private javax.swing.JTabbedPane tab_sp;
    private javax.swing.JTable tbl_SPkhoa;
    private javax.swing.JTable tbl_ThuocTinh;
    private javax.swing.JTable tbl_XemTT;
    // End of variables declaration//GEN-END:variables
}
