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
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.EnergyConnector;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.generic.ImmersiveConnectableBlockEntity;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.SpawnInterdictionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ElectricLanternBlockEntity extends ImmersiveConnectableBlockEntity implements ISpawnInterdiction, IEServerTickableBE,
		IStateBasedDirectional, IHammerInteraction, IBlockBounds, IActiveState, EnergyConnector
{
	public int energyStorage = 0;
	private int energyDraw = IEServerConfig.MACHINES.lantern_energyDraw.get();
	private int maximumStorage = IEServerConfig.MACHINES.lantern_maximumStorage.get();

	public ElectricLanternBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.ELECTRIC_LANTERN.get(), pos, state);
	}

	@Override
	public void tickServer()
	{
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
			checkLight();
	}

	@Override
	public double getInterdictionRangeSquared()
	{
		return getIsActive()?1024: 0;
	}

	@Override
	public void setRemovedIE()
	{
		SpawnInterdictionHandler.removeFromInterdictionTiles(this);
		super.setRemovedIE();
	}

	@Override
	public void onChunkUnloadedIE()
	{
		SpawnInterdictionHandler.removeFromInterdictionTiles(this);
		super.onChunkUnloadedIE();
	}

	@Override
	public void onLoad()
	{
		super.onLoad();
		SpawnInterdictionHandler.addInterdictionTile(this);
	}

	@Override
	public void readCustomNBT(@Nonnull CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		energyStorage = nbt.getInt("energyStorage");
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.putInt("energyStorage", energyStorage);
	}

	@Override
	public boolean triggerEvent(int id, int arg)
	{
		if(id==1)
		{
			this.markContainingBlockForUpdate(null);
			checkLight();
			return true;
		}
		return super.triggerEvent(id, arg);
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset)
	{
		return WireType.LV_CATEGORY.equals(cableType.getCategory());
	}

	@Override
	public Vec3 getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		BlockPos other = con.getOtherEnd(here).getPosition();
		int xDif = other.getX()-worldPosition.getX();
		int zDif = other.getZ()-worldPosition.getZ();
		boolean flipped = getFacing()==Direction.UP;
		if(Math.abs(xDif) >= Math.abs(zDif))
			return new Vec3(xDif < 0?.25: xDif > 0?.75: .5, flipped?.9375: .0625, .5);
		return new Vec3(.5, flipped?.9375: .0625, zDif < 0?.25: zDif > 0?.75: .5);
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return Shapes.box(.1875f, 0, .1875f, .8125f, 1, .8125f);
	}


	@Override
	public Property<Direction> getFacingProperty()
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
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return false;
	}

	@Override
	public boolean hammerUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		if(!level.isClientSide)
			setFacing(getFacing().getOpposite());
		for(ConnectionPoint cp : getConnectionPoints())
			for(Connection c : getLocalNet(cp.getIndex()).getConnections(cp))
				if(!c.isInternal())
					globalNet.updateCatenaryData(c, level);
		setChanged();
		markContainingBlockForUpdate(getBlockState());
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