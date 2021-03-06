package com.dream.ivpc.activity.report;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import org.viewflow.android.widget.CircleFlowIndicator;
import org.viewflow.android.widget.ViewFlow;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.dream.ivpc.BaseActivity;
import com.dream.ivpc.PageChange;
import com.dream.ivpc.R;
import com.dream.ivpc.adapter.ExamResultRptAdapter;
import com.dream.ivpc.model.ExamRptBean;
import com.dream.ivpc.model.ExamRptPageBean;
import com.dream.ivpc.util.XMLParseUtil;
import com.dream.ivpc.util.FileUtil;
import com.dream.ivpc.util.ImageUtil;

public class ExamRptPicture extends BaseActivity {

	Context mContext;
	private ViewFlow viewFlow;
	private CircleFlowIndicator indic;

    View.OnClickListener ocLister = new View.OnClickListener() {  
        @Override  
        public void onClick(View v) {
        	finish();
        	switch(v.getId()){
        		case (R.id.customBack):PageChange.go2CandidateDeatil(mContext);break;
        		case (R.id.imgLogout):PageChange.logout(mContext);break;
        	}
        }  
    };
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.candidate_resume_group);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title2);
        
//        ImageButton closeIB = (ImageButton) findViewById(R.id.closeIB);
//        closeIB.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				finish();
//			}
//		});
        
		mContext = getApplicationContext();
		((ImageView) findViewById(R.id.customBack)).setOnClickListener(ocLister);
		((ImageView) findViewById(R.id.imgLogout)).setOnClickListener(ocLister);
		
		new LoadTask().execute(new String[] {});
		viewFlow = (ViewFlow) findViewById(R.id.viewflow);
		indic = (CircleFlowIndicator) findViewById(R.id.viewflowindic);
	}

	/*
	 * If your min SDK version is < 8 you need to trigger the
	 * onConfigurationChanged in ViewFlow manually, like this
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		viewFlow.onConfigurationChanged(newConfig);
	}
	
	public String getRptPath(String admin,String candiate) {
		String basePath = Environment.getExternalStorageDirectory() + "/interviewer";
		return basePath + File.separator + admin + File.separator + candiate + File.separator + "exam_rpt.xml";
	}

	private class LoadTask extends AsyncTask<String, Void, String> {
		private List<Bitmap> bitmapList = new ArrayList<Bitmap>();
		private boolean succFlag = false;
		private ProgressBar progressBar;

		@Override
		protected void onPreExecute() {
			Log.i(LOG_TAG, "onPreExecute() called");
			progressBar = (ProgressBar) findViewById(R.id.loading_resume);
			progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected String doInBackground(String... urls) {
			try {

				// get image from local
				FileInputStream inputStream = FileUtil.getFileInputStream(getRptPath("admin","tangqi"));

				// get resume bean
				ExamRptBean rptBean = XMLParseUtil.parseExamRpt(inputStream);
				List<ExamRptPageBean> pageList = rptBean.getPageList();

				for (ExamRptPageBean pageBean : pageList) {
					Bitmap bitmap = ImageUtil.decodeBase64(pageBean.getContent().toString());
					bitmapList.add(bitmap);
				}
				succFlag = true;
			} catch (Exception e) {
				Log.e(LOG_TAG, "erro message:" + e.getMessage());
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			progressBar.setVisibility(View.GONE);
			if (succFlag) {
				viewFlow.setAdapter(new ExamResultRptAdapter(mContext, bitmapList),bitmapList.size());
				viewFlow.setFlowIndicator(indic);
			}
		}

	}
}
