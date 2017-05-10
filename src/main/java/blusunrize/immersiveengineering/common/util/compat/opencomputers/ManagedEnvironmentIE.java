package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import li.cil.oc.api.Network;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class ManagedEnvironmentIE<T extends TileEntityIEBase> extends AbstractManagedEnvironment implements NamedBlock
{
	World w;
	BlockPos pos;
	Class<? extends TileEntityIEBase> myClass;

	//teClass and the type parameter HAVE to match
	public ManagedEnvironmentIE(World w, BlockPos p, Class<? extends TileEntityIEBase> teClass)
	{
		this.w = w;
		pos = p;
		myClass = teClass;
		setNode(Network.newNode(this, Visibility.Network).withComponent(preferredName(), Visibility.Network).create());
	}

	@SuppressWarnings("unchecked")
	protected T getTileEntity()
	{
		TileEntity te = w.getTileEntity(pos);
		if(te != null && myClass.isAssignableFrom(te.getClass()))
			return (T) te;
		return null;
	}
}
