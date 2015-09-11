package city.happening.happening.LargeContainer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import city.happening.happening.Cards.CardFragment;
import city.happening.happening.R;

/**
 * Created by Alex on 6/17/2015.
 */
public class HappeningMain extends FragmentActivity{


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);
        if (fragment == null) {
            fragment = new CardFragment();
            manager.beginTransaction().add(R.id.fragmentContainer, fragment).commit();

        }


    }





}



