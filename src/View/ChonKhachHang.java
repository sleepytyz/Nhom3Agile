/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package View;

import DAO.KhachHangDAO;
import Model.KhachHang;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import DAO.KhachHangDAO;
import java.sql.SQLException;

public class ChonKhachHang extends javax.swing.JDialog {

    private QLHD parent;
    DefaultTableModel khangmodel;
    private KhachHangDAO khdao = new KhachHangDAO();

    public ChonKhachHang(JFrame owner, QLHD parent) {
        super(owner, true); // Modal dialog
        this.parent = parent; // Gán parent
        initComponents();
        initTablesp();
        loadDataToTable();
        setLocationRelativeTo(owner);
    }

    public void initTablesp() {
        String[] cols = new String[]{"Mã khách hàng", "Tên khách hàng", "Giới tính", "Số điện thoại", "Trạng thái", "Địa chỉ"};
        khangmodel = new DefaultTableModel();
        khangmodel.setColumnIdentifiers(cols);
        jTable1.setModel(khangmodel);
    }

    private void loadDataToTable() {
    DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
    model.setRowCount(0); // Xóa dữ liệu cũ

    try {
        List<KhachHang> list = khdao.getAllKhachHangg(); // Sử dụng phương thức mới
        
        for (KhachHang kh : list) {
            model.addRow(new Object[]{
                kh.getMakh(),
                kh.getTenkh(),
                kh.getGioiTinh(),
                kh.getSdt(),
                kh.getTrangThai(),
                kh.getDiaChi()
            });
        }
        
        // Debug
        System.out.println("Đã load " + list.size() + " khách hàng (chỉ hiển thị Khách mới/Khách quen)");
        
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, 
            "Lỗi khi tải dữ liệu khách hàng: " + ex.getMessage(),
            "Lỗi", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}

    private void chonKhachHang() {
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng!");
            return;
        }

        String tenKH = jTable1.getValueAt(selectedRow, 1).toString();
        String sdt = jTable1.getValueAt(selectedRow, 3).toString();

        if (parent != null) { // Sửa từ 'áv' thành 'parent'
            parent.setKhachHang(tenKH, sdt);
        }
        this.dispose();
    }
    
