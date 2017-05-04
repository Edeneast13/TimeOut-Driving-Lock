package com.brianroper.putitdown;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.neura.standalonesdk.events.NeuraEvent;
import com.neura.standalonesdk.events.NeuraPushCommandFactory;

import java.util.Map;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by brianroper on 5/1/17.
 */

public class DrivingEventService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map data = remoteMessage.getData();
        if (NeuraPushCommandFactory.getInstance().isNeuraEvent(data)) {
            NeuraEvent event = NeuraPushCommandFactory.getInstance().getEvent(data);
            Log.i(getClass().getSimpleName(), "received Neura event - " + event.toString());
            Intent drivingService = new Intent(this, DrivingService.class);
            if(event.getEventName().equals("userStartedDriving")){
                Log.i("Driving Session: ", "Started");
                startService(drivingService);
                addNeuraEventLog(event);
            }
            else if(event.getEventName().equals("userFinishedDriving")){
                Log.i("Driving Session: ", "Stopped");
                stopService(drivingService);
                addNeuraEventLog(event);
            }
        }
    }

    public void addNeuraEventLog(final NeuraEvent event){
        Realm realm;
        Realm.init(getApplicationContext());
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        realm = Realm.getInstance(realmConfiguration);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
               NeuraEventLog neuraEventLog = realm.createObject(NeuraEventLog.class, event.getNeuraId());
               neuraEventLog.setEventName(event.getEventName());
               neuraEventLog.setTimestamp(event.getEventTimestamp());
               realm.copyToRealmOrUpdate(neuraEventLog);
            }
        });
        realm.close();
    }
}
