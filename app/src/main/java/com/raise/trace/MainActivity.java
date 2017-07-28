package com.raise.trace;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void click_test(View view) {
        Toast.makeText(MainActivity.this, R.string.app_name, Toast.LENGTH_SHORT).show();
    }
}
