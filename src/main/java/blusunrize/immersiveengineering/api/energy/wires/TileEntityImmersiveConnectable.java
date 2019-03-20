/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy.wires;


import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties.ConnectionModelData;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.api.energy.wires.WireType.*;

public abstract class TileEntityImmersiveConnectable extends TileEntityIEBase implements IImmersiveConnectable
{
	protected GlobalWireNetwork globalNet;

	protected boolean canTakeLV()
	{
		return false;
	}

	protected boolean canTakeMV()
	{
		return false;
	}

	protected boolean canTakeHV()
	{
		return false;
	}

	protected boolean isRelay()
	{
		return false;
	}

	@Override
	public void onEnergyPassthrough(int amount)
	{
	}

	@Override
	public boolean canConnect()
	{
		return true;
	}

	@Override
	public BlockPos getConnectionMaster(WireType cableType, TargetingInfo target)
	{
		return getPos();
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset)
	{
		String category = cableType.getCategory();
		//TODO handle connectors vs relays, but not in here
		return (HV_CATEGORY.equals(category)&&canTakeHV())
				||(MV_CATEGORY.equals(category)&&canTakeMV())
				||(LV_CATEGORY.equals(category)&&canTakeLV());
	}

	@Override
	public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget)
	{
	}

	@Override
	public void removeCable(Connection connection)
	{
		this.markDirty();
		if(world!=null)
		{
			IBlockState state = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, state, state, 3);
		}
	}

	private List<Pair<Float, Consumer<Float>>> sources = new ArrayList<>();
	private long lastSourceUpdate = 0;

	@Override
	public void addAvailableEnergy(float amount, Consumer<Float> consume)
	{
		long currentTime = world.getTotalWorldTime();
		if(lastSourceUpdate!=currentTime)
		{
			sources.clear();
			Pair<Float, Consumer<Float>> own = getOwnEnergy();
			if(own!=null)
				sources.add(own);
			lastSourceUpdate = currentTime;
		}
		if(amount > 0&&consume!=null)
			sources.add(new ImmutablePair<>(amount, consume));
	}

	@Nullable
	protected Pair<Float, Consumer<Float>> getOwnEnergy()
	{
		return null;
	}

	@Override
	public void setWorld(@Nonnull World worldIn)
	{
		super.setWorld(worldIn);
		globalNet = GlobalWireNetwork.getNetwork(worldIn);
	}

	protected float getBaseDamage(Connection c)
	{
		if(c.type==COPPER)
			return 8*2F/c.type.getTransferRate();
		else if(c.type==ELECTRUM)
			return 8*5F/c.type.getTransferRate();
		else if(c.type==STEEL)
			return 8*15F/c.type.getTransferRate();
		return 0;
	}

	protected float getMaxDamage(Connection c)
	{
		return c.type.getTransferRate()/8F*getBaseDamage(c);
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==-1||id==255)
		{
			IBlockState state = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, state, state, 3);
			return true;
		}
		else if(id==254)
		{
			IBlockState state = world.getBlockState(pos);
			if(state instanceof IExtendedBlockState)
			{
				state = state.getActualState(world, getPos());
				state = state.getBlock().getExtendedState(state, world, getPos());
				ImmersiveEngineering.proxy.removeStateFromSmartModelCache((IExtendedBlockState)state);
				ImmersiveEngineering.proxy.removeStateFromConnectionModelCache((IExtendedBlockState)state);
			}
			world.notifyBlockUpdate(pos, state, state, 3);
			return true;
		}
		return super.receiveClientEvent(id, arg);
	}

	@Nullable
	@Override
	public ConnectionPoint getTargetedPoint(TargetingInfo info, Vec3i offset)
	{
		return new ConnectionPoint(pos, 0);
	}

	@Override
	public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket)
	{
	}

	@Override
	public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket)
	{
	}

	public ConnectionModelData genConnBlockstate()
	{
		Set<Connection> ret = new HashSet<>();
		for(ConnectionPoint cp : getConnectionPoints())
		{
			LocalWireNetwork local = globalNet.getLocalNet(cp);
			Collection<Connection> conns = local.getConnections(cp);
			if(conns==null)
				return new ConnectionModelData(ImmutableSet.of(), pos);
			//TODO change model data to only include catenary (a, oX, oY) and number of vertices to render
			for(Connection c : conns)
				if(!c.isInternal())
				{
					// generate subvertices
					c.generateCatenaryData(world);
					ret.add(c);
				}
		}
		return new ConnectionModelData(ret, pos);
	}

	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		IELogger.logger.info("Unloading connector at {}", pos);
		globalNet.onConnectorUnload(pos, this);
	}

	@Override
	public void onLoad()
	{
		super.onLoad();
		IELogger.info("Loading connector at {}", pos);
		globalNet.onConnectorLoad(this, world);
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		IELogger.logger.info("Removing connector at {}", pos);
		globalNet.removeConnector(this);
	}

	@Override
	public Collection<ConnectionPoint> getConnectionPoints()
	{
		//TODO override in the relevant conn classes
		return ImmutableList.of(new ConnectionPoint(pos, 0));
	}
}