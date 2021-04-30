import java.io.*;   /* for input and output */
import java.net.*;  /* for networking */

public class Server {
    public static void main(String[] args) {
        Socket socket = null;           /* Initialise socket to null */
        ServerSocket servSocket = null; /* Initialise socket to null */
        
        System.out.println("Server is listening...");
        
        try {
            servSocket = new ServerSocket(1234);    /* Try to bind port to socket */
        } catch(IOException e) {
            e.printStackTrace();                    /* Display error message */
            System.out.println("Server error");     /* when port can't be created */
        }
        
        while(true){
            try {
                Game game = new Game();
                Game.Player playerX = game.new Player(servSocket.accept(), 'X');
                Game.Player player0 = game.new Player(servSocket.accept(), 'O');
                playerX.setOpponent(player0);
                player0.setOpponent(playerX);
                game.currentPlayer = playerX;
                playerX.start();
                player0.start();
            } catch(Exception e) {
                e.printStackTrace();
                System.out.println("Connection Error");
            }
        }
    }        
}

class Game {
    private Player[] board = {null, null, null, null, null, null,null, null, null};
    Player currentPlayer;
    
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
    
    public synchronized boolean legalMove(int loc, Player player) { //check for legal moves
        if(player == currentPlayer && board[loc] == null) {
            board[loc] = currentPlayer;
            currentPlayer = currentPlayer.opponent;
            currentPlayer.otherPlayerMoved(loc);
            return true;
        }
        return false;
    }
    
    class Player extends Thread {
        char mark;
        Socket socket = null;
        Player opponent = null;
        PrintWriter outToClient = null;
        BufferedReader inFromClient = null;              
        
        public Player(Socket socket, char mark) {
            this.socket = socket;   /* same as socket in server to standardize socket among threads */
            this.mark = mark;
            
            try {   /* try to create buffer and writer for communication */
                inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                outToClient = new PrintWriter(socket.getOutputStream(), true);
                outToClient.println("WELCOME " + mark);
                outToClient.println("MESSAGE Waiting for opponent to connect");
            } catch(IOException e) {
                System.out.println("Player died: " + e);
            }
        }
        
        public void setOpponent(Player opponent) {
            this.opponent = opponent;
        }
        
        public void otherPlayerMoved(int loc) { //jot down the opponent moves information
            outToClient.println("OPPONENT_MOVED " + loc);   //opponent location
            outToClient.println(hasWinner() ? "DEFEAT" : boardFilledUp() ? "TIE" : "");
            /*
             * Does the opponent's moves brings up a winner?
             * If it does, then the opponent is the winner
             * and the player lose.
             * At the same time, ensure that the board has been filled up
             * If it does, then the game is tied.
             */
        }
        
        public void run() {                    
            try {   /* try to read input from a client */
                outToClient.println("MESSAGE All Players connected");
                
                /* this is the first move. 
                 * The first player to connect to the server will be
                 * the player with 'X' symbol
                 */
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
                        
                        if(legalMove(location, this)) {
                            outToClient.println("VALID_MOVE");
                            outToClient.println(hasWinner() ? "VICTORY"
                                                : boardFilledUp() ? "TIE"
                                                : "");
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