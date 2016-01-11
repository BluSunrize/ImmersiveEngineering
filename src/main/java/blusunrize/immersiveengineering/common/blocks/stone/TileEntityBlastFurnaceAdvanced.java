package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBlastFurnacePreheater;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityBlastFurnaceAdvanced extends TileEntityBlastFurnace
{
	
	@Override
	public void updateEntity()
	{
		super.updateEntity();
		if(!worldObj.isRemote && worldObj.getTotalWorldTime()%8==0 && master()==null)
		{
			TileEntity inventoryFront = this.worldObj.getTileEntity(xCoord+(facing==4?-1:facing==5?1:0),yCoord-1,zCoord+(facing==2?-1:facing==3?1:0));
			if(this.getStackInSlot(2)!=null)
			{
				ItemStack stack = this.getStackInSlot(2);
				if((inventoryFront instanceof ISidedInventory && ((ISidedInventory)inventoryFront).getAccessibleSlotsFromSide(ForgeDirection.OPPOSITES[facing]).length>0)
						||(inventoryFront instanceof IInventory && ((IInventory)inventoryFront).getSizeInventory()>0))
					stack = Utils.insertStackIntoInventory((IInventory)inventoryFront, stack, ForgeDirection.OPPOSITES[facing]);
				this.setInventorySlotContents(2, stack);
			}
			TileEntity inventoryBack = this.worldObj.getTileEntity(xCoord+(facing==4?3:facing==5?-3:0),yCoord-1,zCoord+(facing==2?3:facing==3?-3:0));
			if(this.getStackInSlot(3)!=null)
			{
				ItemStack stack = this.getStackInSlot(3);
				if((inventoryBack instanceof ISidedInventory && ((ISidedInventory)inventoryBack).getAccessibleSlotsFromSide(facing).length>0)
						||(inventoryBack instanceof IInventory && ((IInventory)inventoryBack).getSizeInventory()>0))
					stack = Utils.insertStackIntoInventory((IInventory)inventoryBack, stack, facing);
				this.setInventorySlotContents(3, stack);
			}
		}
	}
	
	@Override
	public float[] getBlockBounds()
	{
		if(pos%9==4 || pos==1 || pos==10 || pos==31)
			return new float[]{0,0,0,1,1,1};

		float xMin = 0;
		float yMin = 0;
		float zMin = 0;
		float xMax = 1;
		float yMax = 1;
		float zMax = 1;

		if(pos==7)
		{
			xMin = facing<4?.1875f:0;
			xMax = facing<4?.8125f:1;
			zMin = facing>3?.1875f:0;
			zMax = facing>3?.8125f:1;
			yMax = .8125f;
		}
		else
		{
			float indent = 1;
			if(pos<9)
				indent = (pos>2&&pos<6)?.5f:.3125f;
			else if(pos<18)
				indent = .5f;
			else if(pos<27)
				indent = .375f;

			if((pos%9<3&&facing==4)||(pos%9>5&&facing==5)||(pos%3==2&&facing==2)||(pos%3==0&&facing==3))
				xMin = (1-indent);
			if((pos%9<3&&facing==5)||(pos%9>5&&facing==4)||(pos%3==2&&facing==3)||(pos%3==0&&facing==2))
				xMax = indent;
			if((pos%9<3&&facing==2)||(pos%9>5&&facing==3)||(pos%3==2&&facing==5)||(pos%3==0&&facing==4))
				zMin = (1-indent);
			if((pos%9<3&&facing==3)||(pos%9>5&&facing==2)||(pos%3==2&&facing==4)||(pos%3==0&&facing==5))
				zMax = indent;
		}

		return new float[]{xMin,yMin,zMin, xMax,yMax,zMax};
	}

	@Override
	public ItemStack getOriginalBlock()
	{
		if(this.pos == 31)
			return new ItemStack(Blocks.hopper);
		return new ItemStack(IEContent.blockStoneDecoration,1,6);
	}

	@Override
	protected int getProcessSpeed()
	{
		int i = 1;
		for(int w=-2; w<=2; w+=4)
		{
			int xx = facing==4?1: facing==5?-1: facing==2?-w:w;
			int zz = facing==2?1: facing==3?-1: facing==4?w:-w;
			int phf = facing<4?(xx<0?4:5): (zz<0?2:3);
			if(worldObj.getTileEntity(xCoord+xx, yCoord-1, zCoord+zz) instanceof TileEntityBlastFurnacePreheater)
			{
				if( ((TileEntityBlastFurnacePreheater)worldObj.getTileEntity(xCoord+xx, yCoord-1, zCoord+zz)).facing==phf)
					i += ((TileEntityBlastFurnacePreheater)worldObj.getTileEntity(xCoord+xx, yCoord-1, zCoord+zz)).doSpeedup();
			}
		}
		return i;
	}

	@Override
	public String getInventoryName()
	{
		return "IEBlastFurnaceAdvanced";
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		if(!formed)
			return new int[0];
		if(pos==31 && side==1)
			return new int[]{0,1};
		if(pos==1 && side==ForgeDirection.OPPOSITES[facing])
			return new int[]{2};
		if(pos==7 && side==facing)
			return new int[]{3};
		return new int[0];
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side)
	{
		if(!formed)
			return false;
		TileEntityBlastFurnace master = master();
		if(master!=null)
			return master.canInsertItem(slot,stack,side);
		return (slot==0||slot==1) && isItemValidForSlot(slot,stack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side)
	{
		if(!formed)
			return false;
		TileEntityBlastFurnace master = master();
		if(master!=null)
			return master.canExtractItem(slot,stack,side);
		return slot==2||slot==3;
	}



	@Override
	public void disassemble()
	{
		if(formed && !worldObj.isRemote)
		{
			int startX = xCoord - offset[0];
			int startY = yCoord - offset[1];
			int startZ = zCoord - offset[2];
			if(!(offset[0]==0&&offset[1]==0&&offset[2]==0) && !(worldObj.getTileEntity(startX, startY, startZ) instanceof TileEntityBlastFurnaceAdvanced))
				return;

			int xMin= facing==5?-2: facing==4?0:-1;
			int xMax= facing==5? 0: facing==4?2: 1;
			int zMin= facing==3?-2: facing==2?0:-1;
			int zMax= facing==3? 0: facing==2?2: 1;
			for(int yy=-1;yy<=2;yy++)
				for(int xx=xMin;xx<=xMax;xx++)
					for(int zz=zMin;zz<=zMax;zz++)
						if(yy!=2 || (xx>xMin&&xx<xMax && zz>zMin&&zz<zMax))
						{
							ItemStack s = null;
							TileEntity te = worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz);
							if(te instanceof TileEntityBlastFurnaceAdvanced)
							{
								s = ((TileEntityBlastFurnaceAdvanced)te).getOriginalBlock();
								((TileEntityBlastFurnaceAdvanced)te).formed=false;
							}
							if(startX+xx==xCoord && startY+yy==yCoord && startZ+zz==zCoord)
								s = this.getOriginalBlock();
							if(s!=null && Block.getBlockFromItem(s.getItem())!=null)
							{
								if(startX+xx==xCoord && startY+yy==yCoord && startZ+zz==zCoord)
									worldObj.spawnEntityInWorld(new EntityItem(worldObj, xCoord+.5,yCoord+.5,zCoord+.5, s));
								else
								{
									if(Block.getBlockFromItem(s.getItem())==IEContent.blockStoneDevice)
										worldObj.setBlockToAir(startX+xx,startY+yy,startZ+zz);
									worldObj.setBlock(startX+xx,startY+yy,startZ+zz, Block.getBlockFromItem(s.getItem()), s.getItemDamage(), 0x3);
								}
							}
						}
		}
	}
}