package blusunrize.immersiveengineering.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;

public class ValkeryienSkiesUtils implements ValkeryienSkiesUtilsInterface
{

    private static ValkeryienSkiesUtilsInterface valkeryienSkiesUtils;

    public static ValkeryienSkiesUtilsInterface getInstance()
    {
        if(valkeryienSkiesUtils!=null) return valkeryienSkiesUtils;

        boolean isLoaded = ModList.get().isLoaded("valkyrienskies");

        if(isLoaded)
        {
            valkeryienSkiesUtils = new ValkeryienSkiesUtilsImpl();
        }
        else
        {
            valkeryienSkiesUtils = new ValkeryienSkiesUtils();
        }

        return valkeryienSkiesUtils;
    }


    @Override
    public Vec3 getRealWorldPosition(Level level, BlockPos blockPos)
    {
        return Vec3.atCenterOf(blockPos);
    }

    @Override
    public BlockPos getRealWorldBlockPosition(Level level, BlockPos blockPos)
    {
        return blockPos;
    }
   
}