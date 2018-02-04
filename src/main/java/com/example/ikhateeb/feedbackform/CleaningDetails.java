package com.example.ikhateeb.feedbackform;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CleaningDetails extends AppCompatActivity {

    public static final MediaType FORM_DATA_TYPE
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    //URL derived from form URL
    public static final String URL="https://docs.google.com/forms/d/e/1FAIpQLSd6PpKkcoEkYxo6f7P-W_DVAZIEcp17OvjSogz-qgdhcJlCMA/formResponse";
    //input element ids found from the live form page
    public static final String Appartment_size="entry.954772785";
    public static final String City_spinner="entry.1094579118";
    public static final String Region_spinner="entry.1074971930";
    public static final String Bld_number="entry.970906210";
    public static final String Floor_spinner="entry.445536295";
    public static final String Street_spinner="entry.1965846528";

    private Context context ;
    private EditText BldEditText;
    private EditText FloorEditText;
    private EditText StreetEditText;
    private Spinner AppSpinner;
    private Spinner CitySpinner;
    private Spinner RegionSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cleaning_details);

        //save the activity in a context variable to be used afterwards
        context =this;

        //Get references to UI elements in the layout
        Button sendButton = findViewById(R.id.sendButton);
        BldEditText = findViewById(R.id.editTextBuilding);
        FloorEditText = findViewById(R.id.editTextFloor);
        StreetEditText = findViewById(R.id.editTextAddress);
        AppSpinner = findViewById(R.id.spinnerAppSize);
        CitySpinner = findViewById(R.id.spinnerCity);
        RegionSpinner = findViewById(R.id.spinnerRegion);


        //get the spinner from the xml.
        Spinner spinnerApp = findViewById(R.id.spinnerAppSize);
        //create a list of items for the spinner.
        String[] items = new String[]{"Apartment Size","0 to 100 Square meter", "101 to 200 Square meter", "More than 200 meter"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        //set the spinners adapter to the previously created one.
        spinnerApp.setAdapter(adapter);

        //get the spinner from the xml.
        Spinner spinnerCity = findViewById(R.id.spinnerCity);
        //create a list of items for the spinner.
        String[] City = new String[]{"City","Alexandria", "Cairo", "Others"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapterCity = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, City);
        //set the spinners adapter to the previously created one.
        spinnerCity.setAdapter(adapterCity);

        //get the spinner from the xml.
        Spinner spinnerRegion = findViewById(R.id.spinnerRegion);
        //create a list of items for the spinner.
        String[] Region = new String[]{"Region","East", "West", "Montazah"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapterRegion = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Region);
        //set the spinners adapter to the previously created one.
        spinnerRegion.setAdapter(adapterRegion);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Make sure all the fields are filled with values
                if(TextUtils.isEmpty(BldEditText.getText().toString()) ||
                        TextUtils.isEmpty(FloorEditText.getText().toString()) ||
                        TextUtils.isEmpty(StreetEditText.getText().toString())||
                        TextUtils.isEmpty(AppSpinner.getSelectedItem().toString())||
                        TextUtils.isEmpty(CitySpinner.getSelectedItem().toString())||
                        TextUtils.isEmpty(RegionSpinner.getSelectedItem().toString())
                        )
                {
                    Toast.makeText(context,"All fields are mandatory.",Toast.LENGTH_LONG).show();
                    return;
                }

                //Create an object for PostDataTask AsyncTask
                PostDataTask postDataTask = new CleaningDetails.PostDataTask();

                //execute asynctask
                postDataTask.execute(URL,BldEditText.getText().toString(),
                        FloorEditText.getText().toString(),
                        StreetEditText.getText().toString(),
                        AppSpinner.getSelectedItem().toString(),
                        CitySpinner.getSelectedItem().toString(),
                        RegionSpinner.getSelectedItem().toString()
                );
                // Navigate to the next screen caledner
                Intent intent = new Intent(CleaningDetails.this, Confirmation.class);
                CleaningDetails.this.startActivity(intent);


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
            String subject = contactData[2];
            String message = contactData[3];
            String building = contactData[4];
            String street = contactData[5];
            String floor = contactData[6];
            String postBody="";

            try {
                //all values must be URL encoded to make sure that special characters like & | ",etc.
                //do not cause problems
                postBody = Appartment_size+"=" + URLEncoder.encode(email,"UTF-8") +
                        "&" + City_spinner + "=" + URLEncoder.encode(subject,"UTF-8") +
                        "&" + Region_spinner + "=" + URLEncoder.encode(message,"UTF-8")+
                        "&" + Bld_number + "=" + URLEncoder.encode(building,"UTF-8")+
                        "&" + Floor_spinner + "=" + URLEncoder.encode(floor,"UTF-8")+
                        "&" + Street_spinner + "=" + URLEncoder.encode(street,"UTF-8")
                ;
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
            Toast.makeText(context,result?"Address Details set":"There was some error in sending details. Please try again after some time.",Toast.LENGTH_LONG).show();
        }

    }


}
