package biglittleidea.alnn;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import biglittleidea.aln.BluetoothChannel;
import biglittleidea.aln.IChannel;
import biglittleidea.aln.Router;
import biglittleidea.aln.TcpChannel;
import biglittleidea.aln.Packet;
import biglittleidea.aln.TlsChannel;

public class App extends Application {
    private static App instance;

    List<LocalServiceHandler> localServices = new ArrayList<>();
    public final MutableLiveData<List<LocalServiceHandler>> mldLocalServices = new MutableLiveData<>();

    private final MutableLiveData<Boolean> isWifiConnected = new MutableLiveData<>();
    public final MutableLiveData<List<LocalInetInfo>> localInetInfo = new MutableLiveData<>();
    public final MutableLiveData<List<BeaconInfo>> beaconInfo = new MutableLiveData<>();
    public final MutableLiveData<Map<String, Router.NodeInfoItem>> mldNodeInfo = new MutableLiveData<>();
    public final MutableLiveData<Set<String>> directConnections = new MutableLiveData<>();
    public final MutableLiveData<Integer> numActiveConnections = new MutableLiveData<>();
    public final MutableLiveData<List<BluetoothDevice>> bluetoothDevices = new MutableLiveData<>();
    public final MutableLiveData<String> bluetoothDiscoveryStatus = new MutableLiveData<>();
    public final MutableLiveData<Boolean> bluetoothDiscoveryIsActive = new MutableLiveData<>();

    public final MutableLiveData<String> qrDialogLabel = new MutableLiveData<>();
    public final MutableLiveData<String> qrScanResult = new MutableLiveData<>();

    Set<String> services;
    TreeMap<String, Set<String>> actions = new TreeMap<>();
    TreeSet<String> connections = new TreeSet();

    HashMap<String, IChannel> channelMap = new HashMap<>();
    public Router alnRouter;

    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    public static App getInstance() {
        return instance;
    }

    public void send(Packet packet) {
        alnRouter.send(packet);
    }

