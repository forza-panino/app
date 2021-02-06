package com.example.cardriver;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//TODO: le eccezione le gestisco qui o nel main? io direi che dipende se debbano avere un risultato anche nella ui o meno

class BLConnectionHandler {

    private BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private static final String UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private OutputStream outputStream;
    private InputStream inputStream;
    private static final String HANDSHAKE = "handshake";
    static final String EXTRANAME = "com.example.cardriver.BLADAPTER";
    private Context context;
    static final int SELECTOR_RC = 1224;
    Map<String, BluetoothDevice> map = new HashMap<String, BluetoothDevice>();
    boolean connected = false;

    BLConnectionHandler (@org.jetbrains.annotations.NotNull Context context){
        this.bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bluetoothManager.getAdapter();
        this.context = context;
    }

    //Seleziona solo il dispositvo, poi va startata la connessione
    void selectDevice () {

        ArrayList<String> devices_names = new ArrayList<>();
        Set<BluetoothDevice> bluetoothDevices = this.bluetoothAdapter.getBondedDevices();
        if (bluetoothDevices != null) {
            for (BluetoothDevice device : bluetoothDevices){
                map.put(device.getName(), device);
                devices_names.add(device.getName());
            }

            Intent intent = new Intent(context.getApplicationContext(), DeviceSelectionActivity.class);
            intent.putStringArrayListExtra(EXTRANAME, devices_names);
            ((Activity)context).startActivityForResult(intent, SELECTOR_RC);
        } else {
            new AlertDialog.Builder(context)
                    .setTitle("No paired devices")
                    .setTitle("You have no paired devices. Please, pair with receiver and then come back.")
                    .setPositiveButton("OK", null)
                    .show();
        }

    }

    String comm(String msg, boolean RX) throws IOException {

        byte[] bytes = msg.getBytes();

        for (byte byte_ : bytes){
            this.outputStream.write(byte_);
        }

        this.outputStream.write((byte)'\n');

        if (RX) {

            byte[] response = new byte[24];
            byte[] inbound = new byte[1];
            int i = 0;

            for (int n = this.inputStream.read(inbound); n != -1; n = this.inputStream.read(inbound)){
                if (new String(inbound).equals("\n")) break;
                response[i] = inbound[0];
                i++;
            }

            return new String(Arrays.copyOfRange(response, 0, i));

        }

        return "DONE";

    }

    int startConnection () {

        //codes:
        //1->handshake completed succesfully
        //0->wrong device (handshake failed)
        //-1-> IOException
        //-2-> No Device Selected

        if (bluetoothDevice != null) {
            try {
                this.bluetoothSocket = this.bluetoothDevice.createInsecureRfcommSocketToServiceRecord(java.util.UUID.fromString(UUID));
                this.bluetoothAdapter.cancelDiscovery();
                this.bluetoothSocket.connect();
                this.outputStream = this.bluetoothSocket.getOutputStream();
                this.inputStream = this.bluetoothSocket.getInputStream();

                if (this.comm(HANDSHAKE, true).equalsIgnoreCase(HANDSHAKE)) {
                    this.connected = true;
                    return 1;
                }
                else {
                    this.connected = true;
                    return 0;
                }
            } catch (IOException e) {
                Log.e("IOE connecting", e.toString());
                return -1;
            }

        }
        else return -2;
    }

    void closeConnection() throws IOException {

        this.comm("DISC", false);
        this.bluetoothSocket.close();
        this.connected = false;

    }

    /*void send(String command) throws IOException {
        this.outputStream.write(command.getBytes());
    }*/

}
