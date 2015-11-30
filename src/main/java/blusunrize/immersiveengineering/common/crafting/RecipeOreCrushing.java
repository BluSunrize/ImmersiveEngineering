package blusunrize.immersiveengineering.common.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.Utils;

public class RecipeOreCrushing implements IRecipe
{
	String oreName;
	ItemStack dust;

	public RecipeOreCrushing(String oreName, ItemStack dust)
	{
		this.oreName=oreName;
		this.dust=dust;
	}

	@Override
	public boolean matches(InventoryCrafting inv, World world)
	{
		ItemStack hammer = null;
		ItemStack ore = null;
		for(int i=0;i<inv.getSizeInventory();i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(stackInSlot!=null)
			{
				if(hammer==null && stackInSlot.getItem().getToolClasses(stackInSlot).contains(Lib.TOOL_HAMMER))
					hammer = stackInSlot;
				else if(ore==null && Utils.compareToOreName(stackInSlot, "ore"+oreName))
					ore = stackInSlot;
				else
					return false;
			}
		}
		return hammer!=null&&ore!=null;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv)
	{
		return getRecipeOutput();
	}

	@Override
	public int getRecipeSize()
	{
		return 10;
	}
	@Override
	public ItemStack getRecipeOutput()
	{
		return dust!=null?dust.copy():null;
	}

}
