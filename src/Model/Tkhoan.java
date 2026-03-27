/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author XPS
 */
public class Tkhoan {
    private String tentk;
    private String maNV;
    private String mkhau;
    private String vtro;
    private byte[] anh;

    public Tkhoan() {
    }

    public Tkhoan(String tentk, String mkhau, String vtro) {
        this.tentk = tentk;
        this.mkhau = mkhau;
        this.vtro = vtro;
    }
    
    

    public Tkhoan(String tentk, String maNV, String mkhau, String vtro, byte[] anh) {
        this.tentk = tentk;
        this.maNV = maNV;
        this.mkhau = mkhau;
        this.vtro = vtro;
        this.anh = anh;
    }

    public String getTentk() {
        return tentk;
    }

    public void setTentk(String tentk) {
        this.tentk = tentk;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getMkhau() {
        return mkhau;
    }

    public void setMkhau(String mkhau) {
        this.mkhau = mkhau;
    }

    public String getVtro() {
        return vtro;
    }

    public void setVtro(String vtro) {
        this.vtro = vtro;
    }

    public byte[] getAnh() {
        return anh;
    }

    public void setAnh(byte[] anh) {
        this.anh = anh;
    }

    
}
