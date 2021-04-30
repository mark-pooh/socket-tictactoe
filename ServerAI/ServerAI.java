import java.io.*;   /* for input and output */
import java.net.*;  /* for networking */
import java.util.*; /* for AI to randomly chose location */

public class ServerAI {
    public static void main(String[] args) {
        Socket socket = null;             /* Initialise socket to null */
        ServerSocket ServerSocket = null; /* Initialise socket to null */
        
        System.out.println("Server Listening...");
        
        try {
            ServerSocket = new ServerSocket(4445);    /* Try to bind port to socket */
        } catch(IOException e) {
            e.printStackTrace();                    /* Display error message */
            System.out.println("Server error");     /* when port can't be created */
        }
        
        while(true) {
            try {
                Game game = new Game();
                Game.PlayerAI playerAI;
                Random first = new Random();
                if(first.nextInt(2) == 0) {
                    playerAI = game.new PlayerAI(ServerSocket.accept(), 'X');
                } else {
                    playerAI = game.new PlayerAI(ServerSocket.accept(), 'O');
                }
                playerAI.start();
            } catch(Exception e) {
                e.printStackTrace();
                System.out.println("Connection Error");
            }
        }
    }
}

class Game {
    private String[] board = {null, null, null, null, null, null, null, null, null};
    int[] arr = new int[board.length];
    
    public boolean hasWinner() {    //check whether the winner has been decided
        return
            (board[0] != null && board[0] == board[1] && board[0] == board[2])
          ||(board[3] != null && board[3] == board[4] && board[3] == board[5])
          ||(board[6] != null && board[6] == board[7] && board[6] == board[8])
          ||(board[0] != null && board[0] == board[3] && board[0] == board[6])
          ||(board[1] != null && board[1] == board[4] && board[1] == board[7])
          ||(board[2] != null && board[2] == board[5] && board[2] == board[8])
          ||(board[0] != null && board[0] == board[4] && board[0] == board[8])
          ||(board[2] != null && board[2] == board[4] && board[2] == board[6]);
    }
    
    public boolean boardFilledUp() {    //check whether the board has been filled up
        for(int i = 0; i < board.length; i++) {
            if(board[i] == null) {
                return false;
            }
        }
        return true;
    }
    
    public synchronized boolean legalMove(int loc) {    //check for legal moves
        if(board[loc] == null) {
            board[loc] = "X";
            return true;
        }
        return false;
    }
    
    public String serverMove() {    //decide server's move
        int indexArr = 0;
        
        for(int i = 0; i < board.length; i++) {
            if(board[i] == null) {
                arr[indexArr] = i;
                indexArr++;
            }
        }
        
        int randArr = getRandom(arr, indexArr);
        board[randArr] = "O";
        return "OPPONENT_MOVED " + randArr;       
    }
    
    public int getRandom(int[] array, int max) {
        int rand;
        
        rand = new Random().nextInt(array.length);        
        while(rand > max) {
            rand = new Random().nextInt(array.length);
            break;
        }
        
        return array[rand];
    }
    
    class PlayerAI extends Thread {
        char mark;
        Socket socket = null;
        PlayerAI opponent = null;
        PrintWriter outToClient = null;
        BufferedReader inFromClient = null;              
        
        public PlayerAI(Socket socket, char mark) {
            this.socket = socket;   /* same as socket in server to standardize socket among threads */
            this.mark = mark;
            
            try {   /* try to create buffer and writer for communication */
                inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                outToClient = new PrintWriter(socket.getOutputStream(), true);
                outToClient.println("WELCOME " + mark);
            } catch(IOException e) {
                System.out.println("Player died: " + e);
            }
        }
        
        public void run() {                    
            try {   /* try to read input from a client */
                if(mark == 'X') {
                    outToClient.println("MESSAGE Your move");
                }
                
                while(true) { //while the user is connected to the server
                    String command = inFromClient.readLine();   //read input from client
                    
                    /* If client has moved which can be decided my message "MOVE",
                     * check whether the moves is legal, if legal, check for winners
                     * If the moves wasn't legal, print message '?' to the user
                     * If the client QUIT, then quit the connection.
                     * The rest of the code won't be executed.
                     */
                    
                    if(command.startsWith("MOVE")) {
                        int location = Integer.parseInt(command.substring(5));
                        String smove = "";
                        
                        if(legalMove(location)) {
                            outToClient.println("VALID_MOVE");
                            outToClient.println(hasWinner() ? "VICTORY"
                                                : boardFilledUp() ? "TIE"
                                                : "");
                            outToClient.println(serverMove());
                            outToClient.println(hasWinner() ? "DEFEAT" : boardFilledUp() ? "TIE" : "");
                        } else {
                            outToClient.println("MESSAGE ?");
                        }
                    } else if(command.startsWith("QUIT")) {
                        return;
                    }
                }
            } catch(IOException e) {
                System.out.println("Player died: " + e);
            } finally { //close the connections
                try {
                    System.out.println("Closing Connection...");
                    
                    if(inFromClient != null) {
                        inFromClient.close();
                        System.out.println("Socket Input Stream Closed");
                    }
                    
                    if(outToClient != null) {
                        outToClient.close();
                        System.out.println("Socket Out Closed");
                    }
                    
                    if(socket != null) {
                        socket.close();
                        System.out.println("Socket Closed");
                    }
                } catch(IOException ie) {
                    System.out.println("Socket Close Error");
                }
            }
        }
    }
}