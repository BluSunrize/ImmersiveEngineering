/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils;

import blusunrize.immersiveengineering.api.Lib;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = Lib.MODID)
public class SafeChunkUtils
{
	private static final Map<LevelAccessor, Set<ChunkPos>> unloadingChunks = new WeakHashMap<>();

	//Based on a version by Ellpeck, but changed to use canTick and slightly extended
	public static LevelChunk getSafeChunk(LevelAccessor w, BlockPos pos)
	{
		ChunkSource provider = w.getChunkSource();
		ChunkPos chunkPos = new ChunkPos(pos);
		if(unloadingChunks.getOrDefault(w, ImmutableSet.of()).contains(chunkPos))
			return null;
		else if(provider.isTickingChunk(pos))
			return provider.getChunk(chunkPos.x, chunkPos.z, false);
		else
			return null;
	}

	public static boolean isChunkSafe(LevelAccessor w, BlockPos pos)
	{
		return getSafeChunk(w, pos)!=null;
	}

	public static BlockEntity getSafeTE(LevelAccessor w, BlockPos pos)
	{
		LevelChunk c = getSafeChunk(w, pos);
		if(c==null)
			return null;
		else
			return c.getBlockEntity(pos);
	}

	@Nonnull
	public static BlockState getBlockState(LevelAccessor w, BlockPos pos)
	{
		LevelChunk c = getSafeChunk(w, pos);
		if(c==null)
			return Blocks.AIR.defaultBlockState();
		else
			return c.getBlockState(pos);
	}

	public static int getRedstonePower(Level w, BlockPos pos, Direction d)
	{
		if(!isChunkSafe(w, pos))
			return 0;
		else
			return w.getSignal(pos, d);
	}

	public static int getRedstonePowerFromNeighbors(Level w, BlockPos pos)
	{
		int ret = 0;
		for(Direction d : DirectionUtils.VALUES)
		{
			int atNeighbor = getRedstonePower(w, pos.relative(d), d);
			ret = Math.max(ret, atNeighbor);
			if(ret >= 15)
				break;
		}
		return ret;
	}


	public static void onChunkUnload(ChunkEvent.Unload ev)
	{
		unloadingChunks.computeIfAbsent(ev.getWorld(), w -> new HashSet<>()).add(ev.getChunk().getPos());
	}

	public static void onTick(WorldTickEvent ev)
	{
		if(ev.phase==Phase.START)
			unloadingChunks.remove(ev.world);
	}
}
