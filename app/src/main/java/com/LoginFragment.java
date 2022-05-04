package com.aashishkumar.androidproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.aashishkumar.androidproject.models.Credentials;
import com.aashishkumar.androidproject.utils.SendPostAsyncTask;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;



 /*
 * Fragment to handle the log in process
 *
 * @author Aayush Shah
 * @author Hien Doan
 * @author Robert Bohlman
 */
public class LoginFragment extends Fragment implements View.OnClickListener {

    private OnLoginFragmentInteractionListener mListener;
    private Credentials mCredentials;
    private int mMemberID;
    private String mUsername;
    private String mFirebaseToken;
    private CheckBox mStayLoggedInCheck;


    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        // Get the sign in button and add click listener to it
        Button login = (Button) v.findViewById(R.id.signinBtn_login_fragment);
        login.setOnClickListener(this);

        // Get the register button and add click listener to it
        Button register = (Button) v.findViewById(R.id.registerBtn_login_fragment);
        register.setOnClickListener(view ->mListener.onRegisterClicked());

        // GEt the stay logged in checkbox option
        mStayLoggedInCheck = (CheckBox) v.findViewById(R.id.checkBox_login_fragment);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        //retrieve the stored credentials from SharedPrefs
        if (prefs.contains(getString(R.string.keys_prefs_email)) &&
                prefs.contains(getString(R.string.keys_prefs_password))) {

            final String email = prefs.getString(getString(R.string.keys_prefs_email), "");
            final String password = prefs.getString(getString(R.string.keys_prefs_password), "");

            //Load the two login EditTexts with the credentials found in SharedPrefs
            EditText emailEdit = getActivity().findViewById(R.id.enter_verification_code_fragment);
            emailEdit.setText(email);
            EditText passwordEdit = getActivity().findViewById(R.id.passText_login_fragment);
            passwordEdit.setText(password);

            getFirebaseToken(email, password);
        }
    }


    @Override
    public void onClick(View view) {
        if (mListener != null) {
            switch (view.getId()) {
                case R.id.signinBtn_login_fragment:
                    attemptLogin(view);
                    break;
                default:
                    Log.wtf("", "Didn't expect to see me...");
            }
        }
    }

    /**
     * GEt the firebase token
     *
     * @param email is the email used to login
     * @param password is password used to login
     */
    private void getFirebaseToken(final String email, final String password) {
        mListener.onWaitFragmentInteractionShow();

        //add this app on this device to listen for the topic all
        FirebaseMessaging.getInstance().subscribeToTopic("all");

        //the call to getInstanceId happens asynchronously. task is an onCompleteListener
        //similar to a promise in JS.
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM: ", "getInstanceId failed", task.getException());
                        mListener.onWaitFragmentInteractionHide();
                        return;
                    }

                    // Get new Instance ID token
                    mFirebaseToken = task.getResult().getToken();
                    Log.d("FCM: ", mFirebaseToken);
                    //the helper method that initiates login service
                    doLogin(email, password);
                });
        //no code here. wait for the Task to complete.
    }

    /**
     * Log user in
     * @param theButton is the view
     */
    private void attemptLogin(final View theButton) {

        EditText emailEdit = getActivity().findViewById(R.id.enter_verification_code_fragment);
        EditText passwordEdit = getActivity().findViewById(R.id.passText_login_fragment);

        boolean hasError = false;
        if (emailEdit.getText().length() == 0) {
            hasError = true;
            emailEdit.setError("Field must not be empty.");
        }  else if (!isValidEmail(emailEdit.getText().toString())) {
            hasError = true;
            emailEdit.setError("Field must contain a valid email address.");
        }
        if (passwordEdit.getText().length() == 0) {
            hasError = true;
            passwordEdit.setError("Field must not be empty.");
        }

        if (!hasError) {
            String email = emailEdit.getText().toString();
            String password = passwordEdit.getText().toString();
            getFirebaseToken(email, password);
        }
    }

    /**
     * Helper method to check for email validation
     *
     * @param email is the email needed to be checked
     * @return whether this email is valid or not
     */
    private boolean isValidEmail(String email) {
        boolean result = false;
        char[] array = email.toCharArray();
        for (int i = 0; i < array.length; i++) {
            if (array[i] == '@') {
                result = true;
                break;
            }
        }
        return result;
    }


    /**
     * Helper method to login automatically
     */
    private void doLogin(String email, String password) {
        Credentials credentials = new Credentials.Builder(
                email, password).build();

        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_login))
                .appendPath(getString((R.string.ep_with_token)))
                .build();

        //build the JSONObject
        JSONObject msg = credentials.asJSONObject();

        try {
            msg.put("token", mFirebaseToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mCredentials = credentials;

        //instantiate and execute the AsyncTask.
        //Feel free to add a handler for onPreExecution so that a progress bar
        //is displayed or maybe disable buttons.
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleLoginOnPre)
                .onPostExecute(this::handleLoginOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    /**
     * Handle errors that may occur during the AsyncTask.
     * @param result the error message provide from the AsyncTask
     */
    private void handleErrorsInTask(String result) {
        Log.e("ASYNCT_TASK_ERROR",  result);
    }

    /**
     * Handle the setup of the UI before the HTTP call to the webservice.
     */
    private void handleLoginOnPre() {
    }

    /**
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     * @param result the JSON formatted String response from the web service
     */
    private void handleLoginOnPost(String result) {
        try {

            Log.d("JSON result",result);
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");
            mUsername = resultsJSON.getString("username");
            mMemberID = resultsJSON.getInt("memberid");
            mUsername = resultsJSON.getString("username");

            //Log.e("username ", mUsername);
            //Log.e("userid ", mMemberID);
            mListener.onWaitFragmentInteractionHide();
            if (success) {
                //Login was successful. Inform the Activity so it can do its thing.
                // Only save credentials if stay login is checked
                if (mStayLoggedInCheck.isChecked()) {
                    saveCredentials(mCredentials);
                }
                mListener.onLoginSuccess(mCredentials, mUsername, mMemberID);
            } else {
                //Login was unsuccessful. Don’t switch fragments and inform the user
                ((TextView) getView().findViewById(R.id.enter_verification_code_fragment))
                        .setError("Login Unsuccessful");
            }

        } catch (JSONException e) {
            //It appears that the web service didn’t return a JSON formatted String
            //or it didn’t have what we expected in it.
            Log.e("JSON_PARSE_ERROR",  result
                    + System.lineSeparator()
                    + e.getMessage());

            mListener.onWaitFragmentInteractionHide();
            ((TextView) getView().findViewById(R.id.enter_verification_code_fragment))
                    .setError("Login Unsuccessful");
        }
    }

    /**
     * Helper method to save credential for stay logged in option
     *
     * @param credentials is the credentials of this member
     */
    private void saveCredentials(final Credentials credentials) {
        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        //Store the credentials in SharedPrefs
        prefs.edit().putString(getString(R.string.keys_prefs_email), credentials.getEmail()).apply();
        prefs.edit().putString(getString(R.string.keys_prefs_password), credentials.getPassword()).apply();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLoginFragmentInteractionListener) {
            mListener = (OnLoginFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLoginFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnLoginFragmentInteractionListener
            extends WaitFragment.OnFragmentInteractionListener {
        void onLoginSuccess(Credentials mCredentials, String username, int id);
        void onRegisterClicked();
    }
}
