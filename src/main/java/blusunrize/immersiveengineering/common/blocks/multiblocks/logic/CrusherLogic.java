package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.*;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.CrusherLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.DirectProcessingItemHandler;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInWorld;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor.RecipeSource;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext.ProcessContextInWorld;
import blusunrize.immersiveengineering.common.blocks.multiblocks.shapes.CrusherShapes;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.function.Function;

public class CrusherLogic implements IServerTickableMultiblock<State>
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
		// TODO entity input
		// TODO rendering/sync
		state.processor.tickServer(state, context.getLevel(), true);
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
			insertionHandler = new StoredCapability<>(
					new DirectProcessingItemHandler<>(capabilitySource.levelSupplier(), processor)
			);
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
