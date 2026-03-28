/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package View;

import DAO.HoaDonDao;
import DAO.SanPhamDAO;
import DAO.ThongKeDAO;
import Model.Guidoanhthu;
import Model.HoaDon;
import Model.SanPham;
import Model.TopSanPham;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth; // Không cần thiết nếu không dùng cho tháng
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

// Thêm các import cần thiết cho JCalendar, Dialog và MouseListener
import com.toedter.calendar.JDateChooser;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class QLTKE extends javax.swing.JPanel {

    private ThongKeDAO thongKeDAO;
    private DefaultTableModel hoaDonTableModel;
    private DefaultTableModel topSanPhamTableModel;
    private HoaDonDao hddao = new HoaDonDao();
    private SanPhamDAO spdao = new SanPhamDAO();
    private static final DateTimeFormatter UI_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final SimpleDateFormat UI_DATE_DISPLAY_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    // Các biến thành phần UI (được khai báo trong initComponents() và phần cuối file)
    // KHÔNG KHAI BÁO LẠI Ở ĐÂY, CHỈ DÙNG CÁC BIẾN CÓ SẴN.
    // Constructor của QLTKE
    public QLTKE() {
        initComponents(); // Giữ nguyên dòng này để khởi tạo các thành phần GUI
        thongKeDAO = new ThongKeDAO();
        setupTables();       
        initData(); // Gọi initData để thiết lập ngày mặc định và dữ liệu ban đầu
        initTable();
        updateGeneralStatistics(); // Cập nhật thống kê chung ban đầu (bao gồm tồn kho)
        JDateChooser startDateChooser = new JDateChooser();
        JDateChooser endDateChooser = new JDateChooser();
        // Thêm một listener ngay lập tức để cập nhật doanh thu/đơn hàng
        // dựa trên ngày mặc định của TF_MaKH1 và TF_MaKH3 khi khởi tạo
        LocalDate startDate = LocalDate.parse(TF_MaKH1.getText(), UI_DATE_FORMATTER);
        LocalDate endDate = LocalDate.parse(TF_MaKH3.getText(), UI_DATE_FORMATTER);
        updateGeneralStatisticsForPeriod(startDate, endDate);
        TF_MaKH1.setText(""); // Bắt đầu với ô ngày bắt đầu trống
        TF_MaKH3.setText("");
        jLabel_DoanhThu.setText("");
        jLabel_DonHang.setText("");

        TF_SoLuongCanhBao.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                loadCanhBao();
            }

            public void removeUpdate(DocumentEvent e) {
                loadCanhBao();
            }

            public void changedUpdate(DocumentEvent e) {
            }

            private void loadCanhBao() {
                String input = TF_SoLuongCanhBao.getText().trim();
                if (!input.matches("\\d+")) {
                    return;
                }

                int nguong = Integer.parseInt(input);
                List<SanPham> list = thongKeDAO.getSanPhamCanhBaoSoLuong(nguong);

                DefaultTableModel model = new DefaultTableModel(
                        new String[]{"Mã SP", "Tên SP", "Loại SP", "Giá", "Số lượng", "Màu sắc", "Kích thước", "Chất liệu", "Trạng thái"}, 0
                );

                for (SanPham sp : list) {
                    model.addRow(new Object[]{
                        sp.getMasp(),
                        sp.getTensp(),
                        sp.getLoaisp(),
                        sp.getGia(),
                        sp.getSluong(),
                        sp.getMausac(),
                        sp.getKichThuoc(),
                        sp.getChatLieu(),
                        sp.getTrangThai()
                    });
                }

                tbl_CanhBaoSL.setModel(model); // GÁN MODEL TRƯỚC

                // SAU ĐÓ GÁN RENDERER
                DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value,
                            boolean isSelected, boolean hasFocus, int row, int column) {

                        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                        try {
                            int sl = Integer.parseInt(table.getValueAt(row, 4).toString()); // cột số lượng

                            if (sl < 10) {
                                c.setBackground(new Color(255, 80, 80)); // đỏ đậm
                            } else if (sl < 20) {
                                c.setBackground(new Color(255, 170, 100)); // cam nhạt
                            } else {
                                c.setBackground(Color.WHITE); // bình thường
                            }

                            if (isSelected) {
                                c.setBackground(c.getBackground().darker());
                            }

                        } catch (Exception e) {
                            c.setBackground(Color.WHITE);
                        }

                        return c;
                    }
                };

                // Gán renderer cho tất cả cột sau khi setModel
                for (int i = 0; i < tbl_CanhBaoSL.getColumnCount(); i++) {
                    tbl_CanhBaoSL.getColumnModel().getColumn(i).setCellRenderer(renderer);
                }
            }

        });

        btnLocTop5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String startText = TF_nbday.getText().trim();
                    String endText = TF_nkthuc.getText().trim();

                    if (startText.isEmpty() || endText.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Vui lòng nhập ngày bắt đầu và kết thúc!");
                        return;
                    }

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate startDate = LocalDate.parse(startText, formatter);
                    LocalDate endDate = LocalDate.parse(endText, formatter);

                    // Tạo model mới với tiêu đề cột
                    DefaultTableModel model = new DefaultTableModel(
                            new Object[]{"Mã sản phẩm", "Tên sản phẩm", "Số lượng bán"}, 0
                    );

                    // Gán lại model cho bảng (đảm bảo bảng có cột)
                    tblTopSanPham.setModel(model);

                    // Lấy dữ liệu từ DAO
                    List<TopSanPham> topList = thongKeDAO.getTop5SanPhamByDateRange(startDate, endDate);

                    for (TopSanPham sp : topList) {
                        model.addRow(new Object[]{
                            sp.getMaSanPham(),
                            sp.getTenSanPham(),
                            sp.getTongSoLuongBan()
                        });
                    }

                } catch (DateTimeParseException dtpe) {
                    JOptionPane.showMessageDialog(null, "Ngày không đúng định dạng! (yyyy-MM-dd)");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Lỗi khi tìm sản phẩm: " + ex.getMessage());
                }
            }
        });

        // THÊM MOUSELISTENER VÀO TF_MaKH1 VÀ TF_MaKH3 CHO CHỌN NGÀY BẰNG LỊCH (PHẦN TRÊN)
        TF_MaKH1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showDateChooserDialog(TF_MaKH1);
            }
        });

        TF_MaKH3.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showDateChooserDialog(TF_MaKH3);
            }
        });

        rdoNgay.addActionListener(e -> hienThiThongKeTheoNgay());
        rdoThang.addActionListener(e -> hienThiThongKeTheoThang());
        rdoNam.addActionListener(e -> hienThiThongKeTheoNam());

        btnGuiBaoCao.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // ✅ Kiểm tra xem đã chọn radio nào chưa
                if (!rdoNgay.isSelected() && !rdoThang.isSelected() && !rdoNam.isSelected()) {
                    JOptionPane.showMessageDialog(null, "Vui lòng chọn kiểu thống kê: Ngày, Tháng hoặc Năm trước khi gửi báo cáo!");
                    return; // ❌ Không cho gửi tiếp
                }

                try {
                    String tieuDe = "";
                    String loaiLoc = "";
                    String thoiGianHienThi = "";
                    List<HoaDon> danhSach;

                    Guidoanhthu gui = new Guidoanhthu();

                    // ✅ Xử lý theo từng radio
                    if (rdoNgay.isSelected()) {
                        loaiLoc = "NGAY";
                        LocalDate ngay = LocalDate.now(); // hoặc lấy từ DatePicker
                        danhSach = hddao.getHoaDonTheoThoiGian(loaiLoc, ngay);
                        tieuDe = gui.taoTieuDeBaoCao("ngay", ngay, 0, 0);
                        thoiGianHienThi = ngay.toString();
                    } else if (rdoThang.isSelected()) {
                        loaiLoc = "THANG";
                        int thang = LocalDate.now().getMonthValue(); // hoặc lấy từ ComboBox
                        int nam = LocalDate.now().getYear();
                        danhSach = hddao.getHoaDonTheoThoiGian(loaiLoc, LocalDate.now());
                        tieuDe = gui.taoTieuDeBaoCao("thang", null, thang, nam);
                        thoiGianHienThi = "Tháng " + thang + "/" + nam;
                    } else {
                        loaiLoc = "NAM";
                        int nam = LocalDate.now().getYear();
                        danhSach = hddao.getHoaDonTheoThoiGian(loaiLoc, LocalDate.now());
                        tieuDe = gui.taoTieuDeBaoCao("nam", null, 0, nam);
                        thoiGianHienThi = "Năm " + nam;
                    }

                    // ✅ Tính tổng doanh thu
                    double tongDoanhThu = danhSach.stream().mapToDouble(HoaDon::getTongTien).sum();

                    // ✅ Gửi email
                    String noiDung = gui.taoNoiDungBaoCao(danhSach, tongDoanhThu, thoiGianHienThi);
                    gui.guiBaoCaoEmail("minhthth06876@gmail.com", tieuDe, noiDung);

                    JOptionPane.showMessageDialog(null, "✅ Đã gửi báo cáo qua email cho chủ cửa hàng!");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "❌ Lỗi khi gửi email báo cáo!");
                }
            }
        });

        tabMain.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int selectedIndex = tabMain.getSelectedIndex();

                if (selectedIndex == 1) { // Tab Biểu đồ
                    // Reset các label thống kê
                    jLabel_DoanhThu.setText("");
                    jLabel_DonHang.setText("");
                    DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
                    model.setRowCount(0);

                } else if (selectedIndex == 0) { // Tab Tìm kiếm
                    // Reset dữ liệu tìm kiếm
                    jLabel_DoanhThu.setText("");
                    jLabel_DonHang.setText("");
                    TF_MaKH1.setText(""); // Bắt đầu với ô ngày bắt đầu trống
                    TF_MaKH3.setText("");
                    buttonGroup1.clearSelection();

                    DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
                    model.setRowCount(0);
                }
            }
        });

        rdoNgay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                capNhatSoLieu("NGAY");
            }
        });

        rdoThang.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                capNhatSoLieu("THANG");
            }
        });

        rdoNam.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                capNhatSoLieu("NAM");
            }
        });

        btnBieuDo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String kieuLoc = "";
                    if (rdoNgay.isSelected()) {
                        kieuLoc = "NGAY";
                    } else if (rdoThang.isSelected()) {
                        kieuLoc = "THANG";
                    } else if (rdoNam.isSelected()) {
                        kieuLoc = "NAM";
                    } else {
                        JOptionPane.showMessageDialog(null, "Vui lòng chọn kiểu lọc (ngày/tháng/năm)");
                        return;
                    }

                    LocalDate now = LocalDate.now();
                    List<HoaDon> danhSach = hddao.getHoaDonTheoThoiGian(kieuLoc, now);
                    hienThiBieuDoDoanhThu(danhSach, kieuLoc);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Lỗi khi tạo biểu đồ!");
                }
            }
        });

        btnBieuDoHoaDon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String kieuLoc = "";
                    if (rdoNgay.isSelected()) {
                        kieuLoc = "NGAY";
                    } else if (rdoThang.isSelected()) {
                        kieuLoc = "THANG";
                    } else if (rdoNam.isSelected()) {
                        kieuLoc = "NAM";
                    } else {
                        JOptionPane.showMessageDialog(null, "Vui lòng chọn kiểu lọc (ngày/tháng/năm)");
                        return;
                    }

                    LocalDate now = LocalDate.now();
                    List<HoaDon> danhSach = hddao.getHoaDonTheoThoiGian(kieuLoc, now);
                    hienThiBieuDoHoaDon(danhSach, kieuLoc);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Lỗi khi tạo biểu đồ!");
                }
            }
        });

        btnLocTop5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                validateAndLoadData();
            }
        });

