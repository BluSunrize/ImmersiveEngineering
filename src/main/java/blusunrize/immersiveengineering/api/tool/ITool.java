package blusunrize.immersiveengineering.api.tool;

import net.minecraft.item.ItemStack;

/**
 * Items that implement this will be allowed in the toolbox
 */
public interface ITool
{
	boolean isTool(ItemStack item);
}
