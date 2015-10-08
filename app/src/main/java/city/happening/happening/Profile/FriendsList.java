package city.happening.happening.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import city.happening.happening.R;

/**
 * Created by Alex on 9/3/2015.
 */
public class FriendsList extends Fragment {

    private FrListAdapter mAdapter;
    private LayoutInflater mInflater;
    private ParseUser mParseUser;
    private ListView mListView;
    private ArrayList<Map<String,String>> mFriends;


    private String mParseID, mFaceBookId;
    public static final String EXTRA_PROFILE_ID = "happening.PROFILE_ID";
    public static final String EXTRA_PROFILE_ID_FB = "happening.PROFILE_ID_FB";

    public static FriendsList newInstance(String userId, String idFB){
        Log.e("FriendsList", "Id " + userId);
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_PROFILE_ID, userId);
        args.putSerializable(EXTRA_PROFILE_ID_FB, idFB);
        FriendsList fragment = new FriendsList();
        fragment.setArguments(args);

        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.e("EventsList", "Created");
        mInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mParseID = (String)getArguments().getSerializable(EXTRA_PROFILE_ID);
        mFaceBookId = (String)getArguments().getSerializable(EXTRA_PROFILE_ID_FB);
        if (mParseID==null){
            mParseID = ParseUser.getCurrentUser().getObjectId();
        }
        Log.e("FriendProfile oncreate","Id"+mParseID);
        ParseQuery<ParseUser> query= ParseUser.getQuery() ;
        query.whereEqualTo("objectId",mParseID);
        try {
            mParseUser = query.getFirst();
            Log.e("FriendProfile","user"+mParseUser);
        }catch (ParseException e){
            Log.e("FriendProfile","exception" +e);
        }


    }



    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragmentwithlist,container,false);
        mListView = (ListView) v.findViewById(R.id.listView);
        mFriends = new ArrayList<>();
        ArrayList<Map<String,String>>tempList =(ArrayList<Map<String,String>>)mParseUser.get("friends");
        ArrayList<String> names = new ArrayList<>();
        for (Map<String,String>user:tempList){
            names.add(user.get("name"));
        }
        Collections.sort(names);
        Log.e("EventFriend",""+names);
        for (String name: names){
            for (int i = 0;i<tempList.size();i++){
                if (name==tempList.get(i).get("name")){
                    mFriends.add(tempList.get(i));
                }
            }
        }


        Log.e("EventFriend List",""+mFriends);

        mAdapter = new FrListAdapter(getActivity(),mFriends);
        Log.e("EventFriend List",""+mAdapter);
        mListView.setAdapter(mAdapter);


        return v;
    }




    private class FrListAdapter extends BaseAdapter {
        List<Map<String,String>> mFriends;
        Context mContext;

        public FrListAdapter(Context context,ArrayList<Map<String,String>>friends) {
            mFriends = friends;
            mContext =context;
            mInflater = LayoutInflater.from(context);
        }


        @Override
        public int getCount() {
            return mFriends.size();
        }

        @Override
        public Map<String,String> getItem(int position) {
            return mFriends.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            ViewHolder holder;
            if(convertView == null) {
                v = mInflater.inflate(R.layout.friend_list_item, parent, false);
                holder = new ViewHolder();
                holder.name =(TextView) v.findViewById(R.id.friendname);
                holder.avatar =(ProfilePictureView) v.findViewById(R.id.profpic);
                holder.container = (LinearLayout) v.findViewById(R.id.friendContainer);
                v.setTag(holder);
            } else {
                v = convertView;
                holder = (ViewHolder)v.getTag();
            }
            holder.name.setText("");
            final String fbID =(String) mFriends.get(position).get("id");
            final String parseID =(String) mFriends.get(position).get("parseId");
            String name = (String)mFriends.get(position).get("name");

            holder.avatar.setProfileId(fbID);
            holder.avatar.setPresetSize(ProfilePictureView.SMALL);
            holder.name.setText(name);
            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getActivity(),ProfileActivity.class);
                    Log.e("FriendsList","PArseID"+parseID);
                    i.putExtra(ProfileFragment.EXTRA_PROFILE_ID, parseID);
                    i.putExtra(ProfileFragment.EXTRA_PROFILE_ID_FB,fbID);
                    startActivityForResult(i,0);
                }
            });





            return v;
        }
    }
    private static class ViewHolder {
        public ProfilePictureView avatar;
        public TextView name;
        public LinearLayout container;

    }


}
