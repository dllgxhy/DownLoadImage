package com.example.newmate1102.downloadimage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button downloadimage;
    private String urlstr ="http://img4q.duitang.com/uploads/item/201501/13/20150113172039_Ea5ry.jpeg";
    private ImageView image;
    private final static  int FILE_DELETE = 1;
    private final static  int IMAGE_SHOW = 2;

    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case FILE_DELETE:
                    Toast.makeText(MainActivity.this,"文件存在，已删除",Toast.LENGTH_SHORT).show();
                    image.setVisibility(View.INVISIBLE);
                    break;
                case IMAGE_SHOW:
                    String url_local = msg.getData().getString("filepath");
                    Bitmap bitmap = getLoacalBitmap(url_local); //从本地取图片(在cdcard中获取)  //
                    image .setImageBitmap(bitmap); //设置Bitmap
                    image.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };

    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);  ///把流转化为Bitmap图片

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        downloadimage = (Button)findViewById(R.id.downloadimage);
        image = (ImageView)findViewById(R.id.image);
        downloadimage.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                OutputStream out = null;
                String path = "xuhyfile";
                final String filename = "beauty.jpg";
                try{
                    URL url = new URL(urlstr);
                    HttpURLConnection connect = (HttpURLConnection)url.openConnection();
                    String SDCard = Environment.getExternalStorageDirectory()+"";
                    String filepath = SDCard+"/"+ path +"/" +filename;

                    File file = new File(filepath);
                    Message msg = new Message();
                    if(file.exists()){
                        file.delete();
                        msg.what = FILE_DELETE;
                        handler.sendMessage(msg);
                        Log.d("new thread", "file.exists");

                    }else {
                        InputStream input = connect.getInputStream();
                        String dir = SDCard + "/" + path;
                        new File(dir).mkdir();
                        file.createNewFile();
                        out = new FileOutputStream(file);

                        byte[] buffer = new byte[4 * 1024];
                        int count = 0;
                        while ((count = input.read(buffer)) > 0) {
                            out.write(buffer,0,count);
                            Log.d("count = ",count+"");
                        }

                        out.flush();
                        msg.what = IMAGE_SHOW;
                        Bundle bundle = new Bundle();
                        bundle.putString("filepath", filepath);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                        Log.d("write data", "file not exists");
                    }
                }catch(MalformedURLException e) {
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                } finally {
                    try{
                        if(out !=null){
                            out.close();
                        }
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
