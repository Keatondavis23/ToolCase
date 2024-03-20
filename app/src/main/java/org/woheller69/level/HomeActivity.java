package org.woheller69.level;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;

public class HomeActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ImageView cardLevel = findViewById(R.id.button_level);
        cardLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, Level.class);
                startActivity(intent);
            }
        });

        //RangeFinder Button
        ImageView buttonRangeFinder = findViewById(R.id.button_range_finder);
        buttonRangeFinder.setOnClickListener(v -> {
                    Intent intent = new Intent(HomeActivity.this, RangeFinder.class);
                    startActivity(intent);
        });

        //Thermometer Button
        ImageView thermometer = findViewById(R.id.button_temperature);
        thermometer.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, Thermometer.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
