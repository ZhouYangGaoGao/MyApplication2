package com.gaogao.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


  public void clickHandler(View source){
      TextView tv=(TextView) findViewById(R.id.show);
      tv.setText("hello android-"+new java.util.Date());
  }
  }