// Thêm phím tắt Enter cho trường ngày kết thúc
        TF_nkthuc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                validateAndLoadData();
            }
        });
        // Thay thế toàn bộ MouseListener và FocusListener bằng cái này
        TF_nbday.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                showSingleDateChooserDialog(TF_nbday);
            }
        });

        TF_nkthuc.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                showSingleDateChooserDialog(TF_nkthuc);
            }
        });

// Xóa tất cả các FocusListener cũ
    }

    public void initTable() {
        String[] cols = new String[]{"Mã SP", "Tên SP", "Số Lượng"};
        topSanPhamTableModel = new DefaultTableModel();
        topSanPhamTableModel.setColumnIdentifiers(cols);
        tblTopSanPham.setModel(topSanPhamTableModel);

    }

    private void validateAndLoadData() {
        try {
            String startText = TF_nbday.getText().trim();
            String endText = TF_nkthuc.getText().trim();

            if (startText.isEmpty() || endText.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Vui lòng nhập ngày bắt đầu và kết thúc!");
                return;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startDate = LocalDate.parse(startText, formatter);
            LocalDate endDate = LocalDate.parse(endText, formatter);

            List<TopSanPham> topList = thongKeDAO.getTop5SanPhamByDateRange(startDate, endDate);

            DefaultTableModel model = (DefaultTableModel) tblTopSanPham.getModel();
            model.setRowCount(0);

            for (TopSanPham sp : topList) {
                model.addRow(new Object[]{
                    sp.getMaSanPham(),
                    sp.getTenSanPham(),
                    sp.getTongSoLuongBan()
                });
            }

        } catch (DateTimeParseException dtpe) {
            JOptionPane.showMessageDialog(null, "Ngày không đúng định dạng! (yyyy-MM-dd)");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi tải dữ liệu: " + ex.getMessage());
        }
    }

    private void loadTopSanPham(LocalDate startDate, LocalDate endDate) {
        try {
            List<TopSanPham> topList = new ThongKeDAO().getTop5SanPhamByDateRange(startDate, endDate);
            DefaultTableModel model = (DefaultTableModel) tblTopSanPham.getModel();
            model.setRowCount(0); // Xóa dữ liệu cũ

            for (TopSanPham sp : topList) {
                model.addRow(new Object[]{
                    sp.getMaSanPham(),
                    sp.getTenSanPham(),
                    sp.getTongSoLuongBan()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu sản phẩm bán chạy!");
        }
    }

// Phương thức kiểm tra định dạng ngày
    private boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr);
            return true;
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Ngày không đúng định dạng (yyyy-MM-dd): " + dateStr, "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private String taoKeyTuDate(Date ngay, String kieuLoc) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(ngay);

        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);

        switch (kieuLoc) {
            case "NGAY":
                return String.format("%02d-%02d-%d", day, month, year); // dd-MM-yyyy
            case "THANG":
                return String.format("%02d-%d", month, year); // MM-yyyy
            case "NAM":
                return String.valueOf(year); // yyyy
            default:
                return "";
        }
    }

    private void hienThiBieuDoDoanhThu(List<HoaDon> danhSach, String kieuLoc) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Double> duLieu = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter;

        switch (kieuLoc) {
            case "NGAY":
                formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                for (int i = 6; i >= 0; i--) {
                    LocalDate date = today.minusDays(i);
                    duLieu.put(date.format(formatter), 0.0);
                }
                break;

            case "THANG":
                formatter = DateTimeFormatter.ofPattern("dd-MM"); // Chỉ ngày - tháng
                LocalDate startOfMonth = today.withDayOfMonth(1);
                int lengthOfMonth = today.lengthOfMonth();
                for (int i = 0; i < lengthOfMonth; i++) {
                    LocalDate date = startOfMonth.plusDays(i);
                    duLieu.put(date.format(formatter), 0.0);
                }
                break;

            case "NAM":
                for (int i = 1; i <= 12; i++) {
                    duLieu.put(String.format("Tháng %02d", i), 0.0);
                }
                break;
        }

        // Cộng dồn doanh thu
        for (HoaDon hd : danhSach) {
            Date ngay = hd.getNgayTao();
            double tien = hd.getTongTien();
            Calendar cal = Calendar.getInstance();
            cal.setTime(ngay);

            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH) + 1;
            int year = cal.get(Calendar.YEAR);

            String key = "";
            switch (kieuLoc) {
                case "NGAY":
                    formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    key = LocalDate.of(year, month, day).format(formatter);
                    break;
                case "THANG":
                    formatter = DateTimeFormatter.ofPattern("dd-MM");
                    key = LocalDate.of(year, month, day).format(formatter);
                    break;
                case "NAM":
                    key = String.format("Tháng %02d", month);
                    break;
            }

            if (duLieu.containsKey(key)) {
                duLieu.put(key, duLieu.get(key) + tien);
            }
        }

        for (Map.Entry<String, Double> entry : duLieu.entrySet()) {
            dataset.addValue(entry.getValue(), "Doanh thu", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Biểu đồ Doanh thu theo " + kieuLoc.toLowerCase(),
                kieuLoc.equals("NGAY") ? "Ngày"
                : kieuLoc.equals("THANG") ? "Ngày trong tháng"
                : "Tháng",
                "Doanh thu (VND)",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false);

        // Hiển thị nhãn theo chiều ngang dễ đọc
        CategoryPlot plot = chart.getCategoryPlot();
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);

        // Kích thước biểu đồ tùy theo kiểu lọc
        int width = switch (kieuLoc) {
            case "NGAY" ->
                900;   // 7 ngày
            case "THANG" ->
                1800;  // 31 ngày
            case "NAM" ->
                1800;    // 12 tháng
            default ->
                1000;
        };

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(width, 500));
        chartPanel.setMouseWheelEnabled(false);

        JScrollPane scrollPane = new JScrollPane(chartPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JFrame frame = new JFrame("Biểu đồ Doanh thu");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(scrollPane);
        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void hienThiBieuDoHoaDon(List<HoaDon> danhSach, String kieuLoc) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Integer> duLieu = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter;

        switch (kieuLoc) {
            case "NGAY":
                formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                for (int i = 6; i >= 0; i--) {
                    LocalDate date = today.minusDays(i);
                    duLieu.put(date.format(formatter), 0);
                }
                break;

            case "THANG":
                formatter = DateTimeFormatter.ofPattern("dd-MM"); // Chỉ ngày-tháng
                LocalDate startOfMonth = today.withDayOfMonth(1);
                int lengthOfMonth = startOfMonth.lengthOfMonth();
                for (int i = 0; i < lengthOfMonth; i++) {
                    LocalDate date = startOfMonth.plusDays(i);
                    duLieu.put(date.format(formatter), 0);
                }
                break;

            case "NAM":
                for (int i = 1; i <= 12; i++) {
                    duLieu.put(String.format("Tháng %02d", i), 0);
                }
                break;
        }

        // Đếm số lượng hóa đơn theo key
        for (HoaDon hd : danhSach) {
            Date ngay = hd.getNgayTao();
            Calendar cal = Calendar.getInstance();
            cal.setTime(ngay);

            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH) + 1;
            int year = cal.get(Calendar.YEAR);

            String key = "";
            switch (kieuLoc) {
                case "NGAY":
                    formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    key = LocalDate.of(year, month, day).format(formatter);
                    break;
                case "THANG":
                    formatter = DateTimeFormatter.ofPattern("dd-MM");
                    key = LocalDate.of(year, month, day).format(formatter);
                    break;

                case "NAM":
                    key = String.format("Tháng %02d", month);
                    break;
            }

            if (duLieu.containsKey(key)) {
                duLieu.put(key, duLieu.get(key) + 1);
            }
        }

        for (Map.Entry<String, Integer> entry : duLieu.entrySet()) {
            dataset.addValue(entry.getValue(), "Số lượng HĐ", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Biểu đồ Số lượng Hóa đơn theo " + kieuLoc.toLowerCase(),
                kieuLoc.equals("NGAY") ? "Ngày" : kieuLoc.equals("THANG") ? "Ngày trong tháng" : "Tháng",
                "Số lượng",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false);

        // Để nhãn không bị nghiêng
        CategoryPlot plot = chart.getCategoryPlot();
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);

        // Trục Y là số nguyên
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        int width;
        if (kieuLoc.equals("NAM")) {
            width = 1200;
        } else if (kieuLoc.equals("THANG")) {
            width = 1800;
        } else {
            width = 900;
        }

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(width, 500));
        chartPanel.setMouseWheelEnabled(false); // Tắt zoom bằng chuột

        JScrollPane scrollPane = new JScrollPane(chartPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JFrame frame = new JFrame("Biểu đồ Hóa đơn");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(scrollPane);
        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void capNhatSoLieu(String kieuLoc) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate startDate;
            LocalDate endDate;

            switch (kieuLoc) {
                case "NGAY":
                    startDate = today;
                    endDate = today;
                    break;
                case "THANG":
                    startDate = today.withDayOfMonth(1);
                    endDate = today.withDayOfMonth(today.lengthOfMonth());
                    break;
                case "NAM":
                    startDate = today.withDayOfYear(1);
                    endDate = today.withMonth(12).withDayOfMonth(31);
                    break;
                default:
                    throw new IllegalArgumentException("Kiểu lọc không hợp lệ");
            }

            // Lấy danh sách hóa đơn theo thời gian
            List<HoaDon> danhSach = hddao.getHoaDonTheoThoiGian(kieuLoc, today);

// Lấy top 5 sản phẩm theo khoảng thời gian
            List<TopSanPham> topSP = thongKeDAO.getTop5SanPhamByDateRange(startDate, endDate);

// Tính tổng số hóa đơn và tổng doanh thu
            int soHD = danhSach.size();
            double tongDoanhThu = danhSach.stream().mapToDouble(HoaDon::getTongTien).sum();

// Tổng tồn kho
            int tongTonKho = thongKeDAO.getTongSoLuongTonKho();

// Gán vào các label thống kê
            jLabel_DonHang.setText(String.valueOf(soHD));
            jLabel_DoanhThu.setText(String.format("%,.0f VND", tongDoanhThu));
            jLabel_TonKho.setText(String.valueOf(tongTonKho));

// Cập nhật table top sản phẩm bán chạy
            topSanPhamTableModel.setRowCount(0); // Xoá dữ liệu cũ
            for (TopSanPham sp : topSP) {
                topSanPhamTableModel.addRow(new Object[]{
                    sp.getTenSanPham(),
                    sp.getTongSoLuongBan(),
                    String.format("%,.0f VND", sp.getTongDoanhThu())
                });
            }

// Đổ dữ liệu vào bảng jtbhethang
            topSanPhamTableModel.setRowCount(0);
            int stt = 1;
            for (TopSanPham sp : topSP) {
                topSanPhamTableModel.addRow(new Object[]{
                    stt++,
                    sp.getTenSanPham(),
                    sp.getTongSoLuongBan(),
                    String.format("%,.0f VND", sp.getTongDoanhThu())
                });
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi cập nhật số liệu biểu đồ");
        }
    }

    private void hienThiThongKeTheoNgay() {
        Date today = new Date();

        double doanhThu = thongKeDAO.getDoanhThuTheoNgay(today);
        int soDonHang = thongKeDAO.getSoDonHangTheoNgay(today);
        int tonKho = thongKeDAO.getTongSoLuongTonKho();

        jLabel_DoanhThu.setText(String.format("%,.0f", doanhThu));
        jLabel_DonHang.setText(String.valueOf(soDonHang));
        jLabel_TonKho.setText(String.valueOf(tonKho));

        List<HoaDon> list = thongKeDAO.getHoaDonTheoNgay(today);
        DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
        model.setRowCount(0);
        for (HoaDon hd : list) {
            model.addRow(new Object[]{
                hd.getMahd(),
                hd.getManv(),
                hd.getTenkh(),
                hd.getSdt(),
                hd.getTrangThai(),
                hd.getNgayTao(),
                String.format("%,.0f", hd.getTongTien()),
                String.format("%,.0f", hd.getTienTra()),
                String.format("%,.0f", hd.getTienThua()),
                hd.getThanhToan(),
                hd.getGiaoHang(),
                hd.getGhiChu()
            });
        }
    }

    public void guiBaoCaoEmail(String toEmail, String subject, String noiDung) {
        final String fromEmail = "youremail@gmail.com"; // Gmail bạn
        final String password = "your-app-password"; // Mật khẩu ứng dụng (không dùng mật khẩu thật)

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(noiDung);

            Transport.send(message);
            System.out.println("Gửi mail thành công!");

        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Gửi mail thất bại!");
        }
    }

    private void hienThiThongKeTheoThang() {
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        double doanhThu = thongKeDAO.getDoanhThuTheoThang(month, year);
        int soDonHang = thongKeDAO.getSoDonHangTheoThang(month, year);
        int tonKho = thongKeDAO.getTongSoLuongTonKho();

        jLabel_DoanhThu.setText(String.format("%,.0f", doanhThu));
        jLabel_DonHang.setText(String.valueOf(soDonHang));
        jLabel_TonKho.setText(String.valueOf(tonKho));

        List<HoaDon> list = thongKeDAO.getHoaDonTheoThang(month, year);
        DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
        model.setRowCount(0);
        for (HoaDon hd : list) {
            model.addRow(new Object[]{
                hd.getMahd(),
                hd.getManv(),
                hd.getTenkh(),
                hd.getSdt(),
                hd.getTrangThai(),
                hd.getNgayTao(),
                String.format("%,.0f", hd.getTongTien()),
                String.format("%,.0f", hd.getTienTra()),
                String.format("%,.0f", hd.getTienThua()),
                hd.getThanhToan(),
                hd.getGiaoHang(),
                hd.getGhiChu()
            });
        }
    }

    private void hienThiThongKeTheoNam() {
        int year = LocalDate.now().getYear();

        double doanhThu = thongKeDAO.getDoanhThuTheoNam(year);
        int soDonHang = thongKeDAO.getSoDonHangTheoNam(year);
        int tonKho = thongKeDAO.getTongSoLuongTonKho();

        jLabel_DoanhThu.setText(String.format("%,.0f", doanhThu));
        jLabel_DonHang.setText(String.valueOf(soDonHang));
        jLabel_TonKho.setText(String.valueOf(tonKho));

        List<HoaDon> list = thongKeDAO.getHoaDonTheoNam(year);
        DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
        model.setRowCount(0);
        for (HoaDon hd : list) {
            model.addRow(new Object[]{
                hd.getMahd(),
                hd.getManv(),
                hd.getTenkh(),
                hd.getSdt(),
                hd.getTrangThai(),
                hd.getNgayTao(),
                String.format("%,.0f", hd.getTongTien()),
                String.format("%,.0f", hd.getTienTra()),
                String.format("%,.0f", hd.getTienThua()),
                hd.getThanhToan(),
                hd.getGiaoHang(),
                hd.getGhiChu()
            });
        }
    }

    private void hienThiThongKeNgayHomNay() {
        Date today = new Date();

        double doanhThu = thongKeDAO.getDoanhThuTheoNgay(today);
        int soDonHang = thongKeDAO.getSoDonHangTheoNgay(today);
        int tonKho = thongKeDAO.getTongSoLuongTonKho();

        jLabel_DoanhThu.setText(String.format("%,.0f", doanhThu));
        jLabel_DonHang.setText(String.valueOf(soDonHang));
        jLabel_TonKho.setText(String.valueOf(tonKho));

        List<HoaDon> list = thongKeDAO.getHoaDonTheoNgay(today);
        DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
        model.setRowCount(0); // clear table
        for (HoaDon hd : list) {
            model.addRow(new Object[]{
                hd.getMahd(),
                hd.getNgayTao(),
                String.format("%,.0f", hd.getTongTien())
            });
        }
    }

    // KHÔNG BAO GỒM initComponents() Ở ĐÂY.
    private void updateGeneralStatistics() {
        try {
            int totalStock = thongKeDAO.getTongSoLuongTonKho();
            jLabel_TonKho.setText(String.valueOf(totalStock));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật số liệu tồn kho: " + e.getMessage(), "Lỗi DB", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void updateGeneralStatisticsForPeriod(LocalDate startDate, LocalDate endDate) {
        try {
            double totalRevenue = thongKeDAO.getTongDoanhThuByDateRange(startDate, endDate);
            jLabel_DoanhThu.setText(String.format("%,.0f VNĐ", totalRevenue));

            int totalOrders = thongKeDAO.getTongDonHangByDateRange(startDate, endDate);
            jLabel_DonHang.setText(String.valueOf(totalOrders));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật số liệu thống kê theo khoảng ngày: " + e.getMessage(), "Lỗi DB", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    
    private void initData() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        // Đặt ngày mặc định cho TF_MaKH1 và TF_MaKH3 (phần trên)
        TF_MaKH1.setText(firstDayOfMonth.format(UI_DATE_FORMATTER));
        TF_MaKH3.setText(lastDayOfMonth.format(UI_DATE_FORMATTER));

        // Đặt ngày mặc định cho jComboBox1 và jComboBox3 (phần dưới - nay là ngày)
        // Để trống mặc định để khi khởi tạo, tìm kiếm sẽ theo năm.
        TF_nbday.setText("");
        TF_nkthuc.setText("");

        
    }

    private void setupTables() {
        hoaDonTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        hoaDonTableModel.addColumn("Mã HĐ");
        hoaDonTableModel.addColumn("Mã NV");
        hoaDonTableModel.addColumn("Tên KH");
        hoaDonTableModel.addColumn("SĐT");
        hoaDonTableModel.addColumn("Trạng thái");
        hoaDonTableModel.addColumn("Ngày tạo");
        hoaDonTableModel.addColumn("Tổng tiền");
        hoaDonTableModel.addColumn("Tiền trả");
        hoaDonTableModel.addColumn("Tiền thừa");
        hoaDonTableModel.addColumn("Thanh toán");
        hoaDonTableModel.addColumn("Giao hàng");
        hoaDonTableModel.addColumn("Ghi chú");
        jTable2.setModel(hoaDonTableModel);

    }

    private void fillTableHoaDon(List<HoaDon> list) {
    DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
    model.setRowCount(0);

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    DecimalFormat df = new DecimalFormat("#,###");

    for (HoaDon hd : list) {
        model.addRow(new Object[]{
            hd.getMahd(),
            hd.getManv(),
            hd.getTenkh(),
            hd.getSdt(),
            hd.getTrangThai(),
            sdf.format(hd.getNgayTao()),
            df.format(hd.getTongTien()),
            df.format(hd.getTienTra()),
            df.format(hd.getTienThua()),
            hd.getThanhToan(),
            hd.getGiaoHang(),
            hd.getGhiChu()
        });
    }

    // Đặt độ rộng cụ thể cho từng cột (đơn vị: pixel)
    jTable2.getColumnModel().getColumn(0).setPreferredWidth(80);   // MS-HD
    jTable2.getColumnModel().getColumn(1).setPreferredWidth(80);   // MS NV
    jTable2.getColumnModel().getColumn(2).setPreferredWidth(130);  // Tên KH
    jTable2.getColumnModel().getColumn(3).setPreferredWidth(100);  // SĐT
    jTable2.getColumnModel().getColumn(4).setPreferredWidth(120);  // Trạng thái
    jTable2.getColumnModel().getColumn(5).setPreferredWidth(100);  // Ngày tạo
    jTable2.getColumnModel().getColumn(6).setPreferredWidth(120);  // Tổng tiền (ưu tiên rộng)
    jTable2.getColumnModel().getColumn(7).setPreferredWidth(150);  // Tiền trả (ưu tiên rộng)
    jTable2.getColumnModel().getColumn(8).setPreferredWidth(150);   // Tiền thừa
    jTable2.getColumnModel().getColumn(9).setPreferredWidth(100);  // Thanh toán
    jTable2.getColumnModel().getColumn(10).setPreferredWidth(100); // Giao hàng
    jTable2.getColumnModel().getColumn(11).setPreferredWidth(100); // Ghi chú

    // Tắt chế độ tự động resize để áp dụng độ rộng tùy chỉnh
    jTable2.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
}

    private void fillTopSanPhamTable(List<TopSanPham> list) {
        topSanPhamTableModel.setRowCount(0);

        if (list == null || list.isEmpty()) {
            return;
        }

        for (TopSanPham tsp : list) {
            topSanPhamTableModel.addRow(new Object[]{
                tsp.getTenSanPham(),
                tsp.getTongSoLuongBan(),
                String.format("%,.0f VNĐ", tsp.getTongDoanhThu())
            });
        }
    }

    // Phương thức kiểm tra định dạng ngày
    // Điều chỉnh: Không hiển thị JOptionPane nếu chuỗi ngày trống, chỉ trả về false
    private boolean isValidDate(String dateStr, JTextField field) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return false; // Trả về false nếu trống, không phải là lỗi để có thể tìm theo năm
        }
        try {
            LocalDate.parse(dateStr, UI_DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Ngày không đúng định dạng YYYY-MM-DD: " + dateStr, "Lỗi định dạng ngày", JOptionPane.WARNING_MESSAGE);
            field.requestFocusInWindow();
            return false;
        }
    }

    private void showSingleDateChooserDialog(JTextField field) {
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");

        // Thiết lập ngày hiện tại nếu trường đã có giá trị
        if (!field.getText().isEmpty()) {
            try {
                Date currentDate = new SimpleDateFormat("yyyy-MM-dd").parse(field.getText());
                dateChooser.setDate(currentDate);
            } catch (ParseException e) {
                // Không cần thông báo lỗi ở đây
            }
        }

        // Tạo dialog tùy chỉnh
        JDialog dialog = new JDialog();
        dialog.setTitle("Chọn ngày");
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());
        dialog.add(dateChooser, BorderLayout.CENTER);

        // Panel chứa nút OK
        JPanel buttonPanel = new JPanel();
        JButton btnOK = new JButton("OK");
        btnOK.addActionListener(e -> {
            Date selectedDate = dateChooser.getDate();
            if (selectedDate != null) {
                field.setText(new SimpleDateFormat("yyyy-MM-dd").format(selectedDate));
            }
            dialog.dispose();
        });
        buttonPanel.add(btnOK);

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private void showDateChooserDialog(JTextField field) {
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");

        // Tạo dialog tùy chỉnh
        JDialog dialog = new JDialog();
        dialog.setTitle("Chọn ngày");
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());
        dialog.add(dateChooser, BorderLayout.CENTER);

        // Thêm nút OK
        JButton btnOK = new JButton("OK");
        btnOK.addActionListener(e -> {
            Date selectedDate = dateChooser.getDate();
            if (selectedDate != null) {
                String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate);
                field.setText(formattedDate);
            }
            dialog.dispose();
        });

        JPanel panel = new JPanel();
        panel.add(btnOK);
        dialog.add(panel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel5 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel2 = new javax.swing.JLabel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel_DoanhThu = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel_DonHang = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel_TonKho = new javax.swing.JLabel();
        tabMain = new javax.swing.JTabbedPane();
        jPanel9 = new javax.swing.JPanel();
        btnTim = new javax.swing.JButton();
        TF_MaKH3 = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        TF_MaKH1 = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        rdoNgay = new javax.swing.JRadioButton();
        rdoThang = new javax.swing.JRadioButton();
        rdoNam = new javax.swing.JRadioButton();
        btnBieuDo = new javax.swing.JButton();
        btnBieuDoHoaDon = new javax.swing.JButton();
        btnGuiBaoCao = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbl_CanhBaoSL = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        btnLocTop5 = new javax.swing.JButton();
        TF_nbday = new javax.swing.JTextField();
        jLabel29 = new javax.swing.JLabel();
        TF_nkthuc = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblTopSanPham = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        TF_SoLuongCanhBao = new javax.swing.JTextField();

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        jLabel8.setText("Thống kê");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel9.setText("00");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(60, 60, 60)
                        .addComponent(jLabel8))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(108, 108, 108)
                        .addComponent(jLabel9)))
                .addContainerGap(66, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel8)
                .addGap(63, 63, 63)
                .addComponent(jLabel9)
                .addContainerGap(58, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel2.setText("Thống kê");

        jPanel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

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

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        jLabel1.setText("Doanh thu");

        jLabel_DoanhThu.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel_DoanhThu.setText("00");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        jLabel4.setText("Đơn hàng");

        jLabel_DonHang.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel_DonHang.setText("00");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        jLabel10.setText("Tồn kho");

        jLabel_TonKho.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel_TonKho.setText("00");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(152, 152, 152)
                        .addComponent(jLabel1)
                        .addGap(273, 273, 273)
                        .addComponent(jLabel4)
                        .addGap(359, 359, 359))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(144, 144, 144)
                        .addComponent(jLabel_DoanhThu)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel_DonHang)
                        .addGap(411, 411, 411)))
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addGap(154, 154, 154))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel_TonKho)
                        .addGap(189, 189, 189))))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel4)
                    .addComponent(jLabel10))
                .addGap(53, 53, 53)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel_DonHang)
                    .addComponent(jLabel_TonKho)
                    .addComponent(jLabel_DoanhThu))
                .addContainerGap(96, Short.MAX_VALUE))
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnTim.setText("Tìm");
        btnTim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTimActionPerformed(evt);
            }
        });

        TF_MaKH3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TF_MaKH3ActionPerformed(evt);
            }
        });

        jLabel26.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel26.setText("Ngày kết thúc :");

        TF_MaKH1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TF_MaKH1ActionPerformed(evt);
            }
        });

        jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel25.setText("Ngày bắt đầu :");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addComponent(jLabel25)
                .addGap(31, 31, 31)
                .addComponent(TF_MaKH1, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(63, 63, 63)
                .addComponent(jLabel26)
                .addGap(34, 34, 34)
                .addComponent(TF_MaKH3, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addComponent(btnTim, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(268, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnTim, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TF_MaKH3, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel26)
                    .addComponent(TF_MaKH1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel25))
                .addGap(18, 18, 18))
        );

        tabMain.addTab("Tìm kiếm", jPanel9);

        jPanel10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        buttonGroup1.add(rdoNgay);
        rdoNgay.setText("Ngày hôm nay");
        rdoNgay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdoNgayActionPerformed(evt);
            }
        });

        buttonGroup1.add(rdoThang);
        rdoThang.setText("Tháng nay");
        rdoThang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdoThangActionPerformed(evt);
            }
        });

        buttonGroup1.add(rdoNam);
        rdoNam.setText("Năm nay");
        rdoNam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdoNamActionPerformed(evt);
            }
        });

        btnBieuDo.setText("Biểu đồ doanh thu");

        btnBieuDoHoaDon.setText("Biểu đồ hoá đơn");

        btnGuiBaoCao.setText("Gửi báo cáo doanh thu");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(rdoNgay)
                .addGap(41, 41, 41)
                .addComponent(rdoThang)
                .addGap(46, 46, 46)
                .addComponent(rdoNam)
                .addGap(30, 30, 30)
                .addComponent(btnBieuDo, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(56, 56, 56)
                .addComponent(btnBieuDoHoaDon, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 299, Short.MAX_VALUE)
                .addComponent(btnGuiBaoCao, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addContainerGap(18, Short.MAX_VALUE)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                        .addComponent(btnBieuDo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(3, 3, 3))
                    .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(rdoNgay)
                        .addComponent(rdoThang)
                        .addComponent(rdoNam)
                        .addComponent(btnBieuDoHoaDon, javax.swing.GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE)
                        .addComponent(btnGuiBaoCao, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(13, 13, 13))
        );

        tabMain.addTab("Biểu đồ", jPanel10);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(44, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 1339, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tabMain, javax.swing.GroupLayout.PREFERRED_SIZE, 1339, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(40, 40, 40))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(tabMain, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 316, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65))
        );

        jTabbedPane2.addTab("Doanh thu", jPanel7);

        jPanel8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        tbl_CanhBaoSL.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tbl_CanhBaoSL);

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N

        jLabel27.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel27.setText("Ngày bắt đầu :");

        btnLocTop5.setText("Tìm");
        btnLocTop5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLocTop5ActionPerformed(evt);
            }
        });

        TF_nbday.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TF_nbdayActionPerformed(evt);
            }
        });

        jLabel29.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel29.setText("Ngày kết thúc :");

        TF_nkthuc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TF_nkthucActionPerformed(evt);
            }
        });

        tblTopSanPham.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane3.setViewportView(tblTopSanPham);

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setText("Top 5 sản phẩm bán nhiều nhất");

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel11.setText("Danh sách sản phẩm sắp hết hàng <= ");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addGap(67, 67, 67)
                .addComponent(jLabel27)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(TF_nbday, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(jLabel29)
                .addGap(18, 18, 18)
                .addComponent(TF_nkthuc, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnLocTop5, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(TF_SoLuongCanhBao, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1029, 1029, 1029))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(1396, 1396, 1396))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel7)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 1378, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(22, 22, 22))
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1378, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(TF_nbday, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel29)
                    .addComponent(TF_nkthuc, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLocTop5, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(56, 56, 56)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TF_SoLuongCanhBao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45)
                .addComponent(jLabel6)
                .addGap(281, 281, 281))
        );

        jTabbedPane2.addTab("Sản phẩm", jPanel8);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(42, Short.MAX_VALUE)
                .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 1425, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(669, 669, 669)
                    .addComponent(jLabel2)
                    .addContainerGap(672, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(63, 63, 63)
                .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 771, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(66, Short.MAX_VALUE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(16, 16, 16)
                    .addComponent(jLabel2)
                    .addContainerGap(836, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnTimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTimActionPerformed
        // TODO add your handling code here:
        try {
        // Lấy ngày bắt đầu và kết thúc từ text field
        String startDateStr = TF_MaKH1.getText().trim();
        String endDateStr = TF_MaKH3.getText().trim();
        
        // Kiểm tra dữ liệu nhập vào
        if (startDateStr.isEmpty() || endDateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ ngày bắt đầu và ngày kết thúc", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Parse ngày từ string sang LocalDate
        LocalDate startDate = LocalDate.parse(startDateStr, UI_DATE_FORMATTER);
        LocalDate endDate = LocalDate.parse(endDateStr, UI_DATE_FORMATTER);
        
        // Kiểm tra ngày bắt đầu không lớn hơn ngày kết thúc
        if (startDate.isAfter(endDate)) {
            JOptionPane.showMessageDialog(this, "Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Lấy danh sách hóa đơn trong khoảng thời gian
        List<HoaDon> hoaDonList = thongKeDAO.getDanhSachHoaDonByDateRange(startDate, endDate);
        
        // Tính tổng doanh thu và số đơn hàng
        double tongDoanhThu = hoaDonList.stream().mapToDouble(HoaDon::getTongTien).sum();
        int tongDonHang = hoaDonList.size();
        
        // Hiển thị kết quả lên giao diện
        jLabel_DoanhThu.setText(String.format("%,.0f VNĐ", tongDoanhThu));
        jLabel_DonHang.setText(String.valueOf(tongDonHang));
        
        // Cập nhật bảng hóa đơn
        fillTableHoaDon(hoaDonList);
        
    } catch (DateTimeParseException e) {
        JOptionPane.showMessageDialog(this, "Định dạng ngày không hợp lệ. Vui lòng nhập theo định dạng yyyy-MM-dd", "Lỗi", JOptionPane.ERROR_MESSAGE);
    }   catch (SQLException ex) { 
            Logger.getLogger(QLTKE.class.getName()).log(Level.SEVERE, null, ex);
        } 

    }//GEN-LAST:event_btnTimActionPerformed

    private void TF_MaKH3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TF_MaKH3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_MaKH3ActionPerformed

    private void TF_MaKH1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TF_MaKH1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_MaKH1ActionPerformed

    private void btnLocTop5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLocTop5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnLocTop5ActionPerformed

    private void jTable2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable2MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jTable2MouseClicked

    private void TF_nkthucActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TF_nkthucActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_nkthucActionPerformed

    private void TF_nbdayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TF_nbdayActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TF_nbdayActionPerformed

    private void rdoNamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdoNamActionPerformed
        // TODO add your handling code here:
        int year = LocalDate.now().getYear();
        double dt = thongKeDAO.getDoanhThuTheoNam(year);
        jLabel_DoanhThu.setText(String.format("%,.0f", dt) + " VNĐ");
    }//GEN-LAST:event_rdoNamActionPerformed

    private void rdoNgayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdoNgayActionPerformed
        // TODO add your handling code here:
        Date today = new Date();
        double dt = thongKeDAO.getDoanhThuTheoNgay(today);
        jLabel_DoanhThu.setText(String.format("%,.0f", dt) + " VNĐ");
    }//GEN-LAST:event_rdoNgayActionPerformed

    private void rdoThangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdoThangActionPerformed
        // TODO add your handling code here:
        LocalDate now = LocalDate.now();
        double dt = thongKeDAO.getDoanhThuTheoThang(now.getMonthValue(), now.getYear());
        jLabel_DoanhThu.setText(String.format("%,.0f", dt) + " VNĐ");
    }//GEN-LAST:event_rdoThangActionPerformed

//    public static void main(String args[]) {
//        /* Set the Nimbus look and feel */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(QLTKE.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(QLTKE.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(QLTKE.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(QLTKE.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        //</editor-fold>
//
//        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                // Tạo một JFrame mới
//                JFrame frame = new JFrame("Quản Lý Thống Kê"); // Tiêu đề cửa sổ
//
//                // Tạo đối tượng QLTKE panel
//                QLTKE qltkePanel = new QLTKE();
//
//                // Thêm QLTKE panel vào JFrame
//                frame.add(qltkePanel);
//
//                // Đặt kích thước cho JFrame (hoặc pack() để tự động điều chỉnh theo nội dung)
//                frame.setSize(1400, 800); // Kích thước ví dụ, bạn có thể điều chỉnh
//
//                // Đặt thao tác mặc định khi đóng cửa sổ
//                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//                // Đặt vị trí cửa sổ ở giữa màn hình
//                frame.setLocationRelativeTo(null);
//
//                // Hiển thị JFrame
//                frame.setVisible(true);
//            }
//        });
//    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField TF_MaKH1;
    private javax.swing.JTextField TF_MaKH3;
    private javax.swing.JTextField TF_SoLuongCanhBao;
    private javax.swing.JTextField TF_nbday;
    private javax.swing.JTextField TF_nkthuc;
    private javax.swing.JButton btnBieuDo;
    private javax.swing.JButton btnBieuDoHoaDon;
    private javax.swing.JButton btnGuiBaoCao;
    private javax.swing.JButton btnLocTop5;
    private javax.swing.JButton btnTim;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel_DoanhThu;
    private javax.swing.JLabel jLabel_DonHang;
    private javax.swing.JLabel jLabel_TonKho;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTable jTable2;
    private javax.swing.JRadioButton rdoNam;
    private javax.swing.JRadioButton rdoNgay;
    private javax.swing.JRadioButton rdoThang;
    private javax.swing.JTabbedPane tabMain;
    private javax.swing.JTable tblTopSanPham;
    private javax.swing.JTable tbl_CanhBaoSL;
    // End of variables declaration//GEN-END:variables
}
