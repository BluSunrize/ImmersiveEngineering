/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.shader;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author BluSunrize - 14.05.2018
 */
@FunctionalInterface
public interface IShaderEffectFunction
{
	void execute(@Nonnull Level world, @Nonnull ItemStack shader, @Nullable ItemStack item, @Nonnull String shaderType, @Nonnull Vec3 pos, @Nullable Vec3 direction, float scale);
}
