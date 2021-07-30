package com.murphy.pokotalk.service;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;


public class BootBroadCastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        /* If device boot has completed */
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            /* Start PokoTalk service */
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            JobInfo jobInfo = new JobInfo.Builder(11, new ComponentName(context, PokoTalkService.class))
                    // only add if network access is required
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .build();

            jobScheduler.schedule(jobInfo);
        }
    }
}
