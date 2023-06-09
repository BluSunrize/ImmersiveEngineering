/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.fakeworld;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.lighting.LevelLightEngine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TemplateChunkProvider extends ChunkSource
{
	private final Map<ChunkPos, ChunkAccess> chunks;
	private final Level world;
	private final LevelLightEngine lightManager;

	public TemplateChunkProvider(List<StructureBlockInfo> blocks, Level world, Predicate<BlockPos> shouldShow)
	{
		this.world = world;
		this.lightManager = new LevelLightEngine(this, true, true);
		Map<ChunkPos, List<StructureBlockInfo>> byChunk = new HashMap<>();
		for(StructureBlockInfo info : blocks)
			byChunk.computeIfAbsent(new ChunkPos(info.pos()), $ -> new ArrayList<>()).add(info);
		chunks = byChunk.entrySet().stream()
				.map(e -> Pair.of(e.getKey(), new TemplateChunk(world, e.getKey(), e.getValue(), shouldShow)))
				.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
	}

	@Nullable
	@Override
	public ChunkAccess getChunk(int chunkX, int chunkZ, @Nonnull ChunkStatus requiredStatus, boolean load)
	{
		return chunks.computeIfAbsent(new ChunkPos(chunkX, chunkZ), p -> new EmptyLevelChunk(world, p, world.getUncachedNoiseBiome(0, 0, 0)));
	}

	@Override
	public void tick(BooleanSupplier p_202162_, boolean p_202163_)
	{}

	@Nonnull
	@Override
	public String gatherStats()
	{
		return "?";
	}

	@Override
	public int getLoadedChunksCount()
	{
		return 0;
	}

	@Nonnull
	@Override
	public LevelLightEngine getLightEngine()
	{
		return lightManager;
	}

	@Nonnull
	@Override
	public BlockGetter getLevel()
	{
		return world;
	}
}
