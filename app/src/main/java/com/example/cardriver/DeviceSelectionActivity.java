package com.example.cardriver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class DeviceSelectionActivity extends AppCompatActivity {

    static final String RESULT_NAME = "selected_device";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_selection);

        Intent intent = getIntent();

        LinearLayout devices_container = findViewById(R.id.devices_list_ll);

        for (String device_name : intent.getStringArrayListExtra(BLConnectionHandler.EXTRANAME)){

            LinearLayout row_layout = (LinearLayout) getLayoutInflater().inflate(R.layout.device_selection_row, null);
            ((TextView) row_layout.findViewById(R.id.device_row)).setText(device_name);
            row_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO: fare anche un result false così che si possa sapere quando è annullato (ondestroy o cancel boh)
                    Intent result = new Intent();
                    result.putExtra(RESULT_NAME, ((TextView) view.findViewById(R.id.device_row)).getText());
                    setResult(Activity.RESULT_OK, result);
                    finish();
                }
            });
            devices_container.addView(row_layout);

        }

    }
}
