package city.happening.happening.Login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import city.happening.happening.Cards.CardFragment;
import city.happening.happening.R;


public class LoginFragment extends Fragment {
    ImageView mProfileImage;
    LoginButton mBtnFb;
    TextView mUsername, mEmailID;
    Profile mFbProfile;
    CallbackManager mCallBackManager;
    String name = null, mFbID = null;
    ParseUser parseUser;
    VideoView mVideoView;
    OnLoginSuccessfulListener mCallBack;


    public interface OnLoginSuccessfulListener{
        public void onCreateNewFrag(Boolean worked,Fragment fragment);
    }
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try{
            mCallBack= (OnLoginSuccessfulListener) activity;
        }catch (ClassCastException e){
            throw new ClassCastException(activity.toString()+"must implement OnSucc Listener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup parent,Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_login,parent,false);

       /* mVideoView =(VideoView) v.findViewById(R.id.videoPlayer);
        mVideoView.setVideoURI(Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + R.raw.happening_intro_vid));
        mVideoView.start();
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                mp.start();
            }
        });*/
        mBtnFb = (LoginButton) v.findViewById(R.id.loginButton);

        mCallBackManager = CallbackManager.Factory.create();

        mFbProfile = Profile.getCurrentProfile();

        mBtnFb.setReadPermissions( Arrays.asList("public_profile", "email", "user_friends", "user_location", "user_birthday"));
        mBtnFb.registerCallback(mCallBackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                getUserDetailsFromFB();
            }

            @Override
            public void onCancel() {
                Log.d("Main","Oncancel");
            }

            @Override
            public void onError(FacebookException e) {
                Log.d("Main","onError"+e);
            }
        });



        return v;

    }

    @Override
    public void onResume() {
        super.onResume();
     //   mCallBack.onCreateNewFrag(true, new CardFragment());

    }
    private void handleFBData(JSONObject user,boolean newUser) {
        parseUser = new ParseUser();
        try{
            parseUser.setUsername(user.get("email").toString());
            if (user.get("email") != null) parseUser.setEmail(user.get("email").toString());
            parseUser.setPassword(user.get("link").toString());
            parseUser.put("FBObjectID", user.get("id"));
            Log.e("login", "" + user.get("id").toString());
            parseUser.put("link", user.get("link"));

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
            public void done(ParseUser user1,ParseException e) {
                if (e == null) {
                    Log.d("Parse User", "WOO no errors were in" + user1.getEmail());
                    setDefualtsForUser(user1);
                } else {
                    Log.e("Parse User", "Errors bro" + e);
                }
            }
        });
    }

    private void getUserDetailsFromFB() {

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
                            handleFBData(user, isNew);
                        }
                    });
                    //handleFBData(user);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).executeAsync();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallBackManager.onActivityResult(requestCode, resultCode, data);
    }



    public void setDefualtsForUser(ParseUser user){
        Boolean notisEnabled = true;//needs to say whether push notifications are enabled
        ParseInstallation currentInstallation = new ParseInstallation();
        //set channels need to do this

        ParsePush.subscribeInBackground("global");
        ParsePush.subscribeInBackground("reminders");
        ParsePush.subscribeInBackground("matches");
        ParsePush.subscribeInBackground("friendJoined");
        ParsePush.subscribeInBackground("popularEvents");
        ParsePush.subscribeInBackground("allGroups");
        ParsePush.subscribeInBackground("bestFriends");
        currentInstallation.put("userID", user.getObjectId());
        currentInstallation.put("enabled", notisEnabled);
        currentInstallation.saveEventually();

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
        if(user.get("userLocSubTitle")==null)user.put("userLocSubTitle","");
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

        user.pinInBackground();
        user.saveEventually();
        mCallBack.onCreateNewFrag(true, new CardFragment());

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
     //   mVideoView.suspend();
    }


}
