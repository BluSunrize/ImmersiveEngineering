/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.AbstractConnection;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

//@Optional.Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = "IC2")
public class TileEntityConnectorLV extends TileEntityImmersiveConnectable implements ITickable, IDirectionalTile, IIEInternalFluxHandler, IBlockBounds//, ic2.api.energy.tile.IEnergySink
{
	boolean inICNet=false;
	public EnumFacing facing = EnumFacing.DOWN;
	private long lastTransfer = -1;
	public int currentTickAccepted=0;
	public static int[] connectorInputValues = {};
	private FluxStorage energyStorage = new FluxStorage(getMaxInput(),getMaxInput(),0);

	boolean firstTick = true;

	@Override
	public void update()
	{
		if(!world.isRemote)
		{
			//				if(Lib.IC2 && !this.inICNet)
			//				{
			//					IC2Helper.loadIC2Tile(this);
			//					this.inICNet = true;
			//				}
			if(energyStorage.getEnergyStored()>0)
			{
				int temp = this.transferEnergy(energyStorage.getEnergyStored(), true, 0);
				if(temp>0)
				{
					energyStorage.modifyEnergyStored(-this.transferEnergy(temp, false, 0));
					markDirty();
				}
				addAvailableEnergy(-1F, null);
			}
			currentTickAccepted = 0;
		}
		else if (firstTick)
		{
			Set<Connection> conns = ImmersiveNetHandler.INSTANCE.getConnections(world, pos);
			if (conns!=null)
				for (Connection conn:conns)
					if (pos.compareTo(conn.end)<0&&world.isBlockLoaded(conn.end))
						this.markContainingBlockForUpdate(null);
			firstTick = false;
		}
	}
	//	@Override
	//	public void invalidate()
	//	{
	//		super.invalidate();
	//		unload();
	//	}
	//	void unload()
	//	{
	//		if(Lib.IC2 && this.inICNet)
	//		{
	//			IC2Helper.unloadIC2Tile(this);
	//			this.inICNet = false;
	//		}
	//	}
	//	@Override
	//	public void onChunkUnload()
	//	{
	//		unload();
	//	}


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
		return 0;
	}
	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return true;
	}
	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return false;
	}
	@Override
	public boolean canRotate(EnumFacing axis)
	{
		return false;
	}

	@Override
	protected boolean canTakeLV()
	{
		return true;
	}

	@Override
	public boolean isEnergyOutput()
	{
		BlockPos outPos = getPos().offset(facing);
		if(isRelay())
			return false;
		TileEntity tile = Utils.getExistingTileEntity(world, outPos);
		return EnergyHelper.isFluxReceiver(tile, facing.getOpposite());
	}
	@Override
	public int outputEnergy(int amount, boolean simulate, int energyType)
	{
		if(isRelay())
			return 0;
		int acceptanceLeft = getMaxOutput()-currentTickAccepted;
		if(acceptanceLeft<=0)
			return 0;
		int toAccept = Math.min(acceptanceLeft, amount);

		TileEntity capacitor = Utils.getExistingTileEntity(world, getPos().offset(facing));
		int ret = EnergyHelper.insertFlux(capacitor, facing.getOpposite(), toAccept, simulate);
		//		if(capacitor instanceof IFluxReceiver && ((IFluxReceiver)capacitor).canConnectEnergy(facing.getOpposite()))
		//		{
		//			ret = ((IFluxReceiver)capacitor).receiveEnergy(facing.getOpposite(), toAccept, simulate);
		//		}
		//		else if(capacitor instanceof IEnergyReceiver && ((IEnergyReceiver)capacitor).canConnectEnergy(facing.getOpposite()))
		//		{
		//			ret = ((IEnergyReceiver)capacitor).receiveEnergy(facing.getOpposite(), toAccept, simulate);
		//		}
		//		else if(Lib.IC2 && IC2Helper.isAcceptingEnergySink(capacitor, this, fd.getOpposite()))
		//		{
		//			double left = IC2Helper.injectEnergy(capacitor, fd.getOpposite(), ModCompatability.convertRFtoEU(toAccept, getIC2Tier()), canTakeHV()?(256*256): canTakeMV()?(128*128) : (32*32), simulate);
		//			ret = amount-ModCompatability.convertEUtoRF(left);
		//		}
		//		else if(Lib.GREG && GregTechHelper.gregtech_isValidEnergyOutput(capacitor))
		//		{
		//			long translAmount = (long)ModCompatability.convertRFtoEU(toAccept, getIC2Tier());
		//			long accepted = GregTechHelper.gregtech_outputGTPower(capacitor, (byte)fd.getOpposite().ordinal(), translAmount, 1L, simulate);
		//			int reConv =  ModCompatability.convertEUtoRF(accepted);
		//			ret = reConv;
		//		}
		if(!simulate)
			currentTickAccepted+=ret;
		return ret;
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", facing.ordinal());
		nbt.setLong("lastTransfer", lastTransfer);
		energyStorage.writeToNBT(nbt);
	}
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = EnumFacing.getFront(nbt.getInteger("facing"));
		lastTransfer = nbt.getLong("lastTransfer");
		energyStorage.readFromNBT(nbt);
	}

	@Override
	public Vec3d getConnectionOffset(Connection con)
	{
		EnumFacing side = facing.getOpposite();
		double conRadius = con.cableType.getRenderDiameter()/2;
		return new Vec3d(.5-conRadius*side.getFrontOffsetX(), .5-conRadius*side.getFrontOffsetY(), .5-conRadius*side.getFrontOffsetZ());
	}

	@SideOnly(Side.CLIENT)
	private AxisAlignedBB renderAABB;
	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		//		if(renderAABB==null)
		//		{
		//			if(Config.getBoolean("increasedRenderboxes"))
		//			{
		int inc = getRenderRadiusIncrease();
		return new AxisAlignedBB(this.pos.getX()-inc,this.pos.getY()-inc,this.pos.getZ()-inc, this.pos.getX()+inc+1,this.pos.getY()+inc+1,this.pos.getZ()+inc+1);
		//				renderAABB = new AxisAlignedBB(this.pos.getX()-inc,this.pos.getY()-inc,this.pos.getZ()-inc, this.pos.getX()+inc+1,this.pos.getY()+inc+1,this.pos.getZ()+inc+1);
		//			}
		//			else
		//				renderAABB = super.getRenderBoundingBox();
		//		}
		//		return renderAABB;
	}
	int getRenderRadiusIncrease()
	{
		return WireType.COPPER.getMaxLength();
	}

	IEForgeEnergyWrapper energyWrapper;
	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing)
	{
		if(facing!=this.facing || isRelay())
			return null;
		if(energyWrapper==null || energyWrapper.side!=this.facing)
			energyWrapper = new IEForgeEnergyWrapper(this, this.facing);
		return energyWrapper;
	}

	@Override
	public FluxStorage getFluxStorage()
	{
		return energyStorage;
	}
	@Override
	public SideConfig getEnergySideConfig(EnumFacing facing)
	{
		return (!isRelay()&&facing==this.facing)?SideConfig.INPUT:SideConfig.NONE;
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from)
	{
		if(isRelay())
			return false;
		return from==facing;
	}
	@Override
	public int receiveEnergy(EnumFacing from, int energy, boolean simulate)
	{
		if(world.isRemote || isRelay())
			return 0;
		if(world.getTotalWorldTime()==lastTransfer)
			return 0;

		int accepted = Math.min(Math.min(getMaxOutput(),getMaxInput()), energy);
		accepted = Math.min(getMaxOutput()-energyStorage.getEnergyStored(), accepted);
		if(accepted<=0)
			return 0;

		if(!simulate)
		{
			energyStorage.modifyEnergyStored(accepted);
			lastTransfer = world.getTotalWorldTime();
			markDirty();
		}

		return accepted;
	}

	@Override
	public int getEnergyStored(EnumFacing from)
	{
		if(isRelay())
			return 0;
		return energyStorage.getEnergyStored();
	}
	@Override
	public int getMaxEnergyStored(EnumFacing from)
	{
		if(isRelay())
			return 0;
		return getMaxInput();
	}
	@Override
	public int extractEnergy(EnumFacing from, int energy, boolean simulate)
	{
		return 0;
	}

	public int transferEnergy(int energy, boolean simulate, final int energyType)
	{
		int received = 0;
		if(!world.isRemote)
		{
			Set<AbstractConnection> outputs = ImmersiveNetHandler.INSTANCE.getIndirectEnergyConnections(Utils.toCC(this),
					world, true);
			int powerLeft = Math.min(Math.min(getMaxOutput(),getMaxInput()), energy);
			final int powerForSort = powerLeft;

			if(outputs.size()<1)
				return 0;

			int sum = 0;
			HashMap<AbstractConnection,Integer> powerSorting = new HashMap<>();
			for(AbstractConnection con : outputs)
			{
				IImmersiveConnectable end = ApiUtils.toIIC(con.end, world);
				if(con.cableType!=null && end!=null)
				{
					if (end.isEnergyOutput())
					{
						int atmOut = Math.min(powerForSort, con.cableType.getTransferRate());
						int tempR = end.outputEnergy(atmOut, true, energyType);
						if (tempR > 0)
						{
							powerSorting.put(con, tempR);
							sum += tempR;
						}
					}
				}
			}

			if(sum>0)
				for(AbstractConnection con : powerSorting.keySet())
				{
					IImmersiveConnectable end = ApiUtils.toIIC(con.end, world);
					if(con.cableType!=null && end!=null)
					{
						float prio = powerSorting.get(con)/(float)sum;
						int output = (int)(powerForSort*prio);

						int tempR = end.outputEnergy(Math.min(output, con.cableType.getTransferRate()), true, energyType);
						int r = tempR;
						int maxInput = getMaxInput();
						tempR -= (int) Math.max(0, Math.floor(tempR*con.getPreciseLossRate(tempR,maxInput)));
						end.outputEnergy(tempR, simulate, energyType);
						HashSet<IImmersiveConnectable> passedConnectors = new HashSet<IImmersiveConnectable>();
						float intermediaryLoss = 0;
						for(Connection sub : con.subConnections)
						{
							float length = sub.length/(float)sub.cableType.getMaxLength();
							float baseLoss = (float)sub.cableType.getLossRatio();
							float mod = (((maxInput-tempR)/(float)maxInput)/.25f)*.1f;
							intermediaryLoss = MathHelper.clamp(intermediaryLoss+length*(baseLoss+baseLoss*mod), 0,1);

							int transferredPerCon = ImmersiveNetHandler.INSTANCE.getTransferedRates(world.provider.getDimension()).getOrDefault(sub, 0);
							transferredPerCon += r;
							if(!simulate)
							{
								ImmersiveNetHandler.INSTANCE.getTransferedRates(world.provider.getDimension()).put(sub,transferredPerCon);
								IImmersiveConnectable subStart = ApiUtils.toIIC(sub.start,world);
								IImmersiveConnectable subEnd = ApiUtils.toIIC(sub.end,world);
								if(subStart!=null && passedConnectors.add(subStart))
									subStart.onEnergyPassthrough(r-r*intermediaryLoss);
								if(subEnd!=null && passedConnectors.add(subEnd))
									subEnd.onEnergyPassthrough(r-r*intermediaryLoss);
							}
						}
						received += r;
						powerLeft -= r;
						if(powerLeft<=0)
							break;
					}
				}
			for(AbstractConnection con : outputs)
			{
				IImmersiveConnectable end = ApiUtils.toIIC(con.end, world);
				if(con.cableType!=null && end!=null && end.allowEnergyToPass(null))
				{
					Pair<Float, Consumer<Float>> e = getEnergyForConnection(con);
					end.addAvailableEnergy(e.getKey(), e.getValue());
				}
			}
		}
		return received;
	}

	private Pair<Float, Consumer<Float>> getEnergyForConnection(@Nullable AbstractConnection c)
	{
		float loss = c!=null?c.getAverageLossRate():0;
		float max = (1-loss)*energyStorage.getEnergyStored();
		Consumer<Float> extract = (energy)->{
			energyStorage.modifyEnergyStored((int) (-energy/(1-loss)));
		};
		return new ImmutablePair<>(max, extract);
	}


	public int getMaxInput()
	{
		return connectorInputValues[0];
	}
	public int getMaxOutput()
	{
		return connectorInputValues[0];
	}

	@Nullable
	@Override
	protected Pair<Float, Consumer<Float>> getOwnEnergy()
	{
		return getEnergyForConnection(null);
	}

	@Override
	public float[] getBlockBounds()
	{
		float length = this instanceof TileEntityRelayHV?.875f: this instanceof TileEntityConnectorHV?.75f: this instanceof TileEntityConnectorMV?.5625f: .5f;
		float wMin = this instanceof TileEntityConnectorStructural?.25f:.3125f;
		float wMax = this instanceof TileEntityConnectorStructural?.75f:.6875f;
		switch(facing.getOpposite() )
		{
			case UP:
				return new float[]{wMin,0,wMin,  wMax,length,wMax};
			case DOWN:
				return new float[]{wMin,1-length,wMin,  wMax,1,wMax};
			case SOUTH:
				return new float[]{wMin,wMin,0,  wMax,wMax,length};
			case NORTH:
				return new float[]{wMin,wMin,1-length,  wMax,wMax,1};
			case EAST:
				return new float[]{0,wMin,wMin,  length,wMax,wMax};
			case WEST:
				return new float[]{1-length,wMin,wMin,  1,wMax,wMax};
		}
		return new float[]{0,0,0,1,1,1};
	}

	//	@Optional.Method(modid = "IC2")
	//	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	//	{
	//		return Lib.IC2 && canConnectEnergy(direction);
	//	}
	//	@Optional.Method(modid = "IC2")
	//	public double getDemandedEnergy()
	//	{
	//		return ModCompatability.convertRFtoEU(getMaxInput(), getIC2Tier());
	//	}
	//	@Optional.Method(modid = "IC2")
	//	public int getSinkTier()
	//	{
	//		return getIC2Tier();
	//	}
	//	int getIC2Tier()
	//	{
	//		return this.canTakeHV()?3: this.canTakeMV()?2: 1;
	//	}
	//	@Optional.Method(modid = "IC2")
	//	public double injectEnergy(ForgeDirection directionFrom, double amount, double voltage)
	//	{
	//		int rf = ModCompatability.convertEUtoRF(amount);
	//		if(rf>this.getMaxInput())//More Input than allowed results in blocking
	//			return amount;
	//		int rSimul = transferEnergy(rf, true, 1);
	//		if(rSimul==0)//This will prevent full power void but allow partial transfer
	//			return amount;
	//		int r = transferEnergy(rf, false, 1);
	//		double eu = ModCompatability.convertRFtoEU(r, getIC2Tier());
	//		return amount-eu;
	//	}
}