package city.happening.happening;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseGeoPoint;
import com.usebutton.sdk.Button;
import com.usebutton.sdk.ButtonDropin;
import com.usebutton.sdk.PlacementContext;
import com.usebutton.sdk.util.LocationProvider;

import city.happening.happening.Cards.CardFrag;


public class EventFragment extends Fragment {


    public static final String EXTRA_EVENT_ID = "happening.EVENT_ID";

    TextView  mTitle;
    TextView mDesc;
    ImageView mPic;
    HappFromParse mEvent;
    LinearLayout mLinearLayout;
    private GoogleMap mMap;
    android.widget.Button mInterested;
    android.widget.Button mGoing;
    android.widget.Button mNotInterested;
    //String eventID = null;



    public static EventFragment newInstance(String eventId){
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_EVENT_ID, eventId);
        EventFragment fragment = new EventFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        String eventId = (String)getArguments().getSerializable(EXTRA_EVENT_ID);
        mEvent = HappeningLab.get(getActivity()).getHappening(eventId) ;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_event,parent,false);

        mEvent.setDrawableResourceId(mEvent.getHash());
        mPic = (ImageView)v.findViewById(R.id.eventImage);
        mPic.setImageResource(mEvent.getDrawableResourceId());
        if(mEvent.getImage()!=null) mPic.setImageBitmap(mEvent.getImage());
        mTitle =(TextView) v.findViewById(R.id.title_event);
        mTitle.setText(mEvent.getTitle());
        mDesc =( TextView) v.findViewById(R.id.description_event);
        mDesc.setText(mEvent.getDescription());
        mDesc.setMovementMethod(ScrollingMovementMethod.getInstance());
        mGoing = (android.widget.Button)v.findViewById(R.id.goingButton);
        mNotInterested =(android.widget.Button)v.findViewById(R.id.notInterestedButton);
        mInterested = (android.widget.Button)v.findViewById(R.id.interestedButton);
        mInterested.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "right";
                Intent i = new Intent();
                i.putExtra("MESSAGE", message);
                getActivity().setResult(CardFrag.BUTTON_CLICK_REQUEST, i);
                getActivity().finish();
            }
        });
        mNotInterested.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "left";
                Intent i = new Intent();
                i.putExtra("MESSAGE", message);
                getActivity().setResult(CardFrag.BUTTON_CLICK_REQUEST, i);
                getActivity().finish();
            }
        });
        mGoing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "down";
                Intent i = new Intent();
                i.putExtra("MESSAGE",message);
                getActivity().setResult(CardFrag.BUTTON_CLICK_REQUEST, i);
                getActivity().finish();
            }
        });
        mLinearLayout = (LinearLayout)v.findViewById(R.id.layout);
        mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        ParseGeoPoint location = mEvent.getGeo();
        Log.d("EventFragment",""+location);

        LatLng coordinates = new LatLng(location.getLatitude(),location.getLongitude());


        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());

        if (status == ConnectionResult.SUCCESS) {
            {
                FragmentManager fm = getChildFragmentManager();
                MyMapFragment mMapFragment = MyMapFragment.newInstance(coordinates);
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.add(R.id.my_map_fragment, mMapFragment);
                fragmentTransaction.commit();
                fm.executePendingTransactions();
            }
        }

        // Tell Button SDK about any inbound deeplinks we got from Intent
        Button.getButton(getActivity()).handleIntent(getActivity().getIntent());

        // Get the Button View
        final ButtonDropin buttonDropin = (ButtonDropin) v.findViewById(R.id.main_dropin);


        // Create a PlacementContext for the location you want a ride to.
        final PlacementContext context = PlacementContext.forEndLocation(mEvent.getLocation(),coordinates.latitude ,coordinates.longitude);
        final Location bestLocation = new LocationProvider(getActivity()).getBestLocation();
        if (bestLocation != null) {
            context.withStartLocation(null, bestLocation);
        }

        // Prepare the Button for display with our context
        buttonDropin.prepareForDisplayWithContext(context);
        buttonDropin.prepareForDisplayWithContext(context, new ButtonDropin.Listener() {
            @Override
            public void onPrepared(final boolean willDisplay) {
                // Toggle visibility of UI items here if necessary
                Toast.makeText(getActivity(),
                        String.format("Button %s.", willDisplay ? "available" : "unavailable"),
                        Toast.LENGTH_LONG).show();
            }
        });



        return v;


    }

}
