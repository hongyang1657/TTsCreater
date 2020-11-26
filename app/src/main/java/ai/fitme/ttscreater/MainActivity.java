package ai.fitme.ttscreater;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.qmuiteam.qmui.layout.QMUIButton;
import com.qmuiteam.qmui.widget.QMUILoadingView;
import java.io.File;
import ai.fitme.ttscreater.model.GenerateTTsOnlineModel;
import ai.fitme.ttscreater.utils.FileUtil;
import ai.fitme.ttscreater.utils.L;
import ai.fitme.ttscreater.utils.PermissionsUtils;
import ai.fitme.ttscreater.utils.StatusBarUtil;

public class MainActivity extends Activity {
    //权限
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE,Manifest.permission.RECORD_AUDIO};

    private EditText etInput;
    private QMUIButton btStart;
    private TextView tvPath;
    private QMUILoadingView qmuiLoadingView;
    private GenerateTTsOnlineModel generateTTsOnlineModel = null;

    private static final int MULTI_SENTENCES_PROCESS = 1;
    private static final int GENERATE_FAILED = 2;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MULTI_SENTENCES_PROCESS:
                    index++;
                    if (index==sentences.length){
                        qmuiLoadingView.stop();
                        qmuiLoadingView.setVisibility(View.GONE);
                        btStart.setBackgroundColor(getResources().getColor(R.color.colorRbSelected));
                        btStart.setFocusable(true);
                        btStart.setClickable(true);
                        Toast.makeText(getApplicationContext(), "全部完成", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(getApplicationContext(), sentences[index-1]+".wav生成成功", Toast.LENGTH_SHORT).show();
                        generateTTS(sentences[index]);
                        handler.obtainMessage(MULTI_SENTENCES_PROCESS);
                    }
                    break;
                case GENERATE_FAILED:
                    qmuiLoadingView.stop();
                    qmuiLoadingView.setVisibility(View.GONE);
                    btStart.setBackgroundColor(getResources().getColor(R.color.colorRbSelected));
                    btStart.setFocusable(true);
                    btStart.setClickable(true);
                    Toast.makeText(getApplicationContext(), "生成迎宾词失败，请检查网络后重试", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setTransparent(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_main);
        initView();
        boolean isPermissionPassed = PermissionsUtils.getInstance().chekPermissions(this,permissions , new PermissionsUtils.IPermissionsResult() {
            @Override
            public void passPermissons() {
                L.i("---------------权限通过----------------");
            }

            @Override
            public void forbitPermissons() {
                L.i("---------------权限禁止----------------");
            }
        });
        L.i("isPermissionPassed:"+isPermissionPassed);
        if (!isPermissionPassed){

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (generateTTsOnlineModel!=null){
            generateTTsOnlineModel.destory();
        }
    }

    private void initView(){
        etInput = findViewById(R.id.et_input);
        btStart = findViewById(R.id.bt_start);
        tvPath = findViewById(R.id.tv_path);
        qmuiLoadingView = findViewById(R.id.loading);
        qmuiLoadingView.setVisibility(View.GONE);
        btStart.setRadius(20);
        btStart.setChangeAlphaWhenPress(true);
        FileUtil.makeDir();
    }

    //private String content;
    private String ttsFileName;
    private int index = 0;
    String[] sentences;
    private void getTTsData(){
        //判断是多行输入还是单行输入
        String input = etInput.getText().toString();
        if (input.trim().length()==0){
            Toast.makeText(getApplicationContext(), "内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (input.contains("\n")){
            //多行语句输入
            sentences = input.split("\n");
        }else {
            //单语句输入
            sentences = new String[]{input};
        }
        qmuiLoadingView.start();
        qmuiLoadingView.setVisibility(View.VISIBLE);
        btStart.setBackgroundColor(getResources().getColor(R.color.colorRbNotSelected));
        btStart.setFocusable(false);
        btStart.setClickable(false);
        //在线生成自定义的tts
        generateTTS(sentences[index]);
    }

    //在线生成自定义的tts
    private void generateTTS(final String content){
        if (generateTTsOnlineModel==null){
            generateTTsOnlineModel = new GenerateTTsOnlineModel(this);
        }
        generateTTsOnlineModel.generateTTs(content, new GenerateTTsOnlineModel.OnTTsGenerateListener() {
            @Override
            public void onError() {
                L.i("生成失败");
                handler.obtainMessage(GENERATE_FAILED);
            }

            @Override
            public void onSuccess(byte[] audioData) {
                //音频文件存在本地
                if (content.length()<10){
                    ttsFileName = "tts/tts/"+content+".wav";
                }else {
                    ttsFileName = "tts/tts/"+content.substring(0,10)+".wav";
                }
                boolean isSuccess = FileUtil.setFileAtRoot(ttsFileName,audioData);
                L.i("生成音频文件名："+ttsFileName+" 是否成功："+isSuccess);
                handler.obtainMessage(MULTI_SENTENCES_PROCESS);
            }
        });
    }

    public void click(View v){
        switch (v.getId()){
            case R.id.bt_start:
                getTTsData();
                break;
            case R.id.tv_path:
                //跳转文件夹
                openAssignFolder();
                break;
        }
    }

    private void openAssignFolder(){
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tts";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        //调用系统文件管理器打开指定路径目录
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(getUriForFile(this,dir), "audio/*");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //startActivityForResult(intent, REQUEST_CHOOSEFILE);
        startActivityForResult(Intent.createChooser(intent,"选择浏览工具"),REQUEST_CHOOSEFILE);
    }

    private static Uri getUriForFile(Context context, File file) {
        if (context == null || file == null) {
            throw new NullPointerException();
        }
        Uri uri;
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(context.getApplicationContext(), "ttscreater.fileprovider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionsUtils.getInstance().onRequestPermissionsResult(this,requestCode,permissions,grantResults);
    }

    private final int REQUEST_CHOOSEFILE = 1;
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){//选择文件返回
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode==RESULT_OK){
            switch(requestCode){
                case REQUEST_CHOOSEFILE:
                    break;
            }
        }
    }
}
