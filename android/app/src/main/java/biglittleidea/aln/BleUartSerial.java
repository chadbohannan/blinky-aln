package biglittleidea.aln;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import biglittleidea.alnn.App;

@SuppressLint("MissingPermission")
public class BleUartSerial {
    boolean mIsConnected = false;
    private BluetoothGatt mGatt;
    private BluetoothDevice bleDevice;
    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"); // Client Characteristic Configuration Descriptor
    public static final UUID SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    public interface OnDataHandler {
        void onData(byte[] data);
    }
    private OnDataHandler onDataHandler = null;

    public interface OnConnectHandler {
        void onConnect(boolean isConnected);
    }
    private OnConnectHandler onConnectHandler = null;

    public BleUartSerial(BluetoothDevice bleDevice) {
        this.bleDevice = bleDevice;
    }

    public void setOnConnectHandler(OnConnectHandler och) {
        this.onConnectHandler = och;
    }

    public void setOnDataHandler(OnDataHandler odh) {
        this.onDataHandler = odh;
    }

    public void connect() {
        mGatt = bleDevice.connectGatt(App.getInstance().getBaseContext(), false, gattCallback);
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("ALNN", "onConnectionStateChange, " +  "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("ALNN", "gattCallback, STATE_CONNECTED");
                    gatt.discoverServices();
                    mIsConnected = true;
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("ALNN", "gattCallback STATE_DISCONNECTED");
                    mIsConnected = false;
                    break;
                default:
                    Log.e("ALNN", "gattCallback, STATE_OTHER");
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            enableTXNotification();
            if (onConnectHandler != null) {
                onConnectHandler.onConnect(true);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) { }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//            Log.i("ALNN", String.format("onCharacteristicChanged, %s: %s",
//                    characteristic.getUuid().toString(),
//                    new String(characteristic.getValue())));
            if (onDataHandler != null) {
                onDataHandler.onData(characteristic.getValue());
            }
        }

        @Override
        public void onCharacteristicWrite (BluetoothGatt gatt,
                                           BluetoothGattCharacteristic characteristic,
                                           int status) {
            Log.i("ALNN", String.format("onCharacteristicWrite, %s: %s, status: %d",
                characteristic.getUuid().toString(),
                new String(characteristic.getValue()),
                status));
        }
    };

    public void enableTXNotification()
    {
        BluetoothGattService service = mGatt.getService(SERVICE_UUID);
        if (service == null) {
            Toast.makeText(App.getInstance(), "null service err", Toast.LENGTH_SHORT).show();
            Log.i("ALNN", "enableTXNotification: null service err");
            return;
        }
        BluetoothGattCharacteristic txChar = service.getCharacteristic(TX_CHAR_UUID);
        if (txChar == null) {
            Toast.makeText(App.getInstance(), "null txCharr err", Toast.LENGTH_SHORT).show();
            Log.i("ALNN", "enableTXNotification: null txCharr err");
            return;
        }
        mGatt.setCharacteristicNotification(txChar,true);

        BluetoothGattDescriptor descriptor = txChar.getDescriptor(CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mGatt.writeDescriptor(descriptor);
    }

    public void writeRXCharacteristic(byte[] value)
    {
        List<BluetoothGattService> services = mGatt.getServices();
        if (services == null) {
            Log.d("ALNN", "writeRXCharacteristic: no ble services discovered");
            return;
        }
//        for (BluetoothGattService bgs : services) {
//            Log.i("ALNN", "writeRXCharacteristic: bgs service:"+ bgs.getUuid().toString());
//        }

        BluetoothGattService rxService = mGatt.getService(SERVICE_UUID);
        if (rxService == null) {
            return;
        }
        BluetoothGattCharacteristic rxChar = rxService.getCharacteristic(RX_CHAR_UUID);
        if (rxChar == null) {
            return;
        }

        rxChar.setValue(value);
        rxChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);

        boolean status = mGatt.writeCharacteristic(rxChar);
        Log.d("ALNN", "write txChar status: " + status);
    }


    public void send(byte[] frame) {
        int BLE_FRAME_SZ = 20;
        if (frame.length < BLE_FRAME_SZ) {
            writeRXCharacteristic(frame);
        } else {
            for(int i = 0; i < frame.length; i += BLE_FRAME_SZ) {
                int end = Math.min(i+BLE_FRAME_SZ, frame.length);
                byte[] slice = Arrays.copyOfRange(frame, i, end);
                writeRXCharacteristic(slice);
            }
        }
    }

    public void close() {
        mGatt.close();
    }
}
