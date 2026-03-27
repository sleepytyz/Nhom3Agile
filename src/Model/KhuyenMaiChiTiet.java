/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author XPS
 */
public class KhuyenMaiChiTiet {
    private int maCTKM; // ID tự tăng
    private String maKM;
    private String maSP;

    public KhuyenMaiChiTiet() {
    }

    public KhuyenMaiChiTiet(int maCTKM, String maKM, String maSP) {
        this.maCTKM = maCTKM;
        this.maKM = maKM;
        this.maSP = maSP;
    }

    public int getMaCTKM() {
        return maCTKM;
    }

    public void setMaCTKM(int maCTKM) {
        this.maCTKM = maCTKM;
    }

    public String getMaKM() {
        return maKM;
    }

    public void setMaKM(String maKM) {
        this.maKM = maKM;
    }

    public String getMaSP() {
        return maSP;
    }

    public void setMaSP(String maSP) {
        this.maSP = maSP;
    }
    
    
}
