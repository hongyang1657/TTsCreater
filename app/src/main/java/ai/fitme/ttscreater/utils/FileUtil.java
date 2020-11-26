package ai.fitme.ttscreater.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static android.content.Context.MODE_PRIVATE;

public class FileUtil {

    private static String SDPATH = "";
    /**
     * 获取到sd卡的根目录，并以String形式返回
     *
     * @return
     */
    public static String getSDCardPath() {
        SDPATH = Environment.getExternalStorageDirectory() + "/";
        return SDPATH;
    }

    public static void makeDir(){
        String video_savePath = Environment.getExternalStorageDirectory()
                .toString() + Constants.TTS_PATH;
        File file = new File(video_savePath);
        // video文件夹不存在
        if (!file.exists()) {
            // 创建文件夹
            file.mkdirs();
        }
    }

    public static boolean setFileAtRoot(String fileName, byte[] data){
        try {
            // 判断当前的手机是否有sd卡
            String state = Environment.getExternalStorageState();

            if(!Environment.MEDIA_MOUNTED.equals(state)) {
                // 已经挂载了sd卡
                L.i("已经挂载了sd卡");
                return false;
            }
            File sdCardFile = Environment.getExternalStorageDirectory();
            File file = new File(sdCardFile, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.flush();
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            L.i("setFileAtRoot e:"+e.toString());
        }
        return false;

    }

    //在/data/data/包名/ 下生成文件
    public static boolean setFile(Context context,String path, byte[] data){
        try {
            FileOutputStream fout = context.openFileOutput(path, MODE_PRIVATE);

            fout.write(data);
            fout.close();
        } catch (IOException e) {
            L.i("写文件错误");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    //获取文件流数据
    public static byte[] getFileStream(Context context,String path){
        try {
            FileInputStream fis = context.openFileInput(path);
            byte[] buf = new byte[fis.available()];
            int len;
            while ((len=fis.read(buf))!=-1){
                L.i("---------buff:"+buf.length);
            }

            fis.close();
            return buf;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //获取assets目录的文件流
    public static byte[] getAssetsFileStream(Context context,String fileName){
        try {
            InputStream is = context.getAssets().open(fileName);
            byte[] buf = new byte[is.available()];
            int len;
            while ((len=is.read(buf))!=-1){
                L.i("buff:"+buf.length);
            }
            is.close();
            return buf;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
