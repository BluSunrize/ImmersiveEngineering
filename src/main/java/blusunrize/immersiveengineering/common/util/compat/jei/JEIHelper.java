/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorType;
import blusunrize.immersiveengineering.client.gui.*;
import blusunrize.immersiveengineering.common.blocks.metal.ConveyorBlock;
import blusunrize.immersiveengineering.common.crafting.ArcRecyclingRecipe;
import blusunrize.immersiveengineering.common.gui.CraftingTableContainer;
import blusunrize.immersiveengineering.common.items.EngineersBlueprintItem;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.register.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDevices;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.alloysmelter.AlloySmelterRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.arcfurnace.ArcFurnaceRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.blastfurnace.BlastFurnaceFuelCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.blastfurnace.BlastFurnaceRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.bottlingmachine.BottlingMachineRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.cloche.ClocheRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.cokeoven.CokeOvenRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.crusher.CrusherRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.fermenter.FermenterRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.metalpress.MetalPressRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.mixer.MixerRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.refinery.RefineryRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.sawmill.SawmillRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.squeezer.SqueezerRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.workbench.WorkbenchRecipeCategory;
import com.google.common.collect.Collections2;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Optional;

@JeiPlugin
public class JEIHelper implements IModPlugin
{
	private static final ResourceLocation UID = new ResourceLocation(Lib.MODID, "main");
	public static final ResourceLocation JEI_GUI = new ResourceLocation(Lib.MODID, "textures/gui/jei_elements.png");
	public static IDrawableStatic slotDrawable;
	public static IRecipeSlotTooltipCallback fluidTooltipCallback = new IEFluidTooltipCallback();

	@Override
	public ResourceLocation getPluginUid()
	{
		return UID;
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration subtypeRegistry)
	{
		subtypeRegistry.registerSubtypeInterpreter(
				Misc.BLUEPRINT.asItem(), (stack, $) -> EngineersBlueprintItem.getCategory(stack)
		);
		for(IConveyorType<?> conveyor : ConveyorHandler.getConveyorTypes())
		{
			Item item = ConveyorHandler.getBlock(conveyor).asItem();
			subtypeRegistry.registerSubtypeInterpreter(
					item, (stack, $) -> ItemNBTHelper.getString(stack, ConveyorBlock.DEFAULT_COVER)
			);
		}
	}

	@Override
	public void registerIngredients(IModIngredientRegistration registry)
	{
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry)
	{
		//Recipes
		IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
		registry.addRecipeCategories(
				new CokeOvenRecipeCategory(guiHelper),
				new AlloySmelterRecipeCategory(guiHelper),
				new BlastFurnaceRecipeCategory(guiHelper),
				new BlastFurnaceFuelCategory(guiHelper),
				new ClocheRecipeCategory(guiHelper),
				new MetalPressRecipeCategory(guiHelper),
				new CrusherRecipeCategory(guiHelper),
				new SawmillRecipeCategory(guiHelper),
				new WorkbenchRecipeCategory(guiHelper),
				new SqueezerRecipeCategory(guiHelper),
				new FermenterRecipeCategory(guiHelper),
				new RefineryRecipeCategory(guiHelper),
				ArcFurnaceRecipeCategory.getDefault(guiHelper),
				ArcFurnaceRecipeCategory.getRecycling(guiHelper),
				new BottlingMachineRecipeCategory(guiHelper),
				new MixerRecipeCategory(guiHelper)
		);

		slotDrawable = guiHelper.getSlotDrawable();
	}

