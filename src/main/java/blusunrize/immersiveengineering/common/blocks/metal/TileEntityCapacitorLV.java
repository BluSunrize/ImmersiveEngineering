package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEEnums;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxProvider;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IConfigurableSides;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.RayTraceResult;

public class TileEntityCapacitorLV extends TileEntityIEBase implements ITickable, IFluxProvider,IFluxReceiver,IEnergyProvider,IEnergyReceiver, IBlockOverlayText, IConfigurableSides, IComparatorOverride, ITileDrop
{
	public int[] sideConfig={-1,0,-1,-1,-1,-1};
	FluxStorage energyStorage = new FluxStorage(getMaxStorage(),getMaxInput(),getMaxOutput());

	public int comparatorOutput=0;

	@Override
	public void update()
	{
		if(!worldObj.isRemote)
		{
			for(int i=0; i<6; i++)
				this.transferEnergy(i);

			if(worldObj.getTotalWorldTime()%32==((getPos().getX()^getPos().getZ())&31))
			{
				int i = scaleStoredEnergyTo(15);
				if(i!=this.comparatorOutput)
				{
					this.comparatorOutput=i;
					worldObj.updateComparatorOutputLevel(getPos(), getBlockType());
				}
			}
		}
	}
	public int scaleStoredEnergyTo(int scale)
	{
		return (int)(scale*(energyStorage.getEnergyStored()/(float)energyStorage.getMaxEnergyStored()));
	}

