/*-------------------------------------------------------------------------
Copyrights (c) MewApps. All rights are reserved by MewApps(www.mewapps.com).
----------------------------------------------------------------------------*/

package com.example.hellojni;

import android.app.ListActivity;
import android.app.Activity.*;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

public class SecurityPasswordActivity extends ListActivity implements OnItemClickListener {
    
	private String list [] = { "Change Password" , "Enable/Disable Password" };
	
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String> ( this , android.R.layout.simple_list_item_1 , list );
        getListView ( ).setAdapter(adapter);
        getListView ( ).setOnItemClickListener( this );
    }
    
    public void onItemClick ( AdapterView<?> parent , View view , int position , long id )
    {
//    	Intent fakeCallDataActivityIntent = new Intent ( this , FakeCallDataActivity.class );
//    	fakeCallDataActivityIntent.putExtra( "typeOfCall" , id );
//    	
//    	startActivity ( fakeCallDataActivityIntent );
    	
    	if ( id == 0 )
    	{
    		Intent changePasswordActivityIntent = new Intent( this , ChangePasswordActivity.class );
    		startActivity( changePasswordActivityIntent );
    	}
    	else if ( id == 1 )
    	{
    		Intent enablePasswordActivityIntent = new Intent( this , EnableFaceLockActivity.class );
    		startActivity( enablePasswordActivityIntent );
    	}
    	
    	//finish ( );
    }
    
    public void onPause ( )
    {
    	super.onPause();
    	finish ( );
    }
}