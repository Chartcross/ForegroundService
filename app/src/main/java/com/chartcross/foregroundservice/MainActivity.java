package com.chartcross.foregroundservice;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements TestResultsReceiver.Receiver {

    private TestResultsReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mReceiver = new TestResultsReceiver(new Handler());
        mReceiver.setReceiver(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_start_service) {
            Intent intent = new Intent(this, TestService.class);
            intent.putExtra(TestService.RESULTS_RECEIVER, mReceiver);
            intent.putExtra(TestService.TEST_DATA, "Test data passed from activity");
            startService(intent);
            return true;
        }

        if (id == R.id.action_stop_service) {
            Intent intent = new Intent(this, TestService.class);
            stopService(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == TestService.RESULT_OK) {
            Log.e("MainActivity", resultData.getString(TestService.TEST_DATA));
        }
    }
}
