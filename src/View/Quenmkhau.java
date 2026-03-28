/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package View;

import DAO.NhanVienDAO;
import Model.Guiemail;
import Service.DBConnect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class Quenmkhau extends javax.swing.JDialog {
    private static final Logger logger = Logger.getLogger(Quenmkhau.class.getName());
    private static final int MAX_ATTEMPTS = 3;
    private static final int THOI_GIAN_HIEU_LUC = 5 * 60 * 1000;  // 5 phút + 15 giây = 315 giây ; // 5 phút

    private final NhanVienDAO nvdao;
    private final Guiemail emailService;
    private String emailNguoiQuen;
    private String maXacNhan;
    private int failedAttempts;
    private long thoiGianMoForm;
    private Timer demNguocTimer;
    private long thoiGianGuiMa;

    public Quenmkhau(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setLocationRelativeTo(null);
        
        this.nvdao = new NhanVienDAO();
        this.emailService = new Guiemail();
        this.thoiGianMoForm = System.currentTimeMillis(); // Ghi nhận thời gian mở form
        
        setupUI();
        startDemNguoc(); // Bắt đầu đếm ngược ngay khi mở form
    }

    private void setupUI() {
        jPanel3.setVisible(true);
        lblThoiGianConLai.setVisible(false);
        jPanel2.setVisible(false);
        jPanel1.setVisible(false);
    }

   private void guiMaXacNhan() {
    String lienHe = txtEmail.getText().trim();

    if (lienHe.isEmpty()) {
        showError("Vui lòng nhập email hoặc số điện thoại");
        txtEmail.requestFocus();
        return;
    }

    try {
        // Xác định loại liên hệ
        if (lienHe.contains("@")) {
            if (!nvdao.kiemTraEmailTonTai(lienHe)) {
                showError("Email không tồn tại trong hệ thống");
                txtEmail.requestFocus();
                txtEmail.selectAll();
                return;
            }
            emailNguoiQuen = lienHe;
        } else if (lienHe.matches("\\d{10,11}")) {
            if (!nvdao.kiemTraSoDienThoaiTonTai(lienHe)) {
                showError("Số điện thoại không tồn tại trong hệ thống");
                txtEmail.requestFocus();
                txtEmail.selectAll();
                return;
            }

            emailNguoiQuen = nvdao.layEmailTheoLienHe(lienHe);
            if (emailNguoiQuen == null || emailNguoiQuen.isEmpty()) {
                showError("Không tìm thấy email liên kết với số điện thoại này");
                return;
            }
        } else {
            showError("Vui lòng nhập đúng định dạng:\n- Email (vd: example@domain.com)\n- Số điện thoại (10-11 số)");
            txtEmail.requestFocus();
            txtEmail.selectAll();
            return;
        }

        // Vô hiệu hóa nút gửi trong khi xử lý
        jButton1.setEnabled(false);

        // Tạo mã xác nhận
        maXacNhan = String.format("%06d", new Random().nextInt(999999));

        // Gửi email trong luồng khác
        new Thread(() -> {
            try {
                emailService.sendMaXacNhan(emailNguoiQuen, maXacNhan);

                // Sau khi gửi xong thì cập nhật UI
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        "Mã xác nhận đã được gửi tới: " + emailNguoiQuen + "\nMã có hiệu lực trong 5 phút",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);

                    // Hiện giao diện nhập mã
                    jPanel3.setVisible(true);
                    jPanel1.setVisible(true);
                    lblThoiGianConLai.setVisible(true);

                    // Ghi nhận thời gian bắt đầu
                    thoiGianGuiMa = System.currentTimeMillis();
                    startDemNguoc();
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    logger.log(Level.SEVERE, "Lỗi khi gửi email", e);
                    showError("Không thể gửi mã xác nhận: " + e.getMessage());
                });
            } finally {
                SwingUtilities.invokeLater(() -> jButton1.setEnabled(true));
            }
        }).start();

    } catch (Exception e) {
        logger.log(Level.SEVERE, "Lỗi trong quá trình xác thực", e);
        showError("Lỗi hệ thống: " + e.getMessage());
        jButton1.setEnabled(true);
    }
}


    private void startDemNguoc() {
    if (demNguocTimer != null) {
        demNguocTimer.stop();
    }
    
    // Hiển thị thông báo ban đầu trong 2 giây
    lblThoiGianConLai.setText("Mã đã được gửi!");
    
    // Tạo timer tạm để hiển thị thông báo trước
    Timer initialTimer = new Timer(2000, e -> {
        // Sau 2 giây, bắt đầu đếm ngược thực sự
        demNguocTimer = new Timer(1000, evt -> {
            long thoiGianConLai = THOI_GIAN_HIEU_LUC - (System.currentTimeMillis() - thoiGianGuiMa);
            
            if (thoiGianConLai <= 0) {
                lblThoiGianConLai.setText("Mã đã hết hạn!");
                ((Timer)evt.getSource()).stop();
                maXacNhan = null;
            } else {
                int phut = (int) (thoiGianConLai / 60000);
                int giay = (int) ((thoiGianConLai % 60000) / 1000);
                lblThoiGianConLai.setText(String.format("Thời gian còn lại: %02d:%02d", phut, giay));
            }
        });
        demNguocTimer.start();
        ((Timer)e.getSource()).stop(); // Dừng timer tạm
    });
    initialTimer.setRepeats(false);
    initialTimer.start();
}

    private void kiemTraMaXacNhan() {
        String nhapMa = txtMaNhap.getText().trim();
        
        if (nhapMa.isEmpty()) {
            showError("Vui lòng nhập mã xác nhận");
            return;
        }
        
        // Kiểm tra thời gian từ lúc gửi mã
        long thoiGianConLai = THOI_GIAN_HIEU_LUC - (System.currentTimeMillis() - thoiGianGuiMa);
        if (thoiGianConLai <= 0) {
            showError("Mã xác nhận đã hết hạn. Vui lòng gửi lại mã mới!");
            lblThoiGianConLai.setVisible(false);
            jPanel3.setVisible(true);
            return;
        }
        
        if (!nhapMa.equals(maXacNhan)) {
            failedAttempts++;
            if (failedAttempts >= MAX_ATTEMPTS) {
                showError("Bạn đã nhập sai quá 3 lần. Vui lòng thử lại sau!");
                lblThoiGianConLai.setVisible(false);
                jPanel3.setVisible(true);
                jPanel1.setVisible(false);
                txtMaNhap.setText("");
                return;
            }
            showError("Mã xác nhận không đúng! Bạn còn " + (MAX_ATTEMPTS - failedAttempts) + " lần thử.");
            return;
        }
        
        // Mã đúng, chuyển sang bước đổi mật khẩu
        lblThoiGianConLai.setVisible(false);
        jPanel2.setVisible(true);
        jPanel3.setVisible(true);
        jPanel1.setVisible(true);
        demNguocTimer.stop();
    }


    private void doiMatKhau() {
        String mk1 = new String(txtMatKhauMoi.getPassword());
        String mk2 = new String(txtXacNhanMK.getPassword());

        if (mk1.isEmpty() || mk2.isEmpty()) {
            showError("Vui lòng nhập đầy đủ mật khẩu mới");
            return;
        }
        
        if (!mk1.equals(mk2)) {
            showError("Mật khẩu xác nhận không khớp");
            return;
        }

        if (mk1.length() < 6) {
            showError("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }

        try (Connection con = DBConnect.getConnection(); 
             PreparedStatement ps = con.prepareStatement(
                "UPDATE TKhoan SET matkhau = ? WHERE MaNV = (SELECT MaNV FROM NhanVien WHERE Email = ?)")) {

            ps.setString(1, mk1);
            ps.setString(2, emailNguoiQuen);

            if (ps.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công");
                this.dispose();
            } else {
                showError("Đổi mật khẩu thất bại");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Lỗi khi đổi mật khẩu", e);
            showError("Lỗi hệ thống khi đổi mật khẩu: " + e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
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
        jLabel3 = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        lblThoiGianConLai = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        txtMaNhap = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        txtMatKhauMoi = new javax.swing.JPasswordField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtXacNhanMK = new javax.swing.JPasswordField();
        btnThem = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel3.setText("Nhập email hoặc sđt :");

        jButton1.setText("Gửi mã");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        lblThoiGianConLai.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblThoiGianConLai.setText("00");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblThoiGianConLai)
                    .addComponent(jButton1))
                .addContainerGap(66, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(17, Short.MAX_VALUE)
                .addComponent(lblThoiGianConLai)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jButton1))
                .addContainerGap())
        );

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel4.setText("Nhập mã :");

        jButton2.setText("Check");
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
                .addGap(95, 95, 95)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtMaNhap, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(jButton2)
                .addContainerGap(59, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtMaNhap, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        txtMatKhauMoi.setText("jPasswordField1");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel5.setText("Xác nhận mật khẩu :");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel6.setText("Nhập mật khẩu :");

        txtXacNhanMK.setText("jPasswordField1");
        txtXacNhanMK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtXacNhanMKActionPerformed(evt);
            }
        });

        btnThem.setIcon(new javax.swing.ImageIcon("D:\\MOB1014\\icons\\add.png")); // NOI18N
        btnThem.setText("Thêm");
        btnThem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThemActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtMatKhauMoi, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtXacNhanMK, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btnThem, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(53, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(txtMatKhauMoi, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(24, 24, 24)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtXacNhanMK, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(btnThem, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(58, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(35, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnThemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThemActionPerformed
        // TODO add your handling code here:
        doiMatKhau();
    }//GEN-LAST:event_btnThemActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        guiMaXacNhan();


    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        kiemTraMaXacNhan();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void txtXacNhanMKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtXacNhanMKActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtXacNhanMKActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Quenmkhau.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Quenmkhau.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Quenmkhau.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Quenmkhau.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Quenmkhau dialog = new Quenmkhau(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton btnThem;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel lblThoiGianConLai;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtMaNhap;
    private javax.swing.JPasswordField txtMatKhauMoi;
    private javax.swing.JPasswordField txtXacNhanMK;
    // End of variables declaration//GEN-END:variables
}
