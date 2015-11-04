package city.happening.happening;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.parse.CountCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.usebutton.sdk.ButtonDropin;
import com.usebutton.sdk.PlacementContext;
import com.usebutton.sdk.util.LocationProvider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import city.happening.happening.Profile.ProfileActivity;
import city.happening.happening.Profile.ProfileFragment;


public class EventFragment extends Fragment {


    public static final String EXTRA_EVENT_ID = "happening.EVENT_ID";
    public static final String EXTRA_EVENT_BOOL = "happening.EVENT_BOOL";
    private static final String TAG = "EventFragment";

    TextView  mTitle;
    TextView mDesc;
    ImageView mPic;
    HappFromParse mEvent;
    ScrollView mLinearLayout;
    private boolean isGoing;
    private GoogleMap mMap;
    android.widget.Button mInterested;
    android.widget.Button mGoing;
    android.widget.Button mNotInterested;
    private  boolean mNeedsButtons;
    HorizontalListView mInterestedParties;
    private ParseUser mParseUser;
    private Button mTicketButton;
    ParseQuery<ParseObject> swipesQuery;
    int startPriceNumLabel;
    //String eventID = null;

    public static EventFragment newInstance(String eventId,boolean eventBool){
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_EVENT_ID, eventId);
        args.putSerializable(EXTRA_EVENT_BOOL, eventBool);
        EventFragment fragment = new EventFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mNeedsButtons = false;
        String eventId = (String)getArguments().getSerializable(EXTRA_EVENT_ID);
        mNeedsButtons = (boolean)getArguments().getSerializable(EXTRA_EVENT_BOOL);
        mEvent = HappeningLab.get(getActivity()).getHappening(eventId) ;
        mEvent.setDrawableResourceId(mEvent.getHash());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_event,parent,false);
        Typeface face=Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/OpenSansRegular.ttf");
        //mEvent.setDrawableResourceId(mEvent.getHash());
        mInterestedParties = (HorizontalListView)v.findViewById(R.id.friendScroll);
        mPic = (ImageView)v.findViewById(R.id.eventImage);
        mPic.setImageResource(mEvent.getDrawableResourceId());
        if(mEvent.getImage()!=null) mPic.setImageBitmap(mEvent.getImage());
        mTitle =(TextView) v.findViewById(R.id.title_event);
        mTitle.setTypeface(face);
        mTitle.setText(mEvent.getTitle());
        mDesc =( TextView) v.findViewById(R.id.description_event);
        mDesc.setTypeface(face);
        mDesc.setText(mEvent.getDescription());
        mDesc.setMovementMethod(ScrollingMovementMethod.getInstance());
        mGoing = (android.widget.Button)v.findViewById(R.id.goingButton);
        mNotInterested =(android.widget.Button)v.findViewById(R.id.notInterestedButton);
        mInterested = (android.widget.Button)v.findViewById(R.id.interestedButton);
        mTicketButton = (Button)v.findViewById(R.id.ticketButton);
        if (mNeedsButtons==false){
            mInterested.setVisibility(View.GONE);
            mGoing.setVisibility(View.GONE);
            mNotInterested.setVisibility(View.GONE);
        }else {
            mInterested.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRight(mEvent);


                }
            });
            mNotInterested.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  onLeft(mEvent);

                }
            });
            mGoing.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isGoing = true;
                    onRight(mEvent);
                }
            });
        }

        mLinearLayout = (ScrollView)v.findViewById(R.id.containerEventFrag);
        mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        ArrayList<String> interestedFBiDs = new ArrayList<>();
        ParseQuery swipes = ParseQuery.getQuery("Swipes");
        swipes.whereEqualTo("EventID", mEvent.getObjectId());
        try {
            ArrayList<ParseObject> tempSwipeList =(ArrayList<ParseObject>) swipes.find();
            for (int i=0;i<tempSwipeList.size();i++){
                if (tempSwipeList.get(i).get("FBObjectID")!=null)interestedFBiDs.add((String)tempSwipeList.get(i).get("FBObjectID"));
            }
        }catch (ParseException e){}
        mInterestedParties.setAdapter(new FaceBookScrollAdapter(getActivity(), interestedFBiDs));

        ParseGeoPoint location = mEvent.getGeo();
        Log.d("EventFragment",""+location);

        LatLng coordinates = new LatLng(location.getLatitude(),location.getLongitude());


        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());

        if (status == ConnectionResult.SUCCESS) {
            {
                FragmentManager fm = getChildFragmentManager();
                MyMapFragment mMapFragment = MyMapFragment.newInstance(coordinates);
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.add(R.id.my_map_fragment, mMapFragment);
                fragmentTransaction.commit();
                fm.executePendingTransactions();
            }
        }


        final ButtonDropin buttonDropin = (ButtonDropin)v.findViewById(R.id.main_dropin);
        final PlacementContext context = PlacementContext.forEndLocation(mEvent.getLocation(), coordinates.latitude, coordinates.longitude);

        // To quickly add from as the user's current position, add these lines (this will give price estimates):

        final Location bestLocation = new LocationProvider(getActivity()).getBestLocation();
        if (bestLocation != null) {
            context.withStartLocation(null, bestLocation);
        }

        buttonDropin.prepareForDisplayWithContext(context, new ButtonDropin.Listener() {
            @Override
            public void onPrepared(boolean b) {
                Log.d("EventFrag","Bool "+b);
            }
        });




        final String ticketLink = (String)mEvent.get("TicketLink");
        String url = (String)mEvent.get("URL");
        if (mEvent.get("lowest_price")!=null){
            startPriceNumLabel = 0;
            startPriceNumLabel =(int) mEvent.get("lowest_price");
        }

        if (ticketLink!=null&&(!ticketLink.equals("")||!ticketLink.equals("$0"))){
            if (ticketLink.contains("seatgeek.com")){
                if (startPriceNumLabel>=0){
                    String startingString = "GetTickets - Starting at "+startPriceNumLabel ;
                    mTicketButton.setText(startingString);

                }
            } else if (ticketLink.contains("facebook.com")){
                mTicketButton.setText("RSVP TO FACEBOOK EVENT");

            }else if (ticketLink.contains("meetup.com")){
                mTicketButton.setText("RSVP ON MEETUP.COM");

            }else if(((boolean)mEvent.get("isFreeEvent"))){
                mTicketButton.setText("THIS EVENT IS FREE");

            }
        }else if (url!=null &&(!url.equals("")||(!url.equals("$0")))){
            mTicketButton.setText("MORE INFO ON WEBSITE");
        }else {
            mTicketButton.setVisibility(View.GONE);
        }

        mTicketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ticketLink!=null){
                    Intent i = new Intent(getActivity(), WebActivity.class);
                    i.putExtra(WebActivity.EXTRA_URL_ID,ticketLink);
                    startActivity(i );
                }
            }
        });



         return v;


    }
    static void makeToast(Context ctx, String s){
        Toast.makeText(ctx, s, Toast.LENGTH_SHORT).show();
    }
    private void onRight(Object eventObjects){
        HappFromParse temp = (HappFromParse) eventObjects;
        final HappFromParse tempHappFinal = temp;
        Log.e(TAG, "" + temp.getTitle());

        mParseUser = ParseUser.getCurrentUser();
        final String tempId = temp.getObjectId();
        temp.pinInBackground();
        temp.increment("swipesRight");
        temp.saveEventually();


        final String tag = temp.getHash();
        if (tag.equals("Happy Hour")) {
            mParseUser.increment("HappyHour");
        } else {
            mParseUser.increment(tag);
        }

        if (isGoing)mParseUser.increment("score",3);
        else mParseUser.increment("score",1);

        mParseUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d(TAG, "user saved"); //no error
                } else {
                    Log.e(TAG, "" + e);
                }
            }
        });

        Map<String, String> params = new HashMap<String, String>();
        params.put("userID", mParseUser.getObjectId());
        params.put("eventID", temp.getObjectId());
        params.put("swiped", "right");
        ParseCloud.callFunctionInBackground("swipeAnalytics", params);

        swipesQuery = ParseQuery.getQuery("Swipes");
        swipesQuery.whereEqualTo("EventID", tempId);
        swipesQuery.whereEqualTo("UserID", mParseUser.getObjectId());


        swipesQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (object!=null){
                    boolean going = (boolean)object.get("isGoing");
                    isGoing = going;
                }else {
                    isGoing = false;
                }
                if (e == null) {
                    object.put("swipedAgain", true);
                    object.put("swipedLeft", false);
                    object.put("swipedRight", true);
                    object.put("isGoing", true);
                    //  object.put("friendCount",);
                    ParseQuery timelineQuery = ParseQuery.getQuery("Timeline");
                    timelineQuery.fromLocalDatastore();
                    timelineQuery.whereEqualTo("userId", mParseUser.getObjectId());
                    timelineQuery.whereEqualTo("eventId", tempId);
                    timelineQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject object, ParseException e) {
                            if (e==null){
                                if (isGoing) object.put("type", "going");
                                else object.put("type", "swipeRight");
                                object.saveEventually();
                            }else{
                                Log.e(TAG,"error "+e);
                            }
                        }

                    });
                    ParseQuery activityQuery = ParseQuery.getQuery("Timeline");
                    activityQuery.fromLocalDatastore();
                    activityQuery.whereEqualTo("userParseId", mParseUser.getObjectId());
                    activityQuery.whereEqualTo("eventId", tempId);
                    activityQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject object, ParseException e) {
                            if (e == null) {
                                if (isGoing) object.put("type", "going");
                                else object.put("type", "swipeRight");
                                object.saveEventually();
                            }else {
                                Log.e(TAG,"error "+e);
                            }
                        }

                    });
                    object.saveEventually();
                } else {
                    ParseObject timelineObject = ParseObject.create("Timeline");
                    if (isGoing) timelineObject.put("type", "going");
                    else timelineObject.put("type", "swipedRight");

                    timelineObject.put("userId", mParseUser.getObjectId());
                    timelineObject.put("eventId", tempId);
                    Calendar cal = Calendar.getInstance();
                    Date date = new Date(cal.getTimeInMillis());
                    timelineObject.put("createdDate", date);
                    Log.e(TAG,"TempFinal "+tempHappFinal.getTitle());
                    timelineObject.put("eventTitle", tempHappFinal.getTitle());
                    timelineObject.pinInBackground();
                    timelineObject.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e==null){
                                Log.e(TAG,"saved timeline");
                            }else{
                                Log.e(TAG,"saved timeline error"+e);
                            }
                        }
                    });

                    mParseUser.increment("eventCount", 1);
                    mParseUser.saveEventually();

                    ParseObject swipeObject = ParseObject.create("Swipes");
                    swipeObject.put("UserID", mParseUser.getObjectId());
                    swipeObject.put("EventID", tempId);
                    swipeObject.put("swipedRight", true);
                    swipeObject.put("swipedLeft", false);
                    swipeObject.put("isGoing", isGoing);
                    //swipeObject.put("friendCount");

                    swipeObject.put("FBObjectID", mParseUser.get("FBObjectID"));
                    final ParseObject finalswipeObject = swipeObject;
                    swipeObject.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            finalswipeObject.pinInBackground();
                            if (e == null) {
                                String privacyString = "";
                                if (tempHappFinal.get("privacy") != null)
                                    privacyString = (String) tempHappFinal.get("privacy");

                                if (!privacyString.equals("private")) {
                                    String locString = (String) tempHappFinal.getLocation();
                                    String name = (String) mParseUser.get("firstName") + " " + mParseUser.get("lastName");
                                    Map<String, String> parameters = new HashMap<String, String>();
                                    parameters.put("user", mParseUser.getObjectId());
                                    parameters.put("event", tempId);
                                    parameters.put("fbID", (String) mParseUser.get("FBObjectID"));
                                    ParseCloud.callFunctionInBackground("swipeRight", parameters);
                                }

                            }

                        }
                    });

                }
            }
        });

    }

    private void onLeft(Object eventObjects){
        //removeFirstObjectInAdapter();
        HappFromParse temp = (HappFromParse) eventObjects;
        Log.e(TAG, "" + temp.getTitle());

        mParseUser = ParseUser.getCurrentUser();
        final String tempId = temp.getObjectId();

        temp.unpinInBackground();
        temp.increment("swipesLeft");
        temp.saveEventually();


        Log.d(TAG, "" + ParseUser.getCurrentUser());
        Map<String, String> params = new HashMap<String, String>();
        params.put("userID", mParseUser.getObjectId());
        params.put("eventID", temp.getObjectId());
        params.put("swiped", "left");
        ParseCloud.callFunctionInBackground("swipeAnalytics", params);


        swipesQuery = ParseQuery.getQuery("Swipes");
        swipesQuery.whereEqualTo("EventID", tempId);
        swipesQuery.whereEqualTo("UserID", mParseUser.getObjectId());

        ParseObject swipesObject = ParseObject.create("Swipes");

        swipesQuery.countInBackground(new CountCallback() {
            @Override
            public void done(int i, ParseException e) {

                if (i > 0) {
                    swipesQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject object, ParseException e) {
                            if (object.get("isGoing")!=null){
                                boolean going = (boolean)object.get("isGoing");
                                isGoing = going;
                            }else {
                                isGoing = false;
                            }
                            object.put("swipedAgain", true);
                            object.put("swipedRight", false);
                            object.put("swipedLeft", true);

                            ParseQuery timeLineQuery = ParseQuery.getQuery("Timeline");
                            timeLineQuery.fromLocalDatastore();
                            timeLineQuery.whereEqualTo("userId", mParseUser.getObjectId());
                            timeLineQuery.whereEqualTo("eventId", tempId);
                            timeLineQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject parseObject, ParseException e) {
                                    if (e == null) {
                                        parseObject.unpinInBackground();
                                        parseObject.deleteEventually();
                                    }
                                }
                            });
                            ParseQuery activityQuery = ParseQuery.getQuery("Timeline");
                            //activityQuery.fromLocalDatastore();
                            activityQuery.whereEqualTo("userParseId", mParseUser.getObjectId());
                            activityQuery.whereEqualTo("eventId", tempId);
                            activityQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject parseObject, ParseException e) {
                                    if (e == null) {
                                        parseObject.unpinInBackground();
                                        parseObject.deleteEventually();
                                    }
                                }
                            });

                            object.saveInBackground();
                            object.unpinInBackground();
                        }
                    });
                } else {
                    ParseObject swipesObject = ParseObject.create("Swipes");
                    swipesObject.put("UserID", mParseUser.getObjectId());
                    swipesObject.put("EventID", tempId);
                    swipesObject.put("swipedRight", false);
                    swipesObject.put("swipedLeft", true);
                    swipesObject.put("swipedAgain", true);
                    swipesObject.put("FBObjectID", mParseUser.get("FBObjectID"));
                    swipesObject.saveInBackground();
                }
            }
        });

    }


    public class FaceBookScrollAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<String> mHappenings = new ArrayList<>();
        private Context mContext;


        public FaceBookScrollAdapter(Context context,ArrayList<String> events) {
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
            Typeface face=Typeface.createFromAsset(mContext.getAssets(),
                    "fonts/OpenSansRegular.ttf");
            String parseId = null;
            try {
                ParseUser temp = (ParseUser)friendUser.getFirst();
                parseId =(String) temp.getObjectId();
                holder.name.setText((String)temp.get("firstName"));
                holder.name.setTextColor(getResources().getColor(R.color.black));
                holder.name.setTypeface(face);
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

}
