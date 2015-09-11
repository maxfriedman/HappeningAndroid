package city.happening.happening.LargeContainer;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.PushService;

import org.json.JSONObject;

import java.util.List;

import city.happening.happening.HappFromParse;

/**
 * Created by Alex on 7/20/2015.
 */
public class HappeningApplication extends Application {

    protected static final String TAG = "HappApp";


    @Override
    public void onCreate(){
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
        Parse.enableLocalDatastore(getApplicationContext());
        Parse.initialize(this, "olSntgsT5uY3ZZbJtnjNz8yvol4CxwmArTsbkCZa", "xwmrITvs8UaFBNfBupzXcUa6HN3sU515xp1TsGxu");
        ParseObject.registerSubclass(HappFromParse.class);

        PushService.setDefaultPushCallback(this, HappeningMain.class);






    }



    private List<JSONObject> selectedUsers;
    private JSONObject selectedPlace;

    public List<JSONObject> getSelectedUsers() {
        return selectedUsers;
    }

    public void setSelectedUsers(List<JSONObject> users) {
        selectedUsers = users;
    }



}
