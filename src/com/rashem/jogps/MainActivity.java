package com.rashem.jogps;


import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;

import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.rashem.audio.MP3RadioStreamDelegate;
import com.rashem.audio.MP3RadioStreamPlayer;
import com.rashem.audio.asdf;
import com.rashem.jogps.R;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class MainActivity extends Activity implements MP3RadioStreamDelegate,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener,AdapterView.OnItemSelectedListener,SensorEventListener {

    private SensorManager mSensorManager;

    private Sensor mStepCounterSensor;

    private Sensor mStepDetectorSensor;
    private Drawable changed_color_play;
    private Drawable changed_color_next;
    private Drawable changed_color_last;
    private Button mLastButton;
    private View button_bar;
    private Drawable changed_color_pause;
    private boolean been=false;

    public void PlaySongsFromAPlaylist(int playListID){

        String[] ARG_STRING = {MediaStore.Audio.Media._ID,MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.DISPLAY_NAME,MediaStore.Video.Media.SIZE,android.provider.MediaStore.MediaColumns.DATA};
        Uri membersUri = MediaStore.Audio.Playlists.Members.getContentUri("external", playListID);
        Cursor songsWithingAPlayList =getContentResolver().query(membersUri, ARG_STRING, null, null, null);
        asdf.temp=membersUri;
        asdf.temp2=getApplicationContext();
        int theSongIDIwantToPlay = asdf.var11; // PLAYING FROM THE FIRST SONG
        if(songsWithingAPlayList != null)
        {
            asdf.no_songs=songsWithingAPlayList.getCount();
            songsWithingAPlayList.moveToPosition(theSongIDIwantToPlay);
            String DataStream = songsWithingAPlayList.getString(4);
            //PlayMusic(DataStream);
            asdf.m4u=DataStream;
            songsWithingAPlayList.close();
        }
    }

    public static void PlayMusic(String DataStream){
        MediaPlayer mpObject = new MediaPlayer();
        if(DataStream == null)
            return;
        try {

            mpObject.setDataSource(DataStream);
            mpObject.prepare();
            //mpObject.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    double distance_on_geoid(double lat1, double lon1, double lat2, double lon2) {



// Convert degrees to radians
        lat1 = lat1 * Math.PI / 180.0;
        lon1 = lon1 * Math.PI / 180.0;

        lat2 = lat2 *Math.PI / 180.0;
        lon2 = lon2 * Math.PI / 180.0;

// radius of earth in metres
        double r = 6378100;

// P
        double rho1 = r * Math.cos(lat1);
        double z1 = r * Math.sin(lat1);
        double x1 = rho1 * Math.cos(lon1);
        double y1 = rho1 * Math.sin(lon1);

// Q
        double rho2 = r * Math.cos(lat2);
        double z2 = r * Math.sin(lat2);
        double x2 = rho2 * Math.cos(lon2);
        double y2 = rho2 * Math.sin(lon2);

// Dot product
        double dot = (x1 * x2 + y1 * y2 + z1 * z2);
        double cos_theta = dot / (r * r);

        double theta = Math.acos(cos_theta);

// Distance in Metres
        if (lat1==lat2 && lon1==lon2){
            r=0;
        }
        return r * theta;
    }



    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;
    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;


    protected Location mCurrentLocation;

    protected Boolean mRequestingLocationUpdates = true;
    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;


    //private EditText mEditSpeed;
    private double scaling_factor;
    private Button mNextButton;
    private Button mPlayButton;
    private Button mStopButton;
    private ProgressBar mProgressBar;
    MP3RadioStreamPlayer player;
    private static final String TAG = "MainActivity";



    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }




    public EditText editText;
    public TextView mdisplayspeed;
    public NumberPicker numberPicker;
    public ArrayList<String> playlist_ids = new ArrayList<String>();

    private Drawable resize2(Drawable image,double x, double y) {
        // Get the source image's dimensions
        Bitmap c = ((BitmapDrawable)image).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        c.compress(Bitmap.CompressFormat.PNG, 100, stream);
        //c.compress(Bitmap.CompressFormat.PNG, 100, stream);
        InputStream is = new ByteArrayInputStream(stream.toByteArray());


        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        //BitmapFactory.decodeStream(is);


        int srcWidth = options.outWidth;
        int srcHeight = options.outHeight;

/*
// Only scale if the source is big enough. This code is just trying to fit a image into a certain width.
        if (desiredWidth > srcWidth)
            desiredWidth = srcWidth;


        // Calculate the correct inSampleSize/scale value. This helps reduce memory use. It should be a power of 2
// from: http://stackoverflow.com/questions/477572/android-strange-out-of-memory-issue/823966#823966
        int inSampleSize = 1;
        while (srcWidth / 2 > desiredWidth) {
            srcWidth /= 2;
            srcHeight /= 2;
            inSampleSize *= 2;
        }
*/

        float desiredScale = (float) y;//desiredWidth / srcWidth;

// Decode with inSampleSize

        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inSampleSize = 0;//inSampleSize;
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap sampledSrcBitmap = BitmapFactory.decodeStream(is,null,options);//BitmapFactory.decodeFile(STRING_PATH_TO_FILE, options);

        // Resize
        Matrix matrix = new Matrix();
        matrix.postScale(desiredScale, desiredScale);
        Bitmap scaledBitmap = Bitmap.createBitmap(sampledSrcBitmap, 0, 0, sampledSrcBitmap.getWidth(), sampledSrcBitmap.getHeight(), matrix, true);
        sampledSrcBitmap = null;
        return new BitmapDrawable(getApplicationContext().getResources(), scaledBitmap);
    }
    private Drawable resize(Drawable image,double x, double y) {
        Bitmap b = ((BitmapDrawable)image).getBitmap();
        Bitmap scaledImage = Bitmap.createBitmap ((int)(b.getWidth()*x), (int)(b.getHeight()*y), Bitmap.Config.ARGB_8888);

        Canvas offscreenCanvas = new Canvas (scaledImage);
        Matrix matrix = new Matrix();
        //matrix.setRotate (rotations, centreX, centreY);
        matrix.postScale((float)x, (float)y);
        offscreenCanvas.setMatrix (matrix);

        offscreenCanvas.drawBitmap (b, 0, 0, new Paint(Paint.DITHER_FLAG));


        //Bitmap bitmapResized = Bitmap.createScaledBitmap(b, (int)(x*(double)b.getWidth()), (int)(y*(double)b.getHeight()), false);

        BitmapDrawable a = new BitmapDrawable(getApplicationContext().getResources(), scaledImage);
        //a.setBounds(0,0,(int)(x*(double)b.getWidth()), (int)(y*(double)b.getHeight()));
        return a;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //ColorDrawable colorback = new ColorDrawable(R.color.act_bar_colour);
        //actionBar.setBackgroundDrawable(colorback);



        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        //display.
        int width = size.x;
        int height = size.y;
        float dpi = metrics.ydpi;
        dpi=dpi+1;
        //getResources().getDrawable(R.drawable.button_play).setColorFilter(0xffff0000, PorterDuff.Mode.DST);//=====#####=======================##
        int iColor =getResources().getColor(R.color.colour_of_buttons_text);

        int red = (iColor & 0xFF0000) / 0xFFFF;
        int green = (iColor & 0xFF00) / 0xFF;
        int blue = iColor & 0xFF;

        float[] matrix = { 0, 0, 0, 0, red
                , 0, 0, 0, 0, green
                , 0, 0, 0, 0, blue
                , 0, 0, 0, 1, 0 };

        ColorFilter colorFilter = new ColorMatrixColorFilter(matrix);
        changed_color_play=getResources().getDrawable(R.drawable.button_play);
        changed_color_pause=getResources().getDrawable(R.drawable.button_pause);
        changed_color_next=getResources().getDrawable(R.drawable.button_next);
        changed_color_last=getResources().getDrawable(R.drawable.button_last);



        scaling_factor= (double) (size.y*metrics.ydpi*asdf.button_factor/(854.0*240.0));


        //changed_color_play= ScaleDrawable(changed_color_play,,double,double)
        //ScaleDrawable sd = new ScaleDrawable(changed_color_play, 0, scaling_factor, scaling_factor);
        //sd.setLevel(800);

        //resize(changed_color_play, scaling_factor, scaling_factor);
        //tempdr.setBounds(Rect);
        double scalar = 0.75;

        double floopit=1.0;
        while (floopit>scaling_factor) {
            changed_color_play = resize2(changed_color_play, scalar, scalar);
            floopit=floopit*scalar;
        }
        floopit=1.0;
        while (floopit>scaling_factor) {
            changed_color_pause=resize2(changed_color_pause, scalar, scalar);
            floopit = floopit * scalar;
        }
        floopit=1.0;
        while (floopit>scaling_factor) {
            changed_color_next=resize2(changed_color_next, scalar, scalar);
            floopit = floopit * scalar;
        }
        floopit=1.0;
        while (floopit>scaling_factor) {
            changed_color_last=resize2(changed_color_last, scalar, scalar);
            floopit = floopit * scalar;
        }
        //changed_color_pause=resize(changed_color_pause,scaling_factor,scaling_factor);
        //changed_color_next=resize(changed_color_next,scaling_factor*asdf.minor_button_factor,scaling_factor*asdf.minor_button_factor);
        //changed_color_last=resize(changed_color_last,scaling_factor*asdf.minor_button_factor,scaling_factor*asdf.minor_button_factor);
        //changed_color_play.setBounds(0, 0, 20, 50);
        //changed_color_play.setBounds(0, 0, 50, 50);



        changed_color_pause.setColorFilter(colorFilter);
        changed_color_play.setColorFilter(colorFilter);
        changed_color_next.setColorFilter(colorFilter);
        changed_color_last.setColorFilter(colorFilter);

        PackageManager pm = getApplicationContext().getPackageManager();
        if (pm.hasSystemFeature("FEATURE_SENSOR_STEP_COUNTER")) {
            mSensorManager = (SensorManager)
                    getSystemService(Context.SENSOR_SERVICE);
            mStepCounterSensor = mSensorManager
                    .getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            mStepDetectorSensor = mSensorManager
                    .getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        }else{

        }


        ArrayList<String> playlist_names = new ArrayList<String>();
        playlist_ids.add("nothing here - corresponds to select playlist");
        playlist_names.add("Select Playlist");

        setContentView(R.layout.activity_main);
        String[] projection = { MediaStore.Audio.Playlists._ID, MediaStore.Audio.Playlists.NAME };
        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null);
        //asdf.m3u=MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        //asdf.context= getApplicationContext();

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                asdf.m3u=cursor.getString(0);//+cursor.getString(1);
                playlist_ids.add(asdf.m3u);
                playlist_names.add(cursor.getString(1));
                // do stuff with each playlist
            } while (cursor.moveToNext());
        }

        cursor.close();
        Spinner spinner = (Spinner) findViewById(R.id.planets_spinner);
