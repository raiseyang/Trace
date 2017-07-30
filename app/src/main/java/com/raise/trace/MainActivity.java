package com.raise.trace;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity {
    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void click_test(View view) {
        Trace.logPath(getCacheDir()+"/trace.log");
        Trace.showCodePosition(true);

        Map map = new HashMap();
        map.put("name", "raise");
        map.put("age", "18");

        Set set = map.keySet();

        Trace.d(" d");
        Trace.i(" i");
        Trace.w(" w");
        Trace.e(" e = " + null);
        Trace.d(TAG, " d");
        Trace.i(TAG, " i");
        Trace.w(TAG, " w");
        Trace.e(TAG, " e = " + null);
        Trace.d(TAG, " %s,%d", "raise", 1);
        Trace.i(TAG, " %s,%d", "raise", 1);
        Trace.w(TAG, " %s,%d", "raise", 1);
        Trace.e(TAG, "333", new NullPointerException("fu*k null pointer exception."));
        Trace.json(TAG, "{\"name\":\"BeJson\",\"url\":\"http://www.bejson.com\",\"page\":88,\"isNonProfit\":true}");
        Trace.array(TAG, new String[]{"value1", "value2"});
        Trace.list(TAG, Arrays.asList("list1", "list2", "list3"));
        Trace.map(TAG, map);
        Trace.set(TAG, set);
        Trace.xml(TAG, "<student><age>12</age><name>jack</name><skill><language>chinese</language><run>22</run></skill></student>");
    }
}
