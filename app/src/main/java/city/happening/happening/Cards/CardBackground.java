package city.happening.happening.Cards;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Alex on 7/24/2015.
 */
public class CardBackground{
    private static final String TAG = "CardBackground";
    public Boolean shouldLimit;
    ParseUser mParseUser;
    protected Location mLastLocation;
    private Context mAppContext;



    public CardBackground(Context context, Location location){
        mAppContext = context;
        mLastLocation = location;
    }



    public ParseQuery getQuery(int x,Context context, boolean limit){
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
        eventQuery.whereContainedIn("Hashtag",categories);

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
        weightedQuery.whereGreaterThan("globalWeight", 0);
        weightedQuery.whereGreaterThan("EndTime", today);
        weightedQuery.whereNotEqualTo("private",true);//look for the privacy strings line 194-212 pull from github

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
        if (selectedCity.equals("Washington ,DC")){
            citLoc= new LatLng(38.907192,-77.036871);
        }else if (selectedCity.equals("Boston ,MA")) {
           citLoc = new LatLng(42.358431, -71.059773);
        }else if (selectedCity.equals("Nashville ,TN")){
            citLoc = new LatLng(36.162664,-86.781602);
        }else if (selectedCity.equals("Philadelphia ,PA")){
           citLoc = new LatLng(39.952584,-75.165222);
        }else if (selectedCity.equals("San Francisco ,CA")) {
            citLoc = new LatLng(37.774929, -122.419416);
        }

        Location theCityLoc = new Location("");
        theCityLoc.setLongitude(citLoc.longitude);
        theCityLoc.setLatitude(citLoc.latitude);
        Location theUserLoc = new Location("");
        float distance;
        if(mLastLocation!=null){
             theUserLoc= mLastLocation;
            Log.e(TAG,"Location"+mLastLocation.getLongitude());
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



        finalQuery.addDescendingOrder("weight");
        finalQuery.addDescendingOrder("swipesRight");
        finalQuery.addAscendingOrder("Date");

        finalQuery.setLimit(x);

        return finalQuery;
    }





}
