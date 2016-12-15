package com.juliosueiras.doconmeth;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import android.databinding.DataBindingUtil;

import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.answers.Answers;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

import com.juliosueiras.doconmeth.databinding.ActivityWebBinding;

/**
 * Documentation Web Page Activity
 */
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


