/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.mixin.accessors;

import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.PotionBrewing.MixPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MixPredicate.class)
public interface MixPredicateAccess
{
	@Accessor
	Ingredient getReagent();
}
