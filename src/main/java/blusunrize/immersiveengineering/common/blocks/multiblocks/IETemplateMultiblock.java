/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public abstract class IETemplateMultiblock extends TemplateMultiblock
{
	private final Supplier<BlockState> baseState;

	public IETemplateMultiblock(
			ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, BlockPos size,
			Supplier<BlockState> baseState
	)
	{
		super(loc, masterFromOrigin, triggerFromOrigin, size, ImmutableMap.of());
		this.baseState = baseState;
	}

	@Override
	protected void replaceStructureBlock(StructureBlockInfo info, Level world, BlockPos actualPos, boolean mirrored, Direction clickDirection, Vec3i offsetFromMaster)
	{
		BlockState state = baseState.get();
		if(!offsetFromMaster.equals(Vec3i.ZERO))
			state = state.setValue(IEProperties.MULTIBLOCKSLAVE, true);
		world.setBlockAndUpdate(actualPos, state);
		BlockEntity curr = world.getBlockEntity(actualPos);
		if(curr instanceof MultiblockPartTileEntity)
		{
			MultiblockPartTileEntity tile = (MultiblockPartTileEntity)curr;
			tile.formed = true;
			tile.offsetToMaster = new BlockPos(offsetFromMaster);
			tile.posInMultiblock = info.pos;
			if(state.hasProperty(IEProperties.MIRRORED))
				tile.setMirrored(mirrored);
			tile.setFacing(transformDirection(clickDirection.getOpposite()));
			tile.setChanged();
			world.blockEvent(actualPos, world.getBlockState(actualPos).getBlock(), 255, 0);
		}
		else
			IELogger.logger.error("Expected MB TE at {} during placement", actualPos);
	}

	public Direction transformDirection(Direction original)
	{
		return original;
	}

	public Direction untransformDirection(Direction transformed)
	{
		return transformed;
	}

	public BlockPos multiblockToModelPos(BlockPos posInMultiblock)
	{
		return posInMultiblock.subtract(masterFromOrigin);
	}

	@Override
	public Vec3i getSize(@Nullable Level world)
	{
		return size;
	}

	@Nonnull
	@Override
	protected StructureTemplate getTemplate(@Nullable Level world)
	{
		StructureTemplate result = super.getTemplate(world);
		Preconditions.checkState(
				result.getSize().equals(size),
				"Wrong template size for multiblock %s, template size: %s",
				getTemplateLocation(), result.getSize()
		);
		return result;
	}

	@Override
	protected void prepareBlockForDisassembly(Level world, BlockPos pos)
	{
		BlockEntity te = world.getBlockEntity(pos);
		if(te instanceof MultiblockPartTileEntity)
			((MultiblockPartTileEntity<?>)te).formed = false;
		else if (te != null)
			IELogger.logger.error("Expected multiblock TE at {}, got {}", pos, te);
	}
}
