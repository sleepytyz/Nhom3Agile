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
public class NhanVien {
    private String manv;
    private String tennv;
    private String sdt;
    private String vaitro;
    private String email;
    private String diaChi;
    private String gioitinh;
    private String trangThai;
    private Date ngaySinh;

    public NhanVien() {
    }

    public NhanVien(String manv, String tennv, String sdt, String vaitro, String email, String diaChi, String gioitinh, String trangThai, Date ngaySinh) {
        this.manv = manv;
        this.tennv = tennv;
        this.sdt = sdt;
        this.vaitro = vaitro;
        this.email = email;
        this.diaChi = diaChi;
        this.gioitinh = gioitinh;
        this.trangThai = trangThai;
        this.ngaySinh = ngaySinh;
    }

    public String getManv() {
        return manv;
    }

    public void setManv(String manv) {
        this.manv = manv;
    }

    public String getTennv() {
        return tennv;
    }

    public void setTennv(String tennv) {
        this.tennv = tennv;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getVaitro() {
        return vaitro;
    }

    public void setVaitro(String vaitro) {
        this.vaitro = vaitro;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getGioitinh() {
        return gioitinh;
    }

    public void setGioitinh(String gioitinh) {
        this.gioitinh = gioitinh;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Date getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(Date ngaySinh) {
        this.ngaySinh = ngaySinh;
    }


}
