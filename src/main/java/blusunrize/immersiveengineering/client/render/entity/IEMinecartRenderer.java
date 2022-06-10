/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.common.entities.IEMinecartEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MinecartRenderer;

public class IEMinecartRenderer<T extends IEMinecartEntity<?>> extends MinecartRenderer<T>
{
	public IEMinecartRenderer(Context renderManagerIn, ModelLayerLocation layer)
	{
		super(renderManagerIn, layer);
	}

	public static <T extends IEMinecartEntity<?>>
	EntityRendererProvider<T> provide(ModelLayerLocation layer)
	{
		return ctx -> new IEMinecartRenderer<>(ctx, layer);
	}
}