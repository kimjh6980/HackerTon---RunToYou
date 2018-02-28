//
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license.
//
// Microsoft Cognitive Services (formerly Project Oxford): https://www.microsoft.com/cognitive-services
//
// Microsoft Cognitive Services (formerly Project Oxford) GitHub:
// https://github.com/Microsoft/Cognitive-Face-Android
//
// Copyright (c) Microsoft Corporation
// All rights reserved.
//
// MIT License:
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
package com.microsoft.projectoxford.face.samples.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.VerifyResult;
import com.microsoft.projectoxford.face.samples.R;
import com.microsoft.projectoxford.face.samples.helper.ImageHelper;
import com.microsoft.projectoxford.face.samples.helper.LogHelper;
import com.microsoft.projectoxford.face.samples.helper.SampleApp;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // Web String
    String WEB = "A";
    // Flag to indicate which task is to be performed.
    private static final int REQUEST_SELECT_IMAGE_0 = 0;
    private static final int REQUEST_SELECT_image_1 = 1;

    //---------------------------------------------------------------------------------------------------
    ImageView mImage;
    ImageView mImage1;
    //---------------------------------------------------------------------------------------------------

    // The IDs of the two faces to be verified.
    private UUID mFaceId0;
    private UUID mFaceId1;

    // The two images from where we get the two faces to verify.
    private Bitmap bit;
    //private Bitmap mBitmap0;
    private Bitmap mBitmap1;
    //-----------------------------------------------------------------------------------------------------
    private int index=0;

    private Uri downimgUri;
    private String path;

    // The adapter of the ListView which contains the detected faces from the two images.
    protected FaceListAdapter mFaceListAdapter0;
    protected FaceListAdapter mFaceListAdapter1;

    // Progress dialog popped up when communicating with server.
    ProgressDialog progressDialog;

    Handler handler;
    int turn = 1;

    File file;
    // When the activity is created, set all the member variables to initial state.

    TextView textview;
    String str = "";

    //------------------------- 카메라
    MyCameraSurface mSurface;



    public void DetectFinish(View view) {
        WEB = "B";
    }

    public void SetVerify(View view) {
        new VerificationTask(mFaceId0, mFaceId1).execute();
    }

    private class JSONTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {

            try {
                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("user_id", "androidTest");
                jsonObject.accumulate("name", "yun");

                HttpURLConnection con = null;
                BufferedReader reader = null;

                try{
                    //URL url = new URL("http://192.168.25.16:3000/users");
                    URL url = new URL(urls[0]);
                    //연결을 함
                    con = (HttpURLConnection) url.openConnection();

                    con.setRequestMethod("POST");//POST방식으로 보냄
                    con.setRequestProperty("Cache-Control", "no-cache");//캐시 설정
                    con.setRequestProperty("Content-Type", "application/json");//application JSON 형식으로 전송
                    con.setRequestProperty("Accept", "text/html");//서버에 response 데이터를 html로 받음
                    con.setDoOutput(true);//Outstream으로 post 데이터를 넘겨주겠다는 의미
                    con.setDoInput(true);//Inputstream으로 서버로부터 응답을 받겠다는 의미
                    con.connect();

                    //서버로 보내기위해서 스트림 만듬
                    OutputStream outStream = con.getOutputStream();
                    //버퍼를 생성하고 넣음
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                    writer.write(jsonObject.toString());
                    writer.flush();
                    writer.close();//버퍼를 받아줌

                    //서버로 부터 데이터를 받음
                    InputStream stream = con.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();

                    String line = "";
                    while((line = reader.readLine()) != null){
                        buffer.append(line);
                    }

                    return buffer.toString();//서버로 부터 받은 값을 리턴해줌 아마 OK!!가 들어올것임

                } catch (MalformedURLException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(con != null){
                        con.disconnect();
                    }
                    try {
                        if(reader != null){
                            reader.close();//버퍼를 닫아줌
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            WEB = result;
        }

    }

    // Background task for face verification.
    private class VerificationTask extends AsyncTask<Void, String, VerifyResult> {
        // The IDs of two face to verify.
        private UUID mFaceId0;
        private UUID mFaceId1;

        VerificationTask (UUID faceId0, UUID faceId1) {
            mFaceId0 = faceId0;
            mFaceId1 = faceId1;
        }

        @Override
        protected VerifyResult doInBackground(Void... params) {
            // Get an instance of face service client to detect faces in image.
            FaceServiceClient faceServiceClient = SampleApp.getFaceServiceClient();
            try{
                publishProgress("Verifying...");

                // Start verification.
                return faceServiceClient.verify(
                        mFaceId0,      /* The first face ID to verify */
                        mFaceId1);     /* The second face ID to verify */
            }  catch (Exception e) {
                publishProgress(e.getMessage());
                addLog(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            if (turn > 0) {
//                progressDialog.show();
                addLog("Request: Verifying face " + mFaceId0 + " and face " + mFaceId1);
            }
        }
        @Override
        protected void onProgressUpdate(String... progress) {
//            progressDialog.setMessage(progress[0]);
            setInfo(progress[0]);
        }

        @Override
        protected void onPostExecute(VerifyResult result) {
            if (result != null) {
                addLog("Response: Success. Face " + mFaceId0 + " and face "
                        + mFaceId1 + (result.isIdentical ? " " : " don't ")
                        + "belong to the same person");
            }

            // Show the result on screen when verification is done.
            setUiAfterVerification(result);
        }
    }

    // Background task of face detection.
    private class DetectionTask extends AsyncTask<InputStream, String, Face[]> {
        // Index indicates detecting in which of the two images.
        private int mIndex;
        private boolean mSucceed = true;

        DetectionTask(int index) {
            mIndex = index;
        }

        @Override
        protected Face[] doInBackground(InputStream... params) {
            // Get an instance of face service client to detect faces in image.
            FaceServiceClient faceServiceClient = SampleApp.getFaceServiceClient();
            try{
                publishProgress("Detecting...");
                // Start detection.
                return faceServiceClient.detect(
                        params[0],  /* Input stream of image to detect */
                        true,       /* Whether to return face ID */
                        false,       /* Whether to return face landmarks */
                        /* Which face attributes to analyze, currently we support:
                           age,gender,headPose,smile,facialHair */
                        null);
            }  catch (Exception e) {
                mSucceed = false;
                publishProgress(e.getMessage());
                addLog(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
//            progressDialog.show();
            addLog("Request: Detecting in image" + mIndex);
        }

        @Override
        protected void onProgressUpdate(String... progress) {
//            progressDialog.setMessage(progress[0]);
            setInfo(progress[0]);
        }

        @Override
        protected void onPostExecute(Face[] result) {
            // Show the result on screen when detection is done.
            setUiAfterDetection(result, mIndex, mSucceed);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getString(R.string.subscription_key).startsWith("Please")) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.add_subscription_key_tip_title))
                    .setMessage(getString(R.string.add_subscription_key_tip))
                    .setCancelable(false)
                    .show();
        }
        //---------------------------------------------------------------------------------------------------
        mImage = (ImageView)findViewById(R.id.image_0);
        //---------------------------------------------------------------------------------------------------
        setContentView(R.layout.activity_main);
        // Initialize the two ListViews which contain the thumbnails of the detected faces.
        initializeFaceList(0);
        initializeFaceList(1);


        handler = new Handler();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.progress_dialog_title));

        clearDetectedFaces(0);
        clearDetectedFaces(1);

        // Disable button "verify" as the two face IDs to verify are not ready.
        setVerifyButtonEnabledStatus(false);

        LogHelper.clearVerificationLog();

        //-----------카메라
        mSurface = (MyCameraSurface)findViewById(R.id.preview1);

        textview = (TextView)findViewById(R.id.textView3);
    }

    // Called when image selection is done. Begin detecting if the image is selected successfully.

    protected void sizesetup(int requestCode, int resultCode, Uri data) {
        // Index indicates which of the two images is selected.
        int index;
        if (requestCode == REQUEST_SELECT_IMAGE_0) {
            index = 0;
        } else if (requestCode == 1) {
            index = 1;
        } else {
            return;
        }
        if(resultCode == RESULT_OK) {
            // If image is selected successfully, set the image URI and bitmap.
            Bitmap bitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                    data, getContentResolver());
            if (bitmap != null) {
                // Image is select but not detected, disable verification button.
                setVerifyButtonEnabledStatus(false);
                clearDetectedFaces(index);
                // Set the image to detect.
                if (index == 0) {
                    bit = bitmap;
                    mFaceId0 = null;
                } else {
                    mBitmap1 = bitmap;
                    mFaceId1 = null;
                }

                // Add verification log.
                addLog("Image" + index + ": " + data + " resized to " + bitmap.getWidth()
                        + "x" + bitmap.getHeight());
                // Start detecting in image.
                detect(bitmap, index);
            }
        }
    }

    // Clear the detected faces indicated by index.
    private void clearDetectedFaces(int index) {z
        ListView faceList = (ListView) findViewById(
                index == 0 ? R.id.list_faces_0: R.id.list_faces_1);
        faceList.setVisibility(View.GONE);
        ImageView imageView =(ImageView) findViewById(index == 0 ? R.id.image_0: R.id.image_1);
        imageView.setImageResource(android.R.color.transparent);
    }

    // Called when the "Select Image0" button is clicked in face face verification.
    public void selectImage0(View view) {
        switch (view.getId())   {
            case R.id.select_image_0:(new DownThread("http://khseob0715.dothome.co.kr/mia/test.jpg")).start();
//            case R.id.select_image_0:(new DownThread("http://catchme.iptime.org/images/route.jpeg")).start();
                break;
        }
    }

    class DownThread extends Thread {
        String mAddr;

        DownThread(String addr) {
            mAddr = addr;
        }

        @Override
        public void run() {
            try {
                InputStream is = new URL(mAddr).openStream();
                bit = BitmapFactory.decodeStream(is);
                is.close();
                Message message =  mAfterDown.obtainMessage();
                message.obj = bit;
                mAfterDown.sendMessage(message);
//------------------------------------onActivityResult
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    Handler mAfterDown = new Handler()  {
        public void  handleMessage(Message msg) {
            Bitmap bit = (Bitmap)msg.obj;
            mImage = (ImageView)findViewById(R.id.image_0);
            if(bit==null)   {
                Toast.makeText(MainActivity.this, "bitmap is null", Toast.LENGTH_SHORT).show();
            }   else    {
                mImage.setImageBitmap(bit);
                clearDetectedFaces(0);
                detect(bit, 0);
            }
        }
    };

    // Called when the "Select Image1" button is clicked in face face verification.
    public void selectImage1(View view) {
        mSurface.mCamera.takePicture(null, null, mPicture);
        verify(null);
        // 여기서 selectimage()로 넘어가면 사진 고르거나 찍거나 두개가 뜸. 고로 이거 클릭시 바로 촬영가능하게 ㄱㄱ
        //selectImage();
    }

    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            String sd = Environment.getExternalStorageDirectory().getAbsolutePath();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            // 년월일시분초
            Date currentTime_1 = new Date();
            String filename = formatter.format(currentTime_1);
            String path = sd + "/RunToYou/" + filename + ".jpg";
            file = new File(path);

            try{
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(data);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.parse("file://" + path);
            intent.setData(uri);
            sendBroadcast(intent);
            //비트맵으로 변환하기
            Bitmap bitmap;
            BitmapFactory.Options options  = new BitmapFactory.Options();
            options.inSampleSize = 2;

            Matrix matrix = new Matrix();
            matrix.preRotate(90, 0, 0);
//--------------------------------------------------------------------------------------------------------------------------------------------------------
            bitmap = BitmapFactory.decodeFile(path, options);

            Log.e("bit", String.valueOf(bitmap.getWidth()));
            Log.e("bit", String.valueOf(bitmap.getHeight()));
            mImage1 = (ImageView)findViewById(R.id.image_1);
            mImage1.setImageBitmap(bitmap);

            if(bitmap == null)  {
                Log.e("aa","ssad"); }
            else    {
                Log.e("aa","bitmap is not null");
                sizesetup(1,RESULT_OK, uri);
            }
            //FaceVerify.verify();
//            Toast.makeText(MainActivity.this, "사진저장완료: "+path, Toast.LENGTH_SHORT).show();

        }

    };

    // Called when the "Verify" button is clicked.
    public void verify(View view) {
        setAllButtonEnabledStatus(false);
        handler.postDelayed(new ver(), 2000);
    }

    // Set the verify button is enabled or not.
    private void setVerifyButtonEnabledStatus(boolean isEnabled) {
     //   Button button = (Button) findViewById(R.id.verify);
      //  button.setEnabled(isEnabled);
    }

    // Set all the buttons are enabled or not.
    private void setAllButtonEnabledStatus(boolean isEnabled) {
        Button selectImage1 = (Button) findViewById(R.id.select_image_1);
        selectImage1.setEnabled(isEnabled);
        Button selectImage0 = (Button) findViewById(R.id.select_image_0);
        selectImage0.setEnabled(isEnabled);
    }

    // Initialize the ListView which contains the thumbnails of the detected faces.
    private void initializeFaceList(final int index) {
        ListView listView =
                (ListView) findViewById(index == 0 ? R.id.list_faces_0: R.id.list_faces_1);
        // When a detected face in the GridView is clicked, the face is selected to verify.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FaceListAdapter faceListAdapter =
                        index == 0 ? mFaceListAdapter0: mFaceListAdapter1;
                if (!faceListAdapter.faces.get(position).faceId.equals(
                        index == 0 ? mFaceId0: mFaceId1)) {
                    if (index == 0) {
                        mFaceId0 = faceListAdapter.faces.get(position).faceId;
                    } else {
                        mFaceId1 = faceListAdapter.faces.get(position).faceId;
                    }

                    ImageView imageView =
                            (ImageView) findViewById(index == 0 ? R.id.image_0: R.id.image_1);
                    imageView.setImageBitmap(faceListAdapter.faceThumbnails.get(position));
                    setInfo("");
                }

                // Show the list of detected face thumbnails.
                ListView listView = (ListView) findViewById(
                        index == 0 ? R.id.list_faces_0: R.id.list_faces_1);
                listView.setAdapter(faceListAdapter);
            }
        });
    }

    // Show the result on screen when verification is done.
    private void setUiAfterVerification(VerifyResult result) {
        // Verification is done, hide the progress dialog.
//        progressDialog.dismiss();
        // Enable all the buttons.
        setAllButtonEnabledStatus(true);

        // Show verification result.
        if (result != null) {
            DecimalFormat formatter = new DecimalFormat("#0.00");
            String verificationResult = (result.isIdentical ? "Same person": "Different persons")
                    + "/" + formatter.format(result.confidence)+ "\n";
            str += verificationResult;
            textview.setText(str);
            setInfo(verificationResult);
        }
    }

    // Show the result on screen when detection in image that indicated by index is done.
    private void setUiAfterDetection(Face[] result, int index, boolean succeed) {
//        setSelectImageButtonEnabledStatus(true, index);
        if (succeed) {

            addLog("Response: Success. Detected "
                    + result.length + " face(s) in image" + index);

            setInfo(result.length + " face" + (result.length != 1 ? "s": "")  + " detected");
            // Show the detailed list of detected faces.
            FaceListAdapter faceListAdapter = new FaceListAdapter(result, index);

            // Set the default face ID to the ID of first face, if one or more faces are detected.
            if (faceListAdapter.faces.size() != 0) {
                if (index == 0) {
                    mFaceId0 = faceListAdapter.faces.get(0).faceId;
                    setInfo("mFaceId0");
                }
                else {
                    mFaceId1 = faceListAdapter.faces.get(0).faceId;
                    setInfo("mFaceId1");
                    /*
                    if(mFaceId1 != null)    {
                        Toast.makeText(this, mFaceId1.toString(), Toast.LENGTH_SHORT);
                    }
                    */
                }
                // Show the thumbnail of the default face.
                ImageView imageView = (ImageView) findViewById(index == 0 ? R.id.image_0: R.id.image_1);
                imageView.setImageBitmap(faceListAdapter.faceThumbnails.get(0));
            }

            // Show the list of detected face thumbnails.
            ListView listView = (ListView) findViewById(
                    index == 0 ? R.id.list_faces_0: R.id.list_faces_1);
            listView.setAdapter(faceListAdapter);
            listView.setVisibility(View.VISIBLE);
            // Set the face list adapters and bitmaps.
            if (index == 0) {
                mFaceListAdapter0 = faceListAdapter;
                bit = null;
            } else {
                mFaceListAdapter1 = faceListAdapter;
                mBitmap1 = null;
            }
        }

        if (result != null && result.length == 0) {
            setInfo("No face detected!");
        }

        if ((index == 0 && mBitmap1 == null) || (index == 1 && bit == null) || index == 2) {
//            progressDialog.dismiss();
        }

        if (mFaceId0 != null && mFaceId1 != null) {
            setVerifyButtonEnabledStatus(true);
        }
    }

    // Start detecting in image specified by index.
    private void detect(Bitmap bitmap, int index) {
        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());
        // Start a background task to detect faces in the image.
        new DetectionTask(index).execute(inputStream);

        setInfo("Detecting...");
    }

    // Set the information panel on screen.
    private void setInfo(String info) {
        TextView textView = (TextView) findViewById(R.id.info);
        textView.setText(info);
    }

    // Add a log item.
    private void addLog(String log) {
        LogHelper.addVerificationLog(log);
    }

    // The adapter of the GridView which contains the thumbnails of the detected faces.
    private class FaceListAdapter extends BaseAdapter {
        // The detected faces.
        List<Face> faces;

        int mIndex;

        // The thumbnails of detected faces.
        List<Bitmap> faceThumbnails;

        // Initialize with detection result and index indicating on which image the result is got.
        FaceListAdapter(Face[] detectionResult, int index) {
            faces = new ArrayList<>();
            faceThumbnails = new ArrayList<>();
            mIndex = index;

            if (detectionResult != null) {
                faces = Arrays.asList(detectionResult);
                for (Face face: faces) {
                    try {
                        // Crop face thumbnail without landmarks drawn.
                        faceThumbnails.add(ImageHelper.generateFaceThumbnail(
                                index == 0 ? bit: mBitmap1, face.faceRectangle));
                    } catch (IOException e) {
                        // Show the exception when generating face thumbnail fails.
                        setInfo(e.getMessage());
                    }
                }
            }
        }

        @Override
        public int getCount() {
            return faces.size();
        }

        @Override
        public Object getItem(int position) {
            return faces.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater layoutInflater =
                        (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.item_face, parent, false);
            }
            convertView.setId(position);

            Bitmap thumbnailToShow = faceThumbnails.get(position);
            if (mIndex == 0 && faces.get(position).faceId.equals(mFaceId0)) {
                thumbnailToShow = ImageHelper.highlightSelectedFaceThumbnail(thumbnailToShow);
            } else if (mIndex == 1 && faces.get(position).faceId.equals(mFaceId1)){
                thumbnailToShow = ImageHelper.highlightSelectedFaceThumbnail(thumbnailToShow);
            }

            // Show the face thumbnail.
            ((ImageView)convertView.findViewById(R.id.image_face)).setImageBitmap(thumbnailToShow);

            return convertView;
        }
    }
    private class ver implements Runnable {
        @Override
        public void run() {
            if (WEB == "A") {
                mSurface.mCamera.takePicture(null, null, mPicture);
                new VerificationTask(mFaceId0, mFaceId1).execute();
//                Toast.makeText(getApplicationContext(), "verify", Toast.LENGTH_SHORT).show();

                handler.postDelayed(this, 2000);
            }
        }
    }

    @Override
    protected void onDestroy(){
        WEB = "B";
        super.onDestroy();
    }



}
