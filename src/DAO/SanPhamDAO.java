/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Model.SanPham;
import Service.DBConnect;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author XPS
 */
public class SanPhamDAO {

    public Object[] getRow(SanPham sp) {
        String masp = sp.getMasp();
        String tensp = sp.getTensp();
        String loaisp = sp.getLoaisp();
        float gia = sp.getGia();
        int sluong = sp.getSluong();
        String mausac = sp.getMausac();
        String kichThuoc = sp.getKichThuoc();
        String chatLieu = sp.getChatLieu();
        String trangThai = sp.getTrangThai();

        Object[] row = new Object[]{masp, tensp, loaisp, gia, sluong, mausac, kichThuoc, chatLieu, trangThai};
        return row;
    }

    public List<SanPham> getAll() {
        List<SanPham> listSP = new ArrayList<>();
        String sql = "SELECT * FROM SanPham ORDER BY CAST(RIGHT(MaSP, LEN(MaSP)-2) AS INT) ASC;";
        try {
            Connection con = DBConnect.getConnection();
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            while (rs.next()) {
                String masp = rs.getString(1);
                String tensp = rs.getString(2);
                String loaisp = rs.getString(3);
                float gia = rs.getFloat(4);
                int sluong = rs.getInt(5);
                String mausac = rs.getString(6);
                String kichThuoc = rs.getString(7);
                String chatLieu = rs.getString(8);
                String trangThai = rs.getString(9);
                SanPham hd = new SanPham(masp, tensp, loaisp, gia, sluong, mausac, kichThuoc, chatLieu, trangThai);
                listSP.add(hd);
            }
        } catch (Exception ex) {
        }

        return listSP;
    }
}