    private void updateWifi() {
        localInetInfo.setValue(NetUtil.getLocalInetInfo());
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();

        loadServices();
        loadDirectConnections();
        numActiveConnections.setValue(0);
        bluetoothDiscoveryIsActive.setValue(false);
        bluetoothDevices.setValue(new ArrayList<>());

        // use a consistent UUID for this node; create one if this is the first run
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String localAddress = prefs.getString("__localAddress", "");
        if (localAddress.length() == 0) {
            String addr = UUID.randomUUID().toString();
            prefs.edit().putString("__localAddress", addr).apply();
            localAddress = addr;
        }
        alnRouter = new Router(localAddress);

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
                        handler.postDelayed(() -> updateWifi(), 5000); // wait a long time before sync'ing UI
                    }
                }
            }
        }, intentFilter);

        // Default local service
        addLocalService("log");

        mldNodeInfo.setValue(alnRouter.availableServices());
        alnRouter.setOnStateChangedListener(new Router.OnStateChangedListener() {
            @Override
            public void onStateChanged() {
                mldNodeInfo.postValue(alnRouter.availableServices());
            }
        });
    }

    public void addLocalService(String name) {
        LocalServiceHandler lsh = new LocalServiceHandler(name);
        alnRouter.registerService(lsh.service, lsh);
        localServices.add(lsh);
        mldLocalServices.setValue(localServices);
        Map<String, Router.NodeInfoItem> as = alnRouter.availableServices();
        mldNodeInfo.setValue(as);
    }

    public void removeLocalService(String name) {
        alnRouter.unregisterService(name);
        for (int i = 0; i < localServices.size(); i++) {
            if (localServices.get(i).service.equals(name)) {
                localServices.remove(i--); // remove at i; inspect at i again
            }
        }
        mldLocalServices.setValue(localServices);
    }

    UDPListener makeListener(InetAddress bcastAddress, short port) {
        UDPListener listener = new UDPListener(bcastAddress, port);
        listener.setMessageHandler(new UDPListener.MessageHandler() {
            @Override
            public void onMessage(byte[] message) {
                String uri = new String(message);
                BeaconInfo info = NetUtil.beaconInfoFromUri(uri);
                if (info == null) {
                    Log.d("ALNN", "failed to parse bcast msg:" + uri);
                    return;
                }
                List<BeaconInfo> infos = beaconInfo.getValue();
                if (infos == null) {
                    infos = new ArrayList<BeaconInfo>();
                }
                for (BeaconInfo _info : infos) {
                    if (info.equals(_info))
                        return;
                }
                infos.add(info);
                beaconInfo.postValue(infos);
                Log.d("ALNN", uri);
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

    @SuppressLint("MissingPermission")
    public String connectTo(BluetoothDevice device, String serviceUuid, boolean enable) {
        String path = String.format("%s:%s", device.getAddress(), serviceUuid);
        if (enable) {
            try {
                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID.fromString(serviceUuid));
                IChannel channel = new BluetoothChannel(socket);
                alnRouter.addChannel(channel);
                channelMap.put(path, channel);
            } catch (IOException e) {
                e.printStackTrace();
                return e.getLocalizedMessage();
            }
        } else if (channelMap.containsKey(path)) {
            channelMap.get(path).close();
            channelMap.remove(path);
        }
        numActiveConnections.postValue(channelMap.size());
        bluetoothDevices.postValue(bluetoothDevices.getValue()); // trigger redraw
        return null;
    }

    public boolean isConnected(BluetoothDevice device, String serviceUuid) {
        String path = String.format("%s:%s", device.getAddress(), serviceUuid);
        synchronized (channelMap) {
            return channelMap.containsKey(path);
        }
    }

    public String connectTo(String protocol, String host, short port, String node, boolean enable) {
        synchronized (channelMap) {
            String path = String.format("%s:%d", host, port);
            if (enable && !channelMap.containsKey(path)) {
                Packet p;
                IChannel channel;
                switch (protocol) {
                    case "tcp+aln":
                        channel = new TcpChannel(host, port);
                        break;
                    case "tcp+maln":

                        channel = new TcpChannel(host, port);
                        p = new Packet();
                        p.DestAddress = node;
                        channel.send(p);
                        break;

                    case "tls+aln":
                        channel = new TlsChannel(host, port);
                        break;

                    case "tls+maln":
                        channel = new TlsChannel(host, port);
                        p = new Packet();
                        p.DestAddress = node;
                        channel.send(p);
                        break;

                    default:
                        return "protocol not supported";
                }
                alnRouter.addChannel(channel);
                channelMap.put(path, channel);
            } else if (!enable && channelMap.containsKey(path)) {
                channelMap.get(path).close();
                channelMap.remove(path);
            }
            numActiveConnections.postValue(channelMap.size());
        }
        localInetInfo.setValue(localInetInfo.getValue()); // trigger observers
        return null;
    }

    public boolean isConnected(String protocol, String host, short port) {
        String path = String.format("%s:%d", host, port);
        synchronized (channelMap) {
            return channelMap.containsKey(path);
        }
    }

    public void saveActionItem(String service, String title, String content) {
        if (!actions.containsKey(service)) {
            actions.put(service, new TreeSet<>());
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getInstance());
        Set<String> existingCheck = prefs.getStringSet(service, new TreeSet<>());
        Log.d("ALNN", String.format("%d exist", existingCheck.size()));

        actions.get(service).add(String.format("%s\t%s", title, content));

        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(service, actions.get(service));
        if (!services.contains(service)) {
            services.add(service);
            editor.putStringSet("__services", services);
        }
        editor.apply();
    }

    public void replaceActionItem(String service, String prevTitle, String prevContent, String title, String content) {
        if (!actions.containsKey(service)) {
            actions.put(service, new TreeSet<>());
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getInstance());
        Set<String> existingCheck = prefs.getStringSet(service, new TreeSet<>());
        Log.d("ALNN", String.format("%d exist", existingCheck.size()));


        actions.get(service).remove(String.format("%s\t%s", prevTitle, prevContent));
        actions.get(service).add(String.format("%s\t%s", title, content));

        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(service, actions.get(service));
        if (!services.contains(service)) {
            services.add(service);
            editor.putStringSet("__services", services);
        }
        editor.apply();
    }

    private void loadServices() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getInstance());
        services = prefs.getStringSet("__services", new TreeSet<>());

        for (String service : services) {
            Set<String> records = prefs.getStringSet(service, new TreeSet<>());
            TreeSet<String> copy = new TreeSet<>();
            for (String record : records) copy.add(record);
            actions.put(service, copy);
        }
    }

    public Set<String> getActionsForService(String service) {
        return actions.get(service);
    }

    private void loadDirectConnections() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getInstance());
        Set<String> storedConns = prefs.getStringSet("__connections", new TreeSet<>());

        for (String connection : storedConns) {
            connections.add(connection);
        }
        directConnections.postValue(connections);
    }

    public void saveDirectConnection(String title, String url) {
        if ((title.length() + url.length()) == 0)
            return;

        connections.add(String.format("%s\t%s", title, url));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getInstance());
        prefs.edit().putStringSet("__connections", connections).apply();

        directConnections.postValue(connections);
    }

    public void removeDirectConnection(String content) {
        TreeSet<String> newConnections = new TreeSet();
        for (String connection : connections) {
            if (!connection.equals(content)) {
                newConnections.add(connection);
            }
        }
        connections = newConnections;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getInstance());
        prefs.edit().putStringSet("__connections", connections).apply();

        directConnections.postValue(connections);
    }

    public void toggleBluetoothDiscovery() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (bluetoothDiscoveryIsActive.getValue().booleanValue()) {
            bluetoothAdapter.cancelDiscovery();
            bluetoothDiscoveryStatus.setValue("discovery canceled");
            bluetoothDiscoveryIsActive.setValue(false);
            return;
        }

        bluetoothDiscoveryStatus.setValue("initializing discovery...");
        bluetoothDevices.setValue(new ArrayList<>());

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(new BroadcastReceiver() {

            @SuppressLint("MissingPermission")
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                List<BluetoothDevice> devices = bluetoothDevices.getValue();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (!devices.contains(device)) {
                        devices.add(device);
                    }
                } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(intent.getAction())) {
                    bluetoothDiscoveryStatus.setValue("discovery started");
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
                    bluetoothDiscoveryStatus.setValue("discovery finished");
                    bluetoothDiscoveryIsActive.setValue(false);
                }
                String msg = String.format("%d bluetooth devices found", devices.size());
                bluetoothDiscoveryStatus.setValue(msg);
                bluetoothDevices.postValue(devices);
            }
        }, filter);

        bluetoothAdapter.startDiscovery();
        bluetoothDiscoveryIsActive.setValue(true);
    }

    public short getNetListenPortForInterface(String iface) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getInstance());
        int port = prefs.getInt("__port_for_"+iface, 8082);
        return (short)port;
    }

    public void setNetListenPortForInterface(String iface, short port) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getInstance());
        prefs.edit().putInt("__port_for_"+iface, port).apply();
        localInetInfo.setValue(localInetInfo.getValue());
    }


}
