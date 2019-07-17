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
import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IActiveState;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IProcessTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IUsesBooleanProperty;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;

public class TileEntityAlloySmelter extends TileEntityMultiblockPart<TileEntityAlloySmelter> implements IIEInventory, IActiveState, IGuiTile, IProcessTile
{
	NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
	public int process = 0;
	public int processMax = 0;
	public boolean active = false;
	public int burnTime = 0;
	public int lastBurnTime = 0;
	private static final int[] size = {2, 2, 2};

	public TileEntityAlloySmelter()
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
		return Lib.GUIID_AlloySmelter;
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
		return new ItemStack(IEContent.blockStoneDecoration, 1, BlockTypes_StoneDecoration.ALLOYBRICK.getMeta());
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
				boolean doneWork = false;
				if(process > 0)
				{
					if(inventory.get(0).isEmpty()||inventory.get(1).isEmpty())
					{
						process = 0;
						processMax = 0;
					}
					else
					{
						AlloyRecipe recipe = getRecipe();
						if(recipe!=null&&recipe.time!=processMax)
						{
							processMax = 0;
							process = 0;
							active = false;
						}
						else
						{
							process--;
							doneWork = true;
							if(!active)
								active = true;
						}
					}
					markContainingBlockForUpdate(null);
				}
				if(--burnTime%10==0)
					markContainingBlockForUpdate(null);

				if(process <= 0)
				{
					if(processMax > 0)
					{
						AlloyRecipe recipe = getRecipe();
						if(recipe!=null)
						{
							boolean flip = !recipe.input0.matchesItemStack(inventory.get(0));
							Utils.modifyInvStackSize(inventory, flip?1: 0, -recipe.input0.inputSize);
							Utils.modifyInvStackSize(inventory, flip?0: 1, -recipe.input1.inputSize);

							if(!inventory.get(3).isEmpty())
								inventory.get(3).grow(recipe.output.copy().getCount());
							else
								inventory.set(3, recipe.output.copy());
						}
						processMax = 0;
					}
					AlloyRecipe recipe = getRecipe();
					if(recipe!=null)
					{
						this.process = recipe.time;
						if(!doneWork)
							process--;
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

			if(burnTime <= 10&&getRecipe()!=null&&(process > 10||processMax==0))
			{
				ItemStack fuel = inventory.get(2);
				if(TileEntityFurnace.isItemFuel(fuel))
				{
					lastBurnTime = TileEntityFurnace.getItemBurnTime(fuel);
					burnTime += lastBurnTime;
					Item itemFuel = fuel.getItem();
					fuel.shrink(1);
					if(fuel.isEmpty())
						inventory.set(2, itemFuel.getContainerItem(fuel));
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
	public AlloyRecipe getRecipe()
	{
		if(inventory.get(0).isEmpty()||inventory.get(1).isEmpty())
			return null;
		AlloyRecipe recipe = AlloyRecipe.findRecipe(inventory.get(0), inventory.get(1));
		if(recipe==null)
			return null;
		if(inventory.get(3).isEmpty()||(OreDictionary.itemMatches(inventory.get(3), recipe.output, true)&&inventory.get(3).getCount()+recipe.output.getCount() <= getSlotLimit(3)))
			return recipe;
		return null;
	}

	@Override
	public int[] getCurrentProcessesStep()
	{
		TileEntityAlloySmelter master = master();
		if(master!=this&&master!=null)
			return master.getCurrentProcessesStep();
		return new int[]{processMax-process};
	}

	@Override
	public int[] getCurrentProcessesMax()
	{
		TileEntityAlloySmelter master = master();
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
	public NonNullList<ItemStack> getInventory()
	{
		return this.inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return slot==0||slot==1&&TileEntityFurnace.isItemFuel(stack);
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

	@Override
	public BlockPos getOrigin()
	{
		return getPos().add(-offset[0], -offset[1], -offset[2]).offset(facing, -1).offset(facing.rotateYCCW());
	}
}