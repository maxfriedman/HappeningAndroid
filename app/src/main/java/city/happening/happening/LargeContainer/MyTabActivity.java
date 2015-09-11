package city.happening.happening.LargeContainer;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import city.happening.happening.R;

/**
 * Created by Alex on 9/8/2015.
 */
public class MyTabActivity extends ActionBarActivity {

    private static final String TAB_1_TAG = "tab_1";
    private static final String TAB_2_TAG = "tab_2";
    private static final String TAB_3_TAG = "tab_3";
    private static final String TAB_4_TAG = "tab_4";

    private FragmentTabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabs);
        InitView();
    }
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

    }

    private void InitView() {
        mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);


        mTabHost.addTab(setIndicator(MyTabActivity.this, mTabHost.newTabSpec(TAB_1_TAG),
                R.drawable.tab_indicator_gen, "Events", R.drawable.cards_unpressed), Tab1Container.class, null);
        mTabHost.addTab(setIndicator(MyTabActivity.this,mTabHost.newTabSpec(TAB_2_TAG),
                R.drawable.tab_indicator_gen,"Activity",R.drawable.heart),Tab2Container.class,null);
        mTabHost.addTab(setIndicator(MyTabActivity.this,mTabHost.newTabSpec(TAB_3_TAG),
                R.drawable.tab_indicator_gen,"Groups",R.drawable.groups_tab),Tab3Container.class,null);
        mTabHost.addTab(setIndicator(MyTabActivity.this,mTabHost.newTabSpec(TAB_4_TAG),
                R.drawable.tab_indicator_gen,"Me",R.drawable.profile_tab),Tab4Container.class,null);


    }

    @Override
    public void onBackPressed() {
        boolean isPopFragment = false;
        String currentTabTag = mTabHost.getCurrentTabTag();

        if (currentTabTag.equals(TAB_1_TAG)) {
            isPopFragment = ((BaseContainerFragment)getSupportFragmentManager().findFragmentByTag(TAB_1_TAG)).popFragment();
        } else if (currentTabTag.equals(TAB_2_TAG)) {
            isPopFragment = ((BaseContainerFragment)getSupportFragmentManager().findFragmentByTag(TAB_2_TAG)).popFragment();
        }
        else if (currentTabTag.equals(TAB_3_TAG)) {
            isPopFragment = ((BaseContainerFragment)getSupportFragmentManager().findFragmentByTag(TAB_3_TAG)).popFragment();
        }else if (currentTabTag.equals(TAB_4_TAG)){
            isPopFragment = ((BaseContainerFragment)getSupportFragmentManager().findFragmentByTag(TAB_4_TAG)).popFragment();
        }

        if (!isPopFragment) {
            finish();
        }
    }

    private TabHost.TabSpec setIndicator(Context ctx, TabHost.TabSpec spec,
                                         int resid, String string, int genresIcon) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.tab_item, null);
        v.setBackgroundResource(resid);
        TextView tv = (TextView)v.findViewById(R.id.txt_tabtxt);
        ImageView img = (ImageView)v.findViewById(R.id.img_tabtxt);

        tv.setText(string);
        img.setBackgroundResource(genresIcon);
        return spec.setIndicator(v);
    }


}