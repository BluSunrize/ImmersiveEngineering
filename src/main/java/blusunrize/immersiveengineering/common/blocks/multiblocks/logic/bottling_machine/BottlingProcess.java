/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic.bottling_machine;


import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.api.crafting.TagOutputList;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.bottling_machine.BottlingMachineLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInWorld;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor.InWorldProcessLoader;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext.ProcessContextInWorld;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;

// TODO split into two separate classes for filling and "real" processes?
public class BottlingProcess extends MultiblockProcessInWorld<BottlingMachineRecipe>
{
	private static final RecipeHolder<BottlingMachineRecipe> DUMMY_RECIPE = new RecipeHolder<>(
			IEApi.ieLoc("bottling_dummy"),
			new BottlingMachineRecipe(
					new TagOutputList(TagOutput.EMPTY), IngredientWithSize.of(ItemStack.EMPTY),
					SizedFluidIngredient.of(FluidTags.WATER, 1)
			)
	);
	private static final float TRANSFORMATION_POINT = 0.45f;

	private final boolean isFilling;
	private final List<ItemStack> filledContainer;
	private final FluidTank tank;
	private final BooleanSupplier allowPartialFill;

	public BottlingProcess(
			BiFunction<Level, ResourceLocation, BottlingMachineRecipe> getRecipe,
			Provider provider,
			CompoundTag nbt,
			State state
	)
	{
		super(nbt.getBoolean("isFilling")?($, $1) -> DUMMY_RECIPE.value(): getRecipe, nbt, provider);
		this.tank = state.tank;
		this.allowPartialFill = () -> state.allowPartialFill;
		this.isFilling = nbt.getBoolean("isFilling");
		final ListTag filledNBT = nbt.getList("filledContainer", CompoundTag.TAG_COMPOUND);
		this.filledContainer = new ArrayList<>();
		for(int i = 0; i < filledNBT.size(); i++)
			this.filledContainer.add(ItemStack.parseOptional(provider, filledNBT.getCompound(i)));
	}

	public BottlingProcess(RecipeHolder<BottlingMachineRecipe> recipe, NonNullList<ItemStack> inputItem, State state)
	{
		super(recipe, TRANSFORMATION_POINT, inputItem);
		this.tank = state.tank;
		this.allowPartialFill = () -> state.allowPartialFill;
		this.isFilling = false;
		this.filledContainer = List.of();
	}

	public BottlingProcess(ItemStack inputItem, ItemStack currentContainer, State state)
	{
		super(DUMMY_RECIPE, TRANSFORMATION_POINT, NonNullList.withSize(1, inputItem));
		this.tank = state.tank;
		this.allowPartialFill = () -> state.allowPartialFill;
		this.isFilling = true;
		// copy item into output already, to be filled later
		this.filledContainer = Arrays.asList(currentContainer);
	}

	public static InWorldProcessLoader<BottlingMachineRecipe> loader(State state)
	{
		return (getRecipe, tag, provider) -> {
			if(tag.getBoolean("isFilling"))
				return new BottlingProcess((level, resourceLocation) -> DUMMY_RECIPE.value(), provider, tag, state);
			return new BottlingProcess(getRecipe, provider, tag, state);
		};
	}

	@Override
	public void doProcessTick(ProcessContextInWorld<BottlingMachineRecipe> context, IMultiblockLevel level)
	{
		super.doProcessTick(context, level);

		final Level rawLevel = level.getRawLevel();
		float transPoint = getMaxTicks(rawLevel)*transformationPoint;
		if(processTick >= transPoint&&processTick < 1+transPoint)
		{
			FluidStack fs = tank.getFluid();
			if(!fs.isEmpty())
			{
				// filling recipes use custom logic
				if(isFilling)
				{
					ItemStack ret = FluidUtils.fillFluidContainer(tank, filledContainer.get(0), ItemStack.EMPTY, null);
					if(!ret.isEmpty())
						filledContainer.set(0, ret);
					// reduce process tick, if the item should be held in place
					if(!allowPartialFill.getAsBoolean()&&!FluidUtils.isFluidContainerFull(ret))
						processTick--;
				}
				// normal recipes just consume the fluid at this point
				else
					tank.drain(getRecipe(rawLevel).fluidInput.amount(), FluidAction.EXECUTE);
			}
		}
	}

	@Override
	public List<ItemStack> getDisplayItem(Level level)
	{
		if(isFilling)
			return filledContainer;
		return super.getDisplayItem(level);
	}

	@Override
	protected List<ItemStack> getRecipeItemOutputs(Level level, ProcessContextInWorld<BottlingMachineRecipe> context)
	{
		if(isFilling)
			return filledContainer;
		return super.getRecipeItemOutputs(level, context);
	}

	@Override
	public void writeExtraDataToNBT(CompoundTag nbt, Provider provider)
	{
		super.writeExtraDataToNBT(nbt, provider);
		nbt.putBoolean("isFilling", isFilling);
		final ListTag filledNBT = new ListTag();
		for(ItemStack stack : this.filledContainer)
			filledNBT.add(stack.saveOptional(provider));
		nbt.put("filledContainer", filledNBT);
	}
}
