package city.happening.happening.Profile;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import java.util.Calendar;
import java.util.List;

import city.happening.happening.EventActivity;
import city.happening.happening.EventFragment;
import city.happening.happening.HappFromParse;
import city.happening.happening.HappeningLab;
import city.happening.happening.R;

/**
 * Created by Alex on 9/3/2015.
 */
public class CreatedByMeList extends Fragment {
    private ParseQueryAdapter<HappFromParse> mEventListAdapter;
    private LayoutInflater mInflater;
    private ParseUser mParseUser;
    private ListView mListView;

    public static CreatedByMeList newInstance() {

        CreatedByMeList f = new CreatedByMeList();
        Bundle b = new Bundle();
        f.setArguments(b);

        return f;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragmentwithlist,container,false);
        mListView =(ListView) v.findViewById(R.id.listView);
        mInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ParseQueryAdapter.QueryFactory<HappFromParse> factory = new ParseQueryAdapter.QueryFactory<HappFromParse>() {
            public ParseQuery<HappFromParse> create() {
                Calendar rightNow = Calendar.getInstance();
                ParseQuery<HappFromParse> query = HappFromParse.getQuery();
                rightNow.add(Calendar.MINUTE,-30);
                query.whereGreaterThan("EndTime",rightNow);
                query.orderByAscending("EndTime");
                query.fromLocalDatastore();
                return query;
            }
        };
        mEventListAdapter = new EventListAdapter(getActivity(),factory);
        mListView.setAdapter(mEventListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HappFromParse happFromParse =(HappFromParse) mEventListAdapter.getItem(position);
                happFromParse.setDrawableResourceId(happFromParse.getHash());
                HappeningLab.get(getActivity()).addHappening(happFromParse);
                Intent i = new Intent(getActivity(), EventActivity.class);
                i.putExtra(EventFragment.EXTRA_EVENT_ID, happFromParse.getObjectId());
                startActivityForResult(i, 0);
            }
        });
        loadFromParse();


        return v;
    }



    private void loadFromParse(){
        Calendar rightNow = Calendar.getInstance();
        ParseQuery<ParseObject> swipesQuery = ParseQuery.getQuery("Swipes");
        swipesQuery.whereEqualTo("UserID", ParseUser.getCurrentUser().getObjectId());
        swipesQuery.whereEqualTo("swipesRight", true);
        //swipesQuery.fromLocalDatastore();
        swipesQuery.setLimit(1000);

        ParseQuery eventQuery = ParseQuery.getQuery("Event");
        // eventQuery.fromLocalDatastore();
        eventQuery.whereMatchesKeyInQuery("objectId", "EventID", swipesQuery);
        eventQuery.whereGreaterThan("EndTime", rightNow);
        eventQuery.orderByAscending("Date");
        eventQuery.setLimit(1000);

        eventQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List list, ParseException e) {
                if(e==null){

                    for (int i = 0; i < list.size(); i++) {
                        Log.e("Profile", " " + list.get(i));
                        HappFromParse temp = (HappFromParse) list.get(i);
                        temp.setDrawableResourceId(temp.getHash());
                        temp.pinInBackground();

                    }
                    mEventListAdapter.loadObjects();


                }
            }

        });

    }
    private class EventListAdapter extends ParseQueryAdapter<HappFromParse> {
        public EventListAdapter(Context context,ParseQueryAdapter.QueryFactory<HappFromParse> queryFactory) {
            super(context, queryFactory);
        }

        @Override
        public View getItemView(HappFromParse happening,View convertView,ViewGroup parent){
            ViewHolder holder;
            if(convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_event,parent,false );
                holder = new ViewHolder();
                holder.avatar = (ImageView)convertView.findViewById(R.id.eventImage);
                holder.name = (TextView)convertView.findViewById(R.id.event_title);
                holder.hashTag = (TextView)convertView.findViewById(R.id.hashTag);
                holder.location = (TextView)convertView.findViewById(R.id.eventLocation);
                holder.time = (TextView)convertView.findViewById(R.id.eventTime);
                // holder.facebookScroll = (LinearLayout) convertView.findViewById(R.id.facebookScroll);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            mParseUser = ParseUser.getCurrentUser();
            //  ArrayList<Map<String,String>> friendsList =getFriendsList();

            holder.avatar.setImageResource(happening.getDrawableResourceId());
            holder.avatar.setScaleType(ImageView.ScaleType.FIT_XY);
            if(happening.getImage()!=null){
                holder.avatar.setImageBitmap(happening.getImage());
                // scaleImage(holder.avatar);
            }
//            holder.hashTag.setText(happening.getHash());
            holder.name.setText(happening.getTitle());
            holder.location.setText("At " + happening.getLocation());
            if (happening.get("Date")!=null)holder.time.setText(happening.get("Date").toString());


            return convertView;
        }
    }
    private static class ViewHolder {
        public ImageView avatar;
        public TextView name, location,hashTag,time;
        public LinearLayout facebookScroll;

    }


}
