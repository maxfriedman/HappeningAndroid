package city.happening.happening.Profile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import city.happening.happening.R;

/**
 * Created by Alex on 7/15/2015.
 */
public class ProfileActivity extends FragmentActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);
        if (fragment == null) {
            String profileID = (String)getIntent().getSerializableExtra(ProfileFragment.EXTRA_PROFILE_ID);
            String idfb = (String)getIntent().getSerializableExtra(ProfileFragment.EXTRA_PROFILE_ID_FB);

            fragment = new ProfileFragment().newInstance(profileID,idfb);
            manager.beginTransaction().add(R.id.fragmentContainer, fragment).commit();


        }
    }
}