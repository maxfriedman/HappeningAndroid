package city.happening.happening.Login;

/**
 * Created by Alex on 8/13/2015.
 */

import android.support.v4.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import city.happening.happening.CitySelector;
import city.happening.happening.ProgressDialogFragment;
import city.happening.happening.R;


public class LoginActivity extends AppCompatActivity {
    DialogFragment mDialog;
    ImageView mProfileImage;
    LoginButton mBtnFb;
    TextView mUsername, mEmailID;
    Profile mFbProfile;
    CallbackManager mCallBackManager;
    LoginButton mLoginButton;
    String name = null, mFbID = null;
    ParseUser parseUser;
    VideoView mVideoView;
    ArrayList<Map<String,String>>mFriends = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AccessToken.getCurrentAccessToken()!=null&&!AccessToken.getCurrentAccessToken().isExpired()&&Profile.getCurrentProfile()!=null){
           // mBtnFb.setVisibility(View.GONE);
           // mVideoView.setVisibility(View.GONE);
            mDialog = new ProgressDialogFragment().newInstance("Processing Your Info!!");
            mDialog.show(getSupportFragmentManager(), "Processing Your Info!!");
            getUserDetailsFromFB();
        }

        setContentView(R.layout.activity_login);

        mBtnFb = (LoginButton) findViewById(R.id.btn_fb_login);
        mVideoView = (VideoView) findViewById(R.id.videoPlayer);
        String fileName = "android.resource://" + getPackageName() + "/" + R.raw.happening_intro_vid;
        mVideoView.setVideoURI(Uri.parse(fileName));
        mVideoView.start();


        mCallBackManager = CallbackManager.Factory.create();

        mFbProfile = Profile.getCurrentProfile();

        mBtnFb.setReadPermissions( Arrays.asList("public_profile", "email", "user_friends", "user_location", "user_birthday"));
        mBtnFb.registerCallback(mCallBackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                mBtnFb.setVisibility(View.GONE);
                mVideoView.setVisibility(View.GONE);
                mDialog = new ProgressDialogFragment().newInstance("Processing Your Info!");
                mDialog.show(getSupportFragmentManager(), "Processing Your Info!!");
                getUserDetailsFromFB();

            }

            @Override
            public void onCancel() {
                Log.d("Main", "Oncancel");
            }

            @Override
            public void onError(FacebookException e) {
                Log.d("Main", "onError" + e);
            }
        });


    }

    private void handleFBData(JSONObject user,boolean newUser, ArrayList<Map<String,String>>friends) {
        parseUser = new ParseUser();
        Log.e("LoginActivity",""+friends.get(0).get("id"));
        try{
            parseUser.setUsername(user.get("email").toString());
            if (user.get("email") != null) parseUser.setEmail(user.get("email").toString());
            parseUser.setPassword(user.get("link").toString());
            parseUser.put("FBObjectID", user.get("id"));
            Log.e("login", "" + user.get("id").toString());
            parseUser.put("link", user.get("link"));
            parseUser.put("friends",friends);

            if (user.get("first_name") != null) {
                parseUser.put("firstName", user.get("first_name"));
            }
            if (user.get("last_name") != null) {
                parseUser.put("lastName", user.get("last_name"));
            }
            if (user.get("gender") != null) {
                parseUser.put("gender", user.get("gender"));
            }
            if (user.get("birthday") != null) {
                parseUser.put("birthday", user.get("birthday"));
            }
            if (user.get("location") != null) {
                //handle user location information
                Map<String,String>location = new HashMap<>();
                if (location.get("name")!=null){
                    Log.d("Parse User",""+location.get("name"));
                    parseUser.put("fbLocationName",location.get("name"));
                    parseUser.put("city",location.get("name"));
                }

            }
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            parseUser.put("fbToken", accessToken.getToken());

            Log.d("Parse User","Token "+accessToken.getToken());
            Log.d("Parse USer", ""+parseUser.getEmail()+parseUser.getUsername());
            Log.d("Parse User","link"+parseUser.get("link").toString());
        }catch (JSONException e){

        }
        if(newUser){
            parseUser.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.e("Parse User", "signup");
                        Map<String, String> params = new HashMap<String, String>();
                        AccessToken accessToken = AccessToken.getCurrentAccessToken();
                        params.put("user", parseUser.getObjectId());
                        params.put("name", parseUser.get("first_name") + " " + parseUser.get("last_name"));
                        params.put("fbID", parseUser.getString("FBObjectID"));
                        params.put("fbToken", accessToken.getToken());
                        ParseCloud.callFunctionInBackground("newUser", params);
                        setDefualtsForUser(parseUser);
                    } else {
                        Log.d("Parse User","Error"+e);
                        parseLogin(parseUser);
                    }

                }
            });
        }else{
            parseLogin(parseUser);
        }

    }
    private void parseLogin(ParseUser user){
        user.logInInBackground(user.getEmail(), user.get("link").toString(), new LogInCallback() {
            @Override
            public void done(ParseUser user1, ParseException e) {
                if (e == null) {
                    Log.d("Parse User", "WOO no errors were in" + user1.getEmail());
                    setDefualtsForUser(user1);
                } else {
                    Log.e("Parse User", "Errors bro" + e);
                }
            }
        });
    }

    public void handleFriendsList(JSONArray array){
        ArrayList<Map<String,String>>friends = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Map<String, String> mTemp = new HashMap<String, String>();
            try {
                ParseUser tempUser = new ParseUser();
                String tempName = array.getJSONObject(i).getString("name");
                String tempID = array.getJSONObject(i).getString("id");
                mTemp.put("id", tempID);
                mTemp.put("name", tempName);
                ParseQuery<ParseUser> parseQuery = ParseUser.getQuery();
                parseQuery.whereEqualTo("FBObjectID", tempID);
                tempUser = parseQuery.getFirst();
                String tempParseId = tempUser.getObjectId();
                Log.e("FriendsList", "user" + tempUser.getObjectId());
                mTemp.put("parseId", tempParseId);
            } catch (Exception e) {
                e.printStackTrace();
            }
            friends.add(mTemp);
        }
        mFriends = friends;
        final ArrayList<Map<String,String>>friendList = friends;

        Log.e("LoginActivity", "Fr size" + mFriends.size());
        GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(final JSONObject user, GraphResponse response) {
            /* handle the result */
                try {
                    Log.d("Main", "GraphResponse " + response);
                    Log.d("Main", "GraphResponse " + user);
                    mFbID = response.getJSONObject().getString("id");
                    ParseQuery<ParseUser> parseQuery = ParseUser.getQuery();
                    Log.d("Main", user.get("id").toString());
                    parseQuery.whereEqualTo("FBObjectID", user.get("id"));
                    parseQuery.findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> list, ParseException e) {
//                            if(list.get(0)!=null)Log.d("main","Parse User"+list.get(0).getEmail());
                            Log.d("Main", "query done" + list.size());
                            boolean isNew = true;
                            if (list.size() > 0) {
                                isNew = false;
                            }
                            handleFBData(user, isNew,friendList);
                        }
                    });
                    //handleFBData(user);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).executeAsync();

    }

    private void getUserDetailsFromFB() {

        GraphRequest.newMyFriendsRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONArrayCallback() {
            @Override
            public void onCompleted(JSONArray jsonArray, GraphResponse response) {

                // Application code for users friends
                try {
                    handleFriendsList(jsonArray);
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }).executeAsync();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallBackManager.onActivityResult(requestCode, resultCode, data);
    }


    public void setDefualtsForUser(ParseUser user){
        Boolean notisEnabled = true;//needs to say whether push notifications are enabled
        ParseInstallation currentInstallation = new ParseInstallation();
        Log.e("LoginActivity",""+user.get("friends"));
        Log.e("LoginActivity",""+mFriends.get(0).get("parseId"));
        user.put("friends", mFriends);

        /*

        ParsePush.subscribeInBackground("global");
        ParsePush.subscribeInBackground("reminders");
        ParsePush.subscribeInBackground("matches");
        ParsePush.subscribeInBackground("friendJoined");
        ParsePush.subscribeInBackground("popularEvents");
        ParsePush.subscribeInBackground("allGroups");
        ParsePush.subscribeInBackground("bestFriends");
        currentInstallation.put("userID", user.getObjectId());
        currentInstallation.put("enabled", notisEnabled);
        currentInstallation.saveEventually(); */

        Map<String,String> defaults = new HashMap<>();
        defaults.put("refreshData","no");
        defaults.put("noMoreEvents","no");
        if (user.get("hasCreatedEvent")==null) user.put("hasCreatedEvent",false);
        if (user.get("hasLaunched")==null) user.put("hasLaunched",false);
        if(user.get("hasSwipedRight")==null)user.put("hasSwipedRight",false);
        if(user.get("locStatus")==null)user.put("locStatus","unknown");
        if (notisEnabled)user.put("pushStatus","yes");
        else user.put("pushStatus","maybe");

        if (user.get("socialMode")==null)user.put("socialMode",true);
        if (user.get("userLocTitle")==null)user.put("userLocTitle","");
        if (user.get("userLocSubTitle")==null)user.put("userLocSubTitle","");
        if (user.get("radius")==null)user.put("radius",50);

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

        if(user.get("categories")==null)user.put("categories",categories);
        if(user.get("categoryName")==null)user.put("categoryName","Most Popular");
        if (user.get("time")==null)user.put("time","today");
        Log.d("Parse User", "" + user.get("categories") + user.get("userLocTitle"));
        user.pinInBackground();
        user.saveEventually();
        Intent i = new Intent(this, CitySelector.class);
        startActivity(i);
        finish();


    }



}