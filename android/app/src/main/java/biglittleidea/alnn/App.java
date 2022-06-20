package biglittleidea.alnn;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class App extends Application {
    private static App instance;

    private final MutableLiveData<Boolean> isWifiConnected = new MutableLiveData<>();
    public final MutableLiveData<String> msg = new MutableLiveData<>();
    public final MutableLiveData<List<LocalInetInfo>> localInetInfo = new MutableLiveData<>();
    public final MutableLiveData<List<BeaconInfo>> beaconInfo = new MutableLiveData<>();

    public static App getInstance() {
        return instance;
    }

    private void updateWifi() {
        localInetInfo.setValue(NetUtil.getLocalInetInfo());
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();

        updateWifi();

        // subscribe to wifi status updates
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
                    boolean isConnected = intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false);
                    isWifiConnected.setValue(isConnected);
                    if (isConnected) {
                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(() -> updateWifi(), 5000);
                    } else {
                        msg.setValue("wifi disconnected");
                    }
                }
            }
        }, intentFilter);

    }

    UDPListener makeListener(InetAddress bcastAddress, short port) {
        UDPListener listener = new UDPListener(bcastAddress, port);
        listener.setMessageHandler(new UDPListener.MessageHandler() {
            @Override
            public void onMessage(byte[] message) {
                String uri = new String(message);
                String[] parts = uri.split("://");
                String protocol = parts[0];
                parts = parts[1].split(":");
                String host = parts[0];
                short port = Short.parseShort(parts[1]);

                BeaconInfo info = new BeaconInfo(protocol, host, port);
                List<BeaconInfo> infos = beaconInfo.getValue();
                if (infos == null) {
                    infos = new ArrayList<BeaconInfo>();
                }
                for(BeaconInfo _info : infos) {
                    if(info.equals(_info))
                        return;
                }
                infos.add(info);
                beaconInfo.postValue(infos);
                Log.d("ALNN", uri.toString());
            }
        });
        return listener;
    }

    public void listenToUDP(InetAddress bcastAddress, short port, boolean listen) {
        synchronized (bcastListenMap) {
            String path = String.format("%s:%d", bcastAddress.toString(), port);
            UDPListener listener;
            if (bcastListenMap.containsKey(path)) {
                listener = bcastListenMap.get(path);
            } else {
                listener = makeListener(bcastAddress, port);
                listener.start();
                bcastListenMap.put(path, listener);
                return;
            }
            if (listen && !listener.isRunning()) {
                listener = makeListener(bcastAddress, port);
                listener.start();
                bcastListenMap.put(path, listener);
            } else if (!listen && listener.isRunning()) {
                listener.end();
            }
            localInetInfo.setValue(localInetInfo.getValue()); // trigger observers
        }
    }

    HashMap<String, UDPListener> bcastListenMap = new HashMap<>();
    public boolean isListeningToUDP(InetAddress bcastAddress, short port) {
        String path = String.format("%s:%d", bcastAddress.toString(), port);
        synchronized (bcastListenMap) {
            if (!bcastListenMap.containsKey(path)) {
                return false;
            }
            return bcastListenMap.get(path).isRunning();
        }
    }

    boolean checked = false;
    public void connectTo(String protocol, String host, short port, boolean enable) {
        // TODO
        checked = enable;
        localInetInfo.setValue(localInetInfo.getValue()); // trigger observers
    }

    public boolean isConnected(String protocol, String host, short port) {
        return checked;
    }
}
