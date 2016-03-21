package blusunrize.immersiveengineering.api.energy;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.Utils;

public class IICProxy implements IImmersiveConnectable
{
	private boolean canEnergyPass;
	private int dim;
	private ChunkCoordinates cc;
	public IICProxy(boolean allowPass, int dimension, ChunkCoordinates pos)
	{
		canEnergyPass = allowPass;
		dim = dimension;
		cc = pos;
	}
	public IICProxy(TileEntity te)
	{
		if (!(te instanceof IImmersiveConnectable))
			throw new IllegalArgumentException("Can't create an IICProxy for a null/non-IIC TileEntity");
		dim = te.getWorldObj().provider.dimensionId;
		canEnergyPass = ((IImmersiveConnectable)te).allowEnergyToPass(null);
		cc = Utils.toCC(te);
	}
	@Override
	public boolean allowEnergyToPass(Connection c)
	{
		return canEnergyPass;
	}
	public ChunkCoordinates getPos()
	{
		return cc;
	}
	public int getDimension()
	{
		return dim;
	}
	@Override
	public void removeCable(Connection connection)
	{
		//this will load the chunk the TE is in for 1 tick since it needs to be notified about removed wires
		World w = MinecraftServer.getServer().worldServerForDimension(dim);
		if (w==null)
		{
			IELogger.warn("Tried to remove a wire in dimension "+dim+" which does not exist");
			return;
		}
		TileEntity te = w.getTileEntity(cc.posX, cc.posY, cc.posZ);
		if (!(te instanceof IImmersiveConnectable))
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
	public boolean canConnectCable(WireType cableType, TargetingInfo target)
	{
		return false;
	}
	@Override
	public void connectCable(WireType cableType, TargetingInfo target)
	{}
	@Override
	public WireType getCableLimiter(TargetingInfo target)
	{
		return null;
	}
	@Override
	public void onEnergyPassthrough(int amount)
	{}
	@Override
	public Vec3 getRaytraceOffset(IImmersiveConnectable link)
	{
		return null;
	}
	@Override
	public Vec3 getConnectionOffset(Connection con)
	{
		return null;
	}
	public static IICProxy readFromNBT(NBTTagCompound nbt)
	{
		return new IICProxy(nbt.getBoolean("pass"), nbt.getInteger("dim"), new ChunkCoordinates(nbt.getInteger("x"), nbt.getInteger("y"), nbt.getInteger("z")));
	}
	public NBTTagCompound writeToNBT()
	{
		NBTTagCompound ret = new NBTTagCompound();
		ret.setInteger("dim", dim);
		ret.setInteger("x", cc.posX);
		ret.setInteger("y", cc.posY);
		ret.setInteger("z", cc.posZ);
		ret.setBoolean("pass", canEnergyPass);
		return ret;
	}
}
