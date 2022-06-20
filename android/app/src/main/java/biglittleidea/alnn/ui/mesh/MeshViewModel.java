package biglittleidea.alnn.ui.mesh;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MeshViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public MeshViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("MESH TODO");
    }

    public LiveData<String> getText() {
        return mText;
    }
}