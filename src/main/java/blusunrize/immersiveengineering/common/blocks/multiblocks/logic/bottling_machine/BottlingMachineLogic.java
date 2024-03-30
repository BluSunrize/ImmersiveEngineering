/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic.bottling_machine;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl.RSState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.bottling_machine.BottlingMachineLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInWorld;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor.InWorldProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext.ProcessContextInWorld;
import blusunrize.immersiveengineering.common.blocks.multiblocks.shapes.BottlingMachineShapes;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import blusunrize.immersiveengineering.common.util.DroppingMultiblockOutput;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class BottlingMachineLogic
		implements IMultiblockLogic<State>, IServerTickableComponent<State>, IClientTickableComponent<State>
{
	public static final float TRANSLATION_DISTANCE = 2.5f;
	private static final float STANDARD_TRANSPORT_TIME = 16f*(TRANSLATION_DISTANCE/2); //16 frames in conveyor animation, 1 frame/tick, 2.5 blocks of total translation distance, halved because transport time just affects half the distance
	private static final float STANDARD_LIFT_TIME = 3.75f;
	private static final float MIN_CYCLE_TIME = 60f; //set >= 2*(STANDARD_LIFT_TIME+STANDARD_TRANSPORT_TIME)

	private static final MultiblockFace OUTPUT_POS = new MultiblockFace(3, 1, 1, RelativeBlockFace.RIGHT);
	private static final CapabilityPosition ITEM_INPUT_POS = new CapabilityPosition(0, 1, 1, RelativeBlockFace.RIGHT);
	private static final CapabilityPosition FLUID_INPUT_POS_BACK = new CapabilityPosition(0, 0, 0, RelativeBlockFace.FRONT);
	private static final CapabilityPosition FLUID_INPUT_POS_SIDE = new CapabilityPosition(0, 0, 0, RelativeBlockFace.RIGHT);
	private static final CapabilityPosition ENERGY_INPUT_POS = new CapabilityPosition(2, 1, 0, RelativeBlockFace.UP);
	public static final BlockPos REDSTONE_POS = new BlockPos(1, 0, 1);

	@Override
	public void tickServer(IMultiblockContext<State> context)
	{
		final State state = context.getState();
		boolean active = state.processor.tickServer(state, context.getLevel(), state.rsState.isEnabled(context));
		state.active = active;
		if(active)
			// TODO syncing every tick while the machine is active is a hack. This was how it worked in the old system,
			//  but should be avoided
			context.requestMasterBESync();
	}

	@Override
	public void tickClient(IMultiblockContext<State> context)
	{
		final State state = context.getState();
		if(!state.active)
			return;
		final IMultiblockLevel level = context.getLevel();
		final Level rawLevel = level.getRawLevel();
		for(final MultiblockProcess<BottlingMachineRecipe, ProcessContextInWorld<BottlingMachineRecipe>> process : state.processor.getQueue())
		{
			Player localPlayer = ImmersiveEngineering.proxy.getClientPlayer();
			if(process.processTick==STANDARD_TRANSPORT_TIME-12)
			{
				final BlockPos soundPos = level.toAbsolute(new BlockPos(1, 1, 1));
				rawLevel.playSound(localPlayer, soundPos, IESounds.bottling.get(), SoundSource.BLOCKS, .125F, 0.8F);
				break;
			}
		}
	}

	@Override
	public State createInitialState(IInitialMultiblockContext<State> capabilitySource)
	{
		return new State(capabilitySource);
	}

	@Override
	public void onEntityCollision(IMultiblockContext<State> ctx, BlockPos posInMultiblock, Entity collided)
	{
		final Level level = collided.level();
		if(!new BlockPos(0, 1, 1).equals(posInMultiblock)||level.isClientSide)
			return;
		else if(!(collided instanceof ItemEntity)||!collided.isAlive())
			return;
		final State state = ctx.getState();
		final BlockPos absoluteCollisionPos = ctx.getLevel().toAbsolute(posInMultiblock);
		List<Pair<ItemEntity, ItemStack>> itemsOnConveyor = level.getEntitiesOfClass(
				ItemEntity.class, AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(absoluteCollisionPos))
		).stream().map(itemEntity1 -> Pair.of(itemEntity1, itemEntity1.getItem())).toList();
		if(itemsOnConveyor.isEmpty())
			return;

		ItemStack[] stacks = itemsOnConveyor.stream().map(Pair::getSecond).toArray(ItemStack[]::new);
		BottlingMachineRecipe recipe = BottlingMachineRecipe.findRecipe(level, state.tank.getFluid(), stacks);
		if(recipe==null&&!Utils.isFluidRelatedItemStack(stacks[0]))
			return;

		MultiblockProcessInWorld<BottlingMachineRecipe> process;
		NonNullList<ItemStack> displayStacks;
		if(recipe==null)
		{
			ItemStack inputItem = ItemHandlerHelper.copyStackWithSize(stacks[0], 1);
			displayStacks = Utils.createNonNullItemStackListFromItemStack(inputItem);
			process = new BottlingProcess(inputItem, inputItem.copy(), state);
		}
		else
		{
			displayStacks = recipe.getDisplayStacks(stacks);
			process = new BottlingProcess(recipe, displayStacks, state);
		}

		if(state.processor.addProcessToQueue(process, level, false))
			for(ItemStack stack : displayStacks)
				itemsOnConveyor.stream()
						.filter(t -> ItemStack.isSameItemSameTags(t.getSecond(), stack))
						.findFirst()
						.ifPresent(t -> {
							ItemStack remaining = t.getSecond().copy();
							remaining.shrink(stack.getCount());
							t.getFirst().setItem(remaining);
							if(remaining.isEmpty())
								t.getFirst().discard();
						});
	}

	@Override
	public InteractionResult click(
			IMultiblockContext<State> ctx, BlockPos posInMultiblock,
			Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient
	)
	{
		if(!player.getItemInHand(hand).is(IETags.hammers))
			return InteractionResult.PASS;
		if(!isClient)
		{
			final State state = ctx.getState();
			state.allowPartialFill = !state.allowPartialFill;
			player.displayClientMessage(Component.translatable(
					Lib.CHAT_INFO+"bottling_machine."+(state.allowPartialFill?"partialFill": "completeFill")
			), true);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap)
	{
		if(cap==ForgeCapabilities.ITEM_HANDLER&&ITEM_INPUT_POS.equals(position))
			return ctx.getState().itemInput.cast(ctx);
		else if(cap==ForgeCapabilities.FLUID_HANDLER&&(
				FLUID_INPUT_POS_BACK.equalsOrNullFace(position)||FLUID_INPUT_POS_SIDE.equals(position)
		))
			return ctx.getState().fluidInput.cast(ctx);
		else if(cap==ForgeCapabilities.ENERGY&&ENERGY_INPUT_POS.equalsOrNullFace(position))
			return ctx.getState().energyInput.cast(ctx);
		else
			return LazyOptional.empty();
	}

	@Override
	public void dropExtraItems(State state, Consumer<ItemStack> drop)
	{
		for(final MultiblockProcess<BottlingMachineRecipe, ?> process : state.processor.getQueue())
			if(process instanceof BottlingProcess bottlingProcess)
				bottlingProcess.inputItems.forEach(drop);
	}

	@Override
	public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType)
	{
		return BottlingMachineShapes.SHAPE_GETTER;
	}

	public static float getTransportTime(float processMaxTicks)
	{
		if(processMaxTicks >= MIN_CYCLE_TIME)
			return STANDARD_TRANSPORT_TIME;
		else
			return processMaxTicks*STANDARD_TRANSPORT_TIME/MIN_CYCLE_TIME;
	}

	public static float getLiftTime(float processMaxTicks)
	{
		if(processMaxTicks >= MIN_CYCLE_TIME)
			return STANDARD_LIFT_TIME;
		else
			return processMaxTicks*STANDARD_LIFT_TIME/MIN_CYCLE_TIME;
	}

	public static class State implements IMultiblockState, ProcessContextInWorld<BottlingMachineRecipe>
	{
		public final InWorldProcessor<BottlingMachineRecipe> processor;
		private final AveragingEnergyStorage energy = new AveragingEnergyStorage(16000);
		public final FluidTank tank = new FluidTank(8*FluidType.BUCKET_VOLUME);
		boolean allowPartialFill;
		// Only used on the client
		public boolean active;

		public final RSState rsState = RSState.enabledByDefault();
		private final DroppingMultiblockOutput output;
		private final StoredCapability<IItemHandler> itemInput;
		private final StoredCapability<IFluidHandler> fluidInput;
		private final StoredCapability<IEnergyStorage> energyInput;

		public State(IInitialMultiblockContext<State> ctx)
		{
			final Runnable markDirty = ctx.getMarkDirtyRunnable();
			final Runnable sync = ctx.getSyncRunnable();
			processor = new InWorldProcessor<>(
					2, maxTicks -> 1f-(getTransportTime(maxTicks)+getLiftTime(maxTicks))/maxTicks, 2,
					markDirty, sync,
					BottlingMachineRecipe.RECIPES::getById
			);
			output = new DroppingMultiblockOutput(OUTPUT_POS, ctx);
			itemInput = new StoredCapability<>(new BottlingInsertionHandler(ctx.levelSupplier(), processor, this));
			fluidInput = new StoredCapability<>(new ArrayFluidHandler(tank, false, true, () -> {
				markDirty.run();
				// TODO hack, see TODO comment in tickServer
				sync.run();
			}));
			energyInput = new StoredCapability<>(energy);
		}

		@Override
		public void writeSaveNBT(CompoundTag nbt)
		{
			nbt.put("processor", processor.toNBT());
			nbt.put("energy", energy.serializeNBT());
			nbt.put("tank", tank.writeToNBT(new CompoundTag()));
			nbt.putBoolean("allowPartialFill", allowPartialFill);
		}

		@Override
		public void readSaveNBT(CompoundTag nbt)
		{
			processor.fromNBT(nbt.get("processor"), BottlingProcess.loader(this));
			energy.deserializeNBT(nbt.get("energy"));
			tank.readFromNBT(nbt.getCompound("tank"));
			allowPartialFill = nbt.getBoolean("allowPartialFill");
		}

		@Override
		public void writeSyncNBT(CompoundTag nbt)
		{
			nbt.put("processor", processor.toNBT());
			nbt.put("tank", tank.writeToNBT(new CompoundTag()));
			nbt.putBoolean("active", active);
		}

		@Override
		public void readSyncNBT(CompoundTag nbt)
		{
			processor.fromNBT(nbt.get("processor"), BottlingProcess.loader(this));
			tank.readFromNBT(nbt.getCompound("tank"));
			active = nbt.getBoolean("active");
		}

		@Override
		public AveragingEnergyStorage getEnergy()
		{
			return energy;
		}

		@Override
		public void doProcessOutput(ItemStack result, IMultiblockLevel level)
		{
			this.output.insertOrDrop(result, level);
		}
	}
}
