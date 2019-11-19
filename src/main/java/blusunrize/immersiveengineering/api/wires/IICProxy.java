/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires;

import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class IICProxy implements IImmersiveConnectable
{
	private DimensionType dim;
	private BlockPos pos;

	public IICProxy(DimensionType dimension, BlockPos pos)
	{
		dim = dimension;
		this.pos = pos;
	}

	public IICProxy(TileEntity te)
	{
		if(!(te instanceof IImmersiveConnectable))
			throw new IllegalArgumentException("Can't create an IICProxy for a null/non-IIC TileEntity");
		dim = te.getWorld().getDimension().getType();
		pos = Utils.toCC(te);
		//TODO save internal connections!
	}

	public BlockPos getPos()
	{
		return pos;
	}

	public DimensionType getDimension()
	{
		return dim;
	}

	@Override
	public void removeCable(Connection connection)
	{
		//TODO clean up
		//this will load the chunk the TE is in for 1 tick since it needs to be notified about the removed wires
		World w = DimensionManager.getWorld(ServerLifecycleHooks.getCurrentServer(), dim, false, true);
		if(w==null)
		{
			IELogger.warn("Tried to remove a wire in dimension "+dim+" which does not exist");
			return;
		}
		TileEntity te = w.getTileEntity(pos);
		if(!(te instanceof IImmersiveConnectable))
			return;
		((IImmersiveConnectable)te).removeCable(connection);
	}

	@Override
	public boolean canConnect()
	{
		return false;
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset)
	{
		return false;
	}

	@Override
	public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget)
	{
	}

	@Nullable
	@Override
	public ConnectionPoint getTargetedPoint(TargetingInfo info, Vec3i offset)
	{
		return null;
	}

	@Override
	public void onEnergyPassthrough(int amount)
	{
	}

	@Override
	public Vec3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		return null;
	}

	@Override
	public Collection<ConnectionPoint> getConnectionPoints()
	{
		return ImmutableList.of();//TODO do we need this to work properly? Test breakers in unloaded chunks!
	}

	public static IICProxy readFromNBT(CompoundNBT nbt)
	{
		return new IICProxy(DimensionType.byName(new ResourceLocation(nbt.getString("dim"))),
				NBTUtil.readBlockPos(nbt.getCompound("pos")));
	}

	public CompoundNBT writeToNBT()
	{
		CompoundNBT ret = new CompoundNBT();
		ret.putString("dim", dim.getRegistryName().toString());
		ret.put("pos", NBTUtil.writeBlockPos(pos));
		return ret;
	}

	@Override
	public BlockPos getConnectionMaster(WireType cableType, TargetingInfo target)
	{
		return pos;
	}
}