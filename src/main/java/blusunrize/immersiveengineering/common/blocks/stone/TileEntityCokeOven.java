/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.PropertyBoolInverted;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IActiveState;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IProcessTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IUsesBooleanProperty;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityCokeOven extends TileEntityMultiblockPart<TileEntityCokeOven> implements IIEInventory, IActiveState, IGuiTile, IProcessTile
{
	public FluidTank tank = new FluidTank(12000);
	NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
	public int process = 0;
	public int processMax = 0;
	public boolean active = false;
	private static final int[] size = {3, 3, 3};

	public TileEntityCokeOven()
	{
		super(size);
	}

	@Override
	public PropertyBoolInverted getBoolProperty(Class<? extends IUsesBooleanProperty> inf)
	{
		return inf==IActiveState.class?IEProperties.BOOLEANS[0]: null;
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
		return master();
	}

	@Override
	public float[] getBlockBounds()
	{
		return null;
	}

	@Override
	public ItemStack getOriginalBlock()
	{
		return new ItemStack(IEContent.blockStoneDecoration, 1, 0);
	}

	@Override
	public boolean isDummy()
	{
		return offset[0]!=0||offset[1]!=0||offset[2]!=0;
	}

	@Override
	public void update()
	{
		ApiUtils.checkForNeedlessTicking(this);
		if(!world.isRemote&&formed&&!isDummy())
		{
			boolean a = active;
			boolean b = false;
			if(process > 0)
			{
				if(inventory.get(0).isEmpty())
				{
					process = 0;
					processMax = 0;
				}
				else
				{
					CokeOvenRecipe recipe = getRecipe();
					if(recipe==null||recipe.time!=processMax)
					{
						process = 0;
						processMax = 0;
						active = false;
					}
					else
						process--;
				}
				this.markContainingBlockForUpdate(null);
			}
			else
			{
				if(active)
				{
					CokeOvenRecipe recipe = getRecipe();
					if(recipe!=null)
					{
						Utils.modifyInvStackSize(inventory, 0, -1);
						if(!inventory.get(1).isEmpty())
							inventory.get(1).grow(recipe.output.copy().getCount());
						else if(inventory.get(1).isEmpty())
							inventory.set(1, recipe.output.copy());
						this.tank.fill(new FluidStack(IEContent.fluidCreosote, recipe.creosoteOutput), true);
					}
					processMax = 0;
					active = false;
				}
				CokeOvenRecipe recipe = getRecipe();
				if(recipe!=null)
				{
					this.process = recipe.time;
					this.processMax = process;
					this.active = true;
				}
			}

			if(tank.getFluidAmount() > 0&&tank.getFluid()!=null&&(inventory.get(3).isEmpty()||inventory.get(3).getCount()+1 <= inventory.get(3).getMaxStackSize()))
			{
				ItemStack filledContainer = Utils.fillFluidContainer(tank, inventory.get(2), inventory.get(3), null);
				if(!filledContainer.isEmpty())
				{
					if(inventory.get(2).getCount()==1&&!Utils.isFluidContainerFull(filledContainer))
					{
						inventory.set(2, filledContainer.copy());
						b = true;
					}
					else
					{
						if(!inventory.get(3).isEmpty()&&OreDictionary.itemMatches(inventory.get(3), filledContainer, true))
							inventory.get(3).grow(filledContainer.getCount());
						else if(inventory.get(3).isEmpty())
							inventory.set(3, filledContainer.copy());
						Utils.modifyInvStackSize(inventory, 2, -filledContainer.getCount());
						b = true;
					}
				}
			}

			if(a!=active||b)
			{
				this.markDirty();
				TileEntity tileEntity;
				for(int yy = -1; yy <= 1; yy++)
					for(int xx = -1; xx <= 1; xx++)
						for(int zz = -1; zz <= 1; zz++)
						{
							tileEntity = Utils.getExistingTileEntity(world, getPos().add(xx, yy, zz));
							if(tileEntity!=null)
								tileEntity.markDirty();
							this.markBlockForUpdate(getPos().add(xx, yy, zz), null);
							world.addBlockEvent(getPos().add(xx, yy, zz), IEContent.blockStoneDevice, 1, active?1: 0);
						}
			}
		}
	}

	public CokeOvenRecipe getRecipe()
	{
		CokeOvenRecipe recipe = CokeOvenRecipe.findRecipe(inventory.get(0));
		if(recipe==null)
			return null;

		if(inventory.get(1).isEmpty()||(OreDictionary.itemMatches(inventory.get(1), recipe.output, false)&&inventory.get(1).getCount()+recipe.output.getCount() <= getSlotLimit(1)))
			if(tank.getFluidAmount()+recipe.creosoteOutput <= tank.getCapacity())
				return recipe;
		return null;
	}

	@Override
	public int[] getCurrentProcessesStep()
	{
		TileEntityCokeOven master = master();
		if(master!=this&&master!=null)
			return master.getCurrentProcessesStep();
		return new int[]{processMax-process};
	}

	@Override
	public int[] getCurrentProcessesMax()
	{
		TileEntityCokeOven master = master();
		if(master!=this&&master!=null)
			return master.getCurrentProcessesMax();
		return new int[]{processMax};
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==0)
			this.formed = arg==1;
		else if(id==1)
			this.active = arg==1;
		markDirty();
		this.markContainingBlockForUpdate(null);
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
	public BlockPos getOrigin()
	{
		return getPos().add(-offset[0], -offset[1]-1, -offset[2]).offset(facing.getOpposite()).offset(facing.rotateYCCW());
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side)
	{
		TileEntityCokeOven master = master();
		if(master!=null)
			return new FluidTank[]{master.tank};
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource)
	{
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side)
	{
		return true;
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		TileEntityCokeOven master = master();
		if(master!=null&&master.formed&&formed)
			return master.inventory;
		return this.inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		if(stack.isEmpty())
			return false;
		if(slot==0)
			return CokeOvenRecipe.findRecipe(stack)!=null;
		if(slot==2)
			return Utils.isFluidRelatedItemStack(stack);
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
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return master()!=null;
		return super.hasCapability(capability, facing);
	}

	IItemHandler invHandler = new IEInventoryHandler(4, this, 0, new boolean[]{true, false, true, false}, new boolean[]{false, true, false, true});

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			TileEntityCokeOven master = master();
			if(master==null)
				return null;
			return (T)master.invHandler;
		}
		return super.getCapability(capability, facing);
	}
}