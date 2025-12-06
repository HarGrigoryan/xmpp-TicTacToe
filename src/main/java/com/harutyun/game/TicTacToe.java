package com.harutyun.game;

import com.harutyun.game.core.*;
import com.harutyun.game.enums.CellState;
import com.harutyun.game.enums.GameState;
import com.harutyun.game.enums.Winner;
import com.harutyun.game.exception.IllegalMoveException;
import com.harutyun.cli.BoardRender;
import com.harutyun.xmppclient.GameExtension;
import com.harutyun.xmppclient.GameExtensionProvider;
import lombok.Getter;
import lombok.Setter;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.MessageBuilder;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;


import java.util.List;
import java.util.Scanner;

public class TicTacToe {

    private volatile TicTacToeBoard board;
    @Setter
    private volatile Player player;
    @Setter
    @Getter
    private volatile GameState gameState;
    private volatile Participant opponent;
    private volatile XMPPTCPConnection connection;
    private volatile ChatManager chatManager;
    @Getter
    @Setter
    private volatile String currentGameId;
    private volatile GameOffer gameOffer;
    private final Object chatLock = new Object();
    private volatile boolean moveAckReceived = false;
    private final Scanner scanner;
    private final static int port = 5222;
    private final static int millis = 2000;
    private final static String helpText = """
            Available commands:
              start                - challenge a player (you'll be prompted for character (X/O) and opponent JID)
              accept               - accept an incoming game offer
              play <row> <col>     - make a move (row and col are 0..2). Example: play 1 1
              exit curr            - exit the current game (notify opponent)
              exit                 - quit the program
              help, ?, h           - show this help

            Quick tips:
              • Use zero-based coordinates (0 0 is top-left).
              • After 'start' you'll be asked to pick X or O and opponent@xmpp.jp.
              • The program waits for an ack after sending a move.
            """;

    public TicTacToe(TicTacToeBoard board) {
        this.board = board;
        this.scanner = new Scanner(System.in);
        this.gameState = GameState.IDLE;
    }

    private void setOpponent(String opponentUserName) {
        this.opponent = new Participant(opponentUserName);
        try {
            sendRequestToStart();
        }catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
        setGameState(GameState.PENDING_START);
    }

    private void sendRequestToStart() throws XmppStringprepException, SmackException.NotConnectedException, InterruptedException {
        if (opponent == null) throw new IllegalStateException("No opponent set");

        setCurrentGameId(java.util.UUID.randomUUID().toString());

        CellState initiatorCellState = player.getCellState();
        GameExtension startExt;
        try {
            startExt = new GameExtension("start", getCurrentGameId(), 0, 0, initiatorCellState, "offer");
            Message m = MessageBuilder.buildMessage()
                    .ofType(Message.Type.chat)
                    .setThread(getCurrentGameId())
                    .setBody("game-offer")
                    .addExtension(startExt)
                    .build();
            synchronized (chatLock) {
                Chat localChat = createChat();
                localChat.send(m);
            }
            setGameState(GameState.PENDING_START);
        } catch (IllegalMoveException e) {
            System.out.println("[ERROR] when creating game extension.\n" + e.getMessage());
        }
    }


    private void connectAndLogin() throws Exception {

        ProviderManager.addExtensionProvider(
                GameExtension.ELEMENT,
                GameExtension.NAMESPACE,
                new GameExtensionProvider()
        );

        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setHost("xmpp.jp")
                .setXmppDomain("xmpp.jp")
                .setSecurityMode(ConnectionConfiguration.SecurityMode.ifpossible)
                .setPort(port)
                .setHostnameVerifier((hostname, session) -> true)
                .build();

        connection = new XMPPTCPConnection(config);
        connection.connect();
        try {
            connection.login(player.getName(), player.getPassword());
        }catch (Exception e) {
            System.out.println("Incorrect username and/or password. Please start the program to try again.");
            System.exit(1);
        }
        chatManager = ChatManager.getInstanceFor(connection);
        chatManager.addIncomingListener((from, message, chat) ->
            handleIncomingMessage(from.asEntityBareJidString(), message)
        );
    }

