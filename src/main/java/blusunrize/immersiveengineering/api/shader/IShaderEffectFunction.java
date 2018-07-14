package blusunrize.immersiveengineering.api.shader;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author BluSunrize - 14.05.2018
 */
@FunctionalInterface
public interface IShaderEffectFunction
{
	void execute(@Nonnull World world, @Nonnull ItemStack shader, @Nullable ItemStack item, @Nonnull String shaderType, @Nonnull Vec3d pos, @Nullable Vec3d direction, float scale);
}
