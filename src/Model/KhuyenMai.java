/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

import java.util.Date;

/**
 *
 * @author XPS
 */
public class KhuyenMai {

    private String maKM;
    private String tenKm;
    private Date ngayBdau;
    private Date ngayKthuc;
    private String loaisp;
    private float giamgia;
    private String trangThai;

    public KhuyenMai() {
    }

    public KhuyenMai(String maKM, String tenKm, Date ngayBdau, Date ngayKthuc, String loaisp, float giamgia, String trangThai) {
        this.maKM = maKM;
        this.tenKm = tenKm;
        this.ngayBdau = ngayBdau;
        this.ngayKthuc = ngayKthuc;
        this.loaisp = loaisp;
        this.giamgia = giamgia;
        this.trangThai = trangThai;
    }

    public String getLoaisp() {
        return loaisp;
    }

    public void setLoaisp(String loaisp) {
        this.loaisp = loaisp;
    }

    

    public String getMaKM() {
        return maKM;
    }

    public void setMaKM(String maKM) {
        this.maKM = maKM;
    }

    public String getTenKm() {
        return tenKm;
    }

    public void setTenKm(String tenKm) {
        this.tenKm = tenKm;
    }

    public Date getNgayBdau() {
        return ngayBdau;
    }

    public void setNgayBdau(Date ngayBdau) {
        this.ngayBdau = ngayBdau;
    }

    public Date getNgayKthuc() {
        return ngayKthuc;
    }

    public void setNgayKthuc(Date ngayKthuc) {
        this.ngayKthuc = ngayKthuc;
    }

    public float getGiamgia() {
        return giamgia;
    }

    public void setGiamgia(float giamgia) {
        this.giamgia = giamgia;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
    
    

}
