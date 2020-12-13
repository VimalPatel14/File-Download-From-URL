package com.vimal.filedownload;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tedpark.tedpermission.rx2.TedRx2Permission;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final int progress_bar_type = 0;
    ArrayList<String> file;
    public static ArrayList arrayList = new ArrayList();
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.single).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TedRx2Permission.with(MainActivity.this)
                        .setRationaleTitle("Can we read your storage?")
                        .setRationaleMessage("We need your permission to access your storage and pick image")
                        .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .request()
                        .subscribe(permissionResult -> {
                                    if (permissionResult.isGranted()) {
                                        new DownloadTask(MainActivity.this, "Pate Your URL Here");
                                    } else {
                                        Toast.makeText(getBaseContext(),
                                                "Permission Denied\n" + permissionResult.getDeniedPermissions().toString(), Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                }, throwable -> {
                                }
                        );
            }
        });

        findViewById(R.id.multiple).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TedRx2Permission.with(MainActivity.this)
                        .setRationaleTitle("Can we read your storage?")
                        .setRationaleMessage("We need your permission to access your storage and pick image")
                        .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .request()
                        .subscribe(permissionResult -> {
                                    if (permissionResult.isGranted()) {
                                        file = new ArrayList<String>();
                                        file.add("Pate Your URL Here");
                                        file.add("Pate Your URL Here");
                                        file.add("Pate Your URL Here");
                                        file.add("Pate Your URL Here");
                                        file.add("Pate Your URL Here");
                                        file.add("Pate Your URL Here");

                                        String[] ur = {file.get(0), file.get(1), file.get(2), file.get(3), file.get(4), file.get(5)};
                                        new DownloadFileFromURL().execute(ur);
                                    } else {
                                        Toast.makeText(getBaseContext(),
                                                "Permission Denied\n" + permissionResult.getDeniedPermissions().toString(), Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                }, throwable -> {
                                }
                        );
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type:
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading Image! Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(false);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }

    class DownloadFileFromURL extends AsyncTask<String, Integer, String> {

        File apkStorage = null;
        File outputFile = null;
        String downloadFileName = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
        }

        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {

                for (int i = 0; i < f_url.length; i++) {

                    String fileName = f_url[i].substring(f_url[i].lastIndexOf('/') + 1);

                    if (new CheckForSDCard().isSDCardPresent()) {
                        apkStorage = new File(Environment.getExternalStorageDirectory() + "/" + ".vimal");
                    } else {
                        Toast.makeText(MainActivity.this, "Oops!! There is no SD Card.", Toast.LENGTH_SHORT).show();
                    }
                    if (!apkStorage.exists()) {
                        apkStorage.mkdir();
                        Log.e("tag", "Directory Created.");
                    }
                    downloadFileName = fileName;

                    Log.e("response", downloadFileName + " downloadFileName");

                    outputFile = new File(apkStorage, downloadFileName);//Create Output file in Main File
                    Log.e("response", outputFile + " outputFile");
                    if (!outputFile.exists()) {

                        URL url = new URL(f_url[i]);
                        URLConnection conection = url.openConnection();
                        conection.connect();
                        // getting file length
                        int lenghtOfFile = conection.getContentLength();

                        outputFile.createNewFile();

                        Log.e("response", " file not exist");

                        Log.e("tag", "File Created");
                        arrayList.add(outputFile.getPath());
                        // input stream to read file - with 8k buffer
                        InputStream input = new BufferedInputStream(
                                url.openStream(), 8192);
                        // Output stream to write file
                        OutputStream output = new FileOutputStream(outputFile);

                        byte data[] = new byte[1024];

                        long total = 0;

                        while ((count = input.read(data)) != -1) {
                            total += count;
                            // publishing the progress....
                            // After this onProgressUpdate will be called
                            publishProgress((int) ((total * 100) / lenghtOfFile));

                            // writing data to file
                            output.write(data, 0, count);
                        }

                        // flushing output
                        output.flush();


                        // closing streams
                        output.close();
                        input.close();

                        refreshAndroidGallery(Uri.fromFile(outputFile));
                    } else {
                        Log.e("response", " file already exist");
                        arrayList.add(outputFile.getPath());
                    }
                }
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(Integer... progress) {
            // setting progress percentage
            pDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded

            if (file.size() == arrayList.size()) {
                dismissDialog(progress_bar_type);
                Toast.makeText(MainActivity.this, "File Download Complete", Toast.LENGTH_SHORT).show();
            }


            // Displaying downloaded image into image view
            // Reading image path from sdcard
        }

    }

    public void refreshAndroidGallery(Uri fileUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent mediaScanIntent = new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(fileUri);
            sendBroadcast(mediaScanIntent);
        } else {
            sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
    }

    class DownloadTask {

        private static final String TAG = "Download Task";
        private Context context;
        private String downloadUrl = "", downloadFileName = "";
        private ProgressDialog progressDialog;

        public DownloadTask(Context context, String downloadUrl) {
            this.context = context;
            this.downloadUrl = downloadUrl;
            downloadFileName = downloadUrl.substring(downloadUrl.lastIndexOf('/'));//Create file name by picking download file name from URL
            //Start Downloading Task
            new DownloadingTask().execute();
        }

        private class DownloadingTask extends AsyncTask<Void, Void, Void> {

            File apkStorage = null;
            File outputFile = null;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Downloading...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            protected void onPostExecute(Void result) {
                try {
                    if (outputFile != null) {
                        progressDialog.dismiss();


//                        arrayList.add(outputFile.getPath());
                        Toast.makeText(MainActivity.this, "File Download Complete", Toast.LENGTH_SHORT).show();
                    } else {

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                            }
                        }, 500);
                        progressDialog.dismiss();
                        Toast.makeText(context, "Downloaded Failed", Toast.LENGTH_SHORT).show();

                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    //Change button text if exception occurs

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                        }
                    }, 500);
                    progressDialog.dismiss();
                    Toast.makeText(context, "Downloaded Failed", Toast.LENGTH_SHORT).show();

                }


                super.onPostExecute(result);
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    URL url = new URL(downloadUrl);//Create Download URl
                    HttpURLConnection c = (HttpURLConnection) url.openConnection();//Open Url Connection
                    c.setRequestMethod("GET");//Set Request Method to "GET" since we are grtting data
                    c.connect();//connect the URL Connection

                    //If Connection response is not OK then show Logs
                    if (c.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        Log.e(TAG, "Server returned HTTP " + c.getResponseCode()
                                + " " + c.getResponseMessage());

                    }
                    if (new CheckForSDCard().isSDCardPresent()) {
                        apkStorage = new File(Environment.getExternalStorageDirectory() + "/" + ".vimal");
                    } else
                        Toast.makeText(context, "Oops!! There is no SD Card.", Toast.LENGTH_SHORT).show();

                    //If File is not present create directory
                    if (!apkStorage.exists()) {
                        apkStorage.mkdir();
                        Log.e(TAG, "Directory Created.");
                    }

                    outputFile = new File(apkStorage, downloadFileName);//Create Output file in Main File

                    //Create New File if not present
                    if (!outputFile.exists()) {
                        outputFile.createNewFile();
                        FileOutputStream fos = new FileOutputStream(outputFile);//Get OutputStream for NewFile Location

                        InputStream is = c.getInputStream();//Get InputStream for connection

                        byte[] buffer = new byte[1024];//Set buffer type
                        int len1 = 0;//init length
                        while ((len1 = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, len1);//Write new file
                        }
                        Log.e(TAG, "File Created");
                        fos.close();
                        is.close();
                    }
                    //Close all connection after doing task
                } catch (Exception e) {
                    //Read exception if something went wrong
                    e.printStackTrace();
                    outputFile = null;
                    Log.e(TAG, "Download Error Exception " + e.getMessage());
                }

                return null;
            }
        }
    }

}