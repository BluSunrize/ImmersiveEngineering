/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IActiveState;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IProcessTile;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nullable;

public class BlastFurnaceTileEntity extends MultiblockPartTileEntity<BlastFurnaceTileEntity> implements IIEInventory,
		IActiveState, IInteractionObjectIE, IProcessTile
{
	public static TileEntityType<BlastFurnaceTileEntity> TYPE;

	NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
	public int process = 0;
	public int processMax = 0;
	public boolean active = false;
	public int burnTime = 0;
	public int lastBurnTime = 0;

	public BlastFurnaceTileEntity()
	{
		super(IEMultiblocks.BLAST_FURNACE, TYPE, false);
	}

	protected BlastFurnaceTileEntity(IETemplateMultiblock mb, TileEntityType<? extends BlastFurnaceTileEntity> type)
	{
		super(mb, type, false);
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

			if(burnTime > 0)
			{
				int processSpeed = 1;
				if(process > 0)
					processSpeed = getProcessSpeed();
				burnTime -= processSpeed;
				if(process > 0)
				{
					if(inventory.get(0).isEmpty())
					{
						process = 0;
						processMax = 0;
					}
					else
					{
						BlastFurnaceRecipe recipe = getRecipe();
						if(recipe!=null&&recipe.time!=processMax)
						{
							processMax = 0;
							process = 0;
							active = false;
						}
						else
						{
							process -= processSpeed;
							processSpeed = 0;//Process speed is "used up"
							if(!active)
								active = true;
						}
					}
					markContainingBlockForUpdate(null);
				}

				if(process <= 0)
				{
					if(processMax > 0)
					{
						BlastFurnaceRecipe recipe = getRecipe();
						if(recipe!=null)
						{
							Utils.modifyInvStackSize(inventory, 0, -(recipe.input instanceof ItemStack?((ItemStack)recipe.input).getCount(): 1));
							if(!inventory.get(2).isEmpty())
								inventory.get(2).grow(recipe.output.copy().getCount());
							else
								inventory.set(2, recipe.output.copy());
							if(!recipe.slag.isEmpty())
							{
								if(!inventory.get(3).isEmpty())
									inventory.get(3).grow(recipe.slag.copy().getCount());
								else
									inventory.set(3, recipe.slag.copy());
							}
						}
						processMax = 0;
						burnTime -= process;
					}
					BlastFurnaceRecipe recipe = getRecipe();
					if(recipe!=null)
					{
						this.process = recipe.time-processSpeed;
						this.processMax = recipe.time;
						this.active = true;
					}
				}
			}
			else
			{
				if(active)
					active = false;
			}

			if(burnTime <= 0&&getRecipe()!=null)
			{
				if(BlastFurnaceRecipe.isValidBlastFuel(inventory.get(1)))
				{
					lastBurnTime = BlastFurnaceRecipe.getBlastFuelTime(inventory.get(1));
					burnTime += lastBurnTime;
					Utils.modifyInvStackSize(inventory, 1, -1);
					markContainingBlockForUpdate(null);
				}
			}

			if(a!=active)
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
							markBlockForUpdate(getPos().add(xx, yy, zz), null);
							world.addBlockEvent(getPos().add(xx, yy, zz), getBlockState().getBlock(), 1, active?1: 0);
						}
			}
		}
	}

	@Nullable
	public BlastFurnaceRecipe getRecipe()
	{
		BlastFurnaceRecipe recipe = BlastFurnaceRecipe.findRecipe(inventory.get(0));
		if(recipe==null)
			return null;
		if((inventory.get(0).getCount() >= ((recipe.input instanceof ItemStack)?((ItemStack)recipe.input).getCount(): 1)
				&&inventory.get(2).isEmpty()||(ItemStack.areItemsEqual(inventory.get(2), recipe.output)&&
				inventory.get(2).getCount()+recipe.output.getCount() <= getSlotLimit(2)))
				&&(inventory.get(3).isEmpty()||(ItemStack.areItemsEqual(inventory.get(3), recipe.slag)&&
				inventory.get(3).getCount()+recipe.slag.getCount() <= getSlotLimit(3))))
			return recipe;
		return null;
	}

	protected int getProcessSpeed()
	{
		return 1;
	}

	@Override
	public int[] getCurrentProcessesStep()
	{
		BlastFurnaceTileEntity master = master();
		if(master!=this&&master!=null)
			return master.getCurrentProcessesStep();
		return new int[]{processMax-process};
	}

	@Override
	public int[] getCurrentProcessesMax()
	{
		BlastFurnaceTileEntity master = master();
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
		markContainingBlockForUpdate(null);
		return true;
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		process = nbt.getInt("process");
		processMax = nbt.getInt("processMax");
		active = nbt.getBoolean("active");
		burnTime = nbt.getInt("burnTime");
		lastBurnTime = nbt.getInt("lastBurnTime");
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
		nbt.putInt("burnTime", burnTime);
		nbt.putInt("lastBurnTime", lastBurnTime);
		if(!descPacket)
		{
			nbt.put("inventory", Utils.writeInventory(inventory));
		}
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return this.inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return slot==0?BlastFurnaceRecipe.findRecipe(stack)!=null: slot==1&&BlastFurnaceRecipe.isValidBlastFuel(stack);
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
	protected IFluidTank[] getAccessibleFluidTanks(Direction side)
	{
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resources)
	{
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, Direction side)
	{
		return false;
	}
}