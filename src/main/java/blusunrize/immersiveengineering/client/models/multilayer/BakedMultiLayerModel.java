/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.multilayer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.MinecraftForgeClient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class BakedMultiLayerModel implements IBakedModel
{
	private final Map<BlockRenderLayer, List<IBakedModel>> models;
	private final IBakedModel model;

	public BakedMultiLayerModel(Map<BlockRenderLayer, List<IBakedModel>> models)
	{
		this.models = models;
		BlockRenderLayer[] preferences = {BlockRenderLayer.SOLID, BlockRenderLayer.CUTOUT, BlockRenderLayer.CUTOUT_MIPPED,
				BlockRenderLayer.TRANSLUCENT};
		for(BlockRenderLayer layer : preferences)
			if(models.containsKey(layer))
			{
				List<IBakedModel> solidModels = models.get(layer);
				assert !solidModels.isEmpty();
				model = solidModels.get(0);
				return;
			}
		throw new IllegalArgumentException("Can't create multi layer model without any submodels");
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
	{
		BlockRenderLayer current = MinecraftForgeClient.getRenderLayer();
		if(current==null)
		{
			ImmutableList.Builder<BakedQuad> ret = new Builder<>();
			for(List<IBakedModel> forLayer : models.values())
				for(IBakedModel model : forLayer)
					ret.addAll(model.getQuads(state, side, rand));
			return ret.build();
		}
		else if(models.containsKey(current))
		{
			ImmutableList.Builder<BakedQuad> ret = new Builder<>();
			for(IBakedModel model : models.get(current))
				ret.addAll(model.getQuads(state, side, rand));
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
}
