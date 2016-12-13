package com.juliosueiras.doconmeth;

import android.app.Activity;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import static android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
import com.juliosueiras.doconmeth.databinding.ActivityIndexBinding;
import com.orm.query.Condition;
import com.orm.query.Select;
import io.fabric.sdk.android.Fabric;

public class IndexActivity extends AppCompatActivity {

    ActivityIndexBinding binding;
	List<SearchIndex> currentDocIndexs;

    private void importCSV(String csvPath) {

        Intent intent = getIntent();
        String docName = intent.getStringExtra("currentDocName");

        try {
            File csvFile = new File(csvPath);
            if(!csvFile.isFile()) {
                try{
                    Process su = Runtime.getRuntime().exec("sh");
                    DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

                    outputStream.writeBytes("cd /sdcard/Download/" + intent.getStringExtra("currentDocDirName") + "/Contents/Resources");
                    outputStream.flush();

                    outputStream.writeBytes("sqlite3 -noheader -csv docSet.dsidx \"select * from searchIndex\" > test.csv");
                    outputStream.flush();

                    outputStream.writeBytes("exit\n");
                    outputStream.flush();
                    su.waitFor();
                }catch(IOException e){
                }catch(InterruptedException e){
                }
            }

            FileInputStream is = new FileInputStream(csvFile);

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] RowData = line.split(",");
                String name = RowData[1];
                String type = RowData[2];
                String path = RowData[3];
                SearchIndex searchIndex = new SearchIndex(name, type, path, docName);
                searchIndex.save();
                //do something with "data" and "value"
            }
        } catch (IOException ex) {
            // handle exception
        } finally {
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
        setContentView(R.layout.activity_index);

        Intent intent = getIntent();
        String docName = intent.getStringExtra("currentDocName");

        if(!(Select.from(SearchIndex.class)
            .where(Condition.prop("DOC_TYPE").eq(intent.getStringExtra("currentDocName")))
            .list().size() > 0)) {
            importCSV("/sdcard/Download/" + intent.getStringExtra("currentDocDirName") + "/Contents/Resources/test.csv");
        }

        ArrayList<String> values = new ArrayList<String>();
        currentDocIndexs = Select.from(SearchIndex.class)
            .where(Condition.prop("DOC_TYPE").eq(docName))
            .list();

        for (SearchIndex searchIndex : currentDocIndexs) {
            values.add(searchIndex.name);
        }

        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_doc, values) ;


        Stetho.initializeWithDefaults(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_index);
        binding.indexList.setAdapter(adapter);
        binding.indexList.setOnItemClickListener(_createOnListItemClick());

        // ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
        //         android.R.layout.simple_list_item_1, values);
	}

    protected OnItemClickListener _createOnListItemClick() {
        return (l, v, position,id) -> {
			binding.webview.loadUrl("file:///sdcard/Download/Emmet.docset/Contents/Resources/Documents/" + currentDocIndexs.get(position).path);
        };
    }

	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
	}
}


