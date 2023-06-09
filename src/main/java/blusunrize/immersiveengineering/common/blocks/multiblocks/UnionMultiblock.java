/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks.MultiblockManualData;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.client.utils.UnionMBManualData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class UnionMultiblock implements IMultiblock
{
	private final ResourceLocation name;
	private final List<TransformedMultiblock> parts;
	private final Supplier<Component> displayName;

	public UnionMultiblock(ResourceLocation name, List<TransformedMultiblock> parts)
	{
		this.name = name;
		this.parts = parts;
		this.displayName = () -> parts.stream()
				.map(TransformedMultiblock::multiblock)
				.map(IMultiblock::getDisplayName)
				.map(Component::copy)
				.reduce((c1, c2) -> c1.append(", ").append(c2))
				.orElse(Component.empty().copy());
	}

	@Override
	public ResourceLocation getUniqueName()
	{
		return name;
	}

	@Override
	public boolean isBlockTrigger(BlockState state, Direction side, @Nullable Level world)
	{
		return false;
	}

	@Override
	public boolean createStructure(Level world, BlockPos pos, Direction side, Player player)
	{
		return false;
	}

	@Override
	public List<StructureBlockInfo> getStructure(@Nullable Level world)
	{
		Vec3i min = getMin(world);
		List<StructureBlockInfo> ret = new ArrayList<>();
		for(TransformedMultiblock part : parts)
			for(StructureBlockInfo i : part.multiblock.getStructure(world))
				ret.add(new StructureBlockInfo(part.toUnionCoords(i.pos()).subtract(min), i.state(), i.nbt()));
		return ret;
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}

	@Override
	public Vec3i getSize(@Nullable Level world)
	{
		Vec3i max = Vec3i.ZERO;
		for(TransformedMultiblock part : parts)
			max = max(max, part.toUnionCoords(part.multiblock.getSize(world)));
		Vec3i min = getMin(world);
		return new Vec3i(
				max.getX()-min.getX(),
				max.getY()-min.getY(),
				max.getZ()-min.getZ()
		);
	}

	private Vec3i getMin(@Nullable Level world)
	{
		Vec3i min = Vec3i.ZERO;
		for(TransformedMultiblock part : parts)
		{
			//TODO more intelligent approach?
			final Vec3i size = part.multiblock.getSize(world);
			for(int factorX = 0; factorX < 2; ++factorX)
				for(int factorY = 0; factorY < 2; ++factorY)
					for(int factorZ = 0; factorZ < 2; ++factorZ)
						min = min(min, part.toUnionCoords(new Vec3i(
								size.getX()*factorX,
								size.getY()*factorY,
								size.getZ()*factorZ
						)));
		}
		return min;
	}

	private Vec3i min(Vec3i a, Vec3i b)
	{
		return new Vec3i(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()));
	}

	private Vec3i max(Vec3i a, Vec3i b)
	{
		return new Vec3i(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));
	}

	@Override
	public void disassemble(Level world, BlockPos startPos, boolean mirrored, Direction clickDirectionAtCreation)
	{

	}

	@Override
	public BlockPos getTriggerOffset()
	{
		return BlockPos.ZERO;
	}

	@Override
	public void initializeClient(Consumer<MultiblockManualData> consumer)
	{
		consumer.accept(new UnionMBManualData(parts, getMin(ImmersiveEngineering.proxy.getClientWorld())));
	}

	@Override
	public Component getDisplayName()
	{
		return displayName.get();
	}

	public record TransformedMultiblock(IMultiblock multiblock, Vec3i offset, Rotation rotation)
	{
		public BlockPos toUnionCoords(Vec3i inMultiblockCoords)
		{
			return StructureTemplate.calculateRelativePosition(new StructurePlaceSettings()
					.setRotation(rotation), new BlockPos(inMultiblockCoords)).offset(offset);
		}
	}
}
