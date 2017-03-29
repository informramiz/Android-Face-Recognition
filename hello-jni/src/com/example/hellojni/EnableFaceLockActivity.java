/*--------------------------------
Copyrights (c) MewApps. All rights are reserved by MewApps(www.mewapps.com).
--------------------------------*/

package com.example.hellojni;

import android.app.Activity;
import android.app.ListActivity;
import android.app.Activity.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class EnableFaceLockActivity extends Activity implements OnClickListener {
    
	private Button okButton;
	private RadioButton onRadioButton;
	private RadioButton offRadioButton;
	private boolean passwordStatus;
	private boolean previousStatus;
	
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.enable_face_lock );
        
        okButton = (Button)findViewById( R.id.ok_button_of_enable_face_lock_activity );
        okButton.setOnClickListener( this );
        onRadioButton = (RadioButton)findViewById( R.id.enable_lock_rButton_of_enable_face_lock_activity );
        offRadioButton = (RadioButton)findViewById( R.id.disable_lock_rButton_of_enable_face_lock_activity );
        
        SharedPreferences prefs = getSharedPreferences( Constant.PREF_NAME , Context.MODE_PRIVATE );
        previousStatus = passwordStatus = prefs.getBoolean( Constant.FACE_LOCK_STATUS_PREF , false );
        
        if ( passwordStatus )
        	onRadioButton.setChecked( true );
        else
        	offRadioButton.setChecked( true );
    }
    
    public void onClick ( View view )
    {
    	if ( view == okButton )
    	{
    		if ( passwordStatus == true && previousStatus == false && getTrainingStatus() == false )
    		{
    			showToast( "Please first train the face-locker" );
    			finish( );
    			return;
    		}
    			
    		updateLockStatus( passwordStatus );
    		finish ( );
    	}
    }
    
    private boolean getTrainingStatus ( )
    {
    	SharedPreferences pref = getSharedPreferences( Constant.PREF_NAME , Context.MODE_PRIVATE  ) ;
    	boolean status = pref.getBoolean( Constant.IS_TRAINED_PREF , false );
    	
    	return status;
    }
    
    public void onRadioButtonClicked ( View view )
    {
    	if ( view == onRadioButton )
    		passwordStatus = true;
    	else if ( view == offRadioButton )
    		passwordStatus = false;
    }
    
    public void showToast ( String message )
    {
    	Toast.makeText( this , message , Toast.LENGTH_LONG ).show();
    }

    private void updateLockStatus ( Boolean value )
	{
		 SharedPreferences prefs = getSharedPreferences( Constant.PREF_NAME , 0 );
		 SharedPreferences.Editor editor = prefs.edit();
		 
		 editor.putBoolean( Constant.FACE_LOCK_STATUS_PREF , value );
		 editor.commit();
	}

    public void onPause ( )
    {
    	super.onPause();
    }
}