package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.items.IEItems;
import blusunrize.immersiveengineering.common.items.IEItems.Metals;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OreCrushingRecipe implements ICraftingRecipe
{
	private final ResourceLocation id;

	public OreCrushingRecipe(ResourceLocation id)
	{
		this.id = id;
	}

	@Override
	public boolean matches(@Nonnull CraftingInventory inv, @Nonnull World worldIn)
	{
		return findConversion(inv)!=null;
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(@Nonnull CraftingInventory inv)
	{
		Tag<Item> output = Objects.requireNonNull(findConversion(inv)).getRight();
		if(!output.getAllElements().isEmpty())
			return IEApi.getPreferredTagStack(output.getId());
		else
			return ItemStack.EMPTY;
	}

	@Override
	public boolean canFit(int width, int height)
	{
		return width*height >= 2;
	}

	@Nonnull
	@Override
	public ItemStack getRecipeOutput()
	{
		return new ItemStack(Metals.dusts.get(EnumMetals.COPPER));
	}

	@Nonnull
	@Override
	public ResourceLocation getId()
	{
		return id;
	}

	@Nonnull
	@Override
	public IRecipeSerializer<?> getSerializer()
	{
		return RecipeSerializers.ORE_CRUSHING_SERIALIZER.get();
	}

	public static final List<Pair<Tag<Item>, Tag<Item>>> CRUSHABLE_ORES_WITH_OUTPUT = new ArrayList<>();

	@Nullable
	private Pair<Tag<Item>, Tag<Item>> findConversion(CraftingInventory inv)
	{
		boolean hasHammer = false;
		Pair<Tag<Item>, Tag<Item>> result = null;
		for(int slot = 0; slot < inv.getSizeInventory(); ++slot)
		{
			ItemStack here = inv.getStackInSlot(slot);
			if(!here.isEmpty())
			{
				if(Utils.isHammer(here))
				{
					if(hasHammer)
						return null;
					else
						hasHammer = true;
				}
				else
				{
					if(result==null)
					{
						for(Pair<Tag<Item>, Tag<Item>> candidate : CRUSHABLE_ORES_WITH_OUTPUT)
							if(candidate.getLeft().contains(here.getItem()))
							{
								result = candidate;
								break;
							}
						if(result==null)
							return null;
					}
					else
						return null;
				}
			}
		}
		if(hasHammer)
			return result;
		else
			return null;
	}
}
