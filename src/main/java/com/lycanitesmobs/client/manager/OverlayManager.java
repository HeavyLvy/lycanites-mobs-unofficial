package com.lycanitesmobs.client.manager;

import com.lycanitesmobs.client.util.helpers.DrawHelper;
import net.minecraft.client.Minecraft;


public class OverlayManager {
    public static OverlayManager instance = new OverlayManager();
    public DrawHelper drawHelper;
    public Minecraft minecraft;

    public OverlayManager() {
        this.minecraft = Minecraft.getInstance();
        this.drawHelper = new DrawHelper(minecraft, minecraft.font);
    }

    public static OverlayManager getInstance() {
        return instance;
    }

}
