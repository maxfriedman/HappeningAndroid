package city.happening.happening.Cards;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
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
 * Created by Alex on 6/3/2015.
 */
public class CardFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final String EXTRA_CARD_ID = "Happening.CARD_ID";
    private ArrayList<HappFromParse> mHappenings = new ArrayList<>();
    private CustomArrayAdapter arrayAdapter;
    private int i,j;
    private static final String TAG = "CardFragment";
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    ParseQuery<ParseObject> swipesQuery;
    ParseUser mParseUser;
    CardBackground mCardBackground = new CardBackground();
    @InjectView(R.id.frame)
    SwipeFlingAdapterView flingContainer;


    int count;


    public static CardFragment newInstance() {

        CardFragment f = new CardFragment();
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        count = 0;
        Log.e(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        buildGoogleApiClient();
        newCards();
       // mHappenings.add(mCardBackground.init());
        Log.e(TAG, ParseUser.getCurrentUser().getObjectId());
//        setRetainInstance(true);


    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume" + mHappenings.size());
        setHasOptionsMenu(true);

        //arrayAdapter.notifyDataSetChanged();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause"+mHappenings.size());


    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup parent, Bundle savedInstanceState) {
        Log.e(TAG,"onCreateView");
        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.fragment_card, parent, false);

        flingContainer = (SwipeFlingAdapterView) v.findViewById(R.id.frame);
        ButterKnife.inject(getActivity());
        HappFromParse trial = new HappFromParse();
        trial.setTitle(" ");
        Log.e(TAG, "" + mHappenings.size());
        //mHappenings.add(trial);
        ParseQuery initQuery = mCardBackground.getQuery(2);
        try {
            HappFromParse temp =(HappFromParse) initQuery.getFirst();
            temp.setDrawableResourceId(temp.getHash());
            mHappenings.add(temp);
        }catch (ParseException e){

        }

        Log.e(TAG, "" + mHappenings.size());
        if (mHappenings.size()<10)newCards();
        Log.e(TAG,""+mHappenings.size());
        arrayAdapter = new CustomArrayAdapter(getActivity(), mHappenings);
        arrayAdapter.notifyDataSetChanged();
        flingContainer.setAdapter(arrayAdapter);

        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {

            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
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

                if (temp!=null){
                    temp.unpinInBackground();
                    temp.increment("swipesLeft");
                    temp.saveEventually();

                    ParseUser user = ParseUser.getCurrentUser();
                    Log.d(TAG, "" + ParseUser.getCurrentUser());
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("userID", user.getObjectId());
                    params.put("eventID", temp.getObjectId());
                    params.put("swiped", "left");

                    ParseCloud.callFunctionInBackground("swipeAnalytics", params);

                }
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
                Log.e(TAG, "" + mHappenings.size());
                newCards();
                arrayAdapter.notifyDataSetChanged();
                Log.d("LIST", "notified");
                count++;
                i++;
                Log.e(TAG, "" + mHappenings.size());
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
                View view = flingContainer.getSelectedView();
                view.findViewById(R.id.item_swipe_right_indicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                view.findViewById(R.id.item_swipe_left_indicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
                TextView textView = (TextView) view.findViewById(R.id.likeornot);
                int swipe = 0;
                if (scrollProgressPercent < 0 ){
                    switch (swipe){
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
                    if (swipe==3) swipe =0;
                }else if (scrollProgressPercent>0){
                    switch (swipe){
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
                    if (swipe==3) swipe =0;
                }

            }
        });
        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                makeToast(getActivity(), "Clicked!");
                HappFromParse h = (HappFromParse) dataObject;
                HappeningLab.get(getActivity()).addHappening(h);
                Intent i = new Intent(getActivity(), EventActivity.class);
                i.putExtra(EventFragment.EXTRA_EVENT_ID, h.getObjectId());
                startActivityForResult(i, 0);
            }
        });

        if(mHappenings.size()>1&&mHappenings.get(0).getTitle().equals(" "))mHappenings.remove(0);
        return v;

    }
    public void newCards() {
        Log.e(TAG, "newCards");
        ParseQuery newCardsQuery = mCardBackground.getQuery(10);
        newCardsQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> eventObjects, ParseException e) {
                if (e == null) {
                    ArrayList<HappFromParse>tempList = new ArrayList<HappFromParse>();
                    Log.e(TAG, "newCardsQuery" + eventObjects.size());

                    for (int i = 0; i < eventObjects.size(); i++) {
                        HappFromParse temp = (HappFromParse) eventObjects.get(i);
                        temp.setDrawableResourceId(temp.getHash());
                        Log.d(TAG, "Title " + temp.getTitle()+"Date "+temp.getEndTime().toString());

                        tempList.add(temp);




                    }
                    setHappenings(tempList);
                } else {

                }

            }

        });
    }

    public void setHappenings(ArrayList<HappFromParse> happenings) {

        if (mHappenings.size()>10){

        }else {
            mHappenings.addAll(happenings);
        }

//        arrayAdapter.notifyDataSetChanged();
        count =0;
    }

    static void makeToast(Context ctx, String s){
        Toast.makeText(ctx, s, Toast.LENGTH_SHORT).show();
    }



    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        mCardBackground = new CardBackground(mLastLocation);

        /*if (mLastLocation != null) {
            mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
        } else {
            Toast.makeText(this, R.string.no_location_detected, Toast.LENGTH_LONG).show();
        }*/
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i("HappMain", "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i("HappMain", "Connection suspended");
        mGoogleApiClient.connect();
    }


}


