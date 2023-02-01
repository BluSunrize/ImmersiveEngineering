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
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorType;
import blusunrize.immersiveengineering.client.gui.*;
import blusunrize.immersiveengineering.common.blocks.metal.ConveyorBlock;
import blusunrize.immersiveengineering.common.crafting.ArcRecyclingRecipe;
import blusunrize.immersiveengineering.common.gui.CraftingTableContainer;
import blusunrize.immersiveengineering.common.items.EngineersBlueprintItem;
import blusunrize.immersiveengineering.common.items.PotionBucketItem;
import blusunrize.immersiveengineering.common.items.ShaderItem;
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
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

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
				VanillaTypes.ITEM_STACK, Misc.BLUEPRINT.asItem(), (stack, $) -> EngineersBlueprintItem.getCategory(stack)
		);
		subtypeRegistry.registerSubtypeInterpreter(
				VanillaTypes.ITEM_STACK, Misc.POTION_BUCKET.asItem(), (stack, $) -> PotionBucketItem.getPotion(stack).getName("")
		);
		subtypeRegistry.registerSubtypeInterpreter(
				VanillaTypes.ITEM_STACK, Misc.SHADER.asItem(), (stack, $) -> ItemNBTHelper.getString(stack, ShaderItem.SHADER_NAME_KEY)
		);
		for(IConveyorType<?> conveyor : ConveyorHandler.getConveyorTypes())
		{
			Item item = ConveyorHandler.getBlock(conveyor).asItem();
			subtypeRegistry.registerSubtypeInterpreter(
					VanillaTypes.ITEM_STACK, item, (stack, $) -> ItemNBTHelper.getString(stack, ConveyorBlock.DEFAULT_COVER)
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
		registration.addRecipes(CokeOvenRecipeCategory.TYPE, getRecipes(CokeOvenRecipe.RECIPES));
		registration.addRecipes(AlloySmelterRecipeCategory.TYPE, getRecipes(AlloyRecipe.RECIPES));
		registration.addRecipes(BlastFurnaceRecipeCategory.TYPE, getRecipes(BlastFurnaceRecipe.RECIPES));
		registration.addRecipes(BlastFurnaceFuelCategory.TYPE, getRecipes(BlastFurnaceFuel.RECIPES));
		registration.addRecipes(ClocheRecipeCategory.TYPE, getRecipes(ClocheRecipe.RECIPES));
		registration.addRecipes(MetalPressRecipeCategory.TYPE, filter(getRecipes(MetalPressRecipe.STANDARD_RECIPES), IJEIRecipe::listInJEI));
		registration.addRecipes(CrusherRecipeCategory.TYPE, filter(getRecipes(CrusherRecipe.RECIPES), IJEIRecipe::listInJEI));
		registration.addRecipes(SawmillRecipeCategory.TYPE, filter(getRecipes(SawmillRecipe.RECIPES), IJEIRecipe::listInJEI));
		registration.addRecipes(WorkbenchRecipeCategory.TYPE, filter(getRecipes(BlueprintCraftingRecipe.RECIPES), IJEIRecipe::listInJEI));
		registration.addRecipes(SqueezerRecipeCategory.TYPE, filter(getRecipes(SqueezerRecipe.RECIPES), IJEIRecipe::listInJEI));
		registration.addRecipes(FermenterRecipeCategory.TYPE, filter(getRecipes(FermenterRecipe.RECIPES), IJEIRecipe::listInJEI));
		registration.addRecipes(RefineryRecipeCategory.TYPE, filter(getRecipes(RefineryRecipe.RECIPES), IJEIRecipe::listInJEI));
		registration.addRecipes(ArcFurnaceRecipeCategory.TYPE_RECYCLING, filter(getRecipes(ArcFurnaceRecipe.RECIPES), input -> input instanceof ArcRecyclingRecipe&&input.listInJEI()));
		registration.addRecipes(ArcFurnaceRecipeCategory.TYPE, filter(getRecipes(ArcFurnaceRecipe.RECIPES), input -> !(input instanceof ArcRecyclingRecipe)&&input.listInJEI()));
		registration.addRecipes(BottlingMachineRecipeCategory.TYPE, filter(getRecipes(BottlingMachineRecipe.RECIPES), IJEIRecipe::listInJEI));
		registration.addRecipes(BottlingMachineRecipeCategory.TYPE, getFluidBucketRecipes());
		registration.addRecipes(MixerRecipeCategory.TYPE, filter(getRecipes(MixerRecipe.RECIPES), IJEIRecipe::listInJEI));
	}

	private <T> List<T> filter(List<T> in, Predicate<T> include)
	{
		return in.stream().filter(include).toList();
	}

	private <T extends Recipe<?>> List<T> getRecipes(CachedRecipeList<T> cachedList)
	{
		return List.copyOf(cachedList.getRecipes(Minecraft.getInstance().level));
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration)
	{
		registration.addRecipeTransferHandler(new AssemblerRecipeTransferHandler(registration.getTransferHelper()), RecipeTypes.CRAFTING);
		registration.addRecipeTransferHandler(CraftingTableContainer.class, RecipeTypes.CRAFTING, 1, 9, 10, 18+36);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
	{
		registration.addRecipeCatalyst(new ItemStack(Multiblocks.ASSEMBLER), RecipeTypes.CRAFTING);

		registration.addRecipeCatalyst(new ItemStack(Multiblocks.COKE_OVEN), CokeOvenRecipeCategory.TYPE);
		registration.addRecipeCatalyst(new ItemStack(Multiblocks.ALLOY_SMELTER), AlloySmelterRecipeCategory.TYPE);
		registration.addRecipeCatalyst(new ItemStack(Multiblocks.ADVANCED_BLAST_FURNACE), BlastFurnaceRecipeCategory.TYPE, BlastFurnaceFuelCategory.TYPE);
		registration.addRecipeCatalyst(new ItemStack(Multiblocks.BLAST_FURNACE), BlastFurnaceRecipeCategory.TYPE, BlastFurnaceFuelCategory.TYPE);
		registration.addRecipeCatalyst(new ItemStack(MetalDevices.CLOCHE), ClocheRecipeCategory.TYPE);
		registration.addRecipeCatalyst(new ItemStack(Multiblocks.METAL_PRESS), MetalPressRecipeCategory.TYPE);
		registration.addRecipeCatalyst(new ItemStack(Multiblocks.CRUSHER), CrusherRecipeCategory.TYPE);
		registration.addRecipeCatalyst(new ItemStack(Multiblocks.SAWMILL), SawmillRecipeCategory.TYPE);
		registration.addRecipeCatalyst(new ItemStack(WoodenDevices.WORKBENCH), WorkbenchRecipeCategory.TYPE);
		registration.addRecipeCatalyst(new ItemStack(Multiblocks.AUTO_WORKBENCH), WorkbenchRecipeCategory.TYPE);
		registration.addRecipeCatalyst(new ItemStack(Multiblocks.SQUEEZER), SqueezerRecipeCategory.TYPE);
		registration.addRecipeCatalyst(new ItemStack(Multiblocks.FERMENTER), FermenterRecipeCategory.TYPE);
		registration.addRecipeCatalyst(new ItemStack(Multiblocks.REFINERY), RefineryRecipeCategory.TYPE);
		registration.addRecipeCatalyst(new ItemStack(Multiblocks.ARC_FURNACE), ArcFurnaceRecipeCategory.TYPE, ArcFurnaceRecipeCategory.TYPE_RECYCLING);
		registration.addRecipeCatalyst(new ItemStack(Multiblocks.BOTTLING_MACHINE), BottlingMachineRecipeCategory.TYPE);
		registration.addRecipeCatalyst(new ItemStack(Multiblocks.MIXER), MixerRecipeCategory.TYPE);
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration)
	{
		registration.addRecipeClickArea(CokeOvenScreen.class, 58, 36, 11, 13, CokeOvenRecipeCategory.TYPE);
		registration.addRecipeClickArea(AlloySmelterScreen.class, 84, 35, 22, 16, AlloySmelterRecipeCategory.TYPE);
		registration.addRecipeClickArea(BlastFurnaceScreen.class, 76, 35, 22, 15, BlastFurnaceRecipeCategory.TYPE, BlastFurnaceFuelCategory.TYPE);

		registration.addRecipeClickArea(SqueezerScreen.class, 90, 19, 20, 33, SqueezerRecipeCategory.TYPE);
		registration.addRecipeClickArea(FermenterScreen.class, 90, 19, 20, 33, FermenterRecipeCategory.TYPE);
		registration.addRecipeClickArea(RefineryScreen.class, 92, 24, 14, 20, RefineryRecipeCategory.TYPE);
		registration.addRecipeClickArea(ArcFurnaceScreen.class, 81, 38, 23, 35, ArcFurnaceRecipeCategory.TYPE, ArcFurnaceRecipeCategory.TYPE_RECYCLING);
		registration.addRecipeClickArea(MixerScreen.class, 52, 11, 16, 47, MixerRecipeCategory.TYPE);

		registration.addRecipeClickArea(ModWorkbenchScreen.class, 4, 41, 53, 18, WorkbenchRecipeCategory.TYPE);
		registration.addRecipeClickArea(AutoWorkbenchScreen.class, 90, 12, 39, 37, WorkbenchRecipeCategory.TYPE);

		registration.addRecipeClickArea(CraftingTableScreen.class, 88, 31, 28, 23, RecipeTypes.CRAFTING);

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
							List.of(Lazy.of(() -> bucket)),
							IngredientWithSize.of(new ItemStack(Items.BUCKET)),
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