/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nonnull;

//Used for the skyline hook
public class NoneRenderer<T extends Entity> extends EntityRenderer<T>
{
	public NoneRenderer(Context renderManager)
	{
		super(renderManager);
	}

	@Override
	public void render(T entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn)
	{
	}

	@Override
	public ResourceLocation getTextureLocation(@Nonnull T entity)
	{
		return null;
	}

}
