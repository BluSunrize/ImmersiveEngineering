package blusunrize.immersiveengineering.client.models;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.model.IModelState;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.List;

@SuppressWarnings("deprecation")
public class IEConnectionModel implements IBakedModel
{
	private final LoadingCache<IModelState, IEConnectionModel> cache = CacheBuilder.newBuilder().maximumSize(20).build(new CacheLoader<IModelState, IEConnectionModel>()
	{
		@Override
		public IEConnectionModel load(IModelState state) throws Exception
		{
			return new IEConnectionModel(baseModel, state);
		}
	});

	IBakedModel baseModel;
	IModelState modelState;

	public IEConnectionModel(IBakedModel baseModel, IModelState state)
	{
		this.baseModel = baseModel;
		this.modelState = state;
//		this.format = baseModel instanceof IFlexibleBakedModel?((IFlexibleBakedModel)baseModel).getFormat():DefaultVertexFormats.BLOCK;
	}

	@Override
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType)
	{
		return baseModel.handlePerspective(cameraTransformType);
	}

//	@Override
//	public IBakedModel handleItemState(ItemStack stack)
//	{
//		return baseModel;
//	}

//	@Override
//	public IBakedModel handleBlockState(IBlockState state)
//	{
//		return null;
//	}

//	@Override
//	public List<BakedQuad> getGeneralQuads()
//	{
//		return baseModel.getGeneralQuads();
//	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
	{
		return baseModel.getQuads(state, side, rand);
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return baseModel.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d()
	{
		return baseModel.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return baseModel.isBuiltInRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return baseModel.getParticleTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return baseModel.getItemCameraTransforms();
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return baseModel.getOverrides();
	}

//	@Override
//	public VertexFormat getFormat()
//	{
//		return format;
//	}
}