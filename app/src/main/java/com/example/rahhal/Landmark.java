package com.example.rahhal;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Landmark extends AppCompatActivity implements View.OnClickListener {

    private ImageButton deleteButton; private DBHelper databaseHelper;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize database
        databaseHelper = new DBHelper(this);

        // Retrieve the data from the intent
        setContentView(R.layout.activity_landmark);
        String title = getIntent().getStringExtra("Title");
        path = getIntent().getStringExtra("Path");

        // Use the data as needed
        TextView titleTextView = findViewById(R.id.titleTextView);
        TextView paragraphTextView = findViewById(R.id.paragraphTextView);
        ImageView picture = findViewById(R.id.landmarkImageView);
        deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(this);
        titleTextView.setText(title);
        picture.setImageURI(Uri.parse(path));
        if (getIntent().getBooleanExtra("New", false)) {
            ChatGPT ai = new ChatGPT();
            ai.queryAsync("Write a short description about " + title + " in Saudi Arabia. Make it one paragraph long. Write it in "
                    + getResources().getString(R.string.current_language),
                    new ChatGPT.ChatGPTListener() {
                        @Override
                        public void onChatGPTResponse(String response) {
                            paragraphTextView.setText(response);
                            databaseHelper.insertData(path, title, response);
                        }
                    });
        }
        else {
            paragraphTextView.setText(getIntent().getStringExtra("Paragraph"));
        }
    }

    public void onClick(View view){
        databaseHelper.deleteRow(path);
        Toast.makeText(this, "Image successfully deleted!", Toast.LENGTH_SHORT).show();
        finish();
    }

}