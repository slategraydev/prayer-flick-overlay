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

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import java.time.Duration;
import java.time.Instant;
import net.runelite.client.ui.overlay.OverlayPosition;

public class PrayerFlickOverlay extends Overlay
{
    private final PrayerFlickOverlayConfig config;
    private final PrayerFlickOverlayPlugin plugin;
    private final BarRenderer barRenderer;

    @Inject
    private PrayerFlickOverlay(PrayerFlickOverlayConfig config, PrayerFlickOverlayPlugin plugin)
    {
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        this.config = config;
        this.plugin = plugin;
        this.barRenderer = new BarRenderer();
        setMovable(true);
        /*
         * setResizable(true);
         * The resized overlay is "stuck" at that size on every login
         * Tried forcing a different size on initialization, but it's not working
         * Keeping it off for now
         */
        setPosition(OverlayPosition.BOTTOM_LEFT);
    }

    public void resetBounds()
    {
        getBounds().setSize(config.barWidth(), config.barHeight());
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        float overlayOpacity = 1.0f;
        if (config.fadeAfterCombat())
        {
            long timeSinceCombat = Duration.between(plugin.getLastCombatTime(), Instant.now()).toMillis();
            // I should expose this as config options
            long combatGracePeriod = 8000;
            long fadeDuration = 3000;

            if (timeSinceCombat < combatGracePeriod)
            {
                overlayOpacity = 1.0f;
            }
            else if (timeSinceCombat > combatGracePeriod + fadeDuration)
            {
                overlayOpacity = 0.0f;
            }
            else
            {
                long timeIntoFade = timeSinceCombat - combatGracePeriod;
                overlayOpacity = 1.0f - ((float) timeIntoFade / fadeDuration);
            }
        }
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, overlayOpacity));

        int logicalWidth = (getBounds().width > 0) ? getBounds().width : config.barWidth();
        int logicalHeight = (getBounds().height > 0) ? getBounds().height : config.barHeight();

        if (config.displayOrientation() == PrayerFlickOverlayConfig.DisplayOrientation.VERTICAL)
        {
            graphics.translate(0, logicalHeight);
            graphics.rotate(-Math.PI / 2);
        }

        barRenderer.render(graphics, config, plugin, logicalWidth, logicalHeight);
        return new Dimension(logicalWidth, logicalHeight);
    }
} 