package city.happening.happening.ActivityFeed;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;
import com.parse.FindCallback;
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

import city.happening.happening.EventActivity;
import city.happening.happening.EventFragment;
import city.happening.happening.HappFromParse;
import city.happening.happening.HappeningLab;
import city.happening.happening.Profile.ProfileActivity;
import city.happening.happening.Profile.ProfileFragment;
import city.happening.happening.R;

/**
 * Created by Alex on 9/30/2015.
 */
public class FriendActivityList extends Fragment {
    ListView mListView;
    LayoutInflater mInflater;
    ActivityAdapter mAdapter;
    ArrayList<Map<String,String>> mCurrentActivities;
    ArrayList<ParseObject> activityList;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityList = new ArrayList<>();
        mCurrentActivities = new ArrayList<>();
        //loadFromParse();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragmentwithlistdivider,parent,false);
        mListView =(ListView) v.findViewById(R.id.listView);
        mInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ParseQueryAdapter.QueryFactory<ParseObject> factory = new ParseQueryAdapter.QueryFactory<ParseObject>() {
            public ParseQuery<ParseObject> create() {
                ArrayList<Map<String,String>> friends =(ArrayList) ParseUser.getCurrentUser().get("friends");
                ArrayList<String> idsArray = new ArrayList();
                for (Map<String,String> id:friends){
                    String tempId = id.get("id");
                    //Log.e("creatingId array", "" + tempId);
                    idsArray.add(tempId);
                }
                ParseQuery interestedQuery = ParseQuery.getQuery("Activity");
                ArrayList<String>type = new ArrayList<>();
                type.add("interested");
                type.add("going");
                type.add("create");
                interestedQuery.whereContainedIn("type", type);
                interestedQuery.whereContainedIn("userFBId", idsArray);


                interestedQuery.fromLocalDatastore();
                interestedQuery.orderByDescending("createdDate");



                return interestedQuery;

            }
        };

        mAdapter = new ActivityAdapter(getActivity(),factory);
        mListView.setAdapter(mAdapter);
        loadFromParse();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mListView.invalidateViews();
    }

    private void loadFromParse(){
        ArrayList<Map<String,String>> friends =(ArrayList) ParseUser.getCurrentUser().get("friends");
        ArrayList<String> idsArray = new ArrayList();
        for (Map<String,String> id:friends){
            String tempId = id.get("id");
            //Log.e("creatingId array", "" + tempId);
            idsArray.add(tempId);
        }
        ParseQuery finalQuery = ParseQuery.getQuery("Activity");


        ParseQuery interestedQuery = ParseQuery.getQuery("Activity");
        ArrayList<String>type = new ArrayList<>();
        type.add("interested");
        type.add("going");
        type.add("create");
        interestedQuery.whereContainedIn("type", type);
        interestedQuery.whereContainedIn("userFBId", idsArray);

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(interestedQuery);


        finalQuery = ParseQuery.or(queries);
        finalQuery.include("eventObject");
        finalQuery.orderByDescending("createdDate");
        finalQuery.setLimit(20);

        finalQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List list, ParseException e) {
                Log.e("FriendActivityList","Found "+list.size());
                if (list.size() != 0) {
                    for (int i = list.size() - 1; i >= 0; i--) {
                        Map<String, String> activityDict = new HashMap<>();
                        ParseObject object = (ParseObject)list.get(i);
                        int count = 0;
                        object.pinInBackground();
                        activityDict.put("objectId", object.getObjectId());
                        String type = (String) object.get("type");

                        activityDict.put("eventId", (String) object.get("eventId"));

                        mCurrentActivities.add(activityDict);
                        /*try{
                            Log.e("friendActivityList", "title pinned" + object.getObjectId());
                            object.pin("friendActivityList");
                        }catch (ParseException e2){
                            Log.e("FriendActivityList", "exception "+e);
                        }*/

                    }
                    mAdapter.loadObjects();
                    mAdapter.notifyDataSetChanged();


                }
            }

        });
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
                if (temp.getHash()!=null){
                    temp.setDrawableResourceId(temp.getHash());
                    happening = temp;
                }else {
                    happening = new HappFromParse();
                }


            }catch (ParseException e){

            }
            View v;
            ViewHolder holder;
            if(convertView == null) {
                v = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_activity_friend, parent, false);
                holder = new ViewHolder();
                holder.name =(TextView) v.findViewById(R.id.userText);
                holder.picture =(ImageView) v.findViewById(R.id.activityImage);
                holder.date = (TextView)v.findViewById(R.id.activityDate);
                holder.title = (TextView)v.findViewById(R.id.activityTitle);
                holder.userPicture = (ProfilePictureView)v.findViewById(R.id.userImage);
                holder.container = (RelativeLayout)v.findViewById(R.id.eventContainer);
                v.setTag(holder);
            } else {
                v = convertView;
                holder = (ViewHolder)v.getTag();
            }

            if (parseObject!=null){
                Log.e("Parse Object"," "+parseObject );
                String name = (String) parseObject.get("userFullName");
                if (name!=null){
                    holder.name.setText(name+" is interested in: ");
                }

                if (parseObject.get("createdDate")!=null){
                    Calendar cal = Calendar.getInstance();
                    cal.setTime((Date) parseObject.get("createdDate"));
                    holder.date.setText(" " + String.format("%1$ta %1$tb %1$td at %1$tI:%1$tM %1$Tp", cal));
                }

                if (parseObject.get("userFBId")!=null){
                    final String fbId= (String) parseObject.get("userFBId");
                    final String parseID =(String) parseObject.get("userParseId");
                    holder.userPicture.setProfileId(fbId);
                    holder.userPicture.setPresetSize(ProfilePictureView.SMALL);
                    holder.userPicture.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(getActivity(),ProfileActivity.class);
                            Log.e("FriendsList","PArseID"+parseID);
                            i.putExtra(ProfileFragment.EXTRA_PROFILE_ID, parseID);
                            i.putExtra(ProfileFragment.EXTRA_PROFILE_ID_FB,fbId);
                            startActivityForResult(i,0);
                        }
                    });
                }

                if (parseObject.get("eventName")!=null){
                    String title = (String) parseObject.get("eventName");
                    holder.title.setText(title);

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
    private static class ViewHolder {
        ProfilePictureView userPicture;
        ImageView picture;
        TextView name,date,title;
        RelativeLayout container;

    }


}

/*

 LoadFriendActivities loadFriendActivities = new LoadFriendActivities(mAdapter,new ArrayList<Map<String, String>>());
        loadFriendActivities.execute();
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                        currentPage++;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                    // I load the next page of gigs using a background task,
                    // but you can call any function here.
                    new LoadFriendActivities(mAdapter,new ArrayList<Map<String, String>>()).execute();
                    loading = true;
                }
            }
        });
 */

