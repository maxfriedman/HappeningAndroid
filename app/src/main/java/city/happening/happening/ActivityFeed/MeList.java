package city.happening.happening.ActivityFeed;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import city.happening.happening.EventActivity;
import city.happening.happening.EventFragment;
import city.happening.happening.HappFromParse;
import city.happening.happening.HappeningLab;
import city.happening.happening.R;

/**
 * Created by Alex on 9/29/2015.
 */
public class MeList extends Fragment {
    ActivityAdapter mAdapter;
    LayoutInflater mInflater;
    ListView mListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragmentwithlistdivider, container,false);
        mListView =(ListView) v.findViewById(R.id.listView);

        mInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ParseQueryAdapter.QueryFactory<ParseObject> factory = new ParseQueryAdapter.QueryFactory<ParseObject>() {
            public ParseQuery<ParseObject> create() {
                ArrayList<Map<String,String>> friends =(ArrayList) ParseUser.getCurrentUser().get("friends");
                ArrayList<Map<String,String>> idsArray = new ArrayList();
                for (Map<String,String> id:friends){
                    Map<String,String> tempId = new HashMap<>();
                    tempId.put("parseId",id.get("parseId"));
                    Log.e("creatingId array", "" + tempId);
                    idsArray.add(id);
                }
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Activity");
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
                type.add("friendJoined");
                interestedQuery.whereContainedIn("type", type);
                interestedQuery.whereContainedIn("userFBId", idsArray);

                List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
                //queries.add(interestedQuery);
                queries.add(reminderQuery);
                queries.add(friendJoinedQuery);

                finalQuery = ParseQuery.or(queries);
                finalQuery.include("eventObject");
                finalQuery.orderByDescending("createdDate");



                return finalQuery;

            }
        };

        mAdapter = new ActivityAdapter(getActivity(),factory);
        mListView.setAdapter(mAdapter);



        return v;
    }

    private class ActivityAdapter extends ParseQueryAdapter<ParseObject> {

        public ActivityAdapter(Context context,ParseQueryAdapter.QueryFactory<ParseObject> queryFactory) {
            super(context, queryFactory);
        }

        @Override
        public ParseObject getItem(int index) {
            return super.getItem(index);
        }


        @Override
        public View getItemView(ParseObject parseObject,View convertView,ViewGroup parent){
            final String eventId =(String) parseObject.get("eventId");
            ParseQuery parseQuery = ParseQuery.getQuery("Event");
            HappFromParse happening = null;
            try{
                HappFromParse temp = (HappFromParse)parseQuery.get(eventId);
                temp.setDrawableResourceId(temp.getHash());
                happening = temp;

            }catch (ParseException e){

            }

            View v;
            ViewHolder holder;
            if (parseObject.get("type").equals("friendJoined")){
                if(convertView == null) {
                    v = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_activity_reminder, parent, false);
                    holder = new ViewHolder();
                    holder.name =(TextView) v.findViewById(R.id.reminderText);
                    holder.userPicture =(ProfilePictureView) v.findViewById(R.id.reminderImage);
                    holder.container2 = (LinearLayout)v.findViewById(R.id.container);
                    v.setTag(holder);

                } else {
                    v = convertView;
                    holder = (ViewHolder)v.getTag();
                }
                if (parseObject!=null){

                    holder.name.setText(((String)parseObject.get("userFullName"))+" Just joined Happening WOO!!!");
                    holder.userPicture.setProfileId(((String)parseObject.get("userFBId")));

                }
                return v;

            }else {
                if(convertView == null) {
                    v = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_activity_me, parent, false);
                    holder = new ViewHolder();
                    holder.name =(TextView) v.findViewById(R.id.userText);
                    holder.picture =(ImageView) v.findViewById(R.id.activityImage);
                    holder.date = (TextView)v.findViewById(R.id.activityDate);
                    holder.title = (TextView)v.findViewById(R.id.activityTitle);
                    holder.container = (RelativeLayout)v.findViewById(R.id.eventContainer);
                    v.setTag(holder);


                } else {
                    v = convertView;
                    holder = (ViewHolder)v.getTag();
                }

                if (parseObject!=null){
                    Date date = (Date)parseObject.get("eventDate");
                    Calendar cal2 = Calendar.getInstance();
                    cal2.setTime(date);
                    Calendar now = Calendar.getInstance();
                    long timeTillStart = TimeUnit.MILLISECONDS.toMinutes(cal2.getTimeInMillis()-now.getTimeInMillis());
                    Log.e("Activity","tillStart "+timeTillStart);
                    holder.name.setText("Reminder Event Starts in " +timeTillStart+ " minutes");


                    if (parseObject.get("createdDate")!=null){
                        Calendar cal = Calendar.getInstance();
                        cal.setTime((Date) parseObject.get("createdDate"));
                        if (cal!=null)holder.date.setText(" " + String.format("%1$ta %1$tb %1$td at %1$tI:%1$tM %1$Tp", cal));
                    }


                    if (parseObject.get("eventName")!=null){
                        String title = (String) parseObject.get("eventName");
                        if (title!=null)holder.title.setText(title);

                    }
                    holder.picture.setImageResource(happening.getDrawableResourceId());
                    if (happening.getImage()!=null)holder.picture.setImageBitmap(happening.getImage());
                    final HappFromParse tempHapp = happening;
                    holder.container.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            HappeningLab.get(getActivity()).addHappening(tempHapp);
                            Intent i = new Intent(getActivity(), EventActivity.class);
                            i.putExtra(EventFragment.EXTRA_EVENT_ID, eventId);
                            startActivityForResult(i, 0);
                        }
                    });


                }
                return v;
            }




        }
    }
    private static class ViewHolder {
        ProfilePictureView userPicture;
        ImageView picture;
        TextView name,date,title;
        RelativeLayout container;
        LinearLayout container2;

    }



    //checkforupdatedActivities
    //

    /*public void queries(){
        ParseQuery meQuery= ParseQuery.getQuery("Activity");
        meQuery.fromPin("me");
        meQuery.orderByDescending("createdDate");
        meQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List list, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < list.size(); i++) {

                        ParseObject object = (ParseObject) list.get(i);
                    }

                }
            }
        });

        ParseQuery friendsQuery = ParseQuery.getQuery("Activity");
        friendsQuery.fromPin("friends");
        friendsQuery.orderByDescending("createdDate");
        friendsQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List list, ParseException e) {
                if (e==null){
                    for (int i=0 ; i<list.size();i++){
                        ParseObject object = (ParseObject)list.get(i);

                    }
                }
            }

        });


    }
    public void updateFriends(){
        ParseQuery finalQuery = ParseQuery.getQuery("Activity");

        ParseQuery interestedQuery = ParseQuery.getQuery("Activity");
        ArrayList<String>type = new ArrayList<>();
        type.add("interested");
        type.add("going");
        type.add("create");
        interestedQuery.whereContainedIn("type", type);
        ArrayList<Map<String,String>> friends =(ArrayList<Map<String,String>>) ParseUser.getCurrentUser().get("friends");
        interestedQuery.whereContainedIn("userFBId", friends);//need just the fbids!!

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(interestedQuery);
        finalQuery = ParseQuery.or(queries);
        finalQuery.orderByDescending("createdDate");

        //finalQuery.setLimit(15);

        finalQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List list, ParseException e) {
                if (e==null){
                    for (int i =list.size()-1;i>=0;i--){

                    }
                }
            }


        });


    }*/
}
