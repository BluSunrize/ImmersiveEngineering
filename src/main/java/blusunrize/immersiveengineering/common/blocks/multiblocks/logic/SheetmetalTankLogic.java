/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl.RSState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.client.utils.TextUtils;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.SheetmetalTankLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.interfaces.MBMemorizeStructure;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.interfaces.MBOverlayText;
import blusunrize.immersiveengineering.common.blocks.multiblocks.shapes.SiloTankShapes;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import blusunrize.immersiveengineering.common.util.LayeredComparatorOutput;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class SheetmetalTankLogic implements IServerTickableComponent<State>, MBOverlayText<State>, MBMemorizeStructure<State>
{
	private static final SiloTankShapes SHAPE_GETTER = new SiloTankShapes(4, false);
	public static final BlockPos IO_POS = new BlockPos(1, 0, 1);
	private static final BlockPos INPUT_POS = new BlockPos(1, 4, 1);

	@Override
	public void tickServer(IMultiblockContext<State> context)
	{
		final State state = context.getState();
		state.comparatorHelper.update(context, state.tank.getFluidAmount());
		if(!state.rsState.isEnabled(context)||state.tank.isEmpty())
			return;
		for(Supplier<@Nullable IFluidHandler> outputRef : state.outputs)
		{
			int outSize = Math.min(FluidType.BUCKET_VOLUME, state.tank.getFluidAmount());
			FluidStack out = state.tank.getFluid().copyWithAmount(outSize);
			IFluidHandler output = outputRef.get();
			if(output==null)
				continue;
			int accepted = output.fill(out, FluidAction.SIMULATE);
			if(accepted > 0)
			{
				int drained = output.fill(out.copyWithAmount(Math.min(out.getAmount(), accepted)), FluidAction.EXECUTE);
				state.tank.drain(drained, FluidAction.EXECUTE);
				context.markMasterDirty();
				context.requestMasterBESync();
				if(state.tank.isEmpty())
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
	public void registerCapabilities(CapabilityRegistrar<State> register)
	{
		register.register(FluidHandler.BLOCK, (state, position) -> {
			if(IO_POS.equals(position.posInMultiblock()))
				return state.ioHandler;
			else if(INPUT_POS.equals(position.posInMultiblock()))
				return state.inputHandler;
			else
				return null;
		});
	}

	@Nullable
	@Override
	public List<Component> getOverlayText(State state, Player player, boolean hammer)
	{
		if(Utils.isFluidRelatedItemStack(player.getItemInHand(InteractionHand.MAIN_HAND)))
			return List.of(TextUtils.formatFluidStack(state.tank.getFluid()));
		return null;
	}

	@Override
	public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType)
	{
		return SHAPE_GETTER;
	}

	@Override
	public ItemInteractionResult click(
			IMultiblockContext<State> ctx, BlockPos posInMultiblock,
			Player player, InteractionHand hand, BlockHitResult absoluteHit,
			boolean isClient
	)
	{
		if(FluidUtils.interactWithFluidHandler(player, hand, ctx.getState().tank))
		{
			ctx.markDirtyAndSync();
			return ItemInteractionResult.SUCCESS;
		}
		else
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public void setMemorizedBlockState(State state, BlockPos pos, BlockState blockState)
	{
		state.structureMemo.put(pos, blockState);
	}

	@Override
	public BlockState getMemorizedBlockState(State state, BlockPos pos)
	{
		return state.structureMemo.get(pos);
	}

	public static class State implements IMultiblockState
	{
		public final FluidTank tank = new FluidTank(512*FluidType.BUCKET_VOLUME);
		private final LayeredComparatorOutput<IMultiblockContext<?>> comparatorHelper;
		private final List<Supplier<@Nullable IFluidHandler>> outputs;
		private final IFluidHandler inputHandler;
		private final IFluidHandler ioHandler;
		public final RSState rsState = RSState.disabledByDefault();
		private final StructureMemo structureMemo = new StructureMemo();

		public State(IInitialMultiblockContext<State> capabilitySource)
		{
			this.comparatorHelper = LayeredComparatorOutput.makeForSiloLike(tank.getCapacity(), 4);
			ImmutableList.Builder<Supplier<@Nullable IFluidHandler>> outputBuilder = ImmutableList.builder();
			for(RelativeBlockFace face : RelativeBlockFace.values())
				if(face!=RelativeBlockFace.DOWN)
				{
					final BlockPos neighbor = face.offsetRelative(IO_POS, -1);
					outputBuilder.add(capabilitySource.getCapabilityAt(FluidHandler.BLOCK, neighbor, face));
				}
			this.outputs = outputBuilder.build();
			Runnable changedAndSync = () -> {
				capabilitySource.getSyncRunnable().run();
				capabilitySource.getMarkDirtyRunnable().run();
			};
			this.inputHandler = new ArrayFluidHandler(tank, false, true, changedAndSync);
			this.ioHandler = new ArrayFluidHandler(tank, true, true, changedAndSync);
		}

		@Override
		public void writeSaveNBT(CompoundTag nbt, Provider provider)
		{
			nbt.put("tank", tank.writeToNBT(provider, new CompoundTag()));
			structureMemo.writeSaveNBT(nbt, provider);
		}

		@Override
		public void readSaveNBT(CompoundTag nbt, Provider provider)
		{
			tank.readFromNBT(provider, nbt.getCompound("tank"));
			structureMemo.readSaveNBT(nbt, provider);
		}

		@Override
		public void writeSyncNBT(CompoundTag nbt, Provider provider)
		{
			writeSaveNBT(nbt, provider);
		}

		@Override
		public void readSyncNBT(CompoundTag nbt, Provider provider)
		{
			readSaveNBT(nbt, provider);
		}
	}
}
