package com.example.android.laptopcontroller;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    public static final String SERVER_IP_FLAG = "com.ducat.deep.androidclient.ip";     //HOW CAN I CHANGE THEM??
    public static final String SERVER_PORT_FLAG = "com.ducat.deep.androidclient.port";
    private CommandSender mSender;
    TextView tv;
    private float initX = 0;
    private float initY = 0;
    private float disX = 0;
    private float disY = 0;
    private boolean isConnected = false;
    private boolean mouseMoved = false;
    private Socket socket;
    private PrintWriter out;
    private String mLastInput;

    EditText t1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mouse);
        Intent i = getIntent();
        try{
            Constants.SERVER_IP = i.getExtras().getString("ip");
        }
        catch(Exception e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
        Constants.SERVER_PORT = i.getExtras().getInt("port");

        t1 = (EditText) findViewById(R.id.textdata);
        tv = (TextView) findViewById(R.id.mousepad);

        tv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {                           //WHAT IS HAPPENING IN THE FIRST CASE AND THE SECOND CASE??
                    case MotionEvent.ACTION_DOWN:            //MEANING OF ACTION DOWN??
                        initX = event.getX();
                        initY = event.getY();
                        mouseMoved = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        disX = event.getX() - initX;
                        disY = event.getY() - initY;
                        initX = event.getX();
                        initY = event.getY();
                        if (disX != 0 || disY != 0) {
                            out.println(disX + "," + disY); //send mouse movement to server
                        }
                        mouseMoved = true;
                        break;

                }
                return true;
            }
        });


        t1.addTextChangedListener(new TextWatcher() {

            //THIS METHOD NOTIFY THAT 'COUNT' CHARACTERS BEGINNING AT 'START' ARE ABOUT TO REPLACED BY BY NEW TEXT WITH LENGHT 'AFTER'
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { //WHAT ARE THESE INPUT PARAMETERS??
                //Toast.makeText(MainActivity.this,s.toString(), Toast.LENGTH_SHORT).show();
                mLastInput = s.toString();
            }

            //THIS METHOD NOTIFY THAT'COUNT' CHARACTERS BEGINNING AT'START' HAVE JUST REPLACED OLD TEXT THAT HAD LENGTH 'BEFORE'
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() == 0) return;
                String lastChar = s.subSequence(s.length() - 1, s.length()).toString();

                if (!TextUtils.isEmpty(lastChar) && s.length() > mLastInput.length()) {
                    //Toast.makeText(MainActivity.this, lastChar, Toast.LENGTH_SHORT).show();      //WHAT ARE WE DOING HERE??
                    sendCommand(OperationData.OPERATION_TYPE_TEXT, 0, 0, lastChar);
                }
            }

            //THIS METHOD IS CALLED TO NOTIFY THA SOME TEXT IS CHANGED WITHIN S
            @Override
            public void afterTextChanged(Editable s) {
                //Toast.makeText(MainActivity.this, "after", Toast.LENGTH_SHORT).show();

            }
        });

        t1.setOnKeyListener(new View.OnKeyListener() {             //WHY ARE WE HAVING THIS METHOD??  AND IT IS ALWAYS RETURNING FALSE??
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //Toast.makeText(MainActivity.this, event.getKeyCode()+"", Toast.LENGTH_SHORT).show();
                return false;                                      //WHY WE ARE NOT DOING ANYTHING IN THIS METHOD??
            }
        });
        mSender = new CommandSender(null, -1);
        mSender.start();
    }

    public void delete(View v) {
        sendCommand(OperationData.OPERATION_DEL_TEXT, 0, 0, "");
    }

    private void sendCommand(int operationKind, int x, int y, String input) {
        Message msg = Message.obtain(mSender.mHandler);
        OperationData operation = new OperationData();
        operation.setOperationKind(operationKind);
        if (x != 0) operation.setMoveX(x);
        if (y != 0) operation.setMoveY(y);
        if (input != null) operation.setInputStr(input);
        //d

        // Toast.makeText(this, "Hello", Toast.LENGTH_SHORT).show();
        Log.i("hello", "" + operation);
        msg.obj = CommandParser.parseCommand(operation);                     //WHAT IS THIS DOING ??
        out.println(msg.obj);
        msg.sendToTarget();
        // Toast.makeText(this, "Hello", Toast.LENGTH_SHORT).show();
    }

    public void submit(View v) {
        if (v.getId() == R.id.left) {
            if (isConnected && out != null) {
                out.println(Constants.MOUSE_LEFT_CLICK); //send "previous" to server
            }
        } else if (isConnected && out != null) {
            out.println(Constants.MOUSE_RIGHT_CLICK); //send "previous" to server
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_data, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_connect) {
            ConnectPhoneTask connectPhoneTask = new ConnectPhoneTask();
            connectPhoneTask.execute(Constants.SERVER_IP); //try to connect to server in another thread
            return true;
        }

        return false;
    }

    private class ConnectPhoneTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {                 //WHAT KIND OF INPUT PARAMETERS ARE RECEIVED IN THIS METHOD, WHAT IS ... ??
            boolean result = true;
            try {
                InetAddress serverAddr = InetAddress.getByName(params[0]);   //WHY THE INDEX IS 0 ??
                socket = new Socket(serverAddr, Constants.SERVER_PORT);//Open socket on server IP and port
            } catch (IOException e) {
                Log.e("remotedroid", "Error while connecting", e);
                result = false;
            }
            return result;

        }

        @Override
        protected void onPostExecute(Boolean result) {
            isConnected = result;
            Toast.makeText(MainActivity.this, isConnected ? "Connected to server!" : "Error while connecting", Toast.LENGTH_LONG).show();
            try {
                if (isConnected) {
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket                              //STUDY ABOUT ALL THESE WRITERS
                            .getOutputStream())), true); //create output stream to send data to server
                }
            } catch (IOException e) {
                Log.e("remotedroid", "Error while creating OutWriter", e);
                Toast.makeText(MainActivity.this, "Error while connecting", Toast.LENGTH_LONG).show();
            }
        }

    }

}
