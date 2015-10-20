package blusunrize.immersiveengineering.common.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.Utils;

public class RecipeOreCrushing implements IRecipe
{
	String oreName;
	int dustMeta;

	public RecipeOreCrushing(String oreName, int dustMeta)
	{
		this.oreName=oreName;
		this.dustMeta=dustMeta;
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
				else if(ore==null && (Utils.compareToOreName(stackInSlot, "ore"+oreName) || Utils.compareToOreName(stackInSlot, "ingot"+oreName)))
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
		int amount = 0;
		for(int i=0;i<inv.getSizeInventory();i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(stackInSlot!=null)
			{
				if(Utils.compareToOreName(stackInSlot, "ore"+oreName))
					amount=2;
				else if(Utils.compareToOreName(stackInSlot, "ingot"+oreName))
					amount=1;
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
		if(dustMeta>=0)
			return new ItemStack(IEContent.itemMetal,1,dustMeta);
		return OreDictionary.getOres("dust"+oreName).isEmpty()?null:IEApi.getPreferredOreStack("dust"+oreName);
	}

}
