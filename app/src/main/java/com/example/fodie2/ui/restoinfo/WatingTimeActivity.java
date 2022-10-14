package com.example.fodie2.ui.restoinfo;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fodie2.PaymentActivity;
import com.example.fodie2.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class WatingTimeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wating_time);
        getSupportActionBar().hide();
        findViewById(R.id.proceed_to_payment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), PaymentActivity.class);
                startActivity(i);
            }
        });
    }
}