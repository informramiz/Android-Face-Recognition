package com.example.hellojni;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AdminReceiver extends DeviceAdminReceiver 
{
	private final static String TAG = "AdminReceiver";

	public void showToast ( Context context , String message )
	{
		Toast.makeText( context , message, Toast.LENGTH_LONG ).show();
	}
	
	@Override
    public void onEnabled(Context context, Intent intent) 
	{
//        showToast( context , "Ramiz Admin enabled" );
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return "Face-Lock is going off";
    }

    @Override
    public void onDisabled(Context context, Intent intent) 
    {
//    	showToast (context , "OnDisable--Ramiz" );
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) 
    {
//    	showToast ( context , "On Password changed" );
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) 
    {
//    	showToast ( context , "On Password failed" );
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) 
    {
    	
//    	showToast ( context , "onPasswordSucceeded" );
    }

    
//    @Override
//    public void onPasswordExpiring(Context context, Intent intent) 
//    {
//    	
//    }
    
    
}
