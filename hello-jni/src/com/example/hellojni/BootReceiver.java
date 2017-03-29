

package com.example.hellojni;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.FaceDetector.Face;
import android.net.Uri;
import android.provider.CallLog.Calls;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context , Intent intent ) 
	{
		SharedPreferences prefs = context.getSharedPreferences( Constant.PREF_NAME , Context.MODE_PRIVATE );
        boolean lockStatus = prefs.getBoolean( Constant.FACE_LOCK_STATUS_PREF , false );
        
        if ( lockStatus == false )
        	return;
		
		Intent intentNew = new Intent ( context , FaceUnlockActivity.class );
		intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
		context.startActivity ( intentNew );
	}

}
