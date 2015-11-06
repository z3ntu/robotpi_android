package io.github.z3ntu.remoterobot.socket;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by luca on 12.06.15.
 */
public class ConnectionHandler extends Handler {
    public static final String MESSAGE_DATA = "be.zweetinc.PcController.Message_Data";
    private Socket client;
    private PrintWriter printwriter;
    private Message message;
    private String ip;

    public ConnectionHandler(Looper looper, String ip) {
        super(looper);
        this.ip = ip;
    }

    @Override
    public void handleMessage(Message msg) {
        message = msg;
        super.handleMessage(msg);    //To change body of overridden methods use File | Settings | File Templates.
        if (message.what != MessageCode.CLASS_CONNECTION) {
            sendMessageToServer();
        } else {
            handleConnection();
        }
    }

    private void handleConnection() {
        if (message.arg1 == MessageCode.CONNECTION_CONNECT) {
            makeConnection();
        } else {
            closeConnection();
        }
    }

    private void sendMessageToServer() {
        printwriter.println(message.obj);
    }

    protected void quit() {
        getLooper().quit();
    }

    private void makeConnection() {
        try {
            client = new Socket(ip, 2048);
            printwriter = new PrintWriter(client.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void closeConnection() {
        printwriter.close();
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        quit();
    }

    public static class MessageCode {
        public static int CLASS_CONNECTION = 0;
        public static int CONNECTION_CONNECT = 1;
        public static int CLASS_COMMAND = 2;
    }

}
