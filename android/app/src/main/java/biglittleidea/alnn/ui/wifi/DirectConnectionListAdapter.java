package biglittleidea.alnn.ui.wifi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.util.Log;
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

import biglittleidea.alnn.App;
import biglittleidea.alnn.R;

public class DirectConnectionListAdapter extends BaseAdapter {

    LayoutInflater inflter;
    Activity activity;
    DirectConnectionItem[] connectionList;
    LifecycleOwner lifecycleOwner;

    Dialog dialog;

    public DirectConnectionListAdapter(Activity activity, DirectConnectionItem[] connectionList, LifecycleOwner lifecycleOwner) {
        inflter = (LayoutInflater.from(activity));
        this.activity = activity;
        this.connectionList = connectionList;
        this.lifecycleOwner = lifecycleOwner;
        dialog = makeAddConnectionDialog();
    }

    @Override
    public int getCount() {
        return connectionList.length  + 1;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view  = inflter.inflate(R.layout.saved_connection_item, null);
        ImageView icon = view.findViewById(R.id.icon);
        TextView titleText = view.findViewById(R.id.titleTextView);
        TextView protocolText = view.findViewById(R.id.protocolTextView);
        TextView addressText = view.findViewById(R.id.addressTextView);
        Switch connectSwitch = view.findViewById(R.id.connect_switch);

        if (position < connectionList.length) {
            final DirectConnectionItem item = connectionList[position];
            icon.setImageResource(R.drawable.server_host);
            connectSwitch.setVisibility(View.VISIBLE);
            titleText.setText(item.title);
            protocolText.setText(item.protocol);
            addressText.setText(String.format("%s:%d\n%s", item.host, item.port, item.node));

            App app = App.getInstance();
            connectSwitch.setChecked(app.isConnected(item.protocol, item.host, item.port));
            connectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String err = app.connectTo(item.protocol, item.host, item.port, item.node, isChecked);
                    if (err != null) {
                        Log.d("ALNN", "on connect err:" + err);
                    }
                }
            });
            app.numActiveConnections.observe(lifecycleOwner, new Observer<Integer>() {
                @Override
                public void onChanged(Integer integer) {
                    connectSwitch.setChecked(app.isConnected(item.protocol, item.host, item.port));
                }
            });

        } else {
            titleText.setText("");
            protocolText.setText("Add New Remote Connection");
            addressText.setText("");
            icon.setImageResource(R.drawable.icons8_plus);
            connectSwitch.setVisibility(View.INVISIBLE);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.show();
                }
            });
        }
        return view;
    }

    Dialog makeAddConnectionDialog() {
        final Dialog dialog = new Dialog(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.add_connection_dialog, null);
        dialog.setContentView(view);
        dialog.setTitle("Add Connection");

        App app = App.getInstance();
        EditText labelEdit =  view.findViewById(R.id.titleEdit);
        labelEdit.setText(app.qrDialogLabel.getValue());
        app.qrDialogLabel.observe(lifecycleOwner, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                EditText labelEdit =  view.findViewById(R.id.titleEdit);
                labelEdit.setText(s);
            }
        });

        EditText urlEdit =  view.findViewById(R.id.contentEdit);
        urlEdit.setText(app.qrScanResult.getValue());
        app.qrScanResult.observe(lifecycleOwner, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                EditText urlEdit =  view.findViewById(R.id.contentEdit);
                urlEdit.setText(s);
            }
        });

        ImageView qrButton = view.findViewById(R.id.scan_qr_button);
        qrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText labelEdit =  view.findViewById(R.id.titleEdit);
                App.getInstance().qrDialogLabel.postValue(labelEdit.getText().toString());
                try {
                    Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                    intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes
                    activity.startActivityForResult(intent, 0);
                } catch (Exception e) {
                    Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
                    Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
                    activity.startActivity(marketIntent);
                }
            }
        });
        view.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        view.findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText titleEdit = view.findViewById(R.id.titleEdit);
                EditText contentEdit = view.findViewById(R.id.contentEdit);

                String title = titleEdit.getText().toString();
                String content = contentEdit.getText().toString();

                App.getInstance().saveDirectConnection(title, content);
                titleEdit.setText("");
                contentEdit.setText("");

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
