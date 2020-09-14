/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class CompositeBakedModel<T extends IBakedModel> implements IBakedModel
{
	protected final T base;

	public CompositeBakedModel(T base)
	{
		this.base = base;
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand)
	{
		return base.getQuads(state, side, rand, EmptyModelData.INSTANCE);
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return base.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d()
	{
		return base.isGui3d();
	}

	@Override
	public boolean func_230044_c_()
	{
		return base.func_230044_c_();
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return base.isBuiltInRenderer();
	}

	@Nonnull
	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return base.getParticleTexture(EmptyModelData.INSTANCE);
	}

	@Nonnull
	@Override
	public ItemOverrideList getOverrides()
	{
		return base.getOverrides();
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData)
	{
		return base.getQuads(state, side, rand, extraData);
	}

	@Override
	public boolean isAmbientOcclusion(BlockState state)
	{
		return base.isAmbientOcclusion(state);
	}

	@Nonnull
	@Override
	public IModelData getModelData(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData)
	{
		return base.getModelData(world, pos, state, tileData);
	}

	@Override
	public TextureAtlasSprite getParticleTexture(@Nonnull IModelData data)
	{
		return base.getParticleTexture(data);
	}
}
