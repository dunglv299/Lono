package com.teusoft.lono.dao;

import android.content.Context;
import com.teusoft.lono.utils.MyConstants;
import com.teusoft.lono.dao.DaoMaster.DevOpenHelper;

/**
 * Helper to manage database
 * 
 */
public class MyDatabaseHelper {

	private DevOpenHelper mDevOpenHelper;
	private DaoMaster mDaoMaster;
	private DaoSession mSession;

	private static MyDatabaseHelper myDatabaseHelper;

	private MyDatabaseHelper(Context context) {
		mDevOpenHelper = new DevOpenHelper(context.getApplicationContext(),
				MyConstants.DATABASE_NAME, null);
		mDaoMaster = new DaoMaster(mDevOpenHelper.getWritableDatabase());
		mSession = mDaoMaster.newSession();
	}

	public static MyDatabaseHelper getInstance(Context context) {
		if (myDatabaseHelper == null) {
			myDatabaseHelper = new MyDatabaseHelper(context);
		}
		return myDatabaseHelper;
	}

	/**
	 * @return the mSession
	 */
	public DaoSession getmSession() {
		return mSession;
	}

	// public DevOpenHelper getmDevOpenHelper() {
	// return mDevOpenHelper;
	// }
	//
	// public void setmDevOpenHelper(DevOpenHelper mDevOpenHelper) {
	// this.mDevOpenHelper = mDevOpenHelper;
	// }
	//
	public DaoMaster getmDaoMaster() {
		return mDaoMaster;
	}

	//
	// public void setmDaoMaster(DaoMaster mDaoMaster) {
	// this.mDaoMaster = mDaoMaster;
	// }

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		myDatabaseHelper = null;
		mDaoMaster = null;
		mDevOpenHelper.close();
		mDevOpenHelper = null;

	}

}
