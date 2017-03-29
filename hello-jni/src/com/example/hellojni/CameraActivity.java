package com.example.hellojni;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.imgproc.Imgproc;

import com.example.hellojni.FaceUnlockActivity.Recognize;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.View.*;
import android.widget.Button;
import android.widget.Toast;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

public class CameraActivity extends Activity implements OnClickListener
{
	private Button okButton;
	private int noOfPics;
	private static final int CAMERA_CODE = 1001;
	private static final String TAG = "CameraActivity";
	private File faceLockDirFile;
	private File mCascadeFile;
	private ProgressDialog pDialog;
	private boolean status;
	private boolean detectionStatus;
	
	public void onCreate ( Bundle savedInstanceState )
	{
		super.onCreate(savedInstanceState);
		setContentView( R.layout.camera_activity );
		
		okButton = (Button)findViewById( R.id.ok_button_of_camera_activity );
		okButton.setOnClickListener( this );
		noOfPics = 1;
		
		faceLockDirFile = getDir( Constant.FACE_LOCK_DIR , MODE_PRIVATE );
		mCascadeFile = new File( faceLockDirFile , "lbpcascade_frontalface.xml");
		pDialog = new ProgressDialog( this );
	}
	
	private void updateLockStatus ( Boolean value )
	{
		 SharedPreferences prefs = getSharedPreferences( Constant.PREF_NAME , 0 );
		 SharedPreferences.Editor editor = prefs.edit();
		 
		 editor.putBoolean( Constant.FACE_LOCK_STATUS_PREF , value );
		 editor.commit();
	}
	
	public void log ( String msg )
	{
		Log.v ( "Tag" , msg );
	}

	@Override
	public void onClick( View view ) 
	{
		if ( view == okButton )
		{
//			updateLockStatus( false );
			startCamera();
		}
	}
	
	private String getPicPath ( int picNo )
	{
		return Environment.getExternalStorageDirectory().getPath() + "/" + picNo + ".png" ;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
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

	private void startCamera ( )
	{
		File file = new File ( getPicPath( noOfPics ) );
    	
    	if ( file.exists( ) )
    		file.delete( );
    	
    	Intent intent = new Intent ( android.provider.MediaStore.ACTION_IMAGE_CAPTURE  );
		intent.putExtra( MediaStore.EXTRA_SCREEN_ORIENTATION , ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
		intent.putExtra( MediaStore.EXTRA_OUTPUT , Uri.fromFile( file ) ) ;
		startActivityForResult ( intent , CAMERA_CODE  );
	}

	class Learn extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute() 
		{
			super.onPreExecute();
			
			pDialog.setMessage( "Training locker..." );
			pDialog.setCancelable( true );
			pDialog.setIndeterminate( false );
			pDialog.show();
		}
		
		@Override
		protected String doInBackground(String... args) 
		{
			status = HelloCV.learn( Environment.getExternalStorageDirectory().getPath() , Environment.getExternalStorageDirectory().getPath() + "/" + Constant.DATA_SET_NAME, null , 1 , Constant.NO_OF_TRAINING_PICS );
			
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) 
		{
			super.onPostExecute(result);
			
			pDialog.dismiss();
			if ( status == false )
			{
				showToast( "Training unsuccessfull" );
			}
			else
				showToast( "Training successfull" );
			
			finish( );
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
			detectionStatus = HelloCV.lbpDetector( mCascadeFile.getAbsolutePath() , getPicPath( noOfPics ) , getPicPath(noOfPics) );
			
			return null;
		}
		
		@Override
		protected void onPostExecute(String rsult) 
		{
			super.onPostExecute(rsult);
			
			pDialog.dismiss();
			if ( detectionStatus == false )
			{
				showToast( "No face detected in this image" );
			}
			else
				noOfPics++;
	        
			if ( noOfPics <= Constant.NO_OF_TRAINING_PICS )
				startCamera ( );
			else if ( noOfPics > Constant.NO_OF_TRAINING_PICS )
			{
				//showToast( "Training Complete" );
				SharedPreferences pref = getSharedPreferences( Constant.PREF_NAME , MODE_PRIVATE );
				SharedPreferences.Editor editor = pref.edit();
				
				editor.putBoolean( Constant.IS_TRAINED_PREF , true );
				editor.commit( );
				
				new Learn( ).execute( );
			}
			
		}
	}
		
}

