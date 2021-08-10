/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.metal.MetalPressBlockEntity;
import blusunrize.immersiveengineering.common.register.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.util.IELogger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public class MetalPressMultiblock extends IETemplateMultiblock
{
	public MetalPressMultiblock()
	{
		super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/metal_press"),
				new BlockPos(1, 1, 0), new BlockPos(1, 1, 0), new BlockPos(3, 3, 1),
				Multiblocks.metalPress);
	}

	@Override
	public float getManualScale()
	{
		return 13;
	}

	@Override
	public Direction transformDirection(Direction original)
	{
		return original.getClockWise();
	}

	@Override
	public Direction untransformDirection(Direction transformed)
	{
		return transformed.getCounterClockWise();
	}

	@Override
	public BlockPos multiblockToModelPos(BlockPos posInMultiblock)
	{
		return super.multiblockToModelPos(new BlockPos(
				posInMultiblock.getZ()+1,
				posInMultiblock.getY(),
				1-posInMultiblock.getX()
		));
	}

	@Override
	protected void replaceStructureBlock(StructureBlockInfo info, Level world, BlockPos actualPos, boolean mirrored, Direction clickDirection, Vec3i offsetFromMaster)
	{
		Direction mbDirection;
		if(mirrored)
			mbDirection = transformDirection(clickDirection);
		else
			mbDirection = transformDirection(clickDirection.getOpposite());
		BlockState state = Multiblocks.metalPress.defaultBlockState();
		if(!offsetFromMaster.equals(Vec3i.ZERO))
			state = state.setValue(IEProperties.MULTIBLOCKSLAVE, true);
		world.setBlockAndUpdate(actualPos, state);
		BlockEntity curr = world.getBlockEntity(actualPos);
		if(curr instanceof MetalPressBlockEntity)
		{
			MetalPressBlockEntity tile = (MetalPressBlockEntity)curr;
			tile.formed = true;
			tile.offsetToMaster = new BlockPos(offsetFromMaster);
			tile.posInMultiblock = info.pos;
			tile.setFacing(mbDirection);
			tile.setChanged();
			world.blockEvent(actualPos, world.getBlockState(actualPos).getBlock(), 255, 0);
		}
		else
			IELogger.logger.error("Expected metal press TE at {} during placement", actualPos);
	}
}