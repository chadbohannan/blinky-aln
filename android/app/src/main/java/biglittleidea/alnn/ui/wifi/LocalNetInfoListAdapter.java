package biglittleidea.alnn.ui.wifi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
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
//        ImageView icon = (ImageView) view.findViewById(R.id.icon);
//        icon.setImageResource(flags[i]);
        TextView text = (TextView)           view.findViewById(R.id.textView);
        text.setText(String.format("%s\n%s\n%s",
            info.name,
            info.inetAddress.toString(),
            info.bcastAddress.toString()
        ));

        Switch toggle = (Switch) view.findViewById(R.id.listen_switch);
        short port = 8282; // TODO expose parameter to UI
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
