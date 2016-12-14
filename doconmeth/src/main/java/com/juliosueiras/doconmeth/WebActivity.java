package com.juliosueiras.doconmeth;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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

// import com.orm.query.Condition;
// import com.orm.query.Select;

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

import com.juliosueiras.doconmeth.databinding.ActivityWebBinding;

public class WebActivity extends AppCompatActivity {

    ActivityWebBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Answers(), new Crashlytics());
        setContentView(R.layout.activity_web);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_web);

        Intent intent = getIntent();
        String docPath = intent.getStringExtra("docPath");
        String docDir = intent.getStringExtra("docDir");

        binding.webview.loadUrl("file:///sdcard/Download/" + docDir + "/Contents/Resources/Documents/" + docPath);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
	}
}


