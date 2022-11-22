package com.example.milkyway;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class CitySummary extends AppCompatActivity {

    String pricesUrl = "https://cost-of-living-and-prices.p.rapidapi.com/prices?city_name=Taipei&country_name=Taiwan";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_summary);

        // City Highlight #1: Domestic beer costs 51.12 TWD!

//        TableLayout tableLayout1 = findViewById(R.id.table_layout1); // here we grab the tablelayout
//
//        Random randomColour = new Random();

        for(int i=1; i<6; i++) {

//            TableRow tableRow = new TableRow(this); //making a row
//            TextView textView = new TextView(this); //making the text for that row

            // new
            AsyncTaskRunner runner = new AsyncTaskRunner();
            try {
                String itemPrice = runner.execute(pricesUrl).get();
//                Toast.makeText(CitySummary.this, "Worked!!!", Toast.LENGTH_SHORT).show();

//                String text = "City Highlight #" + i + ": " + itemPrice;  // adding details and params of the text view to be added
//                textView.setText(text); // for the text/images/etc, look at the adapter thing for the city spinner lab
//                textView.setWidth(900); // mahan how do I set this to match parent or something?
//
//                textView.setHeight(100);
//                textView.setGravity(Gravity.CENTER);
//                textView.setBackgroundColor(Color.argb(255, randomColour.nextInt(256), randomColour.nextInt(256), randomColour.nextInt(256)));
//
//                tableRow.addView(textView); // adding the text to the row
//
//                tableLayout1.addView(tableRow); // THIS is where we add the row to the table layout


            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
//        tableLayout1.setForegroundGravity(Gravity.CENTER);

    }

    private class AsyncTaskRunner extends AsyncTask<String, Void, String> {

        String itemPrice;

        @Override
        protected String doInBackground(String... strings) {
            RequestQueue queue = Volley.newRequestQueue(CitySummary.this);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, strings[0], null, response -> {
                try {
                    JSONArray prices = response.getJSONArray("prices");
                    int itemId;
                    double tempPrice = 0.00;
                    String currencyCode;
                    String itemName;
                    Random random = new Random();
                    int randomItemId = random.nextInt(prices.length());

                    for (int i = 0; i < prices.length(); i++) {
                        // City Highlight #1: Domestic beer costs 51.12 TWD!
                        JSONObject jsonObjectPrice = prices.getJSONObject(i);
                        itemId = jsonObjectPrice.getInt("good_id");
                        if (itemId == randomItemId) {
                            itemName = jsonObjectPrice.getString("item_name");
                            tempPrice = jsonObjectPrice.getDouble("avg");
                            currencyCode = jsonObjectPrice.getString("currency_code");
                            StringBuilder sb = new StringBuilder(itemName + " costs " + tempPrice
                                    + " " + currencyCode);
                            itemPrice = itemName + " costs " + tempPrice + " " + currencyCode;
                            Toast.makeText(CitySummary.this, sb, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        itemId = 1;
                    }
                    onSuccess();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> Toast.makeText(CitySummary.this, error.toString(), Toast.LENGTH_SHORT).show()) {

                /** Passing some request headers* */
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("X-RapidAPI-Key", "API_KEY_REMOVED");
                    headers.put("X-RapidAPI-Host", "cost-of-living-and-prices.p.rapidapi.com");
                    return headers;
                }
            };

            queue.add(request);
            return itemPrice;  // seems to only return null?
        }

        protected void onSuccess() {
            Toast.makeText(CitySummary.this, "onPostExecute", Toast.LENGTH_SHORT).show();
//            setData("hi");

            TableLayout tableLayout1 = findViewById(R.id.table_layout1); // here we grab the tablelayout

            Random randomColour = new Random();
            TableRow tableRow = new TableRow(CitySummary.this); //making a row
            TextView textView = new TextView(CitySummary.this); //making the text for that row

            String text = "City Highlight: " + itemPrice;  // adding details and params of the text view to be added
            textView.setText(text); // for the text/images/etc, look at the adapter thing for the city spinner lab
            textView.setWidth(900); // mahan how do I set this to match parent or something?

            textView.setHeight(100);
            textView.setGravity(Gravity.CENTER);
            textView.setBackgroundColor(Color.argb(255, randomColour.nextInt(256), randomColour.nextInt(256), randomColour.nextInt(256)));

            tableRow.addView(textView); // adding the text to the row

            tableLayout1.addView(tableRow); // THIS is where we add the row to the table layout

            tableLayout1.setForegroundGravity(Gravity.CENTER);
        }

        private void setData(String data){
            Toast.makeText(CitySummary.this, "setData", Toast.LENGTH_SHORT).show();
//            mTextView.setText(data);
        }
    }
}