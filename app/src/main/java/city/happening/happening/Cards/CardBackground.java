package city.happening.happening.Cards;

import android.location.Location;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import city.happening.happening.HappFromParse;

/**
 * Created by Alex on 7/24/2015.
 */
public class
        CardBackground  {
    private static final String TAG = "CardBackground";
    Boolean shouldRefresh;
    Boolean shouldLimit=false;
    ArrayList<HappFromParse> mAllCards = new ArrayList<>();
    int evCount;
    ParseUser parseUser;
    String noerr = "";
    String tempParseId = new String();
    private ArrayList<Map<String,String>> mFriendArrayList = new ArrayList<>();
    ArrayList<Map<String,String>> tempList = new ArrayList<>();
    protected Location mLastLocation;

    public CardBackground(){

    }
    public CardBackground(Location location){
        this.mLastLocation = location;
    }


    public ArrayList newCards(){
        Log.e(TAG,"newCards");
        ParseQuery newCardsQuery = getQuery(10);
        newCardsQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> eventObjects, ParseException e) {
                if (e == null) {
                    evCount = eventObjects.size();
                    Log.e(TAG,"newCardsQuery"+eventObjects.size());

                   /* if (eventObjects.size() == 0 && shouldLimit) {
                        shouldLimit = false;
                    } else if (eventObjects.size() == 0 && !shouldLimit) {
                        //set no more events to no
                    }
                    */
                    for (int i = 0; i < eventObjects.size(); i++) {
                        HappFromParse temp = (HappFromParse) eventObjects.get(i);
                       temp.setDrawableResourceId(temp.getHash());
                        Log.d(TAG," "+temp.getTitle());
                        mAllCards.add(temp);

                    }
                } else {

                }

            }

        });

        return mAllCards;
    }

    public HappFromParse init() {
        final ArrayList<HappFromParse> mInit = new ArrayList<>();
        ParseQuery initQuery = getQuery(2);
        try {
            HappFromParse temp =(HappFromParse) initQuery.getFirst();
            temp.setDrawableResourceId(temp.getHash());
            return temp;
        }catch (ParseException e){

        }


        return null;


    }
    public ParseQuery getQuery(int x){

        Log.e(TAG,"getQuery");
        ParseQuery eventQuery;
        ParseQuery finalQuery;
        Calendar rightNow = Calendar.getInstance();
        Date today = new Date(rightNow.getTimeInMillis());
        parseUser = ParseUser.getCurrentUser();

      //  String[] categories = (String[])parseUser.get("categories");

        eventQuery = ParseQuery.getQuery("Event");


        /* sort the query by categories chosen in settings.. Default = All cats on first launch
        initialize categories
        eventQuery.whereContainedIn("Hashtag",categories);
        //Sorts the query by most recent events and only shows those after today
        shouldLimit != where boolean for no more events is true
        if(defaults have today listed){
            eventQuery.whereGreaterThan("EndTime",today);//Need to create a now minus from now
            if(shouldLimit){
                eventQuery.whereLessThan("Date",endOfDay);//need to create an end of day variable
            }

        }else if(defaults have tomorrow listed){
            Date tomorrowDate = todays date + time until next beginning of day
            eventQuery.whereGreaterThan("EndTime",TomorrowDate);

            if(shouldLimit){
                eventQuery.whereLessThan("Date",TomorrowDate+endOfDay);//need to create an end of day variable
            }
        }else{
            Date nextWeekDate = find date until equals next weekday
            Date sundayDate = beginnning of the next week

            if(beginningOfDay == sundayDate){//Selected weekend on a sunday
                eventQuery.whereGreaterThan("EndTime",Today);//all sundays events that have at least 30 mins left
                if(shouldLimit){
                    eventQuery.whereLessThan("Date",Today+endOfDay);
                }
            }else if(beginningOfDay == Sat Midday){
                eventQuery.whereGreaterThan("EndTime",SaturdayMidDay);//all that have at least 30 mins left
                if(shouldLimit){
                    eventQuery.whereLessThan("Date",Sunday+endOfDay);
                }
            }else{
                 eventQuery.whereGreaterThan("EndTime",SaturdayBeginning of day);//All weekend events
                if(shouldLimit){
                    eventQuery.whereLessThan("Date",Sunday+endOfDay);
            }
        }
         */

        eventQuery.whereGreaterThan("EndTime", today);
        ParseQuery<ParseObject> weightedQuery = ParseQuery.getQuery("Event");
        weightedQuery.whereGreaterThan("globalWeight", 0);
        weightedQuery.whereGreaterThan("EndTime", today);

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(weightedQuery);
        queries.add(eventQuery);
        finalQuery = ParseQuery.or(queries);


       // ParseGeoPoint userLocation = parseUser.getParseGeoPoint("userLoc");
        ParseGeoPoint userLocation = new ParseGeoPoint();
        int radius = parseUser.getInt("radius");

        ArrayList<Map<String,String>> friends = (ArrayList) ParseUser.getCurrentUser().get("friends");
        //ask about friends id arrays

        ParseQuery<ParseObject> mySwipesQuery = ParseQuery.getQuery("Swipes");

        if(shouldLimit&&parseUser.get("time").equals("today")){
            mySwipesQuery.setLimit(1000);
            mySwipesQuery.whereEqualTo("UserID",parseUser.getObjectId());
           // mySwipesQuery.whereNotContainedIn("swipedAgain",true);
            finalQuery.whereDoesNotMatchKeyInQuery("objectId","EventID",mySwipesQuery);
        }else{
            mySwipesQuery.setLimit(1000);
            mySwipesQuery.whereEqualTo("UserID", parseUser.getObjectId());
            finalQuery.whereDoesNotMatchKeyInQuery("objectId","EventID",mySwipesQuery);
        }



      /* if (mLastLocation!=null){
           userLocation.setLatitude(mLastLocation.getLatitude());
           userLocation.setLatitude(mLastLocation.getLongitude());
           Log.e(TAG, "lat" + userLocation.getLatitude());
           Log.e(TAG, "long" + userLocation.getLongitude());

       }else{*/
           userLocation.setLatitude(38.907192);
           userLocation.setLongitude(-77.036871);
     //  }



        float milesToLat = 69;
        float earthRadius = 6378137;
        double dn = radius * 1609.344;
        double de = radius * 1609.344;
        double dLat = dn / earthRadius;
        double dLon = de / (earthRadius * Math.cos(Math.PI * userLocation.getLatitude() / 180));
        double lat1 = userLocation.getLatitude() - (dLat * 180 / Math.PI);
        double lon1 = userLocation.getLongitude() - (dLon * 180 / Math.PI);
        double lat2 = userLocation.getLatitude() + (dLat * 180 / Math.PI);
        double lon2 = userLocation.getLongitude() + (dLon * 180 / Math.PI);

        ParseGeoPoint swc = new ParseGeoPoint(lat1, lon1);
        ParseGeoPoint nwc = new ParseGeoPoint(lat2, lon2);

        finalQuery.whereWithinGeoBox("GeoLoc", swc, nwc);
        //finalQuery.whereWithinMiles("GeoLoc", userLocation, 50);
        finalQuery.orderByDescending("globalWeight");
        finalQuery.addDescendingOrder("weight");
        finalQuery.addDescendingOrder("swipesRight");
        finalQuery.addAscendingOrder("Date");

        finalQuery.setLimit(x);

        return finalQuery;
    }




}
