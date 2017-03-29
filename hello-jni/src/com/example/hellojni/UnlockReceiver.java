package com.example.hellojni;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.sax.StartElementListener;

public class UnlockReceiver extends BroadcastReceiver 
{

	@Override
	public void onReceive(Context context, Intent intent) 
	{
		
		SharedPreferences prefs = context.getSharedPreferences( Constant.PREF_NAME , Context.MODE_PRIVATE );
        boolean lockStatus = prefs.getBoolean( Constant.FACE_LOCK_STATUS_PREF , false );
        
        if ( lockStatus == false )
        	return;
        
//		if ( intent.getAction().equals( Intent.ACTION_SCREEN_ON ))
//		{
			Intent intnt = new Intent ( context , FaceUnlockActivity.class );
			intnt.setFlags ( Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity( intnt );
//		}
	}
	
}
