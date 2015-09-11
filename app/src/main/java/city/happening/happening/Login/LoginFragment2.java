package city.happening.happening.Login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import city.happening.happening.Cards.CardFragment;
import city.happening.happening.R;

/**
 * Created by Alex on 8/11/2015.
 */
public class LoginFragment2 extends Fragment {
    ParseUser mCurrentUser;
    Button mButton;
    CallbackManager mCallbackManager;
    OnLoginSuccessfulListener mCallBack;
    ParseUser newUser;

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
        View v = inflater.inflate(R.layout.fragment_login, parent, false);
        Log.e("Login","Oncreate view started");
        mButton =(Button) v.findViewById(R.id.loginButton);
        mCurrentUser =ParseUser.getCurrentUser();
        mButton.setText("FaceBook");
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Login","onClick");
                mCallbackManager = CallbackManager.Factory.create();
                LoginManager mLoginManager = LoginManager.getInstance();
                mLoginManager.logInWithReadPermissions(getActivity(), Arrays.asList("public_profile", "email", "user_friends", "user_location", "user_birthday"));
                mLoginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.e("Login","onSuccess");
                        if (!mCurrentUser.isNew()) {
                            GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject user, GraphResponse graphResponse) {
                                    try {
                                        Log.e("Login","onCompleted try catch");
                                        mCurrentUser.setUsername(user.get("id").toString());
                                        if (user.get("email") != null)
                                            mCurrentUser.setEmail(user.get("email").toString());
                                        mCurrentUser.setPassword("password");
                                        mCurrentUser.put("FBObjectID", user.get("id"));
                                        Log.e("login", "" + user.get("id").toString());
                                        mCurrentUser.put("link", user.get("link"));

                                        if (user.get("first_name") != null) {
                                            mCurrentUser.put("firstName", user.get("first_name"));
                                        }
                                        if (user.get("last_name") != null) {
                                            mCurrentUser.put("lastName", user.get("last_name"));
                                        }
                                        if (user.get("gender") != null) {
                                            mCurrentUser.put("gender", user.get("gender"));
                                        }
                                        if (user.get("birthday") != null) {
                                            mCurrentUser.put("birthday", user.get("birthday"));
                                        }
                                        if (user.get("location") != null) {
                                            //handle user location information
                                        }
                                        mCurrentUser.put("fbToken", AccessToken.getCurrentAccessToken().toString());
                                        mCurrentUser.logInInBackground(mCurrentUser.getUsername(), "password", new LogInCallback() {
                                            @Override
                                            public void done(ParseUser parseUser, ParseException e) {
                                                if (e == null) {
                                                    Log.e("Login","logindone");
                                                    setDefualtsForUser(parseUser);
                                                } else {
                                                    Log.e("login2", "error" + e);

                                                }
                                            }
                                        });

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).executeAsync();
                        } else {
                            GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject user, GraphResponse graphResponse) {
                                    Log.e("Login","newmerequest");
                                    newUser = new ParseUser();
                                    try {
                                        newUser.setUsername(user.get("id").toString());
                                        if (user.get("email") != null)
                                            newUser.setEmail(user.get("email").toString());
                                        newUser.setPassword("password");
                                        newUser.put("FBObjectID", user.get("id"));
                                        Log.e("login", "" + newUser.get("FBObjectID"));
                                        newUser.put("link", user.get("link"));

                                        if (user.get("first_name") != null) {
                                            newUser.put("firstName", user.get("first_name"));
                                        }
                                        if (user.get("last_name") != null) {
                                            newUser.put("lastName", user.get("last_name"));
                                        }
                                        if (user.get("gender") != null) {
                                            newUser.put("gender", user.get("gender"));
                                        }
                                        if (user.get("birthday") != null) {
                                            newUser.put("birthday", user.get("birthday"));
                                        }
                                        if (user.get("location") != null) {
                                            //handle user location information
                                        }
                                        newUser.put("fbToken", AccessToken.getCurrentAccessToken().toString());
                                        Log.e("login", "" + newUser.get("firstName"));
                                        newUser.signUpInBackground(new SignUpCallback() {
                                            @Override
                                            public void done(ParseException e) {

                                            }
                                        });
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).executeAsync();
                        }
                    }

                    @Override
                    public void onCancel() {
                        Log.e("Login","Canceled");
                    }

                    @Override
                    public void onError(FacebookException e) {
                        Log.e("login","error"+e);
                    }
                });
            }
        });



        return v;

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
        mCallBack.onCreateNewFrag(true,new CardFragment());
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }


}
