/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.wires;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.wires.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;

public class WireTemplateHelper
{
	private static final String CONNECTIONS_KEY = Lib.MODID+":connections";

	public static void fillConnectionsInArea(
			World worldIn, BlockPos startPos, BlockPos size, IConnectionTemplate template
	)
	{
		template.getStoredConnections().clear();
		GlobalWireNetwork net = getNetwork(worldIn);
		if (net == null)
			return;
		BlockPos endPos = startPos.add(size).add(-1, -1, -1);
		MutableBoundingBox box = new MutableBoundingBox(startPos, endPos);
		Vector3i offset = new Vector3i(box.minX, box.minY, box.minZ);
		for(BlockPos pos : BlockPos.getAllInBoxMutable(startPos, endPos))
		{
			TileEntity te = worldIn.getTileEntity(pos);
			if(!(te instanceof IImmersiveConnectable))
				continue;
			for(ConnectionPoint cp : ((IImmersiveConnectable)te).getConnectionPoints())
				for(Connection conn : net.getLocalNet(cp).getConnections(cp))
				{
					if(conn.isInternal())
						continue;
					ConnectionPoint otherEnd = conn.getOtherEnd(cp);
					if(otherEnd.compareTo(cp) < 0||!box.isVecInside(otherEnd.getPosition()))
						// only add once and only if fully in captured area
						continue;
					template.getStoredConnections().add(new Connection(
							conn.type,
							new ConnectionPoint(pos.subtract(offset), cp.getIndex()),
							new ConnectionPoint(otherEnd.getPosition().subtract(offset), otherEnd.getIndex())
					));
				}
		}
	}

	public static void addConnectionsFromTemplate(
			IServerWorld iworld, IConnectionTemplate template, PlacementSettings orientation, BlockPos startPos
	)
	{
		if(template.getStoredConnections().isEmpty())
			return;
		World world = iworld.getWorld();
		GlobalWireNetwork net = getNetwork(world);
		if (net == null)
			return;
		for(Connection relative : template.getStoredConnections())
		{
			ConnectionPoint endA = getAbsolutePoint(relative.getEndA(), orientation, world, startPos);
			ConnectionPoint endB = getAbsolutePoint(relative.getEndB(), orientation, world, startPos);
			if(endA==null||endB==null)
				continue;
			net.addConnection(new Connection(relative.type, endA, endB));
		}
	}

	public static void addConnectionsToNBT(IConnectionTemplate template, CompoundNBT out)
	{
		if(template.getStoredConnections().isEmpty())
			return;
		ListNBT connectionsNBT = new ListNBT();
		for(Connection c : template.getStoredConnections())
			connectionsNBT.add(c.toNBT());
		out.put(CONNECTIONS_KEY, connectionsNBT);
	}

	public static void readConnectionsFromNBT(CompoundNBT compound, IConnectionTemplate template)
	{
		ListNBT connectionsNBT = compound.getList(CONNECTIONS_KEY, NBT.TAG_COMPOUND);
		template.getStoredConnections().clear();
		for(int i = 0; i < connectionsNBT.size(); i++)
			template.getStoredConnections().add(new Connection(connectionsNBT.getCompound(i)));
	}

	@Nullable
	private static ConnectionPoint getAbsolutePoint(
			ConnectionPoint relative, PlacementSettings orientation, World world, BlockPos base
	)
	{
		BlockPos absolutePos = Template.transformedBlockPos(orientation, relative.getPosition()).add(base);
		TileEntity connector = world.getTileEntity(absolutePos);
		if(!(connector instanceof IImmersiveConnectable))
			return null;
		ConnectionPoint point = new ConnectionPoint(absolutePos, relative.getIndex());
		if(!((IImmersiveConnectable)connector).getConnectionPoints().contains(point))
			return null;
		return point;
	}

	@Nullable
	private static GlobalWireNetwork getNetwork(World world) {
		return world.getCapability(NetHandlerCapability.NET_CAPABILITY)
				.resolve()
				.orElse(null);
	}
}
