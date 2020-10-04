/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.ComparableItemStack;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.util.ListUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.RegistryObject;

import java.util.*;

/**
 * @author BluSunrize - 07.01.2016
 * <p>
 * The recipe for the metal press
 */
public class MetalPressRecipe extends MultiblockRecipe
{
	public static IRecipeType<MetalPressRecipe> TYPE = IRecipeType.register(Lib.MODID+":metal_press");
	public static RegistryObject<IERecipeSerializer<MetalPressRecipe>> SERIALIZER;

	public IngredientWithSize input;
	public final ComparableItemStack mold;
	public final ItemStack output;

	public MetalPressRecipe(ResourceLocation id, ItemStack output, IngredientWithSize input, ComparableItemStack mold, int energy)
	{
		super(output, TYPE, id);
		this.output = output;
		this.input = input;
		this.mold = mold;
		this.totalProcessEnergy = energy;
		this.totalProcessTime = 60;

		setInputListWithSizes(Lists.newArrayList(this.input));
		this.outputList = ListUtils.fromItem(this.output);
	}

	@Override
	protected IERecipeSerializer<MetalPressRecipe> getIESerializer()
	{
		return SERIALIZER.get();
	}

	public MetalPressRecipe setInputSize(int size)
	{
		this.input = new IngredientWithSize(this.input.getBaseIngredient(), size);
		return this;
	}

	public boolean matches(ItemStack mold, ItemStack input, World world)
	{
		return this.input.test(input);
	}

	public MetalPressRecipe getActualRecipe(ItemStack mold, ItemStack input, World world)
	{
		return this;
	}

	// Initialized by reload listener
	public static Map<ResourceLocation, MetalPressRecipe> recipeList = Collections.emptyMap();
	private static ArrayListMultimap<ComparableItemStack, MetalPressRecipe> recipesByMold = ArrayListMultimap.create();

	public static void updateRecipesByMold()
	{
		recipesByMold = ArrayListMultimap.create();
		recipeList.values().forEach(recipe -> recipesByMold.put(recipe.mold, recipe));
	}

	public static MetalPressRecipe findRecipe(ItemStack mold, ItemStack input, World world)
	{
		if(mold.isEmpty()||input.isEmpty())
			return null;
		ComparableItemStack comp = ComparableItemStack.create(mold, false);
		List<MetalPressRecipe> list = recipesByMold.get(comp);
		for(MetalPressRecipe recipe : list)
			if(recipe.matches(mold, input, world))
				return recipe.getActualRecipe(mold, input, world);
		return null;
	}

	public static List<MetalPressRecipe> removeRecipes(ItemStack output)
	{
		List<MetalPressRecipe> list = new ArrayList<>();
		Set<ComparableItemStack> keySet = new HashSet<>(recipesByMold.keySet());
		for(ComparableItemStack mold : keySet)
		{
			Iterator<MetalPressRecipe> it = recipesByMold.get(mold).iterator();
			while(it.hasNext())
			{
				MetalPressRecipe ir = it.next();
				if(ItemStack.areItemsEqual(ir.output, output))
				{
					list.add(ir);
					it.remove();
				}
			}
		}
		return list;
	}

	public static boolean isValidMold(ItemStack itemStack)
	{
		if(itemStack.isEmpty())
			return false;
		return recipesByMold.containsKey(ComparableItemStack.create(itemStack, false));
	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 0;
	}
}