package city.happening.happening.Cards.CardAdapters.HorizontalScroll;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import city.happening.happening.R;

/**
 * Created by Alex on 9/9/2015.
 */
public class HorizontalScrollAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<Map<String,String>> mHappenings = new ArrayList<>();
    private Context mContext;


    public HorizontalScrollAdapter(Context context,ArrayList<Map<String,String>> events) {
        mInflater = LayoutInflater.from(context);
        mHappenings = events;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mHappenings.size();
    }

    @Override
    public Map<String,String> getItem(int position) {
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
        Map<String,String> friend= getItem(position);
        Log.d("Friendlist","fr"+friend);
        Log.d("Friendlist","fr"+friend.get("id"));
        if(convertView == null) {
            v = mInflater.inflate(R.layout.friend_list_item, parent, false);
            holder = new ViewHolder();
            holder.image = (ProfilePictureView)v.findViewById(R.id.profpic);
            holder.name = (TextView)v.findViewById(R.id.friendname);

            v.setTag(holder);
        } else {
            v = convertView;
            holder = (ViewHolder)v.getTag();
        }
        String tempFBID = friend.get("id");
        Log.d("Friendlist",tempFBID);
        String tempName = friend.get("name");

        holder.image.setProfileId(tempFBID);
        holder.image.setPresetSize(ProfilePictureView.SMALL);
       // holder.name.setText(tempName);


        return  v;
    }

    private class ViewHolder {
        public ProfilePictureView image;
        public TextView name;



    }
}
