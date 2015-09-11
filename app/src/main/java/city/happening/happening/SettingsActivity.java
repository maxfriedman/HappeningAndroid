package city.happening.happening;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.parse.ParseUser;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mTodayButton =(RadioButton) findViewById(R.id.todayButton);
        mTomorrowButton = (RadioButton) findViewById(R.id.tomorrowButton);
        mWeekendButton = (RadioButton) findViewById(R.id.weekendButton);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mRadius = (TextView) findViewById(R.id.radius);
        mParseUser = ParseUser.getCurrentUser();

        mTodayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mParseUser.put("time", "today");
                mTomorrowButton.setChecked(false);
                mWeekendButton.setChecked(false);
            }
        });
        mTomorrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mParseUser.put("time", "tomorrow");
                mTodayButton.setChecked(false);
                mWeekendButton.setChecked(false);
            }
        });
        mWeekendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mParseUser.put("time", "this weekend");
                mTomorrowButton.setChecked(false);
                mTodayButton.setChecked(false);
            }
        });
        mSeekBar.setMax(50);
        mSeekBar.setProgress(mSeekBar.getMax());
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
                mRadius.setText(""+progress+" mi. away");

            }
        });

        mRadius.setText(""+ mSeekBar.getProgress()+" mi. away");




    }
}
