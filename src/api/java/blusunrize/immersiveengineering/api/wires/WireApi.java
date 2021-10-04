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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class WireApi
{
	public static final Map<WireType, FeedthroughModelInfo> INFOS = new HashMap<>();

	public static void registerFeedthroughForWiretype(WireType w, ResourceLocation texLoc, double[] uvs,
													  double connLength, BlockState conn)
	{
		INFOS.put(w, new FeedthroughModelInfo(conn, texLoc, uvs, connLength, connLength));
	}

	public static void registerFeedthroughForWiretype(WireType w, ResourceLocation texLoc, double[] uvs,
													  double connLength, double connOffset, BlockState conn)
	{
		INFOS.put(w, new FeedthroughModelInfo(conn, texLoc, uvs, connLength, connOffset));
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

	public static record FeedthroughModelInfo(
			BlockState connector,
			ResourceLocation texture,
			double[] uvs,
			double connLength,
			double connOffset
	)
	{
		public boolean isValidConnector(BlockState state)
		{
			if(state.getBlock()!=connector.getBlock())
				return false;
			for(Property<?> p : state.getProperties())
				if(p!=IEProperties.FACING_ALL&&p!=BlockStateProperties.WATERLOGGED&&!state.getValue(p).equals(connector.getValue(p)))
					return false;
			return true;
		}
	}
}