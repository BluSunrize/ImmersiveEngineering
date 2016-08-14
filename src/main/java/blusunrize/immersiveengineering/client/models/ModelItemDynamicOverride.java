package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.common.items.IEItemInterfaces;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author BluSunrize - 12.08.2016
 */
public class ModelItemDynamicOverride implements IPerspectiveAwareModel
{
	IBakedModel itemModel;

	public ModelItemDynamicOverride(IBakedModel itemModel)
	{
		this.itemModel = itemModel;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
	{
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
		if(itemModel instanceof IPerspectiveAwareModel)
			((IPerspectiveAwareModel) itemModel).handlePerspective(cameraTransformType);
		return Pair.of(this, TRSRTransformation.identity().getMatrix());
	}

	static ItemOverrideList dynamicOverrides = new ItemOverrideList(new ArrayList())
	{
		HashMap<String, IBakedModel> modelCache = new HashMap();

		@Override
		public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity)
		{
			if(stack != null && stack.getItem() instanceof IEItemInterfaces.ITextureOverride)
			{
				IEItemInterfaces.ITextureOverride texOverride = (IEItemInterfaces.ITextureOverride) stack.getItem();
				String key = texOverride.getModelCacheKey(stack);
				if(key != null)
				{
					IBakedModel model = modelCache.get(key);
					if(model == null)
					{
						ItemLayerModel layerModel = new ItemLayerModel(ImmutableList.copyOf(texOverride.getTextures(stack, key)));

						Function<ResourceLocation, TextureAtlasSprite> textureGetter = new Function<ResourceLocation, TextureAtlasSprite>()
						{
							@Override
							public TextureAtlasSprite apply(ResourceLocation location)
							{
								return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
							}
						};
						model = layerModel.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, textureGetter);
						modelCache.put(key, model);
					}
					return model;
				}
			}
			return originalModel;
		}
	};
}