    private void handleIncomingMessage(String fromJid, Message message) {
        try {
            List<GameExtension> extensions = message.getExtensions(GameExtension.class);
            GameExtension extension = extensions.isEmpty() ? null : extensions.get(0);
            if (extension == null || opponent != null && !fromJid.equalsIgnoreCase(opponent.getUserName())
            || currentGameId != null && !extension.getGameId().equals(currentGameId)) {
                System.out.println("Chat from " + fromJid + ": " + message.getBody());
                return;
            }

            switch (extension.getAction()) {
                case "move":
                    try {
                        onOpponentMove(extension);
                    } catch (Exception e) {
                        System.out.println("Opponent move failed: " + e.getMessage());
                    }
                    break;
                case "ack":
                    if(message.getBody().equals("move")){
                        if(extension.getStatus().equalsIgnoreCase("ok"))
                        {
                            if(extension.getMove() != null) {
                                try {
                                    board.performStep(extension.getMove());
                                    System.out.println("\nYour move was successfully performed (ack received).");
                                    moveAckReceived = true;
                                    System.out.println(BoardRender.renderBoard(board, player));
                                    Winner winner = board.getWinner();
                                    if(winner != Winner.NOONE)
                                    {
                                        System.out.println(winner + " has won the game!");
                                        setGameState(GameState.IDLE);
                                        System.out.println("Game finished.");
                                    }
                                    System.out.flush();

                                }catch (IllegalMoveException e){
                                    System.out.println("Last move failed because: " + e.getMessage());
                                }
                            }
                        }
                    }else if(extension.getStatus().equalsIgnoreCase("invalid")){
                        System.out.println("Last move failed: " + message.getBody());
                        moveAckReceived=true;
                    }

                    break;
                case "start":
                    if (getGameState() == GameState.PENDING_START) {
                        if (extension.getStatus().trim().equalsIgnoreCase("ok"))
                            setGameState(GameState.IN_PROGRESS);
                    } else if (getGameState() == GameState.IDLE) {
                        if (extension.getStatus().trim().equalsIgnoreCase("offer")) {
                            Participant potentialOpponent = new Participant(fromJid);
                            potentialOpponent.setCellState(extension.getMove().getCellState());
                            gameOffer = new GameOffer(extension.getGameId(), potentialOpponent);
                            System.out.printf("%nIncoming game offer from %s to play as '%s'. Type 'accept' to accept or 'decline' to reject.%n",
                                    fromJid, potentialOpponent.getCellState());
                            System.out.flush();
                        }
                    }
                    break;
                case "exit":
                    if (extension.getStatus().equalsIgnoreCase("terminated")) {
                        if (getGameState() == GameState.IN_PROGRESS) {
                            System.out.printf("Your opponent '%s' has exited the game.\n", fromJid);
                            System.out.flush();
                            setGameState(GameState.IDLE);
                        }
                    }
            }
        }catch (Throwable t){
            System.err.println("[handleIncomingMessage] UNCAUGHT exception: " + t.getMessage());
        }
    }


    private void onOpponentMove(GameExtension ext) throws Exception {
        Move move = ext.getMove();
        CellState opponentsCharacter = move.getCellState();

        if(opponentsCharacter == player.getCellState())
            sendInvalidMessageAck(ext.getGameId(), "Wrong character.");

        try {
            board.performStep(move);
        }catch (IllegalMoveException e)
        {
            sendInvalidMessageAck(ext.getGameId(), e.getMessage());
            return;
        }

        try {
            sendMoveAck(ext);
        }catch (Exception e)
        {
            System.out.println("Sending move ack message failed.");
        }

        System.out.println("\nMove received from " + opponent.getUserName() + ": " + move);
        System.out.println(BoardRender.renderBoard(board, player));
        if(board.getWinner() != Winner.NOONE) {
            System.out.println("Winner: " + board.getWinner());
            gameState = GameState.IDLE;
            System.out.println("Game Finished!");
            System.out.flush();
        }

    }


    private void sendInvalidMessageAck(String gameId, String body) throws Exception {
        GameExtension ackExt = new GameExtension("ack", gameId, "invalid");
        Message m = MessageBuilder.buildMessage().
                ofType(Message.Type.chat)
                .setThread(gameId)
                .setBody(body)
                .addExtension(ackExt)
                .build();
        synchronized (chatLock) {
            Chat localChat = createChat();
            localChat.send(m);
        }
    }

    private void sendMessage(String gameId, String status, String action) throws Exception {
        GameExtension ackExt = new GameExtension(action, gameId, status);
        Message m = MessageBuilder.buildMessage().
                ofType(Message.Type.chat)
                .setThread(gameId)
                .setBody(action)
                .addExtension(ackExt).
                build();
        synchronized (chatLock) {
            Chat localChat = createChat();
            localChat.send(m);
        }
    }

    private void sendMoveAck(GameExtension ext) throws Exception {
        if(ext.getMove() == null)
            throw new IllegalArgumentException("Move cannot be null for move acknowledgement messages.");
        ext.setStatus("ok");
        ext.setAction("ack");
        Message m = MessageBuilder.buildMessage()
                .ofType(Message.Type.chat)
                .setThread(ext.getGameId())
                .setBody("move")
                .addExtension(ext)
                .build();
        synchronized (chatLock) {
            Chat localChat = createChat();
            localChat.send(m);
        }
    }

    private void startGame(CellState opponentCellState ) {
        setGameState(GameState.IN_PROGRESS);
        board = new TicTacToeBoard();
        this.opponent.setCellState(opponentCellState);
        player.setCellState(opponentCellState == CellState.X ? CellState.O : CellState.X);
        System.out.println(BoardRender.renderBoard(board, player));
    }

