package city.happening.happening.ActivityFeed;

import android.os.AsyncTask;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Alex on 9/30/2015.
 */
public class LoadFriendActivities extends AsyncTask<Void,Void,ArrayList<ParseObject>> {

    private final FriendActivityAdapter mAdapter;
    ArrayList<Map<String,String>> mCurrentActivities;

    public LoadFriendActivities(FriendActivityAdapter adapter,ArrayList<Map<String,String>> currentActivities){
        mAdapter = adapter;
        mCurrentActivities =currentActivities;
    }

    private ParseQuery getActivities(){
    ArrayList<Map<String,String>> friends =(ArrayList) ParseUser.getCurrentUser().get("friends");
        ArrayList<Map<String,String>> idsArray = new ArrayList();
        for (Map<String,String> id:friends){
            Map<String,String> tempId = new HashMap<>();
            tempId.put("parseId",id.get("parseId"));
            Log.e("creatingId array", "" + tempId);
            idsArray.add(tempId);
        }
        ParseQuery finalQuery = ParseQuery.getQuery("Activity");

        ParseQuery reminderQuery = ParseQuery.getQuery("Activity");
        reminderQuery.whereEqualTo("type","reminder");
        reminderQuery.whereEqualTo("userParseId",ParseUser.getCurrentUser().getObjectId());

        ParseQuery friendJoinedQuery = ParseQuery.getQuery("Activity");
        friendJoinedQuery.whereEqualTo("type","friendJoined");
        friendJoinedQuery.whereContainedIn("userParseId",idsArray);

        ParseQuery interestedQuery = ParseQuery.getQuery("Activity");
        ArrayList<String>type = new ArrayList<>();
        type.add("interested");
        type.add("going");
        type.add("create");
        interestedQuery.whereContainedIn("type", type);
        interestedQuery.whereContainedIn("userFBId", idsArray);

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(interestedQuery);
        queries.add(reminderQuery);
        queries.add(friendJoinedQuery);

        finalQuery = ParseQuery.or(queries);
        finalQuery.include("eventObject");
        finalQuery.orderByDescending("createdDate");
        finalQuery.setLimit(15);

;
        //finalQuery.setLimit(15);





        return finalQuery;
     }

    @Override
    protected ArrayList<ParseObject> doInBackground(Void... params) {
        ParseQuery friendQuery = getActivities();
        ArrayList<ParseObject> activityList = new ArrayList<>();
        try {
            ArrayList<ParseObject> list = (ArrayList<ParseObject>)friendQuery.find();
            if (list.size()!=0){
                for (int i =list.size()-1;i>=0;i--){
                    Map<String,String> activityDict = new HashMap<>();
                    ParseObject object = list.get(i);
                    int count = 0;
                    for (Map<String,String> map:mCurrentActivities){
                        if (map.get("objectId")==object.getObjectId())count++;
                    }
                    if (count!=0){
                        object.pinInBackground();
                        activityDict.put("objectId", object.getObjectId());
                        String type = (String) object.get("type");
                        if (type.equals("interested")||type.equals("going")||type.equals("create")){
                            if (object.get("eventId")!=null){
                                activityDict.put("eventId",(String)object.get("eventId"));
                            }
                            mCurrentActivities.add(activityDict);

                            activityList.add(object);

          }
                    }

                }
            }

        }catch (ParseException e){

        }
        return activityList;

    }
    protected void onPostExecute(ArrayList<ParseObject> activities) {
        mAdapter.upDateEntries(activities);
    }
}
