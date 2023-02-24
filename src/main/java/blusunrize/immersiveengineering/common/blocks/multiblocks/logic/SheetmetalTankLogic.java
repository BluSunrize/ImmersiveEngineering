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
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.StoredCapability;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.client.utils.TextUtils;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.SheetmetalTankLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.interfaces.MBOverlayText;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import blusunrize.immersiveengineering.common.util.LayeredComparatorOutput;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class SheetmetalTankLogic implements IServerTickableComponent<State>, MBOverlayText<State>
{
	public static final BlockPos IO_POS = new BlockPos(1, 0, 1);
	private static final BlockPos INPUT_POS = new BlockPos(1, 4, 1);

	@Override
	public void tickServer(IMultiblockContext<State> context)
	{
		final State state = context.getState();
		state.comparatorHelper.update(context, state.tank.getFluidAmount());
		if(!state.rsState.isEnabled(context)||state.tank.isEmpty())
			return;
		for(CapabilityReference<IFluidHandler> outputRef : state.outputs)
		{
			int outSize = Math.min(FluidType.BUCKET_VOLUME, state.tank.getFluidAmount());
			FluidStack out = Utils.copyFluidStackWithAmount(state.tank.getFluid(), outSize, false);
			IFluidHandler output = outputRef.getNullable();
			if(output==null)
				continue;
			int accepted = output.fill(out, FluidAction.SIMULATE);
			if(accepted > 0)
			{
				int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.getAmount(), accepted), false), FluidAction.EXECUTE);
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
	public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap)
	{
		if(cap==ForgeCapabilities.FLUID_HANDLER)
		{
			if(IO_POS.equals(position.posInMultiblock()))
				return ctx.getState().ioHandler.cast(ctx);
			else if(INPUT_POS.equals(position.posInMultiblock()))
				return ctx.getState().inputHandler.cast(ctx);
		}
		return LazyOptional.empty();
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
		return pos -> {
			if(pos.getX()%2==0&&pos.getY()==0&&pos.getZ()%2==0)
				return Shapes.box(.375f, 0, .375f, .625f, 1, .625f);
			else
				return Shapes.block();
		};
	}

	@Override
	public InteractionResult click(
			IMultiblockContext<State> ctx, BlockPos posInMultiblock,
			Player player, InteractionHand hand, BlockHitResult absoluteHit,
			boolean isClient
	)
	{
		if(FluidUtils.interactWithFluidHandler(player, hand, ctx.getState().tank))
		{
			ctx.markDirtyAndSync();
			return InteractionResult.SUCCESS;
		}
		else
			return InteractionResult.PASS;
	}

	public static class State implements IMultiblockState
	{
		public final FluidTank tank = new FluidTank(512*FluidType.BUCKET_VOLUME);
		private final LayeredComparatorOutput<IMultiblockContext<?>> comparatorHelper;
		private final List<CapabilityReference<IFluidHandler>> outputs;
		private final StoredCapability<IFluidHandler> inputHandler;
		private final StoredCapability<IFluidHandler> ioHandler;
		public final RSState rsState = RSState.disabledByDefault();

		public State(IInitialMultiblockContext<State> capabilitySource)
		{
			this.comparatorHelper = LayeredComparatorOutput.makeForSiloLike(tank.getCapacity(), 4);
			ImmutableList.Builder<CapabilityReference<IFluidHandler>> outputBuilder = ImmutableList.builder();
			for(RelativeBlockFace face : RelativeBlockFace.values())
				if(face!=RelativeBlockFace.DOWN)
				{
					final BlockPos neighbor = face.offsetRelative(IO_POS, -1);
					outputBuilder.add(capabilitySource.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, neighbor, face));
				}
			this.outputs = outputBuilder.build();
			Runnable changedAndSync = () -> {
				capabilitySource.getSyncRunnable().run();
				capabilitySource.getMarkDirtyRunnable().run();
			};
			this.inputHandler = new StoredCapability<>(new ArrayFluidHandler(tank, false, true, changedAndSync));
			this.ioHandler = new StoredCapability<>(new ArrayFluidHandler(tank, true, true, changedAndSync));
		}

		@Override
		public void writeSaveNBT(CompoundTag nbt)
		{
			nbt.put("tank", tank.writeToNBT(new CompoundTag()));
		}

		@Override
		public void readSaveNBT(CompoundTag nbt)
		{
			tank.readFromNBT(nbt.getCompound("tank"));
		}

		@Override
		public void writeSyncNBT(CompoundTag nbt)
		{
			writeSaveNBT(nbt);
		}

		@Override
		public void readSyncNBT(CompoundTag nbt)
		{
			readSaveNBT(nbt);
		}
	}
}
