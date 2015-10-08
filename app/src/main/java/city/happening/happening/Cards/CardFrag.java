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

import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
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
import city.happening.happening.R;

/**
 * Created by Alex on 9/28/2015.
 */
public class CardFrag extends Fragment {
    public static final String EXTRA_CARD_ID = "Happening.CARD_ID";
    private static final String TAG = "CardFrag";
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public static final int BUTTON_CLICK_REQUEST = 1;

    private DialogFragment mDialog;
    private ArrayList<HappFromParse> mHappenings;
    private CustomArrayAdapter arrayAdapter;
    private UserLocManager mLocManager;
    private Location mLastLocation;

    private boolean getHasRun,initHasRun;
    private int i,j;
    Boolean shouldLimit;

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
        mHappenings = new ArrayList<>();
        getHasRun = false;
        swipe =0;
        initHasRun = false;
        count = 0;
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
        //new FetchHappTask().execute();
        //arrayAdapter.notifyDataSetChanged();

        if (mHappenings.size()==0){
            ParseQuery newCardsQuery = mCardBackground.getQuery(10, getActivity(),false);
            try {
                List<ParseObject> list = newCardsQuery.find();
                for (int i = 0; i <list.size(); i++) {
                    HappFromParse temp = (HappFromParse) list.get(i);
                    temp.setDrawableResourceId(temp.getHash());
                    Log.d(TAG, "Title " + temp.getTitle() + "Date " + temp.getEndTime().toString());
                    mHappenings.add(temp);

                }
                flingContainer.invalidate();
                arrayAdapter.notifyDataSetChanged();

            }catch (ParseException e){Log.e(TAG,"ParseExeption");}
            Log.e(TAG, "onResume" + mHappenings.size());
        }


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
        Log.e(TAG, "onPause" + mHappenings.size());
       // mHappenings = new ArrayList<>();
       // arrayAdapter = new CustomArrayAdapter(getActivity(),mHappenings);
       // arrayAdapter.notifyDataSetChanged();
        Log.e(TAG, "onPause" + mHappenings.size());
        Log.e(TAG, "onPause" + arrayAdapter.getCount());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup parent, Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView");
        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.fragment_card, parent, false);
        cardContainer =(FrameLayout) v.findViewById(R.id.cardContainer);
        mHappenings = new ArrayList();
        if (initHasRun==true)newCards();
        //count++;
        Log.e(TAG, "newCards");
        flingContainer = (SwipeFlingAdapterView) v.findViewById(R.id.frame);
        ButterKnife.inject(getActivity());
       /* if (mHappenings.size()==0&&!initHasRun){
            initHasRun=true;

            //  initHappenings();
            ParseQuery newCardsQuery = mCardBackground.getQuery(10, getActivity(),false);
            try {
                List<ParseObject> list = newCardsQuery.find();
                for (int i = 0; i <list.size(); i++) {
                    HappFromParse temp = (HappFromParse) list.get(i);
                    temp.setDrawableResourceId(temp.getHash());
                    Log.d(TAG, "Title " + temp.getTitle() + "Date " + temp.getEndTime().toString());
                    mHappenings.add(temp);

                }
            }catch (ParseException e){Log.e(TAG,"ParseExeption");}
        }else if (mHappenings.size()==0){
            newCards();
        }*/
       // mDialog = new ProgressDialogFragment().newInstance("Loading Happenings");
        newCards();
        arrayAdapter = new CustomArrayAdapter(getActivity(),mHappenings);
        arrayAdapter.notifyDataSetChanged();
        flingContainer.setAdapter(arrayAdapter);
        flingContainer.setMinStackInAdapter(0);

        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                Log.d("LIST", "removed object!");
                mHappenings.remove(0);
                arrayAdapter.notifyDataSetChanged();
                Log.e(TAG, "" + mHappenings.size());
            }

            @Override
            public void onLeftCardExit(Object eventObjects) {
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

                swipesQuery
                        .countInBackground(new CountCallback() {
                            @Override
                            public void done(int i, ParseException e) {
                                if (i > 0) {
                                    swipesQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                                        @Override
                                        public void done(ParseObject object, ParseException e) {
                                            object.put("swipedAgain", true);
                                            object.put("swipedRight", false);
                                            object.put("swipedLeft", true);

                                            ParseQuery timeLineQuery = ParseQuery.getQuery("Timeline");
                                    /*    timeLineQuery.fromLocalDatastore();
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
                                        activityQuery.fromLocalDatastore();
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
                                        });*/
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

            @Override
            public void onRightCardExit(Object eventObjects) {
                HappFromParse temp = (HappFromParse) eventObjects;
                Log.e(TAG, "" + temp.getTitle());

                mParseUser = ParseUser.getCurrentUser();
                final String tempId = temp.getObjectId();
                temp.pinInBackground();
                temp.increment("swipesRight");
                temp.saveEventually();


                String tag = temp.getHash();
                if (tag.equals("Happy Hour")) {
                    mParseUser.increment("HappyHour");
                } else {
                    mParseUser.increment(tag);
                }
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
                swipesQuery.whereEqualTo("EventID", temp.getObjectId());
                swipesQuery.whereEqualTo("UserID", mParseUser.getObjectId());

                // ParseObject swipesObject = ParseObject.c
                //the fuck was that ^

                swipesQuery.countInBackground(new CountCallback() {
                    @Override
                    public void done(int i, ParseException e) {
                        if (i > 0) {
                            swipesQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject object, ParseException e) {

                                    object.put("swipedAgain", true);
                                    object.put("swipedAgain", false);
                                    object.put("swipedRight", true);
                                    object.put("isGoing", true);
                                    object.saveInBackground();
                                }
                            });
                        } else {
                            ParseObject swipesObject = ParseObject.create("Swipes");
                            swipesObject.put("UserID", mParseUser.getObjectId());
                            swipesObject.put("EventID", tempId);
                            swipesObject.put("swipedRight", true);
                            swipesObject.put("swipedLeft", false);
                            swipesObject.put("isGoing", true);
                            swipesObject.put("FBObjectID", mParseUser.get("FBObjectID"));
                            swipesObject.saveInBackground();
                        }
                    }
                });

            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Ask for more data here
                newCards();
                count++;
                i++;
                Log.e(TAG, "" + mHappenings.size());
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
                View view = flingContainer.getSelectedView();
                View right = view.findViewById(R.id.item_swipe_right_indicator);
                right.setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                View left = view.findViewById(R.id.item_swipe_left_indicator);
                left.setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);

                TextView textView = (TextView) view.findViewById(R.id.likeornot);
                Log.e(TAG,"scroll"+scrollProgressPercent);
                textView.setText(scrollProgressPercent<0?getPositive():getNegative());



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
                startActivityForResult(i, BUTTON_CLICK_REQUEST);
            }
        });


        return v;
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
        //mDialog = ProgressDialogFragment.newInstance();
       // mDialog.show(getChildFragmentManager(),"Loading!!");
        Log.e(TAG, "newCards");
        ParseQuery newCardsQuery = mCardBackground.getQuery(10,getActivity(),shouldLimit);
        newCardsQuery.findInBackground(new FindCallback<ParseObject>() {
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
        });
    }

    public void setHappenings(ArrayList<HappFromParse> happenings) {
        if (mHappenings.size()<10){
            mHappenings.addAll(happenings);
            arrayAdapter.notifyDataSetChanged();
           // mDialog.dismiss();
        }

    }

}
