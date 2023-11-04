/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;

import javax.annotation.Nonnull;

public class SafeChunkUtils
{
	public static LevelChunk getSafeChunk(LevelAccessor w, BlockPos pos)
	{
		ChunkSource provider = w.getChunkSource();
		ChunkPos chunkPos = new ChunkPos(pos);
		//TODO does this do what I want?
		return provider.getChunkNow(chunkPos.x, chunkPos.z);
	}

	public static boolean isChunkSafe(LevelAccessor w, BlockPos pos)
	{
		return getSafeChunk(w, pos)!=null;
	}

	public static BlockEntity getSafeBE(LevelAccessor w, BlockPos pos)
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
}
