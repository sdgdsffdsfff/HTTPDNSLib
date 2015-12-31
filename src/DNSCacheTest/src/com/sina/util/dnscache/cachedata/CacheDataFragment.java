package com.sina.util.dnscache.cachedata;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sina.util.dnscache.DNSCache;
import com.sina.util.dnscache.R;
import com.sina.util.dnscache.cache.IDnsCache;
import com.sina.util.dnscache.model.DomainModel;
import com.sina.util.dnscache.model.IpModel;
import com.sina.util.dnscache.simulationtask.TaskManager;
import com.sina.util.dnscache.simulationtask.TaskModel;
import com.sina.util.dnscache.util.HttpDnsRecordUtil;
import com.sina.util.dnscache.util.ToastUtil;

/**
 * Created by Doraemon on 2014/7/15.
 */
public class CacheDataFragment extends Fragment {

	public TextView task_list_start = null;
	public TextView http_dns_timer_start = null;
	public TextView memory_cache_data = null;
	public TextView domain_table_data = null;
	public TextView ip_table_data = null;
	public TextView httpdns_api_data = null ; 

	public ImageButton memory_cache_data_clear_Btn = null;
	public ImageButton db_cache_data_clear_Btn = null;
	public ImageButton clearBtn = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View contentView = inflater.inflate(R.layout.fragment_cache_data, null);

		clearBtn = (ImageButton) contentView.findViewById(R.id.clearBtn);
		clearBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				// ////////////////////////////////
				TaskManager.getInstance().stopTask();
				TaskManager.getInstance().clear();
				HttpDnsRecordUtil.removeFiles();
				// ThreadPool.ID_NUMBER = 0;
				ToastUtil.showText(CacheDataFragment.this.getActivity(),
						"数据清理完毕");
				
				DNSCache.getInstance().dnsManager.initDebugInfo();
				
				// ////////////////////////////////
			}
		});

		task_list_start = (TextView) contentView
				.findViewById(R.id.task_list_start);
		http_dns_timer_start = (TextView) contentView
				.findViewById(R.id.http_dns_timer_start);
		memory_cache_data = (TextView) contentView
				.findViewById(R.id.memory_cache_data);
		domain_table_data = (TextView) contentView
				.findViewById(R.id.domain_table_data);
		ip_table_data = (TextView) contentView.findViewById(R.id.ip_table_data);
		httpdns_api_data = (TextView) contentView.findViewById(R.id.httpdns_api_data);

		memory_cache_data_clear_Btn = (ImageButton) contentView
				.findViewById(R.id.memory_cache_data_clear_Btn);
		memory_cache_data_clear_Btn
				.setOnClickListener(new Button.OnClickListener() {
					public void onClick(View v) {
						DNSCache.getInstance().getDnsCacheManager().clearMemoryCache();
//						DNSCache.getInstance().clear();
						updataData();
					}
				});

		db_cache_data_clear_Btn = (ImageButton) contentView
				.findViewById(R.id.db_cache_data_clear_Btn);
		db_cache_data_clear_Btn
				.setOnClickListener(new Button.OnClickListener() {
					public void onClick(View v) {
						DNSCache.getInstance().getDnsCacheManager().clear();
//						DNSCache.getInstance().clear();
						updataData();
					}
				});

		startTimer();
		return contentView;
	}

	@Override
	public void onResume() {
		super.onResume();

		updataData();
	}

	public void updataData() {

		// 测试任务
		ArrayList<TaskModel> taskList = TaskManager.getInstance().list;
		if (taskList != null) {
			task_list_start.setText("任务总数量：" + taskList.size() + "\n");
		}else{
			task_list_start.setText("任务总数量：" + 0 + "\n");
		}
		
		// httpdns后台任务
		String timerInfoStr = "任务更新间隔:"
				+ (DNSCache.getInstance().sleepTime / 1000) + "秒\n";
		timerInfoStr += "任务启动时间倒计时:"
				+ DNSCache.getInstance().getTimerDelayedStartTime() + "\n";
		http_dns_timer_start.setText(timerInfoStr);

		// IDnsCache 缓存接口
		IDnsCache dnsCache = DNSCache.getInstance().getDnsCacheManager();
		// 内存缓存层数据提取
		ArrayList<DomainModel> memoryCacheList = dnsCache.getAllMemoryCache();
		String memoryCacheStr = "";
		for (DomainModel model : memoryCacheList) {
			memoryCacheStr += model.toString();
		}
		memory_cache_data.setText(memoryCacheStr);
		// domain表数据提取
		ArrayList<DomainModel> tableDomainList = dnsCache.getAllTableDomain();
		String tableDomainStr = "";
		for (DomainModel model : tableDomainList) {
			tableDomainStr += model.toString();
		}
		domain_table_data.setText(tableDomainStr);
		ArrayList<IpModel> tableIpList = dnsCache.getTableIP();
		String tableIpStr = "";
		for (IpModel model : tableIpList) {
			tableIpStr += model.toString();
		}
		ip_table_data.setText(tableIpStr);
		
		
		ArrayList<String> httpdnsDatalist = DNSCache.getInstance().dnsManager.getDebugInfo() ;
		String res = "" ; 
		for( String str : httpdnsDatalist ){
			res += "*\n" + str + "\n\n" ;
		}
		httpdns_api_data.setText(res);
		
	}

	public final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0: // 刷新业务
				updataData();
				break;
			}
		}
	};

	// ///////////////////////////////////////////////////////////////////////////////////

	/**
	 * 定时器休眠时间
	 */
	private final int sleepTime = 1 * 1000;

	/**
	 * 启动定时器
	 */
	private void startTimer() {
		timer = new Timer();
		timer.schedule(task, 0, sleepTime);
	}

	/**
	 * 定时器Obj
	 */
	private Timer timer = null;

	/**
	 * 定时器任务
	 */
	private TimerTask task = new TimerTask() {
		@Override
		public void run() {
			Message message = new Message();
			message.what = 0;
			handler.sendMessage(message);
		}
	};

	// ///////////////////////////////////////////////////////////////////////////////////
}
