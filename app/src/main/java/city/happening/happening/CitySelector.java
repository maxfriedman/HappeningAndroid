package city.happening.happening;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseUser;

import city.happening.happening.LargeContainer.MyTabActivity;

/**
 * Created by Alex on 9/24/2015.
 */
public class CitySelector extends Activity {
    ListView mListView;
    ParseUser mParseUser;
    CityListAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cityselector);
        mListView =(ListView) findViewById(R.id.citylistview);
        mParseUser =ParseUser.getCurrentUser();
        mAdapter = new CityListAdapter(this,getResources().getStringArray(R.array.cities));
        mListView.setAdapter(mAdapter);
    }

    public class CityListAdapter extends BaseAdapter{
        private LayoutInflater mInflater;
        private String[] mCities;
        Context mContext;
        public CityListAdapter(Context context,String[] cities) {
            mInflater = LayoutInflater.from(context);
            mCities = cities;
            mContext = context;
        }

        @Override
        public int getCount() {
            return mCities.length;
        }

        @Override
        public String getItem(int position) {
            return mCities[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            ViewHolder holder;
            final int tempPosition = position;
            if(convertView == null) {
                v = mInflater.inflate(R.layout.city_list_item, parent, false);
                holder = new ViewHolder();
                holder.name =(TextView) v.findViewById(R.id.cityTextView);
                holder.picture =(ImageView) v.findViewById(R.id.cityImage);
                holder.container = (LinearLayout)v.findViewById(R.id.cityContainer);
                v.setTag(holder);
            } else {
                v = convertView;
                holder = (ViewHolder)v.getTag();
            }
            holder.name.setText(mCities[position]);
            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   mParseUser.put("userLocTitle",mCities[tempPosition]);
                    Intent i = new Intent(mContext, MyTabActivity.class);
                    startActivity(i);
                }
            });


            return v;
        }

        private class ViewHolder {
            ImageView picture;
            TextView name;
            LinearLayout container;
        }

    }
}
