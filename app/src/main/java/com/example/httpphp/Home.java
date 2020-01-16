package com.example.httpphp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Home extends AppCompatActivity {


    EditText url;
    Button Start;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Start = findViewById(R.id.start);
        url = findViewById(R.id.geturl);

        Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotomain();
            }
        });




    }
    public void gotomain()
    {
        Intent intent = new Intent(Home.this,MainActivity.class);
        intent.putExtra("geturl",url.getText().toString());
        startActivity(intent);

    }
}
