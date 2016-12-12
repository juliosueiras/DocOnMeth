package com.juliosueiras.doconmeth;

import android.app.DownloadManager;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.WebView;
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
import java.util.List;
import java.util.zip.GZIPInputStream;

import com.orm.query.Select;
import com.orm.query.Condition;

import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;

import android.databinding.DataBindingUtil;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.facebook.stetho.Stetho;
import com.juliosueiras.doconmeth.databinding.ActivityMainBinding;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends ListActivity {

    ActivityMainBinding binding;
	BroadcastReceiver onComplete;

	/**
	 * @param context used to check the device version and DownloadManager information
	 * @return true if the download manager is available
	 */
	public static boolean isDownloadManagerAvailable(Context context) {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			return true;
		}
		return false;
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
        Stetho.initializeWithDefaults(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

         onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {

                try {
                    File archive = new File("/sdcard/Download/HTML.tgz");
                    File destination = new File("/sdcard/Download/");

                    Archiver archiver = ArchiverFactory.createArchiver("tar", "gz");
                    archiver.extract(archive, destination);
                    archive.delete();


                    _showToast("Finish Uncompress");
                } catch(IOException e){ e.printStackTrace();
                }

            }
        };


        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

			ArrayList<String> values = new ArrayList<String>();
			List<SearchIndex> searchIndexs = SearchIndex.listAll(SearchIndex.class);

			for (SearchIndex searchIndex : searchIndexs) {
				values.add(searchIndex.name);
			}


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, values);


        // Assign adapter to List
        setListAdapter(adapter);
		// WebView webview = new WebView(this);
		// setContentView(webview);
    }

	@Override
	public void onDestroy() {
		super.onDestroy();
        unregisterReceiver(onComplete);
	}


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        super.onListItemClick(l, v, position, id);

        // ListView Clicked item index
		File f = new File(Environment.getExternalStorageDirectory() + "/Download/HTML.docset");
		if(!f.isDirectory()) {
			String url = "http://sanfrancisco.kapeli.com/feeds/HTML.tgz";
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
			request.setDescription("HTML Download");
			request.setTitle("HTML");

			// in order for this if to run, you must use the android 3.2 to compile your app
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				request.allowScanningByMediaScanner();
				request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
			}
			request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "HTML.tgz");

			// get download service and enqueue file
			DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
			manager.enqueue(request);
		} else {
			SearchIndex searchIndex = Select.from(SearchIndex.class).where(Condition.prop("id").eq(position)).list().get(0);
			binding.webview.loadUrl("file:///sdcard/Download/HTML.docset/Contents/Resources/Documents/" + searchIndex.type);
		}

        // // ListView Clicked item value
        // Answers.getInstance().logCustom(new CustomEvent("User click on an Item ")
        //         .putCustomAttribute("Item Value",itemValue));
        // binding.output.setText("Click : \n  Position :"+itemPosition+"  \n  ListItem : " +itemValue);
    }

}

















