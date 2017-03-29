package com.example.hellojni;

import android.app.Activity;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class PasswordUnlockActivity extends Activity implements OnClickListener 
{
	Button okButton;
	DevicePolicyManager policyManager;
	ComponentName myAdminSample;
	int REQ_CODE = 1001;
	private EditText passwordEditText;
	private boolean isAuthentic;
	
	public void onCreate ( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.password_unlock_activity );
		
		okButton = (Button)findViewById( R.id.ok_button_of_password_unlock_activity );
		okButton.setOnClickListener( this );
		
		policyManager = (DevicePolicyManager)getSystemService( Context.DEVICE_POLICY_SERVICE );
		myAdminSample = new ComponentName( this , AdminReceiver.class );
		
		passwordEditText = (EditText)findViewById( R.id.password_eText_of_password_unlock_activity );
		isAuthentic = false;
	}
	
	@Override
	public void onClick(View view) 
	{
		String password = passwordEditText.getText().toString();
		
		if ( password == null || password.equals( "" ) || !password.equals( getPassword( ) ) )
		{
			Toast.makeText( this , "Invalid Password" , Toast.LENGTH_LONG ).show();
			return;
		}
		
		isAuthentic = true;
		Toast.makeText( this , "Welcome" , Toast.LENGTH_LONG ).show();
		finish ( );
	}
	
	private String getPassword ( )
	{
		SharedPreferences pref = getSharedPreferences( Constant.PREF_NAME , MODE_PRIVATE );
		return pref.getString( Constant.PASSWORD_PREF , "12345"  );
	}
	
	public boolean isActive ( )
	{
		return policyManager.isAdminActive(myAdminSample);
	}
	
	public void onPause ( )
	{
		super.onPause();
		
		if ( isActive() && !isAuthentic )
			policyManager.lockNow();
	}
}
