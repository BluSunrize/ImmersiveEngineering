/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tags.Tag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

import java.util.Map;
import java.util.function.Supplier;

public abstract class IETemplateMultiblock extends TemplateMultiblock
{
	private final Supplier<BlockState> baseState;

	public IETemplateMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, Map<Block, Tag<Block>> tags, Supplier<BlockState> baseState)
	{
		super(loc, masterFromOrigin, triggerFromOrigin, tags);
		this.baseState = baseState;
	}

	public IETemplateMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, Supplier<BlockState> baseState)
	{
		this(loc, masterFromOrigin, triggerFromOrigin, ImmutableMap.of(), baseState);
	}

	@Override
	protected void replaceStructureBlock(BlockInfo info, World world, BlockPos actualPos, boolean mirrored, Direction clickDirection, Vec3i offsetFromMaster)
	{
		world.setBlockState(actualPos, baseState.get());
		TileEntity curr = world.getTileEntity(actualPos);
		if(curr instanceof MultiblockPartTileEntity)
		{
			MultiblockPartTileEntity tile = (MultiblockPartTileEntity)curr;
			tile.formed = true;
			tile.offsetToMaster = new BlockPos(offsetFromMaster);
			tile.posInMultiblock = info.pos;
			tile.setMirrored(mirrored);
			tile.setFacing(clickDirection.getOpposite());
			tile.markDirty();
			world.addBlockEvent(actualPos, world.getBlockState(actualPos).getBlock(), 255, 0);
		}
	}
}
