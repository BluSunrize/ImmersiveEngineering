/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.blocks.wooden.WoodenBarrelBlockEntity;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static blusunrize.immersiveengineering.api.IEEnums.IOSideConfig.OUTPUT;

public class PipeValveBlockEntity extends IEBaseBlockEntity implements IStateBasedDirectional
{
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
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
	}

	private final Map<Direction, IFluidHandler> sidedFluidHandler = new HashMap<>();

	{
		sidedFluidHandler.put(Direction.DOWN, new SidedFluidHandler(this, Direction.DOWN));
		sidedFluidHandler.put(Direction.UP, new SidedFluidHandler(this, Direction.UP));
		sidedFluidHandler.put(null, new SidedFluidHandler(this, null));
	}

	public static void registerCapabilities(BECapabilityRegistrar<? extends WoodenBarrelBlockEntity> registrar)
	{
		registrar.register(
				FluidHandler.BLOCK,
				(be, side) -> ((WoodenBarrelBlockEntity)be).sidedFluidHandler.getOrDefault(side, null)
		);
	}

	static class SidedFluidHandler implements IFluidHandler
	{
		WoodenBarrelBlockEntity barrel;
		@Nullable
		Direction facing;

		SidedFluidHandler(WoodenBarrelBlockEntity barrel, @Nullable Direction facing)
		{
			this.barrel = barrel;
			this.facing = facing;
		}

		@Override
		public int fill(FluidStack resource, FluidAction doFill)
		{
			if(resource.isEmpty()||(facing!=null&&barrel.sideConfig.get(facing)!=IOSideConfig.INPUT)||!barrel.isFluidValid(resource))
				return 0;

			int i = barrel.tank.fill(resource, doFill);
			if(i > 0&&doFill.execute())
			{
				barrel.setChanged();
				barrel.markContainingBlockForUpdate(null);
			}
			return i;
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction doDrain)
		{
			if(resource.isEmpty())
				return FluidStack.EMPTY;
			return this.drain(resource.getAmount(), doDrain);
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction doDrain)
		{
			if(facing!=null&&barrel.sideConfig.get(facing)!=OUTPUT)
				return FluidStack.EMPTY;
			FluidStack f = barrel.tank.drain(maxDrain, doDrain);
			if(!f.isEmpty())
			{
				barrel.setChanged();
				barrel.markContainingBlockForUpdate(null);
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
			return barrel.tank.getFluidInTank(tank);
		}

		@Override
		public int getTankCapacity(int tank)
		{
			return barrel.tank.getTankCapacity(tank);
		}

		@Override
		public boolean isFluidValid(int tank, @Nonnull FluidStack stack)
		{
			return true;
		}
	}

}
