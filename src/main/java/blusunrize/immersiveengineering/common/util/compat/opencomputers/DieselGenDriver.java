package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityDieselGenerator;
import cpw.mods.fml.common.Optional;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.prefab.DriverTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class DieselGenDriver extends DriverTileEntity
{

	@Override
	public ManagedEnvironment createEnvironment(World w, int x, int y, int z)
	{
		TileEntity te = w.getTileEntity(x, y, z);
		if (te instanceof TileEntityDieselGenerator)
		{
			TileEntityDieselGenerator master = ((TileEntityDieselGenerator)te).master();
			int pos = ((TileEntityDieselGenerator)te).pos;
			if (master!=null&&((pos==21&&!master.mirrored)||(pos==23&&master.mirrored)))
				return new DieselEnvironment(w, master.xCoord, master.yCoord, master.zCoord);
		}
		return null;
	}

	@Override
	public Class<?> getTileEntityClass()
	{
		return TileEntityDieselGenerator.class;
	}


	public class DieselEnvironment extends ManagedEnvironmentIE<TileEntityDieselGenerator>
	{

		public DieselEnvironment(World w, int x, int y, int z)
		{
			super(w, x, y, z, TileEntityDieselGenerator.class);
		}

		@Optional.Method(modid = "OpenComputers")
		@Callback(doc = "function(enable:boolean) -- allow or disallow the generator to run when it can")
		public Object[] setEnabled(Context context, Arguments args)
		{
			getTileEntity().computerActivated = args.checkBoolean(0);
			return null;
		}

		@Optional.Method(modid = "OpenComputers")
		@Callback(doc = "function():boolean -- get whether the generator is currently producing energy")
		public Object[] isActive(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().active};
		}

		@Optional.Method(modid = "OpenComputers")
		@Callback(doc = "function():table -- get information about the internal fuel tank")
		public Object[] getTankInfo(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().tank.getInfo()};
		}

		@Override
		public String preferredName() {
			return "ie_diesel_generator";
		}

		@Override
		public int priority() {
			return 1000;
		}

		@Optional.Method(modid = "OpenComputers")
		// "override" what gets injected by OC's class transformer
		public void onConnect(Node node)
		{
			TileEntityDieselGenerator te = getTileEntity();
			if (te!=null)
			{
				te.computerControlled = true;
				te.computerActivated = true;
			}
		}

		@Optional.Method(modid = "OpenComputers")
		// "override" what gets injected by OC's class transformer
		public void onDisconnect(Node node)
		{
			TileEntityDieselGenerator te = getTileEntity();
			if (te!=null)
				te.computerControlled = false;
		}

	}
}
