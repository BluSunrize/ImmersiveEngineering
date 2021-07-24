/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.common.entities.IEMinecartEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MinecartRenderer;

public class IEMinecartRenderer extends MinecartRenderer<IEMinecartEntity>
{
	public IEMinecartRenderer(Context renderManagerIn)
	{
		//TODO net.minecraftforge.fmlclient.registry.RenderingRegistry#registerLayerDefinition
		super(renderManagerIn);
	}
}