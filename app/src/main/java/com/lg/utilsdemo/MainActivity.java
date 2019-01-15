package com.lg.utilsdemo;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.lg.utilsdemo.activity.SensorActivity;
import com.lg.utilsdemo.adapter.StringAdapter;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private String[] itemStr = {"重力感应"};
    private Class<Activity>[] activityList = new Class[]{SensorActivity.class};
    private RecyclerView recyclerView;
    private StringAdapter stringAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        stringAdapter = new StringAdapter(R.layout.item_string,Arrays.asList(itemStr));
        recyclerView.setAdapter(stringAdapter);
        stringAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                startActivity(new Intent(MainActivity.this, activityList[position]));
            }
        });
    }
}
