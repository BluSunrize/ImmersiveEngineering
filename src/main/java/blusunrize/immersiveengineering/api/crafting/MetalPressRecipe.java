package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.ComparableItemStack;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author BluSunrize - 07.01.2016
 *
 * The recipe for the metal press
 */
public class MetalPressRecipe extends MultiblockRecipe
{
	public static float energyModifier = 1;
	public static float timeModifier = 1;
	
	public final IngredientStack input;
	public final ComparableItemStack mold;
	public final ItemStack output;
	public MetalPressRecipe(ItemStack output, Object input, ItemStack mold, int energy)
	{
		this.output = output;
		this.input = ApiUtils.createIngredientStack(input);
		this.mold = ApiUtils.createComparableItemStack(mold);
		this.totalProcessEnergy = (int)Math.floor(energy*energyModifier);
		this.totalProcessTime = (int)Math.floor(120*timeModifier);

		this.inputList = Lists.newArrayList(this.input);
		this.outputList = Lists.newArrayList(this.output);
	}
	public MetalPressRecipe setInputSize(int size)
	{
		this.input.inputSize = size;
		return this;
	}
	@Override
	public void setupJEI()
	{
		super.setupJEI();
		this.jeiItemInputList = new ArrayList[2];
		this.jeiItemInputList[0] =Lists.newArrayList(jeiTotalItemInputList);
		this.jeiItemInputList[1] = Lists.newArrayList(mold.stack);
		this.jeiTotalItemInputList.add(mold.stack);
	}
	
	public static ArrayListMultimap<ComparableItemStack, MetalPressRecipe> recipeList = ArrayListMultimap.create();
	public static MetalPressRecipe addRecipe(ItemStack output, Object input, ItemStack mold, int energy)
	{
		MetalPressRecipe r = new MetalPressRecipe(output, input, mold, energy);
		recipeList.put(r.mold, r);
		return r;
	}
	public static MetalPressRecipe findRecipe(ItemStack mold, ItemStack input, boolean checkStackSize)
	{
		if(mold==null || input==null)
			return null;
		ComparableItemStack comp = ApiUtils.createComparableItemStack(mold);
		List<MetalPressRecipe> list = recipeList.get(comp);
		for(MetalPressRecipe recipe : list)
			if(recipe.input.matches(input))
				return recipe;
		return null;
	}
	public static List<MetalPressRecipe> removeRecipes(ItemStack output)
	{
		List<MetalPressRecipe> list = new ArrayList();
		for(ComparableItemStack mold : recipeList.keySet())
		{
			Iterator<MetalPressRecipe> it = recipeList.get(mold).iterator();
			while(it.hasNext())
			{
				MetalPressRecipe ir = it.next();
				if(OreDictionary.itemMatches(ir.output, output, true))
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
		if(itemStack==null)
			return false;
		return recipeList.containsKey(ApiUtils.createComparableItemStack(itemStack));
	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 0;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		nbt.setTag("input", input.writeToNBT(new NBTTagCompound()));
		nbt.setTag("mold", mold.writeToNBT(new NBTTagCompound()));
		return nbt;
	}
	public static MetalPressRecipe loadFromNBT(NBTTagCompound nbt)
	{
		IngredientStack input = IngredientStack.readFromNBT(nbt.getCompoundTag("input"));
		ComparableItemStack mold = ComparableItemStack.readFromNBT(nbt.getCompoundTag("mold"));
		List<MetalPressRecipe> list = recipeList.get(mold);
		for(MetalPressRecipe recipe : list)
			if(recipe.input.equals(input))
				return recipe;
		return null;
	}
}