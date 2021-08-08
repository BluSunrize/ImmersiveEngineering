/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.DirectionalBlockPos;
import blusunrize.immersiveengineering.common.blocks.metal.BlastFurnacePreheaterBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class BlastFurnaceAdvancedBlockEntity extends BlastFurnaceBlockEntity<BlastFurnaceAdvancedBlockEntity>
{
	public BlastFurnaceAdvancedBlockEntity(
			BlockEntityType<BlastFurnaceAdvancedBlockEntity> type, BlockPos pos, BlockState state
	)
	{
		super(IEMultiblocks.ADVANCED_BLAST_FURNACE, type, pos, state);
	}

	private final CapabilityReference<IItemHandler> output = CapabilityReference.forBlockEntityAt(this,
			() -> new DirectionalBlockPos(worldPosition.relative(getFacing(), 2).offset(0, -1, 0), getFacing().getOpposite()),
			CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
	);
	private final CapabilityReference<IItemHandler> slag = CapabilityReference.forBlockEntityAt(this,
			() -> new DirectionalBlockPos(worldPosition.relative(getFacing(), -2).offset(0, -1, 0), getFacing().getOpposite()),
			CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
	);

	@Override
	public void tickServer()
	{
		super.tickServer();
		if(level.getGameTime()%8!=0||isDummy())
			return;
		if(!this.inventory.get(2).isEmpty())
		{
			ItemStack stack = this.inventory.get(2);
			stack = Utils.insertStackIntoInventory(output, stack, false);
			this.inventory.set(2, stack);
		}
		if(!this.inventory.get(3).isEmpty())
		{
			ItemStack stack = this.inventory.get(3);
			stack = Utils.insertStackIntoInventory(slag, stack, false);
			this.inventory.set(3, stack);
		}
	}

	@Nonnull
	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		if((posInMultiblock.getX()==1&&posInMultiblock.getZ()==1)
				||ImmutableSet.of(
				new BlockPos(1, 0, 2),
				new BlockPos(1, 1, 2),
				new BlockPos(1, 3, 1)
		).contains(posInMultiblock))
			return Shapes.block();

		float xMin = 0;
		float yMin = 0;
		float zMin = 0;
		float xMax = 1;
		float yMax = 1;
		float zMax = 1;

		if(new BlockPos(1, 0, 0).equals(posInMultiblock))
		{
			xMin = getFacing().getAxis()==Axis.Z?.1875f: 0;
			xMax = getFacing().getAxis()==Axis.Z?.8125f: 1;
			zMin = getFacing().getAxis()==Axis.X?.1875f: 0;
			zMax = getFacing().getAxis()==Axis.X?.8125f: 1;
			yMax = .8125f;
		}
		else
		{
			float indent = 1;
			if(posInMultiblock.getY()==0)
				indent = posInMultiblock.getZ()==1?.5f: .3125f;
			else if(posInMultiblock.getY()==1)
				indent = .5f;
			else if(posInMultiblock.getY()==2)
				indent = .375f;

			if((this.posInMultiblock.getZ()==2&&getFacing()==Direction.WEST)||
					(posInMultiblock.getZ()==0&&getFacing()==Direction.EAST)||
					(posInMultiblock.getX()==0&&getFacing()==Direction.SOUTH)||
					(posInMultiblock.getX()==2&&getFacing()==Direction.NORTH))
				xMin = (1-indent);
			if((this.posInMultiblock.getZ()==2&&getFacing()==Direction.EAST)||
					(posInMultiblock.getZ()==0&&getFacing()==Direction.WEST)||
					(posInMultiblock.getX()==0&&getFacing()==Direction.NORTH)||
					(posInMultiblock.getX()==2&&getFacing()==Direction.SOUTH))
				xMax = indent;
			if((this.posInMultiblock.getZ()==0&&getFacing()==Direction.SOUTH)||
					(posInMultiblock.getZ()==2&&getFacing()==Direction.NORTH)||
					(posInMultiblock.getX()==2&&getFacing()==Direction.EAST)||
					(posInMultiblock.getX()==0&&getFacing()==Direction.WEST))
				zMin = (1-indent);
			if((this.posInMultiblock.getZ()==0&&getFacing()==Direction.NORTH)||
					(posInMultiblock.getZ()==2&&getFacing()==Direction.SOUTH)||
					(posInMultiblock.getX()==2&&getFacing()==Direction.WEST)||
					(posInMultiblock.getX()==0&&getFacing()==Direction.EAST))
				zMax = indent;
		}

		return Shapes.box(xMin, yMin, zMin, xMax, yMax, zMax);
	}

	@Override
	protected int getProcessSpeed()
	{
		int i = 1;
		for(int j = 0; j < 2; j++)
			i += getFromPreheater(j==0, BlastFurnacePreheaterBlockEntity::doSpeedup, 0);
		return i;
	}

	public <V> V getFromPreheater(boolean left, Function<BlastFurnacePreheaterBlockEntity, V> getter, V orElse)
	{
		return getPreheater(left).map(getter).orElse(orElse);
	}

	public Optional<BlastFurnacePreheaterBlockEntity> getPreheater(boolean left)
	{
		Direction phf = left?getFacing().getClockWise(): getFacing().getCounterClockWise();
		BlockPos pos = getBlockPos().offset(0, -1, 0).relative(phf, 2);
		BlockEntity te = Utils.getExistingTileEntity(level, pos);
		if(te instanceof BlastFurnacePreheaterBlockEntity)
			return Optional.of((BlastFurnacePreheaterBlockEntity)te);
		return Optional.empty();
	}

	private final LazyOptional<IItemHandler> inputHandler = registerConstantCap(
			new IEInventoryHandler(2, this, 0, new boolean[]{true, true}, new boolean[]{false, false})
	);
	private final LazyOptional<IItemHandler> outputHandler = registerConstantCap(
			new IEInventoryHandler(1, this, 2, new boolean[]{false}, new boolean[]{true})
	);
	private final LazyOptional<IItemHandler> slagHandler = registerConstantCap(
			new IEInventoryHandler(1, this, 3, new boolean[]{false}, new boolean[]{true})
	);
	//TODO output is facing, 2
	private static final BlockPos outputOffset = new BlockPos(1, 0, 0);
	private static final BlockPos slagOutputOffset = new BlockPos(1, 0, 2);
	private static final BlockPos inputOffset = new BlockPos(1, 3, 1);
	private static final Set<BlockPos> ioOffsets = ImmutableSet.of(inputOffset, outputOffset, slagOutputOffset);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(ioOffsets.contains(posInMultiblock)&&capability==net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			BlastFurnaceAdvancedBlockEntity master = master();
			if(master!=null)
			{
				if(inputOffset.equals(posInMultiblock)&&facing==Direction.UP)
					return master.inputHandler.cast();
				if(outputOffset.equals(posInMultiblock)&&facing==master.getFacing())
					return master.outputHandler.cast();
				if(slagOutputOffset.equals(posInMultiblock)&&facing==master.getFacing().getOpposite())
					return master.slagHandler.cast();
			}
		}
		return super.getCapability(capability, facing);
	}
}