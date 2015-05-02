package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.api.CrusherRecipe;
import blusunrize.immersiveengineering.client.ClientEventHandler.ICustomBoundingboxes;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockCrusher;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.Utils;
import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityCrusher extends TileEntityMultiblockPart implements IEnergyReceiver, ISidedInventory, ICustomBoundingboxes
{
	public int facing = 2;
	public EnergyStorage energyStorage = new EnergyStorage(32000);
	List<ItemStack> inputs = new ArrayList();
	int process = 0;

	public float barrelRotation=0;
	public boolean active = false;
	public boolean mobGrinding = false;

	public TileEntityCrusher master()
	{
		if(offset[0]==0&&offset[1]==0&&offset[2]==0)
			return null;
		TileEntity te = worldObj.getTileEntity(xCoord-offset[0], yCoord-offset[1], zCoord-offset[2]);
		return te instanceof TileEntityCrusher?(TileEntityCrusher)te : null;
	}
	public static boolean _Immovable()
	{
		return true;
	}

	@Override
	public ItemStack getOriginalBlock()
	{
		ItemStack s = MultiblockCrusher.instance.getStructureManual()[pos%15/5][pos%5][pos/15];
		return s!=null?s.copy():null;
	}

	@Override
	public void updateEntity()
	{
		if(!formed || pos!=17)
			return;

		if(active||mobGrinding)
		{
			barrelRotation += 18f;
			barrelRotation %= 360f;
		}

		if(!worldObj.isRemote)
		{
			boolean update = false;
			if(worldObj.getBlockPowerInput(xCoord+(facing==4?-1:facing==5?1:facing==2?-2:2),yCoord+1,zCoord+(facing==2?-1:facing==3?1:facing==4?2:-2))<=0)
			{
				AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(xCoord-.5625,yCoord+1.5,zCoord-.5625, xCoord+1.5625,yCoord+2.875,zCoord+1.5625);
				List<EntityItem> itemList = worldObj.getEntitiesWithinAABB(EntityItem.class, aabb);
				if(!itemList.isEmpty())
					for(EntityItem e : itemList)
					{
						ItemStack input = ((EntityItem)e).getEntityItem();
						if(CrusherRecipe.findRecipe(input)==null)
						{
							e.setDead();
							continue;
						}
						addStackToInputs(input);
						e.setDead();
					}
				List<EntityLivingBase> livingList = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
				if(!livingList.isEmpty())
				{
					for(EntityLivingBase e : livingList)
						if(!e.isDead && e.getHealth()>0)
						{
							int consumed = this.energyStorage.extractEnergy(80, true);
							e.attackEntityFrom(IEDamageSources.causeGrinderDamage(), consumed/20f);
							EventHandler.crusherMap.put(e.getUniqueID(), this);
							mobGrinding = true;
							update = true;
						}
				}
				else if(process<=0&&mobGrinding)
				{
					mobGrinding = false;
					update = true;
				}
						

				if(process>0)
				{
					int consumed = this.energyStorage.extractEnergy(80, true);
					process -= consumed;
				}

				if(process<=0 && !inputs.isEmpty())
					if(active)
					{
						ItemStack inputStack = inputs.get(0);
						CrusherRecipe recipe = CrusherRecipe.findRecipe(inputStack);
						if(recipe==null)
						{
							inputs.remove(0);
							active = false;
							return;
						}
						ItemStack outputStack = recipe.output;
						outputItem(outputStack);

						inputStack.stackSize-= (recipe.input instanceof String)? 1: ((ItemStack)recipe.input).stackSize;
						if(inputStack.stackSize>0)
							inputs.set(0, inputStack);
						else
							inputs.remove(0);
						active = false;
						update = true;
					}
					else
					{
						ItemStack inputStack = inputs.get(0);
						CrusherRecipe recipe = CrusherRecipe.findRecipe(inputStack);
						if(recipe!=null)
							this.process = recipe.energy;
						else
							inputs.remove(0);
						active = true;
						update = true;
					}
			}
			else if(active)
			{
				active=false;
				update = true;
			}
			if(update)
			{
				this.markDirty();
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}

		}
	}

	public boolean addStackToInputs(ItemStack stack)
	{
		for(int i=0;i<inputs.size();i++)
			if(this.inputs.get(i)!=null && this.inputs.get(i).isItemEqual(stack) && (this.inputs.get(i).stackSize+stack.stackSize <= stack.getMaxStackSize()))
			{
				this.inputs.get(i).stackSize+=stack.stackSize;
				return true;
			}
		this.inputs.add(stack);
		return true;
	}
	public void outputItem(ItemStack stack)
	{
		TileEntity inventory = this.worldObj.getTileEntity(xCoord+(facing==4?-2:facing==5?2:0),yCoord,zCoord+(facing==2?-2:facing==3?2:0));
		if(isInventory(inventory, facing))
		{
			stack = Utils.insertStackIntoInventory((IInventory)inventory, stack, facing);
		}

		if(stack != null)
		{
			ForgeDirection fd = ForgeDirection.getOrientation(facing);
			EntityItem ei = new EntityItem(worldObj, xCoord+.5+fd.offsetX*2, yCoord+.5, zCoord+.5+fd.offsetZ*2, stack.copy());
			ei.motionX = (0.075F * fd.offsetX);
			ei.motionY = 0.025000000372529D;
			ei.motionZ = (0.075F * fd.offsetZ);
			this.worldObj.spawnEntityInWorld(ei);
		}
	}
	boolean isInventory(TileEntity tile, int side)
	{
		if(tile instanceof ISidedInventory && ((ISidedInventory)tile).getAccessibleSlotsFromSide(side).length>0)
			return false;
		if(tile instanceof IInventory && ((IInventory)tile).getSizeInventory()>0)
			return true;
		return false;
	}


	@Override
	public void readCustomNBT(NBTTagCompound nbt)
	{
		super.readCustomNBT(nbt);
		facing = nbt.getInteger("facing");
		barrelRotation = nbt.getFloat("barrelRotation");
		active = nbt.getBoolean("active");
		mobGrinding = nbt.getBoolean("modGrinding");
		process = nbt.getInteger("process");
		energyStorage.readFromNBT(nbt);
	}
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

		NBTTagList invList = nbt.getTagList("inputs", 10);
		inputs.clear();
		for(int i=0;i<invList.tagCount();i++)
			inputs.add( ItemStack.loadItemStackFromNBT(invList.getCompoundTagAt(i)));
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt)
	{
		super.writeCustomNBT(nbt);
		nbt.setInteger("facing", facing);
		nbt.setFloat("barrelRotation", barrelRotation);
		nbt.setBoolean("active", active);
		nbt.setBoolean("modGrinding", mobGrinding);
		nbt.setInteger("process", process);
		energyStorage.writeToNBT(nbt);
	}
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		NBTTagList invList = new NBTTagList();
		for(ItemStack s : inputs)
			invList.appendTag(s.writeToNBT(new NBTTagCompound()));
		nbt.setTag("inputs", invList);
	}


	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(pos==17)
			return AxisAlignedBB.getBoundingBox(xCoord-(facing==2||facing==3?2:1),yCoord,zCoord-(facing==4||facing==5?2:1), xCoord+(facing==2||facing==3?3:2),yCoord+2,zCoord+(facing==4||facing==5?3:2));
		return AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord, xCoord,yCoord,zCoord);
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
			int startX = xCoord-(f==4?il: f==5?-il: f==2?-iw : iw);
			int startY = yCoord-ih;
			int startZ = zCoord-(f==2?il: f==3?-il: f==5?-iw : iw);

			for(int l=0;l<3;l++)
				for(int w=-2;w<=2;w++)
					for(int h=-1;h<=1;h++)
					{
						int xx = (f==4?l: f==5?-l: f==2?-w : w);
						int yy = h;
						int zz = (f==2?l: f==3?-l: f==5?-w : w);

						ItemStack s = null;
						if(worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz) instanceof TileEntityCrusher)
						{
							s = ((TileEntityCrusher)worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz)).getOriginalBlock();
							((TileEntityCrusher)worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz)).formed=false;
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
					}
		}
	}

	@Override
	public int getSizeInventory()
	{
		if(!formed)
			return 0;
		return inputs.size();
	}
	@Override
	public ItemStack getStackInSlot(int slot)
	{
		if(!formed)
			return null;
		if(master()!=null)
			return master().getStackInSlot(slot);
		if(slot<inputs.size())
			return inputs.get(slot);
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
		inputs.set(slot, stack);
		if (stack != null && stack.stackSize > getInventoryStackLimit())
			stack.stackSize = getInventoryStackLimit();
	}
	@Override
	public String getInventoryName()
	{
		return "IECrusher";
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
		return true;
	}
	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		if(!formed)
			return new int[0];
		if(master()!=null)
			return master().getAccessibleSlotsFromSide(side);
		return new int[]{0,1,2,3,4,5,6,7,8};
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
		return true;
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
			TileEntityCrusher master = master();
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
			this.master().energyStorage.getEnergyStored();
		return energyStorage.getEnergyStored();
	}
	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		if(this.master()!=null)
			this.master().energyStorage.getMaxEnergyStored();
		return energyStorage.getMaxEnergyStored();
	}
}