/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.mixin.accessors;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(PotionBrewing.class)
public interface PotionBrewingAccess
{
	@Accessor("POTION_TYPE_CONVERSIONS")
	static List<PotionBrewing.MixPredicate<Potion>> getConversions()
	{
		throw new UnsupportedOperationException("Replaced by Mixin");
	}
}
