/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.multilayer;

import blusunrize.immersiveengineering.client.models.BakedIEModel;
import blusunrize.immersiveengineering.mixin.accessors.client.RenderTypeAccess;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BakedMultiLayerModel extends BakedIEModel
{
	private final Map<String, BakedModel> models;
	private final BakedModel model;

	public BakedMultiLayerModel(Map<String, BakedModel> models)
	{
		this.models = models;
		RenderType[] preferences = {
				RenderType.solid(),
				RenderType.cutout(),
				RenderType.cutoutMipped(),
				RenderType.translucent(),
		};
		for(RenderType layer : preferences)
		{
			String name = ((RenderTypeAccess)layer).getName();
			if(models.containsKey(name))
			{
				model = models.get(name);
				return;
			}
		}
		throw new IllegalArgumentException("Can't create multi layer model without any submodels");
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData)
	{
		RenderType current = MinecraftForgeClient.getRenderLayer();
		if(current==null)
		{
			ImmutableList.Builder<BakedQuad> ret = new Builder<>();
			for(BakedModel model : models.values())
				ret.addAll(model.getQuads(state, side, rand));
			return ret.build();
		}
		String name = ((RenderTypeAccess)current).getName();
		if(models.containsKey(name))
		{
			ImmutableList.Builder<BakedQuad> ret = new Builder<>();
			ret.addAll(models.get(name).getQuads(state, side, rand, EmptyModelData.INSTANCE));
			return ret.build();
		}
		else
			return ImmutableList.of();
	}

	@Override
	public boolean useAmbientOcclusion()
	{
		return model.useAmbientOcclusion();
	}

	@Override
	public boolean isGui3d()
	{
		return model.isGui3d();
	}

	@Override
	public boolean isCustomRenderer()
	{
		return model.isCustomRenderer();
	}

	@Nonnull
	@Override
	public TextureAtlasSprite getParticleIcon()
	{
		return model.getParticleIcon();
	}

	@Nonnull
	@Override
	public ItemOverrides getOverrides()
	{
		return model.getOverrides();
	}

	@Override
	public BakedModel handlePerspective(TransformType cameraTransformType, PoseStack mat)
	{
		model.handlePerspective(cameraTransformType, mat);
		return this;
	}
}
