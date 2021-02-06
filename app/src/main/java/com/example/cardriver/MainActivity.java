package com.example.cardriver;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private BLConnectionHandler blConnectionHandler;
    private TextView status_bar;
    private Context context;
    private SharedPreferences activityPreferences;
    private String first_launch_preference_key = "first_launch_preference_key";

    //TODO: quando darò la possibilità di annullare l'apertura di connessione che si fa? si re-istanzia l'handler?
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK){
            Button button = findViewById(R.id.connect_button);
            blConnectionHandler.bluetoothDevice = blConnectionHandler.map.get(data.getStringExtra(DeviceSelectionActivity.RESULT_NAME));
            switch (blConnectionHandler.startConnection()) {
                case 1:
                    status_bar.setText("Receiver status: CONNECTED");
                    status_bar.setTextColor(getResources().getColor(R.color.connected));
                    button.setTextColor(getResources().getColor(R.color.not_connected));
                    button.setText("DISC");
                    break;
                case 0:
                    status_bar.setText("Receiver status: WRONG DEVICE");
                    status_bar.setTextColor(getResources().getColor(R.color.not_connected));
                    button.setTextColor(getResources().getColor(R.color.not_connected));
                    button.setText("DISC");
                    break;
                case -1:
                    status_bar.setText("ERROR OCCURRED (MAYBE RECEIVER OFF)");
                    status_bar.setTextColor(getResources().getColor(R.color.not_connected));
                    //TODO: è necessario?
                    blConnectionHandler = new BLConnectionHandler(context);
                    button.setText("CONNECT");
                    button.setTextColor(getResources().getColor(R.color.connected));
                    break;
                case -2:
                    status_bar.setTextColor(getResources().getColor(R.color.not_connected));
                    status_bar.setText("Receiver status: NO DEV SELECTED");
                    button.setText("CONNECT");
                    button.setTextColor(getResources().getColor(R.color.connected));
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        activityPreferences = getPreferences(MODE_PRIVATE);

        status_bar = findViewById(R.id.status_bar);
        WebView webView = findViewById(R.id.camera_display);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://192.168.4.1:81/stream");

        blConnectionHandler = new BLConnectionHandler(this);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //TODO: chiudere
        //blConnectionHandler.closeConnection();
    }

    private static int velocity = 1;

    public void velocitySetter(View view) {

        switch(view.getId()){

            case R.id.vel1:
                velocity = 0;
                view.setEnabled(false);
                findViewById(R.id.vel2).setEnabled(true);
                findViewById(R.id.vel3).setEnabled(true);
                break;

            case R.id.vel2:
                velocity = 1;
                view.setEnabled(false);
                findViewById(R.id.vel1).setEnabled(true);
                findViewById(R.id.vel3).setEnabled(true);
                break;

            case R.id.vel3:
                velocity = 2;
                view.setEnabled(false);
                findViewById(R.id.vel1).setEnabled(true);
                findViewById(R.id.vel2).setEnabled(true);
                break;

        }

    }

    private static String machine = "CAR";

    public void machineSelector(View view) {

        if (((SwitchCompat) findViewById(R.id.toggle)).isChecked()) {
            machine = "ARM";
            ((TextView) findViewById(R.id.arm)).setTextColor(getResources().getColor(R.color.selected_textcolor));
            ((TextView) findViewById(R.id.car)).setTextColor(getResources().getColor(R.color.notselected_textcolor));
            findViewById(R.id.CLAW_CLOSE).setEnabled(true);
            findViewById(R.id.CLAW_OPEN).setEnabled(true);
            findViewById(R.id.LIFT_UP).setEnabled(true);
            findViewById(R.id.LIFT_DOWN).setEnabled(true);
        }

        else {
            machine = "CAR";
            ((TextView) findViewById(R.id.car)).setTextColor(getResources().getColor(R.color.selected_textcolor));
            ((TextView) findViewById(R.id.arm)).setTextColor(getResources().getColor(R.color.notselected_textcolor));
            findViewById(R.id.CLAW_CLOSE).setEnabled(false);
            findViewById(R.id.CLAW_OPEN).setEnabled(false);
            findViewById(R.id.LIFT_UP).setEnabled(false);
            findViewById(R.id.LIFT_DOWN).setEnabled(false);
        }

    }

    public void connectToDevice() {
        //ATTENZIONE: se vuoi fare un handling del risultato di selectdevice() sali sopra su
        // onActivityResult(): è quello che gestisce il risultato di tutto (qui fai solo da starter)
        if (blConnectionHandler.bluetoothAdapter != null) {
            if (blConnectionHandler.bluetoothAdapter.isEnabled()) {
                blConnectionHandler.selectDevice();
            }
            else {
                new AlertDialog.Builder(context)
                        .setIcon(R.drawable.ic_launcher_foreground)
                        .setTitle("Enable Bluetooth")
                        .setMessage("The app is going to enable the Bluetooth.\nPress OK to continue or CANCEL to discard.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                blConnectionHandler.bluetoothAdapter.enable();
                                //TODO: non mi piace thread.sleep() un giorno modificare
                                while (!blConnectionHandler.bluetoothAdapter.isEnabled()) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        Log.e("thread error", e.toString());
                                    }
                                }
                                blConnectionHandler.selectDevice();
                            }
                        })
                        .setNegativeButton("CANCEL", null)
                        .show();
            }
        }
        else {
            new AlertDialog.Builder(context)
                    .setIcon(R.drawable.ic_launcher_foreground)
                    .setTitle("Buetooth required")
                    .setMessage("The app needs the Bluetooth feature in order to continue with this action.\n" +
                            "You can still use the camera stream, though.")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    public void disconnect() {
        try {
            blConnectionHandler.closeConnection();
            Button button = findViewById(R.id.connect_button);
            button.setText("CONNECT");
            button.setTextColor(getResources().getColor(R.color.connected));
            status_bar.setText("Receiver status: DISCONNECTED");
            status_bar.setTextColor(getResources().getColor(R.color.not_connected));


        }
        catch (IOException e) {
            Log.e("error closing connection", e.toString());
        }
    }

    public void connectClickedHandler(View view) {

        //primo avvio -> dialog di informazione
        if (activityPreferences.getBoolean(first_launch_preference_key, true)) {
            activityPreferences.edit()
                    .putBoolean(first_launch_preference_key, false)
                    .apply();

            new AlertDialog.Builder(context)
                    .setTitle("Warning!")
                    .setMessage("If you haven't already paired the receiver the app won't show it in the list.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            connectToDevice();
                        }
                    })
                    .show();

        }

        //già sa che deve pairare; bisogna solo controllare se una connessione sia già attiva o meno
        else {
            if (blConnectionHandler.connected) disconnect();
            else connectToDevice();
        }

    }

}
