package ai.fitme.ttscreater.utils;

public interface UdpReceiveListener {
    void onReceiver(byte[] bytes);
    void onReceiver(String msg);
}
