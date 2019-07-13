package blusunrize.immersiveengineering.common.util.compat112.opencomputers;

import blusunrize.immersiveengineering.common.blocks.metal.FloodlightTileEntity;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FloodlightDriver extends DriverSidedTileEntity
{

	@Override
	public ManagedEnvironment createEnvironment(World w, BlockPos bp, Direction facing)
	{
		TileEntity te = w.getTileEntity(bp);
		if(te instanceof FloodlightTileEntity)
			return new FermenterEnvironment(w, bp);
		return null;
	}

	@Override
	public Class<?> getTileEntityClass()
	{
		return FloodlightTileEntity.class;
	}


	public class FermenterEnvironment extends ManagedEnvironmentIE<FloodlightTileEntity>
	{

		public FermenterEnvironment(World w, BlockPos bp)
		{
			super(w, bp, FloodlightTileEntity.class);
		}

		@Override
		public String preferredName()
		{
			return "ie_floodlight";
		}

		@Override
		public int priority()
		{
			return 1000;
		}

		@Override
		public void onConnect(Node node)
		{
			FloodlightTileEntity te = getTileEntity();
			te.controllingComputers++;
			te.computerOn = true;
		}


		@Override
		public void onDisconnect(Node node)
		{
			FloodlightTileEntity te = getTileEntity();
			if(te!=null)
				te.controllingComputers--;
		}

		@Callback(doc = "function():int -- gets the maximum amount of energy stored")
		public Object[] getMaxEnergyStored(Context context, Arguments args)
		{
			return new Object[]{80};
		}

		@Callback(doc = "function():int -- gets the amount of energy stored")
		public Object[] getEnergyStored(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().energyStorage};
		}

		@Callback(doc = "function(up:boolean) -- turns the floodlight")
		public Object[] turnAroundXZ(Context context, Arguments args)
		{
			boolean up = args.checkBoolean(0);
			getTileEntity().turnX(up, true);
			return null;
		}

		@Callback(doc = "function(direction:boolean) -- turns the floodlight")
		public Object[] turnAroundY(Context context, Arguments args)
		{
			boolean dir = args.checkBoolean(0);
			getTileEntity().turnY(dir, true);
			return null;
		}

		@Callback(doc = "function():boolean -- checks whether the floodlight can turn again yet")
		public Object[] canTurn(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().canComputerTurn()};
		}

		@Callback(doc = "function(on:boolean) -- turns the floodlight on and off")
		public Object[] setEnabled(Context context, Arguments args)
		{
			getTileEntity().computerOn = args.checkBoolean(0);
			return null;
		}

		@Callback(doc = "function():boolean -- checks whether the floodlight is on")
		public Object[] isActive(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().active};
		}

		@Callback(direct = false, doc = "function():nil -- waits until the floodlightn can turn again")
		public Object[] waitForCooldown(Context context, Arguments args)
		{
			FloodlightTileEntity te = getTileEntity();
			if(te.turnCooldown > 0)
				context.pause(te.turnCooldown/20F);
			return null;
		}


	}
}
