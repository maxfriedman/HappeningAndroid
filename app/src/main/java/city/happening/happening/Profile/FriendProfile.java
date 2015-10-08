package city.happening.happening.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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
 * Created by Alex on 10/2/2015.
 */
public class FriendProfile extends Fragment {

    public static final String EXTRA_PROFILE_ID = "happening.PROFILE_ID";
    private ParseQueryAdapter<ParseObject> mEventListAdapter;
    private LayoutInflater mInflater;
    private ParseUser mParseUser;
    private ListView mListView;
    String profileId;

    public static FriendProfile newInstance(String userId) {
        Log.e("FriendProf","Id"+userId);
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_PROFILE_ID, userId);
        FriendProfile fragment = new FriendProfile();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.e("FriendProfile", "Created");
        mInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        profileId = (String)getArguments().getSerializable(EXTRA_PROFILE_ID);

        Log.e("FriendProfile oncreate","Id"+profileId);
        ParseQuery<ParseUser> query= ParseUser.getQuery() ;
        query.whereEqualTo("objectId",profileId);
        try {
            mParseUser = query.getFirst();
            Log.e("FriendProfile","user"+mParseUser);
        }catch (ParseException e){
            Log.e("FriendProfile","exception" +e);
        }


    }



    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        Log.e("EventsList", "oncreateView");
        View v = inflater.inflate(R.layout.fragmentwithlist, container, false);
        mListView =(ListView) v.findViewById(R.id.listView);
        mInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ParseQueryAdapter.QueryFactory<ParseObject> factory = new ParseQueryAdapter.QueryFactory<ParseObject>() {
            public ParseQuery<ParseObject> create() {
                Calendar rightNow = Calendar.getInstance();
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
                rightNow.add(Calendar.MINUTE, -30);
                Date today = new Date(rightNow.getTimeInMillis());
                query.whereGreaterThan("EndTime", today);
                ParseQuery<ParseObject> swipesQuery = ParseQuery.getQuery("Swipes");
                swipesQuery.whereEqualTo("UserID", mParseUser.getObjectId());
                query.whereDoesNotMatchKeyInQuery("objectId","EventId",swipesQuery);
                query.orderByAscending("EndTime");
                query.fromPin("friendPin");
                /*Calendar rightNow = Calendar.getInstance();
                rightNow.add(Calendar.MINUTE, -30);
                Date today = new Date(rightNow.getTimeInMillis());
                ParseQuery<ParseObject> swipesQuery = ParseQuery.getQuery("Swipes");
                swipesQuery.whereEqualTo("UserID", profileId);
                Log.e("Events", "userid " + profileId);
                swipesQuery.whereEqualTo("swipedRight", true);
                swipesQuery.setLimit(1000);

                ParseQuery eventQuery = ParseQuery.getQuery("Event");
                eventQuery.whereMatchesKeyInQuery("objectId", "EventID", swipesQuery);
                eventQuery.whereGreaterThan("EndTime", today);
                eventQuery.orderByAscending("Date");
                eventQuery.setLimit(1000);*/

                return query;
            }
        };
        mEventListAdapter = new EventListAdapter(getActivity(),factory);
        mListView.setAdapter(mEventListAdapter);
        mEventListAdapter.getCount();

        View header = inflater.inflate(R.layout.header,null);
        ProfilePictureView pictureView = (ProfilePictureView)header.findViewById(R.id.profilePicture);
        pictureView.setProfileId((String)mParseUser.get("FBObjectID"));
        pictureView.setPresetSize(ProfilePictureView.LARGE);

        TextView name = (TextView) header.findViewById(R.id.userName);
        name.setText("" + mParseUser.get("firstName") + " " + mParseUser.get("lastName"));
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
        swipesQuery.whereEqualTo("UserID", profileId);
        Log.e("Events", "userid" + profileId);
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
                    Log.e("EventsList", "list" + list.size());
                    for (int i = 0; i < list.size(); i++) {
                        Log.e("Profile", " " + list.get(i));
                        HappFromParse temp = (HappFromParse) list.get(i);
                        temp.setDrawableResourceId(temp.getHash());
                        Log.e("Profile", "title" + temp.getTitle());
                        try {
                            temp.pin("friendpin");
                        }catch (ParseException e2){

                        }

                        //temp.pinInBackground();


                    }
                    mEventListAdapter.loadObjects();
                    mEventListAdapter.notifyDataSetChanged();


                }else {
                    Log.e("EventsList", "error"+e);
                }
            }

        });

    }
    private class EventListAdapter extends ParseQueryAdapter<ParseObject> {
        public EventListAdapter(Context context,ParseQueryAdapter.QueryFactory<ParseObject> queryFactory) {
            super(context, queryFactory);
        }

        @Override
        public ParseObject getItem(int index) {
            return super.getItem(index);
        }

        @Override
        public View getItemView(ParseObject object,View convertView,ViewGroup parent){
            HappFromParse happening = (HappFromParse) object;
            happening.setDrawableResourceId(happening.getHash());
            final HappFromParse tempHapp =(HappFromParse) happening;
            ViewHolder holder;
            if(convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_event,parent,false );
                holder = new ViewHolder();
                holder.avatar = (ImageView)convertView.findViewById(R.id.eventImage);
                holder.name = (TextView)convertView.findViewById(R.id.event_title);
                holder.hashTag = (TextView)convertView.findViewById(R.id.hashTag);
                holder.location = (TextView)convertView.findViewById(R.id.eventLocation);
                holder.time = (TextView)convertView.findViewById(R.id.eventTime);
                holder.item = (LinearLayout)convertView.findViewById(R.id.eventCard);

                // holder.facebookScroll = (LinearLayout) convertView.findViewById(R.id.facebookScroll);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            //  ArrayList<Map<String,String>> friendsList =getFriendsList();
            if (happening!=null){

                holder.avatar.setImageResource(happening.getDrawableResourceId());
                holder.avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
                if(happening.getImage()!=null){
                    //Bitmap temp = ImageHelper.getRoundedCornerBitmap(happening.getImage(),50);
                    holder.avatar.setImageBitmap(happening.getImage());

                }
//            holder.hashTag.setText(happening.getHash());
                holder.name.setText(happening.getTitle());
                holder.location.setText("At " + happening.getLocation());
                if (happening.get("Date")!=null){
                    Calendar cal = Calendar.getInstance();
                    cal.setTime((Date) happening.get("Date"));
                    holder.time.setText( " " + String.format("%1$ta %1$tb %1$td at %1$tI:%1$tM %1$Tp", cal));
                }
                holder.item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HappeningLab.get(getActivity()).addHappening(tempHapp);
                        Intent i = new Intent(getActivity(), EventActivity.class);
                        i.putExtra(EventFragment.EXTRA_EVENT_ID, tempHapp.getObjectId());
                        startActivityForResult(i, 0);
                    }
                });

            }


            return convertView;
        }
    }
    private static class ViewHolder {
        public ImageView avatar;
        public TextView name, location,hashTag,time;
        public LinearLayout item;

    }
}