     private boolean validateForm(boolean isAddingNew) {
        // isAddingNew = true nếu đang ở chế độ thêm mới, false nếu là sửa

        StringBuilder errors = new StringBuilder();

        // 1. Validate Tên Khách hàng (TF_TenKH)
        String tenKH = jTextField2.getText().trim();
        if (tenKH.isEmpty()) {
            errors.append("- Tên khách hàng không được để trống.\n");
        } else if (!tenKH.matches("^[\\p{L}\\s.'-]+$")) { // Chấp nhận chữ cái (unicode), dấu cách, dấu nháy đơn, gạch nối
            errors.append("- Tên khách hàng chỉ chấp nhận chữ cái và dấu cách.\n");
        }

        // 2. Validate Số điện thoại (TF_SDT)
        String sdt = jTextField3.getText().trim();
        if (sdt.isEmpty()) {
            errors.append("- Số điện thoại không được để trống.\n");
        } else if (!sdt.matches("^\\d{10}$")) { // Phải có đúng 10 chữ số
            errors.append("- Số điện thoại phải là 10 chữ số.\n");
        } else {
            // Kiểm tra trùng lặp SĐT (chỉ khi thêm mới)
            if (isAddingNew) {
                try {
                    KhachHang existingKh = khdao.getKhachHangBySdt(sdt);
                    if (existingKh != null) {
                        errors.append("- Số điện thoại này đã tồn tại trong hệ thống.\n");
                    }
                } catch (SQLException e) {
                    errors.append("- Lỗi kiểm tra trùng lặp SĐT: " + e.getMessage() + "\n");
                    e.printStackTrace();
                }
            }
        }

        // 3. Validate Địa chỉ (TA_diachi)
        String diaChi = jTextField5.getText().trim();
        if (diaChi.isEmpty()) {
            errors.append("- Địa chỉ không được để trống.\n");
        }

        // 4. Validate Giới tính (rdonam, rdonu)
        if (!jRadioButton1.isSelected() && !jRadioButton2.isSelected()) {
            errors.append("- Vui lòng chọn giới tính (Nam hoặc Nữ).\n");
        }

        // 5. Validate Trạng thái (rdokhachquen, rdokhachmoi)
        if (!jRadioButton4.isSelected()) {
            errors.append("- Vui lòng chọn trạng thái .\n");
        }

        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(this, errors.toString(), "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void luuKhachHang() {
    // Kiểm tra validate form (tương tự phần đầu)
    if (!validateForm(true)) { // isAddingNew = true
        return;
    }

    try {
        // Tạo mã KH tự động (nếu cần)
        String maKH = jTextField1.getText().trim();
        if (maKH.isEmpty()) {
            maKH = generateNewMaKH();
            jTextField1.setText(maKH);
        }

        // Xử lý giới tính
        String gioiTinh = "";
        if (jRadioButton1.isSelected()) {
            gioiTinh = "Nam";
        } else if (jRadioButton2.isSelected()) {
            gioiTinh = "Nữ";
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn giới tính!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Xử lý trạng thái
        String trangThai = "Khách mới";

        // Validate số điện thoại
        String sdt = jTextField3.getText().trim();
        if (!sdt.matches("\\d{10,11}")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại phải có 10-11 chữ số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            jTextField3.requestFocus();
            return;
        }

        // Validate các trường bắt buộc
        String tenKH = jTextField2.getText().trim();
        String diaChi = jTextField5.getText().trim();
        
        if (tenKH.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên khách hàng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            jTextField2.requestFocus();
            return;
        }

        if (diaChi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập địa chỉ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            jTextField5.requestFocus();
            return;
        }

        // Kiểm tra mã KH đã tồn tại chưa (nếu cần)
        if (khdao.isMaKHExist(maKH)) {
            JOptionPane.showMessageDialog(this, "Mã khách hàng đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            jTextField1.requestFocus();
            return;
        }

        // Tạo đối tượng khách hàng
        KhachHang kh = new KhachHang(
                maKH,
                tenKH,
                gioiTinh,
                sdt,
                trangThai,
                diaChi
        );

        // Thực hiện thêm vào database
        khdao.insert(kh);

        JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công!");
        loadDataToTable(); // Hoặc loadKhachHangData() tùy theo tên phương thức của bạn
        jTabbedPane1.setSelectedIndex(0); // Chuyển về tab danh sách
        clearForm(); // Xóa form nếu cần

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Lỗi khi thêm khách hàng: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}
    
    private String generateNewMaKH() {
        try {
            String lastMaKH = khdao.getLastMaKH();

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
    
    
    private void clearForm() {
        jTextField1.setText(generateNewMaKH());
        jTextField2.setText("");
        jTextField3.setText("");
        jTextField5.setText("");
        buttonGroup1.clearSelection();
        buttonGroup2.clearSelection(); 
        jRadioButton4.setSelected(true); 
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
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton2 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jTextField2 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jRadioButton4 = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

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

        jButton2.setText("Chọn");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 718, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(326, 326, 326)
                        .addComponent(jButton2)))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 353, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                .addComponent(jButton2)
                .addGap(52, 52, 52))
        );

        jTabbedPane1.addTab("Chọn khách hàng", jPanel1);

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Địa chỉ :");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Giới tính :");

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setText("Nam");
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText("Nữ");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("Tạo khách hàng mới");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Số điện thoại :");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Tên khách hàng :");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Mã khách hàng :");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("Trạng thái :");

        jButton1.setText("Thêm");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        buttonGroup2.add(jRadioButton4);
        jRadioButton4.setText("Khách mới");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel4))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap(175, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel2)
                            .addComponent(jLabel5)
                            .addComponent(jLabel3)
                            .addComponent(jLabel7))))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addGap(11, 11, 11)
                                    .addComponent(jRadioButton1)
                                    .addGap(120, 120, 120)
                                    .addComponent(jRadioButton2)
                                    .addGap(89, 89, 89))
                                .addComponent(jTextField1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 335, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jTextField2)
                                .addComponent(jTextField3))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton1)
                            .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jRadioButton4))))
                .addGap(100, 100, 100))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(288, 288, 288)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(12, 12, 12)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(65, 65, 65))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(37, 37, 37)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton2)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(34, 34, 34)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRadioButton4))
                .addGap(31, 31, 31)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 42, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Tạo khách hàng", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 751, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(33, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 514, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(24, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        chonKhachHang();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        luuKhachHang();
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(ChonKhachHang.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ChonKhachHang dialog = new ChonKhachHang(null, null); // Sử dụng constructor hiện có
                dialog.setModal(true); // Thiết lập modal
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField5;
    // End of variables declaration//GEN-END:variables
}
