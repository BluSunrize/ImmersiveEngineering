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
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IActiveState;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IProcessTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IUsesBooleanProperty;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;

public class TileEntityBlastFurnace extends TileEntityMultiblockPart<TileEntityBlastFurnace> implements IIEInventory, IActiveState, IGuiTile, IProcessTile
{
	NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
	public int process = 0;
	public int processMax = 0;
	public boolean active = false;
	public int burnTime = 0;
	public int lastBurnTime = 0;
	private static final int[] size = {3, 3, 3};

	public TileEntityBlastFurnace()
	{
		super(size);
	}

	public TileEntityBlastFurnace(int[] size)
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
		return Lib.GUIID_BlastFurnace;
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
		return new ItemStack(IEContent.blockStoneDecoration, 1, 1);
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
							world.addBlockEvent(getPos().add(xx, yy, zz), IEContent.blockStoneDevice, 1, active?1: 0);
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
				&&inventory.get(2).isEmpty()||(OreDictionary.itemMatches(inventory.get(2), recipe.output, true)&&inventory.get(2).getCount()+recipe.output.getCount() <= getSlotLimit(2)))
				&&(inventory.get(3).isEmpty()||(OreDictionary.itemMatches(inventory.get(3), recipe.slag, true)&&inventory.get(3).getCount()+recipe.slag.getCount() <= getSlotLimit(3))))
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
		TileEntityBlastFurnace master = master();
		if(master!=this&&master!=null)
			return master.getCurrentProcessesStep();
		return new int[]{processMax-process};
	}

	@Override
	public int[] getCurrentProcessesMax()
	{
		TileEntityBlastFurnace master = master();
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
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		process = nbt.getInteger("process");
		processMax = nbt.getInteger("processMax");
		active = nbt.getBoolean("active");
		burnTime = nbt.getInteger("burnTime");
		lastBurnTime = nbt.getInteger("lastBurnTime");
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
		nbt.setInteger("burnTime", burnTime);
		nbt.setInteger("lastBurnTime", lastBurnTime);
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
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side)
	{
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resources)
	{
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side)
	{
		return false;
	}
}