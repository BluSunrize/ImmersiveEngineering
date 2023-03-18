/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.metal.BlastFurnacePreheaterBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.AdvBlastFurnaceLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.FurnaceHandler.IFurnaceEnvironment;
import blusunrize.immersiveengineering.common.blocks.multiblocks.shapes.AdvBlastFurnaceShapes;
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.WrappingItemHandler;
import blusunrize.immersiveengineering.common.util.inventory.WrappingItemHandler.IntRange;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public class AdvBlastFurnaceLogic
		implements IMultiblockLogic<State>, IServerTickableComponent<State>, IClientTickableComponent<State>
{
	private static final Vec3 SMOKE_POS = new Vec3(1.5, 3.9, 1.5);
	private static final BlockPos[] HEATER_OFFSETS = {
			new BlockPos(-1, 0, 1), new BlockPos(3, 0, 1)
	};
	private static final MultiblockFace OUTPUT_OFFSET = new MultiblockFace(1, 0, -1, RelativeBlockFace.FRONT);
	private static final MultiblockFace SLAG_OUTPUT_OFFSET = new MultiblockFace(1, 0, 3, RelativeBlockFace.BACK);
	private static final CapabilityPosition OUTPUT_CAP = CapabilityPosition.opposing(OUTPUT_OFFSET);
	private static final CapabilityPosition SLAG_OUTPUT_CAP = CapabilityPosition.opposing(SLAG_OUTPUT_OFFSET);
	private static final CapabilityPosition INPUT_CAP = new CapabilityPosition(1, 3, 1, RelativeBlockFace.UP);

	@Override
	public void tickServer(IMultiblockContext<State> context)
	{
		final IMultiblockLevel level = context.getLevel();
		final boolean wasActive = isActive(level);
		final boolean active = context.getState().innerState.furnace.tickServer(context);
		if(active!=wasActive)
			NonMirrorableWithActiveBlock.setActive(level, IEMultiblocks.ADVANCED_BLAST_FURNACE, active);
		if(!level.shouldTickModulo(8))
			return;
		final State state = context.getState();
		final IItemHandlerModifiable inventory = state.innerState.getInventory();
		ItemStack stack = inventory.getStackInSlot(2);
		if(!stack.isEmpty())
		{
			stack = Utils.insertStackIntoInventory(state.outputRef, stack, false);
			inventory.setStackInSlot(2, stack);
		}
		stack = inventory.getStackInSlot(3);
		if(!stack.isEmpty())
		{
			stack = Utils.insertStackIntoInventory(state.slagRef, stack, false);
			inventory.setStackInSlot(3, stack);
		}
	}

	@Override
	public void tickClient(IMultiblockContext<State> context)
	{
		final IMultiblockLevel level = context.getLevel();
		if(isActive(level))
		{
			final Vec3 particlePos = level.toAbsolute(SMOKE_POS);
			level.getRawLevel().addAlwaysVisibleParticle(
					ParticleTypes.CAMPFIRE_COSY_SMOKE,
					particlePos.x, particlePos.y, particlePos.z,
					ApiUtils.RANDOM.nextDouble(-0.00625, 0.00625), .05, ApiUtils.RANDOM.nextDouble(-0.00625, 0.00625)
			);
		}
	}

	private boolean isActive(IMultiblockLevel level)
	{
		return level.getBlockState(IEMultiblocks.ADVANCED_BLAST_FURNACE.getMasterFromOriginOffset())
				.getValue(NonMirrorableWithActiveBlock.ACTIVE);
	}

	@Override
	public State createInitialState(IInitialMultiblockContext<State> capabilitySource)
	{
		return new State(capabilitySource);
	}

	@Override
	public <T>
	LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap)
	{
		if(cap==ForgeCapabilities.ITEM_HANDLER)
		{
			final State state = ctx.getState();
			if(OUTPUT_CAP.equals(position))
				return state.outputHandler.cast(ctx);
			else if(SLAG_OUTPUT_CAP.equals(position))
				return state.slagHandler.cast(ctx);
			else if(INPUT_CAP.equals(position))
				return state.inputHandler.cast(ctx);
		}
		return LazyOptional.empty();
	}

	@Override
	public void dropExtraItems(State state, Consumer<ItemStack> drop)
	{
		MBInventoryUtils.dropItems(state.getInventory(), drop);
	}

	@Override
	public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType)
	{
		return AdvBlastFurnaceShapes.SHAPE_GETTER;
	}

	public static class State implements IMultiblockState, IFurnaceEnvironment<BlastFurnaceRecipe>
	{
		private final BlastFurnaceLogic.State innerState;
		private final CapabilityReference<IItemHandler> outputRef;
		private final CapabilityReference<IItemHandler> slagRef;
		private final StoredCapability<IItemHandler> inputHandler;
		private final StoredCapability<IItemHandler> outputHandler;
		private final StoredCapability<IItemHandler> slagHandler;

		public State(IInitialMultiblockContext<State> ctx)
		{
			this.innerState = new BlastFurnaceLogic.State(ctx);
			this.outputRef = ctx.getCapabilityAt(ForgeCapabilities.ITEM_HANDLER, OUTPUT_OFFSET);
			this.slagRef = ctx.getCapabilityAt(ForgeCapabilities.ITEM_HANDLER, SLAG_OUTPUT_OFFSET);
			this.inputHandler = new StoredCapability<>(new WrappingItemHandler(
					getInventory(), true, false, new IntRange(0, 2)
			));
			this.outputHandler = new StoredCapability<>(new WrappingItemHandler(
					getInventory(), false, true, new IntRange(2, 3)
			));
			this.slagHandler = new StoredCapability<>(new WrappingItemHandler(
					getInventory(), false, true, new IntRange(3, 4)
			));
		}

		@Override
		public void writeSaveNBT(CompoundTag nbt)
		{
			innerState.writeSaveNBT(nbt);
		}

		@Override
		public void readSaveNBT(CompoundTag nbt)
		{
			innerState.readSaveNBT(nbt);
		}

		@Override
		public IItemHandlerModifiable getInventory()
		{
			return innerState.getInventory();
		}

		@Override
		public @Nullable BlastFurnaceRecipe getRecipeForInput()
		{
			return innerState.getRecipeForInput();
		}

		@Override
		public int getBurnTimeOf(Level level, ItemStack fuel)
		{
			return innerState.getBurnTimeOf(level, fuel);
		}

		@Override
		public int getProcessSpeed(IMultiblockLevel level)
		{
			int i = 1;
			for(final BlockPos offset : HEATER_OFFSETS)
			{
				final BlastFurnacePreheaterBlockEntity preheater = getPreheater(level, offset);
				if(preheater!=null)
					i += preheater.doSpeedup();
			}
			return i;
		}

		@Override
		public void turnOff(IMultiblockLevel level)
		{
			for(final BlockPos offset : HEATER_OFFSETS)
			{
				final BlastFurnacePreheaterBlockEntity preheater = getPreheater(level, offset);
				if(preheater!=null)
					preheater.turnOff();
			}
		}

		@Nullable
		public BlastFurnacePreheaterBlockEntity getPreheater(IMultiblockLevel level, BlockPos pos)
		{
			BlockEntity te = level.getBlockEntity(pos);
			return te instanceof BlastFurnacePreheaterBlockEntity heater?heater: null;
		}

		public GetterAndSetter<Boolean> preheaterActive(IMultiblockLevel level, int index)
		{
			return GetterAndSetter.getterOnly(() -> {
				final BlastFurnacePreheaterBlockEntity heater = getPreheater(level, HEATER_OFFSETS[index]);
				return heater!=null&&heater.active;
			});
		}

		public ContainerData getStateView()
		{
			return innerState.getStateView();
		}
	}
}
