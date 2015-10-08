package city.happening.happening.ActivityFeed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;
import com.parse.ParseObject;

import java.util.ArrayList;

import city.happening.happening.R;

/**
 * Created by Alex on 9/30/2015.
 */
public class FriendActivityAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    public ArrayList<ParseObject> mActivities;
    Context mContext;
    public FriendActivityAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mActivities = new ArrayList<>();
        mContext = context;
    }

    @Override
    public int getCount() {
        return mActivities.size();
    }

    @Override
    public ParseObject getItem(int position) {
        return mActivities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        ViewHolder holder;
        final int tempPosition = position;
        if(convertView == null) {
            v = mInflater.inflate(R.layout.list_item_activity_friend, parent, false);
            holder = new ViewHolder();
            holder.name =(TextView) v.findViewById(R.id.userText);
            holder.picture =(ImageView) v.findViewById(R.id.activityImage);
            holder.date = (TextView)v.findViewById(R.id.activityDate);
            holder.title = (TextView)v.findViewById(R.id.activityTitle);
            holder.userPicture = (ProfilePictureView)v.findViewById(R.id.userImage);
            // holder.container = (LinearLayout)v.findViewById(R.id.cityContainer);
            v.setTag(holder);
        } else {
            v = convertView;
            holder = (ViewHolder)v.getTag();
        }
        holder.name.setText("");


        return v;
    }
    public void upDateEntries(ArrayList<ParseObject> activities) {
        mActivities.addAll(activities);
        notifyDataSetChanged();
    }

    private class ViewHolder {
        ProfilePictureView userPicture;
        ImageView picture;
        TextView name,date,title;
        LinearLayout container;
    }


}
