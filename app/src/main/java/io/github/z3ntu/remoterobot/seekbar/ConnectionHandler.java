package io.github.z3ntu.remoterobot.seekbar;


import android.app.FragmentManager;
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

    private Socket client;
    private PrintWriter printwriter;
    private Message message;
    private String ip;
    private FragmentManager fragmentManager;

    private boolean socketActive = false;

    public ConnectionHandler(Looper looper, String ip, FragmentManager fragmentManager) {
        super(looper);
        this.ip = ip;
        this.fragmentManager = fragmentManager;
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
        if(printwriter != null) {
            printwriter.println(message.obj);
        } else {
            new ErrorDialogFragment().setText("There is no connection!").show(fragmentManager, null);
        }
    }

    protected void quit() {
        getLooper().quit();
    }

    private void makeConnection() {
        try {
            client = new Socket(ip, 2048);
            printwriter = new PrintWriter(client.getOutputStream(), true);
            socketActive = true;
        } catch (IOException e) {
            new ErrorDialogFragment().setText("Error while connecting to the socket!").show(fragmentManager, null);
            socketActive = false;
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void closeConnection() {
        System.out.println("Closing connection...");
        printwriter.close();
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        socketActive = false;
        quit();
    }

    public static class MessageCode {
        public static int CLASS_CONNECTION = 0;
        public static int CONNECTION_CONNECT = 1;
        public static int CLASS_COMMAND = 2;
    }

    public boolean isSocketActive(){
        return socketActive;
    }

}
