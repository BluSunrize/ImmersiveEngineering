/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.shader.impl;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderLayer;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;

public class ShaderCaseShield extends ShaderCase
{
	public ShaderCaseShield(ShaderLayer... layers)
	{
		super(layers);
	}

	public ShaderCaseShield(Collection<ShaderLayer> layers)
	{
		super(layers);
	}

	@Override
	public ResourceLocation getShaderType()
	{
		return new ResourceLocation(ImmersiveEngineering.MODID, "shield");
	}

	@Override
	public int getLayerInsertionIndex()
	{
		return layers.length-1;
	}

	@Override
	public boolean shouldRenderGroupForPass(String modelPart, int pass)
	{
		if("flash".equals(modelPart)||"shock".equals(modelPart))
			return pass==getLayers().length-1;
		return true;
	}
}