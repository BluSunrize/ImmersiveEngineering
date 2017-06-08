package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RecipeShapelessIngredient extends ShapelessOreRecipe
{
	static IngredientStack[] tempIngredients;

	List<IngredientStack> ingredients;
	int nbtCopyTargetSlot = -1;
	int toolDamageSlot = -1;

	public RecipeShapelessIngredient(ItemStack result, Object... recipe)
	{
		super(result, saveIngredients(recipe));
		setIngredients(tempIngredients);
		tempIngredients = null;
	}

	public RecipeShapelessIngredient setIngredients(IngredientStack[] ingr)
	{
		ingredients = new ArrayList();
		input.clear();
		for(IngredientStack stack : ingr)
			if(stack != null)
			{
				ingredients.add(stack);
				input.add(stack.getShapedRecipeInput());
			}
		return this;
	}

	public List<IngredientStack> getIngredients()
	{
		return ingredients;
	}

	public RecipeShapelessIngredient setNBTCopyTargetRecipe(int slot)
	{
		this.nbtCopyTargetSlot = slot;
		return this;
	}

	public RecipeShapelessIngredient setToolDamageRecipe(int slot)
	{
		this.toolDamageSlot = slot;
		return this;
	}

	public static Object[] saveIngredients(Object... recipe)
	{
		Object[] converted = new Object[recipe.length];
		tempIngredients = new IngredientStack[recipe.length];
		for(int i = 0; i < recipe.length; i++)
		{
			IngredientStack ingr = ApiUtils.createIngredientStack(recipe[i]);
			if(ingr != null)
			{
				tempIngredients[i] = ingr;
				converted[i] = Blocks.FIRE;
			}
		}
		return converted;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting matrix)
	{
		if(nbtCopyTargetSlot >= 0 && nbtCopyTargetSlot < ingredients.size())
			for(int i = 0; i < matrix.getSizeInventory(); i++)
			{
				ItemStack slot = matrix.getStackInSlot(i);
				if(ingredients.get(nbtCopyTargetSlot).matchesItemStack(slot))
				{
					ItemStack out = output.copy();
					if(!matrix.getStackInSlot(nbtCopyTargetSlot).isEmpty() && matrix.getStackInSlot(nbtCopyTargetSlot).hasTagCompound())
						out.setTagCompound(matrix.getStackInSlot(nbtCopyTargetSlot).getTagCompound().copy());
					return out;
				}
			}
		return super.getCraftingResult(matrix);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv)
	{
		NonNullList<ItemStack>  remains = super.getRemainingItems(inv);
		for(int i = 0; i < remains.size(); i++)
		{
			ItemStack s = inv.getStackInSlot(i);
			ItemStack remain = remains.get(i);
			if(toolDamageSlot >= 0 && toolDamageSlot < ingredients.size())
			{
				ItemStack tool = ItemStack.EMPTY;
				if(remain.isEmpty() && !s.isEmpty() && ingredients.get(toolDamageSlot).matchesItemStack(s))
					tool = s.copy();
				else if(!remain.isEmpty() && ingredients.get(toolDamageSlot).matchesItemStack(remain))
					tool = remain;
				if(!tool.isEmpty() && tool.getItem().isDamageable())
				{
					tool.setItemDamage(tool.getItemDamage() + 1);
					if(tool.getItemDamage() > tool.getMaxDamage())
						tool = ItemStack.EMPTY;
					remains.set(i, tool);
				}
			}
			if(!s.isEmpty() && remain.isEmpty() && s.getItem() instanceof UniversalBucket)
			{
				ItemStack empty = ((UniversalBucket) s.getItem()).getEmpty();
				if(!empty.isEmpty())
					remains.set(i, empty.copy());
			}
		}
		return remains;
	}

	@Override
	public boolean matches(InventoryCrafting matrix, World world)
	{
		ArrayList<IngredientStack> required = new ArrayList(ingredients);

		for(int i = 0; i < matrix.getSizeInventory(); i++)
		{
			ItemStack slot = matrix.getStackInSlot(i);
			if(!slot.isEmpty())
			{
				boolean inRecipe = false;
				Iterator<IngredientStack> iterator = required.iterator();
				while(iterator.hasNext())
				{
					IngredientStack next = iterator.next();
					if(next.matchesItemStack(slot))
					{
						inRecipe = true;
						iterator.remove();
						break;
					}
				}
				if(!inRecipe)
					return false;
			}
		}
		return required.isEmpty();
	}
}