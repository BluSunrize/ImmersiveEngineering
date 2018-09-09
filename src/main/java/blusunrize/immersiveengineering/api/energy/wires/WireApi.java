package blusunrize.immersiveengineering.api.energy.wires;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.models.smart.ConnLoader;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public final class WireApi
{
	public static final Map<WireType, FeedthroughModelInfo> INFOS = new HashMap<>();

	@SideOnly(Side.CLIENT)
	@Deprecated//This should be done in JSON in IE 86+
	public static void registerConnectorForRender(String key, ResourceLocation baseModel,
												  @Nullable ImmutableMap<String, String> texReplacement)
	{
		if(ConnLoader.baseModels.containsKey(key))
			IELogger.warn("Tried to register connector model for "+key+" twice. Active mod: "+Loader.instance().activeModContainer().getModId());
		ConnLoader.baseModels.put(key, baseModel);
		if(texReplacement!=null)
			ConnLoader.textureReplacements.put(key, texReplacement);
	}

	@Deprecated
	public static void registerFeedthroughForWiretype(WireType w, ResourceLocation model, ImmutableMap<String, String> texRepl,
													  ResourceLocation texLoc, float[] uvs, double connLength, Predicate<IBlockState> matches,
													  float dmgPerEnergy, float maxDmg, Function<Float, Float> postProcessDmg)
	{
		INFOS.put(w, new FeedthroughModelInfo(model, texRepl, texLoc, uvs, connLength, connLength, matches, dmgPerEnergy, maxDmg, postProcessDmg));
	}

	@Deprecated
	public static void registerFeedthroughForWiretype(WireType w, ResourceLocation model, ImmutableMap<String, String> texRepl,
													  ResourceLocation texLoc, float[] uvs, double connLength, double connOffset,
													  Predicate<IBlockState> matches,
													  float dmgPerEnergy, float maxDmg, Function<Float, Float> postProcessDmg)
	{
		INFOS.put(w, new FeedthroughModelInfo(model, texRepl, texLoc, uvs, connLength, connOffset, matches, dmgPerEnergy, maxDmg, postProcessDmg));
	}

	@Deprecated
	public static void registerFeedthroughForWiretype(WireType w, ResourceLocation model, ResourceLocation texLoc, float[] uvs,
													  double connLength, Predicate<IBlockState> matches,
													  float dmgPerEnergy, float maxDmg, Function<Float, Float> postProcessDmg)
	{
		INFOS.put(w, new FeedthroughModelInfo(model, ImmutableMap.of(), texLoc, uvs, connLength, connLength, matches, dmgPerEnergy, maxDmg, postProcessDmg));
	}

	public static void registerFeedthroughForWiretype(WireType w, ResourceLocation model, ImmutableMap<String, String> texRepl,
													  ResourceLocation texLoc, float[] uvs, double connLength, IBlockState conn,
													  float dmgPerEnergy, float maxDmg, Function<Float, Float> postProcessDmg)
	{
		INFOS.put(w, new FeedthroughModelInfo(model, texRepl, texLoc, uvs, connLength, connLength, conn, dmgPerEnergy, maxDmg, postProcessDmg));
	}

	public static void registerFeedthroughForWiretype(WireType w, ResourceLocation model, ImmutableMap<String, String> texRepl,
													  ResourceLocation texLoc, float[] uvs, double connLength, double connOffset,
													  IBlockState conn,
													  float dmgPerEnergy, float maxDmg, Function<Float, Float> postProcessDmg)
	{
		INFOS.put(w, new FeedthroughModelInfo(model, texRepl, texLoc, uvs, connLength, connOffset, conn, dmgPerEnergy, maxDmg, postProcessDmg));
	}

	public static void registerFeedthroughForWiretype(WireType w, ResourceLocation model, ResourceLocation texLoc, float[] uvs,
													  double connLength, IBlockState conn,
													  float dmgPerEnergy, float maxDmg, Function<Float, Float> postProcessDmg)
	{
		INFOS.put(w, new FeedthroughModelInfo(model, ImmutableMap.of(), texLoc, uvs, connLength, connLength, conn, dmgPerEnergy, maxDmg, postProcessDmg));
	}

	@Nullable
	public static WireType getWireType(IBlockState state)
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
		public final float dmgPerEnergy;
		public final float maxDmg;
		public final Function<Float, Float> postProcessDmg;
		@Nullable
		public IBlockState conn;
		@SideOnly(Side.CLIENT)
		public IBakedModel model;
		final ResourceLocation texLoc;
		@SideOnly(Side.CLIENT)
		public TextureAtlasSprite tex;
		public final double[] uvs = new double[4];
		public final double connLength;
		public final double connOffset;
		@Nullable
		private Predicate<IBlockState> matches;

		public FeedthroughModelInfo(ResourceLocation model, ImmutableMap<String, String> texRepl, ResourceLocation texLoc, float[] uvs,
									double connLength, double connOffset, @Nullable Predicate<IBlockState> matches,
									float dmgPerEnergy, float maxDmg, Function<Float, Float> postProcessDmg)
		{
			modelLoc = model;
			this.texLoc = texLoc;
			for(int i = 0; i < 4; i++)
				this.uvs[i] = uvs[i];
			texReplacements = texRepl;
			this.connLength = connLength;
			this.connOffset = connOffset;
			this.dmgPerEnergy = dmgPerEnergy;
			this.maxDmg = maxDmg;
			this.matches = matches;
			this.postProcessDmg = postProcessDmg;
		}

		public FeedthroughModelInfo(ResourceLocation model, ImmutableMap<String, String> texRepl, ResourceLocation texLoc, float[] uvs,
									double connLength, double connOffset, IBlockState conn,
									float dmgPerEnergy, float maxDmg, Function<Float, Float> postProcessDmg)
		{
			this(model, texRepl, texLoc, uvs, connLength, connOffset, (Predicate<IBlockState>)null, dmgPerEnergy, maxDmg, postProcessDmg);
			this.conn = conn;
		}

		public boolean isValidConnector(IBlockState state)
		{
			if(matches!=null)
			{
				return matches.test(state);
			}
			else
			{
				assert conn!=null;
				if(state.getBlock()!=conn.getBlock())
					return false;
				for(IProperty<?> p : state.getPropertyKeys())
					if(p!=IEProperties.FACING_ALL&&!state.getValue(p).equals(conn.getValue(p)))
						return false;
				return true;
			}
		}

		public boolean canReplace()
		{
			return conn!=null;
		}

		@SideOnly(Side.CLIENT)
		public void onResourceReload(Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter, VertexFormat format)
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
			this.model = model.bake(model.getDefaultState(), format, bakedTextureGetter);
			tex = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texLoc.toString());
		}
	}
}