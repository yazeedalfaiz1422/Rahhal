package com.example.rahhal;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraFragment extends Fragment implements ChatGPT.ChatGPTListener {

    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    String currentPhotoPath = "";
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private String[] classMapping;
    private Module module;
    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the permission launcher
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                openCamera();
            } else {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getActivity();
        loadJSON();
        createModel();

        // Obtain a reference to the button
        Button captureButton = view.findViewById(R.id.takePictureButton);

        // Set an OnClickListener on the button
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the camera permission has been granted
                if (requireCameraPermission()) {
                    openCamera();
                } else {
                    // Request the camera permission
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA);
                }
            }
        });
    }

    private boolean requireCameraPermission() {
        return getActivity().checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) == null | true) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(), "com.example.vista.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Photo taken successfully, you can use the currentPhotoPath to access the saved image
            // You can further process the image or save its path to a database if needed
            // For example, you can display a preview of the image in an ImageView
            Bitmap bitmap = resizeImage(currentPhotoPath, 224, 224);
            String landmark = predict(bitmap);
            Intent intent = new Intent(context, Landmark.class);
            intent.putExtra("Title", landmark);
            intent.putExtra("Path", currentPhotoPath);
            intent.putExtra("New", true);
            startActivity(intent);
        }
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
            InputStream inputStream = context.getAssets().open("classMapping.json");
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

    private void createModel(){
        this.module = MainActivity.getModule();
    }

    private String predict(Bitmap bitmap){
        final Tensor inputStream = TensorImageUtils.bitmapToFloat32Tensor(bitmap,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);
        final Tensor outputTensor = module.forward(IValue.from(inputStream)).toTensor();

        final float[] scores = outputTensor.getDataAsFloatArray();

        float maxScore = -Float.MAX_VALUE;
        int maxScoreIdx = -1;
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] > maxScore) {
                maxScore = scores[i];
                maxScoreIdx = i;
            }
        }

        return classMapping[maxScoreIdx];
    }

    public static Bitmap resizeImage(String imagePath, int targetWidth, int targetHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;
        int orientation = getImageOrientation(imagePath);

        // Rotate the image if needed
        Matrix matrix = new Matrix();
        if (orientation != ExifInterface.ORIENTATION_NORMAL) {
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    break;
            }
        }

        // Calculate the sample size
        int sampleSize = 1;
        if (imageWidth > targetWidth || imageHeight > targetHeight) {
            final int halfWidth = imageWidth / 2;
            final int halfHeight = imageHeight / 2;

            while ((halfWidth / sampleSize) >= targetWidth && (halfHeight / sampleSize) >= targetHeight) {
                sampleSize *= 2;
            }
        }

        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;

        // Decode the image with rotation and resizing
        Bitmap decodedBitmap = BitmapFactory.decodeFile(imagePath, options);
        Bitmap resizedBitmap = Bitmap.createBitmap(decodedBitmap, 0, 0, decodedBitmap.getWidth(), decodedBitmap.getHeight(), matrix, true);
        decodedBitmap.recycle();

        return resizedBitmap;
    }

    private static int getImageOrientation(String imagePath) {
        try {
            ExifInterface exifInterface = new ExifInterface(imagePath);
            return exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (IOException e) {
            e.printStackTrace();
            return ExifInterface.ORIENTATION_NORMAL;
        }
    }

    @Override
    public void onChatGPTResponse(String response) {

    }
}
