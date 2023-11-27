package com.example.rahhal;

import static androidx.core.app.ActivityCompat.recreate;

import android.content.Intent;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class HistoryFragment extends Fragment implements OnImageClickListener {

    DBHelper db;
    List<String> imagePaths;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DBHelper(getContext());
        imagePaths = db.getAllImagePaths();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    public void onViewCreated(View view, Bundle saveInstanceState){
        // Initialize the RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(),
                LinearLayoutManager.VERTICAL, false));


        // Create an instance of the ImageAdapter
        RowAdapter imageAdapter = new RowAdapter(imagePaths, this, getContext());

        // Set the adapter on the RecyclerView
        recyclerView.setAdapter(imageAdapter);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.divider));
        recyclerView.addItemDecoration(itemDecoration);
    }

    @Override
    public void onImageClick(String data){
        Intent intent = new Intent(getContext(), Landmark.class);
        String[]d = db.getRowByPath(data);
        intent.putExtra("Title", d[1]);
        intent.putExtra("Path", d[0]);
        intent.putExtra("Paragraph", d[2]);
        intent.putExtra("New", false);
        startActivity(intent);
    }
}