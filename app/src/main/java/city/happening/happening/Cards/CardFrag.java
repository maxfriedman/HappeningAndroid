package city.happening.happening.Cards;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import city.happening.happening.Cards.CardAdapters.CustomArrayAdapter;
import city.happening.happening.Cards.CardAdapters.SwipeFlingAdapterView;
import city.happening.happening.EventActivity;
import city.happening.happening.EventFragment;
import city.happening.happening.HappFromParse;
import city.happening.happening.HappeningLab;
import city.happening.happening.ProgressDialogFragment;
import city.happening.happening.R;

/**
 * Created by Alex on 9/28/2015.
 */
public class CardFrag extends Fragment {
    private static final String TAG = "CardFrag";
    public static final int BUTTON_CLICK_REQUEST = 1;

    private DialogFragment mDialog;
    private ArrayList<HappFromParse> mHappenings;
    private CustomArrayAdapter arrayAdapter;
    private UserLocManager mLocManager;
    private Location mLastLocation;

    private int i,j;
    Boolean shouldLimit,createViewRan;
    Boolean isGoing;

    ParseQuery<ParseObject> swipesQuery;
    ParseUser mParseUser;
    CardBackground mCardBackground;
    @InjectView(R.id.frame)
    SwipeFlingAdapterView flingContainer;
    FrameLayout cardContainer;
    int count;
    int swipe;

