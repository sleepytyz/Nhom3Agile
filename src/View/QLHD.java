/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package View;

import DAO.HoaDonChiTietDAO;
import DAO.HoaDonDao;
import DAO.SanPhamDAO;
import Model.HoaDon;
import Model.HoaDonChiTiet;
import Model.SanPham;
import Service.DBConnect;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 *
 * @author XPS
 */
public class QLHD extends javax.swing.JPanel {

    private List<HoaDon> listHD = new ArrayList<>();
    private JTextField txtMaKH, txtTenKH;
    private JButton btnChonKH;
    DefaultTableModel hoadon1model;
    DefaultTableModel hoadonctmodel;
    DefaultTableModel hoadondagiaomodel;
    DefaultTableModel sanphammodel;
    HoaDonDao hddao = new HoaDonDao();
    HoaDonChiTietDAO hdctdao = new HoaDonChiTietDAO();
    SanPhamDAO spdao = new SanPhamDAO();
    Connection con = (Connection) DBConnect.getConnection();
    private String selectedSDT;// hoặc tên biến tương ứng
    private ChonKhachHang dialog; // Khai báo biến instance để theo dõi dialog
    private boolean dialogIsOpen = false;
    private String maHDHienTai;
    private String maNhanVien;

    private boolean warningShown = false;
    DecimalFormat formatter = new DecimalFormat("#,###");