    private void runCLI() throws Exception {
        System.out.println("Enter command: start, play, exit, exit curr, help");
        while (true) {
            String command = scanner.nextLine().trim();
            if (command.equalsIgnoreCase("start")) {
                startNewGame();
            } else if(command.equalsIgnoreCase("accept")) {
                acceptIncomingGameOffer();
            } else if (command.startsWith("play ")) {
                playMove(command);
            } else if (command.equalsIgnoreCase("exit")) {
                connection.disconnect();
                System.out.printf("Disconnected. Bye %s!%n", player.getName());
                break;
            }else if (command.contains("help")
                    || command.contains("?")
                    || command.equalsIgnoreCase("h")) {
                System.out.println(helpText);
            } else if (command.equalsIgnoreCase("exit curr")) {
                exitCurrentGame();
            }

        }
    }

    private void startNewGame() throws Exception {
        if(getGameState() != GameState.IDLE) {
            System.out.println("Please exit from current game by inputting 'exit curr'. And start again afterwards.");
            return;
        }
        System.out.println("Choose your character (X or O):");
        CellState cellState;
        try {
            cellState = CellState.valueOf(scanner.nextLine().trim().toUpperCase());
        }catch (IllegalArgumentException e) {
            System.out.println("Invalid symbol chosen. Please try again.");
            return;
        }
        player.setCellState(cellState);
        System.out.println("Please input opponents userName in the following format 'userNaem@xmpp.jp'");
        setOpponent(scanner.nextLine().trim());
        System.out.println("Waiting for opponent to agree to the game:");
        int i = 1;
        boolean success = true;
        while(getGameState() != GameState.IN_PROGRESS) {
            if(i++ == 30) {
                System.out.println("\nOpponent is not answering");
                success = false;
                break;
            }
            System.out.print("|");
            Thread.sleep(millis);
        }
        if(success) {
            startGame(player.getCellState() == CellState.X ? CellState.O : CellState.X);
        }
    }

    private void acceptIncomingGameOffer() {
        if(gameOffer != null)
        {
            if(gameState == GameState.IDLE){
                this.opponent = gameOffer.getOpponent();
                setCurrentGameId(gameOffer.getGameId());
                try {
                    sendMessage(gameOffer.getGameId(), "ok", "start");
                } catch (Exception e) {
                    System.out.println("[ERROR] while sending the ack" + e.getMessage());
                }
                setGameState(GameState.IN_PROGRESS);
                try {
                    startGame(opponent.getCellState());
                }catch (Exception e) {
                    System.out.println("[ERROR] while starting the game" + e.getMessage());
                }
            }
            gameOffer = null;
        }
    }

    private void playMove(String command) throws Exception {
        if(gameState != GameState.IN_PROGRESS) {
            System.out.println("There is no ongoing game to make a move on.");
            return;
        }
        Move move = extractMove(command);
        if (move == null) return;
        GameExtension gameExtension = new GameExtension("move", getCurrentGameId(), "ok", move);
        Message m =MessageBuilder.buildMessage()
                .ofType(Message.Type.chat)
                .setThread(getCurrentGameId())
                .setBody("move")
                .addExtension(gameExtension)
                .build();
        synchronized (chatLock) {
            Chat localChat = createChat();
            localChat.send(m);
        }
        System.out.println("Your move has been sent to the opponent.\nWaiting for ack.");
        int i = 0;
        while (!moveAckReceived){
            if(i++ == 30) {
                System.out.println("\nOpponent is not answering");
                break;
            }
            System.out.print("|");
            Thread.sleep(millis);
        }
        if(!moveAckReceived) {
            exitCurrentGame();
        }
    }

    private Move extractMove(String command) {
        int row, col;
        try {
            row = Integer.parseInt(command.split(" ")[1]);
            col = Integer.parseInt(command.split(" ")[2]);
        }catch (Exception e) {
            System.out.println("ERROR: invalid row or col number. Please try again.");
            return null;
        }
        Move move;
        try
        {
            move = new Move(player.getCellState(), row, col);
        }catch(Exception e){
            System.out.println("Invalid input for playing move: " + e.getMessage() + "\nPlease try again.");
            return null;
        }
        return move;
    }

    private void exitCurrentGame() throws Exception {
        if(getGameState() == GameState.IN_PROGRESS) {
            gameState = GameState.IDLE;
            sendMessage(getCurrentGameId(), "terminated", "exit");
            System.out.println("Current game has been terminated.");
        }else {
            System.out.println("No ongoing game to exit from.");
        }
    }

    public void play(String username, String password) throws Exception {
        setPlayer(new Player(username, password));
        connectAndLogin();
        System.out.println("Welcome to TicTacToe through XMPP!");
        System.out.println("Logged in as: " + connection.getUser());
        runCLI();
    }

    private Chat createChat() throws XmppStringprepException {
        if (chatManager == null) throw new IllegalStateException("chatManager not initialized");
        return chatManager.chatWith(JidCreate.entityBareFrom(opponent.getUserName()));
    }
}
