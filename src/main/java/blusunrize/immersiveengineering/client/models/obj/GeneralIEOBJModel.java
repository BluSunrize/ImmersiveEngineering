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
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.utils.client.CombinedModelData;
import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import blusunrize.immersiveengineering.client.models.obj.GeneralIEOBJModel.ModelKey;
import blusunrize.immersiveengineering.client.models.obj.SpecificIEOBJModel.ShadedQuads;
import blusunrize.immersiveengineering.client.models.obj.callback.IEOBJCallback;
import blusunrize.immersiveengineering.client.models.obj.callback.IEOBJCallbacks;
import blusunrize.immersiveengineering.client.models.obj.callback.block.BlockCallback;
import blusunrize.immersiveengineering.client.models.obj.callback.item.ItemCallback;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.mixin.accessors.client.obj.OBJModelAccess;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.ModelGroup;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

public class GeneralIEOBJModel<T> implements ICacheKeyProvider<ModelKey<T>>
{
	private final Cache<GroupKey<T>, List<ShadedQuads>> groupCache = CacheBuilder.newBuilder()
			.maximumSize(100)
			.build();
	private final LoadingCache<ModelKey<T>, SpecificIEOBJModel<T>> modelCache = CacheBuilder.newBuilder()
			.maximumSize(100)
			.build(CacheLoader.from(p -> new SpecificIEOBJModel<>(this, p.callbackKey(), p.shader(), p.renderTypeIfRelevant())));
	private final IEOBJCallback<T> callback;
	private final OBJModel baseModel;
	private final TextureAtlasSprite particles;
	private final IModelConfiguration owner;
	private final Function<Material, TextureAtlasSprite> spriteGetter;
	private final ModelState sprite;
	private final boolean isDynamic;
	private final ItemOverrides overrides;
	private final ModelProperty<T> keyProperty;

	public GeneralIEOBJModel(IEOBJCallback<T> callback, OBJModel baseModel, IModelConfiguration owner, Function<Material, TextureAtlasSprite> spriteGetter, ModelState sprite, boolean isDynamic)
	{
		this.callback = callback;
		this.baseModel = baseModel;
		this.particles = spriteGetter.apply(owner.resolveTexture("particle"));
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
		this.keyProperty = IEOBJCallbacks.getModelProperty(callback);
	}

	@Override
	public List<BakedQuad> getQuads(ModelKey<T> key)
	{
		if(key==null)
			return ImmutableList.of();
		return modelCache.getUnchecked(key).getQuads(null, null, Utils.RAND, EmptyModelData.INSTANCE);
	}

	@Nullable
	@Override
	public ModelKey<T> getKey(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData)
	{
		if(side!=null)
			return null;
		if(!extraData.hasProperty(keyProperty))
			return null;
		T key = extraData.getData(keyProperty);
		RenderType layerToCheck;
		if(BlockCallback.castOrDefault(callback).dependsOnLayer())
			layerToCheck = MinecraftForgeClient.getRenderLayer();
		else
			layerToCheck = null;
		ShaderCase shader = extraData.getData(CapabilityShader.MODEL_PROPERTY);
		return new ModelKey<>(key, shader, layerToCheck);
	}

	@Nonnull
	@Override
	public IModelData getModelData(
			@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData
	)
	{
		BlockCallback<T> blockCB = BlockCallback.castOrDefault(callback);
		BlockEntity blockEntity = level.getBlockEntity(pos);
		T key = blockCB.extractKey(level, pos, state, blockEntity);
		List<IModelData> toCombine = new ArrayList<>(3);
		toCombine.add(tileData);
		toCombine.add(new SinglePropertyModelData<>(key, keyProperty));
		if(blockEntity!=null)
		{
			LazyOptional<ShaderWrapper> shaderCap = blockEntity.getCapability(CapabilityShader.SHADER_CAPABILITY);
			if(shaderCap.isPresent())
				toCombine.add(new SinglePropertyModelData<>(
						shaderCap.orElseThrow(RuntimeException::new).getCase(), CapabilityShader.MODEL_PROPERTY
				));
		}
		return CombinedModelData.combine(toCombine.toArray(new IModelData[0]));
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
		return particles;
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

	public Cache<GroupKey<T>, List<ShadedQuads>> getGroupCache()
	{
		return groupCache;
	}

	public Map<String, ModelGroup> getGroups()
	{
		return ((OBJModelAccess)baseModel).getParts();
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

	public IEOBJCallback<T> getCallback()
	{
		return callback;
	}

	private class Overrides extends ItemOverrides
	{
		private final ItemCallback<T> callback;

		private Overrides(IEOBJCallback<T> callback)
		{
			this.callback = ItemCallback.castOrDefault(callback);
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
					.map(ShaderWrapper::getCase)
					.orElse(null);
			return modelCache.getUnchecked(new ModelKey<>(key, shader, null));
		}
	}

	public record ModelKey<T>(
			T callbackKey, ShaderCase shader, @Nullable RenderType renderTypeIfRelevant
	)
	{
	}

	public record GroupKey<T>(
			T callbackKey, ShaderCase shader, @Nullable RenderType renderTypeIfRelevant, String group
	)
	{
	}
}
