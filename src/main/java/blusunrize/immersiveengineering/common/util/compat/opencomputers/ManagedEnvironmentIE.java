package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import li.cil.oc.api.Network;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

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
		if(te!=null&&myClass.isAssignableFrom(te.getClass()))
			return (T)te;
		return null;
	}

	public abstract static class ManagedEnvMultiblock<T2 extends TileEntityMultiblockMetal<?, ?>> extends ManagedEnvironmentIE<T2>
	{

		public ManagedEnvMultiblock(World w, BlockPos p, Class<? extends TileEntityIEBase> teClass)
		{
			super(w, p, teClass);
		}

		protected Object[] enableComputerControl(Context context, Arguments args)
		{
			boolean allow = args.checkBoolean(0);
			if(allow)
				getTileEntity().computerOn = Optional.of(true);
			else
				getTileEntity().computerOn = Optional.empty();
			return null;
		}

		protected Object[] setEnabled(Context context, Arguments args)
		{
			boolean enabled = args.checkBoolean(0);
			TileEntityMultiblockMetal<?, ?> te = getTileEntity();
			if(!te.computerOn.isPresent())
				throw new IllegalStateException("Computer control must be enabled to enable or disable the machine");
			te.computerOn = Optional.of(enabled);
			return null;
		}
	}
}
