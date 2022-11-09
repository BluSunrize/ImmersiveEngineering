package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.*;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.CrusherLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.DirectProcessingItemHandler;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInWorld;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor.RecipeSource;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext.ProcessContextInWorld;
import blusunrize.immersiveengineering.common.blocks.multiblocks.shapes.CrusherShapes;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.function.Function;

public class CrusherLogic implements IServerTickableMultiblock<State>, IClientTickableMultiblock<State>
{
	public static final BlockPos MASTER_OFFSET = new BlockPos(2, 1, 1);

	@Override
	public State createInitialState(MultiblockCapabilitySource capabilitySource)
	{
		return new State(capabilitySource);
	}

	@Override
	public void tickServer(IMultiblockContext<State> context)
	{
		final var state = context.getState();
		// TODO redstone disabling
		// TODO comparator values
		// TODO sound
		final var wasActive = state.renderAsActive;
		state.renderAsActive = state.processor.tickServer(state, context.getLevel(), true);
		if(wasActive!=state.renderAsActive)
			context.requestMasterBESync();
	}

	@Override
	public void tickClient(IMultiblockContext<State> context)
	{
		final var state = context.getState();
		if(state.renderAsActive)
			state.barrelAngle = (state.barrelAngle+18)%360;
	}

	@Override
	public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, BlockPos posInMultiblock, @Nullable RelativeBlockFace side, Capability<T> cap)
	{
		final var state = ctx.getState();
		if(cap==ForgeCapabilities.ITEM_HANDLER&&isInInput(posInMultiblock, false))
			return state.insertionHandler.cast(ctx);
		if(cap==ForgeCapabilities.ENERGY)
			if(side==null||(posInMultiblock.equals(new BlockPos(4, 1, 1))&&side==RelativeBlockFace.UP))
				return state.energyHandler.cast(ctx);
		return LazyOptional.empty();
	}

	@Override
	public Function<BlockPos, VoxelShape> shapeGetter()
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
		if(collided.level.isClientSide||!isInInput(posInMultiblock, true))
			return;
		if(!collided.isAlive())//TODO ||isRSDisabled())
			return;
		final var state = ctx.getState();
		final var level = ctx.getLevel();
		final var internalBB = new AABB(1.4375, 1.25, 0.4375, 2.5625, 2.5, 1.5625);
		final var crusherInternal = level.toAbsolute(internalBB);
		if(!collided.getBoundingBox().intersects(crusherInternal))
			return;
		if(collided instanceof ItemEntity itemEntity)
		{
			ItemStack stack = itemEntity.getItem();
			if(stack.isEmpty())
				return;
			final var remaining = state.insertionHandler.getValue().insertItem(0, stack, false);
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
				collided.hurt(IEDamageSources.crusher, consumed/20f);
			}
		}
	}

	public static class State implements IMultiblockState, ProcessContextInWorld<CrusherRecipe>
	{
		private final AveragingEnergyStorage energy = new AveragingEnergyStorage(32000);
		private final MultiblockProcessor<CrusherRecipe, ProcessContextInWorld<CrusherRecipe>> processor;
		private boolean renderAsActive;
		private float barrelAngle;

		private final CapabilityReference<IItemHandler> output;
		private final StoredCapability<IItemHandler> insertionHandler;
		private final StoredCapability<IEnergyStorage> energyHandler = new StoredCapability<>(energy);

		public State(MultiblockCapabilitySource capabilitySource)
		{
			this.output = capabilitySource.getCapabilityAt(
					ForgeCapabilities.ITEM_HANDLER, new BlockPos(2, 0, 3), RelativeBlockFace.FRONT
			);
			this.processor = new MultiblockProcessor<>(
					2048, 0, 1,
					capabilitySource::markMasterDirty,
					new RecipeSource<>(CrusherRecipe::findRecipe, CrusherRecipe.RECIPES::getById)
			);
			final var insertionHandler = new DirectProcessingItemHandler<>(capabilitySource.levelSupplier(), processor)
					.setProcessStacking(true);
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
			output = Utils.insertStackIntoInventory(this.output, output, false);
			if(!output.isEmpty())
				Utils.dropStackAtPos(
						level.getRawLevel(),
						level.toAbsolute(new BlockPos(2, 0, 3)),
						output,
						RelativeBlockFace.BACK.forFront(level.getOrientation())
				);
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
	}
}
