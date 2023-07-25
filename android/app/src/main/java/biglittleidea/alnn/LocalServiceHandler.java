package biglittleidea.alnn;

import android.util.Pair;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import biglittleidea.aln.IPacketHandler;
import biglittleidea.aln.Packet;

public class LocalServiceHandler implements IPacketHandler {
    public String service;
    public List<Pair<Date,Packet>> packets = new ArrayList<>();
    public final MutableLiveData<List<Pair<Date,Packet>>> mdlPackets = new MutableLiveData<>();

    public LocalServiceHandler(String service) {
        this.service = service;
    }

    @Override
    public void onPacket(Packet p) {
        packets.add(new Pair<>(new Date(), p));
        mdlPackets.setValue(packets);
    }
}
