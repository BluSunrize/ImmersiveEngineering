package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.List;

@ZenClass("mods.immersiveengineering.AlloySmelter")
public class AlloySmelter
{
    @ZenMethod
    public static void addRecipe(IItemStack output, IIngredient first, IIngredient second, int time)
    {
        Object oFirst = CraftTweakerHelper.toObject(first), oSecond = CraftTweakerHelper.toObject(second);
        if(oFirst == null || oSecond == null)
            return;

        AlloyRecipe r = new AlloyRecipe(CraftTweakerHelper.toStack(output), oFirst, oSecond, time);
        CraftTweakerAPI.apply(new Add(r));
    }

    private static class Add implements IUndoableAction
    {
        private final AlloyRecipe recipe;

        public Add(AlloyRecipe recipe)
        {
            this.recipe = recipe;
        }

        @Override
        public void apply()
        {
            AlloyRecipe.recipeList.add(recipe);
            IECompatModule.jeiAddFunc.accept(recipe);
        }

        @Override
        public boolean canUndo()
        {
            return true;
        }

        @Override
        public void undo()
        {
            AlloyRecipe.recipeList.remove(recipe);
            IECompatModule.jeiRemoveFunc.accept(recipe);
        }

        @Override
        public String describe()
        {
            return "Adding Alloy Smelter Recipe for " + recipe.output.getDisplayName();
        }

        @Override
        public String describeUndo()
        {
            return "Removing Alloy Smelter Recipe for " + recipe.output.getDisplayName();
        }

        @Override
        public Object getOverrideKey()
        {
            return null;
        }
    }

    @ZenMethod
    public static void removeRecipe(IItemStack output)
    {
        CraftTweakerAPI.apply(new Remove(CraftTweakerHelper.toStack(output)));
    }

    private static class Remove implements IUndoableAction
    {
        private final ItemStack output;
        List<AlloyRecipe> removedRecipes;

        public Remove(ItemStack output)
        {
            this.output = output;
        }

        @Override
        public void apply()
        {
            removedRecipes = AlloyRecipe.removeRecipes(output);
            for(AlloyRecipe recipe : removedRecipes)
                IECompatModule.jeiRemoveFunc.accept(recipe);
        }

        @Override
        public void undo()
        {
            if(removedRecipes != null)
                for(AlloyRecipe recipe : removedRecipes)
                    if(recipe != null)
                    {
                        AlloyRecipe.recipeList.add(recipe);
                        IECompatModule.jeiAddFunc.accept(recipe);
                    }
        }

        @Override
        public String describe()
        {
            return "Removing Alloy Smelter Recipe for " + output.getDisplayName();
        }

        @Override
        public String describeUndo()
        {
            return "Re-Adding Alloy Smelter Recipe for " + output.getDisplayName();
        }

        @Override
        public Object getOverrideKey()
        {
            return null;
        }

        @Override
        public boolean canUndo()
        {
            return true;
        }
    }
}
