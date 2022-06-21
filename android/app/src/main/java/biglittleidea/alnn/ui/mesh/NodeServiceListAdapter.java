package biglittleidea.alnn.ui.mesh;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import biglittleidea.aln.Router;
import biglittleidea.alnn.App;
import biglittleidea.alnn.LocalInetInfo;
import biglittleidea.alnn.R;

public class NodeServiceListAdapter extends BaseAdapter {
    LayoutInflater inflter;
    List<Router.ServiceListItem> list = new ArrayList<>();
    App app;
    public NodeServiceListAdapter(App app, Set<Router.ServiceListItem> services) {
        this.app = app;
        for (Router.ServiceListItem item : services)
            this.list.add(item);
        inflter = (LayoutInflater.from(app));
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Router.ServiceListItem getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        Router.ServiceListItem info = list.get(position);
        view = inflter.inflate(R.layout.mesh_node_item, null);
//        ImageView icon = (ImageView) view.findViewById(R.id.icon);
//        icon.setImageResource(flags[i]);
        TextView text = (TextView) view.findViewById(R.id.textView);
        text.setText(String.format("%s",
            info.service
        ));
        return view;
    }
}
