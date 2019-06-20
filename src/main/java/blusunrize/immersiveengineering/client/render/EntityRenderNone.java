/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class EntityRenderNone<T extends Entity> extends Render<T>
{
	public EntityRenderNone(RenderManager renderManager)
	{
		super(renderManager);
	}

	@Override
	public void doRender(@Nonnull T entity, double x, double y, double z, float f0, float f1)
	{
	}

	@Override
	protected ResourceLocation getEntityTexture(@Nonnull T p_110775_1_)
	{
		return null;
	}

}
