/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.ImmersiveConnectableTileEntity;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.EnergyConnector;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.SpawnInterdictionHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ElectricLanternTileEntity extends ImmersiveConnectableTileEntity implements ISpawnInterdiction, ITickableTileEntity,
		IStateBasedDirectional, IHammerInteraction, IBlockBounds, IActiveState, EnergyConnector
{
	public int energyStorage = 0;
	private int energyDraw = IEConfig.MACHINES.lantern_energyDraw.get();
	private int maximumStorage = IEConfig.MACHINES.lantern_maximumStorage.get();

	public ElectricLanternTileEntity()
	{
		super(IETileTypes.ELECTRIC_LANTERN.get());
	}

	@Override
	public void tick()
	{
		if(world.isRemote)
			return;
		boolean activeBeforeTick = getIsActive();
		if(energyStorage >= energyDraw)
		{
			energyStorage -= energyDraw;
			if(!activeBeforeTick)
				setActive(true);
		}
		else if(activeBeforeTick)
			setActive(false);

		if(getIsActive()!=activeBeforeTick)
		{
			checkLight();
		}
	}

	@Override
	public double getInterdictionRangeSquared()
	{
		return getIsActive()?1024: 0;
	}

	@Override
	public void remove()
	{
		SpawnInterdictionHandler.removeFromInterdictionTiles(this);
		super.remove();
	}

	@Override
	public void onChunkUnloaded()
	{
		SpawnInterdictionHandler.removeFromInterdictionTiles(this);
		super.onChunkUnloaded();
	}

	@Override
	public void onLoad()
	{
		super.onLoad();
		SpawnInterdictionHandler.addInterdictionTile(this);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		energyStorage = nbt.getInt("energyStorage");
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.putInt("energyStorage", energyStorage);
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==1)
		{
			this.markContainingBlockForUpdate(null);
			checkLight();
			return true;
		}
		return super.receiveClientEvent(id, arg);
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset)
	{
		return WireType.LV_CATEGORY.equals(cableType.getCategory());
	}

	@Override
	public Vec3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		BlockPos other = con.getOtherEnd(here).getPosition();
		int xDif = other.getX()-pos.getX();
		int zDif = other.getZ()-pos.getZ();
		boolean flipped = getFacing()==Direction.UP;
		if(Math.abs(xDif) >= Math.abs(zDif))
			return new Vec3d(xDif < 0?.25: xDif > 0?.75: .5, flipped?.9375: .0625, .5);
		return new Vec3d(.5, flipped?.9375: .0625, zDif < 0?.25: zDif > 0?.75: .5);
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable ISelectionContext ctx)
	{
		return VoxelShapes.create(.1875f, 0, .1875f, .8125f, 1, .8125f);
	}


	@Override
	public EnumProperty<Direction> getFacingProperty()
	{
		return IEProperties.FACING_TOP_DOWN;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.FIXED_DOWN;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3d hit, LivingEntity entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return false;
	}

	@Override
	public boolean hammerUseSide(Direction side, PlayerEntity player, Hand hand, Vec3d hitVec)
	{
		if(!world.isRemote)
			setFacing(getFacing().getOpposite());
		return true;
	}

	@Override
	public boolean isSource(ConnectionPoint cp)
	{
		return false;
	}

	@Override
	public boolean isSink(ConnectionPoint cp)
	{
		return true;
	}

	@Override
	public int getRequestedEnergy()
	{
		return maximumStorage-energyStorage;
	}

	@Override
	public void insertEnergy(int amount)
	{
		energyStorage += amount;
	}
}