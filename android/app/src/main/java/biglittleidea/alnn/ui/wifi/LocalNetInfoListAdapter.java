package biglittleidea.alnn.ui.wifi;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import biglittleidea.alnn.App;
import biglittleidea.alnn.LocalInetInfo;
import biglittleidea.alnn.R;

public class LocalNetInfoListAdapter extends BaseAdapter {
    LayoutInflater inflter;
    List<LocalInetInfo> list;
    App app;
    public LocalNetInfoListAdapter(App app, List<LocalInetInfo> list) {
        this.app = app;
        this.list = list;
        inflter = (LayoutInflater.from(app));
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public LocalInetInfo getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).name.hashCode();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LocalInetInfo info = list.get(position);
        view = inflter.inflate(R.layout.wifi_bcast_item, null);
        ImageView icon = view.findViewById(R.id.icon);
        icon.setImageResource(R.drawable.network_card);
        TextView text = view.findViewById(R.id.textView);
        text.setText(String.format("%s\n%s\n%s",
            info.name,
            info.inetAddress.toString(),
            info.bcastAddress.toString()
        ));
        short port = 8082; // TODO expose parameter to UI

        TextView portText = view.findViewById(R.id.portView);
        portText.setText(String.format("port %d", port));

        Switch toggle = view.findViewById(R.id.listen_switch);
        toggle.setChecked(app.isListeningToUDP(info.bcastAddress, port));
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                app.listenToUDP(info.bcastAddress, port, isChecked);
            }
        });

        return view;
    }
}
