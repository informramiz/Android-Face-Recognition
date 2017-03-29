package com.example.hellojni;
import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class FaceUnlockActivity extends Activity
{
	private Button okButton;
	private DevicePolicyManager policyManager;
	private ComponentName myAdminSample;
	private int REQ_CODE = 1001;
	private File faceLockDirFile;
	private File mCascadeFile;
	private final static int CAMERA_CODE = 1002;
	private boolean isAuthentic;
	private boolean isRedirecting;
	private boolean isStartingCamera;
	private int result;
	private ProgressDialog pDialog;
	private boolean status;

//	private Handler mHander = new Handler()
//	{
//		@Override
//		public void handleMessage(Message msg) 
//		{
//			if ( result == -1 )
//			{
//				Toast.makeText( FaceUnlockActivity.this , "Don't recognize you" , Toast.LENGTH_LONG ).show();
//				
//				isRedirecting = true;
//				Intent intent = new Intent ( FaceUnlockActivity.this , PasswordUnlockActivity.class );
//				startActivity ( intent );
//				finish ( );
//				return;
//			}
//			
//			Toast.makeText( FaceUnlockActivity.this , "Welcome" , Toast.LENGTH_LONG ).show();
//			isAuthentic = true;
//			finish ( );
//		}
//	};
	
	public void onCreate ( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		
		TextView tv = new TextView ( this );
		tv.setText( "Processing...." );
		setContentView( tv );
		
		faceLockDirFile = getDir( Constant.FACE_LOCK_DIR , MODE_PRIVATE );
		mCascadeFile = new File( faceLockDirFile , "lbpcascade_frontalface.xml");
		isAuthentic = false;
		isRedirecting = false;
		isStartingCamera = false;
		
//		policyManager = (DevicePolicyManager)getSystemService( Context.DEVICE_POLICY_SERVICE );
//		myAdminSample = new ComponentName( this , AdminReceiver.class );
		pDialog = new ProgressDialog( FaceUnlockActivity.this );
		startCamera();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
//		policyManager.lockNow();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		isStartingCamera = false;
		if ( resultCode != RESULT_OK )
		{
				Toast.makeText( this , "An error occured" , Toast.LENGTH_LONG ).show();
				finish( );
				return;
		}
		
		new DetectFace().execute( );		
	}
	
	private void showToast ( String msg )
	{
		Toast.makeText( this , msg,  Toast.LENGTH_LONG ).show();
	}
	
	public boolean isActive ( )
	{
		return policyManager.isAdminActive(myAdminSample);
	}
	
	public void onPause ( )
	{
		super.onPause();
		
//		if ( isActive() && !isAuthentic && !isRedirecting && !isStartingCamera )
//			policyManager.lockNow();
	}

	private String getPicPath ( )
	{
		return faceLockDirFile.getAbsolutePath() + "/" + Constant.TEST_PIC_NAME;
	}

	private void startCamera ( )
	{
		File file = new File ( Environment.getExternalStorageDirectory().getPath() + "/" + Constant.TEST_PIC_NAME );
    	
    	if ( file.exists( ) )
    		file.delete( );
    	
    	Intent intent = new Intent ( android.provider.MediaStore.ACTION_IMAGE_CAPTURE  );
		intent.putExtra( MediaStore.EXTRA_SCREEN_ORIENTATION , 1 );
		intent.putExtra( MediaStore.EXTRA_OUTPUT , Uri.fromFile( file ) ) ;
		
		isStartingCamera = true;
		startActivityForResult ( intent , CAMERA_CODE   );
	}

	class Recognize extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute() 
		{
			super.onPreExecute();
			
			pDialog.setMessage( "Recognizing..." );
			pDialog.setCancelable( true );
			pDialog.setIndeterminate( false );
			pDialog.show();
		}
		
		@Override
		protected String doInBackground(String... args) 
		{
			result = HelloCV.recognize( Environment.getExternalStorageDirectory().getPath() + "/" + Constant.DATA_SET_NAME , null , 1 , Constant.NO_OF_TRAINING_PICS ,  Environment.getExternalStorageDirectory().getPath() + "/" + Constant.TEST_PIC_NAME );
			
			return null;
		}
		
		@Override
		protected void onPostExecute(String rsult) 
		{
			super.onPostExecute(rsult);
			
			pDialog.dismiss();
			if ( result == -1 )
			{
				Toast.makeText( FaceUnlockActivity.this , "Don't recognize you" , Toast.LENGTH_LONG ).show();
				
				isRedirecting = true;
				Intent intent = new Intent ( FaceUnlockActivity.this , PasswordUnlockActivity.class );
				//startActivity ( intent );
				finish ( );
			}
			
			Toast.makeText( FaceUnlockActivity.this , "Welcome, similarity : " + result , Toast.LENGTH_LONG ).show();
			log ( "Similarity : " + result );
			isAuthentic = true;
			finish ( );
		}
	}
	
	class DetectFace extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute() 
		{
			super.onPreExecute();
			
			pDialog.setMessage( "Detecting face..." );
			pDialog.setCancelable( false );
			pDialog.setIndeterminate( false );
			pDialog.show();
		}
		
		@Override
		protected String doInBackground(String... args) 
		{
			status = HelloCV.lbpDetector( mCascadeFile.getAbsolutePath() , Environment.getExternalStorageDirectory().getPath() + "/" + Constant.TEST_PIC_NAME , Environment.getExternalStorageDirectory().getPath() + "/" + Constant.TEST_PIC_NAME );
			
			return null;
		}
		
		@Override
		protected void onPostExecute(String rsult) 
		{
			super.onPostExecute(rsult);
			
			pDialog.dismiss();
			if ( status == false )
			{
				showToast( "No face detected in this image" );
				isRedirecting = true;
				Intent intent = new Intent ( FaceUnlockActivity.this , PasswordUnlockActivity.class );
				//startActivity ( intent );
				finish ( );
				return;
			}
			
			new Recognize( ).execute( );
		}
	}
		
	private void log ( String msg )
	{
		Log.v ( "Hello" , msg );
	}

}
