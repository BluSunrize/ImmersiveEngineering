package blusunrize.immersiveengineering.api.shader;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * @author BluSunrize - 14.05.2018
 */
@FunctionalInterface
public interface IShaderEffectFunction
{
	void execute(World world, ItemStack shader, ItemStack item, String shaderType, Vec3d pos);
}