    /**
     * Creates new form QLHD
     */
    public QLHD(String maNV) {
        initComponents();
        initTablehdon();
        fillTablehdon();
        initTablehdonct();
        initTablesp();
        fillTablesp();

        jPanel2.setPreferredSize(new Dimension(908, 253));
        TF_Khachdua.setEnabled(false);
        jButton4.setEnabled(false);
        jButton5.setEnabled(false);

        TF_Khachdua.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                JTextField textField = (JTextField) e.getSource();
                String text = textField.getText();

                // Bỏ dấu phẩy và ký tự không phải số
                text = text.replaceAll("[^\\d]", "");

                if (!text.isEmpty()) {
                    try {
                        long number = Long.parseLong(text);
                        textField.setText(formatter.format(number));
                    } catch (NumberFormatException ex) {
                        // Có thể log lỗi hoặc xử lý khác nếu cần
                        textField.setText("");
                    }
                }
            }
        });

        TF_Khachdua.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                tinhTienThua();
            }

            public void removeUpdate(DocumentEvent e) {
                tinhTienThua();
            }

            public void insertUpdate(DocumentEvent e) {
                tinhTienThua();
            }
        });
        jComboBox1.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    capNhatTheoPhuongThuc();
                }
            }
        });
        this.maNhanVien = maNV;
        TF_MaNV.setText(maNV);

        jComboBox2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object selected = jComboBox2.getSelectedItem();
                if (selected == null) {
                    // Nếu chưa chọn gì thì disable các nút
                    jButton4.setEnabled(false);
                    jButton5.setEnabled(false);
//                    jButton6.setEnabled(false);

                    return;
                }

                String phuongThuc = selected.toString();

                if (phuongThuc.equalsIgnoreCase("Giao hàng")) {
                    jButton4.setEnabled(true);
                    jButton5.setEnabled(true);
//                    jButton6.setEnabled(true);
                    jButton2.setEnabled(false);
                } else {
                    jButton4.setEnabled(false);
                    jButton5.setEnabled(false);
//                    jButton6.setEnabled(false);
                    jButton2.setEnabled(true);
                }
            }
        });

        tbhdon123.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tbhdon123.getSelectedRow();
                if (row >= 0) {
                    String maHD = hoadon1model.getValueAt(row, 0).toString();
                    hienThongTinHoaDon(maHD);      // Hiển thị thông tin hóa đơn
                    fillTablehdonct(maHD);          // Hiển thị chi tiết hóa đơn
                    TF_Khachdua.setEditable(false);
                }
            }
        });

        jComboBox1.addActionListener(e -> {
            if (jComboBox1.getSelectedIndex() > 0) {
                String selectedMethod = jComboBox1.getSelectedItem().toString();

                if (selectedMethod.equals("Quẹt thẻ") || selectedMethod.equals("Chuyển khoản")) {
                    try {
                        String tongTienStr = TF_Tongtien.getText().replaceAll("[^\\d]", "");
                        if (!tongTienStr.isEmpty()) {
                            double tongTien = Double.parseDouble(tongTienStr);
                            TF_Khachdua.setText(String.format("%.0f", tongTien));
                            TF_Khachdua.setEditable(false);
                        }
                    } catch (NumberFormatException ex) {
                        TF_Khachdua.setText("");
                    }
                } else {
                    TF_Khachdua.setEditable(true);
                    TF_Khachdua.setText("");
                }
            }
        });

        jComboBox1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object selectedObj = jComboBox1.getSelectedItem();
                if (selectedObj != null) {
                    String selected = selectedObj.toString();
                    if (!selected.equalsIgnoreCase("-- Chọn --")) {
                        TF_Khachdua.setEnabled(true);
                    } else {
                        TF_Khachdua.setText("");
                        TF_Khachdua.setEnabled(false);
                    }
                } else {
                    // Trường hợp chưa có gì được chọn (null)
                    TF_Khachdua.setText("");
                    TF_Khachdua.setEnabled(false);
                }
            }
        });

        jComboBox2.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String phuongThucGH = jComboBox2.getSelectedItem().toString();
                    DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) jComboBox1.getModel();

                    if (phuongThucGH.equalsIgnoreCase("Giao hàng")) {
                        // Xoá "Quẹt thẻ" nếu có
                        if (model.getIndexOf("Quẹt thẻ") != -1) {
                            model.removeElement("Quẹt thẻ");
                        }
                    } else {
                        // Thêm lại "Quẹt thẻ" nếu bị xoá trước đó
                        if (model.getIndexOf("Quẹt thẻ") == -1) {
                            model.addElement("Quẹt thẻ");
                        }
                    }
                }
            }
        });

    }

    private boolean kiemTraConSanPhamTraHangTrongHoaDon(String maHD) {
        try (Connection con = DBConnect.getConnection()) {
            String sql = "SELECT COUNT(*) FROM HoaDonChiTiet WHERE MaHD = ? AND TrangThaiTraHang = N'Trả hàng'";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maHD);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void initTablehdon() {
        String[] cols = new String[]{"Mã hoá đơn", "Mã nhân viên", "Trạng thái", "Ngày lập", "Ghi chú"};
        hoadon1model = new DefaultTableModel();
        hoadon1model.setColumnIdentifiers(cols);
        tbhdon123.setModel(hoadon1model);
    }

    public void fillTablehdon() {
        hoadon1model.setRowCount(0); // Xóa dữ liệu cũ

        // Lấy dữ liệu mới từ DB
        listHD = hddao.getAll1(); // ← Cập nhật lại listHD ở đây

        for (HoaDon hd : listHD) {
            System.out.println(hd.getMahd() + " - " + hd.getTrangThai()); // Log kiểm tra

            Object[] rowData = {
                hd.getMahd(),
                hd.getManv(),
                hd.getTrangThai(),
                hd.getNgayTao(),
                hd.getGhiChu()
            };
            hoadon1model.addRow(rowData);
        }
    }

    public void initTablehdonct() {
        String[] cols = new String[]{
            "Mã chi tiết hoá đơn", "Mã hoá đơn", "Mã sản phẩm", "Tên sản phẩm",
            "Số lượng", "Đơn giá", "Giảm giá", "Mã khuyến mãi", "Thành tiền",};
        hoadonctmodel = new DefaultTableModel();
        hoadonctmodel.setColumnIdentifiers(cols);
        tbhdonct.setModel(hoadonctmodel);
    }

    public void fillTablehdonct(String maHD) {
        hoadonctmodel.setRowCount(0);
        if (maHD == null || maHD.isEmpty()) {
            return;
        }

        String trangThaiHoaDon = hddao.getTrangThaiByMaHD(maHD);
        List<Object[]> list = hdctdao.getAllWithMaKM(maHD);

        for (Object[] row : list) {
            // Lấy trạng thái trả hàng trong phần tử thứ 10 (index 9)
            String trangThaiTraHang = "";
            if (row.length > 9 && row[9] != null) {
                trangThaiTraHang = row[9].toString();
            }

            if (!"Trả hàng".equalsIgnoreCase(trangThaiTraHang)
                    && ("Đã thanh toán".equalsIgnoreCase(trangThaiHoaDon)
                    || "Đã giao hàng".equalsIgnoreCase(trangThaiHoaDon))) {
                // Cập nhật trạng thái trả hàng
                row[9] = "Thanh toán";
            }

            // Tạo mảng mới chỉ lấy 9 phần tử cho bảng (loại bỏ phần tử thứ 10)
            Object[] rowForTable = new Object[9];
            System.arraycopy(row, 0, rowForTable, 0, 9);

            hoadonctmodel.addRow(rowForTable);
        }
    }

    public void initTablesp() {
        String[] cols = new String[]{"Mã sản phẩm", "Tên sản phẩm", "Loại sản phẩm", "Đơn giá", "Số lượng", "Giảm giá", "Mã KM", "Màu sắc", "Kích thước", "Chất liệu", "Trạng thái"};
        sanphammodel = new DefaultTableModel();
        sanphammodel.setColumnIdentifiers(cols);
        tbsp.setModel(sanphammodel);
    }

    public void fillTablesp() {
        sanphammodel.setRowCount(0);
        for (Object[] row : spdao.getAllWithDiscount()) {
            sanphammodel.addRow(row);
        }
    }

    private void taoHoaDon() {
        try {
            // 1. Tự động tạo mã hóa đơn nếu trống

            TF_Mahd.setText(hddao.taoMaHoaDonMoi());

            // 2. Validate dữ liệu bắt buộc
            // Bỏ điều kiện bắt buộc phải có SDT nếu là "Khách vãng lai"
            if (TF_MaNV.getText().trim().isEmpty()
                    || TF_TenKH.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin bắt buộc!");
                return;
            }

            // 3. Lấy thông tin cơ bản
            String maHD = TF_Mahd.getText().trim();
            String maNV = TF_MaNV.getText().trim();
            String tenKH = TF_TenKH.getText().trim();
            String sdtKH = selectedSDT;
            String trangThai = "Đang xử lý";
            Date ngayTao = new Date(System.currentTimeMillis());
            String ghichu = jTextArea1.getText().trim();

            // 4. Gọi DAO để tạo hóa đơn cơ bản
            boolean result = hddao.taoHoaDonBanDau(
                    maHD,
                    maNV,
                    tenKH,
                    sdtKH,
                    trangThai
            );

            if (result) {
                JOptionPane.showMessageDialog(this, "Tạo hóa đơn thành công!");
                fillTablehdon();  // Load lại bảng hóa đơn
                fillTablehdonct(maHD);

                maHDHienTai = maHD;

                // Cập nhật hiển thị thông tin khách hàng
                TF_TenKH.setText(tenKH);
                TF_sdt.setText(sdtKH);  // Giả sử có JTextField TF_SDT

                jButton2.setEnabled(true); // Kích hoạt nút thanh toán
            } else {
                JOptionPane.showMessageDialog(this, "Tạo hóa đơn thất bại!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tạo hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void capNhatTheoPhuongThuc() {
        SwingUtilities.invokeLater(() -> {
            if (jComboBox1.getSelectedItem() == null) {
                return;
            }

            String pt = jComboBox1.getSelectedItem().toString();
            double tongTien = 0;

            try {
                tongTien = Double.parseDouble(TF_Tongtien.getText().replace(",", ""));
            } catch (NumberFormatException e) {
                tongTien = 0;
            }

            if (pt.equalsIgnoreCase("Quẹt thẻ") || pt.equalsIgnoreCase("Chuyển khoản")) {
                TF_Khachdua.setText(String.format("%.0f", tongTien));
                TF_Khachdua.setEditable(false);
                TF_Tienthua.setText("0");
            } else {
                TF_Khachdua.setText("");
                TF_Khachdua.setEditable(true);
                TF_Tienthua.setText("");
            }
        });
    }

    public void hienThiQRChuyenKhoan(String bankCode, String stk, double soTien, String noiDung) {
        try {
            // Chuyển các thông tin vào link QR của VietQR
            String qrURL = "https://img.vietqr.io/image/" + bankCode + "-" + stk + "-compact.png"
                    + "?amount=" + (long) soTien
                    + "&addInfo=" + java.net.URLEncoder.encode(noiDung, "UTF-8");

            BufferedImage qrImage = ImageIO.read(new URL(qrURL));
            JLabel qrLabel = new JLabel(new ImageIcon(qrImage));
            JOptionPane.showMessageDialog(this, qrLabel, "Quét mã để chuyển khoản", JOptionPane.PLAIN_MESSAGE);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Không thể tải mã QR: " + e.getMessage());
        }
    }

    private void thanhToanHoaDon() {
        try {
            String maHD = TF_Mahd.getText().trim();
            if (maHD.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng tạo hóa đơn trước!");
                return;
            }

            String maNVHienTai = TF_MaNV.getText().trim();

            if (tbhdonct.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Hóa đơn chưa có sản phẩm nào!");
                return;
            }

            String tongTienStr = TF_Tongtien.getText().replaceAll("[^\\d]", "");
            if (tongTienStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tổng tiền không hợp lệ!");
                return;
            }
            double tongTien = Double.parseDouble(tongTienStr);

            Object ptTTObj = jComboBox1.getSelectedItem();
            if (ptTTObj == null || ptTTObj.toString().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn phương thức thanh toán trước!");
                jComboBox1.requestFocus();
                return;
            }
            String phuongThucTT = ptTTObj.toString();

            String tienKhachStr = TF_Khachdua.getText().replaceAll("[^\\d]", "");
            if (tienKhachStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số tiền khách đưa!");
                return;
            }
            double tienKhachDua = Double.parseDouble(tienKhachStr);

            if (tienKhachDua < tongTien) {
                JOptionPane.showMessageDialog(this, "Số tiền khách đưa phải lớn hơn hoặc bằng tổng tiền!");
                return;
            }

            double tienThua = tienKhachDua - tongTien;
            TF_Tienthua.setText(String.format("%,.0f", tienThua));

            Object ptGHObj = jComboBox2.getSelectedItem();
            if (ptGHObj == null || ptGHObj.toString().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn phương thức giao hàng!");
                return;
            }
            String phuongThucGH = ptGHObj.toString();

            String ghiChu = jTextArea1.getText().trim();
            String trangThaiMoi = "Đã thanh toán";

            // Nếu là chuyển khoản → hiển thị mã QR trước
            if (phuongThucTT.equalsIgnoreCase("Chuyển khoản")) {
                String bankCode = "MB"; // mã ngân hàng
                String stk = "0344552008"; // số tài khoản
                String noiDung = "Thanh toan hoa don " + maHD;

                hienThiQRChuyenKhoan(bankCode, stk, tongTien, noiDung);

                // Hỏi người dùng đã chuyển khoản chưa
                int xacNhan = JOptionPane.showConfirmDialog(this,
                        "Khách đã chuyển khoản thành công chưa?",
                        "Xác nhận chuyển khoản",
                        JOptionPane.YES_NO_OPTION);

                if (xacNhan != JOptionPane.YES_OPTION) {
                    JOptionPane.showMessageDialog(this, "Vui lòng hoàn tất chuyển khoản trước khi xác nhận.");
                    return;
                }
            } else if (phuongThucTT.equalsIgnoreCase("Quẹt thẻ")) {
                int xacNhan = JOptionPane.showConfirmDialog(this,
                        "Khách đã quẹt thẻ thành công chưa?",
                        "Xác nhận quẹt thẻ",
                        JOptionPane.YES_NO_OPTION);

                if (xacNhan != JOptionPane.YES_OPTION) {
                    JOptionPane.showMessageDialog(this, "Vui lòng hoàn tất quẹt thẻ trước khi xác nhận.");
                    return;
                }
            }

            // Sau khi mọi điều kiện OK → tiến hành cập nhật hóa đơn
            boolean result = hddao.thanhToanHoaDon(
                    maHD,
                    tongTien,
                    tienKhachDua,
                    tienThua,
                    phuongThucTT,
                    phuongThucGH,
                    trangThaiMoi,
                    ghiChu,
                    maNVHienTai // truyền mã nhân viên
            );

            if (result) {
                JOptionPane.showMessageDialog(this, "Thanh toán thành công!");
                fillTablehdon();

                // Reset
                TF_Mahd.setText("");
                TF_Tongtien.setText("");
                TF_Khachdua.setText("");
                TF_Tienthua.setText("");
                jComboBox1.setSelectedIndex(-1);
                jComboBox2.setSelectedIndex(-1);
                jTextArea1.setText("");

                // Xóa bảng
                DefaultTableModel model = (DefaultTableModel) tbhdonct.getModel();
                model.setRowCount(0);

                int choice = JOptionPane.showConfirmDialog(this,
                        "Bạn có muốn in hóa đơn không?",
                        "In hóa đơn",
                        JOptionPane.YES_NO_OPTION);

                if (choice == JOptionPane.YES_OPTION) {
                    inhdonTuDong(
                            maHD,
                            TF_TenKH.getText(),
                            TF_sdt.getText(),
                            tongTienStr,
                            tienKhachStr,
                            String.format("%,.0f", tienThua),
                            phuongThucTT,
                            phuongThucGH,
                            tongTien
                    );
                }

            } else {
                JOptionPane.showMessageDialog(this, "Thanh toán thất bại!");
            }

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đúng định dạng số cho tiền!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi thanh toán: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void moFormChonKhachHang() {
        if (dialog == null || !dialog.isVisible()) {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            dialog = new ChonKhachHang(parentFrame, this); // truyền this để gọi lại setKhachHang()
            dialog.setVisible(true);
        } else {
            dialog.toFront();
        }
    }

    public void setKhachHang(String tenKH, String sdt) {
        TF_TenKH.setText(tenKH); // Đảm bảo tên TextField chính xác
        TF_sdt.setText(sdt);
        this.selectedSDT = sdt;
    }

    public void themsp() {
        int row = tbsp.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm!");
            return;
        }

        String maSP = tbsp.getValueAt(row, 0).toString();
        String maHD = TF_Mahd.getText().trim();

        if (maHD.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng tạo hóa đơn trước!");
            return;
        }

        // ✅ Validate: chỉ cho phép khi trạng thái hóa đơn là "Đang xử lý"
        String trangThaiHD = null;
        try {
            trangThaiHD = hddao.getTrangThaiByMaHD(maHD); // hàm DAO bạn cần có
        } catch (Exception ex) {
            // Nếu DAO ném lỗi, hiển thị và dừng
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi lấy trạng thái hóa đơn: " + ex.getMessage());
            return;
        }

        if (trangThaiHD == null || !trangThaiHD.equalsIgnoreCase("Đang xử lý")) {
            JOptionPane.showMessageDialog(this, "Chỉ được thêm sản phẩm khi hóa đơn ở trạng thái 'Đang xử lý'.\nHiện tại: "
                    + (trangThaiHD == null ? "Không xác định" : trangThaiHD));
            return;
        }

        String soLuongStr = JOptionPane.showInputDialog(this, "Nhập số lượng sản phẩm:");
        if (soLuongStr == null) {
            // Người dùng bấm Cancel
            return;
        }

        int soLuong;
        try {
            soLuong = Integer.parseInt(soLuongStr);
            if (soLuong <= 0) {
                JOptionPane.showMessageDialog(this, "Số lượng phải > 0");
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số lượng không hợp lệ!");
            return;
        }

        boolean result = hdctdao.themChiTiet(maHD, maSP, soLuong);

        if (result) {
            JOptionPane.showMessageDialog(this, "Thêm sản phẩm thành công!");
            fillTablehdonct(maHD);
            fillTablesp();
            capNhatTongTien(maHD);
        } else {
            JOptionPane.showMessageDialog(this, "Thêm sản phẩm thất bại!");
        }
    }

    private void capNhatTongTien(String maHD) {
        double tongTien = 0;
        for (HoaDonChiTiet hdct : hdctdao.getAll(maHD)) {
            tongTien += hdct.getThanhTien();
        }

        DecimalFormat df = new DecimalFormat("#,###");
        String tongTienFormatted = df.format(tongTien);

        TF_Tongtien.setText(tongTienFormatted);
        hddao.capNhatTongTien(maHD, tongTien);

        // Cập nhật TF_Khachdua nếu là chuyển khoản hoặc quẹt thẻ
        String ptThanhToan = (String) jComboBox1.getSelectedItem();
        if ("Chuyển khoản".equalsIgnoreCase(ptThanhToan) || "Quẹt thẻ".equalsIgnoreCase(ptThanhToan)) {
            TF_Khachdua.setText(tongTienFormatted); // gán luôn giá trị đã format
        }

        tinhTienThua(); // cập nhật lại tiền thừa luôn
    }

    private void tinhTienThua() {
        try {
            // Loại bỏ dấu phẩy nếu có
            String tongTienStr = TF_Tongtien.getText().replaceAll("[^\\d]", "");
            String khachDuaStr = TF_Khachdua.getText().replaceAll("[^\\d]", "");

            if (!tongTienStr.isEmpty() && !khachDuaStr.isEmpty()) {
                double tongTien = Double.parseDouble(tongTienStr);
                double tienKhachDua = Double.parseDouble(khachDuaStr);
                double tienThua = tienKhachDua - tongTien;

                DecimalFormat df = new DecimalFormat("#,###");
                TF_Tienthua.setText(df.format(Math.max(0, tienThua)));
            }
        } catch (NumberFormatException ex) {
            TF_Tienthua.setText("0");
        }
    }

    public void inhdonTuDong(String maHD, String tenKH, String sdt, String tongTien,
            String tienKhach, String tienThua, String ptThanhToan,
            String ptGiao, double soTienQR) {
        try {
            // Tạo thư mục nếu chưa có
            String folderPath = "hoadon";
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdir();
            }

            // Tạo đường dẫn file PDF
            String fileName = "HoaDon_" + maHD + ".pdf";
            File file = new File(folder, fileName);

            Document document = new Document(PageSize.A5, 30, 30, 20, 20);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Font có dấu (đặt file times.ttf ngay trong src/)
            BaseFont bf = BaseFont.createFont("times.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font fontTitle = new Font(bf, 16, Font.BOLD);
            Font fontNormal = new Font(bf, 12, Font.NORMAL);
            Font fontTableHeader = new Font(bf, 12, Font.NORMAL); // giảm độ đậm

            // Tiêu đề
            Paragraph title = new Paragraph("CỬA HÀNG BÁN QUẦN ÁO\n", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph diaChi = new Paragraph("Địa chỉ: Poly school, Cổng số 2, 13 P. Trịnh Văn Bô, Xuân Phương, Nam Từ Liêm, Hà Nội\n\n", fontNormal);
            diaChi.setAlignment(Element.ALIGN_CENTER);
            document.add(diaChi);

            // Thông tin KH + hóa đơn
            document.add(new Paragraph("Mã hóa đơn: " + maHD, fontNormal));
            document.add(new Paragraph("Khách hàng: " + tenKH, fontNormal));
            document.add(new Paragraph("SĐT: " + sdt, fontNormal));
            document.add(new Paragraph("Phương thức giao: " + ptGiao, fontNormal));
            document.add(new Paragraph("Thanh toán: " + ptThanhToan + "\n", fontNormal));
            document.add(new Paragraph("\n", fontNormal));

            // Bảng sản phẩm
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4f, 1.5f, 2f, 2f, 2.5f});
            table.setSpacingAfter(10f); // ✅ Tăng khoảng cách sau bảng

            String[] headers = {"Tên sản phẩm", "Số lượng", "Đơn giá", "Giảm giá", "Thành tiền"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, fontTableHeader));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            for (HoaDonChiTiet ct : hdctdao.timHDTheoma2(maHD)) {
                double thanhTien = ct.getSluong() * ct.getDongia() - ct.getGiamGia();
                table.addCell(new Phrase(ct.getTensp(), fontNormal));
                table.addCell(new Phrase(String.valueOf(ct.getSluong()), fontNormal));
                table.addCell(new Phrase(String.format("%,.0f", ct.getDongia()), fontNormal));
                table.addCell(new Phrase(String.format("%,.0f", ct.getGiamGia()), fontNormal));
                table.addCell(new Phrase(String.format("%,.0f", thanhTien), fontNormal));
            }

            document.add(table);

            // Tổng kết
            Paragraph pTong = new Paragraph("Tổng tiền: " + tongTien + " VND", fontNormal);
            Paragraph pKhach = new Paragraph("Tiền khách đưa: " + tienKhach + " VND", fontNormal);
            Paragraph pThua = new Paragraph("Tiền thừa: " + tienThua + " VND\n", fontNormal);
            pTong.setAlignment(Element.ALIGN_RIGHT);
            pKhach.setAlignment(Element.ALIGN_RIGHT);
            pThua.setAlignment(Element.ALIGN_RIGHT);

            document.add(pTong);
            document.add(pKhach);
            document.add(pThua);

            // QR nếu là chuyển khoản
            if (ptThanhToan.equalsIgnoreCase("Chuyển khoản")) {
                String bankCode = "MB";
                String stk = "0123456789";
                String noiDung = "CK_HD_" + maHD;
                String qrURL = "https://img.vietqr.io/image/" + bankCode + "-" + stk + "-compact.png"
                        + "?amount=" + (long) soTienQR
                        + "&addInfo=" + java.net.URLEncoder.encode(noiDung, "UTF-8");

                Image qrImg = Image.getInstance(new URL(qrURL));
                qrImg.scaleToFit(140, 140);
                qrImg.setAlignment(Element.ALIGN_CENTER);

                document.add(new Paragraph("Vui lòng quét mã để chuyển khoản:", fontNormal));
                document.add(qrImg);
            }

            Paragraph thanks = new Paragraph("\nCảm ơn quý khách đã mua hàng!", fontNormal);
            thanks.setAlignment(Element.ALIGN_CENTER);
            document.add(thanks);

            document.close();
            writer.close();

            // Mở file hóa đơn
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi in hóa đơn: " + e.getMessage());
        }
    }

    private void huyHoaDon() {
        int row = tbhdon123.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hoá đơn cần huỷ");
            return;
        }

        String trangThai = tbhdon123.getValueAt(row, 2).toString().trim();
        if (!trangThai.equals("Đang xử lý")) {
            JOptionPane.showMessageDialog(this, "Chỉ có thể huỷ hoá đơn đang xử lý");
            return;
        }

        String ghiChu = JOptionPane.showInputDialog(this, "Nhập lý do huỷ hoá đơn:");
        if (ghiChu == null || ghiChu.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập lý do huỷ");
            return;
        }

        String maHoaDon = tbhdon123.getValueAt(row, 0).toString();

        // Cập nhật trạng thái và ghi chú vào DB
        int kq = hddao.huyHoaDon(maHoaDon, ghiChu);
        if (kq > 0) {
            JOptionPane.showMessageDialog(this, "Huỷ hoá đơn thành công");
            fillTablehdon(); // Cập nhật lại danh sách
        } else {
            JOptionPane.showMessageDialog(this, "Huỷ hoá đơn thất bại");
        }
    }

    public void xoatoanbo() {
        int rowCount = tbhdonct.getRowCount();
        if (rowCount == 0) {
            JOptionPane.showMessageDialog(this, "Không có sản phẩm nào để xoá!");
            return;
        }

        String maHD = TF_Mahd.getText();
        if (maHD.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy mã hóa đơn!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Xoá toàn bộ sản phẩm?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                for (int i = 0; i < rowCount; i++) {
                    String maSP = tbhdonct.getValueAt(i, 2).toString(); // Cột mã SP
                    String soLuongStr = tbhdonct.getValueAt(i, 4).toString(); // Cột số lượng

                    if (soLuongStr.matches("\\d+")) {
                        int soLuong = Integer.parseInt(soLuongStr);
                        spdao.capNhatTonKho(maSP, soLuong); // Cộng lại tồn kho
                    } else {
                        JOptionPane.showMessageDialog(this, "Số lượng không hợp lệ tại dòng " + (i + 1));
                        continue;
                    }
                }

                // Xóa toàn bộ chi tiết
                hdctdao.xoaToanBoSanPham(maHD);

                // Cập nhật lại giao diện
                fillTablehdonct(maHD);
                fillTablesp();
                capNhatTongTien(maHD);     // << Cập nhật tổng tiền
                tinhTienThua();            // << Cập nhật tiền thừa

                JOptionPane.showMessageDialog(this, "Đã xóa toàn bộ sản phẩm!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa sản phẩm: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void xoa1spam() {
        int row = tbhdonct.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Hãy chọn sản phẩm cần xoá!");
            return;
        }

        String maHD = TF_Mahd.getText();
        String maSP = tbhdonct.getValueAt(row, 2).toString(); // Cột mã SP
        int soLuong = Integer.parseInt(tbhdonct.getValueAt(row, 4).toString()); // Cột số lượng

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xoá sản phẩm này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            hdctdao.xoaMotSanPham(maHD, maSP);
            spdao.capNhatTonKho(maSP, soLuong);

            fillTablehdonct(maHD);
            fillTablesp();
            capNhatTongTien(maHD);     // << Cập nhật tổng tiền
            tinhTienThua();            // << Cập nhật tiền thừa
        }
    }

    private void hienThongTinHoaDon(String maHD) {
        // Lấy đối tượng HoaDon từ database
        HoaDon hd = hddao.getHoaDonByMa(maHD);

        System.out.println("Kiểm tra: Đã tìm thấy hóa đơn có mã " + maHD + "? " + (hd != null));

        if (hd != null) {
            SwingUtilities.invokeLater(() -> {
                // Hiển thị dữ liệu in ra console để debug
                System.out.println("--- Dữ liệu từ Hóa đơn ---");
                System.out.println("Mã HD: " + hd.getMahd());
                System.out.println("Tên KH: " + hd.getTenkh());
                System.out.println("SĐT: " + hd.getSdt());
                System.out.println("Tổng tiền: " + hd.getTongTien());
                System.out.println("Tiền khách đưa: " + hd.getTienTra());
                System.out.println("Tiền thừa: " + hd.getTienThua());
                System.out.println("Ghi chú: " + hd.getGhiChu());
                System.out.println("Hình thức thanh toán: " + hd.getThanhToan());
                System.out.println("Hình thức giao hàng: " + hd.getGiaoHang());
                System.out.println("Trạng thái: " + hd.getTrangThai());
                System.out.println("--------------------------");

                // Hiển thị các thông tin lên TextField
                TF_Mahd.setText(hd.getMahd() != null ? hd.getMahd() : "");
                TF_TenKH.setText(hd.getTenkh() != null ? hd.getTenkh() : "");
                TF_sdt.setText(hd.getSdt() != null ? hd.getSdt() : "");

                TF_Tongtien.setText(String.format("%,.0f", hd.getTongTien()));
                TF_Khachdua.setText(String.format("%,.0f", hd.getTienTra()));

                TF_Tienthua.setText(String.format("%,.0f", hd.getTienThua()));

                jTextArea1.setText(hd.getGhiChu() != null ? hd.getGhiChu() : "");

                // Lấy dữ liệu từ object HoaDon hd
                String dbThanhToan = hd.getThanhToan();
                String dbGiaoHang = hd.getGiaoHang();

// Hiển thị lên JComboBox1 (Thanh Toán)
                if (dbThanhToan != null && !dbThanhToan.trim().isEmpty()) {
                    boolean found = false;
                    for (int i = 0; i < jComboBox2.getItemCount(); i++) {
                        if (jComboBox2.getItemAt(i).toString().equalsIgnoreCase(dbThanhToan.trim())) {
                            jComboBox2.setSelectedIndex(i);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        jComboBox2.addItem(dbThanhToan.trim());
                        jComboBox2.setSelectedItem(dbThanhToan.trim());
                    }
                }

// Hiển thị lên JComboBox2 (Giao Hàng)
                if (dbGiaoHang != null && !dbGiaoHang.trim().isEmpty()) {
                    boolean found = false;
                    for (int i = 0; i < jComboBox1.getItemCount(); i++) {
                        if (jComboBox1.getItemAt(i).toString().equalsIgnoreCase(dbGiaoHang.trim())) {
                            jComboBox1.setSelectedIndex(i);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        jComboBox1.addItem(dbGiaoHang.trim());
                        jComboBox1.setSelectedItem(dbGiaoHang.trim());
                    }
                }

                // Cập nhật trạng thái các nút theo trạng thái hóa đơn
                String trangThai = hd.getTrangThai();
                if (trangThai != null) {
                    trangThai = trangThai.trim();
                }

                if ("Đang giao".equalsIgnoreCase(trangThai)) {
                    jButton4.setEnabled(false);
                    jButton5.setEnabled(true);
                    jButton2.setEnabled(false);
                } else {
                    jButton4.setEnabled(false);
                    jButton5.setEnabled(false);
                    jButton2.setEnabled(true);
                }
            });
        } else {
            System.out.println("Không tìm thấy hóa đơn với mã: " + maHD);
        }
    }

    public void dagiao() {
        int row = tbhdon123.getSelectedRow(); // Lấy dòng được chọn

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn cần cập nhật!");
            return;
        }

        String maHD = tbhdon123.getValueAt(row, 0).toString(); // Cột 0 là mã hóa đơn
        String trangThaiHienTai = tbhdon123.getValueAt(row, 2).toString(); // Cột 2 là trạng thái

        if (!trangThaiHienTai.equalsIgnoreCase("Đang giao")) {
            JOptionPane.showMessageDialog(this, "Chỉ có thể cập nhật hóa đơn đang giao thành đã giao!");
            return;
        }

        int xacNhan = JOptionPane.showConfirmDialog(
                this,
                "Xác nhận hóa đơn \"" + maHD + "\" đã giao thành công?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (xacNhan == JOptionPane.YES_OPTION) {
            String maNV = TF_MaNV.getText().trim(); // Lấy mã NV hiện tại

            boolean updated = hddao.capNhatTrangThaiVaNhanVien(maHD, "Đã giao hàng", maNV);

            if (updated) {
                JOptionPane.showMessageDialog(this, "Đã cập nhật trạng thái thành 'Đã giao hàng'");
                fillTablehdon(); // reload bảng hóa đơn
//                fillTablehdondagiao();
                TF_Mahd.setText("");
                TF_Tongtien.setText("");
                TF_Khachdua.setText("");
                TF_Tienthua.setText("");
                jComboBox1.setSelectedIndex(-1);
                jComboBox2.setSelectedIndex(-1);
                jTextArea1.setText("");

                // Xóa bảng chi tiết giỏ hàng
                DefaultTableModel model = (DefaultTableModel) tbhdonct.getModel();
                model.setRowCount(0);
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
            }
        }
    }

    private float parseSafeFloat(String input) {
        try {
            return Float.parseFloat(input.trim());
        } catch (NumberFormatException e) {
            return 0f;
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

        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbsp = new javax.swing.JTable();
        btnThem = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbhdonct = new javax.swing.JTable();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        TF_TenKH = new javax.swing.JLabel();
        TF_sdt = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        TF_Khachdua = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox<>();
        jComboBox2 = new javax.swing.JComboBox<>();
        jButton2 = new javax.swing.JButton();
        TF_Tongtien = new javax.swing.JLabel();
        TF_Mahd = new javax.swing.JLabel();
        TF_Tienthua = new javax.swing.JLabel();
        TF_MaNV = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tbhdon123 = new javax.swing.JTable();
        jButton3 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();

        jButton8.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jButton8.setText("Thanh toán");

        jButton9.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jButton9.setText("Thanh toán");

        jButton13.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jButton13.setText("Xoá toàn bộ");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel1.setText("Hoá đơn");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Danh sách sản phẩm"));

        tbsp.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tbsp);

        btnThem.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btnThem.setText("Thêm");
        btnThem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThemActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 10, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 798, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnThem)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(91, 91, 91)
                .addComponent(btnThem, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Giỏ hàng"));

        tbhdonct.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(tbhdonct);

        jButton11.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jButton11.setText("Xoá sản phẩm");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        jButton12.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jButton12.setText("Xoá toàn bộ");
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jButton11)
                        .addGap(18, 18, 18)
                        .addComponent(jButton12)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jPanel7.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel23.setText("Tên khách hàng :");

        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel24.setText("Số điện thoại :");

        jButton7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton7.setText("Thay đổi");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        TF_TenKH.setText("Khách bán lẻ");

        TF_sdt.setText("0000000000");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel23)
                    .addComponent(jLabel24))
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(TF_TenKH, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TF_sdt, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel23)
                            .addComponent(TF_TenKH))
                        .addGap(29, 29, 29)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel24)
                            .addComponent(TF_sdt)))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(40, Short.MAX_VALUE))
        );

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel6.setText("Mã hoá đơn :");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel7.setText("Mã nhân viên :");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel9.setText("Tổng tiền :");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel10.setText("Tiền thừa :");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel12.setText("Tiền khách đưa :");

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel13.setText("Hình thức giao hàng :");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel14.setText("Hình thức thanh toán :");

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel15.setText("Ghi chú :");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane4.setViewportView(jTextArea1);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Tiền mặt", "Chuyển khoản", "Quẹt thẻ" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Tại quán", "Giao hàng" }));

        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jButton2.setText("Thanh toán");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        TF_Tongtien.setText("0");

        TF_Mahd.setText("HD00");

        TF_Tienthua.setText("0");

        TF_MaNV.setText("NV00");

        jButton4.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jButton4.setText("Giao hàng");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jButton5.setText("Đã giao");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel14)
                                    .addComponent(jLabel12)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel10)
                                    .addComponent(jLabel13)
                                    .addComponent(jLabel15))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(TF_Mahd, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(TF_Tongtien, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(TF_MaNV, javax.swing.GroupLayout.PREFERRED_SIZE, 253, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(TF_Khachdua, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(TF_Tienthua, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 23, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(TF_Mahd, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(TF_MaNV, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(TF_Tongtien, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_Khachdua, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addGap(29, 29, 29)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(TF_Tienthua, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13))
                .addGap(32, 32, 32)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addGap(18, 18, 18)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Hoá đơn", jPanel4);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        tbhdon123.setModel(new javax.swing.table.DefaultTableModel(
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
        tbhdon123.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbhdon123MouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tbhdon123);

        jButton3.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jButton3.setText("Huỷ");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton14.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jButton14.setText("Tạo hoá đơn");
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 759, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addComponent(jButton3)
                        .addGap(30, 30, 30))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jButton14)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(55, 55, 55)
                        .addComponent(jButton14, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(32, 32, 32)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(29, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Danh sách hoá đơn", jPanel3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 908, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(754, 754, 754)
                                .addComponent(jLabel1))
                            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(18, 18, 18)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 520, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 817, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(29, 29, 29))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnThemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThemActionPerformed
        // TODO add your handling code here:
        themsp();
    }//GEN-LAST:event_btnThemActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // TODO add your handling code here:
        moFormChonKhachHang();
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        thanhToanHoaDon();

    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        // TODO add your handling code here:
        xoa1spam();
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        // TODO add your handling code here:
        xoatoanbo();
    }//GEN-LAST:event_jButton12ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        String maHD = TF_Mahd.getText().trim();
        if (maHD.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Chưa có mã hóa đơn!");
            return;
        }

        String trangThaiHienTai = hddao.getTrangThaiByMaHD(maHD);
        if (trangThaiHienTai.equalsIgnoreCase("Đang giao")) {
            JOptionPane.showMessageDialog(null, "Hóa đơn này đang ở trạng thái Đang giao, không thể chuyển lại!");
            return;
        }

        try {
            // Làm sạch dữ liệu tiền
            String tongTienStr = TF_Tongtien.getText().replace(",", "").trim();
            String khachTraStr = TF_Khachdua.getText().replace(",", "").trim();
            String tienThuaStr = TF_Tienthua.getText().replace(",", "").trim();

            float tongTien = parseSafeFloat(tongTienStr);
            float khachTra = parseSafeFloat(khachTraStr);
            float tienThua = parseSafeFloat(tienThuaStr);

            String ptThanhToan = (jComboBox1.getSelectedItem() == null) ? "" : jComboBox1.getSelectedItem().toString();
            String phuongThuc = (jComboBox2.getSelectedItem() == null) ? "" : jComboBox2.getSelectedItem().toString();
            String trangThai = "Đang giao";

            // Lấy mã nhân viên hiện tại từ form (hoặc biến quản lý)
            String maNV = TF_MaNV.getText().trim();  // Bạn cần có TF_MaNV hoặc thay bằng biến mã NV hiện tại

            // Kiểm tra nếu là giao hàng thì không được chọn "Quẹt thẻ"
            if (phuongThuc.equalsIgnoreCase("Giao hàng") && ptThanhToan.equalsIgnoreCase("Quẹt thẻ")) {
                JOptionPane.showMessageDialog(null, "Giao hàng chỉ hỗ trợ Tiền mặt hoặc Chuyển khoản!");
                return;
            }

            boolean success = hddao.capNhatThongTinGiaoHang(
                    maHD, tongTien, khachTra, tienThua,
                    ptThanhToan, phuongThuc, trangThai, maNV // thêm mã NV
            );

            if (success) {
                JOptionPane.showMessageDialog(null, "Đã chuyển sang trạng thái: Đang giao");
                fillTablehdon();

                // Lấy thêm thông tin để in hóa đơn
                String tenKH = TF_TenKH.getText();
                String sdt = TF_sdt.getText();
                String formattedTongTien = TF_Tongtien.getText();
                String formattedKhachTra = TF_Khachdua.getText();
                String formattedTienThua = TF_Tienthua.getText();

                // In hóa đơn theo phương thức thanh toán
                if (ptThanhToan.equalsIgnoreCase("Tiền mặt") || ptThanhToan.equalsIgnoreCase("Chuyển khoản")) {
                    inhdonTuDong(maHD, tenKH, sdt, formattedTongTien, formattedKhachTra, formattedTienThua, ptThanhToan, phuongThuc, tongTien);
                }

                // Reset form
                TF_Mahd.setText("");
                TF_Tongtien.setText("");
                TF_Khachdua.setText("");
                TF_Tienthua.setText("");
                jComboBox1.setSelectedIndex(-1);
                jComboBox2.setSelectedIndex(-1);
                jTextArea1.setText("");
                ((DefaultTableModel) tbhdonct.getModel()).setRowCount(0);

            } else {
                JOptionPane.showMessageDialog(null, "Cập nhật thất bại!");
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Vui lòng nhập đúng định dạng số tiền!");
            e.printStackTrace();
        }


    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        dagiao();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
        // TODO add your handling code here:
        taoHoaDon();
    }//GEN-LAST:event_jButton14ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        huyHoaDon();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void tbhdon123MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbhdon123MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tbhdon123MouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField TF_Khachdua;
    private javax.swing.JLabel TF_MaNV;
    private javax.swing.JLabel TF_Mahd;
    private javax.swing.JLabel TF_TenKH;
    private javax.swing.JLabel TF_Tienthua;
    private javax.swing.JLabel TF_Tongtien;
    private javax.swing.JLabel TF_sdt;
    private javax.swing.JButton btnThem;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTable tbhdon123;
    private javax.swing.JTable tbhdonct;
    private javax.swing.JTable tbsp;
    // End of variables declaration//GEN-END:variables
}