// Create an ArrayAdapter using the string array and a default spinner layout
        //ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        //        R.array.planets_array, android.R.layout.simple_spinner_item);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, R.layout.cust_spinner, playlist_names);
// Specify the layout to use when the list of choices appears
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setOnItemSelectedListener(this);
        buildGoogleApiClient();


        createLocationRequest();
        numberPicker = (NumberPicker) findViewById(R.id.numberpicker1);
        numberPicker.setMaxValue(100);
        numberPicker.setMinValue(0);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setOnValueChangedListener( new NumberPicker.
                OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int
                    oldVal, int newVal) {
                asdf.targetspeed=(double)newVal;}

        });

        editText = (EditText) findViewById(R.id.speed);
        mdisplayspeed = (TextView) findViewById(R.id.textdisplayspeed);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                asdf.targetspeed=((double)Integer.parseInt(textView.getText().toString()))*asdf.speedfactorfrommps;//*0.44704;//()(string)asdf.fleepers;//(double)i*0.44704;


                return false;
            }
        });
        //button_bar=this.findViewById(R.id.button_bar);


        mPlayButton = (Button) this.findViewById(R.id.button1);


        mPlayButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (asdf.noplay==0){
                    if (asdf.var14==0) {

                        if (asdf.var15 == 0) {
                            pause();
                            //changed_color_play=R.drawable.button_play
                            mPlayButton.setCompoundDrawablesWithIntrinsicBounds( changed_color_play, null, null,null);//mPlayButton.setCompoundDrawablesWithIntrinsicBounds( R.drawable.button_play, 0, 0,0);
                        } else {
                            unpause();
                            mPlayButton.setCompoundDrawablesWithIntrinsicBounds( changed_color_pause, null, null,null);
                        }
                    }else {
                        asdf.var15 =0;
                        play();
                        asdf.var14=0;
                        asdf.oldvar12 = 0;
                        asdf.var13 = 0;
                    }
                }
            }});
