package city.happening.happening.Profile;

import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import java.util.List;
import java.util.Map;

import city.happening.happening.EventActivity;
import city.happening.happening.EventFragment;
import city.happening.happening.FBScrollAdapter;
import city.happening.happening.HappFromParse;
import city.happening.happening.HappeningLab;
import city.happening.happening.HorizontalListView;
import city.happening.happening.ProgressDialogFragment;
import city.happening.happening.R;

/**
 * Created by Alex on 9/3/2015.
 */
public class EventsList extends Fragment {

    private ParseQueryAdapter<ParseObject> mEventListAdapter;
    private LayoutInflater mInflater;
    private ParseUser mParseUser;
    private ListView mListView;
    private ArrayList<HappFromParse>mHappenings;
    private DialogFragment mDialog;
    ArrayList<Map<String,String>> mFriends;


    private String mParseID, mFaceBookId;
    public static final String EXTRA_PROFILE_ID = "happening.PROFILE_ID";
    public static final String EXTRA_PROFILE_ID_FB = "happening.PROFILE_ID_FB";

    public static EventsList newInstance(String userId, String idFB){
        Log.e("EventsList", "Id " + userId);
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_PROFILE_ID, userId);
        args.putSerializable(EXTRA_PROFILE_ID_FB, idFB);
        EventsList fragment = new EventsList();
        fragment.setArguments(args);

        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHappenings = new ArrayList<>();
        setHasOptionsMenu(true);
        Log.e("EventsList", "Created");
        mInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mParseID = (String)getArguments().getSerializable(EXTRA_PROFILE_ID);
        mFaceBookId = (String)getArguments().getSerializable(EXTRA_PROFILE_ID_FB);

        Log.e("Eventslist oncreate", "Id " + mParseID);