    private BroadcastReceiver mLocationReceiver = new LocationReceiver() {
        @Override
        protected void onLocationReceived(Context context, Location loc) {
            mLastLocation = loc;
            mCardBackground.mLastLocation =loc;
            ParseGeoPoint tempGeo = new ParseGeoPoint(loc.getLatitude(),loc.getLongitude());
            mParseUser.put("userLoc",tempGeo);
            mParseUser.saveEventually();
        }

        @Override
        protected void onProviderEnabledChanged(boolean enabled) {
            //   int toastText = enabled ? R.string.gps_enabled : R.string.gps_disabled;
            //   Toast.makeText(getActivity(), toastText, Toast.LENGTH_LONG).show();
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mLocManager =UserLocManager.get(getActivity());
        mLocManager.startLocationUpdates();
        mCardBackground = new CardBackground(getActivity(),mLastLocation);
        mParseUser = ParseUser.getCurrentUser();
        swipe =0;
        count = 0;
        createViewRan = false;
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        String time =(String) mParseUser.get("time");
        if (time!=null){
            if (time.equals("today")||time.equals("tomorrow")|time.equals("this weekend")){
                shouldLimit=true;
            }else {
                shouldLimit = false;
            }
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().registerReceiver(mLocationReceiver, new IntentFilter(UserLocManager.ACTION_LOCATION));
    }

    @Override
    public void onStop() {
        getActivity().unregisterReceiver(mLocationReceiver);
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mParseUser = ParseUser.getCurrentUser();
        Log.e(TAG, "onResume" + mHappenings.size());
        setHasOptionsMenu(true);
        mLocManager.startLocationUpdates();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
    }

    @Override
    public void onPause() {
        super.onPause();
        mLocManager.stopLocationUpdates();
        createViewRan = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup parent, Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView");
        setHasOptionsMenu(true);
        createViewRan = true;
        View v = inflater.inflate(R.layout.fragment_card, parent, false);
        cardContainer =(FrameLayout) v.findViewById(R.id.cardContainer);
        mHappenings = new ArrayList();

        Log.e(TAG, "newCards");
        flingContainer = (SwipeFlingAdapterView) v.findViewById(R.id.frame);
        ButterKnife.inject(getActivity());

        if (mHappenings.size()==0)getQuery(shouldLimit);
        arrayAdapter = new CustomArrayAdapter(getActivity(),mHappenings,getActivity());
        arrayAdapter.notifyDataSetChanged();
        flingContainer.setAdapter(arrayAdapter);
        flingContainer.setMinStackInAdapter(0);


        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                Log.d("LIST", "removed object!");
                mHappenings.remove(0);
                arrayAdapter.notifyDataSetChanged();
                Log.e(TAG, "remove" + mHappenings.size());
            }

            @Override
            public void onLeftCardExit(Object eventObjects) {
                onLeft(eventObjects);
            }

            @Override
            public void onRightCardExit(Object eventObjects) {
                onRight(eventObjects);
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Ask for more data here
                //newCards();
                getQuery(shouldLimit);
                count++;
                i++;
                Log.e(TAG, "empty" + mHappenings.size());
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
                View view = flingContainer.getSelectedView();
                View right = view.findViewById(R.id.item_swipe_right_indicator);
                right.setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                View left = view.findViewById(R.id.item_swipe_left_indicator);
                left.setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
                TextView rightText =(TextView) view.findViewById(R.id.swiperighttext);
                TextView leftText =(TextView) view.findViewById(R.id.swipelefttext);
                leftText.setText(getNegative());
                rightText.setText(getPositive());
                rightText.setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                leftText.setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);

            }
        });
        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {

                HappFromParse h = (HappFromParse) dataObject;
                HappeningLab.get(getActivity()).addHappening(h);
                Intent i = new Intent(getActivity(), EventActivity.class);
                i.putExtra(EventFragment.EXTRA_EVENT_ID, h.getObjectId());
                i.putExtra(EventFragment.EXTRA_EVENT_BOOL, false);
                startActivityForResult(i, BUTTON_CLICK_REQUEST);
            }
        });



        return v;
    }
    private View loadingAdapter(Context c){
        View view = new View(c);
        view.setBackground(getResources().getDrawable(R.drawable.customprogress));

        return view;
    }

    private String getPositive(){
        switch (swipe) {
            case 0:
                swipe++;
                return "Yeah!";
            case 1:
                swipe++;
                return "Like";
            case 2:
                swipe++;
                return "I'm in";
            default:
                if (swipe == 3) swipe = 0;
                return "Let's DO IT";
        }
    }
    private String getNegative(){
        switch (swipe) {
            case 0:
                swipe++;
                return "Eh";
            case 1:
                swipe++;
                return "Nope";
            case 2:
                swipe++;
                return "Not For Me";
            default:
                if (swipe > 3) swipe = 0;
                return "Forget About It";
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check which request we're responding to
        if (requestCode == BUTTON_CLICK_REQUEST) {
            // Make sure the request was successful
            String message = data.getStringExtra("MESSAGE");
            if (message.equals("right")){
                flingContainer.getTopCardListener().selectRight();
                Log.e(TAG,"Went right");
            }else if (message.equals("left")){
                flingContainer.getTopCardListener().selectLeft();
                Log.e(TAG, "Went left" );
            }else if (message.equals("down")){

            }

        }

    }

    public void newCards() {
        Log.e(TAG, "newCards");
         getQuery(shouldLimit);
        /*newCardsQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> eventObjects, ParseException e) {
                if (e == null) {
                    ArrayList<HappFromParse> tempList = new ArrayList<HappFromParse>();
                    Log.e(TAG, "newCardsQuery" + eventObjects.size());
                    if (eventObjects.size()==0&&shouldLimit==true){
                        shouldLimit=false;
                        ParseQuery newCards2 = mCardBackground.getQuery(10,getActivity(),shouldLimit);
                        newCards2.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List list, ParseException e) {
                                if (e==null){
                                    if (list.size()!=0){
                                        ArrayList<HappFromParse> tempList = new ArrayList<HappFromParse>();
                                        for (int i = 0; i < list.size(); i++) {
                                            HappFromParse temp = (HappFromParse) list.get(i);
                                            temp.setDrawableResourceId(temp.getHash());
                                            Log.d(TAG, "Title " + temp.getTitle() + "Date " + temp.getEndTime().toString());
                                            tempList.add(temp);
                                        }setHappenings(tempList);
                                        //mDialog.dismiss();
                                    }else {Log.e(TAG,"No events to be found");}}}

                        });
                    }else {
                        for (int i = 0; i < eventObjects.size(); i++) {
                            HappFromParse temp = (HappFromParse) eventObjects.get(i);
                            temp.setDrawableResourceId(temp.getHash());
                            Log.d(TAG, "Title " + temp.getTitle() + "Date " + temp.getEndTime().toString());
                            tempList.add(temp);
                        }
                        //   mDialog.dismiss();
                        setHappenings(tempList);
                    }
                } else {}
            }
        });*/
    }

    public void setHappenings(ArrayList<HappFromParse> happenings) {
        if (mHappenings.size()<10){
            mHappenings.addAll(happenings);
            arrayAdapter.notifyDataSetChanged();
            // mDialog.dismiss();
        }

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

        /*if (isGoing)mParseUser.increment("score",3);
        else mParseUser.increment("score",1);*/

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

                    if (shouldLimit) {
                        swipeObject.put("swipedAgain", true);
                    }
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
    public void getQuery( boolean limit){
        shouldLimit = limit;

        Log.e(TAG,"getQuery");
        ParseQuery eventQuery;
        ParseQuery finalQuery;
        Calendar rightNow = Calendar.getInstance();
        Date today = new Date(rightNow.getTimeInMillis());
        mParseUser = ParseUser.getCurrentUser();

        eventQuery = ParseQuery.getQuery("Event");

        //sort the query by categories chosen in settings.. Default = All cats on first launch
        ArrayList<String> categories = (ArrayList<String>) mParseUser.get("categories");
        categories.add(null);
        eventQuery.whereContainedIn("Hashtag", categories);

        //        shouldLimit != where boolean for no more events is true

        if(mParseUser.get("time").equals("today")){

            eventQuery.whereGreaterThan("EndTime",today);//Need to create a now minus from now
            if(shouldLimit){
                long longAdd= 86400000-rightNow.getTimeInMillis();
                int add = (int)longAdd;
                rightNow.add(Calendar.MILLISECOND, add);
                Date  endDayz = new Date(rightNow.getTimeInMillis());
                eventQuery.whereLessThan("Date",endDayz);//need to create an end of day variable
            }
        }else if(mParseUser.get("time").equals("tomorrow")){
            long startOfDay = 86400000 - rightNow.getTimeInMillis();
            int addAmt = (int) startOfDay;
            rightNow.add(Calendar.MILLISECOND, -addAmt);
            Date tomorrowDate =new Date(rightNow.getTimeInMillis());
            eventQuery.whereGreaterThan("EndTime",tomorrowDate);//this needs work

            if(shouldLimit){
                //  eventQuery.whereLessThan("Date",TomorrowDate+endOfDay);//need to create an end of day variable
            }
        }else{
            Calendar nextWeekDate = Calendar.getInstance();
            Calendar sundayDate = Calendar.getInstance();
            Calendar saturdayDate = Calendar.getInstance();
            while (sundayDate.get(Calendar.DAY_OF_WEEK)!=1){
                sundayDate.add(Calendar.DAY_OF_WEEK,1);
            }
            while (nextWeekDate.get(Calendar.DAY_OF_WEEK)!=2){
                nextWeekDate.add(Calendar.DAY_OF_WEEK,1);
            }
            while (saturdayDate.get(Calendar.DAY_OF_WEEK)!=7){
                saturdayDate.add(Calendar.DAY_OF_WEEK,1);
            }

            if (today.getDay() == sundayDate.get(Calendar.DAY_OF_WEEK) ) {//Selected weekend on a sunday
                eventQuery.whereGreaterThan("EndTime", today);//all sundays events that have at least 30 mins left
                if (shouldLimit) {
                    Calendar endDay = Calendar.getInstance();
                    long endDayLong= 86400000-endDay.getTimeInMillis();
                    int adding = (int)endDayLong;
                    endDay.add(Calendar.MILLISECOND, adding);
                    Date endOfDay =new Date(endDay.getTimeInMillis());
                    eventQuery.whereLessThan("Date",endOfDay);
                }
            } else if (today.getDay()==saturdayDate.get(Calendar.DAY_OF_WEEK)){
                eventQuery.whereGreaterThan("EndTime", today);//all that have at least 30 mins left
                if (shouldLimit) {
                    eventQuery.whereLessThan("Date", sundayDate);
                }
            }else{
                eventQuery.whereGreaterThan("EndTime", today);//All weekend events
                if (shouldLimit) {
                    eventQuery.whereLessThan("Date", sundayDate);
                }
            }
        }



        ParseQuery<ParseObject> weightedQuery = ParseQuery.getQuery("Event");
        weightedQuery.whereGreaterThan("globalWeight", -1);
        weightedQuery.whereGreaterThan("EndTime", today);
        weightedQuery.whereEqualTo("privacy", "public");//look for the privacy strings line 194-212 pull from github

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(weightedQuery);
        queries.add(eventQuery);
        finalQuery = ParseQuery.or(queries);


        ParseGeoPoint userLocation =(ParseGeoPoint) mParseUser.get("userLoc");
        int radius = mParseUser.getInt("radius");

        ParseQuery<ParseObject> mySwipesQuery = ParseQuery.getQuery("Swipes");

        if(shouldLimit&& mParseUser.get("time").equals("today")){
            mySwipesQuery.setLimit(1000);
            mySwipesQuery.whereEqualTo("UserID", mParseUser.getObjectId());
            mySwipesQuery.whereEqualTo("swipedAgain", true);
            Calendar calendar =Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -28);
            Date thirtyAgo = new Date(calendar.getTimeInMillis());
            mySwipesQuery.whereGreaterThan("createdAt",thirtyAgo);
            finalQuery.whereDoesNotMatchKeyInQuery("objectId","EventID",mySwipesQuery);
        }else{
            Calendar calendar2 =Calendar.getInstance();
            calendar2.add(Calendar.DAY_OF_MONTH, -28);
            Date twentyEight = new Date(calendar2.getTimeInMillis());
            mySwipesQuery.setLimit(1000);
            mySwipesQuery.whereEqualTo("UserID", mParseUser.getObjectId());
            mySwipesQuery.whereGreaterThan("createdAt", twentyEight);
            finalQuery.whereDoesNotMatchKeyInQuery("objectId","EventID",mySwipesQuery);
        }

        String selectedCity =(String) mParseUser.get("userLocTitle");
        //LatLng citLoc =new LatLng(38.907192,-77.036871);
        LatLng citLoc = new LatLng(0,0);
        Location cityloc = new Location("");
        if (selectedCity.equals("Washington, DC")){
            cityloc.setLatitude(38.907192);
            cityloc.setLongitude(-77.036871);
            //citLoc= new LatLng(38.907192,-77.036871);
        }else if (selectedCity.equals("Boston, MA")) {
            cityloc.setLatitude(42.358431);
            cityloc.setLongitude(-71.059773);
            //citLoc = new LatLng(42.358431, -71.059773);
        }else if (selectedCity.equals("Nashville, TN")){
            cityloc.setLatitude(36.162664);
            cityloc.setLongitude(-86.781602);
          //  citLoc = new LatLng(36.162664,-86.781602);
        }else if (selectedCity.equals("Philadelphia, PA")){
            cityloc.setLatitude(39.952584);
            cityloc.setLongitude(-75.165222);
         //   citLoc = new LatLng(39.952584,-75.165222);
        }else if (selectedCity.equals("San Francisco, CA")) {
            cityloc.setLatitude(37.774929);
            cityloc.setLongitude(-122.419416);
         //   citLoc = new LatLng(37.774929, -122.419416);
        }

        Location theCityLoc = cityloc;
        /*theCityLoc.setLongitude(citLoc.longitude);
        theCityLoc.setLatitude(citLoc.latitude);*/
        Location theUserLoc = new Location("");
        float distance;
        if(userLocation!=null){

            theUserLoc.setLongitude(userLocation.getLongitude());
            theUserLoc.setLatitude(userLocation.getLatitude());
            // Log.e(TAG,"Location"+mLastLocation.getLongitude());
            distance =theUserLoc.distanceTo(theCityLoc);
        }else {
            theUserLoc.setLatitude(38.907192);
            theUserLoc.setLongitude(-77.036871);
            distance =theUserLoc.distanceTo(theCityLoc);
        }
        LatLng finalLoc;
        if (distance>20*1609.334|| distance ==0){
            finalLoc = new LatLng(theCityLoc.getLatitude(),theCityLoc.getLongitude());
        }else {
            finalLoc = new LatLng(theUserLoc.getLatitude(),theUserLoc.getLongitude());

        }

        float milesToLat = 69;
        float earthRadius = 6378137;
        double dn = radius * 1609.344;
        double de = radius * 1609.344;
        double dLat = dn / earthRadius;
        double dLon = de / (earthRadius * Math.cos(Math.PI * finalLoc.latitude / 180));
        double lat1 = finalLoc.latitude - (dLat * 180 / Math.PI);
        double lon1 = finalLoc.longitude - (dLon * 180 / Math.PI);
        double lat2 = finalLoc.latitude  + (dLat * 180 / Math.PI);
        double lon2 = finalLoc.longitude + (dLon * 180 / Math.PI);

        ParseGeoPoint swc = new ParseGeoPoint(lat1, lon1);
        ParseGeoPoint nwc = new ParseGeoPoint(lat2, lon2);

        finalQuery.whereWithinGeoBox("GeoLoc", swc, nwc);
        //finalQuery.whereWithinMiles("GeoLoc", userLocation, 50);

        if (mDialog==null)mDialog = new ProgressDialogFragment().newInstance("Loading Happenings");
        finalQuery.addDescendingOrder("globalWeight");
        finalQuery.addDescendingOrder("weight");
        finalQuery.addDescendingOrder("swipesRight");
        finalQuery.addAscendingOrder("Date");
        if (!mDialog.isAdded())mDialog.show(getChildFragmentManager(),"Loading");
            
        finalQuery.setLimit(10);
            finalQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List list, ParseException e) {
                if (list.size() != 10 && shouldLimit) {
                    shouldLimit=false;
                    refreshQuery();
                } else if (list.size() < 2 && !shouldLimit) {
                    Log.e(TAG,"No events to be found");
                }else {
                    mDialog.dismiss();
                    loadCards(list);

                }
            }
        });
    }
    private void refreshQuery(){
        getQuery(shouldLimit);
    }
    private void loadCards(List list) {
        ArrayList<ParseObject> cardsList = (ArrayList<ParseObject>) list;
        if (mHappenings.size()<10){
            Log.e(TAG, "size " + cardsList.size());
            for (int i = 0; i < cardsList.size(); i++) {
                HappFromParse h = (HappFromParse) cardsList.get(i);
                mHappenings.add(h);
            }
            arrayAdapter.notifyDataSetChanged();
        }

    }



}