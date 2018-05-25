package com.stuttgart.uni.mc_exe2.com.stuttgart.uni.mc_exe2.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.stuttgart.uni.mc_exe2.R;


public class MainActivity extends AppCompatActivity {

    private Button mClientButton;
    private Button mServerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int dummy = 5;

        mClientButton = (Button) findViewById(R.id.main_clientButton);
        mServerButton = (Button) findViewById(R.id.main_serverButton);

        mClientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent clientIntent = new Intent(MainActivity.this, ClientActivity.class);
                startActivity(clientIntent);

            }
        });

        mServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent serverIntent = new Intent(MainActivity.this, ServerActivity.class);
                startActivity(serverIntent);

            }
        });
    }
}
