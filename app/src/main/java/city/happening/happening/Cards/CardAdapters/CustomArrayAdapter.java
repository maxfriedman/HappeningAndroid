package city.happening.happening.Cards.CardAdapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import city.happening.happening.Cards.CardAdapters.HorizontalScroll.HorizontalListView;
import city.happening.happening.HappFromParse;
import city.happening.happening.ImageHelper;
import city.happening.happening.InterestedActivity;
import city.happening.happening.R;
import city.happening.happening.WebActivity;

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
    int startPriceNumLabel;
    int interestedCount;


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
        startPriceNumLabel = 0;
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
        Typeface face=Typeface.createFromAsset(mContext.getAssets(),
                "fonts/OpenSansRegular.ttf");
        if(convertView == null) {
            v = mInflater.inflate(R.layout.item, parent, false);
            holder = new ViewHolder();
            holder.avatar = (ImageView)v.findViewById(R.id.picture);
            holder.name = (TextView)v.findViewById(R.id.helloText);
            holder.name.setTypeface(face);
            holder.hashTag = (TextView)v.findViewById(R.id.hashTag);
            holder.hashTag.setTypeface(face);
            holder.location = (TextView)v.findViewById(R.id.descText);
            holder.location.setTypeface(face);
            holder.time = (TextView)v.findViewById(R.id.eventTime);
            holder.time.setTypeface(face);
            holder.facebookScroll = (HorizontalListView) v.findViewById(R.id.facebookScroll);
            holder.photoholder = (FrameLayout)v.findViewById(R.id.photoholder);
            holder.calendar = (ImageView)v.findViewById(R.id.calendar);
            holder.ticketButton = (Button)v.findViewById(R.id.ticketButton);
            holder.interested = (LinearLayout)v.findViewById(R.id.interestedClick);
            holder.interestednum = (TextView)v.findViewById(R.id.interestedParties);
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
            Log.e("ArrayAdapter","Title"+happening.getTitle());
            holder.avatar.setImageResource(happening.getDrawableResourceId());
            holder.avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if(happening.getImage()!=null){
                Bitmap temp = ImageHelper.getRoundedCornerBitmap(happening.getImage(), 10);
               // ImageHelper helper =  new ImageHelper();
               // Bitmap gradientMap = helper.addGradient(temp);
                holder.avatar.setImageBitmap(temp);
                // scaleImage(holder.avatar);
            }
            holder.hashTag.setText(happening.getHash());
            holder.name.setText(happening.getTitle());
            holder.location.setText("At " + happening.getLocation());
            Log.d("Adapter", "date" + happening.get("Date").toString());
            Calendar cal = Calendar.getInstance();
            cal.setTime((Date) happening.get("Date"));
            holder.time.setText(" " + String.format("%1$ta %1$tb %1$td at %1$tI:%1$tM %1$Tp", cal));
           // holder.time.setText(happening.get("Date").toString());
            interestedCount = (int)happening.get("swipesRight");
            holder.interestednum.setText(""+interestedCount);

            }
            final HappFromParse tempHap = happening;
            holder.interested.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (interestedCount!=0) {
                        Intent i = new Intent(mContext, InterestedActivity.class);
                        i.putExtra(InterestedActivity.EXTRA_EVENT_ID, tempHap.getObjectId());
                        mContext.startActivity(i);
                    }else {
                        makeToast(mContext,"Be The First to Swipe!!!");
                    }
                }
            });


            holder.calendar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (clicked==false){
                        clicked = true;

                        Calendar beginTime = Calendar.getInstance();
                        Date startDate = (Date)tempHap.get("Date");
                        beginTime.setTime(startDate);
                        Calendar endTime = Calendar.getInstance();
                        endTime.setTime((Date)tempHap.get("EndTime"));
                        Intent intent = new Intent(Intent.ACTION_INSERT)
                                .setData(CalendarContract.Events.CONTENT_URI)
                                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                                .putExtra(CalendarContract.Events.TITLE, tempHap.getTitle())
                                .putExtra(CalendarContract.Events.EVENT_LOCATION, tempHap.getLocation())
                                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
                        mContext.startActivity(intent);

                    }else {
                        makeToast(mContext,"Already Saved it!");

                    }

                   //Intent i = new Intent(mContext, CalendarUseQuestion.class);
                    //mContext.startActivity(i);
                }
            });

        String ticketLink = (String)tempHap.get("TicketLink");
        String url = (String)tempHap.get("URL");
        if (happening.get("lowest_price")!=null){

            startPriceNumLabel = 0;
            startPriceNumLabel =(int) happening.get("lowest_price");
        }

        if (ticketLink!=null&&(!ticketLink.equals("")||!ticketLink.equals("$0"))&&startPriceNumLabel!=0){
            if (ticketLink.contains("seatgeek.com")){
                if (startPriceNumLabel>=0){
                    String startingString = "GetTickets - Starting at "+startPriceNumLabel ;
                    holder.ticketButton.setText(startingString);



                }
            } else if (ticketLink.contains("facebook.com")){
                holder.ticketButton.setText("RSVP TO FACEBOOK EVENT");

            }else if (ticketLink.contains("meetup.com")){
                holder.ticketButton.setText("RSVP ON MEETUP.COM");

            }else if(((boolean)tempHap.get("isFreeEvent"))){
                holder.ticketButton.setText("THIS EVENT IS FREE");

            }
        }else if (url!=null &&(!url.equals("")||(!url.equals("$0")))){
            holder.ticketButton.setText("MORE INFO ON WEBSITE");
        }else {
            holder.ticketButton.setVisibility(View.GONE);
        }
        final String ticketLinkFinal =ticketLink;
        holder.ticketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ticketLinkFinal!=null){
                    Intent i = new Intent(mContext, WebActivity.class);
                    i.putExtra(WebActivity.EXTRA_URL_ID,ticketLinkFinal);
                    mContext.startActivity(i);

                }
            }
        });


        return v;
    }
    static void makeToast(Context ctx, String s){
        Toast.makeText(ctx, s, Toast.LENGTH_SHORT).show();
    }

    private class ViewHolder {
        public ImageView avatar,calendar;
        public TextView name, location,hashTag,time,interestednum;
        public LinearLayout interested;
        public FrameLayout photoholder;
        public Button ticketButton;
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
