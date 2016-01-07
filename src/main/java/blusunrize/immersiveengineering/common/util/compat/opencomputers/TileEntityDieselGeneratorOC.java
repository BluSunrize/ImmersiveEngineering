package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityDieselGenerator;
import cpw.mods.fml.common.Optional;
import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.SidedComponent;
import li.cil.oc.api.network.Visibility;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;


@Optional.InterfaceList({
	@Optional.Interface(iface = "li.cil.oc.api.network.SidedComponent", modid = "OpenComputers"),
	@Optional.Interface(iface = "li.cil.oc.api.network.Environment", modid = "OpenComputers")})
public class TileEntityDieselGeneratorOC extends TileEntityDieselGenerator implements Environment, SidedComponent
{
	private int connectedCount = 0;
	protected Node node;
	public static final String componentName = "IE:dieselGenerator";

	protected boolean addedToNetwork = false;

	@Optional.Method(modid = "OpenComputers")
	@Override
	public Node node()
	{
		return node;
	}

	@Optional.Method(modid = "OpenComputers")
	@Override
	public void onConnect(final Node node)
	{
		if (node!=this.node)
		{
			TileEntityDieselGeneratorOC master = (TileEntityDieselGeneratorOC) master();
			if(master==null)
				return;
			master.computerActivated = true;
			master.computerControlled = true;
			connectedCount++;
		}
	}

	@Optional.Method(modid = "OpenComputers")
	@Override
	public void onDisconnect(final Node node)
	{
		if (node!=this.node)
			connectedCount--;
		if (connectedCount<=0||node==this.node)
		{
			TileEntityDieselGeneratorOC master = (TileEntityDieselGeneratorOC) master();
			if(master==null)
				return;
			master.computerControlled = false;
			if (node==this.node||connectedCount<0)
				connectedCount = 0;
		}
	}

	@Optional.Method(modid = "OpenComputers")
	@Override
	public void onMessage(final Message message)
	{}

	@Optional.Method(modid = "OpenComputers")
	@Override
	public void updateEntity()
	{
		super.updateEntity();
		if (!worldObj.isRemote)
		{
			if (node == null && canConnectNode(ForgeDirection.EAST)) {
				node = Network.newNode(this, Visibility.Network)
						.withComponent(componentName, Visibility.Network).create();
				addedToNetwork = false;
			}
			if (!addedToNetwork && node != null) {
				addedToNetwork = true;
				Network.joinOrCreateNetwork(this);
				//				IELogger.info("Added to network" + node);
			} 
		}
	}

	@Optional.Method(modid = "OpenComputers")
	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		if (node != null)
			node.remove();
	}

	@Optional.Method(modid = "OpenComputers")
	@Override
	public void invalidate()
	{
		super.invalidate();
		if (node != null)
			node.remove();
	}

	@Optional.Method(modid = "OpenComputers")
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		if (node != null && node.host() == this)
			node.load(nbt.getCompoundTag("oc:node"));
	}

	@Optional.Method(modid = "OpenComputers")
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		if (node != null && node.host() == this)
		{
			final NBTTagCompound nodeNbt = new NBTTagCompound();
			node.save(nodeNbt);
			nbt.setTag("oc:node", nodeNbt);
		}
	}

	@Optional.Method(modid = "OpenComputers")
	@Override
	public boolean canConnectNode(ForgeDirection arg0)
	{
		return (pos==21 && !master().mirrored) || (pos==23 && master().mirrored);
	}

	@Optional.Method(modid = "OpenComputers")
	@Override
	public void mirror()
	{
		if (master()==null)
		{
			computerControlled = false;
			int axis;
			ForgeDirection dir = ForgeDirection.getOrientation(facing);
			if (dir==ForgeDirection.EAST||dir==ForgeDirection.WEST)
				axis = 2;
			else
				axis = 0;
			TileEntity te1 = worldObj.getTileEntity(xCoord+(axis==0?1:-dir.offsetX), yCoord, zCoord+(axis==2?1:-dir.offsetZ));
			mirrorCheck(te1);
			te1 = worldObj.getTileEntity(xCoord-(axis==0?1:dir.offsetX), yCoord, zCoord-(axis==2?1:dir.offsetZ));
			mirrorCheck(te1);
		}
	}

	private void mirrorCheck(TileEntity te)
	{
		if (!(te instanceof TileEntityDieselGeneratorOC))
			return;
		TileEntityDieselGeneratorOC gen = (TileEntityDieselGeneratorOC) te;
		if (gen.canConnectNode(ForgeDirection.EAST))
			gen.node = Network.newNode(gen, Visibility.Network).withComponent(componentName, Visibility.Network).create();
		else if (gen.node!=null)
		{
			gen.node.remove();
			gen.node = null;
		}
		gen.addedToNetwork = false;
	}

	@Optional.Method(modid = "OpenComputers")
	@Callback(doc = "function(enable:boolean) -- allow or disallow the generator to run when it can")
	public Object[] setEnabled(Context context, Arguments args)
	{
		master().computerActivated = args.checkBoolean(0);
		return null;
	}

	@Optional.Method(modid = "OpenComputers")
	@Callback(doc = "function():boolean -- get whether the generator is currently producing energy")
	public Object[] isActive(Context context, Arguments args)
	{
		return new Object[]{master().active};
	}

	@Optional.Method(modid = "OpenComputers")
	@Callback(doc = "function():table -- get information about the internal fuel tank")
	public Object[] getTankInfo(Context context, Arguments args)
	{
		return new Object[]{master().tank.getInfo()};
	}
}