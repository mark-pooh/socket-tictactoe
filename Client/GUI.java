import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GUI {
    public static void main(String[] args) {
        new GUI();  //instantiate new GUI object
    }
    
    public GUI() {
        //setup GUI
        JFrame frame = new JFrame();
        final JPanel msgPanel = new JPanel();
        final JPanel btnPanel = new JPanel();
        final JPanel picPanel = new JPanel();
        JLabel msgLbl = new JLabel("Select Game Mode");
        JLabel picLbl = new JLabel();
        JButton aiBtn = new JButton("Client vs Server");
        JButton clBtn = new JButton("Client vs Client");
        ImageIcon icon = new ImageIcon("vs.png");
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Tic Tac Toe");
        frame.setSize(300, 250);
        
        //set location to center of screen
        frame.setLocationRelativeTo(null);
        
        //add Action Listener to Client button
        clBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                frame.dispose();
                
                try {
                    Runtime.getRuntime().exec("cmd /c java Client");
                } catch (Exception e) {
                    System.err.println("Error creating client instances.");
                }
            }
        });
        
        //add Action Listener to Client button        
        aiBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                frame.dispose();
                
                try {
                    //ClientAI cAI = new ClientAI("localhost");
                    Runtime.getRuntime().exec("cmd /c java ClientAI");
                } catch (Exception e) {
                    System.err.println("Error creating AI instances");
                }
            }
        });
        //add elements to frame
        msgPanel.add(msgLbl);
        
        picLbl.setIcon(icon);
        picPanel.add(picLbl);
        
        btnPanel.add(aiBtn);
        btnPanel.add(clBtn);
              
        frame.add(msgPanel, BorderLayout.NORTH);
        frame.add(picPanel, BorderLayout.CENTER);
        frame.add(btnPanel, BorderLayout.SOUTH);        
        
        frame.setVisible(true);
    }
}
