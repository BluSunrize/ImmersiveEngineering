package blusunrize.immersiveengineering.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface ValkeryienSkiesUtilsInterface {
    Vec3 getRealWorldPosition(Level level, BlockPos blockPos);
    BlockPos getRealWorldBlockPosition(Level level, BlockPos blockPos);
}