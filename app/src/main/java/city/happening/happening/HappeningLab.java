package city.happening.happening;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Alex on 6/24/2015.
 */
public class HappeningLab {
    private ArrayList<HappFromParse> mHappenings;
    private static HappeningLab sHappeningLab;
    private Context mAppContext;



    private HappeningLab(Context appContext){
        mAppContext=appContext;
        mHappenings=new ArrayList<>();
    }
    public static HappeningLab get(Context c){
        if(sHappeningLab==null) {
            sHappeningLab = new HappeningLab(c.getApplicationContext());

        }
        return sHappeningLab;
    }
    public HappFromParse getHappening(String id){

        for(HappFromParse h:mHappenings){
            Log.e("HapLab","Hap"+mHappenings.size());
            Log.e("Error",""+h.getTitle());
            Log.e("Error",""+h.getObjectId());
            if(h.getObjectId().equals(id))
                return  h;
        }
        return null;
    }
    public void addHappening(HappFromParse h){mHappenings.add(h);}
    public ArrayList<HappFromParse> getHappenings(){return mHappenings;}
}
