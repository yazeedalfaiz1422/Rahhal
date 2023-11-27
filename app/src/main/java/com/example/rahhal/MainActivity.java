package com.example.rahhal;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pytorch.Module;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout historyButton, cameraButton, settingsButton;
    private View indicator1, indicator2, indicator3;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private HistoryFragment historyFragment;
    //private CameraFragment cameraFragment;
    private SettingsFragment settingsFragment;
    private testFragment cameraFragment;
    private int container = R.id.fragmentContainer;
    private static Module module; public String[] classMapping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupLocale();
        setContentView(R.layout.activity_main);
        initApp();
        //getSupportActionBar().hide();
        createModel();

        historyButton.setOnClickListener(this);
        cameraButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
        loadJSON();
    }

    @Override
    public void onClick(View v) {
        fragmentTransaction = fragmentManager.beginTransaction();
        int id = v.getId();
        if (id == R.id.historyButton){
            indicator1.setVisibility(View.VISIBLE);
            indicator2.setVisibility(View.INVISIBLE);
            indicator3.setVisibility(View.INVISIBLE);
            fragmentTransaction.replace(container, historyFragment);
        }

        else if(id == R.id.cameraButton){
            indicator2.setVisibility(View.VISIBLE);
            indicator1.setVisibility(View.INVISIBLE);
            indicator3.setVisibility(View.INVISIBLE);
            fragmentTransaction.replace(container, cameraFragment); }
        else if(id == R.id.settingsButton) {
            indicator3.setVisibility(View.VISIBLE);
            indicator2.setVisibility(View.INVISIBLE);
            indicator1.setVisibility(View.INVISIBLE);
            fragmentTransaction.replace(container, settingsFragment); }
        fragmentTransaction.commit();
    }

    private void createModel(){
        try {
            module = Module.load(assetFilePath(this, "Vista_Model.ptl"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initApp(){
        historyButton = findViewById(R.id.historyButton);
        cameraButton = findViewById(R.id.cameraButton);
        settingsButton = findViewById(R.id.settingsButton);
        indicator1 = findViewById(R.id.historyIndicator);
        indicator2 = findViewById(R.id.cameraIndicator);
        indicator3 = findViewById(R.id.settingsIndicator);
        historyFragment = new HistoryFragment();
        //cameraFragment = new CameraFragment();
        settingsFragment = new SettingsFragment();
        cameraFragment = new testFragment();
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(container, historyFragment, "historyFragment");
        fragmentTransaction.add(container, cameraFragment, "cameraFragment");
        fragmentTransaction.add(container, settingsFragment, "settingsFragment");
        //fragmentTransaction.add(container, testFragment, "test");
        fragmentTransaction.replace(container, cameraFragment);
        fragmentTransaction.commit();
    }

    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0){
            return file.getAbsolutePath();
        }
        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream((file))){
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

    private void loadJSON(){
        try {
            InputStream inputStream = getAssets().open("classMapping.json");
            int size=inputStream.available();
            byte[] buffer=new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            String json;
            int max;

            json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray jsonArray = new JSONArray(json);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            max = jsonObject.length();
            classMapping = new String[max];
            Log.v("JSON function is working", max+"");
            for (int i = 0; i < max; i++) {
                    classMapping[i] = jsonObject.getString(i+"");
                    Log.e("TAG", classMapping[i]);
            }
        } catch (JSONException JSONe){
            Log.e("TAG", "JSON Error!");
        } catch (NullPointerException nle) {
            Log.e("TAG", "Null pointer Error!");
        }

        catch (Exception e){
            Log.e("TAG", "error!!!");
        }
    }

    public static Module getModule() {
        return module;
    }

    public void setupLocale(){
        SharedPreferences preferences = getSharedPreferences("LanguagePreferences", Context.MODE_PRIVATE);
        String selectedLocale = preferences.getString("selectedLocale", null);
        if (selectedLocale != null) {
            Locale newLocale = new Locale(selectedLocale);
            Locale.setDefault(newLocale);

            Resources resources = getResources();
            Configuration configuration = resources.getConfiguration();
            configuration.setLocale(newLocale);
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        }
    }

}