/*        mStopButton = (Button) this.findViewById(R.id.button2);
        mStopButton.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                stop();
*//*
                mPlayButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.button_play, 0, 0, 0);
                asdf.var14=1;

                if (asdf.var13==0) {
                    asdf.var11 = asdf.var11 + 1;
                    if (asdf.var11 == asdf.no_songs) {
                        asdf.var11 = 0;
                        asdf.oldvar12 = 1;

                    }
                }
                asdf.var13=1;*//*
            }
        });*/
        mProgressBar = (ProgressBar) this.findViewById(R.id.progressBar1);

        showGUIStopped();
        mNextButton = (Button) this.findViewById(R.id.button3);


        mNextButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (asdf.var13 == 0) {
                    asdf.var11 = asdf.var11 + 1;
                    if (asdf.var11 == asdf.no_songs) {
                        asdf.var11 = 0;
                        asdf.oldvar12 = 1;
                        stop();

                    }
                }


            }
        });
        mLastButton = (Button) this.findViewById(R.id.button4);

        mLastButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (asdf.var13 == 0) {
                    asdf.var11 = asdf.var11 + 1;
                    if (asdf.var11 == asdf.no_songs) {
                        asdf.var11 = 0;
                        asdf.oldvar12 = 1;
                        stop();

                    }
                }


            }
        });
        mPlayButton.setCompoundDrawablesWithIntrinsicBounds( changed_color_play, null, null,null);
        mLastButton.setCompoundDrawablesWithIntrinsicBounds( changed_color_last, null, null,null);
        mNextButton.setCompoundDrawablesWithIntrinsicBounds( changed_color_next, null, null,null);
        mPlayButton.setCompoundDrawablesWithIntrinsicBounds( changed_color_pause, null, null,null);
