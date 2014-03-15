package com.tshevchuk.prayer;

import android.app.Application;

public class PrayerBookApplication extends Application {
	private static PrayerBookApplication instance;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
	}

	public static PrayerBookApplication getInstance() {
		return instance;
	}
}