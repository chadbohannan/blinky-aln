package biglittleidea.alnn.ui.wifi;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import biglittleidea.alnn.App;

public class WifiViewModel extends AndroidViewModel {

//    private final MutableLiveData<String> mText;
    private App app;

    public WifiViewModel(App app) {
        super(app);
        this.app = app;
//        mText = new MutableLiveData<>();
//        mText.setValue("WIFI TODO");
    }

    public LiveData<String> getText() {
        return app.msg;
    }
}