package com.example.rahhal;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import java.util.Locale;


public class SettingsFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        AutoCompleteTextView t = getActivity().findViewById(R.id.autoCompleteTextView);
        // Define the array of options
        String[] languages = getResources().getStringArray(R.array.languages);
        ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), R.layout.dropdown_item, languages);
        t.setAdapter(arrayAdapter);
        t.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = (String) parent.getItemAtPosition(position);

                // Perform your desired action with the selected option
                // For example, display a Toast message with the selected option
                Toast.makeText(getContext(), "Selected: " + selectedOption, Toast.LENGTH_SHORT).show();
                String choice = "";
                switch (position) {
                    case 0:
                        choice = "en";
                        break;
                    case 1:
                        choice = "ar";
                        break;
                    case 2:
                        choice = "es";
                        break;
                    case 3:
                        choice = "zh";
                        break;
                    default:
                        break;
                }
                Locale newLocale = new Locale(choice);
                Locale.setDefault(newLocale);

                Resources resources = getContext().getResources();
                Configuration configuration = resources.getConfiguration();
                configuration.setLocale(newLocale);
                resources.updateConfiguration(configuration, resources.getDisplayMetrics());
                SharedPreferences preferences = getContext().getSharedPreferences("LanguagePreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("selectedLocale", choice);
                editor.apply();
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstance){


    }

}