package city.happening.happening;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.parse.GetDataCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;

/**
 * Created by Alex on 7/15/2015.
 */
@ParseClassName("Event")
public class HappFromParse extends ParseObject {
    private int drawableResourceId;
    Bitmap mImage;
    public String getTitle(){
        return getString("Title");
    }
    public void setTitle(String title){
        put("Title",title);
    }
    public Date getEndTime(){
        return getDate("EndTime");
    }
    public void setEndTime(Date endTime){
        put("EndTime",endTime);
    }
    public ParseGeoPoint getGeo(){
        return getParseGeoPoint("GeoLoc");
    }
    public void setGeo(ParseGeoPoint parseGeoPoint){
        put("GeoLoc",parseGeoPoint);
    }
    public String getHash(){
        return getString("Hashtag");
    }
    public void setHash(String hash){
        put("Hashtag", hash);
    }
    public Bitmap getImage(){

        ParseFile fileObject = getParseFile("Image");
        if(fileObject!=null){
            fileObject.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException e) {
                    if (e == null) {
                        Log.d("test", "We've got data in data.");
                        // use data for something
                        BitmapFactory.Options options=new BitmapFactory.Options();// Create object of bitmapfactory's option method for further option use
                        options.inSampleSize = 2;
                        mImage = BitmapFactory.decodeByteArray(data, 0, data.length,options);

                    } else {
                        Log.e("test", "There was a problem downloading the data.");
                    }
                }


            });
        }/*else if(fileObject==null&&getHash()!=null){
            int id;
            if(getHash().equalsIgnoreCase("dining")){
                id = (R.drawable.dining);
            }else if(getHash().equalsIgnoreCase("fundraiser")){
                id =(R.drawable.fundraiser);
            }else if(getHash().equalsIgnoreCase("Happy Hour")){
                id =(R.drawable.happy_hour);
            }else if(getHash().equalsIgnoreCase("music")){
                id =(R.drawable.music);
            }else if(getHash().equalsIgnoreCase("NightLife")){
                id =(R.drawable.nightlife);
            }else if(getHash().equalsIgnoreCase("shopping")){
                id= (R.drawable.shopping);
            }else if(getHash().equalsIgnoreCase("sports")){
                id = (R.drawable.sports);
            }else if(getHash().equalsIgnoreCase("Entertainment")){
                id =(R.drawable.entertainment);
            }else if(getHash().equalsIgnoreCase("Bar Club")){
                id =(R.drawable.bar_club);
            }else{
                id = R.drawable.other;
            }
            mImage = BitmapFactory.decodeResource(Resources.getSystem(),id);*/
        else{
            mImage=null;

        }

        return mImage;

    }

    public void setImage(ParseFile image){
        if(image!=null){
            image.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException e) {
                    if (e == null) {
                        Log.d("test", "We've got data in data.");
                        // use data for something
                        mImage = BitmapFactory.decodeByteArray(data, 0, data.length);

                    } else {
                        Log.e("test", "There was a problem downloading the data.");
                    }
                }


            });
        }else{
            mImage = null;
        }

    }

    public void setDrawableResourceId(String Hash) {
        if(getHash().equalsIgnoreCase("dining")){
            drawableResourceId = (R.drawable.dining);
        }else if(getHash().equalsIgnoreCase("fundraiser")){
            drawableResourceId =(R.drawable.fundraiser);
        }else if(getHash().equalsIgnoreCase("Happy Hour")){
            drawableResourceId =(R.drawable.happy_hour);
        }else if(getHash().equalsIgnoreCase("music")){
            drawableResourceId =(R.drawable.music);
        }else if(getHash().equalsIgnoreCase("NightLife")){
            drawableResourceId =(R.drawable.nightlife);
        }else if(getHash().equalsIgnoreCase("shopping")){
            drawableResourceId = (R.drawable.shopping);
        }else if(getHash().equalsIgnoreCase("sports")){
            drawableResourceId = (R.drawable.sports);
        }else if(getHash().equalsIgnoreCase("Entertainment")){
            drawableResourceId =(R.drawable.entertainment);
        }else if(getHash().equalsIgnoreCase("Bar Club")){
            drawableResourceId =(R.drawable.bar_club);
        }else{
            drawableResourceId = R.drawable.other;
        }

    }
    public void setDrawableResourceId(int id){
        drawableResourceId = id;
    }


    public int getDrawableResourceId() {

        return drawableResourceId;
    }

    public String getLocation(){
        return getString("Location");
    }
    public void setLocation(String location){
        put("Location",location);
    }
    public String getDescription(){
        return getString("Description");
    }
    public void setDescription(String description){
        put("Description",description);
    }

    public static ParseQuery<HappFromParse> getQuery() {
        return ParseQuery.getQuery(HappFromParse.class);
    }


}
