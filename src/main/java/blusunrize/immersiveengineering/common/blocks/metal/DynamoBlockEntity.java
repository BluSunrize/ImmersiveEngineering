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
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches.IEBlockCapabilityCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

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
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
	}

	private final IRotationAcceptor rotationCap = new RotationAcceptor();
	private final Map<Direction, IEBlockCapabilityCache<IEnergyStorage>> neighbors = IEBlockCapabilityCaches.allNeighbors(
			EnergyStorage.BLOCK, this
	);

	public static void registerCapabilities(BECapabilityRegistrar<DynamoBlockEntity> registrar)
	{
		registrar.registerAllContexts(EnergyStorage.BLOCK, $ -> NullEnergyStorage.INSTANCE);
		registrar.register(IRotationAcceptor.CAPABILITY, (be, side) -> side==be.getFacing()?be.rotationCap: null);
	}

	private class RotationAcceptor implements IRotationAcceptor
	{

		@Override
		public void inputRotation(double rotation)
		{
			// TODO: Make this divisor one on next major (1.21?) update, whenever IEServerConfig mult is updated
			int output = (int)((IEServerConfig.MACHINES.dynamo_output.get()/3f)*rotation);
			for(IEBlockCapabilityCache<IEnergyStorage> neighbor : neighbors.values())
			{
				IEnergyStorage capOnSide = neighbor.getCapability();
				if(capOnSide!=null)
					output -= capOnSide.receiveEnergy(output, false);
				if(output <= 0)
					break;
			}
		}
	}
}