        ParseQuery<ParseUser> query= ParseUser.getQuery() ;
        query.whereEqualTo("objectId", mParseID);
        try {
            mParseUser = query.getFirst();
            Log.e("FriendProfile","user"+mParseUser);
            mFriends =(ArrayList<Map<String,String>>) mParseUser.get("friends");
            Log.e("EventsLst","FriendList "+mFriends.size());
        }catch (ParseException e){
            Log.e("FriendProfile","exception" +e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        Log.e("EventsList", "oncreateView");
        View v = inflater.inflate(R.layout.fragmentwithlist, container, false);
        try{
            HappFromParse.unpinAll();
        }catch (ParseException e){
            Log.e("FriendProfile","exception" +e);
        }

        mListView =(ListView) v.findViewById(R.id.listView);
        mInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ParseQueryAdapter.QueryFactory<ParseObject> factory = new ParseQueryAdapter.QueryFactory<ParseObject>() {
            public ParseQuery<ParseObject> create() {

                Calendar rightNow = Calendar.getInstance();
                Date today = new Date(rightNow.getTimeInMillis());
                ParseQuery eventQuery = ParseQuery.getQuery("Event");
                eventQuery.fromPin(mParseID);
                eventQuery.whereGreaterThan("EndTime", today);
                eventQuery.orderByAscending("Date");
                eventQuery.setLimit(1000);
                return eventQuery;

            }
        };
        mEventListAdapter = new EventListAdapter(getActivity(),factory);
        mListView.setAdapter(mEventListAdapter);


        View header = inflater.inflate(R.layout.header,null);
        ProfilePictureView pictureView = (ProfilePictureView)header.findViewById(R.id.profilePicture);
        pictureView.setProfileId(mFaceBookId);
        pictureView.setPresetSize(ProfilePictureView.CUSTOM);
        Typeface face=Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/OpenSansRegular.ttf");
        TextView name = (TextView) header.findViewById(R.id.userName);
        name.setTypeface(face);
        name.setText((String) mParseUser.get("firstName") + " " + (String) mParseUser.get("lastName"));
        mListView.addHeaderView(header);
        loadFromParse();
        mListView.invalidateViews();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        //mEventListAdapter.notifyDataSetChanged();

    }

    private void loadFromParse(){
        Log.e("EventsList", "LoadfromParse");

        //HappFromParse.unpinAll();
        Calendar rightNow = Calendar.getInstance();
        Date today = new Date(rightNow.getTimeInMillis());
        ParseQuery<ParseObject> swipesQuery = ParseQuery.getQuery("Swipes");
        swipesQuery.whereEqualTo("UserID",mParseID);
        Log.e("Events", "userid" + mParseID);
        swipesQuery.whereEqualTo("swipedRight", true);
        //swipesQuery.fromLocalDatastore();
        swipesQuery.setLimit(1000);

        ParseQuery eventQuery = ParseQuery.getQuery("Event");
       // eventQuery.fromLocalDatastore();
        eventQuery.whereMatchesKeyInQuery("objectId", "EventID", swipesQuery);
        eventQuery.whereGreaterThan("EndTime", today);
        eventQuery.orderByAscending("Date");
        eventQuery.setLimit(1000);
        if (mDialog == null)
            mDialog = new ProgressDialogFragment().newInstance("Loading Happenings");
        if (!mDialog.isAdded()) mDialog.show(getChildFragmentManager(), "Loading");

        eventQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List list, ParseException e) {
                if(e==null){
                    Log.e("EventsList", "list"+list.size());
                    for (int i = 0; i < list.size(); i++) {

                        HappFromParse temp = (HappFromParse) list.get(i);
                        temp.setDrawableResourceId(temp.getHash());
                        Log.e("Profile", " " +temp.getTitle());
                        temp.pinInBackground(mParseID);


                    }
                    mEventListAdapter.loadObjects();
                    mEventListAdapter.notifyDataSetChanged();
                    mDialog.dismiss();



                }else {
                    Log.e("EventsList", "error"+e);
                }
            }

        });

    }
    private class EventListAdapter extends ParseQueryAdapter<ParseObject> {

        public EventListAdapter(Context context,ParseQueryAdapter.QueryFactory<ParseObject> queryFactory) {
            super(context, queryFactory);
        }

        @Override
        public ParseObject getItem(int index) {
            return super.getItem(index);
        }

        @Override
        public View getItemView( ParseObject parseObject,View convertView,ViewGroup parent){
            HappFromParse happening =(HappFromParse) parseObject;
            happening.setDrawableResourceId(happening.getHash());
            final HappFromParse tempHapp = happening;
            Typeface face=Typeface.createFromAsset(getContext().getAssets(),
                    "fonts/OpenSansRegular.ttf");
            ViewHolder mHolder;
            if(convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_event,parent,false );
                mHolder = new ViewHolder();
                mHolder.avatar = (ImageView)convertView.findViewById(R.id.eventImage);
                mHolder.name = (TextView)convertView.findViewById(R.id.event_title);
                mHolder.name.setTypeface(face);
                mHolder.hashTag = (TextView)convertView.findViewById(R.id.hashTag);
     //           mHolder.hashTag.setTypeface(face);
                mHolder.location = (TextView)convertView.findViewById(R.id.eventLocation);
                mHolder.location.setTypeface(face);
                mHolder.time = (TextView)convertView.findViewById(R.id.eventTime);
                mHolder.time.setTypeface(face);
                mHolder.item = (LinearLayout)convertView.findViewById(R.id.eventCard);

                mHolder.facebookscroll = (HorizontalListView) convertView.findViewById(R.id.facebookScroll);
                convertView.setTag(mHolder);
            } else {
                mHolder = (ViewHolder)convertView.getTag();
            }
           // mParseUser = ParseUser.getCurrentUser();

            if (happening!=null){

                mHolder.avatar.setImageResource(happening.getDrawableResourceId());
                mHolder.avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
                mHolder.avatar.setTag(happening.getObjectId());
                new LoadImage(mHolder.avatar,happening).execute();

//              holder.hashTag.setText(happening.getHash());
                mHolder.name.setText(happening.getTitle());
                mHolder.location.setText("At " + happening.getLocation());
                if (happening.get("Date")!=null){
                    Calendar cal = Calendar.getInstance();
                    cal.setTime((Date) happening.get("Date"));
                    mHolder.time.setText( " " + String.format("%1$ta %1$tb %1$td at %1$tI:%1$tM %1$Tp", cal));
                }
                mFriends =(ArrayList<Map<String,String>>) mParseUser.get("friends");

                mHolder.facebookscroll.setTag(happening.getObjectId());
                new LoadScroll(mHolder.facebookscroll,happening.getObjectId()).execute();




                mHolder.item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        HappFromParse h = (HappFromParse) tempHapp;
                        HappeningLab.get(getActivity()).addHappening(h);
                        Intent i = new Intent(getActivity(), EventActivity.class);
                        i.putExtra(EventFragment.EXTRA_EVENT_ID, h.getObjectId());
                        i.putExtra(EventFragment.EXTRA_EVENT_BOOL, true);
                        startActivityForResult(i,0);
                    }
                });


            }


            return convertView;
        }
    }
    private static class ViewHolder {
        public ImageView avatar;
        public TextView name, location,hashTag,time;
        public LinearLayout item;
        public HorizontalListView facebookscroll;

    }
    class LoadImage extends AsyncTask<Object, Void, Bitmap>{

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
                bitmap = BitmapFactory.decodeResource(getResources(),mEvent.getDrawableResourceId());
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
    class LoadScroll extends AsyncTask<Object, Void, FBScrollAdapter>{

        private HorizontalListView imv;
        private String path;
        private String mEvent;

        public LoadScroll(HorizontalListView imv, String event) {
            this.imv = imv;
            this.path = imv.getTag().toString();
            this.mEvent = event;
        }

        @Override
        protected FBScrollAdapter doInBackground(Object... params) {
            ArrayList<String> interestedFBiDs = new ArrayList<>();
            ParseQuery swipes = ParseQuery.getQuery("Swipes");
            swipes.whereEqualTo("EventID",mEvent);
            FBScrollAdapter adapter = new FBScrollAdapter(getActivity(),interestedFBiDs);
            try {
                ArrayList<ParseObject> tempSwipeList =(ArrayList<ParseObject>) swipes.find();
                for (int i=0;i<tempSwipeList.size();i++){
                    if (tempSwipeList.get(i).get("FBObjectID")!=null)interestedFBiDs.add((String)tempSwipeList.get(i).get("FBObjectID"));
                }
                adapter.notifyDataSetChanged();
            }catch (ParseException e){}
            return adapter;
        }
        @Override
        protected void onPostExecute(FBScrollAdapter result) {
            if (!imv.getTag().toString().equals(path)) {
               /* The path is not same. This means that this
                  image view is handled by some other async task.
                  We don't do anything and return. */
                return;
            }

            if(result != null && imv != null){
                imv.setAdapter(result);
            }
        }

    }
    public class FaceBookScrollAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<String> mHappenings = new ArrayList<>();
        private Context mContext;


        public FaceBookScrollAdapter(Context context,ArrayList<String> events) {
            mInflater = LayoutInflater.from(context);
            mHappenings = events;
            mContext = context;
        }

        @Override
        public int getCount() {
            return mHappenings.size();
        }

        @Override
        public String getItem(int position) {
            return mHappenings.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            ViewHolder holder;
            final String friendID = getItem(position);


            if(convertView == null) {
                v = mInflater.inflate(R.layout.list_item_friend_scroll, parent, false);
                holder = new ViewHolder();
                holder.image = (ProfilePictureView)v.findViewById(R.id.friendPic);
                holder.name = (TextView)v.findViewById(R.id.friendname);

                v.setTag(holder);
            } else {
                v = convertView;
                holder = (ViewHolder)v.getTag();
            }
            Log.d("Friendlist", "");


            holder.image.setProfileId(friendID);
            holder.image.setPresetSize(ProfilePictureView.SMALL);
            ParseQuery friendUser = ParseUser.getQuery();
            friendUser.whereEqualTo("FBObjectID", friendID);
            String parseId = null;
            try {
                ParseUser temp = (ParseUser)friendUser.getFirst();
                parseId =(String) temp.getObjectId();
                holder.name.setText((String)temp.get("firstName"));
            }catch (ParseException e){

            }
            final String parseID =parseId;
            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent i = new Intent(mContext,ProfileActivity.class);
                    //Log.e("FriendsList","PArseID"+parseID);
                    if (parseID != null) i.putExtra(ProfileFragment.EXTRA_PROFILE_ID, parseID);
                    i.putExtra(ProfileFragment.EXTRA_PROFILE_ID_FB, friendID);
                    mContext.startActivity(i);

                }
            });




            return  v;
        }

        private class ViewHolder {
            public ProfilePictureView image;
            public TextView name;


        }
    }



}
