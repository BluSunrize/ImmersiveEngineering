/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IActiveState;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IProcessTile;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.util.CachedRecipe;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class AlloySmelterTileEntity extends MultiblockPartTileEntity<AlloySmelterTileEntity> implements IIEInventory,
		IActiveState, IInteractionObjectIE, IProcessTile, IBlockBounds
{
	NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
	public int process = 0;
	public int processMax = 0;
	public int burnTime = 0;
	public int lastBurnTime = 0;
	public final AlloySmelterState guiState = new AlloySmelterState();
	private final Supplier<AlloyRecipe> cachedModel = CachedRecipe.cached(
			AlloyRecipe::findRecipe, () -> inventory.get(0), () -> inventory.get(1)
	);

	public AlloySmelterTileEntity()
	{
		super(IEMultiblocks.ALLOY_SMELTER, IETileTypes.ALLOY_SMELTER.get(), false);
	}

	@Override
	public boolean canUseGui(Player player)
	{
		return formed;
	}

	@Override
	public IInteractionObjectIE getGuiMaster()
	{
		return master();
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return Shapes.block();
	}

	@Override
	public void tick()
	{
		checkForNeedlessTicking();
		if(!level.isClientSide&&formed&&!isDummy())
		{
			final boolean activeBeforeTick = getIsActive();

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
							setActive(false);
						}
						else
						{
							process--;
							doneWork = true;
							if(!activeBeforeTick)
								setActive(true);
						}
					}
					markContainingBlockForUpdate(null);
				}
				burnTime--;

				if(process <= 0)
				{
					if(processMax > 0)
					{
						AlloyRecipe recipe = getRecipe();
						if(recipe!=null)
						{
							boolean flip = !recipe.input0.test(inventory.get(0));
							Utils.modifyInvStackSize(inventory, flip?1: 0, -recipe.input0.getCount());
							Utils.modifyInvStackSize(inventory, flip?0: 1, -recipe.input1.getCount());

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
						setActive(true);
					}
				}
			}
			else
			{
				if(activeBeforeTick)
					setActive(false);
			}

			if(burnTime <= 10&&getRecipe()!=null)
			{
				ItemStack fuel = inventory.get(2);
				int newBurnTime = ForgeHooks.getBurnTime(fuel);
				if(newBurnTime > 0)
				{
					lastBurnTime = newBurnTime;
					burnTime += lastBurnTime;
					Item itemFuel = fuel.getItem();
					fuel.shrink(1);
					if(fuel.isEmpty())
						inventory.set(2, itemFuel.getContainerItem(fuel));
					markContainingBlockForUpdate(null);
				}
			}

			final boolean activeAfterTick = getIsActive();
			if(activeBeforeTick!=activeAfterTick)
			{

				this.setChanged();
				for(int x = 0; x < 2; ++x)
					for(int y = 0; y < 2; ++y)
						for(int z = 0; z < 2; ++z)
						{
							BlockPos actualPos = getBlockPosForPos(new BlockPos(x, y, z));
							BlockEntity te = Utils.getExistingTileEntity(level, actualPos);
							if(te instanceof AlloySmelterTileEntity)
								((AlloySmelterTileEntity)te).setActive(activeAfterTick);
						}

			}
		}
	}

	@Nullable
	public AlloyRecipe getRecipe()
	{
		AlloyRecipe recipe = cachedModel.get();
		if(recipe==null)
			return null;
		ItemStack output = inventory.get(3);
		if(output.isEmpty()||(ItemStack.isSame(output, recipe.output)&&
				output.getCount()+recipe.output.getCount() <= getSlotLimit(3)))
			return recipe;
		return null;
	}

	@Override
	public int[] getCurrentProcessesStep()
	{
		AlloySmelterTileEntity master = master();
		if(master!=this&&master!=null)
			return master.getCurrentProcessesStep();
		return new int[]{processMax-process};
	}

	@Override
	public int[] getCurrentProcessesMax()
	{
		AlloySmelterTileEntity master = master();
		if(master!=this&&master!=null)
			return master.getCurrentProcessesMax();
		return new int[]{processMax};
	}

	@Override
	public boolean triggerEvent(int id, int arg)
	{
		if(id==0)
			this.formed = arg==1;
		setChanged();
		markContainingBlockForUpdate(null);
		return true;
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		if(!descPacket)
		{
			ContainerHelper.loadAllItems(nbt, inventory);
			process = nbt.getInt("process");
			processMax = nbt.getInt("processMax");
			burnTime = nbt.getInt("burnTime");
			lastBurnTime = nbt.getInt("lastBurnTime");
		}
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		if(!descPacket)
		{
			nbt.putInt("process", process);
			nbt.putInt("processMax", processMax);
			nbt.putInt("burnTime", burnTime);
			nbt.putInt("lastBurnTime", lastBurnTime);
			ContainerHelper.saveAllItems(nbt, inventory);
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
		return slot==0||slot==1&&FurnaceBlockEntity.isFuel(stack);
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}

	@Override
	public void doGraphicalUpdates()
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

	//Based on AbstractFurnaceTileEntity#getBurnTime, which is non-static protected now
	private static int getBurnTime(ItemStack stack)
	{
		if(stack.isEmpty())
			return 0;
		else
		{
			return ForgeHooks.getBurnTime(stack);
		}
	}

	public class AlloySmelterState implements ContainerData
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
		public int getCount()
		{
			return 4;
		}
	}
}