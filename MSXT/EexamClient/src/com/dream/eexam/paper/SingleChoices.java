package com.dream.eexam.paper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.dream.eexam.base.R;
import com.dream.eexam.model.Choice;
import com.dream.eexam.model.Question;
import com.dream.eexam.util.DatabaseUtil;
import com.dream.eexam.util.XMLParseUtil;

public class SingleChoices extends BaseQuestion {
	
	public final static String LOG_TAG = "SingleChoices";
	
	//components statement
	TextView questionTV = null;
	ListView listView;
	Button preBtn;
	Button nextBtn;

	//data statement
//	Question question;
	MyListAdapter adapter;
	List<String> listItemID = new ArrayList<String>();

	public void loadHeader(){
		homeTV = (TextView)findViewById(R.id.homeTV);
		remainingTimeLabel = (TextView)findViewById(R.id.remainingTimeLabel);
		remainingTime = (TextView)findViewById(R.id.remainingTime);
		completedSeekBar = (SeekBar) findViewById(R.id.completedSeekBar);
		completedPercentage = (TextView)findViewById(R.id.completedPercentage);
		
		catalogsTV = (TextView)findViewById(R.id.header_tv_catalogs);
//		currentTV = (TextView)findViewById(R.id.header_tv_current);
		waitTV = (TextView)findViewById(R.id.header_tv_waiting);
	}
	
	public void setHeader(){
		//set exam header(Left)
		homeTV.setText("Home");
		
		//set exam header(Right)
		remainingTimeLabel.setText("Time Remaining: ");
		remainingTime.setText(String.valueOf(detailBean.getTime())+" mins");
		completedSeekBar.setThumb(null);
		completedPercentage.setText("50%"+" Finished");
		
		//set question text
		catalogsTV.setText(detailBean.getCatalogDescByCid(currentCatalogIndex)+
				"("+String.valueOf(detailBean.getCatalogSizeByCid(currentCatalogIndex))+")");
		catalogsTV.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showWindow(v);
			}
		});
		
