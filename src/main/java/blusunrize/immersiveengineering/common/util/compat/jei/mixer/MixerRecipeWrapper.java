package blusunrize.immersiveengineering.common.util.compat.jei.mixer;

import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.common.crafting.MixerRecipePotion;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.MultiblockRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

public class MixerRecipeWrapper extends MultiblockRecipeWrapper
{
	public MixerRecipeWrapper(MixerRecipe recipe)
	{
		super(recipe);
		if(recipe instanceof MixerRecipePotion)
		{
			recipeInputs = new List[]{new ArrayList()};
			fluidOutputs.clear();
			for(PotionHelper.MixPredicate<PotionType> mixPredicate : PotionHelper.POTION_TYPE_CONVERSIONS)
				if(mixPredicate.input==((MixerRecipePotion)recipe).inputPotionType)
					for(ItemStack potionIngred : JEIHelper.modRegistry.getIngredientRegistry().getPotionIngredients())
						if(mixPredicate.reagent.apply(potionIngred))
						{
							recipeInputs[0].add(potionIngred);
							fluidOutputs.add(MixerRecipePotion.getFluidStackForType(mixPredicate.output, recipe.fluidAmount));
						}

		}
	}
}