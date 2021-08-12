package co.ke.tonyoa.android.vossified;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;

import java.util.concurrent.TimeUnit;

import co.ke.tonyoa.android.vossified.Data.SyncWorker;
import co.ke.tonyoa.android.vossified.Data.VossitContract;

import static co.ke.tonyoa.android.vossified.Data.SyncWorker.INITIAL_LOAD;
import static co.ke.tonyoa.android.vossified.Data.SyncWorker.finishedFirstCategories;
import static co.ke.tonyoa.android.vossified.Data.SyncWorker.finishedSync;


public class SplashActivity extends AppCompatActivity {

    private final int SPLASH_DISPLAY_LENGTH = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        //Enqueue for syncing every hour
        androidx.work.Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(SyncWorker.class, 1, TimeUnit.HOURS).setConstraints(constraints).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("sync", ExistingPeriodicWorkPolicy.KEEP, workRequest);

        if (Utils.hasNetwork((ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE))){
            Cursor cursorCategories=getContentResolver().query(Uri.withAppendedPath(VossitContract.BASEURI,
                    VossitContract.CATEGORIESENTRY.TABLENAME), null, null, null, null);
            if (cursorCategories==null || cursorCategories.getCount()==0){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (!finishedFirstCategories){
                            //Waiting for initial categories to load
                        }
                        startMainActivity();
                    }
                }).start();
            }
            else {
                waitSomeSeconds();
            }
        }
        else {
            waitSomeSeconds();
        }


    }

    private void waitSomeSeconds() {
        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                startMainActivity();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    private void startMainActivity() {
        Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

}
