package com.juliosueiras.doconmeth;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import static android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import android.databinding.DataBindingUtil;

import android.support.design.widget.Snackbar;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.answers.Answers;

import com.crashlytics.android.Crashlytics;

import com.facebook.stetho.Stetho;

import com.orm.query.Condition;
import com.orm.query.Select;

import io.fabric.sdk.android.Fabric;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;

import com.juliosueiras.doconmeth.databinding.ActivityMainBinding;

/**
 * Home page activity
 */
public class MainActivity extends AppCompatActivity {

    Map<String, String> docLinkMap = new HashMap<String, String>();

    ActivityMainBinding binding;
    BroadcastReceiver onComplete;
    private String _currentDocZip;
    private ProgressDialog progress;

    public static boolean isDownloadManagerAvailable(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return true;
        }
        return false;
    }

    /**
     * AsyncTask for running uncompressing for docs
     */
    private class UncompressDocTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            progress.setTitle(_currentDocZip + " Uncompressing");
            progress.setMessage("Uncompressing(May take a while)");
            progress.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                File archive = new File("/sdcard/Download/" + _currentDocZip);
                File destination = new File("/sdcard/Download/");
                Archiver archiver = ArchiverFactory.createArchiver("tar", "gz");

                archiver.extract(archive, destination);
                archive.delete();

                return _currentDocZip;
            } catch(IOException e){ e.printStackTrace();
            }
            return "Finish";
        }

        @Override
        protected void onPostExecute(String result) {
            progress.hide();
            _showToast(result + " Uncompress");
        }
    }

    /**
     * helper method for showing a toast
     * @param msg the message to be feature in the toast
     */
    private void _showToast(String msg) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, msg, duration);
        toast.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Answers(), new Crashlytics());
        setContentView(R.layout.activity_main);

        Stetho.initializeWithDefaults(this);

        _loadLinkJSON();
        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_doc, docLinkMap.keySet().toArray(new String[docLinkMap.size()]));
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.docList.setAdapter(adapter);
        binding.docList.setOnItemClickListener(_createOnListItemClick());
        binding.docList.setOnItemLongClickListener(_createOnListItemLongClick());

        progress = new ProgressDialog(this);
        progress.setCancelable(false);



        onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {
                new UncompressDocTask().execute();
            }
        };


        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onComplete);
    }

    /**
     * Load documentation link from test.json under assets folder of the app
     */
    private void _loadLinkJSON() {
        JSONParser parser = new JSONParser();

        try {

            String jsonText = IOUtils.toString(getAssets().open("test.json"), "UTF-8");

            JSONArray docArray = (JSONArray) parser.parse(jsonText);

            Iterator<JSONObject> iterator = docArray.iterator();

            while (iterator.hasNext()) {
                JSONObject doc = (JSONObject) iterator.next();
                String name = (String)doc.get("docName");
                String path = (String)doc.get("docPath");
                docLinkMap.put(name, path);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    /**
     * helper method for downloading docs over network
     * @param docName documentation name
     * @param docLink documentation link
     * @param docZip documentation zip name
     */
    private void _downloadDoc(String docName, String docLink, String docZip) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(docLink));
        request.setDescription(docName + " Download");
        request.setTitle(docName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, docZip);

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        _currentDocZip = docZip;

        manager.enqueue(request);
    }

    protected OnItemLongClickListener _createOnListItemLongClick() {
        return (l, v, position,id) -> {
            _deleteDocDialog(position);
            return true;
        };
    }

    /**
     * Delete directory
     * @return whether deletion was successful
     */
    private boolean _deleteDirectory(File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    _deleteDirectory(files[i]);
                }
                else {
                    files[i].delete();
                }
            }
        }
        return( path.delete() );
    }

    /**
     * helper method for prompting deletion for the selected documentation
     * @param position the position of the selected documentation
     */
    private void _deleteDocDialog(int position) {
        String docName = binding.docList.getItemAtPosition(position).toString();
        String docLink = docLinkMap.get(docName);
        String docZip  = docLink.split("/")[4];
        String docDir  = docZip.split(".tgz")[0] + ".docset";

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Delete " + docName + "?");

        String positiveText = getString(android.R.string.yes);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File f = new File(Environment.getExternalStorageDirectory() + "/Download/" + docDir);
                        if(f.exists()) {
                            _deleteDirectory(f);

                            /*
                             * Delete the selected doc index
                             */
                            SearchIndex.deleteInTx(
                                    Select.from(SearchIndex.class)
                                    .where(Condition.prop("DOC_TYPE").eq(docZip.split(".tgz")[0])).list()
                                    );
                        }

                    }
                });

        String negativeText = getString(android.R.string.no);
        builder.setNegativeButton(negativeText, 
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    protected OnItemClickListener _createOnListItemClick() {
        return (l, v, position,id) -> {
            String docName = binding.docList.getItemAtPosition(position).toString();
            String docLink = docLinkMap.get(docName);
            String docZip  = docLink.split("/")[4];
            String docDir  = docZip.split(".tgz")[0] + ".docset";

            File f = new File(Environment.getExternalStorageDirectory() + "/Download/" + docDir);

            if(!f.isDirectory()) {

                File docZipFile = new File(Environment.getExternalStorageDirectory() + "/Download/" + docZip);
                if (docZipFile.isFile()) {
                    _currentDocZip = docZip;
                    new UncompressDocTask().execute();
                } else {
                    _currentDocZip = docZip;
                    if(isOnline()) {
                        _downloadDoc(docName,docLink,docZip);
                    } else {
                        Snackbar
                            .make(v, "No network connection", Snackbar.LENGTH_SHORT)
                            .show();
                    }
                }

            } else {

                Intent intent = new Intent(MainActivity.this, IndexActivity.class);
                intent.putExtra("currentDocDirName", docDir);
                intent.putExtra("currentDocName", docZip.split(".tgz")[0]);

                startActivity(intent);
            }

        };
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }

    /**
     * check if device is online
     * @return the status of network connectivity
     */
    private boolean isOnline() {
        ConnectivityManager cm =
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
