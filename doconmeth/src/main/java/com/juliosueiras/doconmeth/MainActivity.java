package com.juliosueiras.doconmeth;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    String[] docList = {"Handlebars","IPhone","WindowsMobile","Blackberry",
        "WebOS","Ubuntu","Windows7","Max OS X"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_doc, docArray);

        ListView listView = (ListView) findViewById(R.id.doc_list);
        listView.setAdapter(adapter);
    }
}

