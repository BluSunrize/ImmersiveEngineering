/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IActiveState;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IProcessBE;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.register.IEContainerTypes;
import blusunrize.immersiveengineering.common.register.IEContainerTypes.BEContainer;
import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.util.CachedRecipe;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

//TODO merge with blast furnace&alloy smelter to some degree?
public class CokeOvenBlockEntity extends MultiblockPartBlockEntity<CokeOvenBlockEntity> implements IIEInventory,
		IActiveState, IInteractionObjectIE<CokeOvenBlockEntity>, IProcessBE, IBlockBounds
{
	public static final int INPUT_SLOT = 0;
	public static final int OUTPUT_SLOT = 1;
	public static final int EMPTY_CONTAINER_SLOT = 2;
	public static final int FULL_CONTAINER_SLOT = 3;

	public FluidTank tank = new FluidTank(12*FluidAttributes.BUCKET_VOLUME);
	private final NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
	private final Supplier<CokeOvenRecipe> cachedRecipe = CachedRecipe.cached(
			CokeOvenRecipe::findRecipe, () -> inventory.get(INPUT_SLOT)
	);
	public int process = 0;
	public int processMax = 0;
	public CokeOvenData guiData = new CokeOvenData();

	public CokeOvenBlockEntity(BlockEntityType<CokeOvenBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(IEMultiblocks.COKE_OVEN, type, false, pos, state);
	}

	@Override
	public boolean canUseGui(Player player)
	{
		return formed;
	}

	@Override
	public CokeOvenBlockEntity getGuiMaster()
	{
		return master();
	}

	@Override
	public BEContainer<CokeOvenBlockEntity, ?> getContainerType()
	{
		return IEContainerTypes.COKE_OVEN;
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return Shapes.block();
	}

	@Override
	public void tickServer()
	{
		super.tickServer();
		final boolean activeBeforeTick = getIsActive();
		if(process > 0)
		{
			if(inventory.get(INPUT_SLOT).isEmpty())
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
					Utils.modifyInvStackSize(inventory, INPUT_SLOT, -recipe.input.getCount());
					if(!inventory.get(OUTPUT_SLOT).isEmpty())
						inventory.get(OUTPUT_SLOT).grow(recipe.output.copy().getCount());
					else if(inventory.get(OUTPUT_SLOT).isEmpty())
						inventory.set(OUTPUT_SLOT, recipe.output.copy());
					this.tank.fill(new FluidStack(IEFluids.CREOSOTE.getStill(), recipe.creosoteOutput), FluidAction.EXECUTE);
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

		ItemStack inFullSlot = inventory.get(FULL_CONTAINER_SLOT);
		if(tank.getFluidAmount() > 0&&(inFullSlot.isEmpty()||inFullSlot.getCount()+1 <= inFullSlot.getMaxStackSize()))
		{
			ItemStack filledContainer = Utils.fillFluidContainer(
					tank,
					inventory.get(EMPTY_CONTAINER_SLOT).copy(),
					inFullSlot,
					null
			);
			if(!filledContainer.isEmpty())
			{
				if(inventory.get(EMPTY_CONTAINER_SLOT).getCount()==1&&!Utils.isFluidContainerFull(filledContainer))
					inventory.set(EMPTY_CONTAINER_SLOT, filledContainer.copy());
				else
				{
					if(!inFullSlot.isEmpty()&&ItemHandlerHelper.canItemStacksStack(inFullSlot, filledContainer))
						inFullSlot.grow(filledContainer.getCount());
					else if(inFullSlot.isEmpty())
						inventory.set(FULL_CONTAINER_SLOT, filledContainer.copy());
					Utils.modifyInvStackSize(inventory, EMPTY_CONTAINER_SLOT, -filledContainer.getCount());
				}
				setChanged();
				this.markContainingBlockForUpdate(null);
			}
		}

		final boolean activeAfterTick = getIsActive();
		if(activeBeforeTick!=activeAfterTick)
		{
			this.setChanged();
			for(int x = 0; x < 3; ++x)
				for(int y = 0; y < 3; ++y)
					for(int z = 0; z < 3; ++z)
					{
						BlockPos actualPos = getBlockPosForPos(new BlockPos(x, y, z));
						BlockEntity te = Utils.getExistingTileEntity(level, actualPos);
						if(te instanceof CokeOvenBlockEntity)
							((CokeOvenBlockEntity)te).setActive(activeAfterTick);
					}
		}
	}

	@Nullable
	public CokeOvenRecipe getRecipe()
	{
		CokeOvenRecipe recipe = cachedRecipe.get();
		if(recipe==null)
			return null;

		if(inventory.get(OUTPUT_SLOT).isEmpty()||(ItemStack.isSame(inventory.get(OUTPUT_SLOT), recipe.output)&&
				inventory.get(OUTPUT_SLOT).getCount()+recipe.output.getCount() <= getSlotLimit(OUTPUT_SLOT)))
			if(tank.getFluidAmount()+recipe.creosoteOutput <= tank.getCapacity())
				return recipe;
		return null;
	}

	@Override
	public int[] getCurrentProcessesStep()
	{
		CokeOvenBlockEntity master = master();
		if(master!=this&&master!=null)
			return master.getCurrentProcessesStep();
		return new int[]{processMax-process};
	}

	@Override
	public int[] getCurrentProcessesMax()
	{
		CokeOvenBlockEntity master = master();
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
		this.markContainingBlockForUpdate(null);
		return true;
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);

		tank.readFromNBT(nbt.getCompound("tank"));
		if(!descPacket)
		{
			ContainerHelper.loadAllItems(nbt, inventory);
			process = nbt.getInt("process");
			processMax = nbt.getInt("processMax");
		}
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);

		CompoundTag tankTag = tank.writeToNBT(new CompoundTag());
		nbt.put("tank", tankTag);
		if(!descPacket)
		{
			nbt.putInt("process", process);
			nbt.putInt("processMax", processMax);
			ContainerHelper.saveAllItems(nbt, inventory);
		}
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(Direction side)
	{
		CokeOvenBlockEntity master = master();
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
		CokeOvenBlockEntity master = master();
		if(master!=null&&master.formed&&formed)
			return master.inventory;
		return this.inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		if(stack.isEmpty())
			return false;
		if(slot==INPUT_SLOT)
			return CokeOvenRecipe.findRecipe(stack)!=null;
		if(slot==EMPTY_CONTAINER_SLOT)
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
			CokeOvenBlockEntity master = master();
			if(master!=null)
				return master.invHandler.cast();
		}
		return super.getCapability(capability, facing);
	}

	public class CokeOvenData implements ContainerData
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
		public int getCount()
		{
			return 2;
		}
	}
}