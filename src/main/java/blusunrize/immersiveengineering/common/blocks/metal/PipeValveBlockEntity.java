/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches.IEBlockCapabilityCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Map;

public class PipeValveBlockEntity extends IEBaseBlockEntity implements IStateBasedDirectional
{
	public final Map<Direction, IEBlockCapabilityCache<IFluidHandler>> blockFluidHandlers = IEBlockCapabilityCaches.allNeighbors(
			FluidHandler.BLOCK, this
	);

	public PipeValveBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.PIPE_VALVE.get(), pos, state);
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_ALL;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.SIDE_CLICKED_INVERTED;
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket) { }

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket) { }

	public boolean isFluidValid(FluidStack fluid)
	{
		return true;
	}

	public final IFluidHandler inputCap = new SidedFluidHandler(this, getFacing().getOpposite());
	public final IFluidHandler outputCap = new SidedFluidHandler(this, getFacing());

	public static void registerCapabilities(BECapabilityRegistrar<? extends PipeValveBlockEntity> registrar)
	{
		registrar.register(FluidHandler.BLOCK, (be, side) -> (side==be.getFacing()?be.outputCap:side==be.getFacing().getOpposite()?be.inputCap:null));
	}

	static class SidedFluidHandler implements IFluidHandler
	{
		PipeValveBlockEntity valve;
		@Nullable
		Direction side;

		SidedFluidHandler(PipeValveBlockEntity valve, @Nullable Direction side)
		{
			this.valve = valve;
			this.side = side;
		}

		@Override
		public int fill(FluidStack resource, FluidAction doFill)
		{
			if(resource.isEmpty()||side==null||(!side.equals(valve.getFacing().getOpposite()))) return 0;
			//Try to pass fluid through immediately, if we can't do this then don't bother
			IEBlockCapabilityCache<IFluidHandler> capRef = valve.blockFluidHandlers.get(valve.getFacing());
			IFluidHandler handler = capRef.getCapability();
			int filled = 0;
			if(handler!=null) filled = handler.fill(resource, doFill);
			return filled;
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction doDrain)
		{
			if(resource.isEmpty()||side==null||(!side.equals(valve.getFacing()))) return FluidStack.EMPTY;
			IFluidHandler input = valve.blockFluidHandlers.get(valve.getFacing().getOpposite()).getCapability();
			if(input!=null) return input.drain(resource, doDrain);
			else return FluidStack.EMPTY;
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction doDrain)
		{
			if(side==null||(!side.equals(valve.getFacing()))) return FluidStack.EMPTY;
			IFluidHandler input = valve.blockFluidHandlers.get(valve.getFacing()).getCapability();
			if(input!=null) return input.drain(maxDrain, doDrain);
			else return FluidStack.EMPTY;
		}

		@Override
		public int getTanks()
		{
			return 1;
		}

		@Nonnull
		@Override
		public FluidStack getFluidInTank(int tank)
		{
			return FluidStack.EMPTY;
		}

		@Override
		public int getTankCapacity(int tank)
		{
			return 0;
		}

		@Override
		public boolean isFluidValid(int tank, @Nonnull FluidStack stack)
		{
			return true;
		}
	}

}