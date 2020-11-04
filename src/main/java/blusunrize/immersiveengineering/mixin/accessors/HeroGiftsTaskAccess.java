/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.mixin.accessors;

import net.minecraft.entity.ai.brain.task.GiveHeroGiftsTask;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(GiveHeroGiftsTask.class)
public interface HeroGiftsTaskAccess
{
	@Accessor("GIFTS")
	static Map<VillagerProfession, ResourceLocation> getGifts()
	{
		throw new UnsupportedOperationException("Replaced by Mixin");
	}
}
