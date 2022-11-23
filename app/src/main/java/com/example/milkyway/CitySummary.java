package com.example.milkyway;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.matteobattilana.weather.PrecipType;
import com.github.matteobattilana.weather.WeatherView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class CitySummary extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    String pricesUrl;
    Animation sunAnim,cloud1Anim,cloud2Anim, cloud3Anim, AnimtitleAnim;
    ImageView sun,cloud1,cloud2, cloud3;
    Map<String, ArrayList<String>> fiveDaysWeather = new LinkedHashMap<>();
    WeatherView weatherView;
    String tempUrl;
    EditText etCity;
    TextView tvResult;
    Button btnGetData;
    private final String url = "https://api.openweathermap.org/data/2.5/forecast";
    private final String appid = "API_KEY_REMOVED";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_city_summary);

        Bundle bundle = this.getIntent().getExtras().getBundle("bundle") ;
        String countryName = bundle.getString("countryName");
        String cityName = bundle.getString("cityName").toLowerCase(Locale.ROOT);
        pricesUrl = "https://cost-of-living-and-prices.p.rapidapi.com/prices?city_name="
                + cityName + "&country_name=" + countryName;

        TextView cityNameView = findViewById(R.id.summary_city_name);
        cityNameView.setText(cityName);

        tempUrl = url + "?q=" + cityName + "&appid=" + appid;
        AsyncTaskRunnerWeather runnerWeather = new AsyncTaskRunnerWeather();
        runnerWeather.execute(tempUrl);

        ImageView image = findViewById(R.id.cityImage);
        Picasso.get()
                .load("https://countryflagsapi.com/png/" + countryName)
                .resize(600, 400) // resizes the image to these dimensions (in pixel)
                .centerCrop()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.image_not_found)
                .into(image);

        sunAnim = AnimationUtils.loadAnimation(this,R.anim.sun);
        cloud1Anim = AnimationUtils.loadAnimation(this,R.anim.cloud1);
        cloud2Anim = AnimationUtils.loadAnimation(this,R.anim.cloud2);
        cloud3Anim = AnimationUtils.loadAnimation(this, R.anim.cloud3);
        sun = findViewById(R.id.sun);
        cloud1 = findViewById(R.id.cloud1);
        cloud2 = findViewById(R.id.cloud2);
        cloud3 = findViewById(R.id.cloud3);

        for (int i = 1; i <= 10; i++) {

            AsyncTaskRunner runner = new AsyncTaskRunner();
            try {
                runner.execute(pricesUrl).get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private class AsyncTaskRunnerWeather extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            RequestQueue queue = Volley.newRequestQueue(CitySummary.this);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, strings[0], null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {

                        JSONArray fiveDayHourlyForecast = response.getJSONArray("list");


                        for(int i=0; i< fiveDayHourlyForecast.length(); i++) {
                            String tempDate = fiveDayHourlyForecast.getJSONObject(i).getString("dt_txt").substring(0,10);
                            ArrayList<String> oneDayWeatherInfo = new ArrayList<>();

                            if (!fiveDaysWeather.containsKey(tempDate)) {
                                oneDayWeatherInfo.add(fiveDayHourlyForecast.getJSONObject(i).getJSONObject("main").getString("temp"));
                                oneDayWeatherInfo.add(fiveDayHourlyForecast.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("main"));

                                fiveDaysWeather.put(tempDate, oneDayWeatherInfo);

                            }

                        }

                        onSuccess();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(CitySummary.this, error.toString(), Toast.LENGTH_SHORT).show();
                }
            });
            queue.add(request);
            return null;
        }

        protected void onSuccess() throws ParseException {
//            int[] buttonViews = {R.id.date1, R.id.date2, R.id.date3, R.id.date4, R.id.date5, R.id.date6};
            Locale currentLocale = Locale.getDefault();
            weatherView = findViewById(R.id.weather_view);

            ArrayList<String> spinnerList = new ArrayList<>();
            spinnerList.add("5 Day Forecast");

            View linearLayout =  findViewById(R.id.linearSpinner);
            Spinner spinner = new Spinner(getApplicationContext());
            spinner.setDropDownWidth(200);
//            spinner.setMinimumWidth(100);
            spinner.setBackgroundColor(0);
//            spinner.setBackgroundTintMode(PorterDuff.Mode.DARKEN);
            spinner.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));

            DateFormat formatter = new SimpleDateFormat("EEEE", currentLocale);
            Iterator<Map.Entry<String, ArrayList<String>>> itr = fiveDaysWeather.entrySet().iterator();
            ArrayList<String> descriptionList = new ArrayList<>();
            for (int i =0; i < 6; i++) {
                Map.Entry<String, ArrayList<String>> entry = itr.next();
                String description = entry.getValue().get(1);
                descriptionList.add(description);
                String key= entry.getKey();
                Date date1=new SimpleDateFormat("yyyy-MM-dd").parse(key);
                String day = formatter.format(date1);
                spinnerList.add(day.substring(0, 3));

// Clouds (few, scattered, overcast, broken), rain (light, moderate)   clear sky

            }
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int item, long l) {
                    if(item == 1) {
                        String desc = descriptionList.get(0).toLowerCase();
                        if (desc.contains("clouds")) {
                            sun.setVisibility(View.GONE);
                            sun.clearAnimation();
                            cloud1.setVisibility(View.VISIBLE);
                            cloud1.startAnimation(cloud1Anim);
                            cloud2.setVisibility(View.VISIBLE);
                            cloud2.startAnimation(cloud2Anim);
                            cloud3.setVisibility(View.VISIBLE);
                            cloud3.startAnimation(cloud3Anim);
                        } else if (desc.contains("rain")) {
                            sun.clearAnimation();
                            cloud1.setVisibility(View.VISIBLE);
                            cloud1.startAnimation(cloud1Anim);
                            cloud2.setVisibility(View.VISIBLE);
                            cloud2.startAnimation(cloud2Anim);
                            weatherView.setWeatherData(PrecipType.RAIN);
                        }else if (desc.contains("clear")) {
                            cloud1.setVisibility(View.GONE);
                            cloud2.setVisibility(View.GONE);
                            cloud3.setVisibility(View.GONE);
                            cloud1.clearAnimation();
                            sun.setVisibility(View.VISIBLE);
                            sun.startAnimation(sunAnim);

                        } else if (desc.contains("snow")){
                            sun.clearAnimation();
                            sun.setVisibility(View.GONE);
                            cloud1.setVisibility(View.VISIBLE);
                            cloud1.startAnimation(cloud1Anim);
                            cloud2.setVisibility(View.VISIBLE);
                            cloud2.startAnimation(cloud2Anim);
                            weatherView.setWeatherData(PrecipType.SNOW);
//                            weatherView.setBackgroundColor(Color.RED);
                        }
                    } else if(item == 2) {
                        String desc = descriptionList.get(1).toLowerCase();
                        if (desc.contains("clouds")) {
                            sun.setVisibility(View.GONE);
                            sun.clearAnimation();
                            cloud1.setVisibility(View.VISIBLE);
                            cloud1.startAnimation(cloud1Anim);
                            cloud2.setVisibility(View.VISIBLE);
                            cloud2.startAnimation(cloud2Anim);
                            cloud3.setVisibility(View.VISIBLE);
                            cloud3.startAnimation(cloud3Anim);
                        } else if (desc.contains("rain")) {
                            sun.clearAnimation();
                            cloud1.setVisibility(View.VISIBLE);
                            cloud1.startAnimation(cloud1Anim);
                            cloud2.setVisibility(View.VISIBLE);
                            cloud2.startAnimation(cloud2Anim);
                            weatherView.setWeatherData(PrecipType.RAIN);
                        }else if (desc.contains("clear")) {
                            cloud1.setVisibility(View.GONE);
                            cloud2.setVisibility(View.GONE);
                            cloud3.setVisibility(View.GONE);
                            cloud1.clearAnimation();
                            sun.setVisibility(View.VISIBLE);
                            sun.startAnimation(sunAnim);

                        } else if (desc.contains("snow")){
                            sun.clearAnimation();
                            sun.setVisibility(View.GONE);
                            cloud1.setVisibility(View.VISIBLE);
                            cloud1.startAnimation(cloud1Anim);
                            cloud2.setVisibility(View.VISIBLE);
                            cloud2.startAnimation(cloud2Anim);
                            weatherView.setWeatherData(PrecipType.SNOW);
                            weatherView.setBackgroundColor(Color.RED);
                        }
                    }else if(item == 3) {
                        String desc = descriptionList.get(2).toLowerCase();
                        if (desc.contains("clouds")) {
                            sun.setVisibility(View.GONE);
                            sun.clearAnimation();
                            cloud1.setVisibility(View.VISIBLE);
                            cloud1.startAnimation(cloud1Anim);
                            cloud2.setVisibility(View.VISIBLE);
                            cloud2.startAnimation(cloud2Anim);
                            cloud3.setVisibility(View.VISIBLE);
                            cloud3.startAnimation(cloud3Anim);
                        } else if (desc.contains("rain")) {
                            sun.clearAnimation();
                            cloud1.setVisibility(View.VISIBLE);
                            cloud1.startAnimation(cloud1Anim);
                            cloud2.setVisibility(View.VISIBLE);
                            cloud2.startAnimation(cloud2Anim);
                            weatherView.setWeatherData(PrecipType.RAIN);
                        }else if (desc.contains("clear")) {
                            cloud1.setVisibility(View.GONE);
                            cloud2.setVisibility(View.GONE);
                            cloud3.setVisibility(View.GONE);
                            cloud1.clearAnimation();
                            sun.setVisibility(View.VISIBLE);
                            sun.startAnimation(sunAnim);

                        } else if (desc.contains("snow")){
                            sun.clearAnimation();
                            sun.setVisibility(View.GONE);
                            cloud1.setVisibility(View.VISIBLE);
                            cloud1.startAnimation(cloud1Anim);
                            cloud2.setVisibility(View.VISIBLE);
                            cloud2.startAnimation(cloud2Anim);
                            weatherView.setWeatherData(PrecipType.SNOW);
                            weatherView.setBackgroundColor(Color.RED);
                        }
                    }else if(item == 4) {
                        String desc = descriptionList.get(3).toLowerCase();
                        if (desc.contains("clouds")) {
                            sun.setVisibility(View.GONE);
                            sun.clearAnimation();
                            cloud1.setVisibility(View.VISIBLE);
                            cloud1.startAnimation(cloud1Anim);
                            cloud2.setVisibility(View.VISIBLE);
                            cloud2.startAnimation(cloud2Anim);
                            cloud3.setVisibility(View.VISIBLE);
                            cloud3.startAnimation(cloud3Anim);
                        } else if (desc.contains("rain")) {
                            sun.clearAnimation();
                            cloud1.setVisibility(View.VISIBLE);
                            cloud1.startAnimation(cloud1Anim);
                            cloud2.setVisibility(View.VISIBLE);
                            cloud2.startAnimation(cloud2Anim);
                            weatherView.setWeatherData(PrecipType.RAIN);
                        }else if (desc.contains("clear")) {
                            cloud1.setVisibility(View.GONE);
                            cloud2.setVisibility(View.GONE);
                            cloud3.setVisibility(View.GONE);
                            cloud1.clearAnimation();
                            sun.setVisibility(View.VISIBLE);
                            sun.startAnimation(sunAnim);

                        } else if (desc.contains("snow")){
                            sun.clearAnimation();
                            sun.setVisibility(View.GONE);
                            cloud1.setVisibility(View.VISIBLE);
                            cloud1.startAnimation(cloud1Anim);
                            cloud2.setVisibility(View.VISIBLE);
                            cloud2.startAnimation(cloud2Anim);
                            weatherView.setWeatherData(PrecipType.SNOW);
                            weatherView.setBackgroundColor(Color.RED);
                        }
                    }else if(item == 5) {
                        String desc = descriptionList.get(4).toLowerCase();
                        if (desc.contains("clouds")) {
                            sun.setVisibility(View.GONE);
                            sun.clearAnimation();
                            cloud1.setVisibility(View.VISIBLE);
                            cloud1.startAnimation(cloud1Anim);
                            cloud2.setVisibility(View.VISIBLE);
                            cloud2.startAnimation(cloud2Anim);
                            cloud3.setVisibility(View.VISIBLE);
                            cloud3.startAnimation(cloud3Anim);
                        } else if (desc.contains("rain")) {
                            sun.clearAnimation();
                            cloud1.setVisibility(View.VISIBLE);
                            cloud1.startAnimation(cloud1Anim);
                            cloud2.setVisibility(View.VISIBLE);
                            cloud2.startAnimation(cloud2Anim);
                            weatherView.setWeatherData(PrecipType.RAIN);
                        }else if (desc.contains("clear")) {
                            cloud1.setVisibility(View.GONE);
                            cloud2.setVisibility(View.GONE);
                            cloud3.setVisibility(View.GONE);
                            cloud1.clearAnimation();
                            sun.setVisibility(View.VISIBLE);
                            sun.startAnimation(sunAnim);

                        } else if (desc.contains("snow")){
                            sun.clearAnimation();
                            sun.setVisibility(View.GONE);
                            cloud1.setVisibility(View.VISIBLE);
                            cloud1.startAnimation(cloud1Anim);
                            cloud2.setVisibility(View.VISIBLE);
                            cloud2.startAnimation(cloud2Anim);
                            weatherView.setWeatherData(PrecipType.SNOW);
                            weatherView.setBackgroundColor(Color.RED);
                        }
                    }else if(item == 6) {
                        String desc = descriptionList.get(5).toLowerCase();
                        if (desc.contains("clouds")) {
                            sun.setVisibility(View.GONE);
                            sun.clearAnimation();
                            cloud1.setVisibility(View.VISIBLE);
                            cloud1.startAnimation(cloud1Anim);
                            cloud2.setVisibility(View.VISIBLE);
                            cloud2.startAnimation(cloud2Anim);
                            cloud3.setVisibility(View.VISIBLE);
                            cloud3.startAnimation(cloud3Anim);
                        } else if (desc.contains("rain")) {
                            sun.clearAnimation();
                            cloud1.setVisibility(View.VISIBLE);
                            cloud1.startAnimation(cloud1Anim);
                            cloud2.setVisibility(View.VISIBLE);
                            cloud2.startAnimation(cloud2Anim);
                            weatherView.setWeatherData(PrecipType.RAIN);
                        }else if (desc.contains("clear")) {
                            cloud1.setVisibility(View.GONE);
                            cloud2.setVisibility(View.GONE);
                            cloud3.setVisibility(View.GONE);
                            cloud1.clearAnimation();
                            sun.setVisibility(View.VISIBLE);
                            sun.startAnimation(sunAnim);

                        } else if (desc.contains("snow")){
                            sun.clearAnimation();
                            sun.setVisibility(View.GONE);
                            cloud1.setVisibility(View.VISIBLE);
                            cloud1.startAnimation(cloud1Anim);
                            cloud2.setVisibility(View.VISIBLE);
                            cloud2.startAnimation(cloud2Anim);
                            weatherView.setWeatherData(PrecipType.SNOW);
                            weatherView.setBackgroundColor(Color.RED);
                        }
                    }else if(item == 7) {
                        String desc = descriptionList.get(6).toLowerCase();
                        if (desc.contains("clouds")) {
                            sun.setVisibility(View.GONE);
                            sun.clearAnimation();
                            cloud1.setVisibility(View.VISIBLE);
                            cloud1.startAnimation(cloud1Anim);
                            cloud2.setVisibility(View.VISIBLE);
                            cloud2.startAnimation(cloud2Anim);
                            cloud3.setVisibility(View.VISIBLE);
                            cloud3.startAnimation(cloud3Anim);
                        } else if (desc.contains("rain")) {
                            sun.clearAnimation();
                            cloud1.setVisibility(View.VISIBLE);
                            cloud1.startAnimation(cloud1Anim);
                            cloud2.setVisibility(View.VISIBLE);
                            cloud2.startAnimation(cloud2Anim);
                            weatherView.setWeatherData(PrecipType.RAIN);
                        }else if (desc.contains("clear")) {
                            cloud1.setVisibility(View.GONE);
                            cloud2.setVisibility(View.GONE);
                            cloud3.setVisibility(View.GONE);
                            cloud1.clearAnimation();
                            sun.setVisibility(View.VISIBLE);
                            sun.startAnimation(sunAnim);

                        } else if (desc.contains("snow")){
                            sun.clearAnimation();
                            sun.setVisibility(View.GONE);
                            cloud1.setVisibility(View.VISIBLE);
                            cloud1.startAnimation(cloud1Anim);
                            cloud2.setVisibility(View.VISIBLE);
                            cloud2.startAnimation(cloud2Anim);
                            weatherView.setWeatherData(PrecipType.SNOW);
                            weatherView.setBackgroundColor(Color.RED);
                        }
                    }

                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, spinnerList);

            spinner.setAdapter(spinnerArrayAdapter);
            ((LinearLayout) linearLayout).addView(spinner );


        }

    }



    private class AsyncTaskRunner extends AsyncTask<String, Void, String> {

        String itemPrice;

        @Override
        protected String doInBackground(String... strings) {
            RequestQueue queue = Volley.newRequestQueue(CitySummary.this);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, strings[0], null, response -> {
                boolean isExists = false;

                try {
                    JSONArray prices = response.getJSONArray("prices");
                    int itemId;
                    double tempPrice;
                    String currencyCode;
                    String itemName;
                    Random random = new Random();
                    int randomItemId = random.nextInt(prices.length());

                    for (int i = 0; i < prices.length(); i++) {
                        JSONObject jsonObjectPrice = prices.getJSONObject(i);
                        itemId = jsonObjectPrice.getInt("good_id");
                        itemName = jsonObjectPrice.getString("item_name");

                        if (itemId == randomItemId && jsonObjectPrice.has("item_name")) {
                            isExists = true;

                            if (itemName.contains(",")) {
                                String[] parts = itemName.split(",");
                                itemName = parts[0];
                            }

                            tempPrice = jsonObjectPrice.getDouble("avg");
                            currencyCode = jsonObjectPrice.getString("currency_code");
                            itemPrice = itemName + " costs " + tempPrice + " " + currencyCode + "!";
                            break;
                        }
                    }
                    if (isExists) {
                        onSuccess();
                    }
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
            return null;
        }

        protected void onSuccess() {

            TableLayout tableLayout1 = findViewById(R.id.table_layout1); // here we grab the tablelayout
            final int baseColor = Color.WHITE;
            final int baseRed = Color.red(baseColor);
            final int baseGreen = Color.green(baseColor);
            final int baseBlue = Color.blue(baseColor);

            Random random = new Random();
            TableRow tableRow = new TableRow(CitySummary.this); //making a row
            TextView textView = new TextView(CitySummary.this); //making the text for that row

            textView.setText(itemPrice);
            textView.setWidth(1000);
            textView.setHeight(100);
//            textView.setTextColor(Color.BLUE);
            textView.setGravity(Gravity.LEFT);
//            textView.setBackgroundColor(Color.argb(255,
//                    (baseRed + random.nextInt(256)) / 2,
//                    (baseGreen + random.nextInt(256)) /2 ,
//                    (baseBlue + random.nextInt(256)) / 2));

            tableRow.addView(textView); // adding the text to the row
            tableLayout1.addView(tableRow); // THIS is where we add the row to the table layout

            tableLayout1.setForegroundGravity(Gravity.CENTER);
        }

    }
}


//                btn.setOnClickListener(view -> {
//                        if (description.toLowerCase().contains("clouds")) {
//                        sun.setVisibility(View.GONE);
//                        sun.clearAnimation();
//                        cloud1.setVisibility(View.VISIBLE);
//                        cloud1.startAnimation(cloud1Anim);
//                        cloud2.setVisibility(View.VISIBLE);
//                        cloud2.startAnimation(cloud2Anim);
//                        cloud3.setVisibility(View.VISIBLE);
//                        cloud3.startAnimation(cloud3Anim);
//                        } else if (description.toLowerCase().contains("rain")) {
//                        sun.clearAnimation();
//                        cloud1.setVisibility(View.VISIBLE);
//                        cloud1.startAnimation(cloud1Anim);
//                        cloud2.setVisibility(View.VISIBLE);
//                        cloud2.startAnimation(cloud2Anim);
//                        weatherView.setWeatherData(PrecipType.RAIN);
//                        }else if (description.toLowerCase().contains("clear")) {
//                        cloud1.setVisibility(View.GONE);
//                        cloud2.setVisibility(View.GONE);
//                        cloud3.setVisibility(View.GONE);
//                        cloud1.clearAnimation();
//                        sun.setVisibility(View.VISIBLE);
//                        sun.startAnimation(sunAnim);
//
//                        }
//
//                        });