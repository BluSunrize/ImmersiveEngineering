package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBelljar;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BellJarDriver extends DriverSidedTileEntity {

    @Override
    public ManagedEnvironment createEnvironment(World w, BlockPos bp, EnumFacing facing)
    {
        TileEntity te = w.getTileEntity(bp);
        if(te instanceof TileEntityBelljar)
        {
            TileEntityBelljar belljar = (TileEntityBelljar)te;
            return new BelljarEnvironment(w, belljar.getPos());
        }
        return null;
    }

    @Override
    public Class<?> getTileEntityClass()
    {
        return TileEntityBelljar.class;
    }

    public class BelljarEnvironment extends ManagedEnvironmentIE<TileEntityBelljar>
    {
        public BelljarEnvironment(World w, BlockPos bp)
        {
            super(w, bp, TileEntityBelljar.class);
        }

        @Callback(doc = "function():int -- returns the maximum amount of energy stored")
        public Object[] getMaxEnergyStored(Context context, Arguments args)
        {
            return new Object[]{getTileEntity().energyStorage.getMaxEnergyStored()};
        }

        @Callback(doc = "function():int -- returns the amount of energy stored")
        public Object[] getEnergyStored(Context context, Arguments args)
        {
            return new Object[]{getTileEntity().energyStorage.getEnergyStored()};
        }

        @Callback(doc = "function():table -- returns the water tank")
        public Object[] getWater(Context context, Arguments args)
        {
            return new Object[]{getTileEntity().tank.getInfo()};
        }

        @Callback(doc = "function(slot:int):table -- returns the stack in the specified output slot (1 - 4)")
        public Object[] getOutputStack(Context context, Arguments args)
        {
            int slot = args.checkInteger(0);
            if(slot < 1||slot > 4)
                throw new IllegalArgumentException("Output slots are 1-4");
            return new Object[]{getTileEntity().getInventory().get(slot + 2)};
        }

        @Callback(doc = "function():table -- returns the current fertilizer")
        public Object[] getFertilizerStack(Context context, Arguments args)
        {
            return new Object[]{getTileEntity().getInventory().get(TileEntityBelljar.SLOT_FERTILIZER)};
        }

        @Callback(doc = "function():table -- returns the current soil stack")
        public Object[] getSoilStack(Context context, Arguments args)
        {
            return new Object[]{getTileEntity().getInventory().get(TileEntityBelljar.SLOT_SOIL)};
        }

        @Callback(doc = "function():table -- returns the currently planted seed stack")
        public Object[] getSeedStack(Context context, Arguments args)
        {
            return new Object[]{getTileEntity().getInventory().get(TileEntityBelljar.SLOT_SEED)};
        }

        @Callback(doc = "function():boolean -- returns whether the cloche is currently growing")
        public Object[] isGrowing(Context context, Arguments args)
        {
            return new Object[]{getTileEntity().shouldGrow()};
        }

        @Override
        public String preferredName()
        {
            return "ie_belljar";
        }

        @Override
        public int priority()
        {
            return 1000;
        }

    }
}
