package city.happening.happening.LargeContainer;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import city.happening.happening.Profile.ProfileFragment;
import city.happening.happening.R;

/**
 * Created by Alex on 9/8/2015.
 */
public class Tab4Container extends BaseContainerFragment {

    private boolean IsViewInited;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e("Container", "Tab4");
        return inflater.inflate(R.layout.container_fragment, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!IsViewInited) {
            IsViewInited = true;
            initView();
        }
    }

    private void initView() {
        replaceFragment(new ProfileFragment(), false);
    }

}


