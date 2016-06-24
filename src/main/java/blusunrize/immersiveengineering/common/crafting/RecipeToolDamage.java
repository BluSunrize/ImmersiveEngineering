package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class RecipeToolDamage extends ShapelessOreRecipe
{
	final IngredientStack tool;

	public RecipeToolDamage(ItemStack result, int toolIndex, Object... recipe)
	{
		super(result, recipe);
		this.tool = ApiUtils.createIngredientStack(recipe[toolIndex], true);
	}

	@Override
	public ItemStack[] getRemainingItems(InventoryCrafting inv)
	{
		ItemStack[] originalRemains = super.getRemainingItems(inv);
		if(tool!=null)
			for(int i=0; i<originalRemains.length; i++)
				if(originalRemains[i]==null && inv.getStackInSlot(i)!=null && tool.matchesItemStack(inv.getStackInSlot(i)))
				{
					ItemStack damaged = inv.getStackInSlot(i).copy();
					damaged.setItemDamage(damaged.getItemDamage()+1);
					if(damaged.getItemDamage()<=damaged.getMaxDamage())
						originalRemains[i] = damaged;
				}
				else if(originalRemains[i]!=null && tool.matchesItemStack(originalRemains[i]))
				{
					originalRemains[i].setItemDamage(originalRemains[i].getItemDamage()+1);
					if(originalRemains[i].getItemDamage()>originalRemains[i].getMaxDamage())
						originalRemains[i] = null;
				}
		return originalRemains;
	}
}