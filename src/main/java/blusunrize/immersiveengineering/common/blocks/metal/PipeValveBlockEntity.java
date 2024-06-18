/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
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
	public FluidTank tank = new FluidTank(FluidType.BUCKET_VOLUME, this::isFluidValid);
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
		return PlacementLimitation.PISTON_LIKE;
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		this.readTank(nbt);
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		this.writeTank(nbt, false);
	}

	public void readTank(CompoundTag nbt)
	{
		tank.readFromNBT(nbt.getCompound("tank"));
	}

	public void writeTank(CompoundTag nbt, boolean toItem)
	{
		boolean write = tank.getFluidAmount() > 0;
		CompoundTag tankTag = tank.writeToNBT(new CompoundTag());
		if(!toItem||write)
			nbt.put("tank", tankTag);
	}

	public boolean isFluidValid(FluidStack fluid)
	{
		return true;
	}

	private SidedFluidHandler getFluidHandler(Direction facing, @Nullable Direction side) {
		if (side!=null&&facing.getAxis().equals(side.getAxis())) return new SidedFluidHandler(this, side);
		else return new SidedFluidHandler(this, null);
	}

	public static void registerCapabilities(BECapabilityRegistrar<? extends PipeValveBlockEntity> registrar)
	{
		registrar.register(
				FluidHandler.BLOCK,
				(be, side) -> ((PipeValveBlockEntity)be).getFluidHandler(be.getFacing(), side)
		);
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
			//Try to pass fluid through immediately
			IEBlockCapabilityCache<IFluidHandler> capRef = valve.blockFluidHandlers.get(side);
			IFluidHandler handler = capRef.getCapability();
			int filled = 0;
			if(handler!=null) filled = handler.fill(resource, doFill);
			//If passing the fluid through fails, instead store it in the internal tank
			if (filled == 0) {
				int i = valve.tank.fill(resource, doFill);
				if(i > 0&&doFill.execute())
				{
					valve.setChanged();
					valve.markContainingBlockForUpdate(null);
				}
				return i;
			}
			return filled;
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction doDrain)
		{
			if(resource.isEmpty()||side==null||(!side.equals(valve.getFacing()))) return FluidStack.EMPTY;
			return this.drain(resource.getAmount(), doDrain);
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction doDrain)
		{
			if(side==null||(!side.equals(valve.getFacing()))) return FluidStack.EMPTY;
			FluidStack f = valve.tank.drain(maxDrain, doDrain);
			if(!f.isEmpty())
			{
				valve.setChanged();
				valve.markContainingBlockForUpdate(null);
			}
			return f;
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
			return valve.tank.getFluidInTank(tank);
		}

		@Override
		public int getTankCapacity(int tank)
		{
			return valve.tank.getTankCapacity(tank);
		}

		@Override
		public boolean isFluidValid(int tank, @Nonnull FluidStack stack)
		{
			return true;
		}
	}

}