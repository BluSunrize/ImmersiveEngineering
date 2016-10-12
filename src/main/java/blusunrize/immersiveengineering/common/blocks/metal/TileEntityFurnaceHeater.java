package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.PropertyBoolInverted;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler.IExternalHeatable;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IActiveState;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IUsesBooleanProperty;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

public class TileEntityFurnaceHeater extends TileEntityIEBase implements ITickable, IFluxReceiver, IActiveState, IDirectionalTile
{
	public FluxStorage energyStorage = new FluxStorage(32000,Math.max(256, Math.max(IEConfig.Machines.heater_consumption, IEConfig.Machines.heater_speedupConsumption)));
	//public int[] sockets = new int[6];
	public boolean active = false;
	public EnumFacing facing = EnumFacing.NORTH;

	@Override
	public void update()
	{
		if(!worldObj.isRemote)
		{
			boolean a = active;
			if(active)
				active=false;
			for(EnumFacing fd : EnumFacing.VALUES)
			{
				TileEntity tileEntity = worldObj.getTileEntity(getPos().offset(fd));
				int consumed = 0;
				if(tileEntity!=null)
					if(tileEntity instanceof IExternalHeatable)
						consumed = ((IExternalHeatable)tileEntity).doHeatTick(energyStorage.getEnergyStored(), worldObj.isBlockIndirectlyGettingPowered(getPos())>0);
					else
					{
						ExternalHeaterHandler.HeatableAdapter adapter = ExternalHeaterHandler.getHeatableAdapter(tileEntity.getClass());
						if(adapter!=null)
							consumed = adapter.doHeatTick(tileEntity, energyStorage.getEnergyStored(), worldObj.isBlockIndirectlyGettingPowered(getPos())>0);
					}
				if(consumed>0)
				{
					this.energyStorage.extractEnergy(consumed, false);
					if(!active)
						active = true;
				}
			}
			if(active!=a)
			{
				this.markDirty();
				this.markContainingBlockForUpdate(null);
				worldObj.addBlockEvent(getPos(), this.getBlockType(), 1,active?1:0);
			}
		}
	}

	//	public boolean canHeat(TileEntityFurnace furnace)
	//	{
	//		ItemStack input = furnace.getStackInSlot(0);
	//		if(input == null)
	//			return false;
	//		ItemStack output = FurnaceRecipes.smelting().getSmeltingResult(input);
	//		if(output == null)
	//			return false;
	//		ItemStack existingOutput = furnace.getStackInSlot(2);
	//		if(existingOutput==null)
	//			return true;
	//		if(!existingOutput.isItemEqual(output))
	//			return false;
	//		int stackSize = existingOutput.stackSize+output.stackSize;
	//		return stackSize<=furnace.getInventoryStackLimit() && stackSize<=output.getMaxStackSize();
	//	}

	@Override
	public PropertyBoolInverted getBoolProperty(Class<? extends IUsesBooleanProperty> inf)
	{
		return inf==IActiveState.class?IEProperties.BOOLEANS[0]:null;
	}
	@Override
	public boolean getIsActive()
	{
		return active || worldObj.isBlockIndirectlyGettingPowered(getPos())>0;
	}
	//	@Override
	//	public SideConfig getSideConfig(int side)
	//	{
	//		return IEEnums.SideConfig.values()[this.sockets[side]];
	//	}
	//	@Override
	//	public void toggleSide(int side)
	//	{
	//		sockets[side] = sockets[side]==1?0:1;
	//		this.markDirty();
	//		worldObj.markBlockForUpdate(getPos());
	//		worldObj.addBlockEvent(getPos(), this.getBlockType(), 0, 0);
	//	}
	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==1)
			this.active = arg==1;
		this.markContainingBlockForUpdate(null);
		return true;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		energyStorage.readFromNBT(nbt);
		facing = EnumFacing.getFront(nbt.getInteger("facing"));
		//		sockets = nbt.getIntArray("sockets");
		//		if(sockets.length<6)
		//			sockets = new int[0];
		active = nbt.getBoolean("active");
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		energyStorage.writeToNBT(nbt);
		nbt.setInteger("facing", facing.ordinal());
		//		nbt.setIntArray("sockets", sockets);
		nbt.setBoolean("active", active);
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from)
	{
		return from==facing;
		//		return sockets[from.ordinal()]==1;
	}
	@Override
	public int receiveEnergy(EnumFacing from, int energy, boolean simulate)
	{
		//		if(sockets[from.ordinal()]==0)
		//			return 0;
		if(from!=facing)
			return 0;
		return energyStorage.receiveEnergy(energy, simulate);
	}

	@Override
	public int getEnergyStored(EnumFacing from)
	{
		return energyStorage.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from)
	{
		return energyStorage.getMaxEnergyStored();
	}

	@Override
	public EnumFacing getFacing()
	{
		return this.facing;
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;		
	}
	@Override
	public int getFacingLimitation()
	{
		return 1;
	}
	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return placer.isSneaking();
	}
	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return true;
	}
}