/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires;


import blusunrize.immersiveengineering.api.IEProperties.ConnectionModelData;
import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.client.utils.CombinedModelData;
import blusunrize.immersiveengineering.client.utils.SinglePropertyModelData;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class ImmersiveConnectableTileEntity extends IEBaseTileEntity implements IImmersiveConnectable
{
	protected GlobalWireNetwork globalNet;

	public ImmersiveConnectableTileEntity(TileEntityType<? extends ImmersiveConnectableTileEntity> type)
	{
		super(type);
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
	public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget)
	{
	}

	@Override
	public void removeCable(Connection connection, ConnectionPoint attachedPoint)
	{
		this.markDirty();
		if(world!=null)
		{
			BlockState state = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, state, state, 3);
		}
	}

	@Override
	public void setWorldAndPos(World worldIn, BlockPos pos)
	{
		super.setWorldAndPos(worldIn, pos);
		globalNet = GlobalWireNetwork.getNetwork(worldIn);
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==-1||id==255)
		{
			BlockState state = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, state, state, 3);
			return true;
		}
		else if(id==254)
		{
			BlockState state = world.getBlockState(pos);
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
	public void readCustomNBT(@Nonnull CompoundNBT nbt, boolean descPacket)
	{
	}

	@Override
	public void writeCustomNBT(@Nonnull CompoundNBT nbt, boolean descPacket)
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
			{
				WireLogger.logger.warn("Aborting and returning empty data: null connections at {}", cp);
				return new ConnectionModelData(ImmutableSet.of(), pos);
			}
			//TODO change model data to only include catenary (a, oX, oY) and number of vertices to render
			for(Connection c : conns)
			{
				ConnectionPoint other = c.getOtherEnd(cp);
				if(!c.isInternal())
				{
					IImmersiveConnectable otherConnector = globalNet.getLocalNet(other).getConnector(other);
					if(otherConnector!=null&&!otherConnector.isProxy())
					{
						// generate subvertices
						c.generateCatenaryData(world);
						ret.add(c);
					}
				}
			}
		}
		return new ConnectionModelData(ret, pos);
	}

	@Nonnull
	@Override
	public IModelData getModelData()
	{
		return new CombinedModelData(new SinglePropertyModelData<>(genConnBlockstate(), Model.CONNECTIONS),
				super.getModelData());
	}

	@Override
	public void onChunkUnloaded()
	{
		super.onChunkUnloaded();
		globalNet.onConnectorUnload(pos, this);
	}

	@Override
	public void onLoad()
	{
		super.onLoad();
		globalNet.onConnectorLoad(this, world);
	}

	@Override
	public void remove()
	{
		super.remove();
		globalNet.removeConnector(this);
	}
	@Override
	public Collection<ConnectionPoint> getConnectionPoints()
	{
		return ImmutableList.of(new ConnectionPoint(pos, 0));
	}

	private final Int2ObjectMap<LocalWireNetwork> cachedLocalNets = new Int2ObjectArrayMap<>();

	protected LocalWireNetwork getLocalNet(int cpIndex)
	{
		LocalWireNetwork ret = cachedLocalNets.get(cpIndex);
		ConnectionPoint cp = new ConnectionPoint(getPos(), cpIndex);
		if(ret==null||!ret.isValid(cp))
		{
			ret = globalNet.getLocalNet(cp);
			cachedLocalNets.put(cpIndex, ret);
		}
		return ret;
	}

	@Override
	public BlockPos getPosition()
	{
		return pos;
	}
}
