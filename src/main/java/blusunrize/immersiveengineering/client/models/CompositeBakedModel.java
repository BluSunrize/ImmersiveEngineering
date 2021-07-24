/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class CompositeBakedModel<T extends BakedModel> implements BakedModel
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
	public boolean useAmbientOcclusion()
	{
		return base.useAmbientOcclusion();
	}

	@Override
	public boolean isGui3d()
	{
		return base.isGui3d();
	}

	@Override
	public boolean usesBlockLight()
	{
		return base.usesBlockLight();
	}

	@Override
	public boolean isCustomRenderer()
	{
		return base.isCustomRenderer();
	}

	@Nonnull
	@Override
	public TextureAtlasSprite getParticleIcon()
	{
		return base.getParticleIcon(EmptyModelData.INSTANCE);
	}

	@Nonnull
	@Override
	public ItemOverrides getOverrides()
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
	public IModelData getModelData(@Nonnull BlockAndTintGetter world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData)
	{
		return base.getModelData(world, pos, state, tileData);
	}

	@Override
	public TextureAtlasSprite getParticleIcon(@Nonnull IModelData data)
	{
		return base.getParticleIcon(data);
	}
}
