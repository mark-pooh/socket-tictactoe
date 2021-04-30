import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class ClientAI {
    private JFrame frame = new JFrame("Tic Tac Toe");   //Create frame
    private JLabel messageLabel = new JLabel("");       //label to inform client
    private ImageIcon icon;             //Set client icon
    private ImageIcon opponentIcon;     //Set client opponent's icon
    
    private static int PORT = 4445;
    
    private Square[] board = new Square[9];
    private Square currentSquare;
    
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    
    public ClientAI(String servAddress) throws Exception {
        /* Networking setup */
        socket = new Socket(servAddress, PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        
        /* Layout GUI */
        messageLabel.setBackground(Color.lightGray);        //set label background colour
        frame.getContentPane().add(messageLabel, "South");  //add label to container's bottom
        
        JPanel boardPanel = new JPanel();                   //create the game board
        boardPanel.setBackground(Color.black);              //set the background to be black in colour
        boardPanel.setLayout(new GridLayout(3, 3, 2, 2));
        
        for (int i = 0; i < board.length; i++) {
            final int j = i;
            board[i] = new Square();
            board[i].addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    currentSquare = board[j];
                    out.println("MOVE " + j);
                }
            });
            boardPanel.add(board[i]);
        }

        frame.getContentPane().add(boardPanel, "Center");   //add the panel to the center
    }
    
    public void play() throws Exception {
        String response;
        try {
            response = in.readLine();
            
            if(response.startsWith("WELCOME")) {
                char mark = response.charAt(8); //receive the character after WELCOME and space
                icon = new ImageIcon(mark == 'X' ? "x.png" : "o.png");  //icon x, opponent o
                opponentIcon = new ImageIcon(mark == 'X' ? "o.png" : "x.png");  //icon o, opponent x
                frame.setTitle("Player " + mark);
            }
            
            while(true) {
                response = in.readLine();
                
                if(response.startsWith("VALID_MOVE")) {
                    messageLabel.setText("Valid move, please wait");
                    currentSquare.setIcon(icon);
                    currentSquare.repaint();
                } else if(response.startsWith("OPPONENT_MOVED")) {
                    int loc = Integer.parseInt(response.substring(15));     //Extract character after OPPONENT_MOVED and space
                    board[loc].setIcon(opponentIcon);
                    board[loc].repaint();
                    messageLabel.setText("Opponent moved, your turn");
                } else if(response.startsWith("VICTORY")) {
                    messageLabel.setText("You win");
                    break;
                } else if(response.startsWith("DEFEAT")) {
                    messageLabel.setText("You lose");
                    break;
                } else if(response.startsWith("TIE")) {
                    messageLabel.setText("You tied");
                    break;
                } else if(response.startsWith("MESSAGE")) {
                    messageLabel.setText(response.substring(8));    //Extract character after WELCOME and space
                }
            }
            out.println("QUIT");
        } finally {
            socket.close();
        }
    }
    
    private boolean wantsToPlayAgain() {
        int response = JOptionPane.showConfirmDialog(frame, 
                                                     "Want to play again?", "Tic Tac Toe",
                                                     JOptionPane.YES_NO_OPTION);
        frame.dispose();    //remove the game frame
        return response == JOptionPane.YES_OPTION;
    }
    
    static class Square extends JPanel {
        JLabel label = new JLabel((Icon)null);
        
        public Square() {
            setBackground(Color.white);
            add(label);
        }
        
        public void setIcon(Icon icon) {
            label.setIcon(icon);
        }
    }
    
    public static void main(String[] args) throws Exception {
        while(true) {
            String serverAddress = (args.length == 0) ? "192.168.1.10" : args[1];
            ClientAI client = new ClientAI(serverAddress);
            client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);            
            client.frame.setSize(240, 240);
            client.frame.setVisible(true);
            client.frame.setResizable(false);
            client.frame.setLocationRelativeTo(null);
            client.play();
            
            if(!client.wantsToPlayAgain()) {
                break;
            }
        }
    }
}
