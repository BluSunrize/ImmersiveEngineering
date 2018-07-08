/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.items.ItemSpeedloader;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class RecipeSpeeloader extends ShapedOreRecipe
{
	public RecipeSpeeloader()
	{
		super(null, new ItemStack(IEContent.itemSpeedloader), CraftingHelper.parseShaped("BBB", "BSB", "BBB", 'S', IEContent.itemSpeedloader, 'B', BulletHandler.getBulletStack("casull")));
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting matrix)
	{
		ItemStack speedloader = matrix.getStackInSlot(4);

		if(!speedloader.isEmpty()&&speedloader.getItem() instanceof ItemSpeedloader&&((ItemSpeedloader)speedloader.getItem()).isEmpty(speedloader))
		{
			ItemStack out = speedloader.copy();
			NonNullList<ItemStack> fill = NonNullList.withSize(8, ItemStack.EMPTY);
			for(int i = 0; i < 8; i++)
			{
				int j = i >= 4?i+1: i;
				fill.set(i, Utils.copyStackWithAmount(matrix.getStackInSlot(j), 1));
			}
			((ItemSpeedloader)out.getItem()).setContainedItems(out, fill);
			return out;
		}
		else
			return ItemStack.EMPTY;
	}
}