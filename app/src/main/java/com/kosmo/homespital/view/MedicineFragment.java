package com.kosmo.homespital.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Block;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.Page;
import com.google.api.services.vision.v1.model.Paragraph;
import com.google.api.services.vision.v1.model.Symbol;
import com.google.api.services.vision.v1.model.TextAnnotation;
import com.google.api.services.vision.v1.model.Word;
import com.kosmo.homespital.R;
import com.kosmo.homespital.adapter.FoldingCellListAdapter;
import com.kosmo.homespital.adapter.FoldingCellPresListAdapter;
import com.kosmo.homespital.dialog.addtionalDialog;
import com.kosmo.homespital.model.PrescriptionItem;
import com.kosmo.homespital.model.ReservationItem;
import com.kosmo.homespital.vision.PackageManagerUtils;
import com.kosmo.homespital.vision.PermissionUtils;
import com.ramotion.foldingcell.FoldingCell;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import butterknife.BindView;
import butterknife.ButterKnife;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarListener;

public class MedicineFragment extends Fragment {

    private static final String CLOUD_VISION_API_KEY = "AIzaSyAQDghsz-goj4s3pYKrZ_t43MYU1yHbXVs";
    public static final String FILE_NAME = "temp.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final String TAG = "MedicineFragment";

    private static final int MAX_LABEL_RESULTS = 10;
    private static final int MAX_DIMENSION = 1200;

    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;

    private static float percent;

    private ArrayList<PrescriptionItem> presItems;
    private ArrayList<String> mediList;
    private static ArrayList<String> visionArr;

    private FoldingCellPresListAdapter presAdapter;
    private AlertDialog progressDialog;

    /*@BindView(R.id.reservationList)
    ListView reservationList;*/

    @BindView(R.id.prescriptionList)
    ListView prescriptionList;
    /*private TextView mImageDetails;
    private ImageView mMainImage;*/

    private static Context context;

    @BindView(R.id.addMedicine)
    Button medicineButton;

    @BindView(R.id.removeMedicine)
    Button removeButton;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.i("com.kosmo.homespital","onAttach:Medicine");
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i("com.kosmo.homespital","onCreateView:Medicine");

        View view = inflater.inflate(R.layout.medicine_tablayout,null,false);
        ButterKnife.bind(this,view);

        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*items = new ArrayList<ReservationItem>();
        items.add(
                new ReservationItem(
                        "삼성병원",
                        "내과",
                        "2020-07-07",
                        "15:00",
                        "기침",
                        "승인대기중",
                        "서울특별시 서대문구 창천동",
                        "2020-07-02"));

        FoldingCellListAdapter adapter = new FoldingCellListAdapter(context, items);
        reservationList.setAdapter(adapter);*/

        SharedPreferences autologin = context.getSharedPreferences("autologin",Context.MODE_PRIVATE);

        new LoadPreTask().execute(autologin.getString("email","yoonsj@gmail.com"));

        presItems = new ArrayList<PrescriptionItem>();
/*        presItems.add(
                new PrescriptionItem(
                        "빨간약",
                        "레드벨벳",
                        "분량 3개",
                        "냉장고에 저장",
                        "2222년",
                        "성상",
                        "COLOR_CLASS1",
                        "둥금",
                        "https://nedrug.mfds.go.kr/pbp/cmn/itemImageDownload/148674121065200058",
                        "2020-07-07",
                        "7",
                        "3"));
        presItems.add(
                new PrescriptionItem(
                        "빨간약",
                        "레드벨벳",
                        "분량 3개",
                        "냉장고에 저장",
                        "2222년",
                        "성상",
                        "COLOR_CLASS1",
                        "둥금",
                        "https://nedrug.mfds.go.kr/pbp/cmn/itemImageDownload/148674121065200058",
                        "2020-07-07",
                        "7",
                        "3"));
        presItems.add(
                new PrescriptionItem(
                        "빨간약",
                        "레드벨벳",
                        "분량 3개",
                        "냉장고에 저장",
                        "2222년",
                        "성상",
                        "COLOR_CLASS1",
                        "둥금",
                        "https://nedrug.mfds.go.kr/pbp/cmn/itemImageDownload/148674121065200058",
                        "2020-07-07",
                        "7",
                        "3"));*/

