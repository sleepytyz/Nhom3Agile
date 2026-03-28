/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package View;

import DAO.SanPhamDAO;
import DAO.ThuocTinhDAO;
import Model.SanPham;
import Model.SanPhamGoiY;
import Model.SanPhamGoiYRenderer;
import Model.ThuocTinh;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author XPS
 */
public class MENU22 extends javax.swing.JPanel {

    private int currentPage = 1;
    private final int itemsPerPage = 4;
    private List<SanPham> danhSach;
    private final SanPhamDAO dao = new SanPhamDAO();
    JPanel panelChuaSanPham = new JPanel();

    private List<JLabel> lblTenList;
    private List<JLabel> lblAnhList;
    private List<JLabel> lblGiaList;
    private List<JLabel> lblSoLuongList;
    JScrollPane scrollPaneSanPham = new JScrollPane(panelChuaSanPham);

    List<SanPhamGoiY> currentGoiYSP = new ArrayList<>();
    DefaultListModel<SanPhamGoiY> modelGoiYSP = new DefaultListModel<>();
    JList<SanPhamGoiY> listGoiYSP = new JList<>(modelGoiYSP);

    JPopupMenu popupGoiYSP = new JPopupMenu();
    ThuocTinhDAO ttdao = new ThuocTinhDAO();
    List<ThuocTinh> listLoai = ttdao.getAllLoaiSanPham();
    JComboBox<ThuocTinh> cboLoai = new JComboBox<>();

    public MENU22() {
        initComponents(); // <-- Bạn đã thiết kế sẵn trong giao diện Form
        this.danhSach = dao.getAll1();

        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement(new ThuocTinh("", "Tất cả"));
        for (ThuocTinh tt : listLoai) {
            model.addElement(tt);
        }
        cboLoai1.setModel(model); // Không còn lỗi

        ThuocTinh selectedLoai = (ThuocTinh) cboLoai1.getSelectedItem();
        if (selectedLoai != null) {
            String maLoai = selectedLoai.getMaTT();
            // dùng maLoai để lọc sản phẩm
        }

        JPanel panelChuaSanPham = new JPanel();
        panelChuaSanPham.setLayout(new GridLayout(0, 2, 10, 10));

        int scrollValue = scrollPaneSanPham.getVerticalScrollBar().getValue();
        panelChuaSanPham.removeAll();
        for (int i = 0; i < 4 && i < danhSach.size(); i++) {
            SanPham sp = danhSach.get(i);
            JLabel lblTen = new JLabel(sp.getTensp());
            panelChuaSanPham.add(lblTen);
        }
        panelChuaSanPham.revalidate();
        panelChuaSanPham.repaint();
        scrollPaneSanPham.getVerticalScrollBar().setValue(scrollValue);
        scrollPaneSanPham.getVerticalScrollBar().setValue(scrollValue);

        listGoiYSP.setCellRenderer(new SanPhamGoiYRenderer());

        popupGoiYSP.add(new JScrollPane(listGoiYSP));
        popupGoiYSP.setFocusable(false);
        lblAnhList = Arrays.asList(lblAnh1, lblAnh2, lblAnh3, lblAnh4);
        lblTenList = Arrays.asList(lblTen1, lblTen2, lblTen3, lblTen4);
        lblGiaList = Arrays.asList(lblGia1, lblGia2, lblGia3, lblGia4);
        lblSoLuongList = Arrays.asList(lblSL1, lblSL2, lblSL3, lblSL4);

        btnNext.addActionListener(e -> {
            int totalPages = (int) Math.ceil((double) danhSach.size() / itemsPerPage);
            if (currentPage < totalPages) {
                currentPage++;
            } else {
                currentPage = 1; // Nếu đang ở trang cuối thì quay về trang 1
            }
            hienThiTrang(currentPage);
        });

        btnPrev.addActionListener(e -> {
            int totalPages = (int) Math.ceil((double) danhSach.size() / itemsPerPage);
            if (currentPage > 1) {
                currentPage--;
            } else {
                currentPage = totalPages; // Nếu đang ở trang đầu thì quay về trang cuối
            }
            hienThiTrang(currentPage);
        });

        // Load dữ liệu từ database
        danhSach = dao.getAll1();
        hienThiTrang(currentPage);

        TF_TimTenSP.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                hienThiGoiYSP();
            }

