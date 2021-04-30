# Socket TicTacToe
A project created for socket programming course.

1. Click server.bat
2. Click serverAI.bat
3. Click gui.bat
4. Enjoy!
---------------------
This program used server of ip address 192.168.1.10. If you wish to change the host, please do so at line 

```String serverAddress = (args.length == 0) ? "192.168.1.10" : args[1];```

This line appears twice, in `Client.java` and `ClientAI.java`

*I think I can improvise the code by sending ip as argument.. but it's ok for now*

You need to have Java SDK installed and added to the environment variables (Please google on how to add java to environment variables)

For more fun, play on 3 different PCs
- 2 Client (gui.bat & Client folder)
  - ip in file must refer to the server's ip
- 1 Server (server.bat, serverAI.bat, Server folder)
  - ip as stated above

***The claimed "AI" is not really "AI", just basic conditions and randoms (To add mark to my project 😆)***
