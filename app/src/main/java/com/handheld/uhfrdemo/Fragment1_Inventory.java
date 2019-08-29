package com.handheld.uhfrdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.BRMicro.Tools;
import com.handheld.uhfr.R;
import com.uhf.api.cls.Reader;
import com.uhf.api.cls.Reader.TAGINFO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class Fragment1_Inventory extends Fragment implements OnCheckedChangeListener,OnClickListener{
	private View view;// this fragment UI
	//private TextView tvTagCount;//tag count text view
	private TextView tvTagSum ;//tag sum text view
	private TextView tvScanRunStatus;
	private ListView lvEpc;// epc list view
	private Button btnStart ;//inventory button
	private Button btnClear ;// clear button
	public String tagEPC;
	//private CheckBox checkMulti ;//multi model check box

	private Set<String> epcSet = null ; //store different EPC
	private List<UserDataModel> listEpc = null;//EPC list
	private Map<String, Integer> mapEpc = null; //store EPC position
	private UserDataAdapter adapter ;//epc list adapter

	private boolean isMulti = false ;// multi mode flag
	private int allCount = 0 ;// inventory count

	Thread scanThread=null;
	private long lastTime =  0L ;// record play sound time
	//private CommunicationThread communicationThread = null;
	//handler
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what){
				case 1:
					//List<UserDataModel> listData= new List<UserDataModel>();
					String epc= (String)msg.obj;

					if(listEpc==null)
						listEpc = new ArrayList<UserDataModel>();

					UserDataModel userDataModel = readData(epc);
					if (userDataModel != null) {
						//userDataModel.tagEPC = listData.get(i);
						//userDataModel.tagEPC = epc;

						boolean exist=false;
						for(int i=0;i<listEpc.size();i++)//检查EPC是否存在
						{
							if(userDataModel.tagEPC.equals(listEpc.get(i).tagEPC))//已存在,要修改下其中的值
							{
								listEpc.get(i).startTime=userDataModel.startTime;
								listEpc.get(i).count=userDataModel.count;
								listEpc.get(i).interval=userDataModel.interval;
								listEpc.get(i).temperaure=userDataModel.temperaure;
								listEpc.get(i).lastDataByte=userDataModel.lastDataByte;
								listEpc.get(i).outOfLimit=userDataModel.outOfLimit;
								listEpc.get(i).readCount++;
								exist=true;
								break;
							}

						}
						if(exist==false)listEpc.add(userDataModel);//只有不存在时才加入列表

						Util.play(1, 0);
					}


					if(adapter==null) {
						adapter = new UserDataAdapter(getActivity(), listEpc);
						lvEpc.setAdapter(adapter);
					}
					else
					{
						adapter.notifyDataSetChanged();
					}

					allCount=listEpc.size();
					//tvTagCount.setText("" +  allCount);
					tvTagSum.setText("" + allCount);
					//Util.play(1, 0);
					MainActivity.mSetEpcs=epcSet;

					break ;
				//case 2:
					//stopInventoryScan();
					//break;
			}
		}
	} ;
	@Override
	public View onCreateView(LayoutInflater inflater,
							 @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.e("f1","create view");
		view= inflater.inflate(R.layout.fragment_inventory, null);
		initView();
		IntentFilter filter = new IntentFilter() ;
		filter.addAction("android.rfid.FUN_KEY");
		getActivity().registerReceiver(keyReceiver, filter) ;


		return view/*super.onCreateView(inflater, container, savedInstanceState)*/;
	}
	private UserDataModel readData(String epcstr){
		//UserDataModel userDataModel=new UserDataModel();
		///*
		UserDataModel userDataModel=null;
		int addr = 0 ;
		int len =32 ;
		try
		{
			//EPC结构：0x89  UID  起始时间   间隔   点数   温度
			//          1     7      4        4      2      2
			byte[] epcBytes = Tools.HexString2Bytes(epcstr) ;
			if((Util.bytesToInt(new byte[]{epcBytes[0]})==0x89)&&(epcBytes.length>=20))
			{
				userDataModel = new UserDataModel();

				long tem = Util.bytesToInt(new byte[]{epcBytes[8], epcBytes[9], epcBytes[10], epcBytes[11]});
				userDataModel.startTime = TimeHelp.getYearMonthDayHourMinuteSecond(tem * 1000);

				tem = Util.bytesToInt(new byte[]{epcBytes[12], epcBytes[13], epcBytes[14], epcBytes[15]});
				userDataModel.interval = Long.toString(tem&0xffffff);

				long times=tem>>24;
				userDataModel.outOfLimit=Long.toString(times);

				tem = Util.bytesToInt(new byte[]{epcBytes[16], epcBytes[17]});
				userDataModel.count = Long.toString(tem);

				tem = Util.bytesToInt(new byte[]{epcBytes[18], epcBytes[19]});
				//userDataModel.temperaure=Long.toString(tem);
				if (tem >= 0x00008000) {
					tem -= 0x10000;
				}
				float m = (float) tem / 10;
				userDataModel.temperaure = Float.toString(m);

				userDataModel.nfcUid = Util.bytesToHexString(new byte[]{epcBytes[1], epcBytes[2], epcBytes[3], epcBytes[4], epcBytes[5], epcBytes[6], epcBytes[7]}, ':');

				userDataModel.tagEPC = Tools.Bytes2HexString(epcBytes, 8);
				userDataModel.readCount++;
				/*
				byte [] tempd=new byte[8];
				for(int j=0;j<8;j++)
				{
					tempd[j]= epcBytes[j+12];
				}
				userDataModel.lastDataByte = Tools.Bytes2HexString(tempd, 8);
				*/

			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		//stopInventoryScan();
		//*/
		return userDataModel;
	}
	private void initView() {
		//tvTagCount = (TextView) view.findViewById(R.id.textView_tag_count);
		lvEpc = (ListView) view.findViewById(R.id.listView_epc);
		btnStart = (Button) view.findViewById(R.id.button_start);
		tvTagSum = (TextView) view.findViewById(R.id.textView_tag) ;
		tvScanRunStatus= (TextView) view.findViewById(R.id.textView_runstatus) ;
		//checkMulti = (CheckBox) view.findViewById(R.id.checkBox_multi) ;
		//checkMulti.setOnCheckedChangeListener( this);
		btnClear = (Button) view.findViewById(R.id.button_clear_epc) ;


		lvEpc.setFocusable(false);
		lvEpc.setClickable(false);
		lvEpc.setItemsCanFocus(false);
		lvEpc.setScrollingCacheEnabled(false);
		lvEpc.setOnItemClickListener(null);
		btnStart.setOnClickListener(this);
		btnClear.setOnClickListener(this);
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
//		Log.e("f1","destroy view");
		if (isStart) {
			isStart = false;
			isRunning = false;
			MainActivity.mUhfrManager.stopTagInventory();
		}
		getActivity().unregisterReceiver(keyReceiver);//
	}
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
//		Log.e("f1","pause");
		if (isStart) {
			runInventory();
		}
	}
	private boolean f1hidden = false;
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		f1hidden = hidden;
//		Log.e("hidden", "hide"+hidden) ;
		if (hidden) {
			if (isStart) runInventory();// stop inventory
		}
	}


	private boolean isRunning = false ;
	private boolean isStart = false ;
	String epc ;
	//inventory epc
	private Runnable inventoryTask = new Runnable() {
		@Override
		public void run() {
			while(isRunning){
				if (isStart)
				{
					List<TAGINFO> list1 ;
					if (isMulti)
					{ // multi mode
						list1 = MainActivity.mUhfrManager.tagInventoryRealTime();
					}
					else{
						//sleep can save electricity
//						try {
//							Thread.sleep(250);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
						list1 = MainActivity.mUhfrManager.tagInventoryByTimer((short)50);
						//list1 = MainActivity.mUhfrManager.tagInventoryRealTime();
					}//
					if (list1 != null&&list1.size()>0) {//
						for (TAGINFO tfs:list1) {
							byte[] epcdata = tfs.EpcId;
							epc = Tools.Bytes2HexString(epcdata, epcdata.length);
							if (epc == null || epc.length() == 0) {
								continue ;
							}

							Message msg;
							msg = Message.obtain();
							msg.what=1;
							msg.obj = epc;

							//Bundle b = new Bundle();
							//b.putString("epc", epc);
							//b.putString("rssi", rssi+"");
							//msg.setData(b);
							handler.sendMessage(msg);
						}

					}
				}
			}
		}
	} ;
	private boolean keyControl = true;
	public  void stopInventoryScan()
	{
		//checkMulti.setClickable(true);
		//checkMulti.setTextColor(Color.BLACK);
		if (isMulti)
			MainActivity.mUhfrManager.asyncStopReading();
		else
			MainActivity.mUhfrManager.stopTagInventory();
		try {
			Thread.sleep(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
		isRunning = false;
		//btnStart.setText(getResources().getString(R.string.start_inventory_epc));
		isStart = false;
	}
	private void runInventory() {
		if (keyControl) {
			keyControl = false;
			if (!isStart) {
				isRunning = true;
				cleanBuf();
				if (isMulti) {
					MainActivity.mUhfrManager.setFastMode();
					MainActivity.mUhfrManager.asyncStartReading();
				}else {
					MainActivity.mUhfrManager.setCancleFastMode();
				}
				new Thread(inventoryTask).start();
				btnStart.setText("Stop Intentory EPC");

				Resources resource = getResources();//动太改变文本颜色
				ColorStateList csl = (ColorStateList) resource.getColorStateList(R.color.alert_blue);
				if (csl != null) {
					tvScanRunStatus.setTextColor(csl);
				}
				tvScanRunStatus.setText("Run");

				isStart = true;
			} else {
				//checkMulti.setClickable(true);
				//checkMulti.setTextColor(Color.BLACK);
				if (isMulti)
					MainActivity.mUhfrManager.asyncStopReading();
				else
					MainActivity.mUhfrManager.stopTagInventory();
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					e.printStackTrace();
				}
				isRunning = false;
				btnStart.setText("Start Tag Scan");

				Resources resource = getResources();//动太改变文本颜色
				ColorStateList csl = (ColorStateList) resource.getColorStateList(R.color.text_red);
				if (csl != null) {
					tvScanRunStatus.setTextColor(csl);
				}
				tvScanRunStatus.setText("Stop");

				isStart = false;
			}
			keyControl = true;
		}
	}
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if (isChecked) isMulti = true;
		else isMulti = false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.button_start:
				//startTagRead();
				runInventory() ;
				//stopInventoryScan();
				break ;
			case R.id.button_clear_epc:
				clearEpc();
				break ;
		}
	}

	private void clearEpc(){
		if (epcSet != null) {
			epcSet.removeAll(epcSet); //store different EPC
		}
		if(listEpc != null)
			listEpc.removeAll(listEpc);//EPC list
		if(mapEpc != null)
			mapEpc.clear(); //store EPC position
		if(adapter != null)
			adapter.notifyDataSetChanged();
		allCount = 0 ;
		tvTagSum.setText("0");
		//tvTagCount.setText("0");
		//MainActivity.mSetEpcs.clear();
//        lvEpc.removeAllViews();
	}

	//show tips
	private Toast toast;
	private void showToast(String info) {
		if (toast==null) toast =  Toast.makeText(getActivity(), info, Toast.LENGTH_SHORT);
		else toast.setText(info);
		toast.show();
	}
	//key receiver
	private  long startTime = 0 ;
	private boolean keyUpFalg= true;
	private BroadcastReceiver keyReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (f1hidden) return;
			int keyCode = intent.getIntExtra("keyCode", 0) ;
			if(keyCode == 0){//H941
				keyCode = intent.getIntExtra("keycode", 0) ;
			}
//            Log.e("key ","keyCode = " + keyCode) ;
			boolean keyDown = intent.getBooleanExtra("keydown", false) ;
//			Log.e("key ", "down = " + keyDown);
			if(keyUpFalg&&keyDown && System.currentTimeMillis() - startTime > 500){
				keyUpFalg = false;
				startTime = System.currentTimeMillis() ;
				if ( (keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_F2
						|| keyCode == KeyEvent.KEYCODE_F3 || keyCode == KeyEvent.KEYCODE_F4 ||
						keyCode == KeyEvent.KEYCODE_F5)) {
//                Log.e("key ","inventory.... " ) ;
					//startTagRead();
					runInventory();
				}
				return ;
			}else if (keyDown){
				startTime = System.currentTimeMillis() ;
			}else {
				keyUpFalg = true;
			}

		}
	} ;
	void cleanBuf()
	{
		if(epcSet!=null)epcSet.clear();
		if(listEpc!=null)listEpc.clear();
		if(mapEpc!=null)mapEpc.clear();
		//adapter=null;
		if(lvEpc.getCount()>0) {
			try {

				if(adapter!=null) {
					adapter.notifyDataSetChanged();

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//scanThread=new Thread(inventoryTask);
		//runInventory();
	}

}
