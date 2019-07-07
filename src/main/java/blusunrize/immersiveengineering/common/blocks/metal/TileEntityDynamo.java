/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.energy.IRotationAcceptor;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxConnector;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityDynamo extends TileEntityIEBase implements IIEInternalFluxConnector, IDirectionalTile, IRotationAcceptor
{
	public static TileEntityType<TileEntityDynamo> TYPE;
	public Direction facing = Direction.NORTH;

	public TileEntityDynamo()
	{
		super(TYPE);
	}

	@Override
	public void inputRotation(double rotation, @Nonnull Direction side)
	{
		if(side!=this.facing.getOpposite())
			return;
		int output = (int)(IEConfig.Machines.dynamo_output*rotation);
		for(Direction fd : Direction.VALUES)
		{
			BlockPos outputPos = getPos().offset(fd);
			TileEntity te = Utils.getExistingTileEntity(world, outputPos);
			output -= EnergyHelper.insertFlux(te, fd.getOpposite(), output, false);
		}
	}

	@Override
	public Direction getFacing()
	{
		return facing;
	}

	@Override
	public void setFacing(Direction facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 2;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return true;
	}

	@Override
	public boolean canHammerRotate(Direction side, float hitX, float hitY, float hitZ, LivingEntity entity)
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
		facing = Direction.byIndex(nbt.getInt("facing"));
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.setInt("facing", facing.ordinal());
	}

	@Nonnull
	@Override
	public SideConfig getEnergySideConfig(@Nullable Direction facing)
	{
		return SideConfig.OUTPUT;
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