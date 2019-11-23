package blusunrize.immersiveengineering.api.wires;

import blusunrize.immersiveengineering.api.IEProperties;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.state.IProperty;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.BasicState;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJModel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class WireApi
{
	public static final Map<WireType, FeedthroughModelInfo> INFOS = new HashMap<>();

	public static void registerFeedthroughForWiretype(WireType w, ResourceLocation model, ImmutableMap<String, String> texRepl,
													  ResourceLocation texLoc, float[] uvs, double connLength, BlockState conn)
	{
		INFOS.put(w, new FeedthroughModelInfo(model, texRepl, texLoc, uvs, connLength, connLength, conn));
	}

	public static void registerFeedthroughForWiretype(WireType w, ResourceLocation model, ImmutableMap<String, String> texRepl,
													  ResourceLocation texLoc, float[] uvs, double connLength, double connOffset,
													  BlockState conn)
	{
		INFOS.put(w, new FeedthroughModelInfo(model, texRepl, texLoc, uvs, connLength, connOffset, conn));
	}

	public static void registerFeedthroughForWiretype(WireType w, ResourceLocation model, ResourceLocation texLoc, float[] uvs,
													  double connLength, BlockState conn)
	{
		INFOS.put(w, new FeedthroughModelInfo(model, ImmutableMap.of(), texLoc, uvs, connLength, connLength, conn));
	}

	@Nullable
	public static WireType getWireType(BlockState state)
	{
		for(Map.Entry<WireType, FeedthroughModelInfo> entry : INFOS.entrySet())
			if(entry.getValue().isValidConnector(state))
				return entry.getKey();
		return null;
	}

	public static final Map<String, Set<WireType>> WIRES_BY_CATEGORY = new HashMap<>();

	public static void registerWireType(WireType w)
	{
		String category = w.getCategory();
		if(category!=null)
		{
			if(!WIRES_BY_CATEGORY.containsKey(category))
				WIRES_BY_CATEGORY.put(category, new HashSet<>());
			WIRES_BY_CATEGORY.get(category).add(w);
		}
	}

	public static boolean canMix(WireType a, WireType b)
	{
		String cat = a.getCategory();
		return cat!=null&&cat.equals(b.getCategory());
	}

	public static Set<WireType> getWiresForType(@Nullable String category)
	{
		if(category==null)
			return ImmutableSet.of();
		return WIRES_BY_CATEGORY.get(category);
	}

	public static class FeedthroughModelInfo
	{
		public final ResourceLocation modelLoc;
		final ImmutableMap<String, String> texReplacements;
		@Nonnull
		public BlockState conn;
		@OnlyIn(Dist.CLIENT)
		public IBakedModel model;
		final ResourceLocation texLoc;
		@OnlyIn(Dist.CLIENT)
		public TextureAtlasSprite tex;
		public final double[] uvs = new double[4];
		public final double connLength;
		public final double connOffset;

		public FeedthroughModelInfo(ResourceLocation model, ImmutableMap<String, String> texRepl, ResourceLocation texLoc, float[] uvs,
									double connLength, double connOffset, @Nonnull BlockState conn)
		{
			modelLoc = model;
			this.texLoc = texLoc;
			for(int i = 0; i < 4; i++)
				this.uvs[i] = uvs[i];
			texReplacements = texRepl;
			this.connLength = connLength;
			this.connOffset = connOffset;
			this.conn = conn;
		}

		public boolean isValidConnector(BlockState state)
		{
			if(state.getBlock()!=conn.getBlock())
				return false;
			for(IProperty<?> p : state.getProperties())
				if(p!=IEProperties.FACING_ALL&&!state.get(p).equals(conn.get(p)))
					return false;
			return true;
		}

		@OnlyIn(Dist.CLIENT)
		public void onModelBake(ModelBakeEvent evt)
		{
			IModel model;
			try
			{
				model = ModelLoaderRegistry.getModel(modelLoc);
			} catch(Exception e)
			{
				e.printStackTrace();
				model = ModelLoaderRegistry.getMissingModel();
			}
			if(model instanceof OBJModel)
			{
				OBJModel obj = (OBJModel)model;
				obj = (OBJModel)obj.retexture(texReplacements);
				model = obj.process(ImmutableMap.of("flip-v", "true"));
			}
			//TODO why doesn't this work with a lambda???
			this.model = model.bake(evt.getModelLoader(), new Function<ResourceLocation, TextureAtlasSprite>()
					{
						@Override
						public TextureAtlasSprite apply(ResourceLocation rl)
						{
							return Minecraft.getInstance().getTextureMap().getSprite(rl);
						}
					},
					new BasicState(model.getDefaultState(), false), DefaultVertexFormats.ITEM);
			tex = Minecraft.getInstance().getTextureMap().getSprite(texLoc);
		}
	}
}