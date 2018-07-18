/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy.wires;

import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class IICProxy implements IImmersiveConnectable
{
	private boolean canEnergyPass;
	private int dim;
	private BlockPos pos;

	public IICProxy(boolean allowPass, int dimension, BlockPos _pos)
	{
		canEnergyPass = allowPass;
		dim = dimension;
		pos = _pos;
	}

	public IICProxy(TileEntity te)
	{
		if(!(te instanceof IImmersiveConnectable))
			throw new IllegalArgumentException("Can't create an IICProxy for a null/non-IIC TileEntity");
		dim = te.getWorld().provider.getDimension();
		canEnergyPass = ((IImmersiveConnectable)te).allowEnergyToPass(null);
		pos = Utils.toCC(te);
	}

	@Override
	public boolean allowEnergyToPass(Connection c)
	{
		return canEnergyPass;
	}

	public BlockPos getPos()
	{
		return pos;
	}

	public int getDimension()
	{
		return dim;
	}

	@Override
	public void removeCable(Connection connection)
	{
		//this will load the chunk the TE is in for 1 tick since it needs to be notified about the removed wires
		World w = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dim);
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
	public boolean isEnergyOutput()
	{
		return false;
	}

	@Override
	public int outputEnergy(int amount, boolean simulate, int energyType)
	{
		return 0;
	}

	@Override
	public boolean canConnectCable(WireType cableType, TargetingInfo target, Vec3i offset)
	{
		return false;
	}

	@Override
	public void connectCable(WireType cableType, TargetingInfo target, IImmersiveConnectable other)
	{
	}

	@Override
	public WireType getCableLimiter(TargetingInfo target)
	{
		return null;
	}

	@Override
	public void onEnergyPassthrough(int amount)
	{
	}

	@Override
	public Vec3d getConnectionOffset(Connection con)
	{
		return null;
	}

	public static IICProxy readFromNBT(NBTTagCompound nbt)
	{
		return new IICProxy(nbt.getBoolean("pass"), nbt.getInteger("dim"), new BlockPos(nbt.getInteger("x"), nbt.getInteger("y"), nbt.getInteger("z")));
	}

	public NBTTagCompound writeToNBT()
	{
		NBTTagCompound ret = new NBTTagCompound();
		ret.setInteger("dim", dim);
		ret.setInteger("x", pos.getX());
		ret.setInteger("y", pos.getY());
		ret.setInteger("z", pos.getZ());
		ret.setBoolean("pass", canEnergyPass);
		return ret;
	}

	@Override
	public BlockPos getConnectionMaster(WireType cableType, TargetingInfo target)
	{
		return pos;
	}
}