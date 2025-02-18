package com.lab;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class TypeRush implements Runnable {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new TypeRush());
    }

    private JScrollPane showScrollPane;

    private JTextArea showTextArea;



    @Override
    public void run() {
        JFrame frame = new JFrame("pimyada & phimlaphat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.add(createDisplayPanel(), BorderLayout.BEFORE_FIRST_LINE);
        frame.add(createShowTextPanel(), BorderLayout.BEFORE_LINE_BEGINS);
       
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    private JPanel createDisplayPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        Font font = panel.getFont().deriveFont(16f);

        JLabel label = new JLabel("Welcome to TypingBuddy");
        label.setFont(font);
        panel.add(label);

        return panel;
    }

    private JPanel createShowTextPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        Font font = panel.getFont().deriveFont(16f);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel label = new JLabel("A World of Tales");
        label.setFont(font);
        panel.add(label, gbc);

        gbc.gridy++;
        showTextArea = new JTextArea(10, 40);
        showTextArea.setEditable(false);
        showTextArea.setFont(font);
        showTextArea.setLineWrap(true);
        showTextArea.setWrapStyleWord(true);

        showScrollPane = new JScrollPane(showTextArea);
        panel.add(showScrollPane, gbc);

        return panel;
    }


    

}