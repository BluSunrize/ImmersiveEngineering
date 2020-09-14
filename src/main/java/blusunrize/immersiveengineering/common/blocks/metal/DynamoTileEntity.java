/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.IRotationAcceptor;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxConnector;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DynamoTileEntity extends IEBaseTileEntity implements IIEInternalFluxConnector, IStateBasedDirectional, IRotationAcceptor
{
	public DynamoTileEntity()
	{
		super(IETileTypes.DYNAMO.get());
	}

	@Override
	public void inputRotation(double rotation, @Nonnull Direction side)
	{
		if(side!=this.getFacing().getOpposite())
			return;
		int output = (int)(IEServerConfig.MACHINES.dynamo_output.get()*rotation);
		for(Direction fd : Direction.VALUES)
		{
			BlockPos outputPos = getPos().offset(fd);
			TileEntity te = Utils.getExistingTileEntity(world, outputPos);
			output -= EnergyHelper.insertFlux(te, fd.getOpposite(), output, false);
		}
	}

	@Override
	public EnumProperty<Direction> getFacingProperty()
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
	public boolean canHammerRotate(Direction side, Vector3d hit, LivingEntity entity)
	{
		return true;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return true;
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
	}

	@Nonnull
	@Override
	public IOSideConfig getEnergySideConfig(@Nullable Direction facing)
	{
		return IOSideConfig.OUTPUT;
	}

	@Override
	public boolean canConnectEnergy(Direction from)
	{
		return true;
	}

	IEForgeEnergyWrapper wrapper = new IEForgeEnergyWrapper(this, null);

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(Direction facing)
	{
		return wrapper;
	}
}