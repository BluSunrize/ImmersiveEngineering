package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceFuel;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IServerTickableMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MBInventoryUtils;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.BlastFurnaceLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.FurnaceHandler.IFurnaceEnvironment;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.FurnaceHandler.InputSlot;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.FurnaceHandler.OutputSlot;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.util.CachedRecipe;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler.IOConstraint;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class BlastFurnaceLogic implements IServerTickableMultiblock<State>
{
	@Override
	public void tickServer(IMultiblockContext<State> context)
	{
		final var level = context.getLevel();
		final var wasActive = level.getBlock(IEMultiblocks.BLAST_FURNACE.getMasterFromOriginOffset())
				.getValue(NonMirrorableWithActiveBlock.ACTIVE);
		final boolean active = context.getState().furnace.tickServer(context);
		if(active!=wasActive)
			NonMirrorableWithActiveBlock.setActive(level, IEMultiblocks.BLAST_FURNACE, active);
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
		return LazyOptional.empty();
	}

	@Override
	public InteractionResult clickSimple(IMultiblockContext<State> ctx, Player player, boolean isClient)
	{
		if(!isClient)
			player.openMenu(IEMenuTypes.BLAST_FURNACE_NEW.provide(ctx));
		return InteractionResult.SUCCESS;
	}

	@Override
	public void dropExtraItems(State state, Consumer<ItemStack> drop)
	{
		MBInventoryUtils.dropItems(state.inventory, drop);
	}

	@Override
	public Function<BlockPos, VoxelShape> shapeGetter()
	{
		return $ -> Shapes.block();
	}

	public static class State implements IMultiblockState, IFurnaceEnvironment<BlastFurnaceRecipe>
	{
		private final SlotwiseItemHandler inventory;
		private final FurnaceHandler<BlastFurnaceRecipe> furnace;

		private final Supplier<BlastFurnaceRecipe> cachedRecipe;

		public State(IInitialMultiblockContext<State> ctx)
		{
			final var getLevel = ctx.levelSupplier();
			inventory = new SlotwiseItemHandler(List.of(
					new IOConstraint(true, i -> BlastFurnaceRecipe.findRecipe(getLevel.get(), i, null)!=null),
					new IOConstraint(true, i -> BlastFurnaceFuel.isValidBlastFuel(getLevel.get(), i)),
					IOConstraint.OUTPUT,
					IOConstraint.OUTPUT
			), ctx.getMarkDirtyRunnable());
			furnace = new FurnaceHandler<>(
					1,
					List.of(new InputSlot<>(r -> r.input, 0)),
					List.of(new OutputSlot<>(r -> r.output, 2), new OutputSlot<>(r -> r.slag, 3)),
					r -> r.time,
					ctx.getMarkDirtyRunnable()
			);
			cachedRecipe = CachedRecipe.cached(
					BlastFurnaceRecipe::findRecipe, getLevel, () -> inventory.getStackInSlot(0)
			);
		}

		@Override
		public void writeSaveNBT(CompoundTag nbt)
		{
			nbt.put("inventory", inventory.serializeNBT());
			nbt.put("furnace", furnace.toNBT());
		}

		@Override
		public void readSaveNBT(CompoundTag nbt)
		{
			inventory.deserializeNBT(nbt.getCompound("inventory"));
			furnace.readNBT(nbt.get("furnace"));
		}

		@Override
		public IItemHandlerModifiable getInventory()
		{
			return inventory;
		}

		@Override
		public @Nullable BlastFurnaceRecipe getRecipeForInput()
		{
			return cachedRecipe.get();
		}

		@Override
		public int getBurnTimeOf(Level level, ItemStack fuel)
		{
			return BlastFurnaceFuel.getBlastFuelTime(level, fuel);
		}

		public ContainerData getStateView()
		{
			return furnace.stateView;
		}
	}
}
