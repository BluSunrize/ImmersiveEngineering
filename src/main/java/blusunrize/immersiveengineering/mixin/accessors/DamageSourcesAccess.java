/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.mixin.accessors;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nullable;

// TODO this will probably be made redundant by Forge?
// TODO apparently it is not possible to @Invoker multiple overloads of the same name?
@Mixin(DamageSources.class)
public interface DamageSourcesAccess
{
	@Invoker("source")
	DamageSource source1(ResourceKey<DamageType> p_270957_);

	@Invoker("source")
	DamageSource source2(ResourceKey<DamageType> p_270142_, @Nullable Entity p_270696_);

	@Invoker("source")
	DamageSource source3(ResourceKey<DamageType> p_270076_, @Nullable Entity p_270656_, @Nullable Entity p_270242_);
}
