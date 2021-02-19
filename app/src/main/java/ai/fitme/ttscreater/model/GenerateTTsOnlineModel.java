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
 * SSML 合成规则
 * https://docs.microsoft.com/zh-cn/azure/cognitive-services/speech-service/speech-synthesis-markup?tabs=csharp#adjust-speaking-styles
 *
 * style="newscast"	以正式专业的语气叙述新闻
 * style="customerservice"	以友好热情的语气为客户提供支持
 * style="assistant"	以热情而轻松的语气对数字助理讲话
 * style="chat"	以轻松、随意的语气闲聊
 * style="calm"	以沉着冷静的态度说话。 语气、音调、韵律与其他语音类型相比要统一得多。
 * style="cheerful"	以较高的音调和音量表达欢快、热情的语气
 * style="sad"	以较高的音调、较低的强度和较低的音量表达悲伤的语气。 这种情绪的常见特征是说话时呜咽或哭泣。
 * style="angry"	以较低的音调、较高的强度和较高的音量来表达恼怒的语气。 说话者处于愤怒、生气和被冒犯的状态。
 * style="fearful"	以较高的音调、较高的音量和较快的语速来表达恐惧、紧张的语气。 说话者处于紧张和不安的状态。
 * style="disgruntled"	表达轻蔑和抱怨的语气。 这种情绪的语音表现出不悦和蔑视。
 * style="serious"	表达严肃和命令的语气。 说话者的声音通常比较僵硬，节奏也不那么轻松。
 * style="affectionate"	以较高的音调和音量表达温暖而亲切的语气。 说话者处于吸引听众注意力的状态。 说话者的“个性”往往是讨人喜欢的。
 * style="gentle"	以较低的音调和音量表达温和、礼貌和愉快的语气
 * style="lyrical"	以优美又带感伤的方式表达情感
 *
 * pitch	指示文本的基线音节。 可将音节表述为：
 * 以某个数字后接“Hz”（赫兹）表示的绝对值。 例如 <prosody pitch="600Hz">some text</prosody>。
 * 以前面带有“+”或“-”的数字，后接“Hz”或“st”（用于指定音节的变化量）表示的相对值。 例如 <prosody pitch="+80Hz">some text</prosody> 或 <prosody pitch="-2st">some text</prosody>。 “st”表示变化单位为半音，即，标准全音阶中的半调（半步）。
 * 常量值：
 * x-low
 * low
 * 中
 * high
 * x-high
 * 默认值
 */

public class GenerateTTsOnlineModel {

    /**
     * https://portal.azure.com/#@hy375913212gmail.onmicrosoft.com/resource/subscriptions/8e6822bf-9ac9-436f-8d8d-9c02f88ba0ad/resourcegroups/fitme/providers/Microsoft.CognitiveServices/accounts/fitmeSound/cskeys
     * fitmeSound2 密钥1：968e945232df486d9bf6d83899555169
     * 密钥2：4ea1829cc22441248d2e5cdea20c5b1a
     */

    //发音人
    public static final String CHINESE_GIRL = "zh-CN-XiaoxiaoNeural";
    public static final String CHINESE_BOY = "zh-CN-YunyangNeural";
    public static final String CHINESE_BOY_STORY = "zh-CN-YunyeNeural";
    public static final String CHINESE_TW_GIRL = "zh-TW-HsiaoChenNeural";
    public static final String CHINESE_LITTER_GIRL = "zh-CN-XiaoyouNeural";
    public static final String ENGLISH = "en-US-JennyNeural";


    private static String speechSubscriptionKey = "968e945232df486d9bf6d83899555169";
    private static String serviceRegion = "southeastasia";
    private SpeechConfig speechConfig;
    private SpeechSynthesizer synthesizer;
    private ExecutorService singleThread;
    private SpeechSynthesisResult result;
    private Future<SpeechSynthesisResult> task;

    public GenerateTTsOnlineModel(Context context) {
        singleThread = Executors.newSingleThreadExecutor();
        int requestCode = 5; // unique code for the permission request
        ActivityCompat.requestPermissions((Activity) context, new String[]{INTERNET}, requestCode);
        // Initialize speech synthesizer and its dependencies
        speechConfig = SpeechConfig.fromSubscription(speechSubscriptionKey, serviceRegion);
        speechConfig.setSpeechRecognitionLanguage("zh-CN");
        //speechConfig.setSpeechSynthesisLanguage("zh-TW");
        assert (speechConfig != null);
        synthesizer = new SpeechSynthesizer(speechConfig);
        assert (synthesizer != null);
    }


    public void destory() {
        // Release speech synthesizer and its dependencies
        try {
            task.cancel(true);
            if (result != null) {
                result.close();
            }
            synthesizer.close();
            speechConfig.close();
        } catch (Exception e) {
            L.i("e:" + e.toString());
        }

    }

    public void generateTTs(final String content, final String speaker, final OnTTsGenerateListener listener) {
        singleThread.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    L.i("start");
                    //TODO
                    String ssml = "<speak version=\"1.0\" xmlns=\"https://www.w3.org/2001/10/synthesis\" xmlns:mstts=\"https://www.w3.org/2001/mstts\" xml:lang=\"zh-CN\">\n" +
                            "    <voice name=\""+speaker+"\">\n" +
                            "        <mstts:express-as type=\"customerservice\">\n" +
                            "            <prosody volume=\"+50.00%\" pitch=\"high\">" + content + "\n" +
                            "            </prosody>\n" +
                            "        </mstts:express-as>\n" +
                            "    </voice>\n" +
                            "</speak>";

                    task = synthesizer.SpeakSsmlAsync(ssml);


                    // Note: this will block the UI thread, so eventually, you want to register for the event
                    //task = synthesizer.SpeakTextAsync(content);
                    result = task.get();
                    assert (result != null);

                    L.i("audio length:" + result.getAudioLength() + " reson = " + result.getReason().name());
                    // L.i(i+"生成完毕："+ FileUtil.setFile(getApplicationContext(),strings[i]+".wav",result.getAudioData()));

                    if (result.getReason() == ResultReason.SynthesizingAudioCompleted) {
                        listener.onSuccess(result.getAudioData());
                    } else if (result.getReason() == ResultReason.Canceled) {
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
                    assert (false);
                }
            }
        });
    }

    public interface OnTTsGenerateListener {
        void onError();

        void onSuccess(byte[] audioData);
    }
}
