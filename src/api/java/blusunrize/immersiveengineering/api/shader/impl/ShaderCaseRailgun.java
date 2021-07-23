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

public class ShaderCaseRailgun extends ShaderCase
{
	public ShaderCaseRailgun(ShaderLayer... layers)
	{
		super(layers);
	}

	public ShaderCaseRailgun(Collection<ShaderLayer> layers)
	{
		super(layers);
	}

	@Override
	public ResourceLocation getShaderType()
	{
		return new ResourceLocation(Lib.MODID, "railgun");
	}

	@Override
	public int getLayerInsertionIndex()
	{
		return layers.length-1;
	}

	@Override
	public boolean shouldRenderGroupForPass(String modelPart, int pass)
	{
		if("sled".equals(modelPart)||"wires".equals(modelPart)||"tubes".equals(modelPart))//these pieces only render on the uncoloured pass
			return pass==getLayers().length-1;

		if("grip".equals(modelPart))
			return pass==0;
		return pass!=0;

	}
}