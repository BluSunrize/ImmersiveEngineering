/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.api.DirectionalBlockPos;
import blusunrize.immersiveengineering.common.blocks.metal.BlastFurnacePreheaterTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockBlastFurnaceAdvanced;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlastFurnaceAdvancedTileEntity extends BlastFurnaceTileEntity
{
	public static TileEntityType<BlastFurnaceAdvancedTileEntity> TYPE;

	public BlastFurnaceAdvancedTileEntity()
	{
		super(MultiblockBlastFurnaceAdvanced.instance, TYPE);
	}

	private CapabilityReference<IItemHandler> output = CapabilityReference.forTileEntity(this,
			() -> new DirectionalBlockPos(BlockPos.ORIGIN.offset(facing, 2).add(0, -1, 0), facing.getOpposite()),
			CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
	);
	private CapabilityReference<IItemHandler> slag = CapabilityReference.forTileEntity(this,
			() -> new DirectionalBlockPos(BlockPos.ORIGIN.offset(facing, -2).add(0, -1, 0), facing.getOpposite()),
			CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
	);

	@Override
	public void tick()
	{
		super.tick();
		if(!world.isRemote&&world.getGameTime()%8==0&&!isDummy())
		{
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
	}

	@Override
	public float[] getBlockBounds()
	{
		if(posInMultiblock%9==4||posInMultiblock==1||posInMultiblock==10||posInMultiblock==31)
			return new float[]{0, 0, 0, 1, 1, 1};

		float xMin = 0;
		float yMin = 0;
		float zMin = 0;
		float xMax = 1;
		float yMax = 1;
		float zMax = 1;

		if(posInMultiblock==7)
		{
			xMin = facing.getAxis()==Axis.Z?.1875f: 0;
			xMax = facing.getAxis()==Axis.Z?.8125f: 1;
			zMin = facing.getAxis()==Axis.X?.1875f: 0;
			zMax = facing.getAxis()==Axis.X?.8125f: 1;
			yMax = .8125f;
		}
		else
		{
			float indent = 1;
			if(posInMultiblock < 9)
				indent = (posInMultiblock > 2&&posInMultiblock < 6)?.5f: .3125f;
			else if(posInMultiblock < 18)
				indent = .5f;
			else if(posInMultiblock < 27)
				indent = .375f;

			if((posInMultiblock%9 < 3&&facing==Direction.WEST)||(posInMultiblock%9 > 5&&facing==Direction.EAST)||(posInMultiblock%3==2&&facing==Direction.SOUTH)||(posInMultiblock%3==0&&facing==Direction.NORTH))
				xMin = (1-indent);
			if((posInMultiblock%9 < 3&&facing==Direction.EAST)||(posInMultiblock%9 > 5&&facing==Direction.WEST)||(posInMultiblock%3==2&&facing==Direction.NORTH)||(posInMultiblock%3==0&&facing==Direction.SOUTH))
				xMax = indent;
			if((posInMultiblock%9 < 3&&facing==Direction.SOUTH)||(posInMultiblock%9 > 5&&facing==Direction.NORTH)||(posInMultiblock%3==2&&facing==Direction.EAST)||(posInMultiblock%3==0&&facing==Direction.WEST))
				zMin = (1-indent);
			if((posInMultiblock%9 < 3&&facing==Direction.NORTH)||(posInMultiblock%9 > 5&&facing==Direction.SOUTH)||(posInMultiblock%3==2&&facing==Direction.WEST)||(posInMultiblock%3==0&&facing==Direction.EAST))
				zMax = indent;
		}

		return new float[]{xMin, yMin, zMin, xMax, yMax, zMax};
	}

	@Override
	protected int getProcessSpeed()
	{
		int i = 1;
		for(int j = 0; j < 2; j++)
		{
			Direction phf = j==0?facing.rotateY(): facing.rotateYCCW();
			BlockPos pos = getPos().add(0, -1, 0).offset(phf, 2);
			TileEntity te = Utils.getExistingTileEntity(world, pos);
			if(te instanceof BlastFurnacePreheaterTileEntity)
			{
				if(((BlastFurnacePreheaterTileEntity)te).facing==phf.getOpposite())
					i += ((BlastFurnacePreheaterTileEntity)te).doSpeedup();
			}
		}
		return i;
	}

	private LazyOptional<IItemHandler> inputHandler = registerConstantCap(
			new IEInventoryHandler(2, this, 0, new boolean[]{true, true}, new boolean[]{false, false})
	);
	private LazyOptional<IItemHandler> outputHandler = registerConstantCap(
			new IEInventoryHandler(1, this, 2, new boolean[]{false}, new boolean[]{true})
	);
	private LazyOptional<IItemHandler> slagHandler = registerConstantCap(
			new IEInventoryHandler(1, this, 3, new boolean[]{false}, new boolean[]{true})
	);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if((posInMultiblock==1||posInMultiblock==7||posInMultiblock==31)&&capability==net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			BlastFurnaceAdvancedTileEntity master = (BlastFurnaceAdvancedTileEntity)master();
			if(master==null)
				return null;
			if(posInMultiblock==31&&facing==Direction.UP)
				return master.inputHandler.cast();
			if(posInMultiblock==1&&facing==master.facing)
				return master.outputHandler.cast();
			if(posInMultiblock==7&&facing==master.facing.getOpposite())
				return master.slagHandler.cast();
			return LazyOptional.empty();
		}
		return super.getCapability(capability, facing);
	}
}