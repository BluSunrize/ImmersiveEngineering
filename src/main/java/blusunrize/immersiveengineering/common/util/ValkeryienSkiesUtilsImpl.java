package blusunrize.immersiveengineering.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class ValkeryienSkiesUtilsImpl implements ValkeryienSkiesUtilsInterface {
    public Vec3 getRealWorldPosition(Level level, BlockPos blockPos) {
        Ship ship = VSGameUtilsKt.getShipObjectManagingPos(level, blockPos);
        if (ship != null) {
          Vector3d oldPosJoml = VectorConversionsMCKt.toJOMLD(blockPos);
          Vector3d newPosJoml = ship.getTransform().getShipToWorld().transformPosition(oldPosJoml);
          Vec3 newPos = VectorConversionsMCKt.toMinecraft(newPosJoml);

          return newPos;
        }

        return Vec3.atCenterOf(blockPos);
    }

    @Override
    public BlockPos getRealWorldBlockPosition(Level level, BlockPos blockPos)
    {
        Ship ship = VSGameUtilsKt.getShipObjectManagingPos(level, blockPos);
        if (ship != null) {
          Vector3d oldPosJoml = VectorConversionsMCKt.toJOMLD(blockPos);
          Vector3d newPosJoml = ship.getTransform().getShipToWorld().transformPosition(oldPosJoml);
          Vec3 newPos = VectorConversionsMCKt.toMinecraft(newPosJoml);

          return BlockPos.containing(newPos);
        }

        return blockPos;
    }
}