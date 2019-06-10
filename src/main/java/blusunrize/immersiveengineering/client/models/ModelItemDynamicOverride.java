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
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * @author BluSunrize - 12.08.2016
 */
public class ModelItemDynamicOverride implements IBakedModel
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
			Optional<TRSRTransformation> transform = Optional.of(TRSRTransformation.identity());
			for(int i = 0; i < textures.size(); i++)
				builder.addAll(ItemLayerModel.getQuadsForSprite(i, ClientUtils.getSprite(textures.get(i)), DefaultVertexFormats.ITEM, transform));
			quads = builder.build();
			guiModel = new BakedGuiItemModel(this);
		}
		else
		{
			guiModel = itemModel;
		}
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
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
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType)
	{
		return Pair.of(cameraTransformType==TransformType.GUI?guiModel: this, itemModel.handlePerspective(cameraTransformType).getRight());
	}

	public static final HashMap<String, IBakedModel> modelCache = new HashMap<>();
	static ItemOverrideList dynamicOverrides = new ItemOverrideList(new ArrayList<>())
	{

		@Override
		public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity)
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
						model = new ModelItemDynamicOverride(originalModel instanceof ModelItemDynamicOverride?((ModelItemDynamicOverride)originalModel).itemModel:originalModel, texOverride.getTextures(stack, key));
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
				if(quad.getFace()==EnumFacing.SOUTH)
				{
					builder.add(quad);
				}
			}
			this.quads = builder.build();
		}

		@Nonnull
		@Override
		public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
		{
			if(side==null)
			{
				return quads;
			}
			return ImmutableList.of();
		}

		@Nonnull
		@Override
		public Pair<? extends IBakedModel, Matrix4f> handlePerspective(@Nonnull TransformType type)
		{
			return this.originalModel.itemModel.handlePerspective(type);
		}
	}
}