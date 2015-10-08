package city.happening.happening.Cards.CardAdapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import city.happening.happening.Cards.CardAdapters.HorizontalScroll.HorizontalListView;
import city.happening.happening.HappFromParse;
import city.happening.happening.ImageHelper;
import city.happening.happening.R;

/**
 * Created by Alex on 6/23/2015.
 */
public class CustomArrayAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<HappFromParse> mHappenings = new ArrayList<>();
    private ParseUser mParseUser;
    private Context mContext;
    private Bitmap mBitmap;

    public CustomArrayAdapter(Context context,ArrayList<HappFromParse> events) {
        mInflater = LayoutInflater.from(context);
        mHappenings = events;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mHappenings.size();
    }

    @Override
    public HappFromParse getItem(int position) {
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
        if(convertView == null) {
            v = mInflater.inflate(R.layout.item, parent, false);
            holder = new ViewHolder();
            holder.avatar = (ImageView)v.findViewById(R.id.picture);
            holder.name = (TextView)v.findViewById(R.id.helloText);
            holder.hashTag = (TextView)v.findViewById(R.id.hashTag);
            holder.location = (TextView)v.findViewById(R.id.descText);
            holder.time = (TextView)v.findViewById(R.id.eventTime);
            holder.facebookScroll = (HorizontalListView) v.findViewById(R.id.facebookScroll);
            holder.photoholder = (FrameLayout)v.findViewById(R.id.photoholder);
            v.setTag(holder);
        } else {
            v = convertView;
            holder = (ViewHolder)v.getTag();
        }
        mParseUser = ParseUser.getCurrentUser();
        ArrayList<Map<String,String>> friendsList =getFriendsList();
        Log.d("ArrayAdapter","friends"+friendsList.size());
        HappFromParse happening = getItem(position);
        if (happening!=null){
            holder.avatar.setImageResource(happening.getDrawableResourceId());
            holder.avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if(happening.getImage()!=null){
                Bitmap temp = ImageHelper.getRoundedCornerBitmap(happening.getImage(), 10);
                ImageHelper helper =  new ImageHelper();
                Bitmap gradientMap = helper.addGradient(temp);
                holder.avatar.setImageBitmap(gradientMap);
                // scaleImage(holder.avatar);
            }
            holder.hashTag.setText(happening.getHash());
            holder.name.setText(happening.getTitle());
            holder.location.setText("At " + happening.getLocation());
            Log.d("Adapter", "date" + happening.get("Date").toString());
            Calendar cal = Calendar.getInstance();
            cal.setTime((Date) happening.get("Date"));
            holder.time.setText( " " + String.format("%1$ta %1$tb %1$td at %1$tI:%1$tM %1$Tp", cal));
           // holder.time.setText(happening.get("Date").toString());


            }
            if(mParseUser.get("friends")!=null){
                Log.d("Horz scroll", "" + friendsList.size());
              //  holder.facebookScroll.setAdapter(new FBScrollAdapter(mContext,friendsList));

            }




        return v;
    }

    private class ViewHolder {
        public ImageView avatar;
        public TextView name, location,hashTag,time;
        //public LinearLayout facebookScroll;
        public FrameLayout photoholder;
        public HorizontalListView facebookScroll;

    }

    private ArrayList<Map<String,String>> getFriendsList(){
        ArrayList<Map<String,String>> tempList = new ArrayList<>();
       if (mParseUser.get("friends")!=null){
           tempList =(ArrayList) mParseUser.get("friends");
           Log.d("ArrayAdapter"," "+tempList.size());
       }


        return tempList;
    }




}
