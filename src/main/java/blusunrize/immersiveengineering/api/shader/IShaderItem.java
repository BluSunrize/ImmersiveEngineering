package blusunrize.immersiveengineering.api.shader;

import net.minecraft.item.ItemStack;

public interface IShaderItem
{
	ShaderCase getShaderCase(ItemStack shader, ItemStack item, String shaderType);

	String getShaderName(ItemStack shader);
}