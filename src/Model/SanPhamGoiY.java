/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author XPS
 */
public class SanPhamGoiY {
    private String ten;
    private byte[] hinh;

    public SanPhamGoiY(String ten, byte[] hinh) {
        this.ten = ten;
        this.hinh = hinh;
    }

    public String getTen() {
        return ten;
    }

    public byte[] getHinh() {
        return hinh;
    }

    @Override
    public String toString() {
        return ten; // quan trọng để khi render mặc định vẫn thấy tên
    }
}