/*        ViewGroup.LayoutParams params = mPlayButton.getLayoutParams();
        int eep = mPlayButton.getWidth();
        params.width= -2;//(int) (eep*scaling_factor);
        params.height= -2;//(int) (params.height*scaling_factor);
        mPlayButton.setLayoutParams(params);


        ViewGroup.LayoutParams pa2rams = mNextButton.getLayoutParams();
        pa2rams.width= -2;//(int) (pa2rams.width*scaling_factor);
        pa2rams.height= -2;//(int) (pa2rams.height*scaling_factor);
        mNextButton.setLayoutParams(pa2rams);
        ViewGroup.LayoutParams pa3rams = mLastButton.getLayoutParams();
        pa3rams.width= -2;//(int) (pa3rams.width*scaling_factor);
        pa3rams.height= -2;//(int) (pa3rams.height*scaling_factor);
        mLastButton.setLayoutParams(pa3rams);*/

    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (been==false) {
            View vg = findViewById (R.id.relativeLayout);
            vg.invalidate();
/*            super.onWindowFocusChanged(hasFocus);
            ViewGroup.LayoutParams params = mPlayButton.getLayoutParams();
            float eep = mPlayButton.getWidth();
            params.width = (int) ((float) (mPlayButton.getWidth()) * scaling_factor);
            params.height = (int) ((float) (mPlayButton.getHeight()) * scaling_factor);//(int) (params.height*scaling_factor);
            mPlayButton.setLayoutParams(params);


            ViewGroup.LayoutParams pa2rams = mNextButton.getLayoutParams();
            pa2rams.width = (int) ((float) (mNextButton.getWidth()) * scaling_factor);//(int) (pa2rams.width*scaling_factor);
            pa2rams.height = (int) ((float) (mNextButton.getHeight()) * scaling_factor);//(int) (pa2rams.height*scaling_factor);
            mNextButton.setLayoutParams(pa2rams);
            ViewGroup.LayoutParams pa3rams = mLastButton.getLayoutParams();
            pa3rams.width = (int) ((float) (mLastButton.getWidth()) * scaling_factor);//(int) (pa3rams.width*scaling_factor);
            pa3rams.height = (int) ((float) (mLastButton.getHeight()) * scaling_factor);//(int) (pa3rams.height*scaling_factor);
            mLastButton.setLayoutParams(pa3rams);*/
            been=true;
        }
        // Call here getWidth() and getHeight()
    }


    //public GoogleApiClient mGoogleApiClient;

    public synchronized void buildGoogleApiClient() {
        mGoogleApiClient =  new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
// Sets the desired interval for active location updates. This interval is
// inexact. You may not receive updates at all if no location sources are available, or
// you may receive them slower than requested. You may also receive updates faster than
// requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
// Sets the fastest rate for active location updates. This interval is exact, and your
// application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
// The final argument to {@code requestLocationUpdates()} is a LocationListener
// (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }


    protected void stopLocationUpdates() {
// It is a good practice to remove location requests when the activity is in a paused or
// stopped state. Doing so helps battery performance and is especially
// recommended in applications that request frequent location updates.
// The final argument to {@code requestLocationUpdates()} is a LocationListener
// (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
    }
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }
    @Override
    public void onResume() {
        super.onResume();
// Within {@code onPause()}, we pause location updates, but leave the
// connection to GoogleApiClient intact. Here, we resume receiving
// location updates if the user has requested them.
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
// Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        //stopLocationUpdates();
    }
    @Override
    protected void onStop() {
        super.onStop();
        //if (mGoogleApiClient.isConnected()) {
        //mGoogleApiClient.disconnect();

    }
    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");
