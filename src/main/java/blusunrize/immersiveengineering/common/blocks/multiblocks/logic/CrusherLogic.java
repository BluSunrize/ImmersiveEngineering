/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.ComparatorManager;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl.RSState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.CrusherLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.DirectProcessingItemHandler;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInWorld;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext.ProcessContextInWorld;
import blusunrize.immersiveengineering.common.blocks.multiblocks.shapes.CrusherShapes;
import blusunrize.immersiveengineering.common.util.DroppingMultiblockOutput;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.sound.MultiblockSound;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

public class CrusherLogic implements
		IMultiblockLogic<State>, IServerTickableComponent<State>, IClientTickableComponent<State>
{
	public static final BlockPos MASTER_OFFSET = new BlockPos(2, 1, 1);
	public static final BlockPos REDSTONE_POS = new BlockPos(0, 1, 2);
	private static final MultiblockFace OUTPUT_POS = new MultiblockFace(2, 0, 3, RelativeBlockFace.FRONT);
	private static final CapabilityPosition ENERGY_INPUT = new CapabilityPosition(4, 1, 1, RelativeBlockFace.UP);
	private static final Vec3[] PARTICLE_POSITIONS = {
			new Vec3(2, 2.125, 1.5),
			new Vec3(2.5, 2.125, 1.5),
			new Vec3(3, 2.125, 1.5),
	};

	@Override
	public State createInitialState(IInitialMultiblockContext<State> capabilitySource)
	{
		return new State(capabilitySource);
	}

	@Override
	public void tickServer(IMultiblockContext<State> context)
	{
		final State state = context.getState();
		final boolean wasActive = state.renderAsActive;
		state.renderAsActive = state.processor.tickServer(state, context.getLevel(), state.rsState.isEnabled(context));
		if(wasActive!=state.renderAsActive)
			context.requestMasterBESync();
		if(state.renderAsActive)
			spawnParticles(context.getLevel(), state);
	}

	public void spawnParticles(IMultiblockLevel level, State state)
	{
		if(state.processor.getQueueSize()==0||!(level.getRawLevel() instanceof ServerLevel serverLevel))
			return;
		final MultiblockProcess<CrusherRecipe, ?> particleProcess = state.processor.getQueue().get(0);
		if(!(particleProcess instanceof MultiblockProcessInWorld<?> inWorld)||inWorld.inputItems.isEmpty())
			return;
		final ItemStack particleStack = inWorld.inputItems.get(0);
		final ItemParticleOption particleData = new ItemParticleOption(ParticleTypes.ITEM, particleStack);
		for(final Vec3 relativeOffset : PARTICLE_POSITIONS)
		{
			final Vec3 absolutePos = level.toAbsolute(relativeOffset);
			serverLevel.sendParticles(
					particleData, absolutePos.x, absolutePos.y, absolutePos.z, 8, 0, 0, 0, 0.0625
			);
		}
	}

	@Override
	public void tickClient(IMultiblockContext<State> context)
	{
		final State state = context.getState();
		if(state.renderAsActive)
			state.barrelAngle = (state.barrelAngle+18)%360;
		if(!state.isPlayingSound.getAsBoolean())
		{
			final Vec3 soundPos = context.getLevel().toAbsolute(new Vec3(2.5, 1.5, 1.5));
			state.isPlayingSound = MultiblockSound.startSound(
					() -> state.renderAsActive, context.isValid(), soundPos, IESounds.crusher, 0.5f
			);
		}
	}

	@Override
	public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap)
	{
		final State state = ctx.getState();
		if(cap==ForgeCapabilities.ITEM_HANDLER&&isInInput(position.posInMultiblock(), false))
			return state.insertionHandler.cast(ctx);
		if(cap==ForgeCapabilities.ENERGY&&ENERGY_INPUT.equalsOrNullFace(position))
			return state.energyHandler.cast(ctx);
		return LazyOptional.empty();
	}

	@Override
	public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType)
	{
		return CrusherShapes.SHAPE_GETTER;
	}

	private static boolean isInInput(BlockPos posInMultiblock, boolean allowMiddleLayer)
	{
		if(posInMultiblock.getY()==2||(allowMiddleLayer&&posInMultiblock.getY()==1))
			return posInMultiblock.getX() > 0&&posInMultiblock.getX() < 4;
		return false;
	}

	@Override
	public void onEntityCollision(IMultiblockContext<State> ctx, BlockPos posInMultiblock, Entity collided)
	{
		if(collided.level().isClientSide||!isInInput(posInMultiblock, true))
			return;
		final State state = ctx.getState();
		if(!collided.isAlive()||!state.rsState.isEnabled(ctx))
			return;
		final IMultiblockLevel level = ctx.getLevel();
		final AABB internalBB = new AABB(1.9375, 1.25, 0.9375, 3.0625, 2.5, 2.0625);
		final AABB crusherInternal = level.toAbsolute(internalBB);
		if(!collided.getBoundingBox().intersects(crusherInternal))
			return;
		if(collided instanceof ItemEntity itemEntity)
		{
			ItemStack stack = itemEntity.getItem();
			if(stack.isEmpty())
				return;
			final ItemStack remaining = state.insertionHandler.getValue().insertItem(0, stack, false);
			if(remaining.isEmpty())
				itemEntity.discard();
			else
				itemEntity.setItem(remaining);
		}
		else if(collided instanceof LivingEntity&&(!(collided instanceof Player player)||!player.getAbilities().invulnerable))
		{
			int consumed = state.energy.extractEnergy(80, false);
			if(consumed > 0)
			{
				EventHandler.crusherMap.put(collided.getUUID(), s -> state.doProcessOutput(s, level));
				collided.hurt(IEDamageSources.crusher(collided.level()), consumed/20f);
			}
		}
	}

	public static ComparatorManager<State> makeComparator()
	{
		return ComparatorManager.makeSimple(state -> {
			float fill = state.processor.getQueueSize()/(float)state.processor.getMaxQueueSize();
			return Mth.ceil(fill*14.0F)+(fill > 0?1: 0);
		}, REDSTONE_POS);
	}

	public static class State implements IMultiblockState, ProcessContextInWorld<CrusherRecipe>
	{
		private final AveragingEnergyStorage energy = new AveragingEnergyStorage(32000);
		private final MultiblockProcessor<CrusherRecipe, ProcessContextInWorld<CrusherRecipe>> processor;
		private boolean renderAsActive;
		private float barrelAngle;
		public final RSState rsState = RSState.enabledByDefault();

		private final DroppingMultiblockOutput output;
		private final StoredCapability<IItemHandler> insertionHandler;
		private final StoredCapability<IEnergyStorage> energyHandler = new StoredCapability<>(energy);
		private BooleanSupplier isPlayingSound = () -> false;

		public State(IInitialMultiblockContext<State> ctx)
		{
			this.output = new DroppingMultiblockOutput(OUTPUT_POS, ctx);
			this.processor = new MultiblockProcessor<>(
					2048, 0, 1, ctx.getMarkDirtyRunnable(), CrusherRecipe.RECIPES::getById
			);
			final DirectProcessingItemHandler<CrusherRecipe> insertionHandler = new DirectProcessingItemHandler<>(
					ctx.levelSupplier(), processor, CrusherRecipe::findRecipe
			).setProcessStacking(true);
			this.insertionHandler = new StoredCapability<>(insertionHandler);
		}

		@Override
		public void writeSaveNBT(CompoundTag nbt)
		{
			nbt.put("energy", energy.serializeNBT());
			nbt.put("processor", processor.toNBT());
		}

		@Override
		public void readSaveNBT(CompoundTag nbt)
		{
			energy.deserializeNBT(nbt.get("energy"));
			processor.fromNBT(nbt.get("processor"), MultiblockProcessInWorld::new);
		}

		@Override
		public void writeSyncNBT(CompoundTag nbt)
		{
			nbt.putBoolean("renderActive", renderAsActive);
		}

		@Override
		public void readSyncNBT(CompoundTag nbt)
		{
			renderAsActive = nbt.getBoolean("renderActive");
		}

		@Override
		public void doProcessOutput(ItemStack output, IMultiblockLevel level)
		{
			this.output.insertOrDrop(output, level);
		}

		@Override
		public AveragingEnergyStorage getEnergy()
		{
			return energy;
		}

		public boolean shouldRenderActive()
		{
			return renderAsActive;
		}

		public float getBarrelAngle()
		{
			return barrelAngle;
		}

		public List<MultiblockProcess<CrusherRecipe, ProcessContextInWorld<CrusherRecipe>>> getProcessQueue()
		{
			return processor.getQueue();
		}
	}
}
