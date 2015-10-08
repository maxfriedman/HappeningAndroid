package city.happening.happening.ActivityFeed;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import city.happening.happening.R;

/**
 * Created by Alex on 9/8/2015.
 */
public class InTheWorks extends Fragment {

    /*
    * ACitvity Folder
    * activity table
    *
    *
    *
    * */


    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.notreadyyet,parent,false);

        return v;
    }
}
