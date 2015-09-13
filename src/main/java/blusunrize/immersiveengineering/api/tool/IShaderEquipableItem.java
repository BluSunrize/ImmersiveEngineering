package blusunrize.immersiveengineering.api.tool;

import net.minecraft.item.ItemStack;

public interface IShaderEquipableItem
{
	public boolean canAcceptShader(ItemStack stack, ItemStack shader);
	public void setShaderItem(ItemStack stack, ItemStack shader);
	public ItemStack getShaderItem(ItemStack stack);
}
