/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package View;

import DAO.HoaDonChiTietDAO;
import DAO.HoaDonDao;
import Model.HoaDon;
import Service.DBConnect;
import Model.HoaDonChiTiet;
import Service.DBConnect;
import static com.itextpdf.text.SpecialSymbol.index;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.sql.*;
import com.toedter.calendar.JDateChooser;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import static javax.mail.Flags.Flag.USER;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author XPS
 */
public class QLHDCT extends javax.swing.JPanel {

    DefaultTableModel tableHD;    // cho bảng tbl_DSHD
    DefaultTableModel hoadonctmodel;
    HoaDonChiTietDAO hdctdao = new HoaDonChiTietDAO();
    HoaDonDao hddao = new HoaDonDao();
    private String maHD;
    private DefaultListModel<String> suggestionListModel;
    private JList<String> suggestionList;
    private JPopupMenu suggestionPopup;
    private List<String> currentSuggestions;

    /**
     * Creates new form QLHDCT
     */
    public QLHDCT(String maHD) {
        this.maHD = maHD;
        initComponents();
        initTables();
        TF_NgayBD1.setDateFormatString("yyyy-MM-dd");
        TF_NgayBD2.setDateFormatString("yyyy-MM-dd");
        TF_NgayBD1.addPropertyChangeListener("date", evt -> timKiemHoaDon());
        TF_NgayBD2.addPropertyChangeListener("date", evt -> timKiemHoaDon());

        // Khởi tạo các biến instance đúng cách (không khai báo lại bằng kiểu dữ liệu nữa!)
        suggestionListModel = new DefaultListModel<>();
        suggestionList = new JList<>(suggestionListModel);
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setFocusable(false);

        suggestionPopup = new JPopupMenu();
        suggestionPopup.add(new JScrollPane(suggestionList));
        suggestionPopup.setFocusable(false);

        currentSuggestions = new ArrayList<>();

        // Thêm DocumentListener cho TF_MaKH1
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
                // Không dùng với PlainDocument
            }
        });

        // KeyListener cho TF_MaKH1
        TF_MaKH1.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (!suggestionListModel.isEmpty()) {
                        suggestionList.requestFocusInWindow();
                        suggestionList.setSelectedIndex(0);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (suggestionPopup.isVisible() && !suggestionList.isSelectionEmpty()) {
                        applySelectedSuggestion();
                        e.consume();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hideSuggestions();
                }
            }
        });

        // KeyListener cho suggestionList
        suggestionList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    applySelectedSuggestion();
                    hideSuggestions();
                    TF_MaKH1.requestFocusInWindow();
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hideSuggestions();
                    TF_MaKH1.requestFocusInWindow();
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    if (suggestionList.getSelectedIndex() == 0) {
                        TF_MaKH1.requestFocusInWindow();
                        suggestionList.clearSelection();
                        e.consume();
                    }
                }
            }
        });

        // MouseListener cho suggestionList
        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    applySelectedSuggestion();
                    hideSuggestions();
                    TF_MaKH1.requestFocusInWindow();
                }
            }
        });
    }

    private void initTables() {
        initTablehdonct();
        fillTablehdonct(maHD);
        initTableHD();
        fillTableHD();
        cboTrangThai.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    timKiemHoaDon();
                }
            }
        });

        cboHinhThucGiaoHang.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    timKiemHoaDon();
                }
            }
        });

        cboHinhThucThanhToan.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    timKiemHoaDon();
                }
            }
        });

    }

    private void showSuggestions() {
        String text = TF_MaKH1.getText().trim();
        if (text.isEmpty()) {
            hideSuggestions();
            return;
        }

        currentSuggestions.clear();
        suggestionListModel.clear();

        try (Connection conn = DBConnect.getConnection()) {
            if (conn == null) {
                return;
            }

            String sql = "SELECT DISTINCT SDT, TenKH FROM HoaDon WHERE SDT LIKE ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, "%" + text + "%");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String sdt = rs.getString("SDT");
                    String tenKH = rs.getString("TenKH");
                    String suggestion = sdt + " - " + tenKH;

                    currentSuggestions.add(suggestion);
                    suggestionListModel.addElement(suggestion);
                }
            }

            if (!currentSuggestions.isEmpty()) {
                suggestionList.setSelectedIndex(0);
                suggestionPopup.show(TF_MaKH1, 0, TF_MaKH1.getHeight());
            } else {
                hideSuggestions();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void applySelectedSuggestion() {
        String selected = suggestionList.getSelectedValue();
        if (selected != null && selected.contains(" - ")) {
            String sdt = selected.split(" - ")[0]; // Lấy phần SDT
            TF_MaKH1.setText(sdt);                 // Gán vào ô nhập
            hideSuggestions();

            timKiemHoaDon();                       // Gọi hàm lọc bảng hóa đơn
        }
    }

// Ẩn popup gợi ý
    private void hideSuggestions() {
        suggestionPopup.setVisible(false);
    }

    public void timKiemHoaDon() {
        String trangThai = cboTrangThai.getSelectedItem().toString();
        String giaoHang = cboHinhThucGiaoHang.getSelectedItem().toString();
        String thanhToan = cboHinhThucThanhToan.getSelectedItem().toString();
        String sdt = TF_MaKH1.getText().trim();
        Date batDau = TF_NgayBD1.getDate();
        Date ketThuc = TF_NgayBD2.getDate();

        // Kiểm tra ngày bắt đầu > ngày kết thúc
        if (batDau != null && ketThuc != null && batDau.after(ketThuc)) {
            JOptionPane.showMessageDialog(null, "Ngày bắt đầu không thể lớn hơn ngày kết thúc!");
            return;
        }

        DefaultTableModel model = (DefaultTableModel) tbl_DSHD.getModel();
        model.setRowCount(0);

        try (Connection conn = DBConnect.getConnection()) {
            StringBuilder sql = new StringBuilder("""
            SELECT hd.MaHD, hd.MaNV, hd.TenKH, hd.SDT, hd.TrangThai, 
                   hd.NgayTao, hd.TongTien, hd.TienTra, hd.TienThua,
                   hd.ThanhToan, hd.GiaoHang, hd.GhiChu, nv.TenNV 
            FROM HoaDon hd
            LEFT JOIN NhanVien nv ON hd.MaNV = nv.MaNV
            WHERE 1=1""");

            // Thêm điều kiện tìm kiếm
            if (!trangThai.equals("Tất cả")) {
                sql.append(" AND hd.TrangThai = ?");
            }
            if (!giaoHang.equals("Tất cả")) {
                sql.append(" AND hd.GiaoHang = ?");
            }
            if (!thanhToan.equals("Tất cả")) {
                sql.append(" AND hd.ThanhToan = ?");
            }
            if (!sdt.isEmpty()) {
                sql.append(" AND hd.SDT LIKE ?");
            }

            // Xử lý điều kiện ngày tháng
            if (batDau != null && ketThuc != null) {
                sql.append(" AND hd.NgayTao BETWEEN ? AND ?");
            } else if (batDau != null) {
                sql.append(" AND hd.NgayTao >= ?");
            } else if (ketThuc != null) {
                sql.append(" AND hd.NgayTao <= ?");
            }

            sql.append(" ORDER BY hd.MaHD ASC");

            PreparedStatement ps = conn.prepareStatement(sql.toString());

            int paramIndex = 1;
            if (!trangThai.equals("Tất cả")) {
                ps.setString(paramIndex++, trangThai);
            }
            if (!giaoHang.equals("Tất cả")) {
                ps.setString(paramIndex++, giaoHang);
            }
            if (!thanhToan.equals("Tất cả")) {
                ps.setString(paramIndex++, thanhToan);
            }
            if (!sdt.isEmpty()) {
                ps.setString(paramIndex++, "%" + sdt + "%");
            }

            // Xử lý tham số ngày tháng - sử dụng java.sql.Date
            if (batDau != null && ketThuc != null) {
                ps.setDate(paramIndex++, new java.sql.Date(batDau.getTime()));
                ps.setDate(paramIndex++, new java.sql.Date(ketThuc.getTime()));
            } else if (batDau != null) {
                ps.setDate(paramIndex++, new java.sql.Date(batDau.getTime()));
            } else if (ketThuc != null) {
                ps.setDate(paramIndex++, new java.sql.Date(ketThuc.getTime()));
            }

            System.out.println("SQL: " + ps.toString()); // Debug

            ResultSet rs = ps.executeQuery();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            while (rs.next()) {
                // Xử lý ngày từ ResultSet
                java.sql.Date ngayTaoSQL = rs.getDate("NgayTao");
                String ngayTaoStr = (ngayTaoSQL != null) ? dateFormat.format(ngayTaoSQL) : "";

                Object[] row = new Object[]{
                    rs.getString("MaHD"),
                    rs.getString("MaNV"),
                    rs.getString("TenKH"),
                    rs.getString("SDT"),
                    rs.getString("TrangThai"),
                    ngayTaoStr, // Sử dụng ngày đã định dạng
                    String.format("%,.0f", rs.getDouble("TongTien")),
                    String.format("%,.0f", rs.getDouble("TienTra")),
                    String.format("%,.0f", rs.getDouble("TienThua")),
                    rs.getString("ThanhToan"),
                    rs.getString("GiaoHang"),
                    rs.getString("GhiChu")
                };
                model.addRow(row);
            }

            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(null, "Không tìm thấy hóa đơn nào phù hợp!");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi tìm kiếm hóa đơn: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void initTablehdonct() {
        String[] cols = new String[]{
            "Mã chi tiết hoá đơn", "Mã hoá đơn", "Mã sản phẩm", "Tên sản phẩm",
            "Số lượng", "Đơn giá", "Giảm giá", "Mã khuyến mãi", "Thành tiền"
        };
        hoadonctmodel = new DefaultTableModel();
        hoadonctmodel.setColumnIdentifiers(cols);
        tbl_CTHD.setModel(hoadonctmodel);
    }

    public void fillTablehdonct(String maHD) {
        hoadonctmodel.setRowCount(0);
        if (maHD == null || maHD.isEmpty()) {
            return;
        }

        // Lấy danh sách Object[] từ getAllWithMaKM
        List<Object[]> dataList = hdctdao.getAllWithMaKM(maHD);

        for (Object[] row : dataList) {
            hoadonctmodel.addRow(row);
        }
    }

    public void initTableHD() {
        String[] cols = new String[]{"Mã HD", "Mã NV", "Tên KH", "SĐT", "Trạng thái", "Ngày tạo", "Tổng tiền", "Tiền trả", "Tiền thừa", "Thanh toán", "Giao hàng", "Ghi chú"};
        tableHD = new DefaultTableModel();
        tableHD.setColumnIdentifiers(cols);
        tbl_DSHD.setModel(tableHD);
    }

    public void fillTableHD() {
        tableHD.setRowCount(0);
        for (HoaDon hd : hddao.getAll123()) {
            tableHD.addRow(hddao.getRow(hd));
        }
    }

    public void timHDTheoma() {
        String mahd = TF_MaHD.getText().trim();

        if (mahd.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã hóa đơn cần tìm !");
            return;
        }

        HoaDon hd = hddao.timHDTheoma1(mahd);

        // Lấy model bảng hóa đơn và chi tiết hóa đơn
        DefaultTableModel HD = (DefaultTableModel) tbl_DSHD.getModel();
        DefaultTableModel HDCT = (DefaultTableModel) tbl_CTHD.getModel();

        HD.setRowCount(0); // Xóa bảng hóa đơn
        HDCT.setRowCount(0); // Xóa bảng chi tiết

        if (hd != null) {
            Object[] rowHD = new Object[]{
                hd.getMahd(), hd.getManv(), hd.getTenkh(), hd.getSdt(),
                hd.getTrangThai(), hd.getNgayTao(), hd.getTongTien(),
                hd.getTienTra(), hd.getTienThua(), hd.getThanhToan(),
                hd.getGiaoHang(), hd.getGhiChu()
            };
            HD.addRow(rowHD); // Thêm dòng vào bảng hóa đơn

            List<HoaDonChiTiet> ds = hdctdao.timHDTheoma2(mahd);
            for (HoaDonChiTiet hdct : ds) {
                Object[] rowHDCT = new Object[]{
                    hdct.getMacthd(), hdct.getMahd(), hdct.getMasp(), hdct.getTensp(),
                    hdct.getSluong(), hdct.getDongia(), hdct.getGiamGia(), hdct.getThanhTien()
                };
                HDCT.addRow(rowHDCT); // Thêm dòng vào bảng chi tiết
            }
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn có mã: " + mahd);
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

        TF_NgayBD = new com.toedter.calendar.JDateChooser();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbl_DSHD = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbl_CTHD = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        pnl_TimKiem = new javax.swing.JPanel();
        lbl_MaHD = new javax.swing.JLabel();
        TF_MaHD = new javax.swing.JTextField();
        btn_Tim = new javax.swing.JButton();
        btn_Tim1 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        lbl_MaHD1 = new javax.swing.JLabel();
        cboTrangThai = new javax.swing.JComboBox<>();
        jPanel2 = new javax.swing.JPanel();
        lbl_MaHD2 = new javax.swing.JLabel();
        cboHinhThucGiaoHang = new javax.swing.JComboBox<>();
        jPanel3 = new javax.swing.JPanel();
        lbl_MaHD4 = new javax.swing.JLabel();
        cboHinhThucThanhToan = new javax.swing.JComboBox<>();
        jPanel4 = new javax.swing.JPanel();
        lbl_MaHD5 = new javax.swing.JLabel();
        lbl_MaHD6 = new javax.swing.JLabel();
        TF_NgayBD1 = new com.toedter.calendar.JDateChooser();
        TF_NgayBD2 = new com.toedter.calendar.JDateChooser();
        jPanel5 = new javax.swing.JPanel();
        lbl_MaHD3 = new javax.swing.JLabel();
        TF_MaKH1 = new javax.swing.JTextField();

        TF_NgayBD.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TF_NgayBDMouseClicked(evt);
            }
        });

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel1.setText("Hoá đơn chi tiết");

        tbl_DSHD.setModel(new javax.swing.table.DefaultTableModel(
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
        tbl_DSHD.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbl_DSHDMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tbl_DSHD);

        jLabel2.setText("Danh sách hoá đơn");

        tbl_CTHD.setModel(new javax.swing.table.DefaultTableModel(
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
        tbl_CTHD.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbl_CTHDMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tbl_CTHD);

        jLabel3.setText("Chi tiết hoá đơn");

        pnl_TimKiem.setBorder(javax.swing.BorderFactory.createTitledBorder("Tìm kiếm"));

        lbl_MaHD.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lbl_MaHD.setText("Mã hoá đơn :");

        TF_MaHD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TF_MaHDActionPerformed(evt);
            }
        });

        btn_Tim.setText("Tìm");
        btn_Tim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_TimActionPerformed(evt);
            }
        });

        btn_Tim1.setText("Làm mời");
        btn_Tim1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_Tim1ActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lbl_MaHD1.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lbl_MaHD1.setText("Trạng thái thanh toán");

        cboTrangThai.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Tất cả", "Đang xử lý", "Đã thanh toán", "Đang giao", "Đã giao hàng", "Đã huỷ" }));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(14, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(lbl_MaHD1)
                        .addGap(31, 31, 31))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(cboTrangThai, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(15, 15, 15))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbl_MaHD1)
                .addGap(28, 28, 28)
                .addComponent(cboTrangThai, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(23, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lbl_MaHD2.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lbl_MaHD2.setText("Hình thức giao hàng");

        cboHinhThucGiaoHang.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Tất cả", "Tại quán", "Giao hàng" }));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(lbl_MaHD2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(15, Short.MAX_VALUE)
                .addComponent(cboHinhThucGiaoHang, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbl_MaHD2)
                .addGap(29, 29, 29)
                .addComponent(cboHinhThucGiaoHang, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lbl_MaHD4.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lbl_MaHD4.setText("Hình thức thanh toán");

        cboHinhThucThanhToan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Tất cả", "Tiền mặt", "Chuyển khoản", "Quẹt thẻ" }));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(lbl_MaHD4))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(cboHinhThucThanhToan, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbl_MaHD4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(cboHinhThucThanhToan, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lbl_MaHD5.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lbl_MaHD5.setText("Ngày bắt đầu ");

        lbl_MaHD6.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lbl_MaHD6.setText("Ngày kết thúc");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(TF_NgayBD1, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(lbl_MaHD5)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(lbl_MaHD6)
                        .addGap(20, 20, 20))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(TF_NgayBD2, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbl_MaHD5)
                    .addComponent(lbl_MaHD6))
                .addGap(26, 26, 26)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(TF_NgayBD1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TF_NgayBD2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lbl_MaHD3.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lbl_MaHD3.setText("Số điện thoại KH ");

        TF_MaKH1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TF_MaKH1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(lbl_MaHD3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(16, Short.MAX_VALUE)
                .addComponent(TF_MaKH1, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbl_MaHD3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                .addComponent(TF_MaKH1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26))
        );

        javax.swing.GroupLayout pnl_TimKiemLayout = new javax.swing.GroupLayout(pnl_TimKiem);
        pnl_TimKiem.setLayout(pnl_TimKiemLayout);
        pnl_TimKiemLayout.setHorizontalGroup(
            pnl_TimKiemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_TimKiemLayout.createSequentialGroup()
                .addGap(64, 64, 64)
                .addComponent(lbl_MaHD)
                .addGap(38, 38, 38)
                .addComponent(TF_MaHD, javax.swing.GroupLayout.PREFERRED_SIZE, 926, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btn_Tim, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addComponent(btn_Tim1, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(pnl_TimKiemLayout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(44, 44, 44)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(17, 17, 17))
        );
        pnl_TimKiemLayout.setVerticalGroup(
            pnl_TimKiemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_TimKiemLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnl_TimKiemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_MaHD, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbl_MaHD)
                    .addComponent(btn_Tim)
                    .addComponent(btn_Tim1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnl_TimKiemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnl_TimKiemLayout.createSequentialGroup()
                        .addGroup(pnl_TimKiemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnl_TimKiemLayout.createSequentialGroup()
                        .addGroup(pnl_TimKiemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 25, Short.MAX_VALUE))))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(609, 609, 609)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(36, 44, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2))
                        .addGap(1353, 1353, 1353))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pnl_TimKiem, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(pnl_TimKiem, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(56, 56, 56))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void TF_MaHDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TF_MaHDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_MaHDActionPerformed

    private void btn_TimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_TimActionPerformed
        // TODO add your handling code here:
        timHDTheoma();
    }//GEN-LAST:event_btn_TimActionPerformed

    private void tbl_DSHDMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbl_DSHDMouseClicked
        // TODO add your handling code here:
        int row = tbl_DSHD.getSelectedRow();
        if (row >= 0) {
            String maHD = tbl_DSHD.getValueAt(row, 0).toString();
            fillTablehdonct(maHD); // Cập nhật bảng chi tiết khi chọn hóa đơn
        }
    }//GEN-LAST:event_tbl_DSHDMouseClicked

    private void tbl_CTHDMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbl_CTHDMouseClicked
        // TODO add your handling code here:

    }//GEN-LAST:event_tbl_CTHDMouseClicked

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        // TODO add your handling code here:
        tbl_CTHD.clearSelection();
        tbl_DSHD.clearSelection(); // khi mất focus thì bỏ chọn dòng

    }//GEN-LAST:event_formMouseClicked

    private void btn_Tim1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_Tim1ActionPerformed
        // TODO add your handling code here:
        TF_MaHD.setText("");
        cboTrangThai.setSelectedItem("Tất cả");
        cboHinhThucGiaoHang.setSelectedItem("Tất cả");
        cboHinhThucThanhToan.setSelectedItem("Tất cả");

        TF_MaKH1.setText("");
        TF_NgayBD1.setDate(null);
        TF_NgayBD2.setDate(null);
        fillTableHD();
        fillTablehdonct(maHD);
    }//GEN-LAST:event_btn_Tim1ActionPerformed

    private void TF_MaKH1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TF_MaKH1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_MaKH1ActionPerformed

    private void TF_NgayBDMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TF_NgayBDMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_NgayBDMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField TF_MaHD;
    private javax.swing.JTextField TF_MaKH1;
    private com.toedter.calendar.JDateChooser TF_NgayBD;
    private com.toedter.calendar.JDateChooser TF_NgayBD1;
    private com.toedter.calendar.JDateChooser TF_NgayBD2;
    private javax.swing.JButton btn_Tim;
    private javax.swing.JButton btn_Tim1;
    private javax.swing.JComboBox<String> cboHinhThucGiaoHang;
    private javax.swing.JComboBox<String> cboHinhThucThanhToan;
    private javax.swing.JComboBox<String> cboTrangThai;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lbl_MaHD;
    private javax.swing.JLabel lbl_MaHD1;
    private javax.swing.JLabel lbl_MaHD2;
    private javax.swing.JLabel lbl_MaHD3;
    private javax.swing.JLabel lbl_MaHD4;
    private javax.swing.JLabel lbl_MaHD5;
    private javax.swing.JLabel lbl_MaHD6;
    private javax.swing.JPanel pnl_TimKiem;
    private javax.swing.JTable tbl_CTHD;
    private javax.swing.JTable tbl_DSHD;
    // End of variables declaration//GEN-END:variables
}
