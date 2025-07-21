/*
BSD 2-Clause License

Copyright (c) 2025, slategray
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
*/

package com.prayerflicker;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(PrayerFlickOverlayConfig.GROUP)
public interface PrayerFlickOverlayConfig extends Config
{
    String GROUP = "prayerflick";

    enum DisplayOrientation { HORIZONTAL, VERTICAL }
    enum MovementStyle { SINE, LINEAR, EASE_IN_OUT, EASE_OUT_IN, PING_PONG }
    enum IconPosition { LEFT, MIDDLE, RIGHT, OFF }
    enum ColorMode { SINGLE, ACTIVE }

    @ConfigSection(
            name = "Settings",
            description = "General behavior and layout settings.",
            position = 0
    )
    String settingsSection = "settings";

    @ConfigSection(
            name = "Styling",
            description = "Color, size, and other visual settings.",
            position = 1
    )
    String stylingSection = "styling";

    @ConfigItem(
            position = 0,
            keyName = "displayOrientation",
            name = "Orientation",
            description = "Display the bar horizontally or vertically.",
            section = settingsSection
    )
    default DisplayOrientation displayOrientation() { return DisplayOrientation.HORIZONTAL; }

    @ConfigItem(
            position = 1,
            keyName = "movementStyle",
            name = "Visual Style",
            description = "The animation style of the tick bar.",
            section = settingsSection
    )
    default MovementStyle movementStyle() { return MovementStyle.SINE; }

    @ConfigItem(
            position = 2,
            keyName = "reverseDirection",
            name = "Reverse Direction",
            description = "Reverses the direction of the tick bar.",
            section = settingsSection
    )
    default boolean reverseDirection() { return false; }

    @ConfigItem(
            position = 3,
            keyName = "prayerIcon",
            name = "Prayer Icon",
            description = "Position of the prayer skill icon on the bar.",
            section = settingsSection
    )
    default IconPosition prayerIcon() { return IconPosition.LEFT; }

    @ConfigItem(
            position = 4,
            keyName = "fadeAfterCombat",
            name = "Fade After Combat",
            description = "Fade the overlay out when out of combat.",
            section = settingsSection
    )
    default boolean fadeAfterCombat() { return false; }

    @Alpha
    @ConfigItem(
            position = 0,
            keyName = "tickBarColor",
            name = "Tick Bar Color",
            description = "Color of the moving tick bar.",
            section = stylingSection
    )
    default Color tickBarColor() { return new Color(200, 200, 200, 200); }

    @ConfigItem(
            position = 1,
            keyName = "tickBarThickness",
            name = "Tick Bar Thickness",
            description = "Thickness of the moving tick bar.",
            section = stylingSection
    )
    default int tickBarThickness() { return 15; }

    @ConfigItem(
            position = 2,
            keyName = "colorMode",
            name = "Bar Color Mode",
            description = "Choose between a single static color or active/inactive colors.",
            section = stylingSection
    )
    default ColorMode colorMode() { return ColorMode.SINGLE; }

    @Alpha
    @ConfigItem(
            position = 3,
            keyName = "singleColor",
            name = "Single Bar Color",
            description = "Color of the bar when in single color mode.",
            section = stylingSection
    )
    default Color singleColor() { return new Color(60, 15, 100, 200); }

    @Alpha
    @ConfigItem(
            position = 4,
            keyName = "inactivePrayerColor",
            name = "Inactive Prayer Color",
            description = "Color of the bar when prayer is inactive.",
            section = stylingSection
    )
    default Color inactivePrayerColor() { return new Color(50, 200, 200, 175); }

    @Alpha
    @ConfigItem(
            position = 5,
            keyName = "activePrayerColor",
            name = "Active Prayer Color",
            description = "Color of the bar when prayer is active.",
            section = stylingSection
    )
    default Color activePrayerColor() { return new Color(57, 255, 186, 225); }

    @ConfigItem(
            position = 6,
            keyName = "barWidth",
            name = "Bar Width",
            description = "Width of the prayer bar.",
            section = stylingSection
    )
    default int barWidth() { return 200; }

    @ConfigItem(
            position = 7,
            keyName = "barHeight",
            name = "Bar Height",
            description = "Height of the prayer bar.",
            section = stylingSection
    )
    default int barHeight() { return 20; }

    @Range(max = 5)
    @ConfigItem(
            position = 8,
            keyName = "borderThickness",
            name = "Border Thickness",
            description = "Thickness of the border.",
            section = stylingSection
    )
    default int borderThickness() { return 1; }
} 