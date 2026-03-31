/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author XPS
 */
public class HoaDonChiTiet {
    private String macthd;
    private String mahd;
    private String masp;
    private String tensp;
    private int sluong;
    private float dongia;
    private float giamGia;
    private float thanhTien;
    private String trangthai;
    private String gchu;

    public HoaDonChiTiet() {
    }

    public HoaDonChiTiet(String macthd, String mahd, String masp, String tensp, int sluong, float dongia, float giamGia, float thanhTien) {
        this.macthd = macthd;
        this.mahd = mahd;
        this.masp = masp;
        this.tensp = tensp;
        this.sluong = sluong;
        this.dongia = dongia;
        this.giamGia = giamGia;
        this.thanhTien = thanhTien;
    }

    public HoaDonChiTiet(String macthd, String mahd, String masp, String tensp, int sluong, float dongia, float giamGia, float thanhTien, String trangthai, String gchu) {
        this.macthd = macthd;
        this.mahd = mahd;
        this.masp = masp;
        this.tensp = tensp;
        this.sluong = sluong;
        this.dongia = dongia;
        this.giamGia = giamGia;
        this.thanhTien = thanhTien;
        this.trangthai = trangthai;
        this.gchu = gchu;
    }

    public String getMacthd() {
        return macthd;
    }

    public void setMacthd(String macthd) {
        this.macthd = macthd;
    }

    public String getMahd() {
        return mahd;
    }

    public void setMahd(String mahd) {
        this.mahd = mahd;
    }

    public String getMasp() {
        return masp;
    }

    public void setMasp(String masp) {
        this.masp = masp;
    }

    public String getTensp() {
        return tensp;
    }

    public void setTensp(String tensp) {
        this.tensp = tensp;
    }

    public int getSluong() {
        return sluong;
    }

    public void setSluong(int sluong) {
        this.sluong = sluong;
    }

    public float getDongia() {
        return dongia;
    }

    public void setDongia(float dongia) {
        this.dongia = dongia;
    }

    public float getGiamGia() {
        return giamGia;
    }

    public void setGiamGia(float giamGia) {
        this.giamGia = giamGia;
    }

    public float getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(float thanhTien) {
        this.thanhTien = thanhTien;
    }
    public String getTrangthai() {
        return trangthai;
    }

    public void setTrangthai(String trangthai) {
        this.trangthai = trangthai;
    }

    public String getGchu() {
        return gchu;
    }

    public void setGchu(String gchu) {
        this.gchu = gchu;
    }
    

    
    
}
