/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.fakeworld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public class TemplateChunkSection extends LevelChunkSection
{
	private final int sectionY;
	private final Predicate<BlockPos> shouldShow;
	private final ChunkPos chunkPos;

	public TemplateChunkSection(int sectionY, Registry<Biome> biomeRegistry, Predicate<BlockPos> shouldShow, ChunkPos chunkPos)
	{
		super(biomeRegistry);
		this.sectionY = sectionY;
		this.shouldShow = shouldShow;
		this.chunkPos = chunkPos;
	}

	@Nonnull
	@Override
	public BlockState setBlockState(int x, int y, int z, @Nonnull BlockState state, boolean lock)
	{
		return getBlockState(x, y, z);
	}

	@Nonnull
	public BlockState actuallySetBlockState(int x, int y, int z, @Nonnull BlockState state)
	{
		return super.setBlockState(x, y, z, state, false);
	}

	@Override
	public BlockState getBlockState(int x, int y, int z)
	{
		if(!shouldShow.test(new BlockPos(chunkPos.getMinBlockX()+x, 16*sectionY+y, chunkPos.getMinBlockZ()+z)))
			return Blocks.AIR.defaultBlockState();
		return super.getBlockState(x, y, z);
	}
}
