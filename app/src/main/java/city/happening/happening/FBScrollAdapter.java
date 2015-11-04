package city.happening.happening;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import city.happening.happening.Profile.ProfileActivity;
import city.happening.happening.Profile.ProfileFragment;

/**
 * Created by Alex on 10/7/2015.
 */
public class FBScrollAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<String> mHappenings = new ArrayList<>();
    private Context mContext;


    public FBScrollAdapter(Context context,ArrayList<String> events) {
        mInflater = LayoutInflater.from(context);
        mHappenings = events;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mHappenings.size();
    }

    @Override
    public String getItem(int position) {
        return mHappenings.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        ViewHolder holder;
        final String friendID = getItem(position);


        if(convertView == null) {
            v = mInflater.inflate(R.layout.list_item_friend_scroll, parent, false);
            holder = new ViewHolder();
            holder.image = (ProfilePictureView)v.findViewById(R.id.friendPic);
            holder.name = (TextView)v.findViewById(R.id.friendname);

            v.setTag(holder);
        } else {
            v = convertView;
            holder = (ViewHolder)v.getTag();
        }
        Log.d("Friendlist", "");


        holder.image.setProfileId(friendID);
        holder.image.setPresetSize(ProfilePictureView.SMALL);
        ParseQuery friendUser = ParseUser.getQuery();
        friendUser.whereEqualTo("FBObjectID", friendID);
        String parseId = null;
        try {
            ParseUser temp = (ParseUser)friendUser.getFirst();
            parseId =(String) temp.getObjectId();
            holder.name.setText((String)temp.get("firstName"));
        }catch (ParseException e){

        }
        final String parseID =parseId;
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(mContext,ProfileActivity.class);
                //Log.e("FriendsList","PArseID"+parseID);
                if (parseID != null) i.putExtra(ProfileFragment.EXTRA_PROFILE_ID, parseID);
                i.putExtra(ProfileFragment.EXTRA_PROFILE_ID_FB, friendID);
                mContext.startActivity(i);

            }
        });




        return  v;
    }

    private class ViewHolder {
        public ProfilePictureView image;
        public TextView name;





    }
}
