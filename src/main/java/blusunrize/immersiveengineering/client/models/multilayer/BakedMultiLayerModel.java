/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.multilayer;

import blusunrize.immersiveengineering.client.models.BakedIEModel;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BakedMultiLayerModel extends BakedIEModel
{
	private final Map<String, IBakedModel> models;
	private final IBakedModel model;

	public BakedMultiLayerModel(Map<String, IBakedModel> models)
	{
		this.models = models;
		String[] preferences = {
				RenderType.getSolid().name,
				RenderType.getCutout().name,
				RenderType.getCutoutMipped().name,
				RenderType.getTranslucent().name
		};
		for(String layer : preferences)
			if(models.containsKey(layer))
			{
				model = models.get(layer);
				return;
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
			for(IBakedModel model : models.values())
				ret.addAll(model.getQuads(state, side, rand));
			return ret.build();
		}
		else if(models.containsKey(current.name))
		{
			ImmutableList.Builder<BakedQuad> ret = new Builder<>();
			ret.addAll(models.get(current.name).getQuads(state, side, rand));
			return ret.build();
		}
		else
			return ImmutableList.of();
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return model.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d()
	{
		return model.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return model.isBuiltInRenderer();
	}

	@Nonnull
	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return model.getParticleTexture();
	}

	@Nonnull
	@Override
	public ItemOverrideList getOverrides()
	{
		return model.getOverrides();
	}

	@Override
	public IBakedModel handlePerspective(TransformType cameraTransformType, MatrixStack mat)
	{
		model.handlePerspective(cameraTransformType, mat);
		return this;
	}
}
