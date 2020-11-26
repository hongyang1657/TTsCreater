package ai.fitme.ttscreater.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.IOException;
import java.io.InputStream;

public class Constants {

    //梯控版本
    public static final int PROTOCOL_RELAY = 1001;
    public static final int PROTOCOL_OPTICAL = 1002;
    public static final int PROTOCOL_SAMSUNG = 1003;

    //adb状态
    public static final int ADB_PULL = 1000;
    public static final int ADB_PUSH = 1001;
    public static final int ADB_STAT = 1002;
    public static final int ADB_SHELL = 1003;

    //push 模式
    public static final String PUSH_MODE = "pushMode";
    public static final int MODE_MAIN = 2001;
    public static final int MODE_ADVERTISING = 2002;

    //登记楼层应答
    public static final String[] REGISTER_ANSWER_LIST = {"","intent/已登记.wav","<登记楼层>"};

    public static final String TARGET_PATH_CONFIG_CONF = "/data/found/data/config.conf";

    public static final String TARGET_PATH_TTS_CONFIG = "/data/found/data/tts/ttsConfig.json";

    public static final String TARGET_PATH_MODEL_CONFIG = "/data/found/model/model_config.json";

    public static final String TARGET_PATH_SERIAL_CONFIG = "/data/found/model/serial_IO_config.json";

    public static final String TARGET_PATH_GREET_TTS = "/data/found/data/tts/intent/welcome.wav";      //自定义迎宾问候词路径

    public static final String TARGET_PATH_ADVERTISEMENT = "/data/found/advertisement/advertisement.json";    //广告词配置文件

    public static final String TARGET_PATH_ADVER = "/data/found/advertisement/";

    public static final String test_path = "/data/found/data/tts/intent/99.pcm";
    //public static final String test_path = "/usr/bin/senseflow";
    

    public static final String WAKEUP_SOUND_DEFAULT = "9";
    public static final String WAKEUP_SOUND_1 = "0";
    public static final String WAKEUP_SOUND_2 = "1";
    public static final String WAKEUP_SOUND_3 = "2";
    public static final String WAKEUP_SOUND_4 = "3";

    public static final String DEFAULT_OPEN_DOOR_INDEX = "31";
    public static final String DEFAULT_CLOSE_DOOR_INDEX = "32";

    public static final String CANCEL_TWICE = "02";
    public static final String CANCEL_THRICE = "03";
    public static final String CANCEL_LONGPRESS = "01";

    //开关门信号引脚
    public static final String opendoor_pin_1 = "67";
    public static final String opendoor_pin_2 = "66";
    //开关门信号电平
    public static final String  opendoor_val_h = "1";
    public static final String  opendoor_val_l = "0";
    //回应聊天答疑
    public static final float CLOSE_ANSWER_QUESTION = 1.1f;
    public static final float OPEN_ANSWER_QUESTION = 0f;

    public static final String[] floorItems = new String[]{"-9楼","-8楼","-7楼","-6楼","-5楼","-4楼","-3楼",
            "-2楼", "-1楼","1楼","2楼","3楼","4楼","5楼","6楼","7楼","8楼","9楼","10楼","11楼","12楼",
            "13楼","14楼","15楼","16楼","17楼","18楼","19楼","20楼","21楼","22楼","23楼","24楼","25楼",
            "26楼","27楼","28楼","29楼","30楼","31楼","32楼","33楼","34楼","35楼","36楼","37楼","38楼","39楼","40楼"
            ,"41楼","42楼","43楼","44楼","45楼","46楼","47楼","48楼","49楼","50楼","51楼","52楼","53楼","54楼"
            ,"55楼","56楼","57楼","58楼","59楼","60楼","61楼","62楼","63楼","64楼"};

    public static final String CUSTOM_GREET = "custom_greet";

    public static String getFileContent(Context context,String assetsFileName){
        InputStream is = null;
        String result = "";
        try {
            is = context.getAssets().open(assetsFileName);

            int lenght = is.available();
            byte[] buffer = new byte[lenght];
            is.read(buffer);
            result = new String(buffer, "utf8");
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String getChannel(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return appInfo.metaData.getString("channel");

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }


    public int getVersionCode(Context context) {
        // 包管理器 可以获取清单文件信息
        PackageManager packageManager = context.getPackageManager();
        try {
            // 获取包信息
            // 参1 包名 参2 获取额外信息的flag 不需要的话 写0
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getVersionName(Context context) {

        //获取包管理器
        PackageManager pm = context.getPackageManager();
        //获取包信息
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            //返回版本号
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;

    }

}
