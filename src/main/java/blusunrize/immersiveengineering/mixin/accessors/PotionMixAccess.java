/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.mixin.accessors;

import net.minecraft.core.Holder;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net/minecraft/world/item/alchemy/PotionBrewing$Mix")
// TODO why was the AT to Mix removed in NeoForge?
public interface PotionMixAccess<T>
{
	@Accessor
	Holder<T> getFrom();

	@Accessor
	Ingredient getIngredient();

	@Accessor
	Holder<T> getTo();
}
