/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.wires;

import blusunrize.immersiveengineering.api.IEProperties;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public final class WireApi
{
	public static final Map<WireType, FeedthroughModelInfo> INFOS = new HashMap<>();

	public static void registerFeedthroughForWiretype(WireType w, ResourceLocation texLoc, float[] uvs,
													  double connLength, Supplier<BlockState> conn)
	{
		INFOS.put(w, new FeedthroughModelInfo(texLoc, uvs, connLength, connLength, conn));
	}

	public static void registerFeedthroughForWiretype(WireType w, ResourceLocation texLoc, float[] uvs,
													  double connLength, double connOffset, Supplier<BlockState> conn)
	{
		INFOS.put(w, new FeedthroughModelInfo(texLoc, uvs, connLength, connOffset, conn));
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
		public Supplier<BlockState> conn;
		final ResourceLocation texLoc;
		@OnlyIn(Dist.CLIENT)
		public TextureAtlasSprite tex;
		public final double[] uvs = new double[4];
		public final double connLength;
		public final double connOffset;

		public FeedthroughModelInfo(ResourceLocation texLoc, float[] uvs,
									double connLength, double connOffset, Supplier<BlockState> conn)
		{
			this.texLoc = texLoc;
			for(int i = 0; i < 4; i++)
				this.uvs[i] = uvs[i];
			this.connLength = connLength;
			this.connOffset = connOffset;
			this.conn = conn;
			DistExecutor.runWhenOn(Dist.CLIENT,
					() -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onModelBake));
		}

		public boolean isValidConnector(BlockState state)
		{
			BlockState conn = this.conn.get();
			if(state.getBlock()!=conn.getBlock())
				return false;
			for(Property<?> p : state.getProperties())
				if(p!=IEProperties.FACING_ALL&&p!=BlockStateProperties.WATERLOGGED&&!state.getValue(p).equals(conn.getValue(p)))
					return false;
			return true;
		}

		@OnlyIn(Dist.CLIENT)
		//TODO use more appropriate event
		public void onModelBake(ModelBakeEvent evt)
		{
			tex = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(texLoc);
		}
	}
}