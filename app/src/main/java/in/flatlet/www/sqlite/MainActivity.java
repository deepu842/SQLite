package in.flatlet.www.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    FeedReaderDbHelper mDbHelper;
    Boolean flag;
    SQLiteDatabase db;
    JsonArrayRequest jsonArrayRequest;
    String GET_JSON_DATA_HTTP_URL = "http://flatlet.in/flatlettitlefetcher/titlefetcher.jsp";
    RequestQueue requestQueue;
    private static final String TAG = "MainActivity";
    private AutoCompleteTextView autoComplete;
    private String queryPart;
    private Cursor cursor1;
    private ArrayList<String> list = new ArrayList<>();
    ArrayAdapter<String> adapter;
    ImageView reviewImageView;
    TextView reviewHostelTitle, reviewSecAddress;
    CardView reviewCard;
    JsonObjectRequest jsonObjRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        autoComplete = (AutoCompleteTextView) findViewById(R.id.autoComplete);
        reviewImageView = (ImageView) findViewById(R.id.review_imageView);
        reviewHostelTitle = (TextView) findViewById(R.id.review_hostel_title);
        reviewSecAddress = (TextView) findViewById(R.id.review_sec_address);
        reviewCard = (CardView) findViewById(R.id.review_hostel_card);
        reviewCard.setVisibility(View.GONE);
        mDbHelper = new FeedReaderDbHelper(this);
        db = mDbHelper.getWritableDatabase();
        Log.i(TAG, "onCreate: of main");
        /*new ConnectDatabase().execute();*/

        // Gets the data repository in write mode

       /* mDbHelper.onCreate(db);*/
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("secondTime", false)) {
            // <---- run your one time code here
            Log.i(TAG, "onCreate: before oncreate");
            mDbHelper.onCreateOriginal(db);
            Log.i(TAG, "onCreate: called");


            // mark first time has runned.
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("secondTime", true);
            editor.apply();
        }
        String[] projection = {
                FeedReaderContract.FeedEntry._ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE,

        };

        Cursor cursor = db.query(FeedReaderContract.FeedEntry.TABLE_NAME, projection, null, null, null, null, null);
        int i = cursor.getCount();
        Log.i(TAG, "onCreate: k" + i);


        jsonArrayRequest = new JsonArrayRequest("http://flatlet.in/flatlettitlefetcher/titlefetcher.jsp?count=" + i,

                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.i(TAG, "onResponse: " + response.length());

                        for (int j = 0; j < response.length(); j++) {
                            try {
                                String title = response.getString(j);
                                Log.i(TAG, "onResponse: " + title);
                                ContentValues values = new ContentValues();
                                values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE, title);
                                long newRowId = db.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);
                                Log.i(TAG, "onResponse:1 " + newRowId);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.i(TAG, "onErrorResponse: " + error);


                    }
                });
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest);


        autoComplete = (AutoCompleteTextView) findViewById(R.id.autoComplete);
        autoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.i(TAG, "beforeTextChanged: ");
                if (start<=7)
                reviewCard.setVisibility(View.GONE);
                reviewSecAddress.setText(null);
                reviewImageView.setImageResource(0);


                list.clear();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                Log.i(TAG, "onTextChanged: before if " + count);
                Log.i(TAG, "onTextChanged: before " + before);
                Log.i(TAG, "onTextChanged: start " + start);
                if (start > 1) {

                /*s=(String)queryPart;*/
                    Log.i(TAG, "onTextChanged: after if" + s);


                    cursor1 = db.rawQuery("Select " + FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE + " from "
                            + FeedReaderContract.FeedEntry.TABLE_NAME + " WHERE " + FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE + " LIKE '%" + s + "%'", null);

                    Log.i(TAG, "onTextChanged: query=" + "Select " + FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE + " from "
                            + FeedReaderContract.FeedEntry.TABLE_NAME + " WHERE " + FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE + " LIKE '%" + s + "%'", null);

                    while (cursor1.moveToNext()) {


                        String title = cursor1.getString(0);
                        Log.i(TAG, "onTextChanged: " + title);
                        Log.i(TAG, "onTextChanged: " + cursor1.getCount());

                        list.add(title);
                        Log.i(TAG, "onTextChanged: after add" + title);
                        Log.i(TAG, "onTextChanged: list size " + list.size());


                    }


                    adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item, list);
                    autoComplete.setAdapter(adapter);


                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.i(TAG, "afterTextChanged: ");



            }

        });


        autoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                Log.i(TAG, "onItemClick: clicked hostel is " + adapter.getItem(arg2));
                reviewHostelTitle.setText(adapter.getItem(arg2));
                String title= adapter.getItem(arg2).replace(" ","%20");


                          jsonObjRequest = new JsonObjectRequest
                        ("http://flatlet.in/flatletreviewdata/reviewdata.jsp?title="+title, null, new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {

                                try {
                                    Log.i(TAG, "onResponse: ");
                                    reviewSecAddress.setText(response.getString("address_secondary"));
                                }

                                catch (JSONException e) 
                                {
                                    e.printStackTrace();
                                    Log.i(TAG, "onResponse: catch "+e);
                                }

                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.i(TAG, "onErrorResponse: "+error);


                            }
                        });
                RequestQueue queue =Volley.newRequestQueue(MainActivity.this);
                queue.add(jsonObjRequest);
                Picasso.with(getBaseContext()).load("http://images.flatlet.in/images/24%20Paradise/IMG_20170607_203707-01.jpg").into(reviewImageView);

                reviewCard.setVisibility(View.VISIBLE);



            }
        });


    }

   /* @Override
    protected void onResume() {
        super.onResume();

    }*/




    /*// Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE,"amandeepe");
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE, "sharmae");

// Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);
        Log.i("MainActivity", "onCreate: "+newRowId);*/

    /*private class ConnectDatabase extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] params) {
            Log.i(TAG, "doInBackground: ");
             db = mDbHelper.getWritableDatabase();
            return null;
        }


    }*/

}
