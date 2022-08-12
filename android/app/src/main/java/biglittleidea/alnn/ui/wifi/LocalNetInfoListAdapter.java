package biglittleidea.alnn.ui.wifi;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import java.util.List;

import biglittleidea.alnn.App;
import biglittleidea.alnn.LocalInetInfo;
import biglittleidea.alnn.R;

public class LocalNetInfoListAdapter extends BaseAdapter {
    LayoutInflater inflter;
    List<LocalInetInfo> list;
    Activity activity;
    LifecycleOwner lifecycleOwner;
    public LocalNetInfoListAdapter(Activity activity, LifecycleOwner lifecycleOwner, List<LocalInetInfo> list) {
        this.list = list;
        this.activity = activity;
        this.lifecycleOwner = lifecycleOwner;
        inflter = (LayoutInflater.from(activity));
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
        App app = App.getInstance();
        short port = app.getNetListenPortForInterface(info.inetAddress.toString());
        TextView portText = view.findViewById(R.id.portView);
        portText.setText(String.format("port %d", port));

        portText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeSetListPortDialog(info.inetAddress.toString()).show();
            }
        });

        Switch toggle = view.findViewById(R.id.listen_switch);
        toggle.setChecked(app.isListeningToUDP(info.bcastAddress, port));
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                short port = app.getNetListenPortForInterface(info.inetAddress.toString());
                app.listenToUDP(info.bcastAddress, port, isChecked);
            }
        });

        return view;
    }


    Dialog makeSetListPortDialog(String iface) {
        final Dialog dialog = new Dialog(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.set_listen_port_dialog, null);
        dialog.setContentView(view);
        dialog.setTitle("Configuration");

        App app = App.getInstance();
        short port = app.getNetListenPortForInterface(iface);

        EditText portNumEdit =  view.findViewById(R.id.portNumEdit);
        portNumEdit.setText(String.format("%d", port));

        view.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        view.findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String portText = portNumEdit.getText().toString();
                short port = Short.parseShort(portText);
                App.getInstance().setNetListenPortForInterface(iface, port);

                dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                App.getInstance().qrDialogLabel.postValue("");
                App.getInstance().qrScanResult.postValue("");
            }
        });
        return dialog;
    }
}
