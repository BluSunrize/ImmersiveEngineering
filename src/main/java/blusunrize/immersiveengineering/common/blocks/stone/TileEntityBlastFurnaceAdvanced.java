/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBlastFurnacePreheater;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandler;

public class TileEntityBlastFurnaceAdvanced extends TileEntityBlastFurnace
{

	private static final int[] size = {4, 3, 3};

	public TileEntityBlastFurnaceAdvanced()
	{
		super(size);
	}

	@Override
	public void update()
	{
		super.update();
		if(!world.isRemote&&world.getTotalWorldTime()%8==0&&!isDummy())
		{
			TileEntity inventoryFront = Utils.getExistingTileEntity(world, getPos().offset(facing, 2).add(0, -1, 0));
			if(!this.inventory.get(2).isEmpty())
			{
				ItemStack stack = this.inventory.get(2);
				if(inventoryFront!=null)
					stack = Utils.insertStackIntoInventory(inventoryFront, stack, facing.getOpposite());
				this.inventory.set(2, stack);
			}
			TileEntity inventoryBack = Utils.getExistingTileEntity(world, getPos().offset(facing, -2).add(0, -1, 0));
			if(!this.inventory.get(3).isEmpty())
			{
				ItemStack stack = this.inventory.get(3);
				if(inventoryBack!=null)
					stack = Utils.insertStackIntoInventory(inventoryBack, stack, facing);
				this.inventory.set(3, stack);
			}
		}
	}

	@Override
	public float[] getBlockBounds()
	{
		if(pos%9==4||pos==1||pos==10||pos==31)
			return new float[]{0, 0, 0, 1, 1, 1};

		float xMin = 0;
		float yMin = 0;
		float zMin = 0;
		float xMax = 1;
		float yMax = 1;
		float zMax = 1;

		if(pos==7)
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
			if(pos < 9)
				indent = (pos > 2&&pos < 6)?.5f: .3125f;
			else if(pos < 18)
				indent = .5f;
			else if(pos < 27)
				indent = .375f;

			if((pos%9 < 3&&facing==EnumFacing.WEST)||(pos%9 > 5&&facing==EnumFacing.EAST)||(pos%3==2&&facing==EnumFacing.SOUTH)||(pos%3==0&&facing==EnumFacing.NORTH))
				xMin = (1-indent);
			if((pos%9 < 3&&facing==EnumFacing.EAST)||(pos%9 > 5&&facing==EnumFacing.WEST)||(pos%3==2&&facing==EnumFacing.NORTH)||(pos%3==0&&facing==EnumFacing.SOUTH))
				xMax = indent;
			if((pos%9 < 3&&facing==EnumFacing.SOUTH)||(pos%9 > 5&&facing==EnumFacing.NORTH)||(pos%3==2&&facing==EnumFacing.EAST)||(pos%3==0&&facing==EnumFacing.WEST))
				zMin = (1-indent);
			if((pos%9 < 3&&facing==EnumFacing.NORTH)||(pos%9 > 5&&facing==EnumFacing.SOUTH)||(pos%3==2&&facing==EnumFacing.WEST)||(pos%3==0&&facing==EnumFacing.EAST))
				zMax = indent;
		}

		return new float[]{xMin, yMin, zMin, xMax, yMax, zMax};
	}

	@Override
	public ItemStack getOriginalBlock()
	{
		if(this.pos==31)
			return new ItemStack(Blocks.HOPPER);
		return new ItemStack(IEContent.blockStoneDecoration, 1, 2);
	}

	@Override
	protected int getProcessSpeed()
	{
		int i = 1;
		for(int j = 0; j < 2; j++)
		{
			EnumFacing phf = j==0?facing.rotateY(): facing.rotateYCCW();
			BlockPos pos = getPos().add(0, -1, 0).offset(phf, 2);
			TileEntity te = Utils.getExistingTileEntity(world, pos);
			if(te instanceof TileEntityBlastFurnacePreheater)
			{
				if(((TileEntityBlastFurnacePreheater)te).facing==phf.getOpposite())
					i += ((TileEntityBlastFurnacePreheater)te).doSpeedup();
			}
		}
		return i;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if((pos==1||pos==7||pos==31)&&capability==net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			TileEntityBlastFurnaceAdvanced master = (TileEntityBlastFurnaceAdvanced)master();
			if(master==null)
				return false;
			if(pos==31&&facing==EnumFacing.UP)
				return true;
			if(pos==1&&facing==master.facing)
				return true;
			return pos==7&&facing==master.facing.getOpposite();
		}
		return super.hasCapability(capability, facing);
	}

	IItemHandler inputHandler = new IEInventoryHandler(2, this, 0, new boolean[]{true, true}, new boolean[]{false, false});
	IItemHandler outputHandler = new IEInventoryHandler(1, this, 2, new boolean[]{false}, new boolean[]{true});
	IItemHandler slagHandler = new IEInventoryHandler(1, this, 3, new boolean[]{false}, new boolean[]{true});

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if((pos==1||pos==7||pos==31)&&capability==net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			TileEntityBlastFurnaceAdvanced master = (TileEntityBlastFurnaceAdvanced)master();
			if(master==null)
				return null;
			if(pos==31&&facing==EnumFacing.UP)
				return (T)master.inputHandler;
			if(pos==1&&facing==master.facing)
				return (T)master.outputHandler;
			if(pos==7&&facing==master.facing.getOpposite())
				return (T)master.slagHandler;
			return null;
		}
		return super.getCapability(capability, facing);
	}
}