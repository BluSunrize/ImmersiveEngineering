/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public class TileEntityBlastFurnacePreheater extends TileEntityIEBase implements IIEInternalFluxHandler, IDirectionalTile, IHasDummyBlocks
{
	public static TileEntityType<TileEntityBlastFurnacePreheater> TYPE;

	public boolean active;
	public int dummy = 0;
	public FluxStorage energyStorage = new FluxStorage(8000);
	public Direction facing = Direction.NORTH;
	public float angle = 0;
	public long lastRenderTick = -1;

	public TileEntityBlastFurnacePreheater()
	{
		super(TYPE);
	}

	public int doSpeedup()
	{
		int consumed = IEConfig.Machines.preheater_consumption;
		if(this.energyStorage.extractEnergy(consumed, true)==consumed)
		{
			if(!active)
			{
				active = true;
				this.markContainingBlockForUpdate(null);
			}
			this.energyStorage.extractEnergy(consumed, false);
			return 1;
		}
		else if(active)
		{
			active = false;
			this.markContainingBlockForUpdate(null);
		}
		return 0;
	}

	@Override
	public boolean isDummy()
	{
		return dummy > 0;
	}

	@Override
	public void placeDummies(BlockPos pos, BlockState state, Direction side, float hitX, float hitY, float hitZ)
	{
		for(int i = 1; i <= 2; i++)
		{
			world.setBlockState(pos.add(0, i, 0), state);
			((TileEntityBlastFurnacePreheater)world.getTileEntity(pos.add(0, i, 0))).dummy = i;
			((TileEntityBlastFurnacePreheater)world.getTileEntity(pos.add(0, i, 0))).facing = this.facing;
		}
	}

	@Override
	public void breakDummies(BlockPos pos, BlockState state)
	{
		for(int i = 0; i <= 2; i++)
			if(world.getTileEntity(getPos().add(0, -dummy, 0).add(0, i, 0)) instanceof TileEntityBlastFurnacePreheater)
				world.removeBlock(getPos().add(0, -dummy, 0).add(0, i, 0));
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		dummy = nbt.getInt("dummy");
		facing = Direction.byIndex(nbt.getInt("facing"));
		energyStorage.readFromNBT(nbt);
		active = nbt.getBoolean("active");
		if(descPacket)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.setInt("dummy", dummy);
		nbt.setInt("facing", facing.ordinal());
		nbt.setBoolean("active", active);
		energyStorage.writeToNBT(nbt);
	}

	@Nonnull
	@Override
	public FluxStorage getFluxStorage()
	{
		if(dummy > 0)
		{
			TileEntity te = world.getTileEntity(getPos().add(0, -dummy, 0));
			if(te instanceof TileEntityBlastFurnacePreheater)
				return ((TileEntityBlastFurnacePreheater)te).getFluxStorage();
		}
		return energyStorage;
	}

	@Nonnull
	@Override
	public SideConfig getEnergySideConfig(Direction facing)
	{
		return dummy==2&&facing==Direction.UP?SideConfig.INPUT: SideConfig.NONE;
	}

	IEForgeEnergyWrapper wrapper = new IEForgeEnergyWrapper(this, Direction.UP);

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(Direction facing)
	{
		if(dummy==2&&facing==Direction.UP)
			return wrapper;
		return null;
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
		return false;
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
	public void afterRotation(Direction oldDir, Direction newDir)
	{
		for(int i = 0; i <= 2; i++)
		{
			TileEntity te = world.getTileEntity(getPos().add(0, -dummy+i, 0));
			if(te instanceof TileEntityBlastFurnacePreheater)
			{
				((TileEntityBlastFurnacePreheater)te).setFacing(newDir);
				te.markDirty();
				((TileEntityBlastFurnacePreheater)te).markContainingBlockForUpdate(null);
			}
		}
	}
}