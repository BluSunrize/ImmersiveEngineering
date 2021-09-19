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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;

public class WireTemplateHelper
{
	private static final String CONNECTIONS_KEY = Lib.MODID+":connections";

	public static void fillConnectionsInArea(
			Level worldIn, BlockPos startPos, BlockPos size, IConnectionTemplate template
	)
	{
		template.getStoredConnections().clear();
		GlobalWireNetwork net = getNetwork(worldIn);
		if (net == null)
			return;
		BlockPos endPos = startPos.offset(size).offset(-1, -1, -1);
		BoundingBox box = new BoundingBox(startPos, endPos);
		Vec3i offset = new Vec3i(box.x0, box.y0, box.z0);
		for(BlockPos pos : BlockPos.betweenClosed(startPos, endPos))
		{
			BlockEntity te = worldIn.getBlockEntity(pos);
			if(!(te instanceof IImmersiveConnectable))
				continue;
			for(ConnectionPoint cp : ((IImmersiveConnectable)te).getConnectionPoints())
				for(Connection conn : net.getLocalNet(cp).getConnections(cp))
				{
					if(conn.isInternal())
						continue;
					ConnectionPoint otherEnd = conn.getOtherEnd(cp);
					if(otherEnd.compareTo(cp) < 0||!box.isInside(otherEnd.getPosition()))
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
			ServerLevelAccessor iworld, IConnectionTemplate template, StructurePlaceSettings orientation, BlockPos startPos
	)
	{
		if(template.getStoredConnections().isEmpty())
			return;
		Level world = iworld.getLevel();
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

	public static void addConnectionsToNBT(IConnectionTemplate template, CompoundTag out)
	{
		if(template.getStoredConnections().isEmpty())
			return;
		ListTag connectionsNBT = new ListTag();
		for(Connection c : template.getStoredConnections())
			connectionsNBT.add(c.toNBT());
		out.put(CONNECTIONS_KEY, connectionsNBT);
	}

	public static void readConnectionsFromNBT(CompoundTag compound, IConnectionTemplate template)
	{
		ListTag connectionsNBT = compound.getList(CONNECTIONS_KEY, NBT.TAG_COMPOUND);
		template.getStoredConnections().clear();
		for(int i = 0; i < connectionsNBT.size(); i++)
			template.getStoredConnections().add(new Connection(connectionsNBT.getCompound(i)));
	}

	@Nullable
	private static ConnectionPoint getAbsolutePoint(
			ConnectionPoint relative, StructurePlaceSettings orientation, Level world, BlockPos base
	)
	{
		BlockPos absolutePos = StructureTemplate.calculateRelativePosition(orientation, relative.getPosition()).offset(base);
		BlockEntity connector = world.getBlockEntity(absolutePos);
		if(!(connector instanceof IImmersiveConnectable))
			return null;
		ConnectionPoint point = new ConnectionPoint(absolutePos, relative.getIndex());
		if(!((IImmersiveConnectable)connector).getConnectionPoints().contains(point))
			return null;
		return point;
	}

	@Nullable
	private static GlobalWireNetwork getNetwork(Level world) {
		return world.getCapability(NetHandlerCapability.NET_CAPABILITY)
				.resolve()
				.orElse(null);
	}
}
