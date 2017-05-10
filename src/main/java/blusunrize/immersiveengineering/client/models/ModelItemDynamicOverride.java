package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces;
import com.google.common.base.Optional;
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
	ImmutableList<BakedQuad> quads;

	public ModelItemDynamicOverride(IBakedModel itemModel, @Nullable List<ResourceLocation> textures)
	{
		this.itemModel = itemModel;
		if(textures != null)
		{
			ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
			Optional<TRSRTransformation> transform = Optional.of(TRSRTransformation.identity());
			for(int i = 0; i < textures.size(); i++)
				builder.addAll(ItemLayerModel.getQuadsForSprite(i, ClientUtils.getSprite(textures.get(i)), DefaultVertexFormats.ITEM, transform));
			quads = builder.build();
		}
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
	{
		if(quads != null)
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
		if(itemModel instanceof IPerspectiveAwareModel)
			return Pair.of(this, ((IPerspectiveAwareModel)itemModel).handlePerspective(cameraTransformType).getRight());
		return Pair.of(this, TRSRTransformation.identity().getMatrix());
	}

	static ItemOverrideList dynamicOverrides = new ItemOverrideList(new ArrayList())
	{
		HashMap<String, IBakedModel> modelCache = new HashMap();

		@Override
		public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity)
		{
			if(!stack.isEmpty() && stack.getItem() instanceof IEItemInterfaces.ITextureOverride)
			{
				IEItemInterfaces.ITextureOverride texOverride = (IEItemInterfaces.ITextureOverride) stack.getItem();
				String key = texOverride.getModelCacheKey(stack);
				if(key != null)
				{
					IBakedModel model = modelCache.get(key);
					if(model == null)
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
}