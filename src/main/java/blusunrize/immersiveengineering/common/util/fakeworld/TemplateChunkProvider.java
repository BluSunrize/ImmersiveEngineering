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
		{
			byChunk.computeIfAbsent(new ChunkPos(info.pos), $ -> new ArrayList<>()).add(info);
		}
		chunks = byChunk.entrySet().stream()
				.map(e -> Pair.of(e.getKey(), new TemplateChunk(world, e.getKey(), e.getValue(), shouldShow)))
				.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
	}

	@Nullable
	@Override
	public ChunkAccess getChunk(int chunkX, int chunkZ, @Nonnull ChunkStatus requiredStatus, boolean load)
	{
		return chunks.computeIfAbsent(new ChunkPos(chunkX, chunkZ), p -> new EmptyLevelChunk(world, p));
	}

	@Nonnull
	@Override
	public String gatherStats()
	{
		return "?";
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
