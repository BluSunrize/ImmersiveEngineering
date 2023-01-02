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
import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks.MultiblockManualData;
import blusunrize.immersiveengineering.client.utils.BasicClientProperties;
import blusunrize.immersiveengineering.common.blocks.metal.MetalPressBlockEntity;
import blusunrize.immersiveengineering.common.register.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.util.IELogger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.jetbrains.annotations.NotNull;

import java.util.OptionalDouble;
import java.util.function.Consumer;

public class MetalPressMultiblock extends IETemplateMultiblock
{
	public MetalPressMultiblock()
	{
		super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/metal_press"),
				new BlockPos(1, 1, 0), new BlockPos(1, 1, 0), new BlockPos(3, 3, 1),
				Multiblocks.METAL_PRESS);
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
	public BlockPos multiblockToModelPos(BlockPos posInMultiblock, @NotNull Level level)
	{
		return super.multiblockToModelPos(new BlockPos(
				posInMultiblock.getZ()+1,
				posInMultiblock.getY(),
				1-posInMultiblock.getX()
		), level);
	}

	@Override
	protected void replaceStructureBlock(StructureBlockInfo info, Level world, BlockPos actualPos, boolean mirrored, Direction clickDirection, Vec3i offsetFromMaster)
	{
		Direction mbDirection;
		if(mirrored)
			mbDirection = transformDirection(clickDirection);
		else
			mbDirection = transformDirection(clickDirection.getOpposite());
		BlockState state = Multiblocks.METAL_PRESS.defaultBlockState();
		if(!offsetFromMaster.equals(Vec3i.ZERO))
			state = state.setValue(IEProperties.MULTIBLOCKSLAVE, true);
		world.setBlockAndUpdate(actualPos, state);
		BlockEntity curr = world.getBlockEntity(actualPos);
		if(curr instanceof MetalPressBlockEntity tile)
		{
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

	@Override
	public void initializeClient(Consumer<MultiblockManualData> consumer)
	{
		consumer.accept(new BasicClientProperties(this, OptionalDouble.of(-Mth.HALF_PI)));
	}
}