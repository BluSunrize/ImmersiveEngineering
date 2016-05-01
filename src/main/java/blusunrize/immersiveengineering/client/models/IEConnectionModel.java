package blusunrize.immersiveengineering.client.models;

import java.util.List;

import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.ISmartItemModel;

@SuppressWarnings("deprecation")
public class IEConnectionModel implements IFlexibleBakedModel, ISmartBlockModel, ISmartItemModel, IPerspectiveAwareModel
{
    private final LoadingCache<IModelState, IEConnectionModel> cache = CacheBuilder.newBuilder().maximumSize(20).build(new CacheLoader<IModelState, IEConnectionModel>()
    {
        public IEConnectionModel load(IModelState state) throws Exception
        {
            return new IEConnectionModel(baseModel, state);
        }
    });
	
	IBakedModel baseModel;
	VertexFormat format;
	IModelState modelState;

	public IEConnectionModel(IBakedModel baseModel, IModelState state)
	{
		this.baseModel = baseModel;
		this.modelState = state;
		this.format = baseModel instanceof IFlexibleBakedModel?((IFlexibleBakedModel)baseModel).getFormat():DefaultVertexFormats.BLOCK;
	}

	@Override
	public Pair<? extends IFlexibleBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType)
	{
		if(baseModel instanceof IPerspectiveAwareModel)
			return ((IPerspectiveAwareModel)baseModel).handlePerspective(cameraTransformType);
		return Pair.of(this, new Matrix4f());
	}

	@Override
	public IBakedModel handleItemState(ItemStack stack)
	{
		return baseModel;
	}
	
	@Override
	public IBakedModel handleBlockState(IBlockState state)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<BakedQuad> getGeneralQuads()
	{
		return baseModel.getGeneralQuads();
	}

	@Override
	public List<BakedQuad> getFaceQuads(EnumFacing facing)
	{
		return baseModel.getFaceQuads(facing);
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
	public VertexFormat getFormat()
	{
		return format;
	}
}