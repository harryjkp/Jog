package com.rashem.audio;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;

import java.io.File;
import java.util.List;

/**
 * Plays a MP3 Radio stream using MediaExtractor, MediaCodec and AudioTrack
 *
 * @author Juan Carlos Ospina Gonzalez / juan@supersteil.com
 */
public class asdf {
    public static double k = 1;
    public static float mspeedfromgps = 0;
    public static String mLastUpdateTime;
    public static String a;
    public static double orig_rate = 0;
    public static String fleep;// = "//mnt//sdcard//music//Dirty_Deeds//07_Aint_no_Fun_Waiting.mp3";
    public static double var2;
    public static double var3=0;
    public static double oldvar4=0;
    public static double oldvar2=0;
    public static double var4=0;
    public static double newtime;
    public static double oldtime;
    public static double testspeed;
    public static double fleepspeed;
    public static double testdist;
    public static double lastnonzerok;
    public static double targetspeed = 3.0;
    public static double oldtestdist;
    public static List<Double> oldlatarray;
    public static List<Double> oldlongarray;
    public static List<Long> oldtimearray;
    public static int var5;
    public static double var6;
    public static boolean var7;
    public static int buffer_multiplier = 6;
    public static int var8=0;
    public static int var9=2;
    public static String m3u;
    public static Context context;
    public static int var10;
    public static int var11;
    public static String m4u;
    public static Uri temp;
    public static Context temp2;
    public static String oldm4u;
    public static int oldvar11=1;
    public static int no_songs;
    public static int oldvar12=0;
    public static int var13=0;
    public static int var14=1;
    public static int var15=0;
    public static int noplay;
    public static int fleepers;
    public static double vol=1;
    public static double usedvol=1;
    public static double speedfactorfrommps=0.44704;
    public static boolean preang;
    public static double convertedtargetspeed;
    public static double accfactor;
    public static double wahtever;
    public static String lorp;
    public static String fakespeedfactorfrommps;
    public static int invert=1;
    public static String bloop;
    public static double button_factor=0.3;
    public static double minor_button_factor=1.0;
    public static boolean playing;
    public static int spin_pos;
    public static int stopthatplayer=0;
    public static boolean playingisit;
    public static int started=0;
}
