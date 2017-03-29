/*--------------------------------
Copyrights (c) MewApps. All rights are reserved by MewApps(www.mewapps.com).
--------------------------------*/

package com.example.hellojni;


import java.io.*;
import java.util.Date;

import android.app.*;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.*;
import android.text.style.SuperscriptSpan;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.AdapterView.*;
import android.os.*;
import android.net.*;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.database.sqlite.*;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.graphics.drawable.*;
import android.graphics.drawable.shapes.*;
import android.graphics.*;
import android.telephony.*;
import android.util.Log;
import android.opengl.GLSurfaceView;

public class ChangePasswordActivity extends Activity implements OnClickListener 
{	
	private ContentResolver cv;
	private final String TAG = getClass ( ).getName ( ) ;
	private Button okButton;
	private EditText oldPasswordEditText;
	private EditText newPasswordEditText;
	private EditText verifyNewPasswordEditText;
	
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView ( R.layout.change_password );   
        
        okButton = (Button)findViewById( R.id.ok_button_of_change_password_activity );
        okButton.setOnClickListener( this );
        
        oldPasswordEditText = (EditText)findViewById( R.id.old_password_eText_of_change_password_activity );
        newPasswordEditText = (EditText)findViewById( R.id.new_password_eText_of_change_password_activity );
        verifyNewPasswordEditText = (EditText)findViewById( R.id.verify_new_password_eText_of_change_password_activity );
    }
    
    public void onClick ( View view )
    {
    	if( view == okButton )
    	{
    		String oldPassword = oldPasswordEditText.getText().toString();
    		String newPassword = newPasswordEditText.getText().toString();
    		String verifyNewPassword = verifyNewPasswordEditText.getText().toString();
    		
    		if ( oldPassword == null || oldPassword.equals( "" ) || !oldPassword.equals( getPassword( ) ) )
    		{
    			Toast.makeText( this , "Invalid old Password" , Toast.LENGTH_LONG ).show();
    			return;
    		}
    		
    		if ( newPassword == null || newPassword.equals( "" ) )
    		{
    			Toast.makeText( this , "Please enter new password" , Toast.LENGTH_LONG ).show();
    			return;
    		}
    		
    		if ( verifyNewPassword == null || !newPassword.equals( verifyNewPassword ) )
    		{
    			Toast.makeText( this , "new passwords verification does not match" , Toast.LENGTH_LONG ).show();
    			return;
    		}
    		
    		updatePassword( newPassword );
    		Toast.makeText( this , "Password changed successfully" , Toast.LENGTH_LONG ).show();
    		finish( );
    	}
    }
    
    private String getPassword ( )
    {
    	SharedPreferences prefs = getSharedPreferences( Constant.PREF_NAME , MODE_PRIVATE );
    	String password = prefs.getString( Constant.PASSWORD_PREF , "12345" );
    	
    	return password;
    }
    
    private void updatePassword (String value)
	{
		 SharedPreferences prefs = getSharedPreferences( Constant.PREF_NAME , MODE_PRIVATE );
		 SharedPreferences.Editor editor = prefs.edit();
		 
		 editor.putString( Constant.PASSWORD_PREF , value );
		 editor.commit();
	}
    
    public void showToast ( String message )
    {
    	Toast.makeText( this , message,  Toast.LENGTH_LONG ).show();
    }
    
    public void onPause ( )
    {
    	super.onPause();
    }
}

