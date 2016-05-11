package blusunrize.immersiveengineering.client.models.smart;

import java.util.HashMap;
import java.util.List;

import com.google.common.collect.ImmutableList;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.ClientUtils;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.Attributes;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class ConnModelReal implements IFlexibleBakedModel, ISmartBlockModel
{

	TextureAtlasSprite textureAtlasSprite = Minecraft.getMinecraft().getTextureMapBlocks()
			.getAtlasSprite(ImmersiveEngineering.MODID.toLowerCase() + ":blocks/wire");
	public static final HashMap<ExtBlockstateAdapter, IBakedModel> cache = new HashMap<>();
	IBakedModel base;

	public ConnModelReal(IBakedModel basic)
	{
		base = basic;
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return false;
	}

	@Override
	public boolean isGui3d()
	{
		return false;
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return base.getParticleTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public List<BakedQuad> getFaceQuads(EnumFacing side)
	{
		return base.getFaceQuads(side);
	}

	@Override
	public List<BakedQuad> getGeneralQuads()
	{
		return base.getGeneralQuads();
	}

	@Override
	public VertexFormat getFormat()
	{
		return Attributes.DEFAULT_BAKED_FORMAT;
	}

	@Override
	public IBakedModel handleBlockState(IBlockState iBlockState)
	{
		if (iBlockState instanceof IExtendedBlockState)
		{
			IExtendedBlockState iExtendedBlockState = (IExtendedBlockState) iBlockState;
			ExtBlockstateAdapter ad = new ExtBlockstateAdapter(iExtendedBlockState);
			if (cache.containsKey(ad))
				return cache.get(ad);
			IBakedModel ret = new AssembledBakedModel(iExtendedBlockState, textureAtlasSprite, base);
			cache.put(ad, ret);
			return ret;
		}
		return this;
	}

	public class AssembledBakedModel implements IBakedModel
	{
		IBakedModel basic;
		List<BakedQuad> list;

		public AssembledBakedModel(IBakedModel b)
		{
			basic = b;
			list = b.getGeneralQuads();
		}

		public AssembledBakedModel(IExtendedBlockState iExtendedBlockState, TextureAtlasSprite tex, IBakedModel b)
		{
			list = ClientUtils.convertConnectionFromBlockstate(iExtendedBlockState, tex);
			basic = b;
			list.addAll(basic.getGeneralQuads());
		}

		@Override
		public List<BakedQuad> getFaceQuads(EnumFacing side)
		{
			return ImmutableList.of();
		}

		@Override
		public List<BakedQuad> getGeneralQuads()
		{
			return list;
		}

		@Override
		public boolean isAmbientOcclusion()
		{
			return false;
		}

		@Override
		public boolean isGui3d()
		{
			return false;
		}

		@Override
		public boolean isBuiltInRenderer()
		{
			return false;
		}

		@Override
		public TextureAtlasSprite getParticleTexture()
		{
			return base.getParticleTexture();
		}

		@Override
		public ItemCameraTransforms getItemCameraTransforms()
		{
			return ItemCameraTransforms.DEFAULT;
		}

	}

	public static class ExtBlockstateAdapter
	{
		final IExtendedBlockState state;

		public ExtBlockstateAdapter(IExtendedBlockState s)
		{
			state = s;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == this)
				return true;
			if (!(obj instanceof ExtBlockstateAdapter))
				return false;
			ExtBlockstateAdapter o = (ExtBlockstateAdapter) obj;
			for (IProperty i : state.getPropertyNames())
			{
				Object valOther = o.state.getValue(i);
				if (valOther == null || !valOther.equals(state.getValue(i)))
					return false;
			}
			for (IUnlistedProperty i : state.getUnlistedNames())
			{
				Object valOther = o.state.getValue(i);
				if (valOther == null || !valOther.equals(state.getValue(i)))
					return false;
			}

			return true;
		}

		@Override
		public int hashCode()
		{
			int val = 1;
			for (Object o : state.getProperties().values())
				val += o.hashCode();
			for (Object o : state.getUnlistedProperties().values())
				val += o.hashCode();

			return val;
		}
	}

}