// If the initial location was never previously requested, we use
// FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
// its value in the Bundle and check for it in onCreate(). We
// do not request it again unless the user specifically requests location updates by pressing
// the Start Updates button.
//
// Because we cache the value of the initial location in the Bundle, it means that if the
// user launches the activity,
// moves to a new location, and then changes the device orientation, the original location
// is displayed as the activity is re-created.
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

        }
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
// attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }
    public PowerManager.WakeLock wl;
    @Override
    public void onLocationChanged(Location location) {



        mCurrentLocation = location;


        if (asdf.var2==0^asdf.var2>100000^Double.isNaN(asdf.var2)){
            asdf.var2=mCurrentLocation.getLatitude();
            asdf.var4=mCurrentLocation.getLongitude();

        }


        asdf.wahtever=distance_on_geoid(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), asdf.var2, asdf.var4)-mCurrentLocation.getAccuracy();
        asdf.accfactor = 1/Math.pow((double)mCurrentLocation.getAccuracy(),1);//distance_on_geoid((double)mCurrentLocation.getLatitude(), (double)mCurrentLocation.getLongitude(), asdf.var2, asdf.var4)
        if (asdf.wahtever<0){
           //asdf.accfactor=0;
        }
        asdf.var2 = asdf.var2*(1.0-asdf.accfactor)+asdf.accfactor*mCurrentLocation.getLatitude();
        asdf.var4 = asdf.var4*(1.0-asdf.accfactor)+asdf.accfactor*mCurrentLocation.getLongitude();
        asdf.newtime=(double)System.currentTimeMillis()/1000;//mCurrentLocation.getElapsedRealtimeNanos();


        if (asdf.newtime-asdf.oldtime>1){
            asdf.fakespeedfactorfrommps=PreferenceManager.getDefaultSharedPreferences(this).getString("language_preference", "");
            asdf.speedfactorfrommps=Double.parseDouble(asdf.fakespeedfactorfrommps);
            if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("invert",true)) {
                asdf.invert =1;

            }else {
                asdf.invert =-1;
            }

            //asdf.var11=1-asdf.var11;
            try{
            if (asdf.var11!=asdf.oldvar11) {//need to put in a better place that is always called-------------------------------------------------------------

                PlaySongsFromAPlaylist(Integer.valueOf(asdf.m3u));
                asdf.oldvar11=asdf.var11;
                if (asdf.oldvar12==1){
                    stop();


                }


                if (player != null) {
                    play();
                }
            }}catch(NumberFormatException nfe){

            }




