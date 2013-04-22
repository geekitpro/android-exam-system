package com.dream.eexam.base;

import java.io.File;
import java.io.FileInputStream;

import com.dream.eexam.paper.MultiChoices;
import com.dream.eexam.paper.SingleChoices;
import com.dream.eexam.server.DataParseUtil;
import com.dream.eexam.util.DatabaseUtil;
import com.dream.eexam.util.FileUtil;
import com.dream.eexam.util.SPUtil;
import com.msxt.client.model.Examination;
import com.msxt.client.model.LoginSuccessResult;
import com.msxt.client.model.QUESTION_TYPE;
import com.msxt.client.model.Examination.Question;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ExamContinue extends BaseActivity {
	private static final String LOG_TAG = "ExamContinue";

	ImageView imgHome = null;
	Context mContext;
	
	//Get interviewer information
	LoginSuccessResult succResult = null;
	TextView nameTV = null;
	TextView jobTitleTV = null;
	
	//Exam description
	TextView continueExam = null;
	String examFilePath = null;
	String examFileName = null;
    Examination exam;
    
	//buttons
	private Button continueBtn;
	private Button cancelBtn;
	
	QUESTION_TYPE fQuestionType = null;
	String[] questionTypes;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(LOG_TAG,"onCreate()...");
		
		setContentView(R.layout.exam_continue);
		mContext = getApplicationContext();
		
		String loginResultFilePath  = SPUtil.getFromSP(SPUtil.CURRENT_USER_HOME, sharedPreferences);
		String loginResultFile  = SPUtil.getFromSP(SPUtil.CURRENT_USER_LOGIN_FILE_NAME, sharedPreferences);
		
		try {
	    	FileInputStream inputStream = new FileInputStream(new File(loginResultFilePath+ File.separator+loginResultFile));
	    	succResult = DataParseUtil.getSuccessResult(inputStream);
		} catch (Exception e) {
			Log.i(LOG_TAG,e.getMessage());
		}
		
		imgHome = (ImageView) findViewById(R.id.imgHome);
		imgHome.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				goHome(mContext);
			}
		});
		
		questionTypes = getResources().getStringArray(R.array.question_types);
		
		//get Exam data
		examFilePath = SPUtil.getFromSP(SPUtil.CURRENT_USER_HOME, sharedPreferences);
		examFileName = SPUtil.getFromSP(SPUtil.CURRENT_EXAM_FILE_NAME, sharedPreferences);
    	FileInputStream examStream = FileUtil.getExamStream(examFilePath,examFileName);
    	exam = DataParseUtil.getExam(examStream);
    	
		nameTV = (TextView) this.findViewById(R.id.nameTV);
		nameTV.setText(succResult.getInterviewer());
		
		jobTitleTV = (TextView) this.findViewById(R.id.jobTitleTV);
		jobTitleTV.setText(succResult.getJobtitle());
		
		continueExam = (TextView) this.findViewById(R.id.continueExam);
		continueExam.setText(exam.getName()+getExamProgress());
		
		continueBtn = (Button) findViewById(R.id.continueBtn);
		continueBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
	        	
				int ccIndex = 1;
				int cqIndex = 1;
				if(getccIndex()>0 && getcqIndex()>0){
					ccIndex = getccIndex();
					cqIndex = getcqIndex();
				}
				
				Question fQuestion = DataParseUtil.getQuestionByCidQid(exam, ccIndex, cqIndex);
				if(fQuestion==null){
					ShowDialog(getResources().getString(R.string.dialog_warning),"Can not get question!");
					return;
				}
				
				fQuestionType = fQuestion.getType();
				
				//move question
				Intent intent = new Intent();
				intent.putExtra(SPUtil.CURRENT_EXAM_CATALOG, String.valueOf(ccIndex));
				intent.putExtra(SPUtil.CURRENT_EXAM_INDEX_IN_CATA, String.valueOf(cqIndex));
				if(QUESTION_TYPE.MULTIPLE_CHOICE.equals(fQuestionType)){
					intent.putExtra("questionType",questionTypes[0]);
					intent.setClass( getBaseContext(), MultiChoices.class);
					startActivity(intent);
				}else if(QUESTION_TYPE.SINGLE_CHOICE.equals(fQuestionType)){
					intent.putExtra("questionType",questionTypes[1]);
					intent.setClass( getBaseContext(), SingleChoices.class);
					startActivity(intent);
				}else{
					ShowDialog(mContext.getResources().getString(R.string.dialog_note),"Invalid qeustion type.");
				}
				
//				Log.i(LOG_TAG, "----------Start a New Exam!-----------------");
//				
//				SPUtil.save2SP(SPUtil.CURRENT_EXAM_STATUS, SPUtil.EXAM_STATUS_START_GOING, sharedPreferences);
//				go2QuestionByType(fQuestionType,mContext);
//				saveQuestionMovePara(ccIndex,cqIndex,fQuestionType,sharedPreferences);
//				
//				Log.i(LOG_TAG, "--------------------------------------------");
			}			
		});
		
		cancelBtn = (Button) findViewById(R.id.cancelBtn);
		cancelBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				goHome(mContext);
			}			
		});
	}
	
	public String getExamProgress(){
		String continueExamInfo ;
		DatabaseUtil dbUtil = new DatabaseUtil(mContext);
		dbUtil.open();
		int examAnsweredQuestionSum = dbUtil.fetchAllAnswersCount();
		int examQuestionSum = DataParseUtil.getExamQuestionSum(exam);
		int per = 100 * examAnsweredQuestionSum/examQuestionSum;
		continueExamInfo = "  ("+String.valueOf(per)+"% "+getResources().getString(R.string.msg_complete)+")";
		dbUtil.close();
		return continueExamInfo;
	}

}