package city.happening.happening.Profile;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentManager;


import city.happening.happening.R;

/**
 * Created by Alex on 7/15/2015.
 */
public class ProfileActivity extends Activity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager manager = getFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);
        if (fragment == null) {
         //   fragment = new ProfileFragment();
         //   manager.beginTransaction().add(R.id.fragmentContainer, fragment).commit();


        }
    }
}