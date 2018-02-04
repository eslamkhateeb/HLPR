package com.example.ikhateeb.feedbackform;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
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


public class MainActivity extends AppCompatActivity {


        public static final MediaType FORM_DATA_TYPE
                = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
        //URL derived from form URL
        public static final String URL="https://docs.google.com/forms/d/e/1FAIpQLSd6PpKkcoEkYxo6f7P-W_DVAZIEcp17OvjSogz-qgdhcJlCMA/formResponse";
        //input element ids found from the live form page
        public static final String EMAIL_KEY="entry.1809607067";
        public static final String SUBJECT_KEY="entry.836275946";
        public static final String MESSAGE_KEY="entry.1728902986";

        private Context context ;
        private EditText emailEditText;
        private EditText nameEditText;
        private EditText phoneEditText;
        private EditText promoEditText;

        public String userID;

        @Override
        protected void onCreate(Bundle savedInstanceState){

            super.onCreate(savedInstanceState);

            setContentView(R.layout.activity_main);



            //save the activity in a context variable to be used afterwards
            context =this;

            //Get references to UI elements in the layout
            Button sendButton = findViewById(R.id.sendButton);
            emailEditText = findViewById(R.id.emailEditText);
            nameEditText = findViewById(R.id.nameEditText);
            phoneEditText = findViewById(R.id.phoneEditText);
            promoEditText = findViewById(R.id.promoEditText);

            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //Make sure all the fields are filled with values
                    if(TextUtils.isEmpty(emailEditText.getText().toString()) ||
                            TextUtils.isEmpty(nameEditText.getText().toString()) ||
                            TextUtils.isEmpty(phoneEditText.getText().toString()))
                    {
                        Toast.makeText(context,"All fields are mandatory.",Toast.LENGTH_LONG).show();
                        return;
                    }
                    //Check if a valid email is entered
                    if(!android.util.Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText().toString()).matches())
                    {
                        Toast.makeText(context,"Please enter a valid email.",Toast.LENGTH_LONG).show();
                        return;
                    }

                    //Create an object for PostDataTask AsyncTask
                    PostDataTask postDataTask = new PostDataTask();

                    //execute asynctask
                    postDataTask.execute(URL,emailEditText.getText().toString(),
                            nameEditText.getText().toString(),
                            phoneEditText.getText().toString());

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    Map<String, Object> user = new HashMap<>();
                    user.put("name", nameEditText.getText().toString());
                    user.put("email", emailEditText.getText().toString());
                    user.put("phone", phoneEditText.getText().toString());
                    user.put("promo", promoEditText.getText().toString());
                    db.collection("users")
                            .add(user)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d("HLPRDemo", "DocumentSnapshot added with ID: " + documentReference.getId());
                                    userID = documentReference.getId();
                                    Toast.makeText(getApplicationContext(), userID, Toast.LENGTH_LONG).show();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("HLPRDemo", "Error adding document", e);
                                }
                            });


                    // Navigate to the next screen caledner
                    Intent intent = new Intent(MainActivity.this, caledner.class);
                    intent.putExtra("EXTRA_USER_ID", userID);
                    MainActivity.this.startActivity(intent);
                }
            });
        }

    public void HideKeyboard(View view) {
        InputMethodManager inputMethodManager=(InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
    }

    //AsyncTask to send data as a http POST request
    private class PostDataTask extends AsyncTask<String, Void, Boolean> {

            @Override
            protected Boolean doInBackground(String... contactData) {
                Boolean result = true;
                String url = contactData[0];
                String email = contactData[1];
                String name = contactData[2];
                String phone = contactData[3];
                String postBody="";

                try {
                    //all values must be URL encoded to make sure that special characters like & | ",etc.
                    //do not cause problems
                    postBody = EMAIL_KEY+"=" + URLEncoder.encode(email,"UTF-8") +
                            "&" + SUBJECT_KEY + "=" + URLEncoder.encode(name,"UTF-8") +
                            "&" + MESSAGE_KEY + "=" + URLEncoder.encode(phone,"UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    result=false;
                }


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
                Toast.makeText(context,result?"Data posted successfully ":"There was some error in sending details. Please try again after some time.",Toast.LENGTH_LONG).show();
            }

        }


}
