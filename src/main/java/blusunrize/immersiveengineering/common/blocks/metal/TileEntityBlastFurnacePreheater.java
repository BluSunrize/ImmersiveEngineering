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
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public class TileEntityBlastFurnacePreheater extends TileEntityIEBase implements IIEInternalFluxHandler, IDirectionalTile, IHasDummyBlocks
{
	public boolean active;
	public int dummy = 0;
	public FluxStorage energyStorage = new FluxStorage(8000);
	public EnumFacing facing = EnumFacing.NORTH;
	public float angle = 0;
	public long lastRenderTick = -1;

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
	public void placeDummies(BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		for(int i = 1; i <= 2; i++)
		{
			world.setBlockState(pos.add(0, i, 0), state);
			((TileEntityBlastFurnacePreheater)world.getTileEntity(pos.add(0, i, 0))).dummy = i;
			((TileEntityBlastFurnacePreheater)world.getTileEntity(pos.add(0, i, 0))).facing = this.facing;
		}
	}

	@Override
	public void breakDummies(BlockPos pos, IBlockState state)
	{
		for(int i = 0; i <= 2; i++)
			if(world.getTileEntity(getPos().add(0, -dummy, 0).add(0, i, 0)) instanceof TileEntityBlastFurnacePreheater)
				world.setBlockToAir(getPos().add(0, -dummy, 0).add(0, i, 0));
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		dummy = nbt.getInteger("dummy");
		facing = EnumFacing.byIndex(nbt.getInteger("facing"));
		energyStorage.readFromNBT(nbt);
		active = nbt.getBoolean("active");
		if(descPacket)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("dummy", dummy);
		nbt.setInteger("facing", facing.ordinal());
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
	public SideConfig getEnergySideConfig(EnumFacing facing)
	{
		return dummy==2&&facing==EnumFacing.UP?SideConfig.INPUT: SideConfig.NONE;
	}

	IEForgeEnergyWrapper wrapper = new IEForgeEnergyWrapper(this, EnumFacing.UP);

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing)
	{
		if(dummy==2&&facing==EnumFacing.UP)
			return wrapper;
		return null;
	}

	@Override
	public EnumFacing getFacing()
	{
		return facing;
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 2;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return true;
	}

	@Override
	public boolean canRotate(EnumFacing axis)
	{
		return true;
	}

	@Override
	public void afterRotation(EnumFacing oldDir, EnumFacing newDir)
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