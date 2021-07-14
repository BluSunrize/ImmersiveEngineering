/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceFuel;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IActiveState;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IProcessTile;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import blusunrize.immersiveengineering.common.util.CachedRecipe;
import blusunrize.immersiveengineering.common.util.Utils;
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
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class BlastFurnaceTileEntity extends MultiblockPartTileEntity<BlastFurnaceTileEntity> implements IIEInventory,
		IActiveState, IInteractionObjectIE, IProcessTile, IBlockBounds
{
	NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
	public int process = 0;
	public int processMax = 0;
	public int burnTime = 0;
	public int lastBurnTime = 0;
	private final BlastFurnaceState state = new BlastFurnaceState();
	private final Supplier<BlastFurnaceRecipe> cachedRecipe = CachedRecipe.cached(
			BlastFurnaceRecipe::findRecipe, () -> inventory.get(0)
	);

	public BlastFurnaceTileEntity()
	{
		super(IEMultiblocks.BLAST_FURNACE, IETileTypes.BLAST_FURNACE.get(), false);
	}

	protected BlastFurnaceTileEntity(IETemplateMultiblock mb, TileEntityType<? extends BlastFurnaceTileEntity> type)
	{
		super(mb, type, false);
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
	public VoxelShape getBlockBounds(@Nullable ISelectionContext ctx)
	{
		return VoxelShapes.fullCube();
	}

	@Override
	public void tick()
	{
		checkForNeedlessTicking();
		if(!world.isRemote&&formed&&!isDummy())
		{
			boolean activeBeforeTick = getIsActive();

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
							setActive(false);
						}
						else
						{
							process -= processSpeed;
							processSpeed = 0;//Process speed is "used up"
							if(!activeBeforeTick)
								setActive(true);
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
							Utils.modifyInvStackSize(inventory, 0, -recipe.input.getCount());
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
						setActive(true);
					}
				}
			}
			else
			{
				if(activeBeforeTick)
					setActive(false);
			}

			if(burnTime <= 0&&getRecipe()!=null)
			{
				if(BlastFurnaceFuel.isValidBlastFuel(inventory.get(1)))
				{
					lastBurnTime = BlastFurnaceFuel.getBlastFuelTime(inventory.get(1));
					burnTime += lastBurnTime;
					Utils.modifyInvStackSize(inventory, 1, -1);
					markContainingBlockForUpdate(null);
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
							if(te instanceof BlastFurnaceTileEntity)
								((BlastFurnaceTileEntity)te).setActive(activeAfterTick);
						}
			}
		}
	}

	@Nullable
	public BlastFurnaceRecipe getRecipe()
	{
		BlastFurnaceRecipe recipe = cachedRecipe.get();
		if(recipe==null)
			return null;
		if((inventory.get(2).isEmpty()||(ItemStack.areItemsEqual(inventory.get(2), recipe.output)&&inventory.get(2).getCount()+recipe.output.getCount() <= getSlotLimit(2)))
				&&(inventory.get(3).isEmpty()||(ItemStack.areItemsEqual(inventory.get(3), recipe.slag)&&inventory.get(3).getCount()+recipe.slag.getCount() <= getSlotLimit(3))))
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
		markDirty();
		markContainingBlockForUpdate(null);
		return true;
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		if(!descPacket)
		{
			process = nbt.getInt("process");
			processMax = nbt.getInt("processMax");
			burnTime = nbt.getInt("burnTime");
			lastBurnTime = nbt.getInt("lastBurnTime");
			ItemStackHelper.loadAllItems(nbt, inventory);
		}
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		if(!descPacket)
		{
			nbt.putInt("process", process);
			nbt.putInt("processMax", processMax);
			nbt.putInt("burnTime", burnTime);
			nbt.putInt("lastBurnTime", lastBurnTime);
			ItemStackHelper.saveAllItems(nbt, inventory);
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
		return slot==0?BlastFurnaceRecipe.findRecipe(stack)!=null: slot==1&&BlastFurnaceFuel.isValidBlastFuel(stack);
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

	public BlastFurnaceState getGuiInts()
	{
		return state;
	}

	public class BlastFurnaceState implements IIntArray
	{
		public static final int LAST_BURN_TIME = 0;
		public static final int BURN_TIME = 1;
		public static final int PROCESS_MAX = 2;
		public static final int CURRENT_PROCESS = 3;

		public int getLastBurnTime()
		{
			return get(LAST_BURN_TIME);
		}

		public int getBurnTime()
		{
			return get(BURN_TIME);
		}

		public int getMaxProcess()
		{
			return get(PROCESS_MAX);
		}

		public int getProcess()
		{
			return get(CURRENT_PROCESS);
		}

		@Override
		public int get(int index)
		{
			switch(index)
			{
				case LAST_BURN_TIME:
					return lastBurnTime;
				case BURN_TIME:
					return burnTime;
				case PROCESS_MAX:
					return processMax;
				case CURRENT_PROCESS:
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
				case LAST_BURN_TIME:
					lastBurnTime = value;
					break;
				case BURN_TIME:
					burnTime = value;
					break;
				case PROCESS_MAX:
					processMax = value;
					break;
				case CURRENT_PROCESS:
					process = value;
					break;
				default:
					throw new IllegalArgumentException("Unknown index "+index);
			}
		}

		@Override
		public int size()
		{
			return 4;
		}
	}
}