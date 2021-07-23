/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * @author BluSunrize - 12.08.2016
 */
public class ModelItemDynamicOverride extends BakedIEModel
{
	BakedModel itemModel;
	ImmutableList<BakedQuad> quads;
	BakedModel guiModel;

	public ModelItemDynamicOverride(BakedModel itemModel, @Nullable List<ResourceLocation> textures)
	{
		this.itemModel = itemModel;
		if(textures!=null)
		{
			ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
			for(int i = 0; i < textures.size(); i++)
				builder.addAll(ItemLayerModel.getQuadsForSprite(i, ClientUtils.getSprite(textures.get(i)), Transformation.identity()));
			quads = builder.build();
			guiModel = new BakedGuiItemModel(this);
		}
		else
		{
			guiModel = itemModel;
		}
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData)
	{
		if(quads!=null&&side==null)
			return quads;
		return itemModel.getQuads(state, side, rand);
	}

	@Override
	public boolean useAmbientOcclusion()
	{
		return itemModel.useAmbientOcclusion();
	}

	@Override
	public boolean isGui3d()
	{
		return itemModel.isGui3d();
	}

	@Override
	public boolean isCustomRenderer()
	{
		return itemModel.isCustomRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleIcon()
	{
		return itemModel.getParticleIcon();
	}

	@Override
	public ItemTransforms getTransforms()
	{
		return itemModel.getTransforms();
	}

	@Override
	public ItemOverrides getOverrides()
	{
		return dynamicOverrides;
	}

	@Override
	public BakedModel handlePerspective(TransformType cameraTransformType, PoseStack matrixStack)
	{
		return cameraTransformType==TransformType.GUI?guiModel: this;
	}

	public static final HashMap<String, BakedModel> modelCache = new HashMap<>();
	static ItemOverrides dynamicOverrides = new ItemOverrides()
	{
		@Nullable
		@Override
		public BakedModel resolve(@Nonnull BakedModel originalModel, ItemStack stack, @Nullable ClientLevel worldIn, @Nullable LivingEntity entityIn)
		{
			if(!stack.isEmpty()&&stack.getItem() instanceof IEItemInterfaces.ITextureOverride)
			{
				IEItemInterfaces.ITextureOverride texOverride = (IEItemInterfaces.ITextureOverride)stack.getItem();
				String key = texOverride.getModelCacheKey(stack);
				if(key!=null)
				{
					BakedModel model = modelCache.get(key);
					if(model==null)
					{
						model = new ModelItemDynamicOverride(originalModel, texOverride.getTextures(stack, key));
						modelCache.put(key, model);
					}
					return model;
				}
			}
			return originalModel;
		}
	};

	public static class BakedGuiItemModel extends BakedModelWrapper<ModelItemDynamicOverride>
	{
		private final ImmutableList<BakedQuad> quads;

		public BakedGuiItemModel(ModelItemDynamicOverride originalModel)
		{
			super(originalModel);
			ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
			for(BakedQuad quad : originalModel.quads)
			{
				if(quad.getDirection()==Direction.SOUTH)
				{
					builder.add(quad);
				}
			}
			this.quads = builder.build();
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand)
		{
			if(side==null)
			{
				return quads;
			}
			return ImmutableList.of();
		}

		@Nonnull
		@Override
		public BakedModel handlePerspective(ItemTransforms.TransformType cameraTransformType, PoseStack mat)
		{
			return originalModel.itemModel.handlePerspective(cameraTransformType, mat);
		}
	}
}