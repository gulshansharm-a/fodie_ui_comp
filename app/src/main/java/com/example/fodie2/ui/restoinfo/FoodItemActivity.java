package com.example.fodie2.ui.restoinfo;

import androidx.appcompat.app.AppCompatActivity;
import com.example.fodie2.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class FoodItemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_item);
        getSupportActionBar().hide();
        findViewById(R.id.add_to_cart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),WatingTimeActivity.class);
                startActivity(i);
            }
        });
    }
}