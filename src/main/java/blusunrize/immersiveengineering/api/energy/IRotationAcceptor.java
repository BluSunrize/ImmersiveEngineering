package blusunrize.immersiveengineering.api.energy;

import javax.annotation.Nonnull;

import net.minecraft.util.EnumFacing;

public interface IRotationAcceptor
{
	public void inputRotation(double rotation, @Nonnull EnumFacing side);
}
