package city.happening.happening.Profile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.parse.ParseUser;

import city.happening.happening.R;

/**
 * Created by Alex on 8/31/2015.
 */
public class ProfileFragment extends Fragment {

    private ParseUser mParseUser;

    private ListView mListView;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mParseUser = ParseUser.getCurrentUser();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, parent,false);



       /* ProfilePictureView pictureView =(ProfilePictureView) v.findViewById(R.id.profilePicture);
        TextView userName = (TextView)v.findViewById(R.id.userName);
        TextView userLoc = (TextView) v.findViewById(R.id.userLoc);

        Profile profile = Profile.getCurrentProfile();
        pictureView.setProfileId(profile.getId());
        userName.setText(profile.getName());*/


        Fragment listFragment = new EventsList();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.child_fragment, listFragment).commit();

       return v;
    }

    @Override
    public void onResume(){
        super.onResume();
//        mEventListAdapter.notifyDataSetChanged();

    }


}
