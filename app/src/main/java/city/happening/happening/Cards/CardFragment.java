package city.happening.happening.Cards;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.AsyncTask;
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
import city.happening.happening.ProgressDialogFragment;
import city.happening.happening.R;


/**
 * Created by Alex on 6/3/2015.
 */
public class CardFragment extends Fragment  {
    public static final String EXTRA_CARD_ID = "Happening.CARD_ID";
    private static final String TAG = "CardFragment";
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

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
        mHappenings = new ArrayList<>();
        getHasRun = false;
        initHasRun = false;
        count = 0;
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
        String time =(String) mParseUser.get("time");
        if (time!=null){
            if (time.equals("today")||time.equals("tomorrow")|time.equals("this weekend")){
                shouldLimit=true;
            }else {
                shouldLimit = false;
            }
        }
        setHasOptionsMenu(true);
        mLocManager.startLocationUpdates();
        new FetchHappTask().execute();
        //arrayAdapter.notifyDataSetChanged();
        Log.e(TAG, "onResume" + mHappenings.size());
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
      //  mHappenings = new ArrayList<>();
      //  arrayAdapter.notifyDataSetChanged();
        Log.e(TAG, "onPause" + mHappenings.size());
        Log.e(TAG, "onPause" + arrayAdapter.getCount());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup parent, Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView");
        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.fragment_card, parent, false);
        cardContainer =(FrameLayout) v.findViewById(R.id.cardContainer);

        //count++;
        flingContainer = (SwipeFlingAdapterView) v.findViewById(R.id.frame);
        ButterKnife.inject(getActivity());

        if (mHappenings.size()==0&&!initHasRun){
            initHasRun=true;
            mDialog = new ProgressDialogFragment().newInstance("Load");
            mDialog.show(getChildFragmentManager(), "Loading Happenings!!!");
            Log.e(TAG, "syncron call running");
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
                setUI();
                mDialog.dismiss();
            }catch (ParseException e){Log.e(TAG,"ParseExeption");}
        }else if (mHappenings.size()==0){
            setUI();
            new FetchHappTask().execute();
        }else{
            setUI();
        }


        return v;
    }
    private void initHappenings() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ParseQuery newCardsQuery = mCardBackground.getQuery(10, getActivity(),false);
                try {
                    List<ParseObject> list = newCardsQuery.find();
                    for (int i = 0; i <list.size(); i++) {
                        HappFromParse temp = (HappFromParse) list.get(i);
                        temp.setDrawableResourceId(temp.getHash());
                        Log.d(TAG, "Title " + temp.getTitle() + "Date " + temp.getEndTime().toString());
                        mHappenings.add(temp);
                        //setUI();
                    }
                }catch (ParseException e){Log.e(TAG,"ParseExeption");}
            }
        }).start();
    }
    private void setUI(){

        arrayAdapter = new CustomArrayAdapter(getActivity(),mHappenings);
        arrayAdapter.notifyDataSetChanged();
        flingContainer.setAdapter(arrayAdapter);

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

                //Ask max about the happy hours banching statement

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
                new FetchHappTask().execute();
                arrayAdapter.notifyDataSetChanged();
                count++;
                i++;
                Log.e(TAG, "" + mHappenings.size());
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
                View view = flingContainer.getSelectedView();
                /*view.findViewById(R.id.item_swipe_right_indicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                view.findViewById(R.id.item_swipe_left_indicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
                */TextView textView = (TextView) view.findViewById(R.id.likeornot);
                int swipe = 0;
                if (scrollProgressPercent < 0) {
                    switch (swipe) {
                        case 0:
                            textView.setText("Yeah!");
                            swipe++;
                            break;
                        case 1:
                            textView.setText("Like");
                            swipe++;
                            break;
                        case 2:
                            textView.setText("I'm in");
                            swipe++;
                            break;
                    }
                    if (swipe == 3) swipe = 0;
                } else if (scrollProgressPercent > 0) {
                    switch (swipe) {
                        case 0:
                            textView.setText("eh");
                            swipe++;
                            break;
                        case 1:
                            textView.setText("Nope");
                            swipe++;
                            break;
                        case 2:
                            textView.setText("Not For Me");
                            swipe++;
                            break;
                    }
                    if (swipe == 3) swipe = 0;
                }
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
                startActivityForResult(i, 0);
            }
        });

        mDialog.dismiss();
        //cardContainer.addView(flingContainer);

    }

    private class FetchHappTask extends AsyncTask<Void,Void,ArrayList<HappFromParse>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog = new ProgressDialogFragment().newInstance("Loading");
            mDialog.show(getChildFragmentManager(), "Loading Happenings!!");
        }

        @Override
        protected ArrayList<HappFromParse> doInBackground(Void... params) {
            Boolean lessThanTen = false;
            ArrayList<HappFromParse>tempList = new ArrayList<HappFromParse>();
            count++;
            Log.e(TAG, "newCards");

            mParseUser = ParseUser.getCurrentUser();

            ParseQuery newCardsQuery = mCardBackground.getQuery(10, getActivity(), shouldLimit);
            try {
                int x = newCardsQuery.count();
                Log.e(TAG, "newCards"+x);
                if (x==0){
                    shouldLimit =false;
                    newCardsQuery = mCardBackground.getQuery(10, getActivity(), shouldLimit);
                    if (newCardsQuery.count()==0){
                        mParseUser.put("radius",50);
                    }
                }else if (x<10){
                    shouldLimit = true;
                    lessThanTen =true;
                }
            }catch (ParseException e){}
            try {
                if (shouldLimit==false){
                    newCardsQuery = mCardBackground.getQuery(10,getActivity(),shouldLimit);
                    List<ParseObject> list = newCardsQuery.find();
                    for (int i = 0; i <list.size(); i++) {
                        HappFromParse temp = (HappFromParse) list.get(i);
                        temp.setDrawableResourceId(temp.getHash());
                        Log.d(TAG, "Title " + temp.getTitle()+"Date "+temp.getEndTime().toString());
                        tempList.add(temp);
                    }
                }else if (shouldLimit==true&&lessThanTen==true){
                    List<ParseObject> list = newCardsQuery.find();

                    ParseQuery newCardsQuery2 = mCardBackground.getQuery(10,getActivity(),false);
                    list.addAll(newCardsQuery2.find());
                    for (int i = 0; i <list.size(); i++) {
                        HappFromParse temp = (HappFromParse) list.get(i);
                        temp.setDrawableResourceId(temp.getHash());
                        Log.d(TAG, "Title " + temp.getTitle() + "Date " + temp.getEndTime().toString());
                        tempList.add(temp);
                    }

                }else {
                    newCardsQuery = mCardBackground.getQuery(10, getActivity(),false);

                    List<ParseObject> list = newCardsQuery.find();
                    for (int i = 0; i <list.size(); i++) {
                        HappFromParse temp = (HappFromParse) list.get(i);
                        temp.setDrawableResourceId(temp.getHash());
                        Log.d(TAG, "Title " + temp.getTitle()+"Date "+temp.getEndTime().toString());
                        tempList.add(temp);
                    }
                }

            }catch (ParseException e){Log.e(TAG,"ParseExeption");}
            return tempList;
        }

        @Override
        protected void onPostExecute(ArrayList<HappFromParse> items) {
            mHappenings = items;
            if (arrayAdapter ==null){
                arrayAdapter = new CustomArrayAdapter(getActivity(),mHappenings);
                arrayAdapter.notifyDataSetChanged();
            }
            arrayAdapter= new CustomArrayAdapter(getActivity(),mHappenings);
            flingContainer.setAdapter(arrayAdapter);

           // arrayAdapter.notifyDataSetChanged();
            Log.e("FetchHapp", "Happ size" + mHappenings.size());
            Log.e("FetchHapp", "AdapterSize" + arrayAdapter.getCount());

            setUI();

            mDialog.dismiss();



        }
    }
}


