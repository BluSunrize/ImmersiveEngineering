package blusunrize.immersiveengineering.common.crafting;

import java.util.ArrayList;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.Utils;

public class RecipeOreCrushing implements IRecipe
{
	String oreName;

	public RecipeOreCrushing(String oreName)
	{
		this.oreName=oreName;
	}

	@Override
	public boolean matches(InventoryCrafting inv, World world)
	{
		ItemStack hammer = null;
		ArrayList<ItemStack> ores = new ArrayList();
		for(int i=0;i<inv.getSizeInventory();i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(stackInSlot!=null)
			{
				if(hammer==null && stackInSlot.getItem().getToolClasses(stackInSlot).contains(Lib.TOOL_HAMMER))
					hammer = stackInSlot;
				else if(Utils.compareToOreName(stackInSlot, "ore"+oreName) || Utils.compareToOreName(stackInSlot, "ingot"+oreName))
					ores.add(stackInSlot);
				else
					return false;
			}
		}
		return hammer!=null&&!ores.isEmpty();
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv)
	{
		int amount = 0;
		for(int i=0;i<inv.getSizeInventory();i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(stackInSlot!=null)
			{
				if(Utils.compareToOreName(stackInSlot, "ore"+oreName))
					amount+=2;
				else if(Utils.compareToOreName(stackInSlot, "ingot"+oreName))
					amount++;
			}
		}
		return Utils.copyStackWithAmount(getRecipeOutput(),amount);
	}

	@Override
	public int getRecipeSize()
	{
		return 10;
	}
	@Override
	public ItemStack getRecipeOutput()
	{
		return OreDictionary.getOres("dust"+oreName).isEmpty()?null:OreDictionary.getOres("dust"+oreName).get(0);
	}

}