/*            if (asdf.var8==0) {
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Tag");
                asdf.var8=1;
            }*/
/*
            asdf.var9=asdf.var9-1;
            if (wl.isHeld()==false&asdf.var9>0){
                //wl.acquire();
            }
            if (asdf.var9<0){
                //wl.release();
            }
*/




            asdf.var6=asdf.newtime-asdf.oldtime;
            asdf.testdist=distance_on_geoid(asdf.oldvar2, asdf.oldvar4, asdf.var2, asdf.var4);
            asdf.testdist=distance_on_geoid(asdf.oldvar2, asdf.oldvar4, asdf.var2, asdf.var4);

            asdf.fleepspeed= asdf.testdist/(asdf.newtime- asdf.oldtime);


            //asdf.oldtestdist=asdf.testdist;
            if (true){//asdf.newtime-asdf.oldtime>10) {
                asdf.oldvar2 = asdf.var2;
                asdf.oldvar4 = asdf.var4;
                asdf.oldtime = asdf.newtime;
            }
        }

        asdf.var7=mCurrentLocation.hasSpeed();
        if (asdf.var7==true){
            asdf.fleepspeed=asdf.mspeedfromgps;
            asdf.mspeedfromgps = mCurrentLocation.getSpeed();
        }
        mdisplayspeed.setText(String.format("%.1f", asdf.fleepspeed/asdf.speedfactorfrommps));

        //asdf.fleepspeed= (asdf.testdist-asdf.oldtestdist)/(asdf.newtime- asdf.oldtimearray.get(asdf.var5));//Math.pow((Math.pow(asdf.var2-asdf.oldvar2,2)+Math.pow(asdf.var4-asdf.oldvar4,2)),0.5)/(asdf.newtime-asdf.oldtime);
        //asdf.testspeed = asdf.testspeed*0.98+0.02* asdf.fleepspeed;//Math.pow((Math.pow(asdf.var2-asdf.oldvar2,2)+Math.pow(asdf.var4-asdf.oldvar4,2)),0.5)/(asdf.newtime-asdf.oldtime);



        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

        //Toast.makeText(this, getResources().getString(R.string.location_updated_message),
        //Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
// Refer to the javadoc for ConnectionResult to see what error codes might be returned in
// onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }
    /**
     * Called by Google Play services if the connection to GoogleApiClient drops because of an
     * error.
     */
    public void onDisconnected() {
        Log.i(TAG, "Disconnected");
    }
    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }









    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);

        return super.onCreateOptionsMenu(menu);
        //return true;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.action_settings:
                //asdf.targetspeed = 50;
                Intent i = new Intent(getApplicationContext(), SettingsActivity.class);//-------------------------------------start settings
                startActivity(i);
                //editText.performClick();
