package city.happening.happening.Profile;

import android.content.Context;
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

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import city.happening.happening.FBScrollAdapter;
import city.happening.happening.HappFromParse;
import city.happening.happening.HorizontalListView;
import city.happening.happening.R;

/**
 * Created by Alex on 9/3/2015.
 */
public class EventsList extends Fragment {

    private ParseQueryAdapter<ParseObject> mEventListAdapter;
    private LayoutInflater mInflater;
    private ParseUser mParseUser;
    private ListView mListView;
    ArrayList<Map<String,String>> mFriends;

    private String mParseID, mFaceBookId;
    public static final String EXTRA_PROFILE_ID = "happening.PROFILE_ID";
    public static final String EXTRA_PROFILE_ID_FB = "happening.PROFILE_ID_FB";

    public static EventsList newInstance(String userId, String idFB){
        Log.e("EventsList", "Id " + userId);
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_PROFILE_ID, userId);
        args.putSerializable(EXTRA_PROFILE_ID_FB, idFB);
        EventsList fragment = new EventsList();
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

        /*if (mFaceBookId==null){

            mParseID = ParseUser.getCurrentUser().getObjectId();
            mFaceBookId = Profile.getCurrentProfile().getId();
        }*/

        Log.e("Eventslist oncreate", "Id " + mParseID);

        ParseQuery<ParseUser> query= ParseUser.getQuery() ;
        query.whereEqualTo("objectId", mParseID);
        try {
            mParseUser = query.getFirst();
            Log.e("FriendProfile","user"+mParseUser);
            mFriends =(ArrayList<Map<String,String>>) mParseUser.get("friends");
            Log.e("EventsLst","FriendList "+mFriends.size());
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
               /* Calendar rightNow = Calendar.getInstance();
                ParseQuery<HappFromParse> query = HappFromParse.getQuery();
                rightNow.add(Calendar.MINUTE, -30);
                Date today = new Date(rightNow.getTimeInMillis());
                query.whereGreaterThan("EndTime", today);
                query.orderByAscending("EndTime");
                query.fromPin("EventsList");
                return query;*/
                Calendar rightNow = Calendar.getInstance();
                Date today = new Date(rightNow.getTimeInMillis());
                ParseQuery<ParseObject> swipesQuery = ParseQuery.getQuery("Swipes");
                swipesQuery.whereEqualTo("UserID",mParseID);
                Log.e("Events", "userid" + mParseID);
                swipesQuery.whereEqualTo("swipedRight", true);
                //swipesQuery.fromLocalDatastore();
                swipesQuery.setLimit(1000);

                ParseQuery eventQuery = ParseQuery.getQuery("Event");
                // eventQuery.fromLocalDatastore();
                eventQuery.whereMatchesKeyInQuery("objectId", "EventID", swipesQuery);
                eventQuery.whereGreaterThan("EndTime", today);
                eventQuery.orderByAscending("Date");
                eventQuery.setLimit(1000);
                eventQuery.orderByDescending("EndTime");
                return eventQuery;
            }
        };
        mEventListAdapter = new EventListAdapter(getActivity(),factory);
        mListView.setAdapter(mEventListAdapter);
        mEventListAdapter.getCount();

        /*View header = inflater.inflate(R.layout.header,null);
        ProfilePictureView pictureView = (ProfilePictureView)header.findViewById(R.id.profilePicture);
        pictureView.setProfileId(Profile.getCurrentProfile().getId());
        pictureView.setPresetSize(ProfilePictureView.LARGE);

        TextView name = (TextView) header.findViewById(R.id.userName);
        name.setText(Profile.getCurrentProfile().getName());
        mListView.addHeaderView(header);*/
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

        //HappFromParse.unpinAll();
        Calendar rightNow = Calendar.getInstance();
        Date today = new Date(rightNow.getTimeInMillis());
        ParseQuery<ParseObject> swipesQuery = ParseQuery.getQuery("Swipes");
        swipesQuery.whereEqualTo("UserID",mParseID);
        Log.e("Events", "userid" + mParseID);
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
                        temp.pinInBackground("EventsList");

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
        public View getItemView(ParseObject parseObject,View convertView,ViewGroup parent){
            HappFromParse happening =(HappFromParse) parseObject;
            happening.setDrawableResourceId(happening.getHash());
            final HappFromParse tempHapp = happening;
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

                holder.facebookscroll = (HorizontalListView) convertView.findViewById(R.id.facebookScroll);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
           // mParseUser = ParseUser.getCurrentUser();

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
                mFriends =(ArrayList<Map<String,String>>) mParseUser.get("friends");

                holder.facebookscroll.setAdapter(new FBScrollAdapter(getContext(),mFriends));


            }


            return convertView;
        }
    }
    private static class ViewHolder {
        public ImageView avatar;
        public TextView name, location,hashTag,time;
        public LinearLayout item;
        public HorizontalListView facebookscroll;

    }
}
