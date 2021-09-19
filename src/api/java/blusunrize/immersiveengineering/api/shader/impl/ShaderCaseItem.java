/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.shader.impl;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderLayer;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

public class ShaderCaseItem extends ShaderCase
{
	public ShaderCaseItem(ShaderLayer... layers)
	{
		super(layers);
	}

	public ShaderCaseItem(Collection<ShaderLayer> layers)
	{
		super(layers);
	}

	@Override
	public ResourceLocation getShaderType()
	{
		return new ResourceLocation(Lib.MODID, "item");
	}

	@Override
	public int getLayerInsertionIndex()
	{
		return layers.length-1;
	}

	@Override
	public boolean shouldRenderGroupForPass(String modelPart, int pass)
	{
		return true;
	}
}