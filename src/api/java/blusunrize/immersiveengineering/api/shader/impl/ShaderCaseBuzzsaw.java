/*
 * BluSunrize
 * Copyright (c) 2020
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

public class ShaderCaseBuzzsaw extends ShaderCase
{
	private int bladeLayers = 1;

	public ShaderCaseBuzzsaw(ShaderLayer... layers)
	{
		super(layers);
	}

	public ShaderCaseBuzzsaw(Collection<ShaderLayer> layers)
	{
		super(layers);
	}

	@Override
	public ResourceLocation getShaderType()
	{
		return new ResourceLocation(Lib.MODID, "buzzsaw");
	}

	@Override
	public int getLayerInsertionIndex()
	{
		return layers.length-1;
	}

	@Override
	public boolean shouldRenderGroupForPass(String modelPart, int pass)
	{
		if("blade".equals(modelPart)||"upgrade_blades1".equals(modelPart)||"upgrade_blades2".equals(modelPart))
			return pass >= getLayers().length-bladeLayers;
		if(pass >= getLayers().length-bladeLayers)//Last pass on the buzzsaw is for the blade
			return false;
		if("upgrade_lube".equals(modelPart))//Upgrades only render on the uncoloured pass
			return pass==getLayers().length-2;

		if("grip".equals(modelPart))
			return pass==0;
		return pass!=0;

	}

	public ShaderCaseBuzzsaw addHeadLayers(ShaderLayer... addedLayers)
	{
		addLayers(layers.length, addedLayers);
		bladeLayers += addedLayers.length;
		return this;
	}
}