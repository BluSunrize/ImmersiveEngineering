package blusunrize.immersiveengineering.client.models.smart;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

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
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.client.MinecraftForgeClient;
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
		List<BakedQuad>[] lists;

		public AssembledBakedModel(IBakedModel b)
		{
			basic = b;
			lists = new List[]{b.getGeneralQuads(), ImmutableList.of()};
		}

		public AssembledBakedModel(IExtendedBlockState iExtendedBlockState, TextureAtlasSprite tex, IBakedModel b)
		{
			lists = ClientUtils.convertConnectionFromBlockstate(iExtendedBlockState, tex);
			basic = b;
			if(basic instanceof ISmartBlockModel)
				basic = ((ISmartBlockModel)basic).handleBlockState(iExtendedBlockState);
			lists[0].addAll(basic.getGeneralQuads());
		}

		@Override
		public List<BakedQuad> getFaceQuads(EnumFacing side)
		{
			return ImmutableList.of();
		}

		@Override
		public List<BakedQuad> getGeneralQuads()
		{
			EnumWorldBlockLayer layer = MinecraftForgeClient.getRenderLayer();
			if (layer != EnumWorldBlockLayer.SOLID&&layer!=EnumWorldBlockLayer.TRANSLUCENT)
				return ImmutableList.of();
			List<BakedQuad> quadList = Collections.synchronizedList(Lists.newArrayList(lists[layer==EnumWorldBlockLayer.SOLID?0:1]));
			return quadList;
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
			for (IProperty<?> i : state.getPropertyNames())
			{
				if (!o.state.getProperties().containsKey(i))
					return false;
				Object valOther = o.state.getValue(i);
				Object valMine = state.getValue(i);
				if ((valOther == null^valMine==null) || (valOther!=null&&!valOther.equals(valMine)))
					return false;
			}
			for (IUnlistedProperty<?> i : state.getUnlistedNames())
			{
				if (!o.state.getUnlistedProperties().containsKey(i))
					return false;
				Object valOther = o.state.getValue(i);
				Object valMine = state.getValue(i);
				if ((valOther == null^valMine==null) || (valOther!=null&&!valOther.equals(valMine)))
					return false;
			}

			return true;
		}

		@Override
		public int hashCode()
		{
			int val = 0;
			final int prime = 31;
			for (Object o : state.getProperties().values())
				val = prime*val+(o==null?0:o.hashCode());
			for (Object o : state.getUnlistedProperties().values())
				val = prime*val+(o==null?0:o.hashCode());

			return val;
		}
	}

}
