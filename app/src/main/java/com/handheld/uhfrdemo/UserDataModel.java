package com.handheld.uhfrdemo;

/**
 * Created by Administrator on 2017/8/6 0006.
 */

public class UserDataModel {
    public String tagEPC;
    public String nfcUid;
    public String startTime;
    public String temperaure;
    public String count;
    public String interval;
    public String miniLimit;
    public String maxLimit;
    public String currenTime;
    public String lastDataByte;
    public String outOfLimit;
    //public String readCount;

    public int readCount;

    UserDataModel()
    {
        tagEPC="";
        nfcUid="";
        startTime="";
        temperaure="";
        count="";
        interval="";
        miniLimit="";
        maxLimit="";
        currenTime="";
        lastDataByte="";
        outOfLimit="";
        readCount=0;
    }
}
