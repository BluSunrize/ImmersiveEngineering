/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.shader;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface IShaderItem
{
	ShaderCase getShaderCase(ItemStack shader, ItemStack item, ResourceLocation shaderType);

	ResourceLocation getShaderName(ItemStack shader);
}