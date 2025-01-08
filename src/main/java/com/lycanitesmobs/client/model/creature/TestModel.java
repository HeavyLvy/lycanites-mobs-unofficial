package com.lycanitesmobs.client.model.creature;


import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateQuadruped;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TestModel extends ModelTemplateQuadruped {
    public TestModel() {
        this(1.0F);
    }

    public TestModel(float shadowSize) {
        this.initModel("test", LycanitesMobs.modInfo, "entity/test");
        this.bigChildHead = true;
    }
}
