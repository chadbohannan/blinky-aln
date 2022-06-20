package biglittleidea.alnn.ui.wifi;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import biglittleidea.alnn.App;
import biglittleidea.alnn.BeaconInfo;
import biglittleidea.alnn.LocalInetInfo;
import biglittleidea.alnn.R;

public class BeaconInfoListAdapter extends BaseAdapter {
    LayoutInflater inflter;
    List<BeaconInfo> list;
    App app;
    public BeaconInfoListAdapter(App app, List<BeaconInfo> list) {
        this.app = app;
        this.list = list;
        inflter = (LayoutInflater.from(app));
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public BeaconInfo getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).host.hashCode();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        BeaconInfo info = list.get(position);
        view = inflter.inflate(R.layout.wifi_beacon_item, null);
//        ImageView icon = (ImageView) view.findViewById(R.id.icon);
//        icon.setImageResource(flags[i]);
        TextView protocolText = (TextView) view.findViewById(R.id.protocolTextView);
        protocolText.setText(info.protocol);

        TextView addressText = (TextView) view.findViewById(R.id.addressTextView);
        if (info.path.length() > 0){
            addressText.setText(String.format("%s:%d\n%s", info.host, info.port, info.path));
        } else {
            addressText.setText(String.format("%s:%d", info.host, info.port));
        }

        Switch toggle = (Switch) view.findViewById(R.id.connect_switch);
        toggle.setChecked(app.isConnected(info.protocol, info.host, info.port));
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String err = app.connectTo(info, isChecked);
                if (err != null) {
                    Log.d("ALNN", "on connect err:" + err);
                }
            }
        });

        return view;
    }
}
