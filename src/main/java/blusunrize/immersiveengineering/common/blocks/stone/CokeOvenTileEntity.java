/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IActiveState;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IProcessTile;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CokeOvenTileEntity extends MultiblockPartTileEntity<CokeOvenTileEntity> implements IIEInventory,
		IActiveState, IInteractionObjectIE, IProcessTile
{
	public static TileEntityType<CokeOvenTileEntity> TYPE;

	public FluidTank tank = new FluidTank(12000);
	private NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
	public int process = 0;
	public int processMax = 0;
	public boolean active = false;

	public CokeOvenTileEntity()
	{
		super(IEMultiblocks.COKE_OVEN, TYPE, false);
	}


	@Override
	public boolean getIsActive()
	{
		return this.active;
	}

	@Override
	public boolean canUseGui(PlayerEntity player)
	{
		return formed;
	}

	@Override
	public ResourceLocation getGuiName()
	{
		return Lib.GUIID_CokeOven;
	}

	@Override
	public IInteractionObjectIE getGuiMaster()
	{
		return master();
	}

	@Override
	public float[] getBlockBounds()
	{
		return null;
	}

	@Override
	public void tick()
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
						this.tank.fill(new FluidStack(IEContent.fluidCreosote, recipe.creosoteOutput), FluidAction.EXECUTE);
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
						if(!inventory.get(3).isEmpty()&&ItemStack.areItemStacksEqual(inventory.get(3), filledContainer))
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
							world.addBlockEvent(getPos().add(xx, yy, zz), getBlockState().getBlock(), 1, active?1: 0);
						}
			}
		}
	}

	@Nullable
	public CokeOvenRecipe getRecipe()
	{
		CokeOvenRecipe recipe = CokeOvenRecipe.findRecipe(inventory.get(0));
		if(recipe==null)
			return null;

		if(inventory.get(1).isEmpty()||(ItemStack.areItemsEqual(inventory.get(1), recipe.output)&&
				inventory.get(1).getCount()+recipe.output.getCount() <= getSlotLimit(1)))
			if(tank.getFluidAmount()+recipe.creosoteOutput <= tank.getCapacity())
				return recipe;
		return null;
	}

	@Override
	public int[] getCurrentProcessesStep()
	{
		CokeOvenTileEntity master = master();
		if(master!=this&&master!=null)
			return master.getCurrentProcessesStep();
		return new int[]{processMax-process};
	}

	@Override
	public int[] getCurrentProcessesMax()
	{
		CokeOvenTileEntity master = master();
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
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		process = nbt.getInt("process");
		processMax = nbt.getInt("processMax");
		active = nbt.getBoolean("active");

		tank.readFromNBT(nbt.getCompound("tank"));
		if(!descPacket)
		{
			inventory = Utils.readInventory(nbt.getList("inventory", 10), 4);
		}
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.putInt("process", process);
		nbt.putInt("processMax", processMax);
		nbt.putBoolean("active", active);

		CompoundNBT tankTag = tank.writeToNBT(new CompoundNBT());
		nbt.put("tank", tankTag);
		if(!descPacket)
		{
			nbt.put("inventory", Utils.writeInventory(inventory));
		}
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(Direction side)
	{
		CokeOvenTileEntity master = master();
		if(master!=null)
			return new FluidTank[]{master.tank};
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resource)
	{
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, Direction side)
	{
		return true;
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		CokeOvenTileEntity master = master();
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

	LazyOptional<IItemHandler> invHandler = registerConstantCap(
			new IEInventoryHandler(4, this, 0, new boolean[]{true, false, true, false},
					new boolean[]{false, true, false, true})
	);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			CokeOvenTileEntity master = master();
			if(master==null)
				return null;
			return master.invHandler.cast();
		}
		return super.getCapability(capability, facing);
	}
}