	protected void transferEnergy(int side)
	{
		if(this.sideConfig[side] != 1)
			return;
		EnumFacing fd = EnumFacing.getFront(side);
		TileEntity tileEntity = worldObj.getTileEntity(getPos().offset(fd));
		if(tileEntity instanceof IFluxReceiver)
			this.energyStorage.modifyEnergyStored(-((IFluxReceiver)tileEntity).receiveEnergy(fd.getOpposite(), Math.min(getMaxOutput(), this.energyStorage.getEnergyStored()), false));
		else if(tileEntity instanceof IEnergyReceiver)
			this.energyStorage.modifyEnergyStored(-((IEnergyReceiver)tileEntity).receiveEnergy(fd.getOpposite(), Math.min(getMaxOutput(), this.energyStorage.getEnergyStored()), false));
		//		else if(worldObj.getTileEntity(xCoord+fd.offsetX,yCoord+fd.offsetY,zCoord+fd.offsetZ) instanceof TileEntityConnectorLV)
		//		{
		//			IImmersiveConnectable node = (IImmersiveConnectable) worldObj.getTileEntity(xCoord+fd.offsetX,yCoord+fd.offsetY,zCoord+fd.offsetZ);
		//			if(!node.isEnergyOutput())
		//				return;
		//			List<AbstractConnection> outputs = ImmersiveNetHandler.INSTANCE.getIndirectEnergyConnections(Utils.toCC(node), worldObj);
		//			int received = 0;
		//			int powerLeft = Math.min(getMaxOutput(), this.energyStorage.getEnergyStored());
		//			for(AbstractConnection con : outputs)
		//				if(con!=null && toIIC(con.end, worldObj)!=null)
		//				{
		//					int tempR = toIIC(con.end,worldObj).outputEnergy(Math.min(powerLeft,con.cableType.getTransferRate()), true, 0);
		//					tempR -= (int) Math.floor(tempR*con.getAverageLossRate());
		//					int r = toIIC(con.end, worldObj).outputEnergy(tempR, false, 0);
		//					received += r;
		//					powerLeft -= r;
		//					if(powerLeft<=0)
		//						break;
		//				}
		//			this.energyStorage.modifyEnergyStored(-received);
		//		}
	}
	@Override
	public IEEnums.SideConfig getSideConfig(int side)
	{
		return IEEnums.SideConfig.values()[this.sideConfig[side]+1];
	}
	@Override
	public void toggleSide(int side)
	{
		sideConfig[side]++;
		if(sideConfig[side]>1)
			sideConfig[side]=-1;
		this.markDirty();
		this.markContainingBlockForUpdate(null);
		worldObj.addBlockEvent(getPos(), this.getBlockType(), 0, 0);
	}
	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==0)
		{
			this.markContainingBlockForUpdate(null);
			return true;
		}
		return false;
	}

	public int getMaxStorage()
	{
		return IEConfig.Machines.capacitorLV_storage;
	}
	public int getMaxInput()
	{
		return IEConfig.Machines.capacitorLV_input;
	}
	public int getMaxOutput()
	{
		return IEConfig.Machines.capacitorLV_output;
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setIntArray("sideConfig", sideConfig);
		energyStorage.writeToNBT(nbt);
	}
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		sideConfig = nbt.getIntArray("sideConfig");
		if(sideConfig==null || sideConfig.length<6)
			sideConfig = new int[6];
		energyStorage.readFromNBT(nbt);
	}

	@Override
	public boolean canConnectEnergy(EnumFacing fd)
	{
		return !(fd.ordinal() >= sideConfig.length || sideConfig[fd.ordinal()] < 0);
	}
	@Override
	public int extractEnergy(EnumFacing fd, int amount, boolean simulate)
	{
		if(worldObj.isRemote || fd.ordinal()>=sideConfig.length || sideConfig[fd.ordinal()]!=1)
			return 0;
		int r = energyStorage.extractEnergy(amount, simulate);
		this.markContainingBlockForUpdate(null);
		return r;
	}
	@Override
	public int getEnergyStored(EnumFacing fd)
	{
		return energyStorage.getEnergyStored();
	}
	@Override
	public int getMaxEnergyStored(EnumFacing fd)
	{
		return energyStorage.getMaxEnergyStored();
	}
	@Override
	public int receiveEnergy(EnumFacing fd, int amount, boolean simulate)
	{
		if(worldObj.isRemote || fd.ordinal()>=sideConfig.length || sideConfig[fd.ordinal()]!=0)
			return 0;
		int r = energyStorage.receiveEnergy(amount, simulate);
		return r;
	}
	@Override
	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer)
	{
		if(hammer && IEConfig.colourblindSupport)
		{
			int i = sideConfig[Math.min(sideConfig.length-1, mop.sideHit.ordinal())];
			int j = sideConfig[Math.min(sideConfig.length-1, mop.sideHit.getOpposite().ordinal())];
			return new String[]{
					I18n.format(Lib.DESC_INFO+"blockSide.facing")
					+": "+ I18n.format(Lib.DESC_INFO+"blockSide.connectEnergy."+i),
					I18n.format(Lib.DESC_INFO+"blockSide.opposite")
					+": "+ I18n.format(Lib.DESC_INFO+"blockSide.connectEnergy."+j)
			};
		}
		return null;
	}
	@Override
	public boolean useNixieFont(EntityPlayer player, RayTraceResult mop)
	{
		return false;
	}

	@Override
	public int getComparatorInputOverride()
	{
		return this.comparatorOutput;
	}

	@Override
	public ItemStack getTileDrop(EntityPlayer player, IBlockState state)
	{
		ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
		if(energyStorage.getEnergyStored()>0)
			ItemNBTHelper.setInt(stack, "energyStorage", energyStorage.getEnergyStored());
		ItemNBTHelper.setIntArray(stack, "sideConfig", sideConfig);
		return stack;
	}
	@Override
	public void readOnPlacement(EntityLivingBase placer, ItemStack stack)
	{
		if(ItemNBTHelper.hasKey(stack, "energyStorage"))
			energyStorage.setEnergy(ItemNBTHelper.getInt(stack, "energyStorage"));
		if(ItemNBTHelper.hasKey(stack, "sideConfig"))
			sideConfig = ItemNBTHelper.getIntArray(stack, "sideConfig");
	}
}