//    	currentTV.setText("Q "+String.valueOf(currentQuestionIndex)+" of "+String.valueOf(questionSize));
    	
        //set question text
    	waitTV.setText("Wait "+ String.valueOf(questionSize - comQuestionSize));
    	waitTV.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});  
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.paper_single_choices);
        mContext = getApplicationContext();
        
        loadHeader();
        loadAnswer();
        setHeader();
    	
        //set question text
        questionTV = (TextView)findViewById(R.id.questionTV);
        questionTV.setText(question.getContent());
        questionTV.setTextColor(Color.BLACK);
        
        //set List
        listView = (ListView)findViewById(R.id.lvChoices);
        adapter = new MyListAdapter(question.getChoices());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener(){
        	@Override
			public void onItemClick(AdapterView<?> adapter, View view, int arg2,
					long arg3) {
        		RadioButton cb = (RadioButton)view.findViewById(R.id.list_select);
        		cb.setChecked(!cb.isChecked());
			}      	
        });
        
        preBtn = (Button)findViewById(R.id.preBtn);
        preBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				direction = -1;
				relocationQuestion();
			}
		});
        
        nextBtn = (Button)findViewById(R.id.nextBtn);
        nextBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				direction = 1;
				relocationQuestion();
			}
		});
    
    }
    
    //save answer if not empty 
    public void relocationQuestion(){
    	//clear answer first
    	listItemID.clear();
    	answerString.setLength(0);

    	//get selection choice and assembly to string
		
		//get selection
		for (int i = 0; i < adapter.mChecked.size(); i++) {
			if (adapter.mChecked.get(i)) {
				Choice choice = adapter.choices.get(i);
				listItemID.add(String.valueOf(choice.getChoiceIndex()));
				if(i>0){
					answerString.append(",");
				}
				answerString.append(String.valueOf(choice.getChoiceIndex()));
			}
		}
		
		if (listItemID.size() == 0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(SingleChoices.this);
			builder.setMessage("Answer this question late?")
					.setCancelable(false)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,int id) {
									clearAnswer();
									gotoNewQuestion();
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,int id) {
									dialog.cancel();
								}
							});
			builder.show();
		} else {
			saveAnswer();
	    	gotoNewQuestion();
		}
		
    }
    
    //go to next or previous question
    public void gotoNewQuestion(){
    	InputStream inputStream =  getExamStream();
		String questionType = null;
		try {
			 questionType = XMLParseUtil.readQuestionType(inputStream,currentCatalogIndex,currentQuestionIndex+direction);
			 inputStream.close();
		} catch (Exception e) {
			Log.i(LOG_TAG, e.getMessage());
		}
		if(questionType!=null){
			//move question
			Intent intent = new Intent();
			intent.putExtra("ccIndex", String.valueOf(currentCatalogIndex));
			intent.putExtra("cqIndex", String.valueOf(currentQuestionIndex+direction));
			if(questionTypeM.equals(questionType)){
				intent.putExtra("questionType", "Multi Select");
				intent.setClass( getBaseContext(), MultiChoices.class);
			}else if(questionTypeS.equals(questionType)){
				intent.putExtra("questionType", "Single Select");
				intent.setClass( getBaseContext(), SingleChoices.class);
			}
			finish();
			startActivity(intent);
		}else{
			ShowDialog("Please Change Catalog!");
		}
    }
    
    public void clearAnswer(){
    	Log.i(LOG_TAG, "clearAnswer()...");
    	
    	DatabaseUtil dbUtil = new DatabaseUtil(this);
    	dbUtil.open();
    	dbUtil.deleteAnswer(currentCatalogIndex,currentQuestionIndex);
    	dbUtil.close();
    	
    	Log.i(LOG_TAG, "end clearAnswer().");
    }
    
    public void saveAnswer(){
    	Log.i(LOG_TAG, "saveAnswer()...");
    	
    	DatabaseUtil dbUtil = new DatabaseUtil(this);
    	dbUtil.open();
    	Cursor cursor = dbUtil.fetchAnswer(currentCatalogIndex,currentQuestionIndex);
    	if(cursor != null && cursor.moveToNext()){
    		Log.i(LOG_TAG, "updateAnswer("+currentCatalogIndex+","+currentQuestionIndex+","+answerString.toString()+")");
    		dbUtil.updateAnswer(currentCatalogIndex,currentQuestionIndex,"("+ answerString.toString()+")");
    	}else{
    		Log.i(LOG_TAG, "createAnswer("+currentCatalogIndex+","+currentQuestionIndex+","+answerString.toString()+")");
    		dbUtil.createAnswer(currentCatalogIndex,currentQuestionIndex, "("+ answerString.toString()+")");
    	}
    	
    	dbUtil.close();
    	
    	Log.i(LOG_TAG, "saveAnswer().");
    }
    
    class MyListAdapter extends BaseAdapter{
    	List<Boolean> mChecked = new ArrayList<Boolean>();
    	List<Choice> choices = new ArrayList<Choice>();
    	
		HashMap<Integer,View> map = new HashMap<Integer,View>(); 
    	
    	public MyListAdapter(List<Choice> choices){
    		this.choices = choices;
    		
    		Log.i(LOG_TAG,"answerString:"+answerString);
    		
			for (int i = 0; i < choices.size(); i++) {
				Choice choice = choices.get(i);
//				mChecked.add(false);
				if (answerString.indexOf(String.valueOf(choice.getChoiceIndex())) != -1) {
					mChecked.add(true);
				} else {
					mChecked.add(false);
				}
			}
    	}

		@Override
		public int getCount() {
			return choices.size();
		}

		@Override
		public Object getItem(int position) {
			return choices.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			ViewHolder holder = null;
			
			if (map.get(position) == null) {
				Log.i(LOG_TAG,"position1 = "+position);
				
				LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = mInflater.inflate(R.layout.paper_single_choices_item, null);
				holder = new ViewHolder();
				
				//set 3 component 
				holder.radioButton = (RadioButton)view.findViewById(R.id.radioButton);
				holder.index = (TextView)view.findViewById(R.id.index);
				holder.choiceDesc = (TextView)view.findViewById(R.id.choiceDesc);
				
				final int p = position;
				map.put(position, view);
				
				holder.radioButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						
						RadioButton cb = (RadioButton)buttonView;
						mChecked.set(p, cb.isChecked());
					}
				});
				
				view.setTag(holder);
			}else{
				Log.i(LOG_TAG,"position2 = "+position);
				view = map.get(position);
				holder = (ViewHolder)view.getTag();
			}
			
			Choice choice = choices.get(position);
			holder.radioButton.setChecked(mChecked.get(position));
			holder.index.setText(choice.getChoiceLabel());
			holder.choiceDesc.setText(choice.getChoiceContent());
			
			return view;
		}
    	
    }
    
    static class ViewHolder{
    	RadioButton radioButton;
    	TextView index;
    	TextView choiceDesc;
    }
}
