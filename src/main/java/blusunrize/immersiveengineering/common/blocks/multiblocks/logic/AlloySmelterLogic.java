/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MBInventoryUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.AlloySmelterLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.FurnaceHandler.IFurnaceEnvironment;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.FurnaceHandler.InputSlot;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.FurnaceHandler.OutputSlot;
import blusunrize.immersiveengineering.common.util.CachedRecipe;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler.IOConstraint;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class AlloySmelterLogic implements IMultiblockLogic<State>, IServerTickableComponent<State>
{
	public static final int NUM_SLOTS = 4;

	@Override
	public void tickServer(IMultiblockContext<State> context)
	{
		final IMultiblockLevel level = context.getLevel();
		final Boolean wasActive = level.getBlockState(IEMultiblocks.ALLOY_SMELTER.getMasterFromOriginOffset())
				.getValue(NonMirrorableWithActiveBlock.ACTIVE);
		final boolean active = context.getState().furnace.tickServer(context);
		if(active!=wasActive)
			NonMirrorableWithActiveBlock.setActive(level, IEMultiblocks.ALLOY_SMELTER, active);
	}

	@Override
	public State createInitialState(IInitialMultiblockContext<State> capabilitySource)
	{
		return new State(capabilitySource);
	}

	@Override
	public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType)
	{
		return $ -> Shapes.block();
	}

	@Override
	public void dropExtraItems(State state, Consumer<ItemStack> drop)
	{
		MBInventoryUtils.dropItems(state.inventory, drop);
	}

	public static class State implements IMultiblockState, IFurnaceEnvironment<AlloyRecipe>
	{
		private final SlotwiseItemHandler inventory;
		private final FurnaceHandler<AlloyRecipe> furnace;
		private final Supplier<AlloyRecipe> cachedRecipe;

		public State(IInitialMultiblockContext<State> ctx)
		{
			this.furnace = new FurnaceHandler<>(
					2,
					List.of(new InputSlot<>(a -> a.input0, 0), new InputSlot<>(a -> a.input1, 1)),
					List.of(new OutputSlot<>(a -> a.output, 3)),
					a -> a.time,
					ctx.getMarkDirtyRunnable()
			);
			// This inv is not exposed as a capability, so the constraints just specify player interaction
			inventory = new SlotwiseItemHandler(List.of(
					IOConstraint.NO_CONSTRAINT, IOConstraint.NO_CONSTRAINT,
					new IOConstraint(true, FurnaceBlockEntity::isFuel), IOConstraint.OUTPUT
			), ctx.getMarkDirtyRunnable());
			cachedRecipe = CachedRecipe.cached(
					AlloyRecipe::findRecipe,
					ctx.levelSupplier(),
					() -> inventory.getStackInSlot(0),
					() -> inventory.getStackInSlot(1)
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
		public @Nullable AlloyRecipe getRecipeForInput()
		{
			return cachedRecipe.get();
		}

		@Override
		public int getBurnTimeOf(Level level, ItemStack fuel)
		{
			//TODO more specific type?
			return ForgeHooks.getBurnTime(fuel, RecipeType.SMELTING);
		}

		public ContainerData getStateView()
		{
			return furnace.stateView;
		}
	}
}
