package blusunrize.immersiveengineering.api.tool;

import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public interface IShaderItem
{
	public boolean canEquipOnItem(ItemStack shader, ItemStack item);
	
	public int getPasses(ItemStack shader, ItemStack item, String modelPart);
	
	public IIcon getReplacementIcon(ItemStack shader, ItemStack item, String modelPart, int pass);
	
	public int[] getRGBAColourModifier(ItemStack shader, ItemStack item, String modelPart, int pass);

	public void modifyRender(ItemStack shader, ItemStack item, String modelPart, int pass, boolean pre);

}