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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = Lib.MODID)
public class SafeChunkUtils
{
	private static final Map<IWorld, Set<ChunkPos>> unloadingChunks = new WeakHashMap<>();

	//Based on a version by Ellpeck, but changed to use canTick and slightly extended
	public static Chunk getSafeChunk(@Nullable IWorld w, BlockPos pos)
	{
		if(w==null)
			return null;
		AbstractChunkProvider provider = w.getChunkProvider();
		ChunkPos chunkPos = new ChunkPos(pos);
		if(unloadingChunks.getOrDefault(w, ImmutableSet.of()).contains(chunkPos))
			return null;
		else if(provider.canTick(pos))
			return provider.getChunk(chunkPos.x, chunkPos.z, false);
		else
			return null;
	}

	public static boolean isChunkSafe(@Nullable IWorld w, BlockPos pos)
	{
		return getSafeChunk(w, pos)!=null;
	}

	public static TileEntity getSafeTE(@Nullable IWorld w, BlockPos pos)
	{
		Chunk c = getSafeChunk(w, pos);
		if(c==null)
			return null;
		else
			return c.getTileEntity(pos);
	}

	@Nonnull
	public static BlockState getBlockState(@Nullable IWorld w, BlockPos pos)
	{
		Chunk c = getSafeChunk(w, pos);
		if(c==null)
			return Blocks.AIR.getDefaultState();
		else
			return c.getBlockState(pos);
	}

	public static int getRedstonePower(@Nullable World w, BlockPos pos, Direction d)
	{
		if(w==null||!isChunkSafe(w, pos))
			return 0;
		else
			return w.getRedstonePower(pos, d);
	}

	public static int getRedstonePowerFromNeighbors(@Nullable World w, BlockPos pos)
	{
		int ret = 0;
		for(Direction d : DirectionUtils.VALUES)
		{
			int atNeighbor = getRedstonePower(w, pos.offset(d), d);
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
