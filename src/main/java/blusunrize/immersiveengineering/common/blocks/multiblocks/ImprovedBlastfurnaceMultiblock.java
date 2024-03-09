/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public class ImprovedBlastfurnaceMultiblock extends StoneMultiblock
{
	public ImprovedBlastfurnaceMultiblock()
	{
		super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/improved_blast_furnace"),
				new BlockPos(1, 1, 1), new BlockPos(1, 1, 2), new BlockPos(3, 4, 3),
				IEMultiblockLogic.ADV_BLAST_FURNACE);
	}

	@Override
	public float getManualScale()
	{
		return 14;
	}

	@Override
	protected void replaceStructureBlock(
			StructureBlockInfo info, Level world, BlockPos actualPos,
			boolean mirrored, Direction clickDirection, Vec3i offsetFromMaster
	)
	{
		// This is a hack: The improved BF has its "front" on the wrong side, but we cannot change the meaning of the
		// "front" of a multiblock without breaking existing worlds. This should be removed at the next world-breaking
		// opportunity (1.20.5?).
		super.replaceStructureBlock(
				info, world,
				new BlockPos(actualPos.getX()-2*offsetFromMaster.getX(), actualPos.getY(), actualPos.getZ()-2*offsetFromMaster.getZ()),
				mirrored,
				clickDirection.getOpposite(),
				new Vec3i(-offsetFromMaster.getX(), offsetFromMaster.getY(), -offsetFromMaster.getZ())
		);
	}
}