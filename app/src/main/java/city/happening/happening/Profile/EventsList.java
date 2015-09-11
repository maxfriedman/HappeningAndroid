package city.happening.happening.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.Profile;
import com.facebook.login.widget.ProfilePictureView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import city.happening.happening.EventActivity;
import city.happening.happening.EventFragment;
import city.happening.happening.HappFromParse;
import city.happening.happening.HappeningLab;
import city.happening.happening.R;

/**
 * Created by Alex on 9/3/2015.
 */
public class EventsList extends Fragment {

    private ParseQueryAdapter<HappFromParse> mEventListAdapter;
    private LayoutInflater mInflater;
    private ParseUser mParseUser;
    private ListView mListView;

    public static EventsList newInstance() {

        EventsList f = new EventsList();
        Bundle b = new Bundle();
        f.setArguments(b);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.e("EventsList", "Created");
        mInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mParseUser = ParseUser.getCurrentUser();

    }


    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        Log.e("EventsList", "oncreateView");
        View v = inflater.inflate(R.layout.fragmentwithlist, container, false);
        mListView =(ListView) v.findViewById(R.id.listView);
        mInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ParseQueryAdapter.QueryFactory<HappFromParse> factory = new ParseQueryAdapter.QueryFactory<HappFromParse>() {
            public ParseQuery<HappFromParse> create() {
                Calendar rightNow = Calendar.getInstance();
                ParseQuery<HappFromParse> query = HappFromParse.getQuery();
                rightNow.add(Calendar.MINUTE, -30);
                Date today = new Date(rightNow.getTimeInMillis());
                query.whereGreaterThan("EndTime", today);
                query.orderByAscending("EndTime");
                query.fromLocalDatastore();
                return query;
            }
        };
        mEventListAdapter = new EventListAdapter(getActivity(),factory);
        mListView.setAdapter(mEventListAdapter);
        mEventListAdapter.getCount();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HappFromParse happFromParse = (HappFromParse) mEventListAdapter.getItem(position);
                happFromParse.setDrawableResourceId(happFromParse.getHash());
                HappeningLab.get(getActivity()).addHappening(happFromParse);
                Intent i = new Intent(getActivity(), EventActivity.class);
                i.putExtra(EventFragment.EXTRA_EVENT_ID, happFromParse.getObjectId());
                startActivityForResult(i, 0);
            }
        });
        View header = inflater.inflate(R.layout.header,null);
        ProfilePictureView pictureView = (ProfilePictureView)header.findViewById(R.id.profilePicture);
        pictureView.setProfileId(Profile.getCurrentProfile().getId());
        pictureView.setPresetSize(ProfilePictureView.LARGE);

        TextView name = (TextView) header.findViewById(R.id.userName);
        name.setText(Profile.getCurrentProfile().getName());
        mListView.addHeaderView(header);
        loadFromParse();


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        //mEventListAdapter.notifyDataSetChanged();
        mListView.invalidateViews();
    }

    private void loadFromParse(){
        Log.e("EventsList", "LoadfromParse");
        Calendar rightNow = Calendar.getInstance();
        Date today = new Date(rightNow.getTimeInMillis());
        ParseQuery<ParseObject> swipesQuery = ParseQuery.getQuery("Swipes");
        swipesQuery.whereEqualTo("UserID", ParseUser.getCurrentUser().getObjectId());
        Log.e("Events", "userid" + ParseUser.getCurrentUser().getObjectId());
        swipesQuery.whereEqualTo("swipedRight", true);
        //swipesQuery.fromLocalDatastore();
        swipesQuery.setLimit(1000);

        ParseQuery eventQuery = ParseQuery.getQuery("Event");
       // eventQuery.fromLocalDatastore();
        eventQuery.whereMatchesKeyInQuery("objectId", "EventID", swipesQuery);
        eventQuery.whereGreaterThan("EndTime", today);
        eventQuery.orderByAscending("Date");
        eventQuery.setLimit(1000);

        eventQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List list, ParseException e) {
                if(e==null){
                    Log.e("EventsList", "list"+list.size());
                    for (int i = 0; i < list.size(); i++) {
                        Log.e("Profile", " " + list.get(i));
                        HappFromParse temp = (HappFromParse) list.get(i);
                        temp.setDrawableResourceId(temp.getHash());
                        temp.pinInBackground();

                    }
                    mEventListAdapter.loadObjects();
                    mEventListAdapter.notifyDataSetChanged();


                }else {
                    Log.e("EventsList", "error"+e);
                }
            }

        });

    }
    private class EventListAdapter extends ParseQueryAdapter<HappFromParse> {
        public EventListAdapter(Context context,ParseQueryAdapter.QueryFactory<HappFromParse> queryFactory) {
            super(context, queryFactory);
        }

        @Override
        public HappFromParse getItem(int index) {
            return super.getItem(index);
        }

        @Override
        public View getItemView(HappFromParse happening,View convertView,ViewGroup parent){
            ViewHolder holder;
            if(convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_event,parent,false );
                holder = new ViewHolder();
                holder.avatar = (ImageView)convertView.findViewById(R.id.eventImage);
                holder.name = (TextView)convertView.findViewById(R.id.event_title);
                holder.hashTag = (TextView)convertView.findViewById(R.id.hashTag);
                holder.location = (TextView)convertView.findViewById(R.id.eventLocation);
                holder.time = (TextView)convertView.findViewById(R.id.eventTime);
                // holder.facebookScroll = (LinearLayout) convertView.findViewById(R.id.facebookScroll);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            mParseUser = ParseUser.getCurrentUser();
            //  ArrayList<Map<String,String>> friendsList =getFriendsList();

            holder.avatar.setImageResource(happening.getDrawableResourceId());
            holder.avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if(happening.getImage()!=null){
                holder.avatar.setImageBitmap(happening.getImage());
                // scaleImage(holder.avatar);
            }
//            holder.hashTag.setText(happening.getHash());
            holder.name.setText(happening.getTitle());
            holder.location.setText("At " + happening.getLocation());
            if (happening.get("Date")!=null){
                Calendar cal = Calendar.getInstance();
                cal.setTime((Date) happening.get("Date"));
                holder.time.setText( " " + String.format("%1$ta %1$tb %1$td at %1$tI:%1$tM %1$Tp", cal));
            }


            return convertView;
        }
    }
    private static class ViewHolder {
        public ImageView avatar;
        public TextView name, location,hashTag,time;
        public LinearLayout facebookScroll;

    }
}
