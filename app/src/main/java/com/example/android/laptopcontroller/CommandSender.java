package com.example.android.laptopcontroller;

import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.os.Handler;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;

/**
 * Created by hp on 28-06-2018.
 */

public class CommandSender extends Thread {
    public static final int SEND_COMAND = 0;


   /* public static final String SERVER_IP = "192.168.42.19";
    public static final int SERVER_PORT = 9898;*/

    private String mServerIP;
    private int mServerPort;
    private Socket socket;

    public Handler mHandler;


    public CommandSender(String serverIp, int port) {
        mServerIP = serverIp == null ? Constants.SERVER_IP : serverIp;                    //IF THE SERVERIP IS NULL THAN (LOCALHOST/THE VALUE GIVEN BY THE USER)  WILL BE ASSIGNED
        mServerPort = port == -1 ? Constants.SERVER_PORT : port;

    }


    private boolean send(String str) {
        try {

            Log.i("hello", "ip: " + mServerIP + "port: " + Constants.SERVER_PORT);

            socket = new Socket(mServerIP, Constants.SERVER_PORT);
            Log.i("hello", "socket: " + socket);
            Writer writer = new OutputStreamWriter(socket.getOutputStream());

            writer.append(str);    //THIS WILL APPEND THE STR TO THE WRITER.
            writer.flush();        //WRITER CLASS IS THE PARENT OF ALL THE WRITERS SUCH AS BUFFEREDWRITER,PRINTWRITER
            writer.close();
            socket.close();

            return true;


        } catch (IOException e) {
            return false;
        }
    }


    @Override
    public void run() {

        Looper.prepare();    ///what is this Looper used for ???
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                switch (msg.what) {
                    case SEND_COMAND:
                        send("command");                               //WHAT IS THE USE OF GENERATING TRUE OR FALSE IN THE RUN METHOD AND WHO WILL ACCEPT THESE BOOLEAN VALUES??
                        break;
                    default:
                        Log.e(CommandSender.class.getName(), "Unknown command msg.what = " + msg.what);
                }
            }
        };
        Looper.loop();     //WHAT WILL HAPPEN IF WE DONT USE THE LOOPER??
    }
}
