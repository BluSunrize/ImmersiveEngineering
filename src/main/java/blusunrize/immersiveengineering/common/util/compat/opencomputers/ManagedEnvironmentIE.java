package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import li.cil.oc.api.Network;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.ManagedEnvironment;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public abstract class ManagedEnvironmentIE<T extends TileEntityIEBase> extends ManagedEnvironment  implements NamedBlock {
	World w;
	int x, y, z;
	Class<? extends TileEntityIEBase> myClass;
	//teClass and the type parameter HAVE to match
	public ManagedEnvironmentIE(World w, int x, int y, int z, Class<? extends TileEntityIEBase> teClass)
	{
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
		myClass = teClass;
		setNode(Network.newNode(this, Visibility.Network).withComponent(preferredName(), Visibility.Network).create());
	}
	protected T getTileEntity()
	{
		TileEntity te = w.getTileEntity(x, y, z);
		if (te!=null&&te.getClass()==myClass)
			return (T) te;
		return null;
	}
}
