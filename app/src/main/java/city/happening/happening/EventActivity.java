package city.happening.happening;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;

/**
 * Created by Alex on 6/24/2015.
 */
public class EventActivity extends FragmentActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);
        if (fragment == null) {
            String eventId = (String)getIntent().getSerializableExtra(EventFragment.EXTRA_EVENT_ID);
            boolean eventBool = (boolean)getIntent().getSerializableExtra(EventFragment.EXTRA_EVENT_BOOL);
            fragment =EventFragment.newInstance(eventId,eventBool);
            manager.beginTransaction().add(R.id.fragmentContainer, fragment).commit();

        }

    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu resource
        getMenuInflater().inflate(R.menu.menu_discover, menu);



        // Get the ViewPager's current item position and set its ShareIntent.
        // int currentViewPagerItem = ((ViewPager) findViewById(R.id.viewpager)).getCurrentItem();

        return super.onCreateOptionsMenu(menu);
    }
}
