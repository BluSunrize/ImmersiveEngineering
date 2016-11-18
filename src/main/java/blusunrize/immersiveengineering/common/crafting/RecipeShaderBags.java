package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class RecipeShaderBags implements IRecipe
{
	@Override
	public boolean matches(InventoryCrafting inv, World world)
	{
		ItemStack stack = null;
		for(int i=0;i<inv.getSizeInventory();i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(stackInSlot!=null)
				if(stack==null)
				{
					if(IEContent.itemShaderBag.equals(stackInSlot.getItem()) && ItemNBTHelper.hasKey(stackInSlot, "rarity"))
						stack = stackInSlot;
//					if(IEContent.itemShader.equals(stackInSlot.getItem()) && ItemNBTHelper.hasKey(stackInSlot, "shader_name"))
//						stack = stackInSlot;
				}
				else
					return false;
		}
		return stack!=null;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv)
	{
		for(int i=0;i<inv.getSizeInventory();i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(stackInSlot!=null)
			{
				ItemStack output = new ItemStack(IEContent.itemShaderBag,IEContent.itemShaderBag.equals(stackInSlot.getItem())?2:1);
				EnumRarity next = ShaderRegistry.getLowerRarity(stackInSlot.getRarity());
				if(next!=null)
				{
					ItemNBTHelper.setString(output, "rarity", next.toString());
					return output;
				}
			}
		}
		return null;
	}

	@Override
	public int getRecipeSize()
	{
		return 10;
	}
	@Override
	public ItemStack getRecipeOutput()
	{
		return new ItemStack(IEContent.itemShaderBag,2);
	}

    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting inv)
    {
        return ForgeHooks.defaultRecipeGetRemainingItems(inv);
    }
}