/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei;

import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe.BlastFurnaceFuel;
import blusunrize.immersiveengineering.client.gui.*;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalMultiblock;
import blusunrize.immersiveengineering.common.crafting.ArcRecyclingRecipe;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.alloysmelter.AlloySmelterRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.arcfurnace.ArcFurnaceRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.blastfurnace.BlastFurnaceFuelCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.blastfurnace.BlastFurnaceRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.bottlingmachine.BottlingMachineRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.cokeoven.CokeOvenRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.crusher.CrusherRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.fermenter.FermenterRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.metalpress.MetalPressRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.mixer.MixerRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.refinery.RefineryRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.squeezer.SqueezerRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.workbench.WorkbenchRecipeCategory;
import com.google.common.collect.Collections2;
import mezz.jei.api.*;
import mezz.jei.api.ISubtypeRegistry.ISubtypeInterpreter;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@JEIPlugin
public class JEIHelper implements IModPlugin
{
	public static IJeiHelpers jeiHelpers;
	public static IModRegistry modRegistry;
	public static IDrawable slotDrawable;
	public static ITooltipCallback fluidTooltipCallback = new IEFluidTooltipCallback();

	@Override
	public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry)
	{
		//NBT Ignorance
		subtypeRegistry.registerSubtypeInterpreter(Item.getItemFromBlock(IEContent.blockConveyor), itemStack -> {
			if(!itemStack.isEmpty()&&ItemNBTHelper.hasKey(itemStack, "conveyorType"))
				return ItemNBTHelper.getString(itemStack, "conveyorType");
			return ISubtypeInterpreter.NONE;
		});
		subtypeRegistry.registerSubtypeInterpreter(IEContent.itemBullet, itemStack -> {
			if(!itemStack.isEmpty()&&itemStack.getMetadata()==2&&ItemNBTHelper.hasKey(itemStack, "bullet"))
				return ItemNBTHelper.getString(itemStack, "bullet");
			return ISubtypeInterpreter.NONE;
		});
	}

	@Override
	public void registerIngredients(IModIngredientRegistration registry)
	{
	}

	Map<Class, IERecipeCategory> categories = new LinkedHashMap<>();

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry)
	{
		jeiHelpers = registry.getJeiHelpers();
		//Recipes
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
		slotDrawable = guiHelper.getSlotDrawable();
		if (Config.IEConfig.machines.cokeoven_enabled)
			categories.put(CokeOvenRecipe.class, new CokeOvenRecipeCategory(guiHelper));
		if (Config.IEConfig.machines.alloysmelter_enabled)
			categories.put(AlloyRecipe.class, new AlloySmelterRecipeCategory(guiHelper));
		if (Config.IEConfig.machines.blastfurnace_enabled) {
			categories.put(BlastFurnaceRecipe.class, new BlastFurnaceRecipeCategory(guiHelper));
			categories.put(BlastFurnaceFuel.class, new BlastFurnaceFuelCategory(guiHelper));
		}
		if (Config.IEConfig.machines.metalpress_enabled)
			categories.put(MetalPressRecipe.class, new MetalPressRecipeCategory(guiHelper));
		if (Config.IEConfig.machines.crusher_enabled)
			categories.put(CrusherRecipe.class, new CrusherRecipeCategory(guiHelper));
		categories.put(BlueprintCraftingRecipe.class, new WorkbenchRecipeCategory(guiHelper));
		if (Config.IEConfig.machines.squeezer_enabled)
			categories.put(SqueezerRecipe.class, new SqueezerRecipeCategory(guiHelper));
		if (Config.IEConfig.machines.fermenter_enabled)
			categories.put(FermenterRecipe.class, new FermenterRecipeCategory(guiHelper));
		if (Config.IEConfig.machines.refinery_enabled)
			categories.put(RefineryRecipe.class, new RefineryRecipeCategory(guiHelper));
		if (Config.IEConfig.machines.arcfurnace_enabled) {
			categories.put(ArcFurnaceRecipe.class, ArcFurnaceRecipeCategory.getDefault(guiHelper));
			categories.put(ArcRecyclingRecipe.class, ArcFurnaceRecipeCategory.getRecycling(guiHelper));
		}
		if (Config.IEConfig.machines.bottlingmachine_enabled)
			categories.put(BottlingMachineRecipe.class, new BottlingMachineRecipeCategory(guiHelper));
		if (Config.IEConfig.machines.mixer_enabled)
			categories.put(MixerRecipe.class, new MixerRecipeCategory(guiHelper));
		registry.addRecipeCategories(categories.values().toArray(new IRecipeCategory[0]));
	}

	@Override
	public void register(IModRegistry registryIn)
	{
		modRegistry = registryIn;
		//Blacklist
		jeiHelpers.getIngredientBlacklist().addIngredientToBlacklist(new ItemStack(IEContent.blockCrop, 1, OreDictionary.WILDCARD_VALUE));
		jeiHelpers.getIngredientBlacklist().addIngredientToBlacklist(new ItemStack(IEContent.itemFakeIcons, 1, OreDictionary.WILDCARD_VALUE));
		jeiHelpers.getIngredientBlacklist().addIngredientToBlacklist(new ItemStack(IEContent.blockStoneDevice, 1, OreDictionary.WILDCARD_VALUE));
		jeiHelpers.getIngredientBlacklist().addIngredientToBlacklist(new ItemStack(IEContent.blockMetalMultiblock, 1, OreDictionary.WILDCARD_VALUE));

		modRegistry.getRecipeTransferRegistry().addRecipeTransferHandler(new AssemblerRecipeTransferHandler(), VanillaRecipeCategoryUid.CRAFTING);
		modRegistry.addGhostIngredientHandler(GuiIEContainerBase.class, new IEGhostItemHandler());
		modRegistry.addGhostIngredientHandler(GuiFluidSorter.class, new FluidSorterGhostHandler());
		modRegistry.addRecipeCatalyst(new ItemStack(IEContent.blockMetalMultiblock, 1, BlockTypes_MetalMultiblock.ASSEMBLER.getMeta()), VanillaRecipeCategoryUid.CRAFTING);

		for(IERecipeCategory<Object, IRecipeWrapper> cat : categories.values())
		{
			cat.addCatalysts(registryIn);
			modRegistry.handleRecipes(cat.getRecipeClass(), cat, cat.getRecipeCategoryUid());
		}
//		modRegistry.addRecipeHandlers(categories);

		IELogger.info("Adding recipes to JEI!!");
		if (Config.IEConfig.machines.cokeoven_enabled)
			modRegistry.addRecipes(new ArrayList<>(CokeOvenRecipe.recipeList), "ie.cokeoven");
		if (Config.IEConfig.machines.alloysmelter_enabled)
			modRegistry.addRecipes(new ArrayList<>(AlloyRecipe.recipeList), "ie.alloysmelter");
		if (Config.IEConfig.machines.blastfurnace_enabled) {
			modRegistry.addRecipes(new ArrayList<>(BlastFurnaceRecipe.recipeList), "ie.blastfurnace");
			modRegistry.addRecipes(new ArrayList<>(BlastFurnaceRecipe.blastFuels), "ie.blastfurnace.fuel");
		}
		if (Config.IEConfig.machines.metalpress_enabled)
			modRegistry.addRecipes(new ArrayList<>(Collections2.filter(MetalPressRecipe.recipeList.values(), IJEIRecipe::listInJEI)), "ie.metalPress");
		if (Config.IEConfig.machines.crusher_enabled)
			modRegistry.addRecipes(new ArrayList<>(Collections2.filter(CrusherRecipe.recipeList, IJEIRecipe::listInJEI)), "ie.crusher");
		modRegistry.addRecipes(new ArrayList<>(Collections2.filter(BlueprintCraftingRecipe.recipeList.values(), IJEIRecipe::listInJEI)), "ie.workbench");
		if (Config.IEConfig.machines.squeezer_enabled)
			modRegistry.addRecipes(new ArrayList<>(Collections2.filter(SqueezerRecipe.recipeList, IJEIRecipe::listInJEI)), "ie.squeezer");
		if (Config.IEConfig.machines.fermenter_enabled)
			modRegistry.addRecipes(new ArrayList<>(Collections2.filter(FermenterRecipe.recipeList, IJEIRecipe::listInJEI)), "ie.fermenter");
		if (Config.IEConfig.machines.refinery_enabled)
			modRegistry.addRecipes(new ArrayList<>(Collections2.filter(RefineryRecipe.recipeList, IJEIRecipe::listInJEI)), "ie.refinery");
		if (Config.IEConfig.machines.arcfurnace_enabled) {
			modRegistry.addRecipes(new ArrayList<>(Collections2.filter(ArcFurnaceRecipe.recipeList, input -> input instanceof ArcRecyclingRecipe&&input.listInJEI())), "ie.arcFurnace.recycling");
			modRegistry.addRecipes(new ArrayList<>(Collections2.filter(ArcFurnaceRecipe.recipeList, input -> {
				return !(input instanceof ArcRecyclingRecipe)&&input.listInJEI();
			})), "ie.arcFurnace");
		}
		if (Config.IEConfig.machines.bottlingmachine_enabled)
    		modRegistry.addRecipes(new ArrayList(Collections2.filter(BottlingMachineRecipe.recipeList, input -> input.listInJEI())), "ie.bottlingMachine");
		if (Config.IEConfig.machines.mixer_enabled)
			modRegistry.addRecipes(new ArrayList(Collections2.filter(MixerRecipe.recipeList, input -> input.listInJEI())), "ie.mixer");

		// Allow jumping to recipies from the block GUIs.
		if (Config.IEConfig.machines.cokeoven_enabled)
			modRegistry.addRecipeClickArea(GuiCokeOven.class, 58, 36, 11, 13, "ie.cokeoven");
		if (Config.IEConfig.machines.alloysmelter_enabled)
			modRegistry.addRecipeClickArea(GuiAlloySmelter.class, 84, 35, 22, 16, "ie.alloysmelter");
		if (Config.IEConfig.machines.blastfurnace_enabled)
			modRegistry.addRecipeClickArea(GuiBlastFurnace.class, 76, 35, 22, 15, "ie.blastfurnace", "ie.blastfurnace.fuel");
		if (Config.IEConfig.machines.squeezer_enabled)
			modRegistry.addRecipeClickArea(GuiSqueezer.class, 110, 19, 20, 51, "ie.squeezer");
		if (Config.IEConfig.machines.fermenter_enabled)
			modRegistry.addRecipeClickArea(GuiFermenter.class, 110, 19, 20, 51, "ie.fermenter");
		if (Config.IEConfig.machines.refinery_enabled)
			modRegistry.addRecipeClickArea(GuiRefinery.class, 83, 36, 20, 13, "ie.refinery");
		if (Config.IEConfig.machines.arcfurnace_enabled)
			modRegistry.addRecipeClickArea(GuiArcFurnace.class, 81, 38, 23, 35, "ie.arcFurnace", "ie.arcFurnace.recycling");
		if (Config.IEConfig.machines.mixer_enabled)
			modRegistry.addRecipeClickArea(GuiMixer.class, 76, 11, 58, 47, "ie.mixer");

		modRegistry.addRecipeClickArea(GuiModWorkbench.class, 4, 41, 53, 18, "ie.workbench");
		modRegistry.addRecipeClickArea(GuiAutoWorkbench.class, 90, 12, 39, 37, "ie.workbench");
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
	{
	}

	private IERecipeCategory getFactory(Class recipeClass)
	{
		IERecipeCategory factory = this.categories.get(recipeClass);

		if(factory==null&&recipeClass!=Object.class)
			factory = getFactory(recipeClass.getSuperclass());

		return factory;
	}
}