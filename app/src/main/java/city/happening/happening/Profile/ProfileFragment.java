package city.happening.happening.Profile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.facebook.login.widget.ProfilePictureView;

import city.happening.happening.R;

/**
 * Created by Alex on 8/31/2015.
 */
public class ProfileFragment extends Fragment {

    private String mParseID, mFaceBookId;
    public static final String EXTRA_PROFILE_ID = "happening.PROFILE_ID";
    public static final String EXTRA_PROFILE_ID_FB = "happening.PROFILE_ID_FB";
    private Button mFriendsButton, mEventsButton;
    boolean eventsIsCurr ,friendsIsCurr;
    FragmentManager manager;

    public static ProfileFragment newInstance(String userId, String idFB){
        Log.e("ProfileFrag", "Id Parse" + userId);
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_PROFILE_ID, userId);
        args.putSerializable(EXTRA_PROFILE_ID_FB, idFB);
        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);

        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
       // setRetainInstance(true);
        mParseID = (String)getArguments().getSerializable(EXTRA_PROFILE_ID);
        mFaceBookId = (String)getArguments().getSerializable(EXTRA_PROFILE_ID_FB);
       /* if (mParseID==null||mFaceBookId==null){
            mParseID = ParseUser.getCurrentUser().getObjectId();
            mFaceBookId = Profile.getCurrentProfile().getId();
            Log.e("ProfFragment","PID "+mParseID+" FBID "+mFaceBookId);
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        ProfilePictureView pictureView =(ProfilePictureView) v.findViewById(R.id.userPicture);
        pictureView.setProfileId(mFaceBookId);

        manager = getChildFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);
        if (fragment == null) {
            String profileId =mParseID; //(String)getActivity().getIntent().getSerializableExtra(EventsList.EXTRA_PROFILE_ID);
            String fbid = mFaceBookId;//(String)getActivity().getIntent().getSerializableExtra(EventsList.EXTRA_PROFILE_ID_FB);
            fragment = new EventsList().newInstance(profileId,fbid);
            manager.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
        }
        mEventsButton = (Button)v.findViewById(R.id.EventsButton);
        mFriendsButton = (Button)v.findViewById(R.id.FriendsButton);

        mEventsButton.setText("Events");
        mFriendsButton.setText("Friends");
        eventsIsCurr = true;
        friendsIsCurr =false;


        mEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                friendsIsCurr = false;
                eventsIsCurr = true;

                String profileId = mParseID;//(String) getActivity().getIntent().getSerializableExtra(EventsList.EXTRA_PROFILE_ID);
                String fbid = mFaceBookId;//(String) getActivity().getIntent().getSerializableExtra(EventsList.EXTRA_PROFILE_ID_FB);

               /* if (profileId==null ||fbid==null){
                    profileId = mParseID;
                    fbid = mFaceBookId;
                }*/
                Fragment fragment = new EventsList().newInstance(profileId, fbid);

                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack so the user can navigate back
                transaction.replace(R.id.fragmentContainer, fragment);
                transaction.addToBackStack(null);
                // Commit the transaction
                transaction.commit();

            }
        });

        mFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendsIsCurr = true;
                eventsIsCurr = false;

                String profileId = mParseID;//(String) getActivity().getIntent().getSerializableExtra(FriendsList.EXTRA_PROFILE_ID);
                String fbid = mFaceBookId;//(String) getActivity().getIntent().getSerializableExtra(FriendsList.EXTRA_PROFILE_ID_FB);
               /* if (profileId==null ||fbid==null){
                    profileId = mParseID;
                    fbid = mFaceBookId;
                }*/
                Fragment fragment = new FriendsList().newInstance(profileId, fbid);
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack so the user can navigate back
                transaction.replace(R.id.fragmentContainer,fragment);
                transaction.addToBackStack(null);
                // Commit the transaction
                transaction.commit();



            }
        });


        return v;

    }

        @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();

    }




}
