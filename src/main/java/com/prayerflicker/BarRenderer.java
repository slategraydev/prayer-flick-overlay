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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import net.runelite.api.Constants;

class BarRenderer
{
    private static final Color BACKGROUND = new Color(0, 0, 0, 150);

    void render(Graphics2D graphics, PrayerFlickOverlayConfig config, PrayerFlickOverlayPlugin plugin, int barWidth, int barHeight)
    {
        int tickBarThickness = config.tickBarThickness();
        int borderThickness = config.borderThickness();

        graphics.setColor(BACKGROUND);
        graphics.fillRect(0, 0, barWidth, barHeight);

        graphics.setColor(plugin.getPrayerColor());
        graphics.fillRect(borderThickness, borderThickness, barWidth - (borderThickness * 2), barHeight - (borderThickness * 2));

        BufferedImage icon = plugin.getPrayerIcon();
        if (icon != null)
        {
            double scale = (double) (barHeight - (borderThickness * 2)) / icon.getHeight();
            int newWidth = (int) (icon.getWidth() * scale);
            int newHeight = (int) (icon.getHeight() * scale);
            int iconY = borderThickness + ((barHeight - (borderThickness * 2) - newHeight) / 2);

            int iconX = 0;
            switch (config.prayerIcon())
            {
                case LEFT:
                    iconX = borderThickness + 1;
                    break;
                case MIDDLE:
                    iconX = (barWidth - newWidth) / 2;
                    break;
                case RIGHT:
                    iconX = barWidth - newWidth - borderThickness - 1;
                    break;
            }
            graphics.drawImage(icon, iconX, iconY, newWidth, newHeight, null);
        }

        graphics.setColor(config.tickBarColor());
        long timeSinceLastTick = plugin.getTimeSinceLastTick();

        if (config.movementStyle() == PrayerFlickOverlayConfig.MovementStyle.PING_PONG)
        {
            int travelWidth = barWidth - (borderThickness * 2) - tickBarThickness;

            // Triangle wave for smooth ping-pong-ing
            double intraTickProgress = (double) timeSinceLastTick / (Constants.GAME_TICK_LENGTH - 1);
            double totalProgress = (plugin.getTickCount() % 2 + intraTickProgress) / 2.0;
            double pingPongProgress = 1.0 - Math.abs(totalProgress * 2 - 1.0);

            double xOffset = pingPongProgress * travelWidth;

            if (config.reverseDirection())
            {
                xOffset = travelWidth - xOffset;
            }

            graphics.fill(new Rectangle2D.Double(borderThickness + xOffset, borderThickness, tickBarThickness, barHeight - (borderThickness * 2)));
        }
        else
        {
            // Adding a slide-through effect for all other styles
            // Helps with the jumpy tick bar
            Graphics2D g2d = (Graphics2D) graphics.create();

            int innerBarX = borderThickness;
            int innerBarY = borderThickness;
            int innerBarHeight = barHeight - (borderThickness * 2);
            int innerBarWidth = barWidth - (borderThickness * 2);

            g2d.clipRect(innerBarX, innerBarY, innerBarWidth, innerBarHeight);
            g2d.setComposite(graphics.getComposite());

            double intraTickProgress = (double) timeSinceLastTick / (Constants.GAME_TICK_LENGTH - 1);
            int travelWidth = innerBarWidth + tickBarThickness;
            double progress = 0;

            switch (config.movementStyle())
            {
                case LINEAR:
                    progress = intraTickProgress;
                    break;
                case SINE:
                    progress = (1.0 - Math.cos(intraTickProgress * Math.PI)) / 2.0;
                    break;
                case EASE_IN_OUT:
                    progress = intraTickProgress * intraTickProgress * (3 - 2 * intraTickProgress);
                    break;
                case EASE_OUT_IN:
                    double fastSlowFastProgress = 0.5 * (1 - Math.cos(intraTickProgress * Math.PI));
                    progress = 0.5 * (Math.sin((fastSlowFastProgress * Math.PI) - (Math.PI / 2))) + 0.5;
                    break;
                default:
                    break;
            }

            int xOffset = (int) Math.round(progress * travelWidth);

            if (config.reverseDirection())
            {
                xOffset = travelWidth - xOffset;
            }

            g2d.fillRect(innerBarX - tickBarThickness + xOffset, innerBarY, tickBarThickness, innerBarHeight);
            g2d.dispose();
        }
    }
} 