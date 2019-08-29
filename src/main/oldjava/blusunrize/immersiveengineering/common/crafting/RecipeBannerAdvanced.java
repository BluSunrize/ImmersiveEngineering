/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.BannerAddPatternRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.RecipeSerializers;
import net.minecraft.item.crafting.RecipeSerializers.SimpleSerializer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;

/**
 * @author BluSunrize - 18.09.2016
 */
public class RecipeBannerAdvanced extends BannerAddPatternRecipe
{
	public static LinkedHashMap<BannerPattern, RecipeReference> advancedPatterns = new LinkedHashMap<>();

	public static void addAdvancedPatternRecipe(BannerPattern pattern, IngredientStack stack, int... offset)
	{
		if(offset!=null&&offset.length > 0)
		{
			int[] slotOffset = new int[2];
			slotOffset[0] = offset[0];
			if(offset.length > 1)
				slotOffset[1] = offset[1];
			advancedPatterns.put(pattern, new RecipeReference(stack, slotOffset));
		}
		else
			advancedPatterns.put(pattern, new RecipeReference(stack));

	}

	public RecipeBannerAdvanced(ResourceLocation id)
	{
		super(id);
	}

	@Override
	@Nullable
	public BannerPattern func_201838_c(IInventory invCrafting)
	{
		for(BannerPattern pattern : BannerPattern.values())
			if(advancedPatterns.containsKey(pattern))
			{
				RecipeReference ref = advancedPatterns.get(pattern);
				boolean matchesPattern = true;
				boolean hasIngr = false;
				boolean hasColour = false;

				for(int i = 0; i < invCrafting.getSizeInventory(); i++)
				{
					ItemStack itemstack = invCrafting.getStackInSlot(i);
					if(!itemstack.isEmpty()&&!itemstack.getItem().isIn(ItemTags.BANNERS))
					{
						if(Utils.isDye(itemstack))
						{
							if(hasColour)
							{
								matchesPattern = false;
								break;
							}
							hasColour = true;
						}
						else
						{
							if(hasIngr||!ref.ingredient.matchesItemStack(itemstack))
							{
								matchesPattern = false;
								break;
							}
							if(ref.offsetToBanner[0]!=0||ref.offsetToBanner[1]!=0)
							{
								int w = invCrafting.getWidth();
								int h = invCrafting.getHeight();
								int bannerSlot = i-ref.offsetToBanner[0]-ref.offsetToBanner[1]*w;
								if((i%w==0&&ref.offsetToBanner[0] > 0)//banner needs to be left, but ingr is in leftmost column
										||(i%w==w-1&&ref.offsetToBanner[0] < 0)//banner needs to be right but ingr is in rightmost column
										||(i/h==0&&ref.offsetToBanner[1] > 0)//banner needs to be above but ingr is in topmost row
										||(i/h==h-1&&ref.offsetToBanner[1] < 0)//banner needs to be below but ingr is in bottommost row
										||(bannerSlot < 0||bannerSlot >= invCrafting.getSizeInventory()))//bannerslot is outside grid
								{
									matchesPattern = false;
									break;
								}
								ItemStack bannerCheck = invCrafting.getStackInSlot(bannerSlot);
								if(bannerCheck.isEmpty()||!bannerCheck.getItem().isIn(ItemTags.BANNERS))
								{
									matchesPattern = false;
									break;
								}
							}
							hasIngr = true;
						}
					}
				}
				if(!hasIngr)
					matchesPattern = false;
				if(matchesPattern)
					return pattern;
			}
		return null;
	}

	private static final IRecipeSerializer<RecipeBannerAdvanced> SERIALIZER = RecipeSerializers.register(
			new SimpleSerializer<>(ImmersiveEngineering.MODID+":adv_banner", RecipeBannerAdvanced::new)
	);

	@Nonnull
	@Override
	public IRecipeSerializer<?> getSerializer()
	{
		return SERIALIZER;
	}

	private static class RecipeReference
	{
		IngredientStack ingredient;
		int[] offsetToBanner = new int[2];

		public RecipeReference(IngredientStack ingredient)
		{
			this.ingredient = ingredient;
		}

		public RecipeReference(IngredientStack ingredient, int[] offsetToBanner)
		{
			this.ingredient = ingredient;
			this.offsetToBanner = offsetToBanner;
		}
	}
}