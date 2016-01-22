package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.List;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockMetalPress;
import blusunrize.immersiveengineering.common.util.Utils;
import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityMetalPress extends TileEntityMultiblockPart implements ISidedInventory, IEnergyReceiver
{
	public int facing = 2;
	public EnergyStorage energyStorage = new EnergyStorage(16000);
	public ItemStack[] inventory = new ItemStack[3];
	public MetalPressRecipe[] curRecipes = new MetalPressRecipe[3];
	public int[] process = new int[3];
	public ItemStack mold = null;
	public boolean active;
	public static final int MAX_PROCESS = 120;
	public int stopped = -1;
	private int stoppedReqSize = -1;

	@Override
	public TileEntityMetalPress master()
	{
		if(offset[0]==0&&offset[1]==0&&offset[2]==0)
			return null;
		TileEntity te = worldObj.getTileEntity(xCoord-offset[0], yCoord-offset[1], zCoord-offset[2]);
		return te instanceof TileEntityMetalPress?(TileEntityMetalPress)te : null;
	}

	@Override
	public ItemStack getOriginalBlock()
	{
		if(pos<0)
			return null;
		ItemStack s = null;
		try{
			s = MultiblockMetalPress.instance.getStructureManual() [pos/3][pos%3][0];
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return s!=null?s.copy():null;
	}
	@Override
	public void updateEntity()
	{
		if(!formed || pos!=4)
			return;
		if (worldObj.isRemote)
		{
			if (!active)
				return;
			for (int i = 0;i<process.length;i++)
			{
				if (process[i]>=0&&stopped!=i)
				{
					process[i]++;
					if (process[i]>MAX_PROCESS)
					{
						inventory[i] = null;
						process[i] = -1;
					}
				}
			}
			return;
		}

		boolean update = false;
		for(int i=0; i<inventory.length; i++)
			if(stopped!=i&&inventory[i]!=null)
			{
				if(process[i]>=MAX_PROCESS)
				{
					ItemStack output = inventory[i].copy();
					TileEntity inventoryTile = this.worldObj.getTileEntity(xCoord+(facing==4?-2:facing==5?2:0),yCoord,zCoord+(facing==2?-2:facing==3?2:0));
					if(inventoryTile instanceof IInventory)
						output = Utils.insertStackIntoInventory((IInventory)inventoryTile, output, ForgeDirection.OPPOSITES[facing]);
					if(output!=null)
					{
						ForgeDirection fd = ForgeDirection.getOrientation(facing);
						EntityItem ei = new EntityItem(worldObj, xCoord+.5+(facing==4?-2:facing==5?2:0),yCoord,zCoord+.5+(facing==2?-2:facing==3?2:0), output.copy());
						ei.motionX = (0.075F * fd.offsetX);
						ei.motionY = 0.025000000372529D;
						ei.motionZ = (0.075F * fd.offsetZ);
						this.worldObj.spawnEntityInWorld(ei);
					}
					curRecipes[i] = null;
					process[i]=-1;
					inventory[i]=null;
					update = true;
				}
				if(curRecipes[i]==null)
					curRecipes[i] = MetalPressRecipe.findRecipe(mold, inventory[i], true);

				int perTick = curRecipes[i]!=null?curRecipes[i].energy/MAX_PROCESS:0;
				if((perTick==0 || this.energyStorage.extractEnergy(perTick, true)==perTick)&&process[i]>=0)
				{
					this.energyStorage.extractEnergy(perTick, false);
					if(process[i]++==60 && curRecipes[i]!=null)
					{
						this.inventory[i] = curRecipes[i].output.copy();
						update = true;
					}
					if (!active)
					{
						active = true;
						update = true;
					}
				}
				else if (active)
				{
					active = false;
					update = true;
				}
			}
			else if (stopped==i)
			{
				if (stoppedReqSize<0)
				{
					MetalPressRecipe recipe = MetalPressRecipe.findRecipe(mold, inventory[i], false);
					if (recipe!=null)
						stoppedReqSize = recipe.inputSize;
					else
					{
						stopped = -1;
						update = true;
						continue;
					}
				}
				if (stoppedReqSize<=inventory[i].stackSize)
					stopped = -1;
			}
		if(update)
		{
			this.markDirty();
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	public int getNextProcessID()
	{
		if(master()!=null)
			return master().getNextProcessID();
		int lowestProcess = Integer.MAX_VALUE;
		for(int i=0; i<inventory.length; i++)
			if(inventory[i]==null)
			{
				if (lowestProcess==Integer.MAX_VALUE)
				{
					lowestProcess = 200;
					for(int j=0; j<inventory.length; j++)
					{
						if(inventory[j]!=null && process[j]<lowestProcess && process[j]>=0)
							lowestProcess = process[j];
					}
				}
				if(lowestProcess>40)
					return i;
				else
					return -1;
			}
		return -1;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = nbt.getInteger("facing");
		int[] processTmp = nbt.getIntArray("process");
		inventory = Utils.readInventory(nbt.getTagList("inventory", 10), 3);
		for (int i = 0;i<processTmp.length;i++)
			if ((process[i]<0^processTmp[i]<0)||!descPacket)
				process[i] = processTmp[i];
		energyStorage.readFromNBT(nbt);
		mold = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("mold"));
		if (descPacket)
			active = nbt.getBoolean("active");
		if (nbt.hasKey("stoppedSlot"))
			stopped = nbt.getInteger("stoppedSlot");
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", facing);
		nbt.setIntArray("process", process);
		energyStorage.writeToNBT(nbt);
		nbt.setTag("inventory", Utils.writeInventory(inventory));
		if(this.mold!=null)
			nbt.setTag("mold", this.mold.writeToNBT(new NBTTagCompound()));
		if (descPacket)
			nbt.setBoolean("active", active);
		nbt.setInteger("stoppedSlot", stopped);
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		return false;
	}

	@SideOnly(Side.CLIENT)
	private AxisAlignedBB renderAABB;
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(renderAABB==null)
			if(pos==4)
				renderAABB = AxisAlignedBB.getBoundingBox(xCoord-1,yCoord-1,zCoord-1, xCoord+2,yCoord+2,zCoord+2);
			else
				renderAABB = AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord, xCoord,yCoord,zCoord);
		return renderAABB;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return super.getMaxRenderDistanceSquared()*Config.getDouble("increasedTileRenderdistance");
	}
	@Override
	public float[] getBlockBounds()
	{
		if(pos<3)
			return new float[]{0,0,0,1,1,1};

		float xMin = 0;
		float yMin = 0;
		float zMin = 0;
		float xMax = 1;
		float yMax = 1;
		float zMax = 1;

		if(pos%3==0||pos%3==2)
			yMax = .125f;

		return new float[]{xMin,yMin,zMin, xMax,yMax,zMax};
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

			for(int yy=-1;yy<=1;yy++)
				for(int l=-1; l<=1; l++)
				{
					int xx = f>3?l:0;
					int zz = f<4?l:0;
					ItemStack s = null;
					TileEntity te = worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz);
					if(te instanceof TileEntityMetalPress)
					{
						s = ((TileEntityMetalPress)te).getOriginalBlock();
						((TileEntityMetalPress)te).formed=false;
					}
					if(startX+xx==xCoord && startY+yy==yCoord && startZ+zz==zCoord)
						s = this.getOriginalBlock();
					if(s!=null && Block.getBlockFromItem(s.getItem())!=null)
					{
						if(startX+xx==xCoord && startY+yy==yCoord && startZ+zz==zCoord)
							worldObj.spawnEntityInWorld(new EntityItem(worldObj, xCoord+.5,yCoord+.5,zCoord+.5, s));
						else
						{
							if(Block.getBlockFromItem(s.getItem())==IEContent.blockMetalMultiblocks)
								worldObj.setBlockToAir(startX+xx,startY+yy,startZ+zz);
							int meta = s.getItemDamage();
							worldObj.setBlock(startX+xx,startY+yy,startZ+zz, Block.getBlockFromItem(s.getItem()), meta, 0x3);
						}
						TileEntity tile = worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz);
						if(tile instanceof TileEntityConveyorBelt)
							((TileEntityConveyorBelt)tile).facing = ForgeDirection.OPPOSITES[f];
					}
				}
		}
	}

	@Override
	public int getSizeInventory()
	{
		if(!formed)
			return 0;
		return inventory.length;
	}
	@Override
	public ItemStack getStackInSlot(int slot)
	{
		if(!formed)
			return null;
		TileEntityMetalPress master = master();
		if(master!=null)
			return master.getStackInSlot(slot);
		if(slot<inventory.length)
			return inventory[slot];
		return null;
	}
	@Override
	public ItemStack decrStackSize(int slot, int amount)
	{
		if(!formed)
			return null;
		TileEntityMetalPress master = master();
		if(master!=null)
			return master.decrStackSize(slot,amount);
		ItemStack stack = getStackInSlot(slot);
		if(stack != null)
			if(stack.stackSize <= amount)
				setInventorySlotContents(slot, null);
			else
			{
				stack = stack.splitStack(amount);
				if(stack.stackSize == 0)
					setInventorySlotContents(slot, null);
			}
		this.markDirty();
		return stack;
	}
	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		if(!formed)
			return null;
		TileEntityMetalPress master = master();
		if(master!=null)
			return master.getStackInSlotOnClosing(slot);
		ItemStack stack = getStackInSlot(slot);
		if (stack != null)
			setInventorySlotContents(slot, null);
		return stack;
	}
	@Override
	public void setInventorySlotContents(int slot, ItemStack stack)
	{
		if(!formed)
			return;
		TileEntityMetalPress master = master();
		if(master!=null)
		{
			master.setInventorySlotContents(slot,stack);
			return;
		}
		int stackLimit = getInventoryStackLimit();
		inventory[slot] = stack;
		process[slot] = stack!=null?0:-1;
		if (stack != null && stack.stackSize > stackLimit)
			stack.stackSize = stackLimit;
		MetalPressRecipe recipe = MetalPressRecipe.findRecipe(mold, stack, false);
		if (recipe!=null&&recipe.inputSize>inventory[slot].stackSize)
		{
			stopped = slot;
			stoppedReqSize = recipe.inputSize;
		}
		this.markDirty();
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	@Override
	public String getInventoryName()
	{
		return "IEMetalPress";
	}
	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}
	@Override
	public int getInventoryStackLimit()
	{
		TileEntityMetalPress master = master();
		if(master!=null)
			return master.getInventoryStackLimit();
		if (stopped>=0)
		{
			//some stack is waiting for more items to be able to process
			ItemStack input = inventory[stopped];
			MetalPressRecipe recipe = MetalPressRecipe.findRecipe(mold, input, false);
			if (recipe!=null)
				return recipe.inputSize;
			else
			{
				stopped = -1;
				stoppedReqSize = -1;
			}
		}
		return 1;
	}
	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		return worldObj.getTileEntity(xCoord,yCoord,zCoord)!=this?false:formed&&player.getDistanceSq(xCoord+.5D,yCoord+.5D,zCoord+.5D)<=64;
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
		if(!formed||stack==null)
			return false;
		TileEntityMetalPress master = master();
		if(master!=null)
			return master.isItemValidForSlot(slot,stack);
		if(this.mold!=null)
		{
			MetalPressRecipe recipe = MetalPressRecipe.findRecipe(mold, stack, false);
			return recipe!=null&&(inventory[slot]==null||OreDictionary.itemMatches(stack, inventory[slot], true));
		}
		return true;
	}
	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		if(!formed)
			return new int[0];
		if(pos==3)
		{
			TileEntityMetalPress master = master();
			if (master.stopped>=0)
				return new int[]{master.stopped};
			int next = getNextProcessID();
			if(next==-1)
				return new int[0];
			return new int[]{next};
		}
		return new int[0];
	}
	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side)
	{
		if(!formed)
			return false;
		if(pos==3 && side==ForgeDirection.OPPOSITES[facing])
		{
			if (!isItemValidForSlot(slot,stack))
				return false;
			TileEntityMetalPress master = master();
			return master.inventory[slot]==null||master.inventory[slot].stackSize+stack.stackSize<=getInventoryStackLimit();
		}
		return false;
	}
	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side)
	{
		return false;
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return formed && pos==7 && from==ForgeDirection.UP;
	}
	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		TileEntityMetalPress master = master();
		if(formed && pos==7 && from==ForgeDirection.UP && master!=null)
		{
			int rec = master.energyStorage.receiveEnergy(maxReceive, simulate);
			master.markDirty();
			if(rec>0)
				worldObj.markBlockForUpdate(master.xCoord, master.yCoord, master.zCoord);
			return rec;
		}
		return 0;
	}
	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		TileEntityMetalPress master = master();
		if(master!=null)
			return master.energyStorage.getEnergyStored();
		return energyStorage.getEnergyStored();
	}
	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		TileEntityMetalPress master = master();
		if(master!=null)
			return master.energyStorage.getMaxEnergyStored();
		return energyStorage.getMaxEnergyStored();
	}
}