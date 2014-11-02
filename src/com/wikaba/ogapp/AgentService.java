package com.wikaba.ogapp;

import java.io.FileDescriptor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wikaba.ogapp.agent.FleetEvent;
import com.wikaba.ogapp.agent.OgameAgent;
import com.wikaba.ogapp.utils.AccountCredentials;
import com.wikaba.ogapp.utils.DatabaseManager;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

public class AgentService extends Service {
	static final String LOGTAG = "AgentService";
	
	private IBinder mBinder;
	private LongSparseArray<OgameAgent> ogameSessions;
	private DatabaseManager dbman;
	
	public AgentService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		if(mBinder == null) {
			mBinder = new AgentServiceBinder();
		}
		
		if(ogameSessions == null) {
			ogameSessions = new LongSparseArray<OgameAgent>();
		}
		
		if(dbman == null) {
			dbman = new DatabaseManager(this);
		}
		
		return mBinder;
	}
	
	/**
	 * Logs in to the specified account (from database) using a software agent for Ogame. Hold on to the rowId variable,
	 * because it will be used in all other methods of this service as a key to get the Ogame agent instance.
	 * @param rowId - account's row ID in database
	 * @return true if acquiring session cookies (logging in) for the account completed successfully. false otherwise.
	 */
	public boolean loginToAccount(long rowId) {
		OgameAgent agent = ogameSessions.get(rowId);
		if(agent == null) {
			agent = new OgameAgent();
			AccountCredentials creds = dbman.getAccount(rowId);
			agent.login(creds.universe, creds.username, creds.passwd);
			ogameSessions.put(rowId, agent);
		}
		return true;
	}
	
	/**
	 * Returns the fleet events parsed from the overview event screen.
	 * 
	 * Pre-condition: Method loginToAccount has been called with rowId passed in as the parameter.
	 * 
	 * @param rowId - account's row ID in database, used as key to retrieve Ogame agent instance.
	 * @return list of fleet events from overview screen. Returns null on error.
	 */
	public List<FleetEvent> getFleetEvents(long rowId) {
		OgameAgent agent = ogameSessions.get(rowId);
		if(agent == null) {
			Log.e(LOGTAG, "Please call loginToAccount before calling getFleetEvents");
			return null;
		}
		
		return agent.getOverviewData();
	}
	
	public class AgentServiceBinder extends Binder{
		public AgentService getService() {
			return AgentService.this;
		}
	}
}