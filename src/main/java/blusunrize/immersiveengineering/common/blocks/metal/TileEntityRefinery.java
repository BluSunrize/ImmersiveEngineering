package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.api.energy.DieselHandler.RefineryRecipe;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockRefinery;
import blusunrize.immersiveengineering.common.util.Utils;
import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityRefinery extends TileEntityMultiblockPart implements IFluidHandler, IEnergyReceiver, ISidedInventory
{
	public int facing = 2;
	public FluidTank tank0 = new FluidTank(12000);
	public FluidTank tank1 = new FluidTank(12000);
	public FluidTank tank2 = new FluidTank(12000);
	public EnergyStorage energyStorage = new EnergyStorage(32000,Math.max(256, Config.getInt("refinery_consumption")));
	public ItemStack[] inventory = new ItemStack[6];

	public TileEntityRefinery master()
	{
		if(offset[0]==0&&offset[1]==0&&offset[2]==0)
			return null;
		TileEntity te = worldObj.getTileEntity(xCoord-offset[0], yCoord-offset[1], zCoord-offset[2]);
		return te instanceof TileEntityRefinery?(TileEntityRefinery)te : null;
	}

	public static boolean _Immovable()
	{
		return true;
	}

	@Override
	public ItemStack getOriginalBlock()
	{
		if(pos<0)
			return null;
		ItemStack s = MultiblockRefinery.instance.getStructureManual()[pos%15/5][pos%5][pos/15];
		return s!=null?s.copy():null;
	}
	@Override
	public float[] getBlockBounds()
	{
		if(pos==7)
			return new float[]{.0625f,0,.0625f, .9375f,1,.9375f};
		else if(pos==37)
			return new float[]{facing==4?.5f:0,0,facing==2?.5f:0, facing==5?.5f:1,1,facing==3?.5f:1};
		else if(pos==20||pos==25)
			return new float[]{facing==3?.1875f:0,0,facing==4?.1875f:0, facing==2?.8125f:1,1,facing==5?.8125f:1};
		else if(pos==24||pos==29)
			return new float[]{facing==2?.1875f:0,0,facing==5?.1875f:0, facing==3?.8125f:1,1,facing==4?.8125f:1};
		else if((pos>=5&&pos<15&&pos!=9)||(pos>=35&&pos<45))
		{
			float minY= pos/5>1?0:.375f;
			float maxY=1;
			float minX= facing==3?.4375f: facing==4?.6875f: 0;
			float maxX= facing==2?.5625f: facing==5?.3125f: 1;
			float minZ= facing==4?.4375f: facing==2?.6875f: 0;
			float maxZ= facing==5?.5625f: facing==3?.3125f: 1;
			if(pos%5==4)
			{
				minX+=facing==3?-.4375f: facing==2?.4375f: 0;
				maxX+=facing==3?-.4375f: facing==2?.4375f: 0;
				minZ+=facing==4?-.4375f: facing==5?.4375f: 0;
				maxZ+=facing==4?-.4375f: facing==5?.4375f: 0;
			}
			if(pos/15==2)
			{
				minX+=facing==4?-.6875f: facing==5?.6875f: 0;
				maxX+=facing==4?-.6875f: facing==5?.6875f: 0;
				minZ+=facing==3?.6875f: facing==2?-.6875f: 0;
				maxZ+=facing==3?.6875f: facing==2?-.6875f: 0;
			}
			if(pos%5!=0&&pos%5!=4)
			{
				minX = facing==2||facing==3?0:minX;
				maxX = facing==2||facing==3?1:maxX;
				minZ = facing==4||facing==5?0:minZ;
				maxZ = facing==4||facing==5?1:maxZ;
			}
			return new float[]{minX,minY,minZ, maxX,maxY,maxZ};
		}
		else if(pos==0||pos==1||pos==3 || pos==30||pos==31||pos==33||pos==34)
			return new float[]{0,0,0,1,.5f,1};
		else
		return new float[]{0,0,0,1,1,1};
	}

	@Override
	public void updateEntity()
	{
		if(!formed || pos!=17)
			return;

		if(!worldObj.isRemote && !worldObj.isBlockIndirectlyGettingPowered(xCoord+(facing==4?-1:facing==5?1:facing==2?-2:2),yCoord+1,zCoord+(facing==2?-1:facing==3?1:facing==4?2:-2)))
		{
			boolean update = false;
			RefineryRecipe recipe = getRecipe();
			if(recipe!=null)
			{
				int consumed = Config.getInt("refinery_consumption");
				if(energyStorage.extractEnergy(consumed, true)==consumed && tank2.fill(recipe.output.copy(), false)==recipe.output.amount)
				{
					energyStorage.extractEnergy(consumed, false);
					int drain0 = tank0.getFluid().isFluidEqual(recipe.input0)?recipe.input0.amount: recipe.input1.amount;
					int drain1 = tank0.getFluid().isFluidEqual(recipe.input0)?recipe.input1.amount: recipe.input0.amount;
					tank0.drain(drain0, true);
					tank1.drain(drain1, true);
					tank2.fill(recipe.output.copy(), true);
					update = true;
				}
			}
			if(tank2.getFluidAmount()>0)
			{
				ItemStack filledContainer = Utils.fillFluidContainer(tank2, inventory[4], inventory[5]);
				if(filledContainer!=null)
				{
					if(inventory[5]!=null && OreDictionary.itemMatches(inventory[5], filledContainer, true))
						inventory[5].stackSize+=filledContainer.stackSize;
					else if(inventory[5]==null)
						inventory[5] = filledContainer.copy();
					this.decrStackSize(4, filledContainer.stackSize);
					update = true;
				}

				if(tank2.getFluidAmount()>0)
				{
					ForgeDirection f = ForgeDirection.getOrientation(facing);
					int out = Math.min(144,tank2.getFluidAmount());
					TileEntity te = worldObj.getTileEntity(xCoord+f.offsetX*2,yCoord,zCoord+f.offsetZ*2);
					if(te!=null && te instanceof IFluidHandler && ((IFluidHandler)te).canFill(f.getOpposite(), tank2.getFluid().getFluid()))
					{
						int accepted = ((IFluidHandler)te).fill(f.getOpposite(), new FluidStack(tank2.getFluid().getFluid(),out), false);
						FluidStack drained = this.tank2.drain(accepted, true);
						((IFluidHandler)te).fill(f.getOpposite(), drained, true);
					}
				}
			}

			ItemStack emptyContainer = Utils.drainFluidContainer(tank0, inventory[0]);
			if(emptyContainer!=null)
			{
				if(inventory[1]!=null && OreDictionary.itemMatches(inventory[1], emptyContainer, true))
					inventory[1].stackSize+=emptyContainer.stackSize;
				else if(inventory[1]==null)
					inventory[1] = emptyContainer.copy();
				this.decrStackSize(0, emptyContainer.stackSize);
				update = true;
			}
			emptyContainer = Utils.drainFluidContainer(tank1, inventory[2]);
			if(emptyContainer!=null)
			{
				if(inventory[3]!=null && OreDictionary.itemMatches(inventory[3], emptyContainer, true))
					inventory[3].stackSize+=emptyContainer.stackSize;
				else if(inventory[3]==null)
					inventory[3] = emptyContainer.copy();
				this.decrStackSize(2, emptyContainer.stackSize);
				update = true;
			}

			if(update)
			{
				this.markDirty();
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
		}
	}

	public RefineryRecipe getRecipe()
	{
		RefineryRecipe recipe = DieselHandler.findRefineryRecipe(tank0.getFluid(), tank1.getFluid());
		if(recipe==null)
			return null;
		if(tank2.getFluid()==null || (tank2.getFluid().isFluidEqual(recipe.output) && tank2.getFluidAmount()+recipe.output.amount<=tank2.getCapacity()))
			return recipe;
		return null;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = nbt.getInteger("facing");
		tank0.readFromNBT(nbt.getCompoundTag("tank0"));
		tank1.readFromNBT(nbt.getCompoundTag("tank1"));
		tank2.readFromNBT(nbt.getCompoundTag("tank2"));
		energyStorage.readFromNBT(nbt);
		if(!descPacket)
		{
			NBTTagList invList = nbt.getTagList("inventory", 10);
			for (int i=0; i<invList.tagCount(); i++)
			{
				NBTTagCompound itemTag = invList.getCompoundTagAt(i);
				int slot = itemTag.getByte("Slot") & 255;
				if(slot>=0 && slot<this.inventory.length)
					this.inventory[slot] = ItemStack.loadItemStackFromNBT(itemTag);
			}
		}
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", facing);
		NBTTagCompound tankTag = tank0.writeToNBT(new NBTTagCompound());
		nbt.setTag("tank0", tankTag);
		tankTag = tank1.writeToNBT(new NBTTagCompound());
		nbt.setTag("tank1", tankTag);
		tankTag = tank2.writeToNBT(new NBTTagCompound());
		nbt.setTag("tank2", tankTag);
		energyStorage.writeToNBT(nbt);
		if(!descPacket)
		{
			NBTTagList invList = new NBTTagList();
			for(int i=0; i<this.inventory.length; i++)
				if(this.inventory[i] != null)
				{
					NBTTagCompound itemTag = new NBTTagCompound();
					itemTag.setByte("Slot", (byte)i);
					this.inventory[i].writeToNBT(itemTag);
					invList.appendTag(itemTag);
				}
			nbt.setTag("inventory", invList);
		}
	}



	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if(!formed)
			return 0;
		if(master()!=null)
		{
			if(pos!=15&&pos!=19)
				return 0;
			return master().fill(from,resource,doFill);
		}
		else if(resource!=null)
		{
			int fill = 0;
			if(resource.isFluidEqual(tank0.getFluid()))
				fill = tank0.fill(resource, doFill);
			else if(resource.isFluidEqual(tank1.getFluid()))
				fill = tank1.fill(resource, doFill);
			else if(tank0.getFluidAmount()<=0 && tank1.getFluidAmount()<=0)
				fill = (DieselHandler.findIncompleteRefineryRecipe(resource, null)!=null?tank0.fill(resource, doFill):0);
			else 
			{
				if(tank0.getFluidAmount()>0)
					fill = (DieselHandler.findIncompleteRefineryRecipe(resource, tank0.getFluid())!=null?tank1.fill(resource, doFill):0);
				else if(tank1.getFluidAmount()>0)
					fill = (DieselHandler.findIncompleteRefineryRecipe(resource, tank1.getFluid())!=null?tank0.fill(resource, doFill):0);
			}
			markDirty();
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			return fill;
		}
		return 0;
	}
	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if(!formed)
			return null;
		if(master()!=null)
		{
			if(pos!=2&&pos!=32)
				return null;
			return master().drain(from,resource,doDrain);
		}
		else if(resource!=null)
			return drain(from, resource.amount, doDrain);
		return null;
	}
	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		if(!formed)
			return null;
		if(master()!=null)
		{
			if(pos!=2&&pos!=32)
				return null;
			return master().drain(from,maxDrain,doDrain);
		}
		else
		{
			FluidStack drain = tank2.drain(maxDrain, doDrain);
			markDirty();
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			return drain;
		}
	}
	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		if(!formed)
			return false;
		return true;
	}
	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		if(!formed)
			return false;
		return true;
	}
	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		if(!formed)
			return new FluidTankInfo[]{};
		if(master()!=null)
			return master().getTankInfo(from);
		return new FluidTankInfo[]{tank0.getInfo(),tank1.getInfo(),tank2.getInfo()};
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(pos==17)
			return AxisAlignedBB.getBoundingBox(xCoord-(facing==2||facing==3?2:1),yCoord,zCoord-(facing==4||facing==5?2:1), xCoord+(facing==2||facing==3?3:2),yCoord+3,zCoord+(facing==4||facing==5?3:2));
		return AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord, xCoord,yCoord,zCoord);
	}
	@Override
    @SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return super.getMaxRenderDistanceSquared()*Config.getDouble("increasedTileRenderdistance");
	}
	
	@Override
	public void invalidate()
	{
		super.invalidate();

		if(formed && !worldObj.isRemote)
		{
			int f = facing;
			int il = pos/15;
			int ih = (pos%15/5)-1;
			int iw = (pos%5)-2;
			int startX = xCoord-(f==4?il: f==5?-il: f==2?-iw: iw);
			int startY = yCoord-ih;
			int startZ = zCoord-(f==2?il: f==3?-il: f==5?-iw: iw);
			for(int l=0;l<3;l++)
				for(int w=-2;w<=2;w++)
					for(int h=-1;h<=1;h++)
					{
						if(w==0&&(h==1||(h==0&&l==1)))
							continue;
						int xx = (f==4?l: f==5?-l: f==2?-w : w);
						int yy = h;
						int zz = (f==2?l: f==3?-l: f==5?-w : w);



						ItemStack s = null;
						TileEntity te = worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz);
						if(te instanceof TileEntityRefinery)
						{
							s = ((TileEntityRefinery)te).getOriginalBlock();
							((TileEntityRefinery)te).formed=false;
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
								worldObj.setBlock(startX+xx,startY+yy,startZ+zz, Block.getBlockFromItem(s.getItem()), s.getItemDamage(), 0x3);
							}
						}
						//						
						//						
						//						if((startX+xx!=xCoord) || (startY+yy!=yCoord) || (startZ+zz!=zCoord))
						//						{
						//							ItemStack s = null;
						//							if(worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz) instanceof TileEntityRefinery)
						//							{
						//								((TileEntityRefinery)worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz)).formed=false;
						//								s = ((TileEntityRefinery)worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz)).getOriginalBlock();
						//							}
						//							if(s!=null && Block.getBlockFromItem(s.getItem())!=null)
						//							{
						//								if(Block.getBlockFromItem(s.getItem())==this.getBlockType() && s.getItemDamage()==this.getBlockMetadata())
						//									worldObj.setBlockToAir(startX+xx,startY+yy,startZ+zz);
						//								worldObj.setBlock(startX+xx,startY+yy,startZ+zz, Block.getBlockFromItem(s.getItem()), s.getItemDamage(), 0x3);
						//							}
						//						}
					}
		}
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return formed && pos==37 && ForgeDirection.getOrientation(facing).getOpposite().equals(from);
	}
	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		if(formed && pos==37 && master()!=null && ForgeDirection.getOrientation(facing).getOpposite().equals(from))
		{
			TileEntityRefinery master = master();
			int rec = master.energyStorage.receiveEnergy(maxReceive, simulate);
			master.markDirty();
			if(rec>0)
				worldObj.markBlockForUpdate(master().xCoord, master().yCoord, master().zCoord);
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
		if(master()!=null)
			return master().getStackInSlot(slot);
		if(slot<inventory.length)
			return inventory[slot];
		return null;
	}
	@Override
	public ItemStack decrStackSize(int slot, int amount)
	{
		if(!formed)
			return null;
		if(master()!=null)
			return master().decrStackSize(slot,amount);
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
		return stack;
	}
	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		if(!formed)
			return null;
		if(master()!=null)
			return master().getStackInSlotOnClosing(slot);
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
		if(master()!=null)
		{
			master().setInventorySlotContents(slot,stack);
			return;
		}
		inventory[slot] = stack;
		if (stack != null && stack.stackSize > getInventoryStackLimit())
			stack.stackSize = getInventoryStackLimit();
	}
	@Override
	public String getInventoryName()
	{
		return "IERefinery";
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
		if(!formed)
			return false;
		if(master()!=null)
			return master().isItemValidForSlot(slot,stack);
		if(slot==1||slot==3||slot==5)
			return false;
		if(slot==4)
			return (tank2.getFluidAmount()<=0?FluidContainerRegistry.isEmptyContainer(stack): FluidContainerRegistry.fillFluidContainer(tank2.getFluid(), Utils.copyStackWithAmount(stack,1))!=null);
		FluidStack fs = FluidContainerRegistry.getFluidForFilledItem(stack);
		if(fs==null)
			return false;
		RefineryRecipe partialRecipe = DieselHandler.findIncompleteRefineryRecipe(fs, null);
		if(partialRecipe==null)
			return false;
		if(slot==0)
			return (tank0.getFluidAmount()<=0||fs.isFluidEqual(tank0.getFluid())) && (tank1.getFluidAmount()<=0||DieselHandler.findIncompleteRefineryRecipe(fs, tank1.getFluid())!=null);
		if(slot==2)
			return (tank1.getFluidAmount()<=0||fs.isFluidEqual(tank1.getFluid())) && (tank0.getFluidAmount()<=0||DieselHandler.findIncompleteRefineryRecipe(fs, tank0.getFluid())!=null);
		return false;
	}
	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		if(!formed)
			return new int[0];
		if(master()!=null)
			return master().getAccessibleSlotsFromSide(side);
		return new int[]{0,1,2,3,4,5};
	}
	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side)
	{
		if(!formed)
			return false;
		if(master()!=null)
			return master().canInsertItem(slot,stack,side);
		return isItemValidForSlot(slot,stack);
	}
	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side)
	{
		if(!formed)
			return false;
		if(master()!=null)
			return master().canExtractItem(slot,stack,side);
		return slot==1||slot==3||slot==5;
	}
}