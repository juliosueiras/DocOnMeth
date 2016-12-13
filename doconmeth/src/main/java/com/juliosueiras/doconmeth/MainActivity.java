package com.juliosueiras.doconmeth;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import static android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Process;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.facebook.stetho.Stetho;
import com.juliosueiras.doconmeth.databinding.ActivityMainBinding;
import com.orm.query.Condition;
import com.orm.query.Select;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {
    // String[] docList = {"Handlebars","IPhone","WindowsMobile","Blackberry",
    //     "WebOS","Ubuntu","Windows7","Max OS X"};

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

	private class UncompressDocTask extends AsyncTask<String, Void, String> {
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
			_showToast(result + " Uncompress");
		}
	}

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

		progress = new ProgressDialog(this);
		progress.setTitle("Uncompressing");
		progress.setMessage("Uncompressing(May take a while)");
		progress.setCancelable(false);

        docLinkMap.put("Emmet.io", "http://london.kapeli.com/feeds/Emmet.tgz");
		docLinkMap.put("Akka", "http://newyork.kapeli.com/feeds/Akka.tgz");
    //     "WebOS","Ubuntu","Windows7","Max OS X"};

        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_doc, docLinkMap.keySet().toArray(new String[docLinkMap.size()]));


        Stetho.initializeWithDefaults(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.docList.setAdapter(adapter);
        binding.docList.setOnItemClickListener(_createOnListItemClick());

        onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {
				new UncompressDocTask().execute();

            }
        };


        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

			ArrayList<String> values = new ArrayList<String>();
			List<SearchIndex> searchIndexs = SearchIndex.listAll(SearchIndex.class);

			for (SearchIndex searchIndex : searchIndexs) {
				values.add(searchIndex.name);
			}


        // ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
        //         android.R.layout.simple_list_item_1, values);
    }

	@Override
	public void onDestroy() {
		super.onDestroy();
        unregisterReceiver(onComplete);
	}


    protected OnItemClickListener _createOnListItemClick() {
        return (l, v, position,id) -> {
			String docName = binding.docList.getItemAtPosition(position).toString();
			String docLink = docLinkMap.get(docName);
			String docZip  = docLink.split("/")[4];
			String docDir  = docZip.split(".tgz")[0] + ".docset";

            // super.onListItemClick(l, v, position, id);

			//ListView Clicked item index
            File f = new File(Environment.getExternalStorageDirectory() + "/Download/" + docDir);
            if(!f.isDirectory()) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(docLink));
                request.setDescription(docName + " Download");
                request.setTitle(docName);

                // in order for this if to run, you must use the android 3.2 to compile your app
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                }
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, docZip);

                // get download service and enqueue file
                DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

				_currentDocZip = docZip;

                manager.enqueue(request);
            } else {
                // SearchIndex searchIndex = Select.from(SearchIndex.class).where(Condition.prop("id").eq(position)).list().get(0);
                // binding.webview.loadUrl("file:///sdcard/Download/" + docDir + "/Contents/Resources/Documents/index.html");
            }

            // // ListView Clicked item value
            // Answers.getInstance().logCustom(new CustomEvent("User click on an Item ")
            //         .putCustomAttribute("Item Value",itemValue));
            // binding.output.setText("Click : \n  Position :"+itemPosition+"  \n  ListItem : " +itemValue);
			Intent intent = new Intent(MainActivity.this, IndexActivity.class);
            intent.putExtra("currentDocName", docDir);
            intent.putExtra("currentDocDirName", docZip.split(".tgz")[0]);
			startActivity(intent);
        };
    }

	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
	}
}
