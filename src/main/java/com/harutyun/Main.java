package com.harutyun;


import com.harutyun.game.TicTacToe;
import com.harutyun.game.core.TicTacToeBoard;

import java.io.Console;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        TicTacToe ticTacToe = new TicTacToe(new TicTacToeBoard());
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("Welcome! To play this game, you have to have an account in https://www.xmpp.jp/signup.\nIf you already have one please log in below:");
            System.out.println("Enter username (format: user_name@xmpp.jp):");
            String username = scanner.nextLine();
            String password;
            Console console = System.console();
            if (console != null) {
                char[] pwd = console.readPassword("Enter password:\n");
                password = new String(pwd);
            } else {
                System.out.println("Enter password: ");
                password = scanner.nextLine();
            }
            System.out.println("Starting game...");
            ticTacToe.play(username, password);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
