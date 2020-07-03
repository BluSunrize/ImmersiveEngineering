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
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.world.World;
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
	IBakedModel itemModel;
	ImmutableList<BakedQuad> quads;
	IBakedModel guiModel;

	public ModelItemDynamicOverride(IBakedModel itemModel, @Nullable List<ResourceLocation> textures)
	{
		this.itemModel = itemModel;
		if(textures!=null)
		{
			ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
			for(int i = 0; i < textures.size(); i++)
				builder.addAll(ItemLayerModel.getQuadsForSprite(i, ClientUtils.getSprite(textures.get(i)), TransformationMatrix.identity()));
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
	public boolean isAmbientOcclusion()
	{
		return itemModel.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d()
	{
		return itemModel.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return itemModel.isBuiltInRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return itemModel.getParticleTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return itemModel.getItemCameraTransforms();
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return dynamicOverrides;
	}

	@Override
	public IBakedModel handlePerspective(TransformType cameraTransformType, MatrixStack matrixStack)
	{
		return cameraTransformType==TransformType.GUI?guiModel: this;
	}

	public static final HashMap<String, IBakedModel> modelCache = new HashMap<>();
	static ItemOverrideList dynamicOverrides = new ItemOverrideList()
	{
		@Nullable
		@Override
		public IBakedModel getModelWithOverrides(@Nonnull IBakedModel originalModel, ItemStack stack, @Nullable World worldIn, @Nullable LivingEntity entityIn)
		{
			if(!stack.isEmpty()&&stack.getItem() instanceof IEItemInterfaces.ITextureOverride)
			{
				IEItemInterfaces.ITextureOverride texOverride = (IEItemInterfaces.ITextureOverride)stack.getItem();
				String key = texOverride.getModelCacheKey(stack);
				if(key!=null)
				{
					IBakedModel model = modelCache.get(key);
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
				if(quad.getFace()==Direction.SOUTH)
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
		public IBakedModel handlePerspective(ItemCameraTransforms.TransformType cameraTransformType, MatrixStack mat)
		{
			return originalModel.itemModel.handlePerspective(cameraTransformType, mat);
		}
	}
}