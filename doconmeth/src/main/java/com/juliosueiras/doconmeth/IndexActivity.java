package com.juliosueiras.doconmeth;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Window;
import static android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import android.databinding.DataBindingUtil;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;

import com.crashlytics.android.answers.Answers;

import com.crashlytics.android.Crashlytics;

import com.facebook.stetho.Stetho;

import com.orm.SugarApp;
import com.orm.SugarDb;
import com.orm.query.Condition;
import com.orm.query.Select;

import io.fabric.sdk.android.Fabric;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Process;
import java.lang.Runtime;
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

import com.juliosueiras.doconmeth.SearchIndex;
import com.juliosueiras.doconmeth.databinding.ActivityIndexBinding;

/**
 * Index Activity(the documentation index page)
 */
public class IndexActivity extends AppCompatActivity {

    private ActivityIndexBinding binding;
    private ArrayAdapter<String> adapter;

    /**
     * An Map that contain the current doc indexs(the list of keyword), retrieve by giving the keyword
     * TODO: Require further optimization.
     */
    Map<String, SearchIndex> currentDocIndexs = new HashMap<String, SearchIndex>();

    /**
     * Important helper method for processing the doc index using their individual sqlite database
     * @param docPath the path to the documentation directory
     */
    private void _importIndex(String docPath) {
        Intent intent = getIntent();
        String docName = intent.getStringExtra("currentDocName");

        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(docPath + "/docSet.dsidx", null);

        File tokenFile = new File(docPath + "/Tokens.xml");
        SearchIndex searchIndex;
        if(!tokenFile.isFile()) {
            String query = "select name, type, path from searchIndex";
            Cursor cursor = db.rawQuery(query, null);
            while(cursor.moveToNext()) {
                searchIndex = new SearchIndex(cursor.getString(0), cursor.getString(1), cursor.getString(2), docName);
                searchIndex.save();
            }
        } else {
            String query = "SELECT ZTOKENNAME, ZPATH FROM ZTOKENMETAINFORMATION JOIN ZFILEPATH, ZTOKEN ON ZTOKENMETAINFORMATION.ZFILE = ZFILEPATH.Z_PK AND ZTOKENMETAINFORMATION.ZTOKEN = ZTOKEN.Z_PK";
            Cursor cursor = db.rawQuery(query, null);
            while(cursor.moveToNext()) {
                searchIndex = new SearchIndex(cursor.getString(0), "", cursor.getString(1), docName);
                searchIndex.save();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Answers(), new Crashlytics());
        setContentView(R.layout.activity_index);

        Stetho.initializeWithDefaults(this);

        Intent intent = getIntent();
        String docName = intent.getStringExtra("currentDocName");

        if(!(_isDocPopulated(intent))) {
            _importIndex("/sdcard/Download/" + intent.getStringExtra("currentDocDirName") + "/Contents/Resources");
        }


        ArrayList<String> values = new ArrayList<String>();

        List<SearchIndex> searchIndexs =
            Select.from(SearchIndex.class)
                .where(Condition.prop("DOC_TYPE").eq(docName))
                .list();

        if (values.size() == 0) {
            for (SearchIndex searchIndex : searchIndexs) {
                values.add(searchIndex.name);
                currentDocIndexs.put(searchIndex.name , searchIndex);
            }
        }

        adapter = new ArrayAdapter<String>(this,
                R.layout.activity_doc, values) ;

        binding = DataBindingUtil.setContentView(this, R.layout.activity_index);
        binding.indexList.setAdapter(adapter);
        binding.indexList.setOnItemClickListener(_createOnListItemClick());

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        binding.search.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        binding.search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                IndexActivity.this.adapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                IndexActivity.this.adapter.getFilter().filter(query);
                return true;
            }
        });

    }


    /**
     * create a onListItemClick listener to use with ListView adapter
     * @return the newly created listener class
     */
    protected OnItemClickListener _createOnListItemClick() {
        return (l, v, position,id) -> {
            Intent intent = new Intent(IndexActivity.this, WebActivity.class);
            intent.putExtra("docPath", currentDocIndexs.get(binding.indexList.getItemAtPosition((int)id)).path);
            intent.putExtra("docDir", getIntent().getStringExtra("currentDocDirName"));

            startActivity(intent);
        };
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }

    /**
     * check if doc index exist in the sqlite database
     * @return whether doc exist in database
     */
    private boolean _isDocPopulated(Intent intent) {
        return Select.from(SearchIndex.class)
            .where(Condition.prop("DOC_TYPE").eq(intent.getStringExtra("currentDocName"))).list().size() > 0;
    }
}


