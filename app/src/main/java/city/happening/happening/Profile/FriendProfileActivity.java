package city.happening.happening.Profile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;

import city.happening.happening.R;

/**
 * Created by Alex on 10/2/2015.
 */
public class FriendProfileActivity extends FragmentActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);
        if (fragment == null) {
            String eventId = (String)getIntent().getSerializableExtra(FriendProfile.EXTRA_PROFILE_ID);
            Log.e("FriendProfileActivity","Id"+eventId);
            fragment =FriendProfile.newInstance(eventId);
            manager.beginTransaction().add(R.id.fragmentContainer, fragment).commit();

        }

    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu resource
        getMenuInflater().inflate(R.menu.menu_discover, menu);

        return super.onCreateOptionsMenu(menu);
    }
}
