package com.aashishkumar.androidproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;


/**
 *
 * This Class return current weather.
 * A simple {@link Fragment} subclass.
 */
public class WeatherFragment extends Fragment {


    TextView selectCity, cityField, detailsField, currentTemperatureField, pressure_field, updatedField;
    ImageView imageView;
    ProgressBar loader;
    String location = "Tacoma, US";
    String WEATHER_MAP_API = "58b43eca9e254f02a1f7b75ee9525838";

    public WeatherFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_weather, container, false);
        loader = v.findViewById(R.id.loader);
        selectCity = v.findViewById(R.id.selectCity);
        cityField = v.findViewById(R.id.city_field);
        updatedField = v.findViewById(R.id.updated_field);
        detailsField = v.findViewById(R.id.details_field);
        currentTemperatureField = v.findViewById(R.id.current_temperature_field);
        pressure_field = v.findViewById(R.id.pressure_field);
        imageView = v.findViewById(R.id.image1View);

        Button b = (Button) v.findViewById(R.id.button_forecast);
        b.setOnClickListener(view -> loadFragment(new WeatherforecastFragment()));

        WeatherFragment.DownloadWeather task = new WeatherFragment.DownloadWeather();
        task.execute(location);

        selectCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(v.getContext());
                alertDialog.setTitle("Change City");
                final EditText input = new EditText(v.getContext());
                input.setText(location);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);
                alertDialog.setPositiveButton("Change",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                location = input.getText().toString();
                                WeatherFragment.DownloadWeather task = new WeatherFragment.DownloadWeather();
                                task.execute(location);
                            }
                        });
                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                alertDialog.show();
            }
        });

        return v;
    }

    /**
     * A method helps to load new fragment
     *
     * @param frag is the fragment that needed to load
     */
    private void loadFragment(Fragment frag) {
        FragmentTransaction transaction =
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.activity_home, frag)
                        .addToBackStack(null);
        transaction.commit();
    }


    /**
     * Class to parse the data from url and updating on fields.
     */
    class DownloadWeather extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loader.setVisibility(View.VISIBLE);

        }

        protected String doInBackground(String... args) {
            String xml = Weather_Content.excuteGet("https://api.weatherbit.io/v2.0/current?city=" + args[0] +
                    "&units=imperial&key=" + WEATHER_MAP_API);
            return xml;
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void onPostExecute(String xml) {

            try {
                JSONObject json = new JSONObject(xml);
                if(json != null) {
                    String iconId;
                    JSONObject day = json.getJSONArray("data").getJSONObject(0);
                    JSONObject weather = day.getJSONObject("weather");
                    iconId = weather.getString("icon");
                    updatedField.setText(day.getString("ob_time"));
                    int imageResource = getResources().getIdentifier(("@drawable/"+iconId),null, getActivity().getPackageName());
                    imageView.setImageResource(imageResource);
                    detailsField.setText(weather.getString("description").toUpperCase(Locale.US));
                    currentTemperatureField.setText(String.format("%.2f", day.getDouble("temp")) + "°");
                    pressure_field.setText("Pressure: " + day.getDouble("pres") );
                    cityField.setText(day.getString("city_name")+", "+ day.getString("country_code"));
                    loader.setVisibility(View.GONE);

                }
            } catch (JSONException e) {
                Toast.makeText(getActivity().getApplicationContext(), "Error, Check City", Toast.LENGTH_SHORT).show();
            }

        }

    }

}
