package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.*;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.CokeOvenLogic.State;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.util.CachedRecipe;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler.IOConstraint;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class CokeOvenLogic implements IServerTickableMultiblock<State>
{
	public static final BlockPos MASTER_OFFSET = new BlockPos(1, 1, 1);

	public static final int INPUT_SLOT = 0;
	public static final int OUTPUT_SLOT = 1;
	public static final int EMPTY_CONTAINER_SLOT = 2;
	public static final int FULL_CONTAINER_SLOT = 3;
	public static final int NUM_SLOTS = 4;
	public static final int TANK_CAPACITY = 12*FluidType.BUCKET_VOLUME;

	@Override
	public State createInitialState(IInitialMultiblockContext<State> capabilitySource)
	{
		return new State(capabilitySource);
	}

	@Override
	public void tickServer(IMultiblockContext<State> context)
	{
		final var state = context.getState();
		final var masterBlockState = context.getLevel().getBlock(MASTER_OFFSET);
		final boolean activeBeforeTick = masterBlockState.getValue(CokeOvenBlock.ACTIVE);
		boolean active = activeBeforeTick;
		if(state.process > 0)
		{
			if(state.inventory.getStackInSlot(INPUT_SLOT).isEmpty())
			{
				state.process = 0;
				state.processMax = 0;
			}
			else
			{
				CokeOvenRecipe recipe = getRecipe(context);
				if(recipe==null||recipe.time!=state.processMax)
				{
					state.process = 0;
					state.processMax = 0;
					active = false;
				}
				else
					state.process--;
			}
			context.markMasterDirty();
		}
		else
		{
			if(activeBeforeTick)
			{
				CokeOvenRecipe recipe = getRecipe(context);
				if(recipe!=null)
				{
					state.inventory.getStackInSlot(INPUT_SLOT).grow(-recipe.input.getCount());
					final var outputStack = state.inventory.getStackInSlot(OUTPUT_SLOT);
					if(!outputStack.isEmpty())
						outputStack.grow(recipe.output.get().copy().getCount());
					else if(outputStack.isEmpty())
						state.inventory.setStackInSlot(OUTPUT_SLOT, recipe.output.get().copy());
					state.tank.fill(
							new FluidStack(IEFluids.CREOSOTE.getStill(), recipe.creosoteOutput), FluidAction.EXECUTE
					);
				}
				state.processMax = 0;
				active = false;
			}
			CokeOvenRecipe recipe = getRecipe(context);
			if(recipe!=null)
			{
				state.process = recipe.time;
				state.processMax = state.process;
				active = true;
			}
		}

		if(state.tank.getFluidAmount() > 0&&FluidUtils.fillFluidContainer(
				state.tank,
				EMPTY_CONTAINER_SLOT, FULL_CONTAINER_SLOT,
				state.inventory::getStackInSlot, state.inventory::setStackInSlot
		))
			context.markMasterDirty();

		if(activeBeforeTick!=active)
		{
			context.markMasterDirty();
			for(int x = 0; x < 3; ++x)
				for(int y = 0; y < 3; ++y)
					for(int z = 0; z < 3; ++z)
					{
						final var pos = new BlockPos(x, y, z);
						final var oldState = context.getLevel().getBlock(pos);
						if(oldState.getBlock()==masterBlockState.getBlock())
							context.getLevel().setBlock(pos, oldState.setValue(CokeOvenBlock.ACTIVE, active));
					}
		}
	}

	@Nullable
	public CokeOvenRecipe getRecipe(IMultiblockContext<State> context)
	{
		final var state = context.getState();
		CokeOvenRecipe recipe = state.cachedRecipe.apply(context.getLevel().getRawLevel());
		if(recipe==null)
			return null;

		final var currentOutputStack = state.inventory.getStackInSlot(OUTPUT_SLOT);
		final boolean canOutputItem;
		if(currentOutputStack.isEmpty())
			canOutputItem = true;
		else if(!ItemHandlerHelper.canItemStacksStack(currentOutputStack, recipe.output.get()))
			canOutputItem = false;
		else
			canOutputItem = currentOutputStack.getCount()+recipe.output.get().getCount() <= 64;
		if(canOutputItem&&state.tank.getFluidAmount()+recipe.creosoteOutput <= state.tank.getCapacity())
			return recipe;
		return null;
	}

	@Override
	public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, BlockPos posInMultiblock, @Nullable RelativeBlockFace side, Capability<T> cap)
	{
		final var state = ctx.getState();
		if(cap==ForgeCapabilities.ITEM_HANDLER)
			return state.invCap.cast(ctx);
		else if(cap==ForgeCapabilities.FLUID_HANDLER)
			return state.fluidCap.cast(ctx);
		else
			return LazyOptional.empty();
	}

	@Override
	public InteractionResult click(
			IMultiblockContext<State> ctx,
			BlockPos posInMultiblock,
			Player player,
			InteractionHand hand,
			BlockHitResult absoluteHit,
			boolean isClient
	)
	{
		if(!isClient)
			player.openMenu(IEMenuTypes.COKE_OVEN_NEW.provide(ctx));
		return InteractionResult.SUCCESS;
	}

	@Override
	public Function<BlockPos, VoxelShape> shapeGetter()
	{
		return $ -> Shapes.block();
	}

	public static class State implements IMultiblockState, ContainerData
	{
		public static final int MAX_BURN_TIME = 0;
		public static final int BURN_TIME = 1;
		public static final int NUM_SLOTS = 2;

		private final FluidTank tank = new FluidTank(TANK_CAPACITY);
		private final SlotwiseItemHandler inventory;

		private final Function<Level, CokeOvenRecipe> cachedRecipe;
		private int process = 0;
		private int processMax = 0;

		private final StoredCapability<IItemHandler> invCap;
		private final StoredCapability<IFluidHandler> fluidCap;

		public State(IInitialMultiblockContext<State> ctx)
		{
			final var levelGetter = ctx.levelSupplier();
			inventory = new SlotwiseItemHandler(
					List.of(
							IOConstraint.input(i -> CokeOvenRecipe.findRecipe(levelGetter.get(), i)!=null),
							IOConstraint.OUTPUT,
							IOConstraint.input(Utils::isFluidRelatedItemStack),
							IOConstraint.OUTPUT
					),
					ctx::markMasterDirty
			);
			cachedRecipe = CachedRecipe.cachedSkip1(
					CokeOvenRecipe::findRecipe, () -> inventory.getStackInSlot(INPUT_SLOT)
			);
			this.invCap = new StoredCapability<>(this.inventory);
			this.fluidCap = new StoredCapability<>(
					new ArrayFluidHandler(new IFluidTank[]{tank}, true, false, ctx::markMasterDirty)
			);
		}

		@Override
		public void writeSaveNBT(CompoundTag nbt)
		{
			nbt.put("tank", tank.writeToNBT(new CompoundTag()));
			nbt.putInt("process", process);
			nbt.putInt("processMax", processMax);
			nbt.put("inventory", inventory.serializeNBT());
		}

		@Override
		public void readSaveNBT(CompoundTag nbt)
		{
			tank.readFromNBT(nbt.getCompound("tank"));
			process = nbt.getInt("process");
			processMax = nbt.getInt("processMax");
			inventory.deserializeNBT(nbt.getCompound("inventory"));
		}

		@Override
		public int get(int index)
		{
			return switch(index)
					{
						case MAX_BURN_TIME -> processMax;
						case BURN_TIME -> process;
						default -> throw new IllegalArgumentException("Unknown index "+index);
					};
		}

		@Override
		public void set(int index, int value)
		{
			switch(index)
			{
				case MAX_BURN_TIME -> processMax = value;
				case BURN_TIME -> process = value;
				default -> throw new IllegalArgumentException("Unknown index "+index);
			}
		}

		@Override
		public int getCount()
		{
			return NUM_SLOTS;
		}

		public FluidTank getTank()
		{
			return tank;
		}

		public SlotwiseItemHandler getInventory()
		{
			return inventory;
		}
	}
}
