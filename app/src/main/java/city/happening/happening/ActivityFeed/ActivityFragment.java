package city.happening.happening.ActivityFeed;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import city.happening.happening.R;

/**
 * Created by Alex on 9/29/2015.
 */
public class ActivityFragment extends Fragment {

    FragmentTabHost mTabHost;

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        mTabHost = new FragmentTabHost(getActivity());
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent);
        // getActivity().getActionBar().setTitle("TabFragment");

        mTabHost.addTab(
                mTabHost.newTabSpec("Me").setIndicator(
                        "Me"), MeList.class, null);

        mTabHost.addTab(mTabHost.newTabSpec("Friends").setIndicator("Friends"),
                FriendActivityList.class, null);
        return mTabHost;

    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
        mTabHost = null;
    }
}
