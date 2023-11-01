/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.mixin.accessors;

import net.minecraft.world.item.alchemy.PotionBrewing.Mix;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Mix.class)
public interface PotionMixAccessor
{
	// TODO this is ATd by Forge, but the patch to the same file seems to break the AT?
	@Accessor
	Ingredient getIngredient();
}
