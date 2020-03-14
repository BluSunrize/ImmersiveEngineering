/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import com.google.common.base.Preconditions;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static net.minecraftforge.registries.ForgeRegistries.FLUIDS;

public class IngredientStack
{
	public ItemStack stack = ItemStack.EMPTY;
	public List<ItemStack> stackList;
	public ResourceLocation tag;
	public FluidStack fluid;
	public int inputSize;
	public boolean useNBT;

	public IngredientStack(IItemProvider item)
	{
		this(new ItemStack(item));
	}

	public IngredientStack(ItemStack stack)
	{
		this.stack = stack;
		this.inputSize = stack.getCount();
	}

	public IngredientStack(ResourceLocation tag, int inputSize)
	{
		this.tag = tag;
		this.inputSize = inputSize;
	}

	public IngredientStack(Tag<?> tag)
	{
		this(tag.getId());
	}

	public IngredientStack(ResourceLocation tag)
	{
		this(tag, 1);
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
		this.tag = ingr.tag;
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
		else if(input instanceof ResourceLocation)
		{
			if(this.tag!=null)
				return this.tag.equals(input);
			return ApiUtils.compareToOreName(stack, (ResourceLocation)input);
		}
		throw new UnsupportedOperationException("Can not match against object "+input+" of type "+input.getClass());
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
		if(tag!=null)
			return ApiUtils.getItemsInTag(tag);
		//TODO when universal buckets are back
		//if(fluid!=null&&ForgeModContainer.getInstance().universalBucket!=null)
		//	return Collections.singletonList(UniversalBucket.getFilledBucket(ForgeModContainer.getInstance().universalBucket, fluid.getFluid()));
		return Collections.singletonList(stack);
	}

	public List<ItemStack> getSizedStackList()
	{
		return getStackList().stream()
				.map(s -> ApiUtils.copyStackWithAmount(s, inputSize))
				.collect(Collectors.toList());
	}

	public ItemStack getRandomizedExampleStack(long rand)
	{
		List<ItemStack> all = getStackList();
		return all.get((int)(rand/20)%all.size());
	}

	public ItemStack getExampleStack()
	{
		return getStackList().get(0);
	}

	public boolean matchesItemStack(ItemStack input)
	{
		return matchesItemStack(input, inputSize);
	}

	private boolean matchesItemStack(ItemStack input, int minSize)
	{
		if(input.isEmpty())
			return false;
		if(this.fluid!=null)
		{
			return FluidUtil.getFluidContained(input)
					.map(fs -> fs.containsFluid(fluid))
					.orElse(false);
		}
		if(this.tag!=null)
			return ApiUtils.compareToOreName(input, tag)&&minSize <= input.getCount();
		if(this.stackList!=null)
		{
			for(ItemStack iStack : this.stackList)
				if(ItemStack.areItemsEqual(iStack, input)&&minSize <= input.getCount())
					return true;
		}
		if(!ItemStack.areItemsEqual(stack, input)||minSize > input.getCount())
			return false;
		if(this.useNBT)
		{
			boolean stackHasNBT = stack.hasTag();
			boolean inputHasNBT = input.hasTag();
			if(!stackHasNBT&&!inputHasNBT)
				return true;
			else if(stackHasNBT!=inputHasNBT)
				return false;
			return this.stack.getOrCreateTag().equals(input.getOrCreateTag());
		}
		return true;
	}

	public boolean matchesItemStackIgnoringSize(ItemStack input)
	{
		return matchesItemStack(input, 0);
	}

	@Override
	public boolean equals(Object object)
	{
		if(!(object instanceof IngredientStack))
			return false;
		IngredientStack otherIngredient = (IngredientStack)object;
		if(this.fluid!=null&&otherIngredient.fluid!=null)
			return this.fluid.equals(otherIngredient.fluid);
		if(this.tag!=null&&otherIngredient.tag!=null)
			return this.tag.equals(otherIngredient.tag);
		if(this.stackList!=null&&otherIngredient.stackList!=null)
		{
			for(ItemStack iStack : this.stackList)
				for(ItemStack iStack2 : otherIngredient.stackList)
					if(ItemStack.areItemsEqual(iStack, iStack2))
						return true;
			return false;
		}
		if(!this.stack.isEmpty()&&!otherIngredient.stack.isEmpty())
		{
			ItemStack otherStack = otherIngredient.stack;
			if(!ItemStack.areItemsEqual(stack, otherStack))
				return false;
			if(this.useNBT)
			{
				boolean stackHasNBT = stack.hasTag();
				boolean otherHasNBT = otherStack.hasTag();
				if(!stackHasNBT&&!otherHasNBT)
					return true;
				else if(stackHasNBT!=otherHasNBT)
					return false;
				return this.stack.getOrCreateTag().equals(otherStack.getOrCreateTag());
			}
			return true;
		}
		return false;
	}

	public CompoundNBT writeToNBT(CompoundNBT nbt)
	{
		if(this.fluid!=null)
		{
			nbt.putString("fluid", FLUIDS.getKey(fluid.getFluid()).toString());
			nbt.putInt("fluidAmount", fluid.getAmount());
			nbt.putInt("nbtType", 3);
		}
		else if(this.tag!=null)
		{
			nbt.putString("tag", tag.toString());
			nbt.putInt("nbtType", 2);
		}
		else if(this.stackList!=null)
		{
			ListNBT list = new ListNBT();
			for(ItemStack stack : stackList)
				if(!stack.isEmpty())
					list.add(stack.write(new CompoundNBT()));
			list.add(stack.write(new CompoundNBT()));
			nbt.put("stackList", list);
			nbt.putInt("nbtType", 1);
		}
		else
		{
			nbt.put("stack", stack.write(new CompoundNBT()));
			nbt.put("stack", stack.write(new CompoundNBT()));
			nbt.putInt("nbtType", 0);
			nbt.putBoolean("useNBT", useNBT);
		}
		nbt.putInt("inputSize", inputSize);
		return nbt;
	}

	public static IngredientStack readFromNBT(CompoundNBT nbt)
	{
		if(nbt.contains("nbtType"))
			switch(nbt.getInt("nbtType"))
			{
				case 0:
					ItemStack stack = ItemStack.read(nbt.getCompound("stack"));
					stack.setCount(nbt.getInt("inputSize"));
					IngredientStack ingr = new IngredientStack(stack);
					ingr.useNBT = nbt.getBoolean("useNBT");
					return ingr;
				case 1:
					ListNBT list = nbt.getList("stackList", 10);
					List<ItemStack> stackList = new ArrayList<>();
					for(int i = 0; i < list.size(); i++)
						stackList.add(ItemStack.read(list.getCompound(i)));
					return new IngredientStack(stackList, nbt.getInt("inputSize"));
				case 2:
					return new IngredientStack(new ResourceLocation(nbt.getString("tag")), nbt.getInt("inputSize"));
				case 3:
					FluidStack fs = new FluidStack(FLUIDS.getValue(new ResourceLocation(nbt.getString("fluid"))), nbt.getInt("fluidAmount"));
					return new IngredientStack(fs);
			}
		return null;
	}

	public boolean isValid()
	{
		return !getStackList().isEmpty();
	}
}
