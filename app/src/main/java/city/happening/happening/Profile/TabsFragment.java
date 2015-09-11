package city.happening.happening.Profile;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;

import city.happening.happening.R;

/**
 * Created by Alex on 9/9/2015.
 */
public class TabsFragment extends Fragment implements TabHost.OnTabChangeListener {

    private static final String TAG = "FragmentTabs";
    public static final String TAB_WORDS = "words";
    public static final String TAB_NUMBERS = "numbers";

    private View mRoot;
    private TabHost mTabHost;
    private int mCurrentTab;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.tabs_fragment, null);
        mTabHost = (TabHost) mRoot.findViewById(android.R.id.tabhost);
        setupTabs();
        return mRoot;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);

        mTabHost.setOnTabChangedListener(this);
        mTabHost.setCurrentTab(mCurrentTab);
        // manually start loading stuff in the first tab
        updateTab(TAB_WORDS, R.id.tab_1);
    }

    private void setupTabs() {
        mTabHost.setup(); // you must call this before adding your tabs!
        mTabHost.addTab(newTab(TAB_WORDS, "Tab Words", R.id.tab_1));
        mTabHost.addTab(newTab(TAB_NUMBERS, "Tab Numbers", R.id.tab_2));
    }

    private TabHost.TabSpec newTab(String tag, String labelId, int tabContentId) {
        Log.d(TAG, "buildTab(): tag=" + tag);

        View indicator = LayoutInflater.from(getActivity()).inflate(
                R.layout.tabs_fragment,
                (ViewGroup) mRoot.findViewById(android.R.id.tabs), false);
        ((TextView) indicator.findViewById(R.id.text)).setText(labelId);

        TabHost.TabSpec tabSpec = mTabHost.newTabSpec(tag);
        tabSpec.setIndicator(indicator);
        tabSpec.setContent(tabContentId);
        return tabSpec;
    }

    @Override
    public void onTabChanged(String tabId) {
        Log.d(TAG, "onTabChanged(): tabId=" + tabId);
        if (TAB_WORDS.equals(tabId)) {
            updateTab(tabId, R.id.tab_1);
            mCurrentTab = 0;
            return;
        }
        if (TAB_NUMBERS.equals(tabId)) {
            updateTab(tabId, R.id.tab_2);
            mCurrentTab = 1;
            return;
        }
    }

    private void updateTab(String tabId, int placeholder) {
        FragmentManager fm = getChildFragmentManager();
        if (fm.findFragmentByTag(tabId) == null) {
        //    fm.beginTransaction().replace(placeholder, new EventsList()).commit();
        }
    }

}
