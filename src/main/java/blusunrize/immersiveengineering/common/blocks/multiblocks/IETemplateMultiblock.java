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
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

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
	protected void replaceStructureBlock(BlockInfo info, World world, BlockPos actualPos, boolean mirrored, Direction clickDirection, Vector3i offsetFromMaster)
	{
		BlockState state = baseState.get();
		if(!offsetFromMaster.equals(Vector3i.NULL_VECTOR))
			state = state.with(IEProperties.MULTIBLOCKSLAVE, true);
		world.setBlockState(actualPos, state);
		TileEntity curr = world.getTileEntity(actualPos);
		if(curr instanceof MultiblockPartTileEntity)
		{
			MultiblockPartTileEntity tile = (MultiblockPartTileEntity)curr;
			tile.formed = true;
			tile.offsetToMaster = new BlockPos(offsetFromMaster);
			tile.posInMultiblock = info.pos;
			if(state.hasProperty(IEProperties.MIRRORED))
				tile.setMirrored(mirrored);
			tile.setFacing(transformDirection(clickDirection.getOpposite()));
			tile.markDirty();
			world.addBlockEvent(actualPos, world.getBlockState(actualPos).getBlock(), 255, 0);
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
	public Vector3i getSize(@Nullable World world)
	{
		return size;
	}

	@Nonnull
	@Override
	protected Template getTemplate(@Nullable World world)
	{
		Template result = super.getTemplate(world);
		Preconditions.checkState(
				result.getSize().equals(size),
				"Wrong template size for multiblock %s, template size: %s",
				getTemplateLocation(), result.getSize()
		);
		return result;
	}

	@Override
	protected void prepareBlockForDisassembly(World world, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof MultiblockPartTileEntity)
			((MultiblockPartTileEntity<?>)te).formed = false;
		else if (te != null)
			IELogger.logger.error("Expected multiblock TE at {}, got {}", pos, te);
	}
}
