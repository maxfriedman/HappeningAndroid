package city.happening.happening.Layer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import city.happening.happening.Cards.CardFragment;
import city.happening.happening.R;

/**
 * Created by Alex on 8/17/2015.
 */
public class LayerActivity extends FragmentActivity {

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);
        if (fragment == null) {
            fragment = new LayerListFragment();
            manager.beginTransaction().add(R.id.fragmentContainer, fragment).commit();

        }



    }

}