/*                // search action
                return true;
            case R.id.action_location_found:
                // location found
                LocationFound();
                return true;
            case R.id.action_refresh:
                // refresh
                return true;
            case R.id.action_help:
                // help action
                return true;
            case R.id.action_check_updates:
                // check for updates action
                return true;*/
            default:
                return super.onOptionsItemSelected(item);
        }}
    private void pause(){
        asdf.var15=1;
    }
    private void unpause(){
        asdf.var15=0;
    }

    private void play()
    {
        if(player != null)
        {
            player.stop();
            player.release();
            player = null;

        }

        player = new MP3RadioStreamPlayer();
        player.setUrlString("http://www.tonycuffe.com/mp3/tail%20toddle.mp3");

        player.setDelegate(this);

        showGUIBuffering();

        try {
            player.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stop()
    {
        mPlayButton.setCompoundDrawablesWithIntrinsicBounds(changed_color_play,null,null,null);
        asdf.var14=1;

        if (asdf.var13==0) {
            //asdf.var11 = asdf.var11 + 1; no longer changes song on stop
            if (asdf.var11 == asdf.no_songs) {
                asdf.var11 = 0;
                asdf.oldvar12 = 1;

            }
        }
        asdf.var13=1;
        if (player!=null) {
            player.stop();
            player.release();
            player = null;
        }
    }

    private void showGUIBuffering()
    {
        if (asdf.var15!=1) {
            mProgressBar.setVisibility(View.VISIBLE);
            mPlayButton.setEnabled(false);
            mPlayButton.setCompoundDrawablesWithIntrinsicBounds(changed_color_pause,null,null,null);//drawable.button_pause);
            //mStopButton.setEnabled(false);
        }
    }

    private void showGUIPlaying()
    {
        mProgressBar.setVisibility(View.INVISIBLE);
        mPlayButton.setEnabled(true);
        //mStopButton.setEnabled(true);
        //String fleepy = PreferenceManager.getDefaultSharedPreferences(this).getString("language_preference", "Italian");
        //if (fleepy == fleepy) {

        //}
        //Toast.makeText(getApplicationContext(),);
    }

    private void showGUIStopped()
    {
        mProgressBar.setVisibility(View.INVISIBLE);
        mPlayButton.setEnabled(true);
        //mStopButton.setEnabled(false);
    }


    /****************************************
     *
     *	Delegate methods. These are all fired from a background thread so we have to call any GUI code on the main thread.
     *
     ****************************************/

    @Override
    public void onRadioPlayerPlaybackStarted(MP3RadioStreamPlayer player) {
        Log.i(TAG, "onRadioPlayerPlaybackStarted");;
        this.runOnUiThread(new Runnable(){

            @Override
            public void run() {
                showGUIPlaying();
            }
        });
    }

    @Override
    public void onRadioPlayerStopped(MP3RadioStreamPlayer player) {
        this.runOnUiThread(new Runnable(){

            @Override
            public void run() {
                showGUIStopped();
            }
        });

    }

    @Override
    public void onRadioPlayerError(MP3RadioStreamPlayer player) {
        this.runOnUiThread(new Runnable(){

            @Override
            public void run() {
                showGUIStopped();
            }
        });

    }

    @Override
    public void onRadioPlayerBuffering(MP3RadioStreamPlayer player) {
        this.runOnUiThread(new Runnable(){

            @Override
            public void run() {
                showGUIBuffering();
            }
        });

    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
        if (pos!=0){
            asdf.noplay=0;
            asdf.var11 = 0;
            asdf.m3u=playlist_ids.get(pos);
            PlaySongsFromAPlaylist(Integer.valueOf(asdf.m3u));
            if (player != null) {
                stop();
            }
        }else{
            asdf.noplay=1;
            stop();
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        try {
            wait(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;
        float[] values = sensorEvent.values;
        int value = -1;

        if (values.length > 0) {
            value = (int) values[0];
        }
        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            //textView.setText("Step Counter Detected : " + value);
        } else if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            // For test only. Only allowed value is 1.0 i.e. for step taken
            //textView.setText("Step Detector Detected : " + value);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
