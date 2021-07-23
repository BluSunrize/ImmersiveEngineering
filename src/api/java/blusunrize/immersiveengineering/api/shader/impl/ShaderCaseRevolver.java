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

public class ShaderCaseRevolver extends ShaderCase
{
	public ShaderCaseRevolver(ShaderLayer... layers)
	{
		super(layers);
	}

	public ShaderCaseRevolver(Collection<ShaderLayer> layers)
	{
		super(layers);
	}

	@Override
	public ResourceLocation getShaderType()
	{
		return new ResourceLocation(Lib.MODID, "revolver");
	}

	@Override
	public int getLayerInsertionIndex()
	{
		return layers.length-1;
	}

	@Override
	public boolean shouldRenderGroupForPass(String modelPart, int pass)
	{
		if(pass==0)//first pass is just for the grip
			return "frame".equals(modelPart)||"bayonet_attachment".equals(modelPart);
		if(pass==2)//third pass is just for the blade of the bayonet
			return "player_bayonet".equals(modelPart)||"dev_bayonet".equals(modelPart);
		return true;
	}
}