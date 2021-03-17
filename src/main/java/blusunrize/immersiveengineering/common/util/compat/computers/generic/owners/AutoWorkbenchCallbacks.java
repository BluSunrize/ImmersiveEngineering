package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.common.blocks.metal.AutoWorkbenchTileEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.IndexArgument;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.PoweredMBCallbacks;
import net.minecraft.item.ItemStack;

public class AutoWorkbenchCallbacks extends MultiblockCallbackOwner<AutoWorkbenchTileEntity>
{
	public AutoWorkbenchCallbacks()
	{
		super(AutoWorkbenchTileEntity.class, "auto_workbench");
		addAdditional(PoweredMBCallbacks.INSTANCE);
	}

	@ComputerCallable
	public void selectRecipe(CallbackEnvironment<AutoWorkbenchTileEntity> env, @IndexArgument int selected)
	{
		BlueprintCraftingRecipe[] availableRecipes = env.getObject().getAvailableRecipes();
		if(selected < 0||selected >= availableRecipes.length)
			throw new RuntimeException("Only "+availableRecipes.length+" recipes are available");
		env.getObject().selectedRecipe = selected;
	}

	@ComputerCallable
	public void unselectRecipe(CallbackEnvironment<AutoWorkbenchTileEntity> env)
	{
		env.getObject().selectedRecipe = -1;
	}

	@ComputerCallable
	public ItemStack[] getAvailableRecipes(CallbackEnvironment<AutoWorkbenchTileEntity> env)
	{
		BlueprintCraftingRecipe[] availableRecipes = env.getObject().getAvailableRecipes();
		ItemStack[] outputs = new ItemStack[availableRecipes.length];
		for(int i = 0; i < availableRecipes.length; ++i)
			outputs[i] = availableRecipes[i].output;
		return outputs;
	}

	@ComputerCallable
	public int getSelectedRecipe(CallbackEnvironment<AutoWorkbenchTileEntity> env)
	{
		return env.getObject().selectedRecipe+1;
	}
}
