/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.IRotationAcceptor;
import blusunrize.immersiveengineering.api.energy.NullEnergyStorage;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.ResettableCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class DynamoBlockEntity extends IEBaseBlockEntity implements IStateBasedDirectional
{
	public DynamoBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.DYNAMO.get(), pos, state);
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return true;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return true;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return true;
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
	}

	private final ResettableCapability<IEnergyStorage> energyCap = registerCapability(NullEnergyStorage.INSTANCE);
	private final ResettableCapability<IRotationAcceptor> rotationCap = registerCapability(new RotationAcceptor());
	private final Map<Direction, CapabilityReference<IEnergyStorage>> neighbors = CapabilityReference.forAllNeighbors(
			this, CapabilityEnergy.ENERGY
	);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
	{
		if(cap==CapabilityEnergy.ENERGY)
			return energyCap.cast();
		if(cap==IRotationAcceptor.CAPABILITY&&side==getFacing())
			return rotationCap.cast();
		return super.getCapability(cap, side);
	}

	private class RotationAcceptor implements IRotationAcceptor
	{

		@Override
		public void inputRotation(double rotation)
		{
			int output = (int)(IEServerConfig.MACHINES.dynamo_output.get()*rotation);
			for(CapabilityReference<IEnergyStorage> neighbor : neighbors.values())
			{
				IEnergyStorage capOnSide = neighbor.getNullable();
				if(capOnSide!=null)
					output -= capOnSide.receiveEnergy(output, false);
				if(output <= 0)
					break;
			}
		}
	}
}