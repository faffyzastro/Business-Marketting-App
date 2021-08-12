package co.ke.tonyoa.android.vossified;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;



import java.util.concurrent.TimeUnit;

import co.ke.tonyoa.android.vossified.Data.SyncWorker;


public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {

        if(connectedToInternet(context)) {
            androidx.work.Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
            //Enqueue for syncing once the app is connected to the internet
            OneTimeWorkRequest oneTimeWorkRequest=new OneTimeWorkRequest.Builder(SyncWorker.class).setInitialDelay(3, TimeUnit.SECONDS).setConstraints(constraints).build();
            WorkManager.getInstance(context).beginUniqueWork("oneTime", ExistingWorkPolicy.REPLACE, oneTimeWorkRequest).enqueue();
        }

    }

    boolean connectedToInternet(Context context) {
        ServiceManager serviceManager = new ServiceManager(context);
        if (serviceManager.isNetworkAvailable()) {
            return true;
        }
        else {
            return false;
        }
    }
}
