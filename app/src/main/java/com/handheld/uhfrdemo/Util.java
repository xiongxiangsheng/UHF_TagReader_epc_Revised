package com.handheld.uhfrdemo;

import java.util.HashMap;
import java.util.Map;

import com.handheld.uhfr.R;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class Util {

	
	public static SoundPool sp ;
	public static Map<Integer, Integer> suondMap;
	public static Context context;
	
	//
	public static void initSoundPool(Context context){
		Util.context = context;
		sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
		suondMap = new HashMap<Integer, Integer>();
		suondMap.put(1, sp.load(context, R.raw.beep, 1));
	}
	
	//
	public static  void play(int sound, int number){
		AudioManager am = (AudioManager)Util.context.getSystemService(Util.context.AUDIO_SERVICE);
	   //
	    float audioMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	        
	        //
	        float audioCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
	        float volumnRatio = audioCurrentVolume/audioMaxVolume;
	        sp.play(
	        		suondMap.get(sound), //
	        		audioCurrentVolume, //
	        		audioCurrentVolume, //
	                1, //
	                number, //
	                1);//
	    }
	public static void pasue() {
		sp.pause(0);
	}
	public static void copyData(byte []src,int startIndex,byte []dec,int len)
	{
		for(int i=0;i<len;i++)
		{
			dec[i]=src[startIndex+i];
		}
	}
	public static int bytesToInt(byte[] bytes) {
		int[] parts = new int[]{0, 0, 0, 0};
		for (int i = 0; i < Math.min(bytes.length, 4); i++) {
			parts[i] = (bytes[i] >= 0) ? bytes[i] : bytes[i] + 256;
		}
		return parts[0] + (parts[1] << 8) + (parts[2] << 16) + (parts[3] << 24);
	}
    public static String bytesToHexString(byte[] bytes, char separator) {
        String s = "0";
        StringBuilder hexString = new StringBuilder();
        if ((bytes != null) && (bytes.length > 0)) {
            for (byte b : bytes) {
                int n = b & 0xff;
                if (n < 0x10) {
                    hexString.append("0");
                }
                hexString.append(Integer.toHexString(n));
                if (separator != 0) {
                    hexString.append(separator);
                }
            }
            s = hexString.substring(0, hexString.length() - 1);
        }
        return s;
    }
	public static int CheckSum(byte []buf, int len)
	{
		int tem;
		int i=0,Sum=0;
		for (i=0;i<len;i++)
		{
			tem = (buf[i] >= 0) ? buf[i] : buf[i] + 256;
			Sum+=tem;

		}

		return Sum;
	}
}
