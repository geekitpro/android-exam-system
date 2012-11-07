package com.dream.ivpc;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

	public class CandidateInfoResumePdf extends CandidateInfoBase {
		private final static String TAG = "CandidateInfoResumePdf";
	    private final static String ALBUM_PATH = Environment.getExternalStorageDirectory() + "/download_test/";
	    private ImageView imageView;
	    private Button btnSave;
	    private ProgressDialog myDialog = null;
	    private Bitmap bitmap;
	    private String fileName;
	    private String message;
	    
	    
	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.candidate_info_resume);
	        
	        mContext = getApplicationContext();
	        
	        imageView = (ImageView)findViewById(R.id.imgSource);
	        btnSave = (Button)findViewById(R.id.btnSave);
	        
	        String filePath = "http://ww2.sinaimg.cn/large/8161d11bjw1dym8g5uzmdj.jpg";
	        fileName = "test.jpg";
	        
	        try {
	        	//////////////// 取得的是byte数组, 从byte数组生成bitmap
	        	byte[] data = getImage(filePath);      
	            if(data!=null){      
	                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);// bitmap      
	                imageView.setImageBitmap(bitmap);// display image      
	            }else{      
	                Toast.makeText(CandidateInfoResumePdf.this, "Image error!", 1).show();      
	            }
	            ////////////////////////////////////////////////////////

	            //******** 取得的是InputStream，直接从InputStream生成bitmap ***********/
	        	bitmap = BitmapFactory.decodeStream(getImageStream(filePath));
	            if (bitmap != null) {
	            	imageView.setImageBitmap(bitmap);// display image
	            }
	            //********************************************************************/
	            Log.d(TAG, "set image ...");
	        } catch (Exception e) {   
	            Toast.makeText(CandidateInfoResumePdf.this,"Newwork error!", 1).show();   
	            e.printStackTrace();   
	        }   

	        
	        // 下载图片
	        btnSave.setOnClickListener(new Button.OnClickListener(){
	            public void onClick(View v) {
	                myDialog = ProgressDialog.show(CandidateInfoResumePdf.this, "保存图片", "图片正在保存中，请稍等...", true);
	                new Thread(saveFileRunnable).start();
	        }
	        });
	    }

	    /**  
	     * Get image from newwork  
	     * @param path The path of image  
	     * @return byte[]
	     * @throws Exception  
	     */  
	    public byte[] getImage(String path) throws Exception{   
	        URL url = new URL(path);   
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();   
	        conn.setConnectTimeout(5 * 1000);   
	        conn.setRequestMethod("GET");   
	        InputStream inStream = conn.getInputStream();   
	        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){   
	            return readStream(inStream);   
	        }   
	        return null;   
	    }   
	  
	    /**  
	     * Get image from newwork  
	     * @param path The path of image  
	     * @return InputStream
	     * @throws Exception  
	     */
	    public InputStream getImageStream(String path) throws Exception{   
	        URL url = new URL(path);   
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();   
	        conn.setConnectTimeout(5 * 1000);   
	        conn.setRequestMethod("GET");
	        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){   
	        	return conn.getInputStream();      
	        }   
	        return null; 
	    }
	    /**  
	     * Get data from stream 
	     * @param inStream  
	     * @return byte[]
	     * @throws Exception  
	     */  
	    public static byte[] readStream(InputStream inStream) throws Exception{   
	        ByteArrayOutputStream outStream = new ByteArrayOutputStream();   
	        byte[] buffer = new byte[1024];   
	        int len = 0;   
	        while( (len=inStream.read(buffer)) != -1){   
	            outStream.write(buffer, 0, len);   
	        }   
	        outStream.close();   
	        inStream.close();   
	        return outStream.toByteArray();   
	    } 

	    /**
	     * 保存文件
	     * @param bm
	     * @param fileName
	     * @throws IOException
	     */
	    public void saveFile(Bitmap bm, String fileName) throws IOException {
	        File dirFile = new File(ALBUM_PATH);
	        if(!dirFile.exists()){
	            dirFile.mkdir();
	        }
	        File myCaptureFile = new File(ALBUM_PATH + fileName);
	        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
	        bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
	        bos.flush();
	        bos.close();
	    }
	    
	    private Runnable saveFileRunnable = new Runnable(){
	        @Override
	        public void run() {
	            try {
	                saveFile(bitmap, fileName);
	                message = "图片保存成功！";
	            } catch (IOException e) {
	                message = "图片保存失败！";
	                e.printStackTrace();
	            }
	            messageHandler.sendMessage(messageHandler.obtainMessage());
	        }
	            
	    };
	    
	    private Handler messageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            myDialog.dismiss();
	            Log.d(TAG, message);
	            Toast.makeText(CandidateInfoResumePdf.this, message, Toast.LENGTH_SHORT).show();
	        }
	    };
	}
