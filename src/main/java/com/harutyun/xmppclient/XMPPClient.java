package com.harutyun.xmppclient;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;

public class XMPPClient {

    public static void main(String[] args) throws Exception {

        System.out.println("Starting Har's XMPP Client...");

        // ------------ CONFIGURATION ------------
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setHost("xmpp.jp") // server host
                .setXmppDomain("xmpp.jp") // domain
                //.setUsernameAndPassword("har", "yourPassword")
                .setSecurityMode(ConnectionConfiguration.SecurityMode.ifpossible)
                .setPort(5222)
                // ADD THIS LINE
                .setHostnameVerifier((hostname, session) -> true) // accept any certificate hostname
                .build();

        // ------------ CONNECT ------------
        XMPPTCPConnection connection = new XMPPTCPConnection(config);


        try {
            connection.connect();
            System.out.println("Connected to XMPP server.");

//            AccountManager accountManager = AccountManager.getInstance(connection);
//            accountManager.sensitiveOperationOverInsecureConnection(true); // only if using plain TCP
            String password = "TryingSomething100!";
            String username = "har8";
//            accountManager.createAccount(Localpart.from(username), password);

            connection.login(username, password);
            System.out.println("Logged in as: " + connection.getUser());

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // ------------ LISTEN FOR MESSAGES ------------
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        chatManager.addIncomingListener((from, message, chat) -> {
            System.out.println("\n📩 Message from " + from + ": " + message.getBody());
        });

        // ------------ SEND A TEST MESSAGE ------------
        EntityBareJid friend = JidCreate.entityBareFrom("friend@xmpp.jp");
        Chat chat = chatManager.chatWith(friend);

        chat.send("Hello, this is Har's test message!");

        System.out.println("Message sent.");
        System.out.println("Client is now listening for incoming messages...");

        // keep the client alive
        while (true) {
            Thread.sleep(1000);
        }
    }
}
