package com.handheld.uhfrdemo;

import java.util.ArrayList;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.*;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.handheld.uhfr.R;
import com.handheld.uhfr.UHFRManager;
import com.uhf.api.cls.Reader;

import static android.content.Context.MODE_PRIVATE;

public class MainActivity extends FragmentActivity implements View.OnClickListener {

    private FragmentManager mFm; //fragment manager
    private FragmentTransaction mFt;//fragment transaction
    private Fragment1_Inventory fragment1;//

//    private ArrayList<Fragment> fragments;


    public static UHFRManager mUhfrManager;//uhf
    public static Set<String> mSetEpcs; //epc set ,epc list

    private TextView textView_title;
    private TextView textView_f1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView(); // Init UI

        Util.initSoundPool(this);//Init sound pool
        mSharedPreferences = getSharedPreferences("UHF",MODE_PRIVATE);
    }

    private SharedPreferences mSharedPreferences;
    @Override
    protected void onResume() {
        super.onResume();
        mUhfrManager = UHFRManager.getIntance(MainActivity.this);// Init Uhf module
        if(mUhfrManager!=null){
            mUhfrManager.setPower(mSharedPreferences.getInt("readPower",30), mSharedPreferences.getInt("writePower",30));//set uhf module power
            mUhfrManager.setRegion(Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion",1)));
            Toast.makeText(getApplicationContext(),"FreRegion:"+Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion",1))+
                    "\n"+"Read Power:"+mSharedPreferences.getInt("readPower",30)+
                    "\n"+"Write Power:"+mSharedPreferences.getInt("writePower",30),Toast.LENGTH_LONG).show();
            showToast(getString(R.string.inituhfsuccess));
        }else {
            showToast(getString(R.string.inituhffail));
        }
    }



    // when
    @Override
    protected void onPause() {
        super.onPause();
//        Log.e("main","pause");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mUhfrManager != null) {//close uhf module
            mUhfrManager.close();
            mUhfrManager = null;
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
//        Log.e("main","destroy");
        if (mUhfrManager != null) {//close uhf module
            mUhfrManager.close();
            mUhfrManager = null;
        }
    }

    private void initView() {
//        Log.e("main","init view" );
        fragment1 = new Fragment1_Inventory();
        mFragmentCurrent = fragment1;

//        fragments = new ArrayList<Fragment>();
//        fragments.add(fragment1);
//        fragments.add(fragment2);
//        fragments.add(fragment3);
//        fragments.add(fragment4);
//        fragments.add(fragment5);

        mFm = getSupportFragmentManager();
        mFt = mFm.beginTransaction();
        mFt.add(R.id.framelayout_main, fragment1);
        mFt.commit();

        textView_title = (TextView) findViewById(R.id.title);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_about) {
            PackageManager packageManager = getPackageManager();
            PackageInfo packInfo = null;
            try {
                packInfo = packageManager.getPackageInfo(getPackageName(), 0);
                String version = packInfo.versionName;//get this version
                showToast("Version:" + version
                    +"\nDate:"+"2017-05-20" +"\nType:"+mUhfrManager.getHardware());
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private Fragment mFragmentCurrent;
    //switch fragments
    public void switchContent(Fragment to) {
//        Log.e("switch",""+to.getId());
        textView_f1.setTextColor(getResources().getColor(R.color.gre));

        if (mFragmentCurrent != to) {
            mFt = mFm.beginTransaction();
            if (!to.isAdded()) {    //
                mFt.hide(mFragmentCurrent).add(R.id.framelayout_main, to).commit(); //
            } else {
                mFt.hide(mFragmentCurrent).show(to).commit(); //
            }
            mFragmentCurrent = to;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        }
    }

    private long exitTime = 0;//key down time
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (System.currentTimeMillis() - exitTime >= 2000) {
                    exitTime = System.currentTimeMillis();
                    showToast(getString(R.string.quit_on_double_click_));
                    return true;
                } else {
                    showToast(getString(R.string.exiting));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    finish();
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private Toast mToast;
    //show toast
    private void showToast(String info) {
        if (mToast == null)
            mToast = Toast.makeText(this, info, Toast.LENGTH_SHORT);
        else
            mToast.setText(info);
        mToast.show();
    }
}
