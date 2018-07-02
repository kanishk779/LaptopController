package com.example.android.laptopcontroller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by hp on 28-06-2018.
 */

public class SplashFile extends AppCompatActivity {
    EditText t1,t2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_file);
        t1=(EditText)findViewById(R.id.ipaddress);
        t2=(EditText)findViewById(R.id.port);
    }

    public void submit(View v){
        if(!t1.getText().toString().equals("")&&!TextUtils.isEmpty(t2.getText().toString())){
            Intent in=new Intent(this,MainActivity.class);
            in.putExtra("ip",t1.getText().toString());
            in.putExtra("port",Integer.parseInt(t2.getText().toString()));
            startActivity(in);
            finish();
        }
        else{
            Toast.makeText(this, "Please fill data", Toast.LENGTH_SHORT).show();
        }
    }
}
