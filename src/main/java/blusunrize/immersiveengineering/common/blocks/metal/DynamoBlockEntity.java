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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.EnumMap;
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
	private final Map<Direction, BlockCapabilityCache<IEnergyStorage, ?>> neighbors = new EnumMap<>(Direction.class);

	public static void registerCapabilities(BECapabilityRegistrar<DynamoBlockEntity> registrar)
	{
		registrar.registerAllContexts(EnergyStorage.BLOCK, $ -> NullEnergyStorage.INSTANCE);
		registrar.register(IRotationAcceptor.CAPABILITY, (be, side) -> side==be.getFacing()?be.rotationCap: null);
	}

	@Override
	public void onLoad()
	{
		super.onLoad();
		if(level instanceof ServerLevel serverLevel)
			for(Direction side : Direction.values())
				neighbors.put(side, BlockCapabilityCache.create(
						EnergyStorage.BLOCK, serverLevel, worldPosition.relative(side), side.getOpposite()
				));
	}

	private class RotationAcceptor implements IRotationAcceptor
	{

		@Override
		public void inputRotation(double rotation)
		{
			int output = (int)(IEServerConfig.MACHINES.dynamo_output.get()*rotation);
			for(BlockCapabilityCache<IEnergyStorage, ?> neighbor : neighbors.values())
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