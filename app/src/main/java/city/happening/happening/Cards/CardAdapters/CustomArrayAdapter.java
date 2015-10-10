package city.happening.happening.Cards.CardAdapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import city.happening.happening.Cards.CardAdapters.HorizontalScroll.HorizontalListView;
import city.happening.happening.HappFromParse;
import city.happening.happening.ImageHelper;
import city.happening.happening.R;

/**
 * Created by Alex on 6/23/2015.
 */
public class CustomArrayAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<HappFromParse> mHappenings = new ArrayList<>();
    private ParseUser mParseUser;
    private Context mContext;
    com.google.api.services.calendar.Calendar mService;
    GoogleAccountCredential mCredential;
    private Bitmap mBitmap;
    Boolean clicked = false;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR };

    public CustomArrayAdapter(Context context,ArrayList<HappFromParse> events, Activity activity) {
        mInflater = LayoutInflater.from(context);
        mHappenings = events;
        mContext = context;
        SharedPreferences settings = activity.getPreferences(Context.MODE_PRIVATE);
        mCredential = GoogleAccountCredential.usingOAuth2(
                mContext, Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
    }

    @Override
    public int getCount() {
        return mHappenings.size();
    }

    @Override
    public HappFromParse getItem(int position) {
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
        if(convertView == null) {
            v = mInflater.inflate(R.layout.item, parent, false);
            holder = new ViewHolder();
            holder.avatar = (ImageView)v.findViewById(R.id.picture);
            holder.name = (TextView)v.findViewById(R.id.helloText);
            holder.hashTag = (TextView)v.findViewById(R.id.hashTag);
            holder.location = (TextView)v.findViewById(R.id.descText);
            holder.time = (TextView)v.findViewById(R.id.eventTime);
            holder.facebookScroll = (HorizontalListView) v.findViewById(R.id.facebookScroll);
            holder.photoholder = (FrameLayout)v.findViewById(R.id.photoholder);
            holder.calendar = (ImageView)v.findViewById(R.id.calendar);
            v.setTag(holder);
        } else {
            v = convertView;
            holder = (ViewHolder)v.getTag();
        }
        mParseUser = ParseUser.getCurrentUser();
        ArrayList<Map<String,String>> friendsList =getFriendsList();
        Log.d("ArrayAdapter","friends"+friendsList.size());
        HappFromParse happening = getItem(position);
        if (happening!=null){
            holder.avatar.setImageResource(happening.getDrawableResourceId());
            holder.avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if(happening.getImage()!=null){
                Bitmap temp = ImageHelper.getRoundedCornerBitmap(happening.getImage(), 10);
                ImageHelper helper =  new ImageHelper();
                Bitmap gradientMap = helper.addGradient(temp);
                holder.avatar.setImageBitmap(gradientMap);
                // scaleImage(holder.avatar);
            }
            holder.hashTag.setText(happening.getHash());
            holder.name.setText(happening.getTitle());
            holder.location.setText("At " + happening.getLocation());
            Log.d("Adapter", "date" + happening.get("Date").toString());
            Calendar cal = Calendar.getInstance();
            cal.setTime((Date) happening.get("Date"));
            holder.time.setText( " " + String.format("%1$ta %1$tb %1$td at %1$tI:%1$tM %1$Tp", cal));
           // holder.time.setText(happening.get("Date").toString());


            }
            if(mParseUser.get("friends")!=null){
                Log.d("Horz scroll", "" + friendsList.size());
              //  holder.facebookScroll.setAdapter(new FBScrollAdapter(mContext,friendsList));

            }

            final HappFromParse tempHap = happening;

            holder.calendar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (clicked==false){
                        clicked = true;
                        HttpTransport transport = AndroidHttp.newCompatibleTransport();
                        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
                        mService = new com.google.api.services.calendar.Calendar.Builder(transport, jsonFactory, mCredential)
                                .setApplicationName("Google Calendar API Android Quickstart")
                                .build();
                        Event event = new Event();
                        event.setSummary("Test Event").setLocation(tempHap.getLocation());
                        Date startDate = (Date)tempHap.get("Date");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US);
                        DateTime startDateTime =new DateTime(startDate);
                        EventDateTime start = new EventDateTime().setDateTime(startDateTime);
                        event.setStart(start);
                        DateTime endDateTime = new DateTime(((Date)tempHap.get("EndTime")));
                        EventDateTime end = new EventDateTime().setDateTime(endDateTime);
                        event.setEnd(end);
                        String calendarId = "primary";
                        try {
                            event = mService.events().insert(calendarId, event).execute();
                            makeToast(mContext,"Saved to your calendar!");
                        }catch (Exception e){

                        }

                    }else {
                        makeToast(mContext,"Already Saved it!");

                    }

                   //Intent i = new Intent(mContext, MainActivity.class);
                    //mContext.startActivity(i);
                }
            });




        return v;
    }
    static void makeToast(Context ctx, String s){
        Toast.makeText(ctx, s, Toast.LENGTH_SHORT).show();
    }

    private class ViewHolder {
        public ImageView avatar,calendar;
        public TextView name, location,hashTag,time;
        //public LinearLayout facebookScroll;
        public FrameLayout photoholder;
        public HorizontalListView facebookScroll;

    }

    private ArrayList<Map<String,String>> getFriendsList(){
        ArrayList<Map<String,String>> tempList = new ArrayList<>();
       if (mParseUser.get("friends")!=null){
           tempList =(ArrayList) mParseUser.get("friends");
           Log.d("ArrayAdapter"," "+tempList.size());
       }


        return tempList;
    }




}
