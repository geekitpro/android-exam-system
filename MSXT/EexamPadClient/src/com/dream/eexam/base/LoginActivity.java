package com.dream.eexam.base;

import java.io.ByteArrayInputStream;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.dream.eexam.util.DatabaseUtil;
import com.dream.eexam.util.StoreDataConstants;
import com.dream.eexam.util.SystemConfig;
import com.msxt.client.server.ServerProxy;
import com.msxt.client.server.WebServerProxy;
import com.msxt.client.server.ServerProxy.Result;
import com.msxt.client.server.ServerProxy.STATUS;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends BaseActivity {

	public final static String LOG_TAG = "LoginActivity";
	String saveHost = null;
	EditText idEt = null;
	EditText passwordET = null;
	String saveId = null;
	String savePassword = null;
	Button loginBtn = null;
	Button settingBtn = null;
	String loginResultFile = null;
	String loginResultFilePath = null;
	private Context mContext;
	
	
	public void printStoredDataInDB(){
		Log.i(LOG_TAG,"----------------data in SQLLite-----------------");
		DatabaseUtil dbUtil = new DatabaseUtil(mContext);
		dbUtil.open();
    	Cursor cursor = dbUtil.fetchAllAnswers();
    	while(cursor.moveToNext()){
    		int cid = cursor.getInt(0);
    		int qid = cursor.getInt(1);
			String qidStr = cursor.getString(2);
			String answer = cursor.getString(3);
			Log.i(LOG_TAG, "cid="+String.valueOf(cid)+" qid="+String.valueOf(qid)+" qidStr="+qidStr+" answer="+answer);
		}
    	cursor.close();
    	dbUtil.close();
	}
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.i(LOG_TAG,"onCreate...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        mContext = getApplicationContext();
        
        loadSharedPreferencesData();
        printStoredDataInDB();
		
//        saveHost = sharedPreferences.getString("host", null);
        saveHost = getFromSP(StoreDataConstants.SP_KEY_HOST);
        
        idEt = (EditText) this.findViewById(R.id.idEt);
//		saveId = sharedPreferences.getString("id", null);
		saveId = getFromSP(StoreDataConstants.SP_KEY_ID);
		if(saveId!=null||!"".equals(saveId)){
			idEt.setText(saveId);
		}
		
		passwordET = (EditText) this.findViewById(R.id.passwordET);
//		savePassword = sharedPreferences.getString("password", null);
		saveId = getFromSP(StoreDataConstants.SP_KEY_PWD);
		if(savePassword!=null||!"".equals(savePassword)){
			passwordET.setText(savePassword);
		}
		
		loginBtn = (Button) this.findViewById(R.id.loginBtn);
		loginBtn.setOnClickListener(loginListener);
		
		settingBtn = (Button) this.findViewById(R.id.settingBtn);
		settingBtn.setOnClickListener(settingListener);
		
    }

    View.OnClickListener loginListener = new View.OnClickListener() {  
        @Override  
        public void onClick(View v) { 
        	Button lBtn = (Button)v;
        	lBtn.setEnabled(false);

        	loginResultFile = SystemConfig.getInstance().getPropertyValue("Login_Result");
        	loginResultFilePath = Environment.getExternalStorageDirectory().getPath() 
        	+ File.separator + getResources().getString(R.string.app_file_home)
        	+ File.separator + idEt.getText().toString();
        	
        	//save user file home path to sharedPreferences
			sharedPreferences.edit().putString("userFileHome", loginResultFilePath);
			sharedPreferences.edit().commit();
        	
        	String examPath = getFromSP(StoreDataConstants.SP_KEY_EXAM_PATH);
        	String examFile = getFromSP(StoreDataConstants.SP_KEY_EXAM_FILE);
        	String examStatus = getFromSP(StoreDataConstants.SP_KEY_EXAM_STATUS);
        	
        	if("start".equals(examStatus)&& new File(examPath+File.separator+examFile).exists()){
	        	//go to continue exam page
	        	Intent intent = new Intent();
    			intent.putExtra("loginResultFile", loginResultFile);
    			intent.putExtra("loginResultFilePath", loginResultFilePath);
    			intent.setClass( mContext, ExamContinue.class);
    			startActivity(intent);  
        	}else{
        		//go to start exam page
            	String id = idEt.getText().toString();
            	String password = passwordET.getText().toString();
            	
    			save2SP(StoreDataConstants.SP_KEY_ID, id);
    			save2SP(StoreDataConstants.SP_KEY_PWD, password);
    			
            	if (getWifiIP() != null && getWifiIP().trim().length() > 0 && !getWifiIP().trim().equals("0.0.0.0")){
            		new LoginTask().execute(new String[]{id,password});
            	}else{
            		ShowDialog(mContext.getResources().getString(R.string.dialog_note),
            				mContext.getResources().getString(R.string.msg_network_error));
            	}       		
        	}
        	
        }  
    };
    
    //go to setting page
    View.OnClickListener settingListener = new View.OnClickListener() {  
        @Override  
        public void onClick(View v) { 
        	//go to examList page
        	Intent intent = new Intent();
			intent.setClass( LoginActivity.this, SettingActivity.class);
			startActivity(intent);  	
        }  
    };
    
    
    private class LoginTask extends AsyncTask<String, Void, String> {
    	ProgressDialog progressDialog;
    	ServerProxy proxy;
    	Result loginResult;
    	
    	@Override
    	protected void onPreExecute() {
    		Log.i(LOG_TAG, "onPreExecute() called");
    		progressDialog = ProgressDialog.show(LoginActivity.this, null,mContext.getResources().getString(R.string.msg_login_server), true, false); 
    	}
    	
        @Override
		protected String doInBackground(String... urls) {
        	Integer port = Integer.valueOf(mContext.getResources().getString(R.string.port));
        	proxy =  WebServerProxy.Factroy.createInstance(saveHost, port);
    		loginResult = proxy.login(urls[0], urls[1]);
			return null;
		}

        @Override
        protected void onPostExecute(String result) {
        	progressDialog.dismiss();
        	loginBtn.setEnabled(true);

    		if( loginResult.getStatus() == STATUS.ERROR ) {
    			ShowDialog(mContext.getResources().getString(R.string.dialog_note),
    					loginResult.getErrorMessage());
        	} else {
        		saveFile(loginResultFilePath, loginResultFile, loginResult.getSuccessMessage());
        		
        		try{
    		    	DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    		        ByteArrayInputStream is = new ByteArrayInputStream( loginResult.getSuccessMessage().getBytes() );
    		        Document doc = db.parse( is );
    		        is.close();
    		        
    		        Element root = doc.getDocumentElement();
    		        String status = root.getElementsByTagName("status").item(0).getTextContent();
    		        if( status.equals("failed") ) {
    		        	String desc = root.getElementsByTagName("desc").item(0).getTextContent();
    		        	ShowDialog(mContext.getResources().getString(R.string.dialog_note),
    		        			desc);
    		        } else {
    		        	String conversation = root.getElementsByTagName( "conversation" ).item(0).getTextContent();
    		        	proxy.setConversationId( conversation );
    		        	
//    		        	String examStatus = sharedPreferences.getString("exam_status", null);
    		        	String examStatus = getFromSP(StoreDataConstants.SP_KEY_EXAM_STATUS);
    		        	if(examStatus == null){
        		        	//go to exam List page
        		        	Intent intent = new Intent();
        	    			intent.putExtra("loginResultFile", loginResultFile);
        	    			intent.putExtra("loginResultFilePath", loginResultFilePath);
        	    			intent.setClass( mContext, ExamStart.class);
        	    			startActivity(intent);     		        		
    		        	}else if(StoreDataConstants.SP_VALUE_EXAM_STATUS_START.equals(examStatus)){
        		        	//go to continue exam page
        		        	Intent intent = new Intent();
        	    			intent.putExtra("loginResultFile", loginResultFile);
        	    			intent.putExtra("loginResultFilePath", loginResultFilePath);
        	    			intent.setClass( mContext, ExamContinue.class);
        	    			startActivity(intent);   
    		        	}else if(StoreDataConstants.SP_VALUE_EXAM_STATUS_END.equals(examStatus)){
    		        		ShowDialog(mContext.getResources().getString(R.string.dialog_note),"Your Exam is completed");
    		        	}
    		        }
        		} catch (Exception e) {
        			ShowDialog(mContext.getResources().getString(R.string.dialog_note),"Invalid XML Data");
    			}
        	}
        }
    }


}