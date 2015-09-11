package city.happening.happening.Layer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.layer.sdk.LayerClient;

import city.happening.happening.R;



/**
 * Created by Alex on 8/17/2015.
 */
public class LayerListFragment extends Fragment {
    static public String AppID = "";
    static public LayerClient sLayerClient;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_layerlist, parent, true);
        return v;

    }
}
