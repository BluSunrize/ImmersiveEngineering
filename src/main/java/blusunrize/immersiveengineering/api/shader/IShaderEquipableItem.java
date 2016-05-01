package blusunrize.immersiveengineering.api.shader;

import net.minecraft.item.ItemStack;

/**
 * @author BluSunrize - 29.10.2015
 *
 * To be implemented by items can accept shaders
 */
public interface IShaderEquipableItem
{
	/**
	 * @return a string representing which kind of ShaderCase this item will accept
	 */
	public String getShaderType();
	/**
	 * needs to be integrated with the internal inventory of the item
	 */
	public void setShaderItem(ItemStack stack, ItemStack shader);
	/**
	 * needs to be integrated with the internal inventory of the item
	 */
	public ItemStack getShaderItem(ItemStack stack);
}