        presAdapter = new FoldingCellPresListAdapter(context, presItems);
        prescriptionList.setAdapter(presAdapter);

        prescriptionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                // toggle clicked cell state
                ((FoldingCell) view).toggle(false);
                // register in adapter that state for selected cell is toggled
                presAdapter.registerToggle(pos);
            }
        });

        /** end after 1 month from now */
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH, 1);

        /** start before 1 month from now */
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.MONTH, -1);

        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(view, R.id.calendarView)
                .startDate(startDate.getTime())
                .endDate(endDate.getTime())
                .datesNumberOnScreen(5)
                .dayNameFormat("EEE")
                .dayNumberFormat("dd")
                .monthFormat("MMM")
                .showDayName(true)
                .showMonthName(true)
                .build();

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Date date, int position) {
                if(presItems.size() > 0)
                {
                    Date preDate = null;
                    SimpleDateFormat format = new SimpleDateFormat("yyyy년 MM월 dd일");
                    try {
                        preDate = format.parse(presItems.get(0).getPres_date());
                        Calendar precal = Calendar.getInstance();
                        Calendar precalEnd = Calendar.getInstance();
                        Calendar cal = Calendar.getInstance();
                        precal.setTime(preDate);
                        precalEnd.setTime(preDate);
                        precalEnd.add(Calendar.DATE, Integer.parseInt(presItems.get(0).getCount()));
                        cal.setTime(date);
                        Log.i(TAG,"precal:"+precal.getTime());
                        Log.i(TAG,"precalEnd:"+precalEnd.getTime());
                        Log.i(TAG,"cal:"+cal.getTime());
                        Log.i(TAG,"inPre:"+((precal.getTime().getTime() < cal.getTime().getTime()) && (precalEnd.getTime().getTime() > cal.getTime().getTime())));
                        Log.i(TAG, String.valueOf(((precal.get(Calendar.MONTH)==cal.get(Calendar.MONTH)) && (precal.get(Calendar.DAY_OF_MONTH)==cal.get(Calendar.DAY_OF_MONTH)))));
                        if((precal.getTime().getTime() < cal.getTime().getTime()) && (precalEnd.getTime().getTime() > cal.getTime().getTime()))
                        {
                            prescriptionList.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            prescriptionList.setVisibility(View.GONE);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        medicineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder
                        .setMessage("사진을 선택해주세요")
                        .setPositiveButton("갤러리", (dialog, which) -> startGalleryChooser())
                        .setNegativeButton("카메라", (dialog, which) -> startCamera());
                builder.create().show();
            }
        });

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presItems.clear();
                mediList.clear();
                visionArr.clear();

                presAdapter.clear();


            }
        });
    }

    public void startGalleryChooser() {
        if (PermissionUtils.requestPermission((Activity) context, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "약봉투 사진을 선택해주세요"),
                    GALLERY_IMAGE_REQUEST);
        }
    }

    public void startCamera() {
        if (PermissionUtils.requestPermission(
                (Activity)context,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    public File getCameraFile() {
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Log.i(TAG,"dd:"+data.getData().toString());
            uploadImage(data.getData());
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            Uri photoUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", getCameraFile());
            Log.i(TAG,"dd:"+photoUri.toString());
            uploadImage(photoUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                }
                break;
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();
                }
                break;
        }
    }

    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                /*Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri),
                                MAX_DIMENSION);*/

//                Bitmap bitmap = BitmapFactory.decodeFile("/sdcard/Pictures/KakaoTalk/시연/1594209778175.jpg");

                Bitmap bitmap;

                if(uri.toString().contains("com.kosmo.homespital"))
                {
                    bitmap = BitmapFactory.decodeFile("/sdcard/Pictures/KakaoTalk/시연/1594209778175.jpg");
                }
                else
                {
                    bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                }

                Log.i(TAG,"갤러리:"+uri.toString().contains("com.android.providers"));
                Log.i(TAG,"카메라:"+uri.toString().contains("com.kosmo.homespital"));

                callCloudVision(bitmap);
//                mMainImage.setImageBitmap(bitmap);

            } catch (Exception e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(context, "이미지를 불러오는데 문제가 생겼습니다", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(context, "이미지를 불러오는데 문제가 생겼습니다", Toast.LENGTH_LONG).show();
        }
    }

    private Vision.Images.Annotate prepareAnnotationRequest(Bitmap bitmap) throws IOException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer =
                new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                    /**
                     * We override this so we can inject important identifying fields into the HTTP
                     * headers. This enables use of a restricted cloud platform API key.
                     */
                    @Override
                    protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                            throws IOException {
                        super.initializeVisionRequest(visionRequest);

                        String packageName = context.getPackageName();
                        visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                        String sig = PackageManagerUtils.getSignature(context.getPackageManager(), packageName);

                        visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                    }
                };

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();
        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            // Add the image
            Image base64EncodedImage = new Image();
            // Convert the bitmap to a JPEG
            // Just in case it's a format that Android understands but Cloud Vision
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Base64 encode the JPEG
            base64EncodedImage.encodeContent(imageBytes);
            annotateImageRequest.setImage(base64EncodedImage);

            // add the features we want
            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                Feature labelDetection = new Feature();
                labelDetection.setType("DOCUMENT_TEXT_DETECTION");
                labelDetection.setMaxResults(MAX_LABEL_RESULTS);
                add(labelDetection);
            }});

            // Add the list of one thing to the request
            add(annotateImageRequest);
        }});

        Vision.Images.Annotate annotateRequest =
                vision.images().annotate(batchAnnotateImagesRequest);
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotateRequest.setDisableGZipContent(true);
        Log.d(TAG, "created Cloud Vision request object, sending request");

        return annotateRequest;
    }

    private static class LableDetectionTask extends AsyncTask<Object, Void, ArrayList<String>> {
        private final WeakReference<Context> mActivityWeakReference;
        private Vision.Images.Annotate mRequest;

        LableDetectionTask(Context activity, Vision.Images.Annotate annotate) {
            mActivityWeakReference = new WeakReference<>(activity);
            mRequest = annotate;
        }

        private AlertDialog progressDialog;
        @Override
        protected void onPreExecute() {
            //프로그래스바용 다이얼로그 생성]
            //빌더 생성 및 다이얼로그창 설정
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(false);
            builder.setView(R.layout.progress);


            //빌더로 다이얼로그창 생성
            progressDialog = builder.create();
            progressDialog.show();
        }///////////onPreExecute

        @Override
        protected ArrayList<String> doInBackground(Object... params) {
            try {
                Log.d(TAG, "created Cloud Vision request object, sending request");
                BatchAnnotateImagesResponse response = mRequest.execute();
                return convertResponseToString(response);

            } catch (GoogleJsonResponseException e) {
                Log.d(TAG, "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.d(TAG, "failed to make API request because of other IOException " +
                        e.getMessage());
            }
//            return "Cloud Vision API request failed. Check logs for details.";
            return null;
        }

        protected void onPostExecute(ArrayList<String> result) {
            Context activity = mActivityWeakReference.get();
            if (activity != null && !((Activity)activity).isFinishing()) {
                /*TextView imageDetail = activity.findViewById(R.id.image_details);
                imageDetail.setText(result);*/
                Log.i(TAG,result.toString());
//                Toast.makeText(context,result.toString(),Toast.LENGTH_SHORT).show();
//                Log.d(TAG, "result : " + result);
            }



            //다이얼로그 닫기
            if(progressDialog!=null && progressDialog.isShowing())
                progressDialog.dismiss();
        }


    }

    private void callCloudVision(final Bitmap bitmap) {
        mediList.clear();
        // Switch text to loading
//        mImageDetails.setText(R.string.loading_message);

        // Do the real work in an async task, because we need to use the network anyway
        try {
            AsyncTask<Object, Void, ArrayList<String>> labelDetectionTask = new LableDetectionTask(context, prepareAnnotationRequest(bitmap));

            ArrayList<String> medi_names = labelDetectionTask.execute().get();
            Log.i(TAG,"medi_names:"+medi_names);
            String[] mediArr = medi_names.get(0).split(",");
            Log.i(TAG,"medi_names:"+medi_names.get(0).split(",").length);

            for(String medi : mediArr)
            {
                if(medi.length()>1)
                {
                    Log.i(TAG,"medi:"+medi);
                    mediList.add(medi);
                }
            }

            if(mediList.size() > 0)
            {
                try {
                    new LoadMediShapeTask().execute(String.valueOf(mediList.size()));
                }
                catch (Exception e)
                {
                    Toast.makeText(context,"API 통신이 지연되어 약 정보 로드를 실패했습니다.\r\n다시 시도해주세요",Toast.LENGTH_SHORT).show();
                }

            }
        } catch (IOException e) {
            Log.d(TAG, "failed to make API request because of other IOException " +
                    e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }

        Log.i(TAG,"originalWidth:"+originalWidth);
        Log.i(TAG,"originalHeight:"+originalHeight);
        Log.i(TAG,"resizedWidth:"+resizedWidth);
        Log.i(TAG,"resizedHeight:"+resizedHeight);

        percent = (resizedWidth-originalWidth)/(float)originalWidth;
        Log.i(TAG,"percent:"+percent);

        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private static float percentCal(float number)
    {
        return ((number * percent) + number);
    }

    private static ArrayList<String> convertResponseToString(BatchAnnotateImagesResponse response) {
        visionArr.clear();
        StringBuilder message = new StringBuilder();
        String medi1="",medi2="",medi3="",medi4="",medi5="",medi6="",medi7="",medi8="";
        String presDate = "";
        String duration = "";
        String hospital = "";
        String count = "";

        List<AnnotateImageResponse> responses = response.getResponses();
        for (AnnotateImageResponse res : responses) {
            if (res.getError() != null) {
                Log.i(TAG, String.format("Error: %s\n", res.getError().getMessage()));
                return null;
            }
            TextAnnotation labels = res.getFullTextAnnotation();
            if (labels != null) {
                for (Page page: labels.getPages()) {
                    String pageText = "";
                    for (Block block : page.getBlocks()) {
                        String blockText = "";
                        for (Paragraph para : block.getParagraphs()) {
                            Log.i(TAG, "para:" + para);
                            String paraText = "";
                            for (Word word: para.getWords()) {
                                float min_x = word.getBoundingBox().getVertices().get(0).getX();
                                float max_x = word.getBoundingBox().getVertices().get(2).getX();
                                float min_y = word.getBoundingBox().getVertices().get(0).getY();
                                float max_y = word.getBoundingBox().getVertices().get(2).getY();


                                /*약 제조일 */
                                if(min_x>=770 && max_x<=885 && min_y>=105 && max_y<=130) {
                                    System.out.println(word);
                                    for (Symbol symbol: word.getSymbols()) {
                                        presDate = presDate + symbol.getText();
                                    }
                                }
                                /*복용횟수*/
                                if(min_x>=540 && max_x<=582 && min_y>=252 && max_y<=281) {
                                    System.out.println(word);
                                    for (Symbol symbol: word.getSymbols()) {
                                        count = count + symbol.getText();
                                    }
                                }
                                /*복용기간*/
                                if(min_x>=120 && max_x<=158 && min_y>=110 && max_y<=130) {
                                    System.out.println(word);
                                    for (Symbol symbol: word.getSymbols()) {
                                        duration = duration + symbol.getText();
                                    }
                                }
                                /*진료기관*/
                                if(min_x>=320 && max_x<=465 && min_y>=125 && max_y<=155) {
                                    System.out.println(word);
                                    for (Symbol symbol: word.getSymbols()) {
                                        hospital = hospital + symbol.getText();
                                    }
                                }
                                /*의약품명1*/
                                if(min_x>=240 && max_x<=440 && min_y>=145 && max_y<=170) {
                                    for (Symbol symbol: word.getSymbols()) {
                                        medi1 = medi1 + symbol.getText();
                                    }

                                }
                                if(min_x>=240 && max_x<=440 && min_y>=275 && max_y<=300) {
                                    for (Symbol symbol: word.getSymbols()) {
                                        medi3 = medi3 + symbol.getText();
                                    }

                                }
                                if(min_x>=240 && max_x<=440 && min_y>=405 && max_y<=430) {
                                    for (Symbol symbol: word.getSymbols()) {
                                        medi5 = medi5 + symbol.getText();
                                    }

                                }
                                if(min_x>=240 && max_x<=440 && min_y>=535 && max_y<=560) {
                                    for (Symbol symbol: word.getSymbols()) {
                                        medi7 = medi7 + symbol.getText();
                                    }
                                }
                                /*의약품명2*/
                                if(min_x>=580 && max_x<=780 && min_y>=145 && max_y<=170) {
                                    for (Symbol symbol: word.getSymbols()) {
                                        medi2 = medi2 + symbol.getText();

                                    }

                                }
                                Log.i(TAG,"percentCal(580):"+percentCal(580));
                                Log.i(TAG,"percentCal(780):"+percentCal(780));
                                Log.i(TAG,"percentCal(275):"+percentCal(275));
                                Log.i(TAG,"percentCal(300):"+percentCal(300));
                                if(min_x>=585 && max_x<=780&& min_y>=275 && max_y<=300) {
                                    for (Symbol symbol: word.getSymbols()) {
                                        medi4 = medi4 + symbol.getText();

                                    }

                                }
                                if(min_x>=580 && max_x<=780 && min_y>=405 && max_y<=430) {
                                    for (Symbol symbol: word.getSymbols()) {
                                        medi6 = medi6 + symbol.getText();
                                    }

                                }
                                if(min_x>=580 && max_x<=780 && min_y>=535 && max_y<=560) {
                                    for (Symbol symbol: word.getSymbols()) {
                                        medi8 = medi8+ symbol.getText();
                                    }
                                }
                            }
                        }
                    }
                }
                String totalMedi = String.format("%s,%s,%s,%s", medi1,medi2,medi3,medi4);
                message.append(totalMedi);
                visionArr.add(message.toString());
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                SimpleDateFormat format = new SimpleDateFormat("yyyy년 MM월 dd일");
                String currDate = format.format(cal.getTime());
                visionArr.add(currDate);
                visionArr.add(duration);
                visionArr.add(hospital);
                visionArr.add(count);

                Log.i("com.kosmo.homespital","visionArr:"+visionArr);

//                SharedPreferences autologin = context.getSharedPreferences("autologin", Context.MODE_PRIVATE);
                //"mem_email="+params[1]+"&duration="+params[2]+"&count="+params[3]+"&medi_name="+params[4]+"&hos_name="+params[5];
                /*new InsertPresTask().execute("https://homespital.ngrok.io/proj/Android/Medicine/insertPres?"
                        ,autologin.getString("email","yoonsj@gmail.com")
                        ,duration
                        ,count
                        ,totalMedi
                        ,hospital);*/
            } else {
                message.append("nothing");
            }
        }

        return visionArr;
    }

    private class LoadMediShapeTask extends AsyncTask<String, Void, ArrayList<String>> {

        private ArrayList<String> connList;
        @Override
        protected void onPreExecute() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(false);
            builder.setView(R.layout.progress);
            //빌더로 다이얼로그창 생성
            progressDialog = builder.create();
            progressDialog.show();
            connList = new ArrayList<>();

        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {
            Log.i("com.kosmo.homespital","params[0]:"+params[0]);
            try {
                for(int i = 0; i<Integer.parseInt(params[0]); i++)
                {
                    StringBuffer buf = new StringBuffer();
                    Log.i("com.kosmo.homespital","mediList.get(i):"+mediList.get(i));
                    URL url = new URL(String.format("https://homespital.ngrok.io/proj/Android/Medicine/mediShape?encodeSearch=%s",mediList.get(i)));
                    HttpURLConnection conn=(HttpURLConnection)url.openConnection();
                    conn.setConnectTimeout(20000);
                    conn.setReadTimeout(20000);
                    int responseCode=conn.getResponseCode();
                    Log.i("com.kosmo.homespital","responseCode:"+responseCode);
                    if(responseCode ==HttpURLConnection.HTTP_OK){
                        //연결된 커넥션에서 서버에서 보낸 데이타 읽기
                        BufferedReader br =
                                new BufferedReader(
                                        new InputStreamReader(conn.getInputStream(),"UTF-8"));
                        String line;
                        while((line=br.readLine())!=null){
                            buf.append(line);
                            Log.i("com.kosmo.homespital","line:"+line);
                        }
                        br.close();
                    }
                    conn.disconnect();
                    connList.add(buf.toString());
                }
            }
            catch(Exception e){e.printStackTrace();}

            return connList;
        }

        protected void onPostExecute(ArrayList<String> result) {
            if(result !=null && result.size()!=0)
            {
                try {
                    Log.i(TAG,"약품 모양 로드 성공");

                    for(int i = 0; i<result.size(); i++) {
                        if(result.get(i).length()>1) {
                            PrescriptionItem prescriptionItem = new PrescriptionItem();
                            Log.i("com.kosmo.homespital", "result.get:" + result.get(i));
                            JSONObject json = new JSONObject(connList.get(i));
                            Log.i("com.kosmo.homespital", "result:" + connList.get(i));
                            Log.i("com.kosmo.homespital", "JSONObject:" + json);
                            Log.i("com.kosmo.homespital", "ITEM_NAME:" + json.getString("ITEM_NAME"));
                            prescriptionItem.setITEM_NAME(json.getString("ITEM_NAME"));
                            Log.i("com.kosmo.homespital", "ENTP_NAME:" + json.getString("ENTP_NAME"));
                            prescriptionItem.setENTP_NAME(json.getString("ENTP_NAME"));
                            Log.i("com.kosmo.homespital", "CHART:" + json.getString("CHART"));
                            prescriptionItem.setCHART(json.getString("CHART"));
                            Log.i("com.kosmo.homespital", "COLOR_CLASS1:" + json.getString("COLOR_CLASS1"));
                            prescriptionItem.setCOLOR_CLASS1(json.getString("COLOR_CLASS1"));
                            Log.i("com.kosmo.homespital", "DRUG_SHAPE:" + json.getString("DRUG_SHAPE"));
                            prescriptionItem.setDRUG_SHAPE(json.getString("DRUG_SHAPE"));
                            Log.i("com.kosmo.homespital", "ITEM_IMAGE:" + json.getString("ITEM_IMAGE"));
                            prescriptionItem.setITEM_IMAGE(json.getString("ITEM_IMAGE"));

                            prescriptionItem.setPres_date(visionArr.get(1));
                            prescriptionItem.setCount(visionArr.get(4));
                            prescriptionItem.setDuration(visionArr.get(2));

                            presItems.add(prescriptionItem);


                        }
                    }
                    Log.i("com.kosmo.homespital", "presItems:" + presItems);
                    new LoadMediInfoTask().execute();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            else{
                Log.i(TAG,"약품 모양 로드 실패");
            }
            /*if(progressDialog!=null && progressDialog.isShowing())
                progressDialog.dismiss();*/
        }
    }
    private class LoadMediInfoTask extends AsyncTask<String, Void, ArrayList<String>> {

        private ArrayList<String> connList;
        @Override
        protected void onPreExecute() {

            connList = new ArrayList<>();
        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {

            try {
                Log.i("com.kosmo.homespital", "presItems.size():" + presItems.size());
                for(int i = 0; i<presItems.size(); i++) {
                    Log.i("com.kosmo.homespital", "presItems.get(i):" + presItems.get(i).getITEM_NAME());
                    StringBuffer buf = new StringBuffer();
                    URL url = new URL(String.format("https://homespital.ngrok.io/proj/Android/Medicine/mediInfo?encodeSearch=%s", mediList.get(i)));
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(20000);
                    conn.setReadTimeout(20000);
                    int responseCode = conn.getResponseCode();
                    Log.i("com.kosmo.homespital", "responseCode:" + responseCode);
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        //연결된 커넥션에서 서버에서 보낸 데이타 읽기
                        BufferedReader br =
                                new BufferedReader(
                                        new InputStreamReader(conn.getInputStream(), "UTF-8"));
                        String line;
                        while ((line = br.readLine()) != null) {
                            buf.append(line);
                            Log.i("com.kosmo.homespital", "line:" + line);
                        }
                        br.close();
                    }
                    conn.disconnect();
                    connList.add(buf.toString());
                }
            }
            catch(Exception e){e.printStackTrace();}

            for(int i = 0; i<connList.size(); i++) {
                Log.i("com.kosmo.homespital", "connList:" + connList.get(i));
            }

            return connList;
        }

        protected void onPostExecute(ArrayList<String> result) {
            if(result !=null && result.size()!=0)
            {
                try {
                    Log.i(TAG,"약품 정보 로드 성공");

                    for(int i = 0; i<result.size(); i++) {
                        int index = i;
                        if(result.get(i).length()>1) {
                            JSONObject json = new JSONObject(result.get(i));
                            Log.i("com.kosmo.homespital", "infoResult:" + result);
                            Log.i("com.kosmo.homespital", "JSONObject:" + json);
                            Log.i("com.kosmo.homespital", "MATERIAL_NAME:" + json.getString("MATERIAL_NAME"));
                            presItems.get(i).setMATERIAL_NAME(json.getString("MATERIAL_NAME"));
                            Log.i("com.kosmo.homespital", "STORAGE_METHOD:" + json.getString("STORAGE_METHOD"));
                            presItems.get(i).setSTORAGE_METHOD(json.getString("STORAGE_METHOD"));
                            Log.i("com.kosmo.homespital", "VALID_TERM:" + json.getString("VALID_TERM"));
                            presItems.get(i).setVALID_TERM(json.getString("VALID_TERM"));

                            Log.i("com.kosmo.homespital", "EFFECT:" + json.getString("EE_DOC_DATA"));
                            presItems.get(i).setEFFECT(json.getString("EE_DOC_DATA"));
                            Log.i("com.kosmo.homespital", "USAGE:" + json.getString("UD_DOC_DATA"));
                            presItems.get(i).setUSAGE(json.getString("UD_DOC_DATA"));
                            Log.i("com.kosmo.homespital", "CAREFUL:" + json.getString("NB_DOC_DATA"));
                            presItems.get(i).setCAREFUL(json.getString("NB_DOC_DATA"));

                            presItems.get(i).setAddtionalBtnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    addtionalDialog dialog = new addtionalDialog(context);
                                    dialog.show();
                                    dialog.setEffect(presItems.get(index).getEFFECT());
                                    dialog.setUsage(presItems.get(index).getUSAGE());
                                    dialog.setCareful(presItems.get(index).getCAREFUL());
                                }
                            });
//                    presItems.add(prescriptionItem);
                        }
                    }
                    Log.i("com.kosmo.homespital", "presItems:" + presItems);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            else{
                Log.i(TAG,"약품 정보 로드 실패");
            }

            presAdapter.notifyDataSetChanged();
            if(progressDialog!=null && progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }

    private class LoadPreTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {//네트워크에 연결하는 과정이 있으므로 다른 스레드에서 실행되어야 한다.
            StringBuffer buf = new StringBuffer();
            try {
                URL url = new URL(String.format("https://homespital.ngrok.io/proj/Android/Medicine/getPres?mem_email=%s",params[0]));

                HttpURLConnection conn=(HttpURLConnection)url.openConnection();
                conn.setConnectTimeout(7000);
                conn.setReadTimeout(7000);
                //서버에 요청 및 응답코드 받기
                int responseCode=conn.getResponseCode();
                Log.i("com.kosmo.homespital","responseCode:"+responseCode);
                if(responseCode ==HttpURLConnection.HTTP_OK){
                    //연결된 커넥션에서 서버에서 보낸 데이타 읽기
                    BufferedReader br =
                            new BufferedReader(
                                    new InputStreamReader(conn.getInputStream(),"UTF-8"));
                    String line;
                    while((line=br.readLine())!=null){
                        buf.append(line);
                        Log.i("com.kosmo.homespital","line:"+line);
                    }
                    br.close();
                }
                conn.disconnect();
            }
            catch(Exception e){e.printStackTrace();}


            return buf.toString();
        }

        protected void onPostExecute(String result) {//doInBackground 에서 리턴된 값이 여기로 들어온다.
            if(result !=null && result.length()!=0)
            {
                try
                {
                    Log.i("com.kosmo.homespital","처방전 정보 로드 성공");

                    JSONArray json = new JSONArray(result);
                    Log.i("com.kosmo.homespital","result:"+result);
                    Log.i("com.kosmo.homespital","JSONArray:"+json);

                    String[] mediArr = json.getJSONObject(0).getString("MEDI_NAME").split(",");
                    Log.i(TAG,"medi_names:"+json.getJSONObject(0).getString("MEDI_NAME").split(",").length);

                    mediList = new ArrayList<>();
                    visionArr = new ArrayList<>();

                    visionArr.add(json.getJSONObject(0).getString("MEDI_NAME"));
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(new Date());
                    SimpleDateFormat format = new SimpleDateFormat("yyyy년 MM월 dd일");
                    String currDate = format.format(cal.getTime());
                    visionArr.add(currDate);
                    visionArr.add(json.getJSONObject(0).getString("DURATION"));
                    visionArr.add(json.getJSONObject(0).getString("HOS_NAME"));
                    visionArr.add(json.getJSONObject(0).getString("DURATION"));

                    Log.i("com.kosmo.homespital","visionArr:"+visionArr);

                    for(String medi : mediArr)
                    {
                        if(medi.length()>1)
                        {
                            Log.i(TAG,"medi:"+medi);
                            mediList.add(medi);
                        }
                    }

                    if(mediList.size() > 0)
                    {
                        try {
                            new LoadMediShapeTask().execute(String.valueOf(mediList.size()));
                        }
                        catch (Exception e)
                        {
                            Toast.makeText(context,"API 통신이 지연되어 약 정보 로드를 실패했습니다.\r\n다시 시도해주세요",Toast.LENGTH_SHORT).show();
                        }

                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            else{
                Log.i("com.kosmo.homespital","처방전 정보 로드 실패");
            }

            //다이얼로그 닫기
            if(progressDialog!=null && progressDialog.isShowing())
                progressDialog.dismiss();

        }
    }

    private static class InsertPresTask extends AsyncTask<String, Void, String> {
        private String sendMsg, receiveMsg;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {//네트워크에 연결하는 과정이 있으므로 다른 스레드에서 실행되어야 한다.
            try{
                URL url = new URL(String.format("%s",params[0]));
                HttpURLConnection conn=(HttpURLConnection)url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "mem_email="+params[1]+"&duration="+params[2]+"&count="+params[3]+"&medi_name="+params[4]+"&hos_name="+params[5];
                osw.write(sendMsg);
                osw.flush();
                //서버에 요청 및 응답코드 받기
                int responseCode=conn.getResponseCode();

                if(responseCode ==HttpURLConnection.HTTP_OK){
                    //연결된 커넥션에서 서버에서 보낸 데이타 읽기
                    BufferedReader br =
                            new BufferedReader(
                                    new InputStreamReader(conn.getInputStream(),"UTF-8"));
                    String line;
                    StringBuffer buffer = new StringBuffer();
                    while((line=br.readLine())!=null){
                        buffer.append(line);
                    }
                    receiveMsg = buffer.toString();
                    br.close();
                }
            }
            catch(Exception e){e.printStackTrace();}
            return receiveMsg;
        }

        protected void onPostExecute(String content) {//doInBackground 에서 리턴된 값이 여기로 들어온다.
            if(content !=null && content.length()!=0) {//회원가입 성공한 경우
                try {
                    Toast.makeText(context,"처방전 등록이 성공했습니다",Toast.LENGTH_SHORT).show();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            else{//회원이 아닌 경우
                Toast.makeText(context,"처방전 등록이 실패했습니다",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
