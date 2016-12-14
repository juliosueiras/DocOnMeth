package com.juliosueiras.doconmeth;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import static android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import android.databinding.DataBindingUtil;

import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.answers.Answers;

import com.crashlytics.android.Crashlytics;

import com.facebook.stetho.Stetho;

import com.orm.query.Condition;
import com.orm.query.Select;

import io.fabric.sdk.android.Fabric;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Process;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.juliosueiras.doconmeth.databinding.ActivityIndexBinding;

public class IndexActivity extends AppCompatActivity {

    ActivityIndexBinding binding;
	Map<String, SearchIndex> currentDocIndexs = new HashMap<String, SearchIndex>();
	private ArrayAdapter adapter;

	private static String _getValue(String tag, Element element) {
		NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
		Node node = nodeList.item(0);
		return node.getNodeValue();
	}

	private void importXml(File xmlFile) {
		try {
			Intent intent = getIntent();
			String docName = intent.getStringExtra("currentDocName");

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new FileInputStream(xmlFile));

			Element element=doc.getDocumentElement();
			element.normalize();

			NodeList nList = doc.getElementsByTagName("Token");

			for (int i=0; i<nList.getLength(); i++) {

				Node token = nList.item(i);
				if (token.getNodeType() == Node.ELEMENT_NODE) {
					Element e2 = (Element) token;
					String name = _getValue("Name", e2);
					String type = _getValue("Type", e2);
					String path = _getValue("Path", e2);
					SearchIndex searchIndex = new SearchIndex(name, type, path, docName);
					searchIndex.save();
				}
			}

		} catch(Exception e){
			e.printStackTrace();
		}
	}

    private void importCSV(String csvPath) {
        Intent intent = getIntent();
        String docName = intent.getStringExtra("currentDocName");

        try {
            File csvFile = new File(csvPath);
            if(!csvFile.isFile()) {
                try{
                    Process sh = Runtime.getRuntime().exec("sh");
                    DataOutputStream outputStream = new DataOutputStream(sh.getOutputStream());

                    outputStream.writeBytes("cd /sdcard/Download/" + intent.getStringExtra("currentDocDirName") + "/Contents/Resources");
                    outputStream.flush();

                    outputStream.writeBytes("sqlite3 -noheader -csv docSet.dsidx \"select * from searchIndex\" > test.csv");
                    outputStream.flush();

                    outputStream.writeBytes("exit\n");
                    outputStream.flush();
                    sh.waitFor();
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
		getSupportActionBar().setTitle(docName);



        if(!(Select.from(SearchIndex.class)
            .where(Condition.prop("DOC_TYPE").eq(intent.getStringExtra("currentDocName")))
            .list().size() > 0)) {

			File tokenFile = new File("/sdcard/Download/" + intent.getStringExtra("currentDocDirName") + "/Contents/Resources/Tokens.xml");
			if(tokenFile.isFile()) {
				importXml(tokenFile);
			} else {
				importCSV("/sdcard/Download/" + intent.getStringExtra("currentDocDirName") + "/Contents/Resources/test.csv");
			}
        }

        ArrayList<String> values = new ArrayList<String>();
        List<SearchIndex> searchIndexs = Select.from(SearchIndex.class)
            .where(Condition.prop("DOC_TYPE").eq(docName))
            .list();

        for (SearchIndex searchIndex : searchIndexs) {
            values.add(searchIndex.name);
            currentDocIndexs.put(searchIndex.name , searchIndex);
        }

        adapter = new ArrayAdapter<String>(this,
                R.layout.activity_doc, values) ;


        Stetho.initializeWithDefaults(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_index);
        binding.indexList.setAdapter(adapter);
        binding.indexList.setOnItemClickListener(_createOnListItemClick());

		binding.edtSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
				IndexActivity.this.adapter.getFilter().filter(cs);
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }

			@Override
			public void afterTextChanged(Editable arg0) {}
		});

        // ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
        //         android.R.layout.simple_list_item_1, values);
	}

    protected OnItemClickListener _createOnListItemClick() {
        return (l, v, position,id) -> {
			binding.webview.loadUrl("file:///sdcard/Download/" + getIntent().getStringExtra("currentDocDirName") + "/Contents/Resources/Documents/" + currentDocIndexs.get(binding.indexList.getItemAtPosition((int)id)).path);
        };
    }

	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
	}
}


