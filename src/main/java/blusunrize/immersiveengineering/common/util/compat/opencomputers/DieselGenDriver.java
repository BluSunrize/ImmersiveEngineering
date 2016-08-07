package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityDieselGenerator;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DieselGenDriver extends DriverSidedTileEntity
{
	@Override
	public ManagedEnvironment createEnvironment(World w, BlockPos bp, EnumFacing f)
	{
		TileEntity te = w.getTileEntity(bp);
		if(te instanceof TileEntityDieselGenerator)
		{
			TileEntityDieselGenerator gen = ((TileEntityDieselGenerator) te);
			TileEntityDieselGenerator master = gen.master();
			if(master != null && gen.isRedstonePos())
				return new DieselEnvironment(w, master.getPos());
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

		public DieselEnvironment(World w, BlockPos bp)
		{
			super(w, bp, TileEntityDieselGenerator.class);
		}

		@Callback(doc = "function(enable:boolean) -- allow or disallow the generator to run when it can")
		public Object[] setEnabled(Context context, Arguments args)
		{
			getTileEntity().computerOn = args.checkBoolean(0);
			return null;
		}

		@Callback(doc = "function():boolean -- get whether the generator is currently producing energy")
		public Object[] isActive(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().active};
		}

		@Callback(doc = "function():table -- get information about the internal fuel tank")
		public Object[] getTankInfo(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().tanks[0].getInfo()};
		}

		@Override
		public String preferredName()
		{
			return "ie_diesel_generator";
		}

		@Override
		public int priority()
		{
			return 1000;
		}

		@Override
		public void onConnect(Node node)
		{
			TileEntityDieselGenerator te = getTileEntity();
			if(te != null)
			{
				te.controllingComputers++;
				te.computerOn = true;
			}
		}

		@Override
		public void onDisconnect(Node node)
		{
			TileEntityDieselGenerator te = getTileEntity();
			if(te != null)
				te.controllingComputers--;
		}

	}
}
