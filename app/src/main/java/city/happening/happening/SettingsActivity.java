package city.happening.happening;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;

/**
 * Created by Alex on 7/30/2015.
 */
public class SettingsActivity extends Activity {

    private ParseUser mParseUser;
    private RadioButton mTodayButton;
    private RadioButton mTomorrowButton;
    private RadioButton mWeekendButton;
    private SeekBar mSeekBar;
    private TextView mRadius;
    private Spinner mCities;
    private Spinner mCatagories;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mTodayButton =(RadioButton) findViewById(R.id.todayButton);
        mTomorrowButton = (RadioButton) findViewById(R.id.tomorrowButton);
        mWeekendButton = (RadioButton) findViewById(R.id.weekendButton);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mRadius = (TextView) findViewById(R.id.radius);
        mCities = (Spinner) findViewById(R.id.location_spinner);
        mCatagories = (Spinner) findViewById(R.id.category_spinner);
        mParseUser = ParseUser.getCurrentUser();


        String limiter = (String) mParseUser.get("time");
        if (limiter.equals("today")){
            mTodayButton.setChecked(true);
        }else if (limiter.equals("tomorrow")){
            mTomorrowButton.setChecked(true);
        }else if (limiter.equals("this weekend")){
            mWeekendButton.setChecked(true);
        }

        mTodayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mParseUser.put("time", "today");
                mTomorrowButton.setChecked(false);
                mWeekendButton.setChecked(false);
                mParseUser.saveEventually();
            }
        });
        mTomorrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mParseUser.put("time", "tomorrow");
                mTodayButton.setChecked(false);
                mWeekendButton.setChecked(false);
                mParseUser.saveEventually();
            }
        });
        mWeekendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mParseUser.put("time", "this weekend");
                mTomorrowButton.setChecked(false);
                mTodayButton.setChecked(false);
                mParseUser.saveEventually();
            }
        });
        mSeekBar.setMax(50);
        //if (mParseUser.get("radius"))
        int radius =(int) mParseUser.get("radius");
        Log.e("Settings", "radius is " + radius);

        mSeekBar.setProgress(radius);


        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = mSeekBar.getProgress();
                mRadius.setText("" + progress + " mi. away");
                mParseUser.put("radius", progress);
                mParseUser.saveEventually(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.e("settings", "Radius saved");
                    }
                });
            }
        });

        mRadius.setText("" + mSeekBar.getProgress() + " mi. away");


        ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(this,R.array.cities,R.layout.dropdown_item);
        mCities.setAdapter(cityAdapter);
        mCities.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                char city = parent.getItemAtPosition(position).toString().charAt(0);
                switch (city) {
                    case 'W':
                        mParseUser.put("userLocTitle", "Washington, DC");
                        LatLng dc = new LatLng(38.907192, -77.036871);
                        mParseUser.saveEventually(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                Log.e("Settings","Saved DC");
                            }
                        });
                        break;
                    case 'B':
                        mParseUser.put("userLocTitle", "Boston, MA");
                        LatLng boston = new LatLng(42.358431, -71.059773);
                        mParseUser.saveEventually();
                        break;
                    case 'N':
                        mParseUser.put("userLocTitle", "Nashville, TN");
                        LatLng nash = new LatLng(36.162664, -86.781602);
                        mParseUser.saveEventually();
                        break;
                    case 'P':
                        mParseUser.put("userLocTitle", "Philadelphia, PA");
                        LatLng philly = new LatLng(39.952584, -75.165222);
                        mParseUser.saveEventually();
                        break;
                    case 'S':
                        mParseUser.put("userLocTitle", "San Francisco, CA");
                        LatLng sanfran = new LatLng(37.774929, -122.419416);
                        mParseUser.saveEventually();
                        break;
                }
                mCities.setSelection(position,true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter<CharSequence> catagoryAdapter = ArrayAdapter.createFromResource(this,R.array.categories,R.layout.dropdown_item);
        mCatagories.setAdapter(catagoryAdapter);
        mCatagories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mParseUser.put("categoryName", "Best Deals");
                        ArrayList<String> categories = new ArrayList<>();
                        categories.add("NightLife");
                        categories.add("Entertainment");
                        categories.add("Music");
                        categories.add("Dining");
                        categories.add("Happy Hour");
                        categories.add("Sports");
                        categories.add("Shopping");
                        categories.add("Fundraiser");
                        categories.add("Meetup");
                        categories.add("Freebies");
                        categories.add("Other");
                        categories.add(null);
                        mParseUser.put("categories", categories);
                        mParseUser.saveEventually();
                        break;
                    case 1:
                        mParseUser.put("categoryName", "Best Deals");
                        ArrayList<String> categoriesDeal = new ArrayList<>();
                        categoriesDeal.add("NightLife");
                        categoriesDeal.add("Entertainment");
                        categoriesDeal.add("Music");
                        categoriesDeal.add("Dining");
                        categoriesDeal.add("Happy Hour");
                        categoriesDeal.add("Sports");
                        categoriesDeal.add("Shopping");
                        categoriesDeal.add("Fundraiser");
                        categoriesDeal.add("Meetup");
                        categoriesDeal.add("Freebies");
                        categoriesDeal.add("Other");
                        categoriesDeal.add(null);
                        mParseUser.put("categories", categoriesDeal);
                        mParseUser.saveEventually();
                        break;
                    case 2:
                        mParseUser.put("categoryName", "Sports");
                        ArrayList<String> categoriesSports = new ArrayList<String>();
                        categoriesSports.add("Sports");
                        mParseUser.put("categories", categoriesSports);
                        mParseUser.saveEventually();
                        break;
                    case 3:
                        mParseUser.put("categoryName", "Bars & Clubs");
                        ArrayList<String> categoriesBars = new ArrayList<String>();
                        categoriesBars.add("Happy Hour");
                        categoriesBars.add("Nightlife");
                        mParseUser.put("categoryName", categoriesBars);
                        mParseUser.saveEventually();
                        break;
                    case 4:
                        mParseUser.put("categoryName", "Concerts & Shows");
                        ArrayList<String> categoriesShows = new ArrayList<String>();
                        categoriesShows.add("Entertainment");
                        categoriesShows.add("Music");
                        mParseUser.put("categoryName", categoriesShows);
                        mParseUser.saveEventually();
                        break;


                }
                mCatagories.setSelection(position,true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

}
