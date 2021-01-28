package com.example.p_openvc_face;
//1.搜尋download opevc for android studio,https://opencv.org/releases/,下載sdk
//2.加入api流程參考googel文件
//3.開啟網路權限
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

//B.實作CameraBridgeViewBase.CvCameraViewListener2
public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private JavaCameraView javaCameraView;
    private File cascFile;
    private CascadeClassifier cascadeClassifier;
    private Mat mRgba, mGray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

    }

    private void init() {
        javaCameraView  = findViewById(R.id.javaCameraView);

        //A.init
        if(!OpenCVLoader.initDebug()){
            //如果沒有正確的安裝lib
            OpenCVLoader.initAsync(//使用執行緒下載OpenCv Libs;
                    OpenCVLoader.OPENCV_VERSION, //1.下載的lib版本
                    this,//2.AppContext
                    baseLoaderCallback //3.callBack

                    );
        }else {
            //有正確安裝lib
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        javaCameraView.setCvCameraViewListener(this);
    }


    //當相機畫面開始時
    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.v("hank","onCameraViewStarted() /width:" + width +" /height:" + height);
        mGray = new Mat();
        mRgba = new Mat();
    }

    //當相機畫面停止時
    @Override
    public void onCameraViewStopped() {
        Log.v("hank","onCameraViewStopped()");
        mRgba.release();
        mGray.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.v("hank","onCameraFrame()");

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        //detect face 偵測臉部
        MatOfRect matOfRect = new MatOfRect();
        cascadeClassifier.detectMultiScale(mRgba,matOfRect);

        for(Rect rect : matOfRect.toArray()){
            Imgproc.rectangle(
                    mRgba,
                    new Point(rect.x,rect.y),
                    new Point(rect.x + rect.width , rect.y + rect.height),
                    new Scalar(255,0,0));
        }
        return mRgba;
    }

    //C.實作BaseLoaderCallback,帶入到OpenCVLoader.initAsync參數監聽
    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {


            switch (status) {
                case BaseLoaderCallback.SUCCESS:

                    //打開haarcascade_frontalface_alt2,輸入串流
                    InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);

                    //準備一個File
                    File caschadeDir = getDir("cascade", Context.MODE_PRIVATE);//創建一個contenxDir區域
                    cascFile = new File(caschadeDir, "haarcascade_frontalface_alt2.xml");

                    //輸出串流
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(cascFile);

                        byte[] buffer = new byte[4069];
                        int byteReader;

                        while ((byteReader = is.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, byteReader);
                        }

                        fileOutputStream.close();
                        is.close();

                        cascadeClassifier = new CascadeClassifier(cascFile.getAbsolutePath());//讀取哪個檔案
                        if (cascadeClassifier.empty()) {
                            cascadeClassifier = null;
                        } else
                            caschadeDir.delete();

                            javaCameraView.enableView();//啟動相機畫面連接

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                default:{
                    super.onManagerConnected(status);
                }
                    break;

            }
        }
    };
}