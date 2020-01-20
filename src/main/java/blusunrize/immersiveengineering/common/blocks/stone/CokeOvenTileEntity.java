/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.api.ApiUtils;
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
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

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
	public CokeOvenData guiData = new CokeOvenData();

	public CokeOvenTileEntity()
	{
		super(IEMultiblocks.COKE_OVEN, TYPE, false);
	}

	@Override
	public boolean canUseGui(PlayerEntity player)
	{
		return formed;
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
			final boolean activeBeforeTick = getIsActive();
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
						setActive(false);
					}
					else
						process--;
				}
				this.markContainingBlockForUpdate(null);
			}
			else
			{
				if(activeBeforeTick)
				{
					CokeOvenRecipe recipe = getRecipe();
					if(recipe!=null)
					{
						Utils.modifyInvStackSize(inventory, 0, recipe.input.inputSize);
						if(!inventory.get(1).isEmpty())
							inventory.get(1).grow(recipe.output.copy().getCount());
						else if(inventory.get(1).isEmpty())
							inventory.set(1, recipe.output.copy());
						this.tank.fill(new FluidStack(IEContent.fluidCreosote, recipe.creosoteOutput), FluidAction.EXECUTE);
					}
					processMax = 0;
					setActive(false);
				}
				CokeOvenRecipe recipe = getRecipe();
				if(recipe!=null)
				{
					this.process = recipe.time;
					this.processMax = process;
					setActive(true);
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
						markDirty();
					}
					else
					{
						if(!inventory.get(3).isEmpty()&&ItemHandlerHelper.canItemStacksStack(inventory.get(3), filledContainer))
							inventory.get(3).grow(filledContainer.getCount());
						else if(inventory.get(3).isEmpty())
							inventory.set(3, filledContainer.copy());
						Utils.modifyInvStackSize(inventory, 2, -filledContainer.getCount());
						markDirty();
					}
				}
			}

			final boolean activeAfterTick = getIsActive();
			if(activeBeforeTick!=activeAfterTick)
			{
				this.markDirty();
				for(int x = 0; x < 3; ++x)
					for(int y = 0; y < 3; ++y)
						for(int z = 0; z < 3; ++z)
						{
							BlockPos actualPos = getBlockPosForPos(new BlockPos(x, y, z));
							TileEntity te = Utils.getExistingTileEntity(world, actualPos);
							if(te instanceof CokeOvenTileEntity)
								((CokeOvenTileEntity)te).setActive(activeAfterTick);
						}
			}
		}
	}

	@Nullable
	public CokeOvenRecipe getRecipe()
	{
		if(inventory.get(0).isEmpty())
			return null;
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
		markDirty();
		this.markContainingBlockForUpdate(null);
		return true;
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);

		tank.readFromNBT(nbt.getCompound("tank"));
		if(!descPacket)
		{
			ItemStackHelper.loadAllItems(nbt, inventory);
			process = nbt.getInt("process");
			processMax = nbt.getInt("processMax");
		}
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);

		CompoundNBT tankTag = tank.writeToNBT(new CompoundNBT());
		nbt.put("tank", tankTag);
		if(!descPacket)
		{
			nbt.putInt("process", process);
			nbt.putInt("processMax", processMax);
			ItemStackHelper.saveAllItems(nbt, inventory);
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

	public class CokeOvenData implements IIntArray
	{
		public static final int MAX_BURN_TIME = 0;
		public static final int BURN_TIME = 1;

		@Override
		public int get(int index)
		{
			switch(index)
			{
				case MAX_BURN_TIME:
					return processMax;
				case BURN_TIME:
					return process;
				default:
					throw new IllegalArgumentException("Unknown index "+index);
			}
		}

		@Override
		public void set(int index, int value)
		{
			switch(index)
			{
				case MAX_BURN_TIME:
					processMax = value;
					break;
				case BURN_TIME:
					process = value;
					break;
				default:
					throw new IllegalArgumentException("Unknown index "+index);
			}
		}

		@Override
		public int size()
		{
			return 2;
		}
	}
}