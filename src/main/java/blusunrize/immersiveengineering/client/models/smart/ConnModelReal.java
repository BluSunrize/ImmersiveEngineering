package blusunrize.immersiveengineering.client.models.smart;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.ClientUtils;
import com.google.common.collect.Lists;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ConnModelReal implements IBakedModel
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
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
	{
		if(state instanceof IExtendedBlockState)
		{
			IExtendedBlockState iExtendedBlockState = (IExtendedBlockState) state;
			ExtBlockstateAdapter ad = new ExtBlockstateAdapter(iExtendedBlockState);
			if (cache.containsKey(ad))
				return cache.get(ad).getQuads(state, side, rand);
			IBakedModel ret = new AssembledBakedModel(iExtendedBlockState, textureAtlasSprite, base, rand);
			cache.put(ad, ret);
			return ret.getQuads(state, side, rand);
		}
		return base.getQuads(state, side, rand);
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
	public ItemOverrideList getOverrides()
	{
		return ItemOverrideList.NONE;
	}

	public class AssembledBakedModel implements IBakedModel
	{
		IBakedModel basic;
		IExtendedBlockState extendedState;
		List<BakedQuad> list;
		TextureAtlasSprite texture;

//		public AssembledBakedModel(IBakedModel b)
//		{
//			basic = b;
//			list = b.getGeneralQuads();
//		}

		public AssembledBakedModel(IExtendedBlockState iExtendedBlockState, TextureAtlasSprite tex, IBakedModel b, long posRand)
		{
			basic = b;
			extendedState = iExtendedBlockState;
			texture = tex;
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
		{
			if(list==null)
			{
				list = ClientUtils.convertConnectionFromBlockstate(extendedState, texture);
				list.addAll(basic.getQuads(extendedState, side, rand));
			}
			return Collections.synchronizedList(Lists.newArrayList(list));
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
		public ItemOverrideList getOverrides()
		{
			return ItemOverrideList.NONE;
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
			for(IProperty<?> i : state.getPropertyNames())
			{
				if(!o.state.getProperties().containsKey(i))
					return false;
				Object valThis = state.getValue(i);
				Object valOther = o.state.getValue(i);
				if(valThis==null&&valOther==null)
					continue;
				else if(valOther == null || !valOther.equals(state.getValue(i)))
					return false;
			}
			for(IUnlistedProperty<?> i : state.getUnlistedNames())
			{
				if(!o.state.getUnlistedProperties().containsKey(i))
					return false;
				Object valThis = state.getValue(i);
				Object valOther = o.state.getValue(i);
				if(valThis==null&&valOther==null)
					continue;
				else if (valOther == null || !valOther.equals(state.getValue(i)))
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
				val = prime * val + (o == null ? 0 : o.hashCode());
			for (Object o : state.getUnlistedProperties().values())
				val = prime * val + (o == null ? 0 : o.hashCode());
			return val;
		}
	}

}
