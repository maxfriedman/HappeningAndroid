package city.happening.happening.ActivityFeed;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
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
import java.util.concurrent.TimeUnit;

import city.happening.happening.EventActivity;
import city.happening.happening.EventFragment;
import city.happening.happening.HappFromParse;
import city.happening.happening.HappeningLab;
import city.happening.happening.ProgressDialogFragment;
import city.happening.happening.R;

/**
 * Created by Alex on 9/29/2015.
 */
public class MeList extends Fragment {
    ActivityAdapter mAdapter;
    LayoutInflater mInflater;
    ListView mListView;
    private android.support.v4.app.DialogFragment mDialog;

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
                    tempId.put("parseId", id.get("parseId"));
                    Log.e("creatingId array", "" + tempId);
                    idsArray.add(id);
                }
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Activity");
                ParseQuery finalQuery = ParseQuery.getQuery("Activity");

                ParseQuery reminderQuery = ParseQuery.getQuery("Activity");
                reminderQuery.whereEqualTo("type","reminder");
                reminderQuery.whereEqualTo("userParseId",ParseUser.getCurrentUser().getObjectId());

                ParseQuery friendJoinedQuery = ParseQuery.getQuery("Activity");
                friendJoinedQuery.whereEqualTo("type", "friendJoined");
                friendJoinedQuery.whereContainedIn("userParseId", idsArray);

                List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
                queries.add(reminderQuery);
                queries.add(friendJoinedQuery);

                finalQuery = ParseQuery.or(queries);
                //finalQuery.include("eventObject");
                finalQuery.orderByDescending("createdDate");



                return finalQuery;

            }
        };

        mAdapter = new ActivityAdapter(getActivity(),factory);
        mListView.setAdapter(mAdapter);
        loadFromParse();



        return v;
    }
    private void loadFromParse(){

        ArrayList<Map<String, String>> friends = (ArrayList) ParseUser.getCurrentUser().get("friends");
        ArrayList<Map<String, String>> idsArray = new ArrayList();
        for (Map<String, String> id : friends) {
            Map<String, String> tempId = new HashMap<>();
            tempId.put("parseId", id.get("parseId"));
            Log.e("creatingId array", "" + tempId);
            idsArray.add(id);
        }
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Activity");
        ParseQuery finalQuery = ParseQuery.getQuery("Activity");

        ParseQuery reminderQuery = ParseQuery.getQuery("Activity");
        reminderQuery.whereEqualTo("type", "reminder");
        reminderQuery.whereEqualTo("userParseId", ParseUser.getCurrentUser().getObjectId());

        ParseQuery friendJoinedQuery = ParseQuery.getQuery("Activity");
        friendJoinedQuery.whereEqualTo("type", "friendJoined");
        friendJoinedQuery.whereContainedIn("userParseId", idsArray);

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(reminderQuery);
        queries.add(friendJoinedQuery);

        finalQuery = ParseQuery.or(queries);
        //finalQuery.include("eventObject");
        finalQuery.orderByDescending("createdDate");


        finalQuery.setLimit(20);
        if (mDialog == null)
            mDialog = new ProgressDialogFragment().newInstance("Loading Happenings");
        if (!mDialog.isAdded()) mDialog.show(getChildFragmentManager(), "Loading");


        finalQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List list, ParseException e) {
                Log.e("FriendActivityList", "Found " + list.size());
                if (list.size() != 0) {
                    for (int i = list.size() - 1; i >= 0; i--) {
                        Map<String, String> activityDict = new HashMap<>();
                        ParseObject object = (ParseObject) list.get(i);
                        int count = 0;
                        object.pinInBackground("MEActivityList");

                    }
                    mAdapter.loadObjects();
                    mAdapter.notifyDataSetChanged();
                    mDialog.dismiss();


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
            Typeface face=Typeface.createFromAsset(getContext().getAssets(),
                    "fonts/OpenSansRegular.ttf");
            Typeface facebold=Typeface.createFromAsset(getContext().getAssets(),
                    "fonts/OpenSansBold.ttf");
            Typeface faceLightItalic=Typeface.createFromAsset(getContext().getAssets(),
                    "fonts/OpenSansLightItalic.ttf");
            ParseQuery parseQuery = ParseQuery.getQuery("Event");
            HappFromParse happening = null;
            try{
                HappFromParse temp = (HappFromParse)parseQuery.get(eventId);
                if (temp.getHash()!=null){
                    temp.setDrawableResourceId(temp.getHash());
                    happening = temp;

                }else {
                    happening = temp;
                    happening.setDrawableResourceId(R.drawable.other);
                    Log.e("FriendActivity","Happ is new HAPP "+temp.getTitle() );
                }
                happening = temp;
            }catch (ParseException e){}
            final HappFromParse tempHapp = happening;
            View v;
            ViewHolder mHolder;
            if(convertView == null) {
                v = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_activity_me, parent, false);
                mHolder = new ViewHolder();
                mHolder.name =(TextView) v.findViewById(R.id.userText);
                mHolder.name.setTypeface(face);
                mHolder.userPicture = (ProfilePictureView)v.findViewById(R.id.userImage);
                mHolder.picture =(ImageView) v.findViewById(R.id.activityImage);
                mHolder.date = (TextView)v.findViewById(R.id.activityDate);
                mHolder.date.setTypeface(face);
                mHolder.title = (TextView)v.findViewById(R.id.activityTitle);
                mHolder.title.setTypeface(facebold);
                mHolder.calendar = (ImageView)v.findViewById(R.id.calendar);
                mHolder.container = (RelativeLayout)v.findViewById(R.id.eventContainer);
                v.setTag(mHolder);
            } else {
                v = convertView;
                mHolder = (ViewHolder)v.getTag();
            }
            if (parseObject!=null) {
                if (parseObject.get("type").equals("friendJoined")) {
                    mHolder.name.setText(((String)parseObject.get("userFullName"))+" Just joined Happening WOO!!!");


                    mHolder.userPicture.setProfileId(((String)parseObject.get("userFBId")));
                    mHolder.userPicture.setVisibility(View.VISIBLE);
                    mHolder.picture.setVisibility(View.GONE);
                    mHolder.date.setVisibility(View.GONE);
                    mHolder.title.setVisibility(View.GONE);
                    mHolder.calendar.setVisibility(View.GONE);
                }else{
                    mHolder.calendar.setVisibility(View.VISIBLE);
                    mHolder.picture.setVisibility(View.VISIBLE);
                    mHolder.date.setVisibility(View.VISIBLE);
                    mHolder.title.setVisibility(View.VISIBLE);
                   // holder.userPicture.setProfileId(((String)parseObject.get("userFBId")));
                    Date date = (Date)parseObject.get("eventDate");
                    Calendar cal2 = Calendar.getInstance();
                    cal2.setTime(date);
                    Calendar now = Calendar.getInstance();
                    long timeTillStart = TimeUnit.MILLISECONDS.toMinutes(cal2.getTimeInMillis()-now.getTimeInMillis());
                    Log.e("Activity","tillStart "+timeTillStart);
                    mHolder.name.setText(timeTillStart<=0?"The Event has ended: ":"Reminder Event Starts in " +timeTillStart+ " minutes");

                    if (parseObject.get("createdDate")!=null){
                        Calendar cal = Calendar.getInstance();
                        cal.setTime((Date) parseObject.get("createdDate"));
                        if (cal!=null){
                            mHolder.date.setText(" " + String.format("%1$ta %1$tb %1$td at %1$tI:%1$tM %1$Tp", cal));
                            mHolder.date.setTypeface(faceLightItalic);
                        }
                    }

                    if (parseObject.get("eventName")!=null){
                        String title = (String) parseObject.get("eventName");
                        if (title!=null)mHolder.title.setText(title);

                    }
                    mHolder.picture.setImageResource(happening.getDrawableResourceId());
                    mHolder.picture.setTag(happening.getObjectId());
                    new LoadImage(mHolder.picture,happening).execute();

                    mHolder.container.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            HappeningLab.get(getActivity()).addHappening(tempHapp);
                            Log.e("Me List", "Title " + tempHapp.getTitle());
                            Log.e("Me List","EventID "+eventId);
                            Intent i = new Intent(getActivity(), EventActivity.class);
                            i.putExtra(EventFragment.EXTRA_EVENT_ID, eventId);
                            i.putExtra(EventFragment.EXTRA_EVENT_BOOL, true);
                            startActivityForResult(i, 0);
                        }
                    });
                    mHolder.userPicture.setVisibility(View.GONE);

                    final HappFromParse tempHap = happening;
                    mHolder.calendar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Calendar beginTime = Calendar.getInstance();
                            Date startDate = (Date) tempHap.get("Date");
                            beginTime.setTime(startDate);
                            Calendar endTime = Calendar.getInstance();
                            endTime.setTime((Date) tempHap.get("EndTime"));
                            Intent intent = new Intent(Intent.ACTION_INSERT)
                                    .setData(CalendarContract.Events.CONTENT_URI)
                                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                                    .putExtra(CalendarContract.Events.TITLE, tempHap.getTitle())
                                    .putExtra(CalendarContract.Events.EVENT_LOCATION, tempHap.getLocation())
                                    .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
                            startActivity(intent);

                        }
                    });
                }
            }

            return v;

        }
    }
    private static class ViewHolder {
        ProfilePictureView userPicture;
        ImageView picture, calendar;
        TextView name,date,title;
        RelativeLayout container;
    }
    class LoadImage extends AsyncTask<Object, Void, Bitmap> {

        private ImageView imv;
        private String path;
        private HappFromParse mEvent;

        public LoadImage(ImageView imv, HappFromParse event) {
            this.imv = imv;
            this.path = imv.getTag().toString();
            this.mEvent = event;
        }

        @Override
        protected Bitmap doInBackground(Object... params) {
            Bitmap bitmap = mEvent.getImage();
            if (bitmap==null){
                bitmap = BitmapFactory.decodeResource(getResources(), mEvent.getDrawableResourceId());
            }
            return bitmap;
        }
        @Override
        protected void onPostExecute(Bitmap result) {
            if (!imv.getTag().toString().equals(path)) {
               /* The path is not same. This means that this
                  image view is handled by some other async task.
                  We don't do anything and return. */
                return;
            }

            if(result != null && imv != null){
                imv.setImageBitmap(result);
            }
        }

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
