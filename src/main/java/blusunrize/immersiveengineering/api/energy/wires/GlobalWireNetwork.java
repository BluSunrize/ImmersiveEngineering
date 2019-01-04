/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy.wires;

import blusunrize.immersiveengineering.api.ApiUtils;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nonnull;
import java.util.*;

public class GlobalWireNetwork
{
	private Map<BlockPos, LocalWireNetwork> localNets = new HashMap<>();

	@Nonnull
	public static GlobalWireNetwork getNetwork(World w) {
		if (!w.hasCapability(NetHandlerCapability.NET_CAPABILITY, null))
			throw new RuntimeException("No net handler found for dimension "+w.provider.getDimension()+", remote: "+w.isRemote);
		return Objects.requireNonNull(w.getCapability(NetHandlerCapability.NET_CAPABILITY, null));
	}

	public Connection addConnection(BlockPos posA, BlockPos posB, WireType type)
	{
		IImmersiveConnectable iicA = getLocalNet(posA).getConnector(posA);
		IImmersiveConnectable iicB = getLocalNet(posB).getConnector(posB);
		return addConnection(iicA, iicB, posA, posB, type);
	}

	public Connection addConnection(IImmersiveConnectable iicA, IImmersiveConnectable iicB, WireType type)
	{
		BlockPos posA = ApiUtils.toBlockPos(iicA);
		BlockPos posB = ApiUtils.toBlockPos(iicB);
		return addConnection(iicA, iicB, posA, posB, type);
	}
	private Connection addConnection(IImmersiveConnectable iicA, IImmersiveConnectable iicB, BlockPos posA, BlockPos posB,
									 WireType type)
	{
		LocalWireNetwork netA = localNets.get(posA);
		LocalWireNetwork netB = localNets.get(posB);
		Connection conn = new Connection(type, posA, posB);

		Collection<BlockPos> toSet = new ArrayList<>(2);
		LocalWireNetwork joined;
		if(netA==null&&netB==null)
		{
			joined = new LocalWireNetwork(this);
			toSet.add(posA);
			toSet.add(posB);
			joined.addConnector(posA, iicA);
			joined.addConnector(posB, iicB);
		}
		else if(netA==null)
		{
			toSet.add(posA);
			joined = netB;
			joined.addConnector(posA, iicA);
		}
		else if(netB==null)
		{
			toSet.add(posB);
			joined = netA;
			joined.addConnector(posB, iicB);
		}
		else if(netA!=netB)
		{
			joined = netA.merge(netB);
			toSet = joined.getConnectors();
		}
		else
		{
			joined = netA;
		}
		joined.addConnection(conn);
		for(BlockPos p : toSet)
			localNets.put(p, joined);
		return conn;
	}

	public void removeConnection(Connection c)
	{
		LocalWireNetwork oldNet = localNets.get(c.getEndA());
		oldNet.removeConnection(c);
		//TODO move to private method!
		Collection<LocalWireNetwork> newNets = oldNet.split();
		for(LocalWireNetwork net : newNets)
			if(net!=oldNet)
				for(BlockPos p : net.getConnectors())
					localNets.put(p, net);
	}

	public void readFromNBT(NBTTagCompound nbt)
	{
		NBTTagList locals = nbt.getTagList("locals", NBT.TAG_COMPOUND);
		for(NBTBase b : locals)
		{
			NBTTagCompound subnet = (NBTTagCompound)b;
			LocalWireNetwork localNet = new LocalWireNetwork(subnet, this);
			for(BlockPos p : localNet.getConnectors())
				localNets.put(p, localNet);
		}
	}

	public NBTTagCompound writeToNBT()
	{
		NBTTagCompound ret = new NBTTagCompound();
		NBTTagList locals = new NBTTagList();
		Set<LocalWireNetwork> savedNets = Collections.newSetFromMap(new IdentityHashMap<>());
		for(LocalWireNetwork local : localNets.values())
			if(savedNets.add(local))
				locals.appendTag(local.writeToNBT());
		ret.setTag("locals", locals);
		return ret;
	}

	public LocalWireNetwork getLocalNet(BlockPos pos) {
		return localNets.computeIfAbsent(pos, p->new LocalWireNetwork(this));
	}

	public LocalWireNetwork getNullableLocalNet(BlockPos pos) {
		return localNets.get(pos);
	}

	public static class Connection
	{
		public final WireType type;
		private final BlockPos endA;
		private final BlockPos endB;
		public double catOffsetX;
		public double catOffsetY;
		//TODO better name? move all of these somewhere else?
		public double catA;

		public Connection(WireType type, BlockPos endA, BlockPos endB)
		{
			this.type = type;
			this.endA = endA;
			this.endB = endB;
		}

		public Connection(NBTTagCompound nbt)
		{
			type = WireType.getValue(nbt.getString("type"));
			endA = NBTUtil.getPosFromTag(nbt.getCompoundTag("endA"));
			endB = NBTUtil.getPosFromTag(nbt.getCompoundTag("endB"));
		}

		public BlockPos getOtherEnd(BlockPos known)
		{
			if(known.equals(endA))
				return endB;
			else
				return endA;
		}

		public BlockPos getEndA()
		{
			return endA;
		}

		public BlockPos getEndB()
		{
			return endB;
		}

		public boolean isPositiveEnd(BlockPos p)
		{
			return p.compareTo(endB) > 0;
		}

		public NBTTagCompound toNBT()
		{
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setTag("endA", NBTUtil.createPosTag(endA));
			nbt.setTag("endB", NBTUtil.createPosTag(endB));
			nbt.setString("type", type.getUniqueName());
			return nbt;
		}


		public void generateSubvertices(World world)
		{
			//TODO
		}

		//TODO
		public boolean hasCatenaryVertices()
		{
			return true;
		}

		public boolean isEnd(BlockPos p)
		{
			return p.equals(endA)||p.equals(endB);
		}

		//TODO proper impl
		public Vec3d[] getCatenaryVertices()
		{
			Vec3d[] ret = new Vec3d[17];
			for(int i = 0; i <= 16; ++i)
			{
				double lambda = i/16D;
				//TODO symmetry?
				ret[i] = new Vec3d(lambda*(endA.getX()-endB.getX()),
						lambda*(endA.getY()-endB.getY()),
						lambda*(endA.getZ()-endB.getZ()));
			}
			return ret;
		}
	}
}
