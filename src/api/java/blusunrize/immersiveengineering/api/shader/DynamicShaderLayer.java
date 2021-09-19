/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.shader;

import net.minecraft.resources.ResourceLocation;

public class DynamicShaderLayer extends ShaderLayer
{
	public DynamicShaderLayer(ResourceLocation texture, int colour)
	{
		super(texture, colour);
	}

	@Override
	public boolean isDynamicLayer()
	{
		return true;
	}
}
