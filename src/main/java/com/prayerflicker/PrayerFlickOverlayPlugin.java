/*
 * Copyright (c) 2025, slategray <https://github.com/slategraydev>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:

 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
*/

package com.prayerflicker;

/*
 * References:
 * - honeyhoney for the AttackStyles plugin.
 * - Jos <Malevolentdev@gmail.com> and Rheon <https://github.com/Rheon-D>
 *   for the StatusBars plugin.
 */

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Prayer;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.game.SpriteManager;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.time.Duration;
import java.awt.Color;
import lombok.Getter;
import lombok.AccessLevel;
import net.runelite.api.Constants;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.client.events.ConfigChanged;
import net.runelite.api.Actor;
import net.runelite.api.HitsplatID;
import net.runelite.api.Varbits;

@PluginDescriptor(
	name = "Prayer Flick Overlay"
)
public class PrayerFlickOverlayPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PrayerFlickOverlay overlay;

	@Inject
	private PrayerFlickOverlayConfig config;

	private Instant startOfLastTick = Instant.now();

	@Getter(AccessLevel.PACKAGE)
	private boolean prayersActive = false;

	@Getter(AccessLevel.PACKAGE)
	private BufferedImage prayerIcon;

	@Getter(AccessLevel.PACKAGE)
	private BufferedImage prayerIconActive;

	@Getter(AccessLevel.PACKAGE)
	private long tickCount = 0;

	@Getter(AccessLevel.PACKAGE)
	private Instant lastCombatTime;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		spriteManager.getSpriteAsync(1068, 0, icon -> prayerIcon = icon);
		spriteManager.getSpriteAsync(1058, 0, icon -> prayerIconActive = icon);
		lastCombatTime = Instant.now();
		overlay.resetBounds();
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		startOfLastTick = Instant.now();
		prayersActive = isAnyPrayerActive();
		tickCount = client.getTickCount();

		Actor interacting = client.getLocalPlayer().getInteracting();
		if (interacting != null && interacting.getCombatLevel() > 0)
		{
			lastCombatTime = Instant.now();
		}
	}

	@Subscribe
	public void onActorDeath(ActorDeath actorDeath)
	{
		if (actorDeath.getActor() == client.getLocalPlayer().getInteracting())
		{
			lastCombatTime = Instant.now().minusSeconds(3);
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals(PrayerFlickOverlayConfig.GROUP))
		{
			if (event.getKey().equals("barWidth") || event.getKey().equals("barHeight"))
			{
				overlay.setPreferredSize(new java.awt.Dimension(config.barWidth(), config.barHeight()));
				overlay.resetBounds();
			}
		}
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied)
	{
		if (hitsplatApplied.getActor() == client.getLocalPlayer())
		{
			int hitsplatType = hitsplatApplied.getHitsplat().getHitsplatType();
			switch (hitsplatType)
			{
				case HitsplatID.DAMAGE_ME:
				case HitsplatID.BLOCK_ME:
					lastCombatTime = Instant.now();
					break;
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			startOfLastTick = Instant.now();
			lastCombatTime = Instant.now();
		}
	}

	private boolean isAnyPrayerActive()
	{
		for (Prayer prayer : Prayer.values())
		{
			if (client.isPrayerActive(prayer))
			{
				return true;
			}
		}
		return false;
	}

	private boolean isQuickPrayerActive()
	{
		return client.getVarbitValue(Varbits.QUICK_PRAYER) == 1;
	}

	Color getPrayerColor()
	{
		if (config.prayerMode() == PrayerFlickOverlayConfig.PrayerMode.QUICKPRAYER)
		{
			return config.quickPrayerModeColor();
		}

		return prayersActive ? config.activePrayerColor() : config.inactivePrayerColor();
	}

	BufferedImage getPrayerIcon()
	{
		if (config.prayerIcon() == PrayerFlickOverlayConfig.IconPosition.OFF)
		{
			return null;
		}

		if (config.prayerMode() == PrayerFlickOverlayConfig.PrayerMode.QUICKPRAYER && isQuickPrayerActive())
		{
			return prayerIconActive;
		}

		return prayerIcon;
	}

	long getTimeSinceLastTick()
	{
		long timeSinceLastTick = Duration.between(startOfLastTick, Instant.now()).toMillis();
		return Math.min(timeSinceLastTick, Constants.GAME_TICK_LENGTH - 1);
	}

	@Provides
	PrayerFlickOverlayConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PrayerFlickOverlayConfig.class);
	}
}

