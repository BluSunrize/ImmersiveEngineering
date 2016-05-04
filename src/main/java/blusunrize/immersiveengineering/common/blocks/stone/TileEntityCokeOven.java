package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.PropertyBoolInverted;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockIEMultiblock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IActiveState;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IUsesBooleanProperty;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityCokeOven extends TileEntityMultiblockPart<TileEntityCokeOven> implements IIEInventory, IFluidHandler, IActiveState, IGuiTile
{
	public FluidTank tank = new FluidTank(12000);
	ItemStack[] inventory = new ItemStack[4];
	public int process = 0;
	public int processMax = 0;
	public boolean active = false;

	@Override
	public PropertyBoolInverted getBoolProperty(Class<? extends IUsesBooleanProperty> inf)
	{
		return inf==IActiveState.class?IEProperties.BOOLEANS[0]:null;
	}
	@Override
	public boolean getIsActive()
	{
		return this.active;
	}
	@Override
	public boolean canOpenGui()
	{
		return formed;
	}
	@Override
	public int getGuiID()
	{
		return Lib.GUIID_CokeOven;
	}
	@Override
	public TileEntity getGuiMaster()
	{
		TileEntity master = this.master();
		return master!=null?master:this;
	}

	@Override
	public float[] getBlockBounds()
	{
		return new float[]{0,0,0,1,1,1};
	}
	@Override
	public float[] getSpecialCollisionBounds()
	{
		return null;
	}
	@Override
	public float[] getSpecialSelectionBounds()
	{
		return null;
	}

	@Override
	public ItemStack getOriginalBlock()
	{
		return new ItemStack(IEContent.blockStoneDecoration,1,0);
	}

	@Override
	public boolean isDummy()
	{
		return offset[0]!=0||offset[1]!=0||offset[2]!=0;
	}

	@Override
	public void update()
	{
		if(!worldObj.isRemote&&formed&&!isDummy())
		{
			boolean a = active;
			boolean b = false;
			if(process>0)
			{
				if(inventory[0]==null)
				{
					process=0;
					processMax=0;
				}
				else
				{
					CokeOvenRecipe recipe = getRecipe();
					if(recipe==null || recipe.time!=processMax)
					{
						process=0;
						processMax=0;
						active=false;
					}
					else
						process--;
				}
				worldObj.markBlockForUpdate(getPos());
			}
			else
			{
				if(active)
				{
					CokeOvenRecipe recipe = getRecipe();
					if(recipe!=null)
					{
						Utils.modifyInvStackSize(inventory, 0, -1);
						if(inventory[1]!=null)
							inventory[1].stackSize+=recipe.output.copy().stackSize;
						else if(inventory[1]==null)
							inventory[1] = recipe.output.copy();
						this.tank.fill(new FluidStack(IEContent.fluidCreosote,recipe.creosoteOutput), true);
					}
					processMax=0;
					active=false;
				}
				CokeOvenRecipe recipe = getRecipe();
				if(recipe!=null)
				{
					this.process=recipe.time;
					this.processMax=process;
					this.active=true;
				}
			}

			if(tank.getFluidAmount()>0 && tank.getFluid()!=null && (inventory[3]==null||inventory[3].stackSize+1<=inventory[3].getMaxStackSize()))
			{
				ItemStack filledContainer = Utils.fillFluidContainer(tank, inventory[2], inventory[3]);
				if(filledContainer!=null)
				{
					if(inventory[3]!=null && OreDictionary.itemMatches(inventory[3], filledContainer, true))
						inventory[3].stackSize+=filledContainer.stackSize;
					else if(inventory[3]==null)
						inventory[3] = filledContainer.copy();
					Utils.modifyInvStackSize(inventory, 2, -filledContainer.stackSize);
					b=true;
				}
			}

			if(a!=active || b)
			{
				this.markDirty();
				TileEntity tileEntity;
				for(int yy=-1;yy<=1;yy++)
					for(int xx=-1;xx<=1;xx++)
						for(int zz=-1;zz<=1;zz++)
						{
							tileEntity = worldObj.getTileEntity(getPos().add(xx, yy, zz));
							if(tileEntity!=null)
								tileEntity.markDirty();
							worldObj.markBlockForUpdate(getPos().add(xx, yy, zz));
							worldObj.addBlockEvent(getPos().add(xx, yy, zz), IEContent.blockStoneDevice, 1,active?1:0);
						}
			}
		}
	}
	public CokeOvenRecipe getRecipe()
	{
		CokeOvenRecipe recipe = CokeOvenRecipe.findRecipe(inventory[0]);
		if(recipe==null)
			return null;

		if(inventory[1]==null || (OreDictionary.itemMatches(inventory[1],recipe.output,false) && inventory[1].stackSize+recipe.output.stackSize<=getSlotLimit(1)) )
			if(tank.getFluidAmount()+recipe.creosoteOutput<=tank.getCapacity())
				return recipe;
		return null;
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==0)
			this.formed = arg==1;
		else if(id==1)
			this.active = arg==1;
		markDirty();
		worldObj.markBlockForUpdate(this.getPos());
		return true;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		process = nbt.getInteger("process");
		processMax = nbt.getInteger("processMax");
		active = nbt.getBoolean("active");

		tank.readFromNBT(nbt.getCompoundTag("tank"));
		if(!descPacket)
		{
			inventory = Utils.readInventory(nbt.getTagList("inventory", 10), 4);
		}
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("process", process);
		nbt.setInteger("processMax", processMax);
		nbt.setBoolean("active", active);

		NBTTagCompound tankTag = tank.writeToNBT(new NBTTagCompound());
		nbt.setTag("tank", tankTag);
		if(!descPacket)
		{
			nbt.setTag("inventory", Utils.writeInventory(inventory));
		}
	}

	@Override
	public void disassemble()
	{
		if(formed && !worldObj.isRemote)
		{
			BlockPos startPos = this.getPos().add(-offset[0],-offset[1],-offset[2]);
			if(!(offset[0]==0&&offset[1]==0&&offset[2]==0) && !(worldObj.getTileEntity(startPos) instanceof TileEntityCokeOven))
				return;

			for(int yy=-1;yy<=1;yy++)
				for(int xx=-1;xx<=1;xx++)
					for(int zz=-1;zz<=1;zz++)
					{
						ItemStack s = null;
						TileEntity te = worldObj.getTileEntity(startPos.add(xx, yy, zz));
						if(te instanceof TileEntityCokeOven)
						{
							s = ((TileEntityCokeOven)te).getOriginalBlock();
							((TileEntityCokeOven)te).formed=false;
						}
						if(startPos.add(xx, yy, zz).equals(this.getPos()))
							s = this.getOriginalBlock();
						if(s!=null && Block.getBlockFromItem(s.getItem())!=null)
						{
							if(startPos.add(xx, yy, zz).equals(this.getPos()))
								worldObj.spawnEntityInWorld(new EntityItem(worldObj, getPos().getX()+.5, getPos().getY()+.5, getPos().getZ()+.5, s));
							else
							{
								if(Block.getBlockFromItem(s.getItem()) instanceof BlockIEMultiblock)
									worldObj.setBlockToAir(startPos.add(xx,yy,zz));
								worldObj.setBlockState(startPos.add(xx,yy,zz), Block.getBlockFromItem(s.getItem()).getStateFromMeta(s.getItemDamage()));
							}
						}
					}
		}
	}

	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill)
	{
		return 0;
	}
	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain)
	{
		if(!formed)
			return null;
		TileEntityCokeOven master = master();
		FluidStack fs = null;
		if(master!=null)
			fs = master.tank.drain(resource.amount,doDrain);
		markDirty();
		worldObj.markBlockForUpdate(this.getPos());
		return fs;
	}
	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain)
	{
		if(!formed)
			return null;
		TileEntityCokeOven master = master();
		FluidStack fs = null;
		if(master!=null)
			fs = master.tank.drain(maxDrain, doDrain);
		markDirty();
		worldObj.markBlockForUpdate(this.getPos());
		return fs;
	}
	@Override
	public boolean canFill(EnumFacing from, Fluid fluid)
	{
		return false;
	}
	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid)
	{
		if(!formed)
			return false;
		return true;
	}
	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from)
	{
		if(!formed)
			return new FluidTankInfo[]{};
		TileEntityCokeOven master = master();
		if(master!=null)
			return new FluidTankInfo[]{master.tank.getInfo()};
		return new FluidTankInfo[0];
	}

	@Override
	public ItemStack[] getInventory()
	{
		return this.inventory;
	}
	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		if(stack==null)
			return false;
		if(slot==0)
			return CokeOvenRecipe.findRecipe(stack)!=null;
		if(slot==2)
			return FluidContainerRegistry.isEmptyContainer(stack) || stack.getItem() instanceof IFluidContainerItem;
		return false;
	}
	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}
	@Override
	public void doGraphicalUpdates(int slot)
	{
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if(capability==net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return true;
		return super.hasCapability(capability, facing);
	}
	IItemHandler invHandler = new IEInventoryHandler(4,this,0, new boolean[]{true,false,true,false},new boolean[]{false,true,false,true});
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if(capability==net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			TileEntityCokeOven master = master();
			//			System.out.println("master: "+master);
			if(master==null)
				return null;
			return (T)master.invHandler;
		}
		return super.getCapability(capability, facing);
	}
}