package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class TileEntityBlastFurnacePreheater extends TileEntityIEBase implements IFluxReceiver,IEnergyReceiver, IDirectionalTile, IHasDummyBlocks
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
			if (!active)
			{
				active = true;
				this.markContainingBlockForUpdate(null);
			}
			this.energyStorage.extractEnergy(consumed, false);
			return 1;
		}
		if (active)
		{
			active = false;
			this.markContainingBlockForUpdate(null);
		}
		return 0;
	}

	@Override
	public boolean isDummy()
	{
		return dummy>0;
	}
	@Override
	public void placeDummies(BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		for(int i=1; i<=2; i++)
		{
			worldObj.setBlockState(pos.add(0,i,0), state);
			((TileEntityBlastFurnacePreheater)worldObj.getTileEntity(pos.add(0,i,0))).dummy = i;
			((TileEntityBlastFurnacePreheater)worldObj.getTileEntity(pos.add(0,i,0))).facing = this.facing;
		}
	}
	@Override
	public void breakDummies(BlockPos pos, IBlockState state)
	{
		for(int i=0; i<=2; i++)
			if(worldObj.getTileEntity(getPos().add(0,-dummy,0).add(0,i,0)) instanceof TileEntityBlastFurnacePreheater)
				worldObj.setBlockToAir(getPos().add(0,-dummy,0).add(0,i,0));
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		dummy = nbt.getInteger("dummy");
		facing = EnumFacing.getFront(nbt.getInteger("facing"));
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

	@Override
	public boolean canConnectEnergy(EnumFacing from)
	{
		return from==EnumFacing.UP&&dummy==2;
	}

	@Override
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate)
	{
		if(dummy>0)
		{
			TileEntity te = worldObj.getTileEntity(getPos().add(0,-dummy,0));
			if(te instanceof TileEntityBlastFurnacePreheater)	
				return ((TileEntityBlastFurnacePreheater)te).receiveEnergy(from, maxReceive, simulate);
			return 0;
		}
		return energyStorage.receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int getEnergyStored(EnumFacing from)
	{
		if(dummy>0)
		{
			TileEntity te = worldObj.getTileEntity(getPos().add(0,-dummy,0));
			if(te instanceof TileEntityBlastFurnacePreheater)	
				return ((TileEntityBlastFurnacePreheater)te).getEnergyStored(from);
			return 0;
		}
		return energyStorage.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from)
	{
		if(dummy>0)
		{
			TileEntity te = worldObj.getTileEntity(getPos().add(0,-dummy,0));
			if(te instanceof TileEntityBlastFurnacePreheater)	
				return ((TileEntityBlastFurnacePreheater)te).getMaxEnergyStored(from);
			return 0;
		}
		return energyStorage.getMaxEnergyStored();
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
}