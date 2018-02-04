package com.example.ikhateeb.feedbackform;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class caledner extends AppCompatActivity {

    DatePicker simpleDatePicker;
    Button submit;

    public static final MediaType FORM_DATA_TYPE
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    //URL derived from form URL
    public static final String URL="https://docs.google.com/forms/d/e/1FAIpQLSd6PpKkcoEkYxo6f7P-W_DVAZIEcp17OvjSogz-qgdhcJlCMA/formResponse";
    //input element ids found from the live form page
    public static final String EMAIL_KEY="entry.91477218";
    public static final String SUBJECT_KEY="entry.958947686";
    public static final String MESSAGE_KEY="entry.849657779";

    private Context context ;
    String userID = getIntent().getStringExtra("EXTRA_SESSION_ID");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caledner);

        //save the activity in a context variable to be used afterwards
        context =this;

        /*Add in Oncreate() funtion after setContentView()*/
        simpleDatePicker = findViewById(R.id.simpleDatePicker);
        submit = findViewById(R.id.calButton);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get the values for day of month , month and year from a date picker
                String day = "" + simpleDatePicker.getDayOfMonth();
                String month = "" + (simpleDatePicker.getMonth() + 1);
                String year = "" + simpleDatePicker.getYear();

                //Create an object for PostDataTask AsyncTask
                caledner.PostDataTask postDataTask = new PostDataTask();

                //execute asynctask
                postDataTask.execute(URL,day,
                        month,
                        year);

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Map<String, Object> user = new HashMap<>();
                user.put("Day", day);
                user.put("Month", month);
                user.put("Year", year);
                db.collection("Dates")
                        .document(userID)
                        .set(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void thisVoid) {
                                Log.d("HLPRDemo", "DocumentSnapshot added!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("HLPRDemo", "Error adding document", e);
                            }
                        });

                // Navigate to the next screen cleaning details
                Intent intent = new Intent(caledner.this, CleaningDetails.class);
                caledner.this.startActivity(intent);

            }
        });

    }


    //AsyncTask to send data as a http POST request
    private class PostDataTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... contactData) {
            Boolean result = true;
            String url = contactData[0];
            String email = contactData[1];
            String subject = contactData[2];
            String message = contactData[3];
            String postBody="";

            try {
                //all values must be URL encoded to make sure that special characters like & | ",etc.
                //do not cause problems
                postBody = EMAIL_KEY+"=" + URLEncoder.encode(email,"UTF-8") +
                        "&" + SUBJECT_KEY + "=" + URLEncoder.encode(subject,"UTF-8") +
                        "&" + MESSAGE_KEY + "=" + URLEncoder.encode(message,"UTF-8");
            } catch (UnsupportedEncodingException ex) {
                result=false;
            }

            /*
            //If you want to use HttpRequest class from http://stackoverflow.com/a/2253280/1261816
            try {
			HttpRequest httpRequest = new HttpRequest();
			httpRequest.sendPost(url, postBody);
		}catch (Exception exception){
			result = false;
		}
            */

            try{
                //Create OkHttpClient for sending request
                OkHttpClient client = new OkHttpClient();
                //Create the request body with the help of Media Type
                RequestBody body = RequestBody.create(FORM_DATA_TYPE, postBody);
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                //Send the request
                Response response = client.newCall(request).execute();
            }catch (IOException exception){
                result=false;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result){
            //Print Success or failure message accordingly
            Toast.makeText(context,result?"Date set successfully":"There was some error in sending date. Please try again after some time.",Toast.LENGTH_LONG).show();
        }

    }

}
