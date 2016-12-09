package com.sdl.hellosdlandroid;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.smartdevicelink.transport.SdlBroadcastReceiver;
import com.smartdevicelink.transport.SdlRouterService;

public class SdlReceiver  extends SdlBroadcastReceiver {		
	private static final String TAG = "SdlBroadcastReciever";
	

	@Override
	public void onSdlEnabled(Context context, Intent intent) {
		Log.d(TAG, "SDL Enabled");
		intent.setClass(context, SdlService.class);
		context.startService(intent);
		
	}

	@Override
	public Class<? extends SdlRouterService> defineLocalSdlRouterClass() {
		//Return a local copy of the SdlRouterService located in your project
		return com.sdl.hellosdlandroid.SdlRouterService.class;
	}


}