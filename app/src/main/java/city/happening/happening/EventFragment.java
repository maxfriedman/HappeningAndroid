package city.happening.happening;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseGeoPoint;


public class EventFragment extends Fragment {


    public static final String EXTRA_EVENT_ID = "happening.EVENT_ID";

    TextView  mTitle;
    TextView mDesc;
    ImageView mPic;
    HappFromParse mEvent;
    LinearLayout mLinearLayout;
    private GoogleMap mMap;

    //String eventID = null;
    protected Location mLastLocation;


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

        return v;


    }

}