            public void removeUpdate(DocumentEvent e) {
                hienThiGoiYSP();
            }

            public void changedUpdate(DocumentEvent e) {
            }
        });

        TF_TimTenSP.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN && !modelGoiYSP.isEmpty()) {
                    listGoiYSP.requestFocusInWindow();
                    listGoiYSP.setSelectedIndex(0);
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER && popupGoiYSP.isVisible()) {
                    chonTenSanPham();
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    popupGoiYSP.setVisible(false);
                }
            }
        });

        listGoiYSP.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    chonTenSanPham();
                    popupGoiYSP.setVisible(false);
                    TF_TimTenSP.requestFocusInWindow();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    popupGoiYSP.setVisible(false);
                    TF_TimTenSP.requestFocusInWindow();
                } else if (e.getKeyCode() == KeyEvent.VK_UP && listGoiYSP.getSelectedIndex() == 0) {
                    TF_TimTenSP.requestFocusInWindow();
                    listGoiYSP.clearSelection();
                    e.consume();
                }
            }
        });

        listGoiYSP.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    chonTenSanPham();
                    popupGoiYSP.setVisible(false);
                    TF_TimTenSP.requestFocusInWindow();
                }
            }
        });

        jPanel4.setPreferredSize(new Dimension(300, 500));
        jPanel2.setPreferredSize(new Dimension(300, 500));
        jPanel7.setPreferredSize(new Dimension(300, 500));
        jPanel8.setPreferredSize(new Dimension(300, 500));
        JPanel containerPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        containerPanel.add(jPanel4);
        containerPanel.add(jPanel2);
        containerPanel.add(jPanel7);
        containerPanel.add(jPanel8);

        cboLoai1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ThuocTinh tt = (ThuocTinh) cboLoai1.getSelectedItem();
                if (tt != null) {
                    String tenLoai = tt.getTenTT(); // ✅ ví dụ: "Áo", "Quần", "Mũ"
                    System.out.println("Đã chọn loại: " + tenLoai);

                    if (tenLoai.equals("") || tenLoai.equalsIgnoreCase("Tất cả")) {
                        danhSach = dao.getAll1();
                    } else {
                        danhSach = dao.getByLoai(tenLoai); // ✅ Dùng tên loại để truy vấn
                    }
                    currentPage = 1;
                    hienThiTrang(currentPage);
                }
            }
        });

    }

    private void fillDataToCard() {
        // Danh sách các JLabel
        JLabel[] arrTen = {lblTen1, lblTen2, lblTen3, lblTen4};
        JLabel[] arrGia = {lblGia1, lblGia2, lblGia3, lblGia4};
        JLabel[] arrSL = {lblSL1, lblSL2, lblSL3, lblSL4};
        JLabel[] arrAnh = {lblAnh1, lblAnh2, lblAnh3, lblAnh4};

        // Xóa dữ liệu cũ và hiển thị dữ liệu mới
        for (int i = 0; i < 4; i++) {
            if (i < danhSach.size()) {
                SanPham sp = danhSach.get(i);
                arrTen[i].setText(sp.getTensp());
                arrGia[i].setText(String.format("Giá: %,d VND", (int) sp.getGia()));
                arrSL[i].setText("SL: " + sp.getSluong());

                // Xử lý hiển thị ảnh
                if (sp.getHinhAnh() != null && sp.getHinhAnh().length > 0) {
                    try {
                        ImageIcon icon = new ImageIcon(sp.getHinhAnh());
                        Image img = icon.getImage().getScaledInstance(280, 320, Image.SCALE_SMOOTH);
                        arrAnh[i].setIcon(new ImageIcon(img));
                        arrAnh[i].setText("");
                    } catch (Exception e) {
                        arrAnh[i].setIcon(null);
                        arrAnh[i].setText("Lỗi ảnh");
                    }
                } else {
                    arrAnh[i].setIcon(null);
                    arrAnh[i].setText("Không có ảnh");
                }
            } else {
                // Xóa dữ liệu nếu không có sản phẩm
                arrTen[i].setText("");
                arrGia[i].setText("");
                arrSL[i].setText("");
                arrAnh[i].setIcon(null);
                arrAnh[i].setText("");
            }
        }

        // Cập nhật số trang
        int totalPages = (int) Math.ceil((double) danhSach.size() / itemsPerPage);
        lblPage.setText("Trang " + currentPage + "/" + totalPages);
    }

    public static String removeAccent(String s) {
        String temp = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        return temp.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private void hienThiSanPhamTheoTen(String keyword) {
        int count = 0;
        keyword = keyword.toLowerCase();

        for (int i = 0; i < lblTenList.size(); i++) {
            lblTenList.get(i).setText("");
            lblGiaList.get(i).setText("");
            lblSoLuongList.get(i).setText("");
            lblAnhList.get(i).setIcon(null);
        }

        for (SanPham sp : danhSach) {
            if (removeAccent(sp.getTensp().toLowerCase()).contains(removeAccent(keyword))) {
                if (count >= lblTenList.size()) {
                    break;
                }

                lblTenList.get(count).setText(sp.getTensp());
                lblGiaList.get(count).setText(" " + sp.getGia());
                lblSoLuongList.get(count).setText(" " + sp.getSluong());

                byte[] hinh = sp.getHinhAnh();
                if (hinh != null) {
                    ImageIcon icon = new ImageIcon(hinh);
                    Image img = icon.getImage().getScaledInstance(280, 320, Image.SCALE_SMOOTH);
                    lblAnhList.get(count).setIcon(new ImageIcon(img));
                } else {
                    lblAnhList.get(count).setIcon(null);
                }

                count++;
            }
        }
    }

    private void chonTenSanPham() {
        SanPhamGoiY selected = listGoiYSP.getSelectedValue();
        if (selected != null) {
            TF_TimTenSP.setText(selected.getTen());
            popupGoiYSP.setVisible(false);

            hienThiSanPhamTheoTen(selected.getTen()); // vẫn dùng tên đã chọn làm keyword để lọc nhiều sản phẩm
        }
    }

    private void hienThiGoiYSP() {
        String input = TF_TimTenSP.getText().trim();
        modelGoiYSP.clear();
        currentGoiYSP.clear();

        if (!input.isEmpty()) {
            currentGoiYSP = dao.goiYTenSanPham(input);
            for (SanPhamGoiY sp : currentGoiYSP) {
                modelGoiYSP.addElement(sp);
            }

            if (!currentGoiYSP.isEmpty()) {
                if (!popupGoiYSP.isVisible()) {
                    popupGoiYSP.show(TF_TimTenSP, 0, TF_TimTenSP.getHeight());
                }
            } else {
                popupGoiYSP.setVisible(false);
            }
        } else {
            popupGoiYSP.setVisible(false);
        }

        // ❌ Không cần requestFocus mỗi lần nhập
        // TF_TimTenSP.requestFocusInWindow();
    }

    private void hienThiTrang(int page) {
        int start = (page - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, danhSach.size());

        for (int i = 0; i < itemsPerPage; i++) {
            if (start + i < danhSach.size()) {
                SanPham sp = danhSach.get(start + i);

                lblTenList.get(i).setText(sp.getTensp());
                lblGiaList.get(i).setText(" " + String.format("%,d", (int) sp.getGia()) + " VND");
                lblSoLuongList.get(i).setText(" " + sp.getSluong());

                byte[] hinh = sp.getHinhAnh();

                // In log ra console để kiểm tra dữ liệu ảnh
                System.out.println("SP: " + sp.getTensp() + " | Ảnh: " + (hinh != null ? hinh.length + " bytes" : "null"));

                if (hinh != null && hinh.length > 0) {
                    ImageIcon icon = new ImageIcon(hinh);
                    Image scaled = icon.getImage().getScaledInstance(280, 320, Image.SCALE_SMOOTH);
                    lblAnhList.get(i).setIcon(new ImageIcon(scaled));
                    lblAnhList.get(i).setText("");
                } else {
                    lblAnhList.get(i).setIcon(null);
                    lblAnhList.get(i).setText("Không có ảnh");
                }
            } else {
                lblTenList.get(i).setText("");
                lblGiaList.get(i).setText("");
                lblSoLuongList.get(i).setText("");
                lblAnhList.get(i).setIcon(null);
                lblAnhList.get(i).setText("");
            }
        }

        // 👉 Cập nhật số trang ở lblPage
        int totalPages = (int) Math.ceil((double) danhSach.size() / itemsPerPage);
        lblPage.setText("Trang " + currentPage + " / " + totalPages);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jMenuItem1 = new javax.swing.JMenuItem();
        jLabel16 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        lblAnh2 = new javax.swing.JLabel();
        lblTen2 = new javax.swing.JLabel();
        lblSL2 = new javax.swing.JLabel();
        lblGia2 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        lblAnh1 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        lblTen1 = new javax.swing.JLabel();
        lblGia1 = new javax.swing.JLabel();
        lblSL1 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        cboLoai1 = new javax.swing.JComboBox<>();
        jLabel36 = new javax.swing.JLabel();
        TF_TimTenSP = new javax.swing.JTextField();
        jPanel7 = new javax.swing.JPanel();
        lblAnh3 = new javax.swing.JLabel();
        lblTen3 = new javax.swing.JLabel();
        lblSL3 = new javax.swing.JLabel();
        lblGia3 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        lblAnh4 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        lblTen4 = new javax.swing.JLabel();
        lblGia4 = new javax.swing.JLabel();
        lblSL4 = new javax.swing.JLabel();
        btnPrev1 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        btnPrev = new javax.swing.JButton();
        lblPage = new javax.swing.JLabel();
        btnNext = new javax.swing.JButton();

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 263, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 411, Short.MAX_VALUE)
        );

        jMenuItem1.setText("jMenuItem1");

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel16.setText("Danh sách sản phẩm");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblAnh2.setText("jLabel1");

        lblTen2.setText(".");

        lblSL2.setText(".");

        lblGia2.setText(".");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel14.setText("Tên sản phẩm :");

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel15.setText("Số lượng :");

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel17.setText("Đơn giá :");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblAnh2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel15)
                                .addComponent(jLabel14))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(40, 40, 40)
                                .addComponent(jLabel17)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblSL2, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblTen2, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 68, Short.MAX_VALUE))
                            .addComponent(lblGia2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblAnh2, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(lblTen2))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(lblSL2))
                .addGap(14, 14, 14)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(lblGia2))
                .addContainerGap(34, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblAnh1.setText("jLabel1");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setText("Tên sản phẩm :");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setText("Đơn giá :");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setText("Số lượng :");

        lblTen1.setText(".");

        lblGia1.setText(".");

        lblSL1.setText(".");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblAnh1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblTen1, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblSL1, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 54, Short.MAX_VALUE))
                            .addComponent(lblGia1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(lblAnh1, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(lblTen1))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(lblSL1))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(lblGia1))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        jLabel35.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel35.setText("Lọc theo loại :");

        cboLoai1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-- Tất cả --", "Áo", "Váy", "Quần" }));
        cboLoai1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboLoai1ActionPerformed(evt);
            }
        });

        jLabel36.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel36.setText("Tên sản phẩm :");

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblAnh3.setText("jLabel1");

        lblTen3.setText(".");

        lblSL3.setText(".");

        lblGia3.setText(".");

        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel22.setText("Tên sản phẩm :");

        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel23.setText("Số lượng :");

        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel24.setText("Đơn giá :");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(jLabel24)
                        .addGap(18, 18, 18)
                        .addComponent(lblGia3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(lblAnh3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel23)
                            .addComponent(jLabel22))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTen3, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblSL3, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 54, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblAnh3, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(lblTen3))
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(lblSL3))
                .addGap(14, 14, 14)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(lblGia3))
                .addContainerGap(34, Short.MAX_VALUE))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblAnh4.setText("jLabel1");

        jLabel26.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel26.setText("Tên sản phẩm :");

        jLabel27.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel27.setText("Đơn giá :");

        jLabel28.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel28.setText("Số lượng :");

        lblTen4.setText(".");

        lblGia4.setText(".");

        lblSL4.setText(".");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblAnh4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel26)
                            .addComponent(jLabel27)
                            .addComponent(jLabel28))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblTen4, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblSL4, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 40, Short.MAX_VALUE))
                            .addComponent(lblGia4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(lblAnh4, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26)
                    .addComponent(lblTen4))
                .addGap(18, 18, 18)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28)
                    .addComponent(lblSL4))
                .addGap(18, 18, 18)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(lblGia4))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        btnPrev1.setText("Làm mới");
        btnPrev1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrev1ActionPerformed(evt);
            }
        });

        btnPrev.setIcon(new javax.swing.ImageIcon("D:\\MOB1014\\icons\\rewind.png")); // NOI18N
        btnPrev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrevActionPerformed(evt);
            }
        });

        lblPage.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblPage.setText("jLabel1");

        btnNext.setIcon(new javax.swing.ImageIcon("D:\\MOB1014\\icons\\fast_forward.png")); // NOI18N
        btnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnPrev, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addComponent(lblPage)
                .addGap(18, 18, 18)
                .addComponent(btnNext, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(10, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addComponent(lblPage)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(27, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnNext, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPrev, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(48, 48, 48)
                                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(60, 60, 60)
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(87, 87, 87)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel36)
                                        .addGap(33, 33, 33)
                                        .addComponent(TF_TimTenSP, javax.swing.GroupLayout.PREFERRED_SIZE, 380, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(btnPrev1, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(65, 65, 65)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel35)
                                .addGap(47, 47, 47)
                                .addComponent(cboLoai1, javax.swing.GroupLayout.PREFERRED_SIZE, 326, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(62, 62, 62)
                                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(46, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(TF_TimTenSP, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel36)
                            .addComponent(cboLoai1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel35))
                        .addGap(10, 10, 10)
                        .addComponent(btnPrev1, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(145, 145, 145)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(35, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(560, 560, 560)
                .addComponent(jLabel16)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 66, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_btnNextActionPerformed

    private void btnPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_btnPrevActionPerformed

    private void cboLoai1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboLoai1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cboLoai1ActionPerformed

    private void btnPrev1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrev1ActionPerformed
        // TODO add your handling code here:
        TF_TimTenSP.setText("");
        popupGoiYSP.setVisible(false);
        currentPage = 1;

        danhSach = dao.getAll1(); // hoặc dao.getAll() nếu bạn dùng hàm đó
        hienThiTrang(currentPage);
    }//GEN-LAST:event_btnPrev1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField TF_TimTenSP;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrev;
    private javax.swing.JButton btnPrev1;
    private javax.swing.JComboBox<String> cboLoai1;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JLabel lblAnh1;
    private javax.swing.JLabel lblAnh2;
    private javax.swing.JLabel lblAnh3;
    private javax.swing.JLabel lblAnh4;
    private javax.swing.JLabel lblGia1;
    private javax.swing.JLabel lblGia2;
    private javax.swing.JLabel lblGia3;
    private javax.swing.JLabel lblGia4;
    private javax.swing.JLabel lblPage;
    private javax.swing.JLabel lblSL1;
    private javax.swing.JLabel lblSL2;
    private javax.swing.JLabel lblSL3;
    private javax.swing.JLabel lblSL4;
    private javax.swing.JLabel lblTen1;
    private javax.swing.JLabel lblTen2;
    private javax.swing.JLabel lblTen3;
    private javax.swing.JLabel lblTen4;
    // End of variables declaration//GEN-END:variables
}
