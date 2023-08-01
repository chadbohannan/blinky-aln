package biglittleidea.aln;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import biglittleidea.alnn.App;

@SuppressLint("MissingPermission")
public class BleUartChannel implements IChannel {
    IChannelCloseHandler ch = null;
    IPacketHandler ph = null;

    boolean mIsConnected = false;
    private BluetoothGatt mGatt;


    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"); // Client Characteristic Configuration Descriptor
    public static final UUID SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");


    public BleUartChannel(BluetoothDevice bleDevice) {
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
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("ALNN", "onServicesDiscovered, " + services.toString());
            enableTXNotification();
//            gatt.readCharacteristic(services.get(1).getCharacteristics().get(0));
        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("ALNN", "onCharacteristicRead, " +  characteristic.toString());
            // TODO parse packets
        }
    };

    public void enableTXNotification()
    {
        BluetoothGattService service = mGatt.getService(SERVICE_UUID);
        if (service == null) {
            return;
        }
        BluetoothGattCharacteristic txChar = service.getCharacteristic(TX_CHAR_UUID);
        if (txChar == null) {
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
        for (BluetoothGattService bgs : services) {
            Log.d("ALNN", "bgs service:"+ bgs.getUuid().toString());
        }

        BluetoothGattService RxService = mGatt.getService(SERVICE_UUID);
        if (RxService == null) {
            return;
        }
        BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(RX_CHAR_UUID);
        if (RxChar == null) {
            return;
        }
        RxChar.setValue(value);
        boolean status = mGatt.writeCharacteristic(RxChar);

        Log.d("ALNN", "write txChar status: " + status);
    }

    @Override
    public void send(Packet p) {
        byte[] frame = Frame.toAX25Buffer(p.toFrameBuffer());
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

    @Override
    public void receive(IPacketHandler ph, IChannelCloseHandler ch) {
        this.ph = ph;
        this.ch = ch;
    }

    @Override
    public void close() {
        if (this.ch != null) {
            mGatt.close();
            this.ch.onChannelClosed(this);
        }
    }
}
