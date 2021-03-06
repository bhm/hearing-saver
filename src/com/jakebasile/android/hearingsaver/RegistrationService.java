/*
 * Copyright 2010-2012 Jake Basile and Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jakebasile.android.hearingsaver;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

/**
 * This service stays running to keep the UnplugReceiver registered for the headset unplug
 * broadcast.
 * @author Jake Basile
 */
public class RegistrationService extends Service
{
    private UnplugReceiver receiver;

    @Override
    public void onCreate()
    {
        super.onCreate();
        // see if there is a saved sticky intent.
        Intent previousIntent = registerReceiver(null, new IntentFilter(
            Intent.ACTION_HEADSET_PLUG));
        // if there is, tell the receiver to ignore it.
        if(previousIntent != null)
        {
            UnplugReceiver.getInstance().setIgnoreNext();
        }
        // set up the receiver.
        registerReceiver(UnplugReceiver.getInstance(), new IntentFilter(
            Intent.ACTION_HEADSET_PLUG));
        registerReceiver(UnplugReceiver.getInstance(), new IntentFilter(
            BluetoothDevice.ACTION_ACL_CONNECTED));
        registerReceiver(UnplugReceiver.getInstance(), new IntentFilter(
            BluetoothDevice.ACTION_ACL_DISCONNECTED));
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        VolumeSettings settings = new VolumeSettings(this);
        if(!settings.getEnabled())
        {
            stopSelf();
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        // Be sure and unregister the receiver when the service is destroyed. This usually
        // means the service is being killed by the system, and it will be restarted again
        // momentarily. if we don't unregister, sometimes multiple instances of the
        // receiver getregistered.
        try
        {
            if(receiver != null)
            {
                unregisterReceiver(receiver);
            }
        }
        catch(IllegalArgumentException e)
        {
            // eat this exception.
        }
    }
}
