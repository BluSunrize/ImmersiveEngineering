package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockArcFurnace;
import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityArcFurnace extends TileEntityMultiblockPart implements IEnergyReceiver, ISidedInventory
{
	public int facing = 3;
	public EnergyStorage energyStorage = new EnergyStorage(32000);

	public TileEntityArcFurnace master()
	{
		if(offset[0]==0&&offset[1]==0&&offset[2]==0)
			return null;
		TileEntity te = worldObj.getTileEntity(xCoord-offset[0], yCoord-offset[1], zCoord-offset[2]);
		return te instanceof TileEntityArcFurnace?(TileEntityArcFurnace)te : null;
	}

	@Override
	public ItemStack getOriginalBlock()
	{
		ItemStack s = MultiblockArcFurnace.instance.getStructureManual()[pos/25][pos%5][pos%25/5];
		return s!=null?s.copy():null;
	}

	@Override
	public void updateEntity()
	{
		if(!formed || pos!=17)
			return;


	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = nbt.getInteger("facing");
		energyStorage.readFromNBT(nbt);
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", facing);
		energyStorage.writeToNBT(nbt);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(pos==62)
			return AxisAlignedBB.getBoundingBox(xCoord-2,yCoord-2,zCoord-2, xCoord+3,yCoord+3,zCoord+3);
		return AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord, xCoord,yCoord,zCoord);
	}
	@Override
	public double getMaxRenderDistanceSquared()
	{
		return super.getMaxRenderDistanceSquared()*Config.getDouble("increasedTileRenderdistance");
		//		if(Config.getBoolean("increasedTileRenderdistance"))
		//			return super.getMaxRenderDistanceSquared()*1.5;
		//		return super.getMaxRenderDistanceSquared();
	}
	@Override
	public float[] getBlockBounds()
	{
		int fl = facing;
		int fw = facing;
		if(mirrored)
			fw = ForgeDirection.OPPOSITES[fw];

		if(pos<25)
		{
			if(pos==1||pos==3)
				return new float[]{fl==4?.4375f:0,0,fl==2?.4375f:0, fl==5?.5625f:1,.5f,fl==3?.5625f:1};
			else if(pos!=0 && pos!=2 && pos<20 && (pos>9?(pos%5!=0&&pos%5!=4):true))
				return new float[]{0,0,0,1,.5f,1};
		}
		else if(pos==25)
			return new float[]{fl==5?.5f:0,0,fl==3?.5f:0, fl==4?.5f:1,1,fl==2?.5f:1};
		else if(pos<50)
		{
			if(pos%5==0)
				return new float[]{fw==3?.125f:0,0,fw==4?.125f:0, fw==2?.875f:1,pos%25/5==3?.5f:1,fw==5?.875f:1};
			else if(pos%5==4)
				return new float[]{fw==2?.125f:0,0,fw==5?.125f:0, fw==3?.875f:1,pos%25/5==3?.5f:1,fw==4?.875f:1};
		}
		else if(pos<75)
		{
			if(pos==52)
				return new float[]{fl<4?-.5f:0,0,fl>3?-.5f:0, fl<4?1.5f:1,1,fl>3?1.5f:1};
			else if(pos==60)
				return new float[]{fw==3?.125f:0,0,fw==4?.125f:0, fw==2?.875f:1,1,fw==5?.875f:1};
			else if(pos==64)
				return new float[]{fw==2?.375f:0,0,fw==5?.375f:0, fw==3?.625f:1,1,fw==4?.625f:1};
			else if(pos==70)
				return new float[]{fw==3?.5f:0,0,fw==4?.5f:0, fw==2?.5f:1,1,fw==5?.5f:1};
			else if(pos==74)
				return new float[]{fw==2?.5f:0,0,fw==5?.5f:0, fw==3?.5f:1,1,fw==4?.5f:1};
			else
				return new float[]{0,0,0,1,1,1};
		}
		else if(pos==112)
			return new float[]{fl==4?.0625f:0,0,fl==2?.0625f:0, fl==5?.9375f:1,1,fl==3?.9375f:1};
		else if(pos==117)
			return new float[]{0,.6875f,0,1,1,1};
		else if(pos%25/5==4)
		{
			if(pos%5==1)
				return new float[]{fw==3?.5f:0,0,fw==4?.5f:0, fw==2?.5f:1,1,fw==5?.5f:1};
			else if(pos%5==2)
				return new float[]{fl==5?.25f:0,0,fl==3?.25f:0, fl==4?.75f:1,1,fl==2?.75f:1};
			else if(pos%5==3)
				return new float[]{fw==2?.5f:0,0,fw==5?.5f:0, fw==3?.5f:1,1,fw==4?.5f:1};
		}
		return new float[]{0,0,0,1,1,1};
	}

	@Override
	public void invalidate()
	{
		super.invalidate();

		if(formed && !worldObj.isRemote)
		{
			int f = facing;
			TileEntity master = master();
			if(master==null)
				master = this;

			int startX = master.xCoord;
			int startY = master.yCoord;
			int startZ = master.zCoord;

			for(int h=-2;h<=2;h++)
				for(int l=-2;l<=2;l++)
					for(int w=-2;w<=2;w++)
					{
						int xx = (f==4?l: f==5?-l: f==2?-w : w);
						int yy = h;
						int zz = (f==2?l: f==3?-l: f==5?-w : w);

						ItemStack s = null;
						if(worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz) instanceof TileEntityArcFurnace)
						{
							s = ((TileEntityArcFurnace)worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz)).getOriginalBlock();
							((TileEntityArcFurnace)worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz)).formed=false;
						}
						if(startX+xx==xCoord && startY+yy==yCoord && startZ+zz==zCoord)
							s = this.getOriginalBlock();
						if(s!=null && Block.getBlockFromItem(s.getItem())!=null)
						{
							if(startX+xx==xCoord && startY+yy==yCoord && startZ+zz==zCoord)
								worldObj.spawnEntityInWorld(new EntityItem(worldObj, xCoord+.5,yCoord+.5,zCoord+.5, s));
							else
							{
								if(s.getItem() == Items.cauldron)
									worldObj.setBlock(startX+xx,startY+yy,startZ+zz, Blocks.cauldron);
								else
								{
									if(Block.getBlockFromItem(s.getItem())==IEContent.blockMetalMultiblocks)
										worldObj.setBlockToAir(startX+xx,startY+yy,startZ+zz);
									worldObj.setBlock(startX+xx,startY+yy,startZ+zz, Block.getBlockFromItem(s.getItem()), s.getItemDamage(), 0x3);
								}
							}
						}
					}
		}
	}

	@Override
	public int getSizeInventory()
	{
		if(!formed || pos!=27)
			return 0;
		return 1;
	}
	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return null;
	}
	@Override
	public ItemStack decrStackSize(int slot, int amount)
	{
		return null;
	}
	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		return null;
	}
	@Override
	public void setInventorySlotContents(int slot, ItemStack stack)
	{
		if(!formed || pos!=27)
			return;
	}
	@Override
	public String getInventoryName()
	{
		return "IEArcFurnace";
	}
	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}
	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}
	@Override
	public boolean isUseableByPlayer(EntityPlayer p_70300_1_)
	{
		return true;
	}
	@Override
	public void openInventory()
	{
	}
	@Override
	public void closeInventory()
	{
	}
	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack)
	{
		if(!formed || pos!=27)
			return false;
		return false;
	}
	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return formed&&pos==27&&side==ForgeDirection.UP.ordinal()?new int[]{0}: new int[0];
	}
	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side)
	{
		return formed&&pos==27&&side==ForgeDirection.UP.ordinal();
	}
	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side)
	{
		return false;
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return formed && pos==20 && from==ForgeDirection.UP;
	}
	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		if(formed && this.master()!=null && pos==20 && from==ForgeDirection.UP)
		{
			TileEntityArcFurnace master = master();
			int rec = master.energyStorage.receiveEnergy(maxReceive, simulate);
			master.markDirty();
			worldObj.markBlockForUpdate(master.xCoord, master.yCoord, master.zCoord);
			return rec;
		}
		return 0;
	}
	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		if(this.master()!=null)
			return this.master().energyStorage.getEnergyStored();
		return energyStorage.getEnergyStored();
	}
	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		if(this.master()!=null)
			return this.master().energyStorage.getMaxEnergyStored();
		return energyStorage.getMaxEnergyStored();
	}
}