/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.common.crafting.IngredientFluidStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IngredientStack
{
	public ItemStack stack = ItemStack.EMPTY;
	public List<ItemStack> stackList;
	public String oreName;
	public FluidStack fluid;
	public int inputSize = 1;
	public boolean useNBT;

	public IngredientStack(ItemStack stack)
	{
		this.stack = stack;
		this.inputSize = stack.getCount();
	}

	public IngredientStack(String oreName, int inputSize)
	{
		this.oreName = oreName;
		this.inputSize = inputSize;
	}

	public IngredientStack(String oreName)
	{
		this(oreName, 1);
	}

	public IngredientStack(List<ItemStack> stackList, int inputSize)
	{
		this.stackList = stackList;
		this.inputSize = inputSize;
	}

	public IngredientStack(List<ItemStack> stackList)
	{
		this(stackList, 1);
	}

	public IngredientStack(FluidStack fluid)
	{
		this.fluid = fluid;
	}

	public IngredientStack(IngredientStack ingr)
	{
		this.stack = ingr.stack;
		this.stackList = ingr.stackList;
		this.oreName = ingr.oreName;
		this.fluid = ingr.fluid;
		this.inputSize = ingr.inputSize;
		this.useNBT = ingr.useNBT;
	}


	public IngredientStack setUseNBT(boolean useNBT)
	{
		this.useNBT = useNBT;
		return this;
	}

	public boolean matches(Object input)
	{
		if(input==null)
			return false;
		if(input instanceof IngredientStack)
			return this.equals(input)&&this.inputSize <= ((IngredientStack)input).inputSize;
		if(input instanceof ItemStack)
		{
			return matchesItemStack((ItemStack)input);
		}
		else if(input instanceof ItemStack[])
		{
			for(ItemStack iStack : (ItemStack[])input)
				if(matchesItemStack(iStack))
					return true;
		}
		else if(input instanceof List)
		{
			for(Object io : (List)input)
				if(this.matches(io))
					return true;
		}
		else if(input instanceof String)
		{
			if(this.oreName!=null)
				return this.oreName.equals(input);
			return ApiUtils.compareToOreName(stack, (String)input);
		}
		return false;
	}

	public IngredientStack copyWithSize(int size)
	{
		IngredientStack is = new IngredientStack(this);
		is.inputSize = size;
		return is;
	}

	public IngredientStack copyWithMultipliedSize(double multiplier)
	{
		return copyWithSize((int)Math.floor(this.inputSize*multiplier));
	}

	public List<ItemStack> getStackList()
	{
		if(stackList!=null)
			return stackList;
		if(oreName!=null)
			return OreDictionary.getOres(oreName);
		if(fluid!=null&&ForgeModContainer.getInstance().universalBucket!=null)
			return Collections.singletonList(UniversalBucket.getFilledBucket(ForgeModContainer.getInstance().universalBucket, fluid.getFluid()));
		return Collections.singletonList(stack);
	}

	public List<ItemStack> getSizedStackList()
	{
		List<ItemStack> list;
		if(oreName!=null)
		{
			list = new ArrayList<ItemStack>();
			for(ItemStack stack : OreDictionary.getOres(oreName))
				list.add(ApiUtils.copyStackWithAmount(stack, inputSize));
		}
		else if(fluid!=null&&ForgeModContainer.getInstance().universalBucket!=null)
			list = Collections.singletonList(UniversalBucket.getFilledBucket(ForgeModContainer.getInstance().universalBucket, fluid.getFluid()));
		else if(stackList!=null)
			list = stackList;
		else
			list = Collections.singletonList(ApiUtils.copyStackWithAmount(stack, inputSize));
		return list;
	}

	public ItemStack getRandomizedExampleStack(long rand)
	{
		ItemStack ret = stack;
		if(ret.isEmpty()&&stackList!=null&&stackList.size() > 0)
			ret = stackList.get((int)(rand/20)%stackList.size());
		if(ret.isEmpty()&&oreName!=null)
		{
			List<ItemStack> ores = OreDictionary.getOres(oreName);
			if(ores!=null&&ores.size() > 0)
				ret = ores.get((int)(rand/20)%ores.size());
		}
		if(ret.isEmpty()&&fluid!=null&&ForgeModContainer.getInstance().universalBucket!=null)
			ret = UniversalBucket.getFilledBucket(ForgeModContainer.getInstance().universalBucket, fluid.getFluid());
		return ret;
	}

	public ItemStack getExampleStack()
	{
		ItemStack ret = stack;
		if(ret.isEmpty()&&stackList!=null&&stackList.size() > 0)
			ret = stackList.get(0);
		if(ret.isEmpty()&&oreName!=null)
		{
			List<ItemStack> ores = OreDictionary.getOres(oreName);
			if(ores!=null&&ores.size() > 0)
				ret = ores.get(0);
		}
		if(ret.isEmpty()&&fluid!=null&&ForgeModContainer.getInstance().universalBucket!=null)
			ret = UniversalBucket.getFilledBucket(ForgeModContainer.getInstance().universalBucket, fluid.getFluid());
		return ret;
	}

	public Ingredient toRecipeIngredient()
	{
		Ingredient ret = stack!=null?Ingredient.fromStacks(stack): null;
		if(ret==null&&stackList!=null&&stackList.size() > 0)
			ret = ApiUtils.createIngredientFromList(stackList);
		if(ret==null&&oreName!=null)
			ret = new OreIngredient(oreName);
		if(ret==null&&fluid!=null&&ForgeModContainer.getInstance().universalBucket!=null)
			ret = new IngredientFluidStack(fluid);
		return ret;
	}

	public boolean matchesItemStack(ItemStack input)
	{
		if(input.isEmpty())
			return false;
		if(this.fluid!=null)
		{
			FluidStack fs = FluidUtil.getFluidContained(input);
			if(fs!=null&&fs.containsFluid(fluid))
				return true;
		}
		if(this.oreName!=null)
			return ApiUtils.compareToOreName(input, oreName)&&this.inputSize <= input.getCount();
		if(this.stackList!=null)
		{
			for(ItemStack iStack : this.stackList)
				if(OreDictionary.itemMatches(iStack, input, false)&&this.inputSize <= input.getCount())
					return true;
		}
		if(!OreDictionary.itemMatches(stack, input, false)||this.inputSize > input.getCount())
			return false;
		if(this.useNBT)
		{
			if(this.stack.hasTagCompound()!=input.hasTagCompound())
				return false;
			if(!this.stack.hasTagCompound()&&!input.hasTagCompound())
				return true;
			return this.stack.getTagCompound().equals(input.getTagCompound());
		}
		return true;
	}

	public boolean matchesItemStackIgnoringSize(ItemStack input)
	{
		if(input.isEmpty())
			return false;
		if(this.fluid!=null)
		{
			FluidStack fs = FluidUtil.getFluidContained(input);
			if(fs!=null&&fs.containsFluid(fluid))
				return true;
		}
		if(this.oreName!=null)
			return ApiUtils.compareToOreName(input, oreName);
		if(this.stackList!=null)
		{
			for(ItemStack iStack : this.stackList)
				if(OreDictionary.itemMatches(iStack, input, false))
					return true;
		}
		if(!OreDictionary.itemMatches(stack, input, false))
			return false;
		if(this.useNBT)
		{
			if(this.stack.hasTagCompound()!=input.hasTagCompound())
				return false;
			if(!this.stack.hasTagCompound()&&!input.hasTagCompound())
				return true;
			return this.stack.getTagCompound().equals(input.getTagCompound());
		}
		return true;
	}

	@Override
	public boolean equals(Object object)
	{
		if(!(object instanceof IngredientStack))
			return false;
		IngredientStack otherIngredient = (IngredientStack)object;
		if(this.fluid!=null&&otherIngredient.fluid!=null)
			return this.fluid.equals(otherIngredient.fluid);
		if(this.oreName!=null&&otherIngredient.oreName!=null)
			return this.oreName.equals(otherIngredient.oreName);
		if(this.stackList!=null&&otherIngredient.stackList!=null)
		{
			for(ItemStack iStack : this.stackList)
				for(ItemStack iStack2 : otherIngredient.stackList)
					if(OreDictionary.itemMatches(iStack, iStack2, false))
						return true;
			return false;
		}
		if(!this.stack.isEmpty()&&!otherIngredient.stack.isEmpty())
		{
			ItemStack otherStack = otherIngredient.stack;
			if(!OreDictionary.itemMatches(stack, otherStack, false))
				return false;
			if(this.useNBT)
			{
				if(this.stack.hasTagCompound()!=otherStack.hasTagCompound())
					return false;
				if(!this.stack.hasTagCompound()&&!otherStack.hasTagCompound())
					return true;
				return this.stack.getTagCompound().equals(otherStack.getTagCompound());
			}
			return true;
		}
		return false;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		if(this.fluid!=null)
		{
			nbt.setString("fluid", FluidRegistry.getFluidName(fluid));
			nbt.setInteger("fluidAmount", fluid.amount);
			nbt.setInteger("nbtType", 3);
		}
		else if(this.oreName!=null)
		{
			nbt.setString("oreName", oreName);
			nbt.setInteger("nbtType", 2);
		}
		else if(this.stackList!=null)
		{
			NBTTagList list = new NBTTagList();
			for(ItemStack stack : stackList)
				if(!stack.isEmpty())
					list.appendTag(stack.writeToNBT(new NBTTagCompound()));
			nbt.setTag("stackList", list);
			nbt.setInteger("nbtType", 1);
		}
		else
		{
			nbt.setTag("stack", stack.writeToNBT(new NBTTagCompound()));
			nbt.setInteger("nbtType", 0);
			nbt.setBoolean("useNBT", useNBT);
		}
		nbt.setInteger("inputSize", inputSize);
		return nbt;
	}

	public static IngredientStack readFromNBT(NBTTagCompound nbt)
	{
		if(nbt.hasKey("nbtType"))
			switch(nbt.getInteger("nbtType"))
			{
				case 0:
					ItemStack stack = new ItemStack(nbt.getCompoundTag("stack"));
					stack.setCount(nbt.getInteger("inputSize"));
					IngredientStack ingr = new IngredientStack(stack);
					ingr.useNBT = nbt.getBoolean("useNBT");
					return ingr;
				case 1:
					NBTTagList list = nbt.getTagList("stackList", 10);
					List<ItemStack> stackList = new ArrayList();
					for(int i = 0; i < list.tagCount(); i++)
						stackList.add(new ItemStack(list.getCompoundTagAt(i)));
					return new IngredientStack(stackList, nbt.getInteger("inputSize"));
				case 2:
					return new IngredientStack(nbt.getString("oreName"), nbt.getInteger("inputSize"));
				case 3:
					FluidStack fs = new FluidStack(FluidRegistry.getFluid(nbt.getString("fluid")), nbt.getInteger("fluidAmount"));
					return new IngredientStack(fs);
			}
		return null;
	}
}