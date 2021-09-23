/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj;

import blusunrize.immersiveengineering.api.client.ICacheKeyProvider;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.client.models.obj.SpecificIEOBJModel.ShadedQuads;
import blusunrize.immersiveengineering.client.models.obj.callback.IEOBJCallback;
import blusunrize.immersiveengineering.client.models.obj.callback.ItemCallback;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.ModelGroup;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class GeneralIEOBJModel<T> implements ICacheKeyProvider<String>
{
	private final Cache<Triple<T, ShaderCase, String>, List<ShadedQuads>> groupCache = CacheBuilder.newBuilder()
			.maximumSize(100)
			.build();
	private final IEOBJCallback<T> callback;
	//TODO copied from old IEOBJ
	private final OBJModel baseModel;
	private final BakedModel baseBaked;
	private final IModelConfiguration owner;
	private final Function<Material, TextureAtlasSprite> spriteGetter;
	private final ModelState sprite;
	private final boolean isDynamic;
	private final ItemOverrides overrides;

	public GeneralIEOBJModel(IEOBJCallback<T> callback, OBJModel baseModel, BakedModel baseBaked, IModelConfiguration owner, Function<Material, TextureAtlasSprite> spriteGetter, ModelState sprite, boolean isDynamic)
	{
		this.callback = callback;
		this.baseModel = baseModel;
		this.baseBaked = baseBaked;
		this.owner = owner;
		this.spriteGetter = spriteGetter;
		this.sprite = sprite;
		this.isDynamic = isDynamic;
		if(callback instanceof ItemCallback<T> itemCB)
			this.overrides = new Overrides(itemCB);
		else
			this.overrides = new ItemOverrides()
			{
				@Override
				public BakedModel resolve(
						@Nonnull BakedModel p_173465_, @Nonnull ItemStack p_173466_, @Nullable ClientLevel p_173467_, @Nullable LivingEntity p_173468_, int p_173469_
				)
				{
					return Minecraft.getInstance().getModelManager().getMissingModel();
				}
			};
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pSide, @Nonnull Random pRand)
	{
		return ImmutableList.of();
	}

	@Nullable
	@Override
	public String getKey(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData)
	{
		//TODO
		return "";
	}

	@Override
	public boolean useAmbientOcclusion()
	{
		return true;
	}

	@Override
	public boolean isGui3d()
	{
		return true;
	}

	@Override
	public boolean usesBlockLight()
	{
		return true;
	}

	@Override
	public boolean isCustomRenderer()
	{
		return isDynamic;
	}

	@Nonnull
	@Override
	public TextureAtlasSprite getParticleIcon()
	{
		return getParticleIcon(EmptyModelData.INSTANCE);
	}

	@Override
	public TextureAtlasSprite getParticleIcon(@Nonnull IModelData data)
	{
		//TODO do we actually need baseBaked?
		return baseBaked.getParticleIcon(data);
	}


	@Nonnull
	@Override
	public ItemOverrides getOverrides()
	{
		return overrides;
	}

	@Override
	public boolean doesHandlePerspectives()
	{
		// Done in SpecificIEOBJModel
		return true;
	}

	public Cache<Triple<T, ShaderCase, String>, List<ShadedQuads>> getGroupCache()
	{
		return groupCache;
	}

	public Map<String, ModelGroup> getGroups()
	{
		return OBJHelper.getGroups(baseModel);
	}

	public OBJModel getBaseModel()
	{
		return baseModel;
	}

	public ModelState getSprite()
	{
		return sprite;
	}

	public Function<Material, TextureAtlasSprite> getSpriteGetter()
	{
		return spriteGetter;
	}

	public IModelConfiguration getOwner()
	{
		return owner;
	}

	private class Overrides extends ItemOverrides
	{
		private final ItemCallback<T> callback;
		private final LoadingCache<Pair<T, ShaderCase>, SpecificIEOBJModel<T>> modelCache;

		private Overrides(IEOBJCallback<T> callback)
		{
			this.callback = ItemCallback.castOrDefault(callback);
			modelCache = CacheBuilder.newBuilder()
					.maximumSize(100)
					.expireAfterAccess(60, TimeUnit.SECONDS)
					.build(CacheLoader.from(p -> new SpecificIEOBJModel<>(
							GeneralIEOBJModel.this, callback, p.getFirst(), p.getSecond()
					)));
		}

		@Nullable
		@Override
		public BakedModel resolve(
				@Nonnull BakedModel baseModel, @Nonnull ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity holder, int p_173469_
		)
		{
			GlobalTempData.setActiveHolder(holder);
			T key = callback.extractKey(stack, holder);
			ShaderCase shader = stack.getCapability(CapabilityShader.SHADER_CAPABILITY).resolve()
					.map(wrapper -> {
						ItemStack shaderStack = wrapper.getShaderItem();
						if(shaderStack.getItem() instanceof IShaderItem shaderItem)
							return shaderItem.getShaderCase(shaderStack, stack, wrapper.getShaderType());
						else
							return null;
					})
					.orElse(null);
			return modelCache.getUnchecked(Pair.of(key, shader));
		}
	}
}
