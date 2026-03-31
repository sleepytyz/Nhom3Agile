/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

import java.sql.Date;

/**
 *
 * @author XPS
 */
public class HoaDon {
    private String mahd;
    private String Manv;
    private String tenkh;
    private String sdt;
    private String trangThai;
    private Date ngayTao;
    private double tongTien;
    private double tienTra;
    private double tienThua;
    private String thanhToan;
    private String giaoHang;
    private String ghiChu;

    public HoaDon() {
    }

    public HoaDon(String mahd, String Manv, String trangThai, Date ngayTao, String ghiChu) {
        this.mahd = mahd;
        this.Manv = Manv;
        this.trangThai = trangThai;
        this.ngayTao = ngayTao;
        this.ghiChu = ghiChu;
    }

    public HoaDon(String mahd, String tenkh, String sdt, double tongTien, double tienTra, double tienThua, String thanhToan, String giaoHang, String ghiChu) {
        this.mahd = mahd;
        this.tenkh = tenkh;
        this.sdt = sdt;
        this.tongTien = tongTien;
        this.tienTra = tienTra;
        this.tienThua = tienThua;
        this.thanhToan = thanhToan;
        this.giaoHang = giaoHang;
        this.ghiChu = ghiChu;
    }
    
    

    public HoaDon(String mahd, String Manv, String tenkh, String sdt, String trangThai, Date ngayTao, double tongTien, double tienTra, double tienThua, String thanhToan, String giaoHang, String ghiChu) {
        this.mahd = mahd;
        this.Manv = Manv;
        this.tenkh = tenkh;
        this.sdt = sdt;
        this.trangThai = trangThai;
        this.ngayTao = ngayTao;
        this.tongTien = tongTien;
        this.tienTra = tienTra;
        this.tienThua = tienThua;
        this.thanhToan = thanhToan;
        this.giaoHang = giaoHang;
        this.ghiChu = ghiChu;
    }

    public String getMahd() {
        return mahd;
    }

    public void setMahd(String mahd) {
        this.mahd = mahd;
    }

    public String getManv() {
        return Manv;
    }

    public void setManv(String Manv) {
        this.Manv = Manv;
    }

    public String getTenkh() {
        return tenkh;
    }

    public void setTenkh(String tenkh) {
        this.tenkh = tenkh;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Date getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Date ngayTao) {
        this.ngayTao = ngayTao;
    }

    public double getTongTien() {
        return tongTien;
    }

    public void setTongTien(double tongTien) {
        this.tongTien = tongTien;
    }

    public double getTienTra() {
        return tienTra;
    }

    public void setTienTra(double tienTra) {
        this.tienTra = tienTra;
    }

    public double getTienThua() {
        return tienThua;
    }

    public void setTienThua(double tienThua) {
        this.tienThua = tienThua;
    }

    public String getThanhToan() {
        return thanhToan;
    }

    public void setThanhToan(String thanhToan) {
        this.thanhToan = thanhToan;
    }

    public String getGiaoHang() {
        return giaoHang;
    }

    public void setGiaoHang(String giaoHang) {
        this.giaoHang = giaoHang;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
    
    
    
}
