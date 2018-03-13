package com.xl.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(savedInstanceState);
        setClick();
        startActivityIntent();
    }

    protected abstract void initView(Bundle savedInstanceState);

    protected abstract void setClick();

    protected abstract void startActivityIntent();


}
