package com.prayerflicker;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PrayerFlickOverlayPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(PrayerFlickOverlayPlugin.class);
		RuneLite.main(args);
	}
} 