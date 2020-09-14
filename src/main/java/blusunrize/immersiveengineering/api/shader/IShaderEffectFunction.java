/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.shader;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author BluSunrize - 14.05.2018
 */
@FunctionalInterface
public interface IShaderEffectFunction
{
	void execute(@Nonnull World world, @Nonnull ItemStack shader, @Nullable ItemStack item, @Nonnull String shaderType, @Nonnull Vector3d pos, @Nullable Vector3d direction, float scale);
}
