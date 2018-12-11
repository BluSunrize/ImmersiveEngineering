/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemEngineersBlueprint extends ItemIEBase
{
	public ItemEngineersBlueprint()
	{
		super("blueprint", 1);
	}

	@Override
	public String getTranslationKey(ItemStack stack)
	{
		return this.getTranslationKey();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag)
	{
		String key = ItemNBTHelper.getString(stack, "blueprint");
		if(key!=null&&!key.isEmpty()&&BlueprintCraftingRecipe.blueprintCategories.contains(key))
		{
			String formatKey = Lib.DESC_INFO+"blueprint."+key;
			String formatted = I18n.format(formatKey);
			list.add(formatKey.equals(formatted)?key: formatted);
			if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)||Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
			{
				list.add(I18n.format(Lib.DESC_INFO+"blueprint.creates1"));
				BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(key);
				if(recipes.length > 0)
					for(int i = 0; i < recipes.length; i++)
						list.add(" "+recipes[i].output.getDisplayName());
			}
			else
				list.add(I18n.format(Lib.DESC_INFO+"blueprint.creates0"));
		}
	}


	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list)
	{
		if(this.isInCreativeTab(tab))
			for(String key : BlueprintCraftingRecipe.blueprintCategories)
			{
				ItemStack stack = new ItemStack(this);
				ItemNBTHelper.setString(stack, "blueprint", key);
				list.add(stack);
			}
	}

	@Nonnull
	public BlueprintCraftingRecipe[] getRecipes(ItemStack stack)
	{
		return BlueprintCraftingRecipe.findRecipes(ItemNBTHelper.getString(stack, "blueprint"));
	}
}