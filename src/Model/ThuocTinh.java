/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author win10
 */
public class ThuocTinh {

    private String maTT;
    private String tenTT;
    private String loaiTT;

    public String getMaTT() {
        return maTT;
    }

    public void setMaTT(String maTT) {
        this.maTT = maTT;
    }

    public String getTenTT() {
        return tenTT;
    }

    public void setTenTT(String tenTT) {
        this.tenTT = tenTT;
    }

    public String getLoaiTT() {
        return loaiTT;
    }

    public void setLoaiTT(String loaiTT) {
        this.loaiTT = loaiTT;
    }

    public ThuocTinh(String maTT, String tenTT, String loaiTT) {
        this.maTT = maTT;
        this.tenTT = tenTT;
        this.loaiTT = loaiTT;
    }

    public ThuocTinh(String maTT, String tenTT) {
        this.maTT = maTT;
        this.tenTT = tenTT;
    }

    public ThuocTinh() {
    }

    @Override
    public String toString() {
        return tenTT;
    }

}
