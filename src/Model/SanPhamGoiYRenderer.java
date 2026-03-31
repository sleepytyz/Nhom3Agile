/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author XPS
 */
import java.awt.*;
import javax.swing.*;

public class SanPhamGoiYRenderer extends JPanel implements ListCellRenderer<SanPhamGoiY> {
    private JLabel lblImage = new JLabel();
    private JLabel lblTen = new JLabel();

    public SanPhamGoiYRenderer() {
        setLayout(new BorderLayout(5, 5));
        add(lblImage, BorderLayout.WEST);
        add(lblTen, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(
        JList<? extends SanPhamGoiY> list, SanPhamGoiY value, int index,
        boolean isSelected, boolean cellHasFocus
    ) {
        ImageIcon icon = new ImageIcon(value.getHinh());
        Image scaledImg = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        lblImage.setIcon(new ImageIcon(scaledImg));
        lblTen.setText(value.getTen());

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        setOpaque(true);
        return this;
    }
}
