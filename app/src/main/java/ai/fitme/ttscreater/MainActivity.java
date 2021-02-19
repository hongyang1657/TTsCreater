package ai.fitme.ttscreater;

import androidx.annotation.NonNull;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.qmuiteam.qmui.layout.QMUIButton;
import com.qmuiteam.qmui.widget.QMUILoadingView;
import java.util.Arrays;
import ai.fitme.ttscreater.model.GenerateTTsOnlineModel;
import ai.fitme.ttscreater.utils.Constants;
import ai.fitme.ttscreater.utils.FileUtil;
import ai.fitme.ttscreater.utils.L;
import ai.fitme.ttscreater.utils.PermissionsUtils;
import ai.fitme.ttscreater.utils.StatusBarUtil;
import ai.fitme.ttscreater.utils.ToastUtil;

public class MainActivity extends Activity {
    //权限
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE,Manifest.permission.RECORD_AUDIO};

    private EditText etInput;
    private QMUIButton btStart;
    private RadioGroup radioGroup;
    private QMUILoadingView qmuiLoadingView;
    private GenerateTTsOnlineModel generateTTsOnlineModel = null;

    private static final int MULTI_SENTENCES_PROCESS = 1;
    private static final int GENERATE_FAILED = 2;
    private static String speaker = GenerateTTsOnlineModel.CHINESE_GIRL;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MULTI_SENTENCES_PROCESS:
                    L.i("index:"+index);
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
        radioGroup = findViewById(R.id.radio_g);
        qmuiLoadingView = findViewById(R.id.loading);
        qmuiLoadingView.setVisibility(View.GONE);
        btStart.setRadius(20);
        btStart.setChangeAlphaWhenPress(true);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rb_chinese:
                        speaker = GenerateTTsOnlineModel.CHINESE_GIRL;
                        break;
                    case R.id.rb_english:
                        speaker = GenerateTTsOnlineModel.ENGLISH;
                        break;
                }
            }
        });
        FileUtil.makeDir();
    }

    private String ttsFileName;
    private int index = 0;
    String[] sentences;
    private void getTTsData(){
        index = 0;
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
        L.i("sentence:"+ Arrays.toString(sentences));
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
        L.i("generateTTS:"+index);
        if (generateTTsOnlineModel==null){
            generateTTsOnlineModel = new GenerateTTsOnlineModel(this);
        }
        generateTTsOnlineModel.generateTTs(content,speaker, new GenerateTTsOnlineModel.OnTTsGenerateListener() {
            @Override
            public void onError() {
                L.i("生成失败");
                handler.sendEmptyMessage(GENERATE_FAILED);
            }

            @Override
            public void onSuccess(byte[] audioData) {
                String timestamp = String.valueOf(System.currentTimeMillis());
                //音频文件存在本地
//                if (content.length()<10){
//                    ttsFileName = Constants.TTS_PATH + content + timestamp + ".wav";
//                }else {
//                    ttsFileName = Constants.TTS_PATH + content.substring(0,10)+ timestamp +".wav";
//                }
                ttsFileName = Constants.TTS_PATH + content + ".wav";
                boolean isSuccess = FileUtil.setFileAtRoot(ttsFileName,audioData);
                L.i("生成音频文件名："+ttsFileName+" 是否成功："+isSuccess);
                handler.sendEmptyMessage(MULTI_SENTENCES_PROCESS);
            }
        });
    }

    public void click(View v){
        switch (v.getId()){
            case R.id.bt_start:
                getTTsData();
                break;
            case R.id.tv_path:
                //提示存放的路径
                ToastUtil.showToast(this,"音频文件存储地址:"+FileUtil.getSDCardPath()+Constants.TTS_PATH);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionsUtils.getInstance().onRequestPermissionsResult(this,requestCode,permissions,grantResults);
    }

}
