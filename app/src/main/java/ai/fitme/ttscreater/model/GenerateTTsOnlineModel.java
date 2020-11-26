package ai.fitme.ttscreater.model;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisCancellationDetails;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisResult;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


import ai.fitme.ttscreater.utils.L;

import static android.Manifest.permission.INTERNET;

/**
 * 微软tts
 * https://docs.microsoft.com/zh-cn/azure/cognitive-services/speech-service/language-support#text-to-speech
 */

public class GenerateTTsOnlineModel {

    /**
     * https://portal.azure.com/#@hy375913212gmail.onmicrosoft.com/resource/subscriptions/8e6822bf-9ac9-436f-8d8d-9c02f88ba0ad/resourcegroups/fitme/providers/Microsoft.CognitiveServices/accounts/fitmeSound/cskeys
     * fitmeSound2 密钥1：968e945232df486d9bf6d83899555169
     * 密钥2：4ea1829cc22441248d2e5cdea20c5b1a
     */


    private static String speechSubscriptionKey = "968e945232df486d9bf6d83899555169";
    private static String serviceRegion = "southeastasia";
    private SpeechConfig speechConfig;
    private SpeechSynthesizer synthesizer;
    private ExecutorService singleThread;
    private SpeechSynthesisResult result;
    private Future<SpeechSynthesisResult> task;

    public GenerateTTsOnlineModel(Context context){
        singleThread = Executors.newSingleThreadExecutor();
        int requestCode = 5; // unique code for the permission request
        ActivityCompat.requestPermissions((Activity) context, new String[]{INTERNET}, requestCode);
        // Initialize speech synthesizer and its dependencies
        speechConfig = SpeechConfig.fromSubscription(speechSubscriptionKey, serviceRegion);
        speechConfig.setSpeechRecognitionLanguage("zh-CN");
        //speechConfig.setSpeechSynthesisLanguage("zh-TW");
        assert(speechConfig != null);
        synthesizer = new SpeechSynthesizer(speechConfig);
        assert(synthesizer != null);
    }



    public void destory(){
        // Release speech synthesizer and its dependencies
        try {
            task.cancel(true);
            if (result!=null){
                result.close();
            }
            synthesizer.close();
            speechConfig.close();
        }catch (Exception e){
            L.i("e:"+e.toString());
        }

    }

    public void generateTTs(final String content, final OnTTsGenerateListener listener){
        singleThread.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    L.i("start");
                    //TODO
                    String ssml = "<speak version=\"1.0\" xmlns=\"https://www.w3.org/2001/10/synthesis\" xmlns:mstts=\"https://www.w3.org/2001/mstts\" xml:lang=\"zh-CN\">\n" +
                            "    <voice name=\"zh-CN-XiaoxiaoNeural\">\n" +
                            "        <mstts:express-as type=\"newcast\">\n" +
                            "            <prosody volume=\"+50.00%\" pitch=\"high\">" + content + "\n" +
                            "            </prosody>\n" +
                            "        </mstts:express-as>\n" +
                            "    </voice>\n" +
                            "</speak>";
                    task = synthesizer.SpeakSsmlAsync(ssml);


                    // Note: this will block the UI thread, so eventually, you want to register for the event
                    //task = synthesizer.SpeakTextAsync(content);
                    result = task.get();
                    assert(result != null);

                    L.i("audio length:"+result.getAudioLength()+" reson = "+ result.getReason().name());
                    // L.i(i+"生成完毕："+ FileUtil.setFile(getApplicationContext(),strings[i]+".wav",result.getAudioData()));

                    if (result.getReason() == ResultReason.SynthesizingAudioCompleted) {
                        listener.onSuccess(result.getAudioData());
                    }
                    else if (result.getReason() == ResultReason.Canceled) {
                        String cancellationDetails =
                                SpeechSynthesisCancellationDetails.fromResult(result).toString();
                        L.i("Error synthesizing. Error detail: " +
                                System.lineSeparator() + cancellationDetails +
                                System.lineSeparator() + "Did you update the subscription info?");
                        listener.onError();
                    }
                    result.close();
                } catch (Exception ex) {
                    Log.e("SpeechSDKDemo", "unexpected " + ex.getMessage());
                    assert(false);
                }
            }
        });
    }

    public interface OnTTsGenerateListener{
        void onError();
        void onSuccess(byte[] audioData);
    }
}
