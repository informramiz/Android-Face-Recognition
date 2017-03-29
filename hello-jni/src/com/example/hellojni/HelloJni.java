/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.hellojni;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;


import android.app.Activity;
import android.app.ListActivity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Camera;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

public class HelloJni extends ListActivity implements OnItemClickListener
{
    private String TAG;
    private Button okButton;
    private EditText subjectEditText;
    private TextView resultTextView;
    private TextView timeTextView;
    
    private String subject;
	private ComponentName myAdminSample;
	private static final int REQ_CODE = 1001;
	
	private String list [] = { "Train" , "Enable Face Lock" , "Change Password" , "Face-Unlock" , "Learn" , "Status" };
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String> ( this , android.R.layout.simple_list_item_1 , list );
        getListView ( ).setAdapter(adapter);
        getListView ( ).setOnItemClickListener( this );
        
        myAdminSample = new ComponentName( this , AdminReceiver.class );
        //activateAdmin ( );
        createPreferences();
        
        log ( Environment.getExternalStorageDirectory().getPath() );
    }
    
    private void activateAdmin ( )
    {
    	Intent intent = new Intent ( DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN );
		intent.putExtra( DevicePolicyManager.EXTRA_DEVICE_ADMIN , myAdminSample );
		startActivityForResult(intent, REQ_CODE);
    }
    
    public void onClick ( View view )
    {
    	if ( view == okButton )
    	{
    		Intent i = new Intent ( this , CameraActivity.class );
    		startActivity ( i );
    		
    		subject = subjectEditText.getText().toString().trim();
    		
    		if ( subject == null || subject.equals( "" ) )
    		{
    			showToast ( "Please enter subject no" );
    			return;
    		}
    		
    		int s = Integer.parseInt( subject );
    		
    		if ( s < 1 || s > 100 )
    		{
    			showToast ( "Invalid image no" );
    			return;
    		}
    		
    		subjectEditText.setVisibility( View.GONE );
    		okButton.setVisibility( Button.GONE );
    		
    		resultTextView.setText( "Processing..." );
    		timeTextView.setText( "" );
//    		Thread th = new Thread ( this );
//    		th.start();
    	
    	}
    }
    
    private void loadFiles ( )
    {
    	try
		{
   			InputStream is = getResources().openRawResource( R.raw.lbpcascade_frontalface );
            File faceLockDir = getDir( Constant.FACE_LOCK_DIR , Context.MODE_PRIVATE);
            File flDirFile = new File( faceLockDir , "lbpcascade_frontalface.xml");
           
            FileOutputStream os = new FileOutputStream( flDirFile );

            byte[] buffer = new byte[4096];
            int bytesRead;
            
            while ((bytesRead = is.read(buffer)) != -1) 
            {
                os.write(buffer, 0, bytesRead);
            }
            
            is.close();
            os.close();
		}
		
    	catch ( IOException e )
		{
            	Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
		}
    }
    
    public void showToast ( String msg )
    {
    	Toast.makeText( this , msg, Toast.LENGTH_LONG ).show();
    }
    
    public void onResume ( )
    {
    	super.onResume();
    	Log.i( TAG , "Trying to load OpenCV library");
	 
		 if ( !OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mOpenCVCallBack ) )
		 {
			 Log.e( TAG , "Cannot connect to OpenCV Manager");
		 }
    }
    
	private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) 
	{
		   @Override
		   public void onManagerConnected(int status) 
		   {
			   switch (status) 
			   {
		       		case LoaderCallbackInterface.SUCCESS:
		       		{
		       			Log.i( "HelloOpenCVActivity" , "OpenCV loaded successfully");
		       			
		       			Thread thread = new Thread ( new Runnable() {
							
							@Override
							public void run() 
							{
								System.loadLibrary( "hello-jni" );
			       				loadFiles();
							}
						});
		       				
		       			thread.start ( );
		       		}
		       		break;
		       		
		       		default:
		       		{
		       			super.onManagerConnected(status);
		       		} 
		       		break;
			   }
		   }
	};
	
	public void log ( String msg )
	{
		Log.v ( "Threads" , msg );
	}

	private void createPreferences ( )
	{
		SharedPreferences pref = getSharedPreferences( Constant.PREF_NAME , Context.MODE_PRIVATE );
		
		boolean isAlreadyCreated = pref.contains( Constant.PASSWORD_PREF );
		
		if ( isAlreadyCreated )
			return;
		
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean ( Constant.FACE_LOCK_STATUS_PREF , false );
		editor.putBoolean( Constant.IS_TRAINED_PREF , false );
		editor.putString( Constant.PASSWORD_PREF , "12345" );
		
		editor.commit();
	}

	@Override
	public void onItemClick(AdapterView<?> parent , View view, int pos, long id ) 
	{
		if ( id == 0 )
		{
			Intent intent = new Intent ( this , CameraActivity.class );
			startActivity ( intent );
		}
		else if ( id == 1 )
		{
			Intent intent = new Intent ( this , EnableFaceLockActivity.class );
			startActivity ( intent );
		}
		else if ( id == 2 )
		{
			Intent intent = new Intent ( this , ChangePasswordActivity.class );
			startActivity ( intent );
		}
		else if ( id == 3 )
		{
			Intent intent = new Intent ( this , FaceUnlockActivity.class );
			startActivity ( intent );
		}
		else if ( id == 4 )
		{	
			Thread thread = new Thread ( new Runnable() 
			{
				@Override
				public void run() 
				{
					boolean status = HelloCV.learn( Environment.getExternalStorageDirectory().getPath() , Environment.getExternalStorageDirectory().getPath() + "/" + Constant.DATA_SET_NAME, null , 1 , Constant.NO_OF_TRAINING_PICS );
					log( "Learning status : " + status );
				}
			});
			
			thread.start();
			
//			if ( status == false )
//			{
//				log( "Learning status : " + status );
//				showToast( "Learning status : " + status );
//			}
		}
		else if ( id == 5 )
		{
			File file = new File ( Environment.getExternalStorageDirectory().getPath() + "/" + Constant.DATA_SET_NAME );
			
			if ( file.exists() )
				showToast ( "Yes Created" );
			else
				showToast ( "No, not created" );
		}
	}

}
