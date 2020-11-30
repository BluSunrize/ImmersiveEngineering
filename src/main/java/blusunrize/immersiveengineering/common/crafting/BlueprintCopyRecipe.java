package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;

public class BlueprintCopyRecipe extends ShapedRecipe
{
	public static RegistryObject<IERecipeSerializer<BlueprintCopyRecipe>> SERIALIZER;

	public BlueprintCopyRecipe(ResourceLocation id, String group, int recipeWidth, int recipeHeight, NonNullList<Ingredient> recipeItems)
	{
		super(id, group, recipeWidth, recipeHeight, recipeItems, new ItemStack(Misc.blueprint));
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(@Nonnull CraftingInventory inv)
	{
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stack = inv.getStackInSlot(i);
			if (stack.getItem() == Misc.blueprint)
				return stack.copy();
		}
		return ItemStack.EMPTY;
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(@Nonnull CraftingInventory inv)
	{
		NonNullList<ItemStack> items = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);

		for(int i = 0; i < items.size(); ++i)
		{
			ItemStack item = inv.getStackInSlot(i);
			if (item.hasContainerItem())
				items.set(i, item.getContainerItem());
			else if (item.getItem() == Misc.blueprint)
				items.set(i, item.copy());
		}
		return items;
	}
}