	@Override
	public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration)
	{

	}

	@Override
	public void registerRecipes(IRecipeRegistration registration)
	{
		IELogger.info("Adding recipes to JEI!!");
		registration.addRecipes(CokeOvenRecipe.RECIPES.getClientRecipes(), CokeOvenRecipeCategory.UID);
		registration.addRecipes(AlloyRecipe.RECIPES.getClientRecipes(), AlloySmelterRecipeCategory.UID);
		registration.addRecipes(BlastFurnaceRecipe.RECIPES.getClientRecipes(), BlastFurnaceRecipeCategory.UID);
		registration.addRecipes(BlastFurnaceFuel.RECIPES.getClientRecipes(), BlastFurnaceFuelCategory.UID);
		registration.addRecipes(ClocheRecipe.RECIPES.getClientRecipes(), ClocheRecipeCategory.UID);
		registration.addRecipes(Collections2.filter(MetalPressRecipe.STANDARD_RECIPES.getClientRecipes(), IJEIRecipe::listInJEI), MetalPressRecipeCategory.UID);
		registration.addRecipes(Collections2.filter(CrusherRecipe.RECIPES.getClientRecipes(), IJEIRecipe::listInJEI), CrusherRecipeCategory.UID);
		registration.addRecipes(Collections2.filter(SawmillRecipe.RECIPES.getClientRecipes(), IJEIRecipe::listInJEI), SawmillRecipeCategory.UID);
		registration.addRecipes(Collections2.filter(BlueprintCraftingRecipe.RECIPES.getClientRecipes(), IJEIRecipe::listInJEI), WorkbenchRecipeCategory.UID);
		registration.addRecipes(Collections2.filter(SqueezerRecipe.RECIPES.getClientRecipes(), IJEIRecipe::listInJEI), SqueezerRecipeCategory.UID);
		registration.addRecipes(Collections2.filter(FermenterRecipe.RECIPES.getClientRecipes(), IJEIRecipe::listInJEI), FermenterRecipeCategory.UID);
		registration.addRecipes(Collections2.filter(RefineryRecipe.RECIPES.getClientRecipes(), IJEIRecipe::listInJEI), RefineryRecipeCategory.UID);
		registration.addRecipes(Collections2.filter(ArcFurnaceRecipe.RECIPES.getClientRecipes(), input -> input instanceof ArcRecyclingRecipe&&input.listInJEI()), ArcFurnaceRecipeCategory.UID_RECYCLING);
		registration.addRecipes(Collections2.filter(ArcFurnaceRecipe.RECIPES.getClientRecipes(), input -> !(input instanceof ArcRecyclingRecipe)&&input.listInJEI()), ArcFurnaceRecipeCategory.UID);
		registration.addRecipes(Collections2.filter(BottlingMachineRecipe.RECIPES.getClientRecipes(), IJEIRecipe::listInJEI), BottlingMachineRecipeCategory.UID);
		registration.addRecipes(getFluidBucketRecipes(), BottlingMachineRecipeCategory.UID);
		registration.addRecipes(Collections2.filter(MixerRecipe.RECIPES.getClientRecipes(), IJEIRecipe::listInJEI), MixerRecipeCategory.UID);
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration)
	{
		registration.addRecipeTransferHandler(new AssemblerRecipeTransferHandler(registration.getTransferHelper()), VanillaRecipeCategoryUid.CRAFTING);
		registration.addRecipeTransferHandler(CraftingTableContainer.class, VanillaRecipeCategoryUid.CRAFTING, 1, 9, 10, 18+36);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
	{
		registration.addRecipeCatalyst(new ItemStack(Multiblocks.ASSEMBLER), VanillaRecipeCategoryUid.CRAFTING);

		registration.addRecipeCatalyst(new ItemStack(Multiblocks.COKE_OVEN), CokeOvenRecipeCategory.UID);
		registration.addRecipeCatalyst(new ItemStack(Multiblocks.ALLOY_SMELTER), AlloySmelterRecipeCategory.UID);
		registration.addRecipeCatalyst(new ItemStack(Multiblocks.ADVANCED_BLAST_FURNACE), BlastFurnaceRecipeCategory.UID, BlastFurnaceFuelCategory.UID);
		registration.addRecipeCatalyst(new ItemStack(Multiblocks.BLAST_FURNACE), BlastFurnaceRecipeCategory.UID, BlastFurnaceFuelCategory.UID);
		registration.addRecipeCatalyst(new ItemStack(MetalDevices.CLOCHE), ClocheRecipeCategory.UID);
		registration.addRecipeCatalyst(new ItemStack(Multiblocks.METAL_PRESS), MetalPressRecipeCategory.UID);
		registration.addRecipeCatalyst(new ItemStack(Multiblocks.CRUSHER), CrusherRecipeCategory.UID);
		registration.addRecipeCatalyst(new ItemStack(Multiblocks.SAWMILL), SawmillRecipeCategory.UID);
		registration.addRecipeCatalyst(new ItemStack(WoodenDevices.WORKBENCH), WorkbenchRecipeCategory.UID);
		registration.addRecipeCatalyst(new ItemStack(Multiblocks.AUTO_WORKBENCH), WorkbenchRecipeCategory.UID);
		registration.addRecipeCatalyst(new ItemStack(Multiblocks.SQUEEZER), SqueezerRecipeCategory.UID);
		registration.addRecipeCatalyst(new ItemStack(Multiblocks.FERMENTER), FermenterRecipeCategory.UID);
		registration.addRecipeCatalyst(new ItemStack(Multiblocks.REFINERY), RefineryRecipeCategory.UID);
		registration.addRecipeCatalyst(new ItemStack(Multiblocks.ARC_FURNACE), ArcFurnaceRecipeCategory.UID, ArcFurnaceRecipeCategory.UID_RECYCLING);
		registration.addRecipeCatalyst(new ItemStack(Multiblocks.BOTTLING_MACHINE), BottlingMachineRecipeCategory.UID);
		registration.addRecipeCatalyst(new ItemStack(Multiblocks.MIXER), MixerRecipeCategory.UID);
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration)
	{
		registration.addRecipeClickArea(CokeOvenScreen.class, 58, 36, 11, 13, CokeOvenRecipeCategory.UID);
		registration.addRecipeClickArea(AlloySmelterScreen.class, 84, 35, 22, 16, AlloySmelterRecipeCategory.UID);
		registration.addRecipeClickArea(BlastFurnaceScreen.class, 76, 35, 22, 15, BlastFurnaceRecipeCategory.UID, BlastFurnaceFuelCategory.UID);

		registration.addRecipeClickArea(SqueezerScreen.class, 90, 19, 20, 33, SqueezerRecipeCategory.UID);
		registration.addRecipeClickArea(FermenterScreen.class, 90, 19, 20, 33, FermenterRecipeCategory.UID);
		registration.addRecipeClickArea(RefineryScreen.class, 92, 24, 14, 20, RefineryRecipeCategory.UID);
		registration.addRecipeClickArea(ArcFurnaceScreen.class, 81, 38, 23, 35, ArcFurnaceRecipeCategory.UID, ArcFurnaceRecipeCategory.UID_RECYCLING);
		registration.addRecipeClickArea(MixerScreen.class, 52, 11, 16, 47, MixerRecipeCategory.UID);

		registration.addRecipeClickArea(ModWorkbenchScreen.class, 4, 41, 53, 18, WorkbenchRecipeCategory.UID);
		registration.addRecipeClickArea(AutoWorkbenchScreen.class, 90, 12, 39, 37, WorkbenchRecipeCategory.UID);

		registration.addRecipeClickArea(CraftingTableScreen.class, 88, 31, 28, 23, VanillaRecipeCategoryUid.CRAFTING);

		registration.addGhostIngredientHandler(IEContainerScreen.class, new IEGhostItemHandler());
		registration.addGhostIngredientHandler(FluidSorterScreen.class, new FluidSorterGhostHandler());
	}


	@Override
	public void registerAdvanced(IAdvancedRegistration registration)
	{

	}

	// TODO these throw when joining servers!
	private ArrayList<BottlingMachineRecipe> getFluidBucketRecipes()
	{
		// assume a source and flowing version of each fluid:
		int fluidCount = ForgeRegistries.FLUIDS.getValues().size()/2;

		ArrayList<BottlingMachineRecipe> recipes = new ArrayList<>(fluidCount);
		for(Fluid f : ForgeRegistries.FLUIDS)
			if(f.isSource(f.defaultFluidState()))
			{
				// Sort tags, prioritize vanilla/forge tags, and assume that more slashes means more specific tag
				Optional<ResourceLocation> tag = f.builtInRegistryHolder().tags()
						.map(TagKey::location)
						.min((o1, o2) -> {
							// TODO not symmetric!
							if(!("minecraft".equals(o1.getNamespace())||"forge".equals(o1.getNamespace())))
								return 1;
							return -Long.compare(
									o1.getPath().codePoints().filter(ch -> ch=='/').count(),
									o2.getPath().codePoints().filter(ch -> ch=='/').count()
							);
						});
				ItemStack bucket = f.getBucket().getDefaultInstance();
				if(!bucket.isEmpty()&&tag.isPresent())
					recipes.add(new BottlingMachineRecipe(
							new ResourceLocation(Lib.MODID, "jei_bucket_"+f.getRegistryName().getPath()),
							Lazy.of(() -> bucket),
							Ingredient.of(Items.BUCKET),
							new FluidTagInput(tag.get(), 1000)
					));
			}
		return recipes;
	}

//	@Override
//	public void register(IModRegistry registryIn)
//	{
	//Blacklist
//		jeiHelpers.getIngredientBlacklist().addIngredientToBlacklist(new ItemStack(IEContent.blockCrop, 1, OreDictionary.WILDCARD_VALUE));
//		jeiHelpers.getIngredientBlacklist().addIngredientToBlacklist(new ItemStack(IEContent.itemFakeIcons, 1, OreDictionary.WILDCARD_VALUE));
//		jeiHelpers.getIngredientBlacklist().addIngredientToBlacklist(new ItemStack(IEContent.blockStoneDevice, 1, OreDictionary.WILDCARD_VALUE));
//		jeiHelpers.getIngredientBlacklist().addIngredientToBlacklist(new ItemStack(IEContent.blockMetalMultiblock, 1, OreDictionary.WILDCARD_VALUE));

//		for(IERecipeCategory<Object, IRecipeWrapper> cat : categories.values())
//		{
//			cat.addCatalysts(registryIn);
//			modRegistry.handleRecipes(cat.getRecipeClass(), cat, cat.getRecipeCategoryUid());
//		}
//		modRegistry.addRecipeHandlers(categories);


	// Allow jumping to recipies from the block GUIs.
//	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
	{
	}

//	private IERecipeCategory getFactory(Class recipeClass)
//	{
//		IERecipeCategory factory = this.categories.get(recipeClass);
//
//		if(factory==null&&recipeClass!=Object.class)
//			factory = getFactory(recipeClass.getSuperclass());
//
//		return factory;
//	}
}