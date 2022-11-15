package blusunrize.immersiveengineering.common.blocks.multiblocks.logic.bottling_machine;


import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.bottling_machine.BottlingMachineLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInWorld;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor.InWorldProcessLoader;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext.ProcessContextInWorld;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;

public class BottlingProcess extends MultiblockProcessInWorld<BottlingMachineRecipe>
{
	private static final BottlingMachineRecipe DUMMY_RECIPE = new BottlingMachineRecipe(
			new ResourceLocation(Lib.MODID, "bottling_dummy_recipe"),
			List.of(Lazy.of(() -> ItemStack.EMPTY)), IngredientWithSize.of(ItemStack.EMPTY),
			new FluidTagInput(FluidTags.WATER, 0)
	);
	private static final float TRANSFORMATION_POINT = 0.45f;

	private final boolean isFilling;
	private ItemStack filledContainer = ItemStack.EMPTY;
	private final FluidTank tank;
	private final BooleanSupplier allowPartialFill;

	public BottlingProcess(
			ResourceLocation recipeId,
			BiFunction<Level, ResourceLocation, BottlingMachineRecipe> getRecipe,
			NonNullList<ItemStack> inputItem,
			State state
	)
	{
		super(recipeId, getRecipe, TRANSFORMATION_POINT, inputItem);
		this.tank = state.tank;
		this.allowPartialFill = () -> state.allowPartialFill;
		this.isFilling = false;
	}

	public BottlingProcess(
			BiFunction<Level, ResourceLocation, BottlingMachineRecipe> getRecipe, CompoundTag nbt, State state
	)
	{
		super(getRecipe, nbt);
		this.tank = state.tank;
		this.allowPartialFill = () -> state.allowPartialFill;
		this.isFilling = nbt.getBoolean("isFilling");
		if(isFilling)
			this.filledContainer = inputItems.get(0);
	}

	public BottlingProcess(BottlingMachineRecipe recipe, NonNullList<ItemStack> inputItem, State state)
	{
		super(recipe, TRANSFORMATION_POINT, inputItem);
		this.tank = state.tank;
		this.allowPartialFill = () -> state.allowPartialFill;
		this.isFilling = false;
	}

	public BottlingProcess(NonNullList<ItemStack> inputItem, State state)
	{
		super(DUMMY_RECIPE, TRANSFORMATION_POINT, inputItem);
		this.tank = state.tank;
		this.allowPartialFill = () -> state.allowPartialFill;
		this.isFilling = true;
		// copy item into output already, to be filled later
		this.filledContainer = inputItem.get(0);
	}

	public static InWorldProcessLoader<BottlingMachineRecipe> loader(State state)
	{
		return (getRecipe, tag) -> new BottlingProcess(getRecipe, tag, state);
	}

	@Override
	public void doProcessTick(ProcessContextInWorld<BottlingMachineRecipe> context, IMultiblockLevel level)
	{
		super.doProcessTick(context, level);

		final var rawLevel = level.getRawLevel();
		float transPoint = getMaxTicks(rawLevel)*transformationPoint;
		if(processTick >= transPoint&&processTick < 1+transPoint)
		{
			FluidStack fs = tank.getFluid();
			if(!fs.isEmpty())
			{
				// filling recipes use custom logic
				if(isFilling)
				{
					ItemStack ret = FluidUtils.fillFluidContainer(tank, filledContainer, ItemStack.EMPTY, null);
					if(!ret.isEmpty())
						filledContainer = ret;
					// reduce process tick, if the item should be held in place
					if(!allowPartialFill.getAsBoolean()&&!FluidUtils.isFluidContainerFull(ret))
						processTick--;
				}
				// normal recipes just consume the fluid at this point
				else
					tank.drain(getRecipe(rawLevel).fluidInput.getAmount(), FluidAction.EXECUTE);
			}
		}
	}

	@Override
	public List<ItemStack> getDisplayItem(Level level)
	{
		if(isFilling)
			return List.of(filledContainer);
		return super.getDisplayItem(level);
	}

	@Override
	protected List<ItemStack> getRecipeItemOutputs(Level level)
	{
		if(isFilling)
			return List.of(filledContainer);
		return super.getRecipeItemOutputs(level);
	}

	@Override
	public void writeExtraDataToNBT(CompoundTag nbt)
	{
		super.writeExtraDataToNBT(nbt);
		nbt.putBoolean("isFilling", isFilling);
	}
}
