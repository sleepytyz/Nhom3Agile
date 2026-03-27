/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author XPS
 */
public class SanPham {

    private String masp;
    private String tensp;
    private String loaisp;
    private float gia;
    private int sluong;
    private String mausac;
    private String kichThuoc;
    private String chatLieu;
    private String trangThai;
    private byte[] hinhAnh;

    public SanPham() {
    }

    public SanPham(String masp, String tensp, float gia, int sluong, byte[] hinhAnh) {
        this.masp = masp;
        this.tensp = tensp;
        this.gia = gia;
        this.sluong = sluong;
        this.hinhAnh = hinhAnh;
    }
    
    
    

    public SanPham(String masp, String tensp, String loaisp, float gia, int sluong, String mausac, String kichThuoc, String chatLieu, String trangThai) {
        this.masp = masp;
        this.tensp = tensp;
        this.loaisp = loaisp;
        this.gia = gia;
        this.sluong = sluong;
        this.mausac = mausac;
        this.kichThuoc = kichThuoc;
        this.chatLieu = chatLieu;
        this.trangThai = trangThai;
    }

    public SanPham(String masp, String tensp, String loaisp, float gia, int sluong, String mausac, String kichThuoc, String chatLieu, String trangThai, byte[] hinhAnh) {
        this.masp = masp;
        this.tensp = tensp;
        this.loaisp = loaisp;
        this.gia = gia;
        this.sluong = sluong;
        this.mausac = mausac;
        this.kichThuoc = kichThuoc;
        this.chatLieu = chatLieu;
        this.trangThai = trangThai;
        this.hinhAnh = hinhAnh;
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

    public String getLoaisp() {
        return loaisp;
    }

    public void setLoaisp(String loaisp) {
        this.loaisp = loaisp;
    }

    public float getGia() {
        return gia;
    }

    public void setGia(float gia) {
        this.gia = gia;
    }

    public int getSluong() {
        return sluong;
    }

    public void setSluong(int sluong) {
        this.sluong = sluong;
    }

    public String getMausac() {
        return mausac;
    }

    public void setMausac(String mausac) {
        this.mausac = mausac;
    }

    public String getKichThuoc() {
        return kichThuoc;
    }

    public void setKichThuoc(String kichThuoc) {
        this.kichThuoc = kichThuoc;
    }

    public String getChatLieu() {
        return chatLieu;
    }

    public void setChatLieu(String chatLieu) {
        this.chatLieu = chatLieu;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public byte[] getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(byte[] hinhAnh) {
        this.hinhAnh = hinhAnh;
    }
    
    

}
