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
import blusunrize.immersiveengineering.common.gui.CraftingTableMenu;
import blusunrize.immersiveengineering.common.items.EngineersBlueprintItem;
import blusunrize.immersiveengineering.common.items.PotionBucketItem;
import blusunrize.immersiveengineering.common.items.ShaderItem;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDevices;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.alloysmelter.AlloySmelterRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.arcfurnace.ArcFurnaceRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.blastfurnace.BlastFurnaceFuelCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.blastfurnace.BlastFurnaceRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.bottlingmachine.BottlingMachineRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.cloche.ClocheFertilizerCategory;
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
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
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
				VanillaTypes.ITEM_STACK,
				Misc.BLUEPRINT.asItem(),
				(stack, $) -> EngineersBlueprintItem.getCategory(stack)
		);
		subtypeRegistry.registerSubtypeInterpreter(
				VanillaTypes.ITEM_STACK, Misc.POTION_BUCKET.asItem(), (stack, $) -> PotionBucketItem.getPotion(stack).getName("")
		);
		subtypeRegistry.registerSubtypeInterpreter(
				VanillaTypes.ITEM_STACK, Misc.SHADER.asItem(), (stack, $) -> ItemNBTHelper.getString(stack, ShaderItem.SHADER_NAME_KEY)
		);
		for(IConveyorType<?> conveyor : ConveyorHandler.getConveyorTypes())
			subtypeRegistry.registerSubtypeInterpreter(
					VanillaTypes.ITEM_STACK,
					ConveyorHandler.getBlock(conveyor).asItem(),
					(stack, $) -> ItemNBTHelper.getString(stack, ConveyorBlock.DEFAULT_COVER)
			);
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
				new ClocheFertilizerCategory(guiHelper),
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
	public void registerRecipes(IRecipeRegistration registration)
	{
		IELogger.info("Adding recipes to JEI!!");
		registration.addRecipes(JEIRecipeTypes.COKE_OVEN, getRecipes(CokeOvenRecipe.RECIPES));
		registration.addRecipes(JEIRecipeTypes.ALLOY, getRecipes(AlloyRecipe.RECIPES));
		registration.addRecipes(JEIRecipeTypes.BLAST_FURNACE, getRecipes(BlastFurnaceRecipe.RECIPES));
		registration.addRecipes(JEIRecipeTypes.BLAST_FUEL, getRecipes(BlastFurnaceFuel.RECIPES));
		registration.addRecipes(JEIRecipeTypes.CLOCHE, getRecipes(ClocheRecipe.RECIPES));
		registration.addRecipes(JEIRecipeTypes.CLOCHE_FERTILIZER, getRecipes(ClocheFertilizer.RECIPES));
		registration.addRecipes(JEIRecipeTypes.METAL_PRESS, getFiltered(MetalPressRecipe.STANDARD_RECIPES, IJEIRecipe::listInJEI));
		registration.addRecipes(JEIRecipeTypes.CRUSHER, getFiltered(CrusherRecipe.RECIPES, IJEIRecipe::listInJEI));
		registration.addRecipes(JEIRecipeTypes.SAWMILL, getFiltered(SawmillRecipe.RECIPES, IJEIRecipe::listInJEI));
		registration.addRecipes(JEIRecipeTypes.BLUEPRINT, getFiltered(BlueprintCraftingRecipe.RECIPES, IJEIRecipe::listInJEI));
		registration.addRecipes(JEIRecipeTypes.SQUEEZER, getFiltered(SqueezerRecipe.RECIPES, IJEIRecipe::listInJEI));
		registration.addRecipes(JEIRecipeTypes.FERMENTER, getFiltered(FermenterRecipe.RECIPES, IJEIRecipe::listInJEI));
		registration.addRecipes(JEIRecipeTypes.REFINERY, getFiltered(RefineryRecipe.RECIPES, IJEIRecipe::listInJEI));
		registration.addRecipes(JEIRecipeTypes.ARC_FURNACE_RECYCLING, getFiltered(ArcFurnaceRecipe.RECIPES, input -> input instanceof ArcRecyclingRecipe&&input.listInJEI()));
		registration.addRecipes(JEIRecipeTypes.ARC_FURNACE, getFiltered(ArcFurnaceRecipe.RECIPES, input -> !(input instanceof ArcRecyclingRecipe)&&input.listInJEI()));
		registration.addRecipes(JEIRecipeTypes.BOTTLING_MACHINE, getFiltered(BottlingMachineRecipe.RECIPES, IJEIRecipe::listInJEI));
		registration.addRecipes(JEIRecipeTypes.BOTTLING_MACHINE, getFluidBucketRecipes());
		registration.addRecipes(JEIRecipeTypes.MIXER, getFiltered(MixerRecipe.RECIPES, IJEIRecipe::listInJEI));
	}

	private <T extends Recipe<?>> List<T> getRecipes(CachedRecipeList<T> cachedList)
	{
		return getFiltered(cachedList, $ -> true);
	}

	private <T extends Recipe<?>> List<T> getFiltered(CachedRecipeList<T> cachedList, Predicate<T> include)
	{
		return cachedList.getRecipes(Minecraft.getInstance().level).stream()
				.filter(include)
				.toList();
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration)
	{
		registration.addRecipeTransferHandler(new AssemblerRecipeTransferHandler(registration.getTransferHelper()), RecipeTypes.CRAFTING);
		registration.addRecipeTransferHandler(
				CraftingTableMenu.class, IEMenuTypes.CRAFTING_TABLE.getType(), RecipeTypes.CRAFTING,
				1, 9, 10, 18+36
		);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
	{
		registration.addRecipeCatalyst(IEMultiblockLogic.ASSEMBLER.iconStack(), RecipeTypes.CRAFTING);

		registration.addRecipeCatalyst(IEMultiblockLogic.COKE_OVEN.iconStack(), JEIRecipeTypes.COKE_OVEN);
		registration.addRecipeCatalyst(IEMultiblockLogic.ALLOY_SMELTER.iconStack(), JEIRecipeTypes.ALLOY);
		registration.addRecipeCatalyst(IEMultiblockLogic.ADV_BLAST_FURNACE.iconStack(), JEIRecipeTypes.BLAST_FURNACE, JEIRecipeTypes.BLAST_FUEL);
		registration.addRecipeCatalyst(IEMultiblockLogic.BLAST_FURNACE.iconStack(), JEIRecipeTypes.BLAST_FURNACE, JEIRecipeTypes.BLAST_FUEL);
		registration.addRecipeCatalyst(new ItemStack(MetalDevices.CLOCHE), JEIRecipeTypes.CLOCHE);
		registration.addRecipeCatalyst(new ItemStack(MetalDevices.CLOCHE), JEIRecipeTypes.CLOCHE_FERTILIZER);
		registration.addRecipeCatalyst(IEMultiblockLogic.METAL_PRESS.iconStack(), JEIRecipeTypes.METAL_PRESS);
		registration.addRecipeCatalyst(IEMultiblockLogic.CRUSHER.iconStack(), JEIRecipeTypes.CRUSHER);
		registration.addRecipeCatalyst(IEMultiblockLogic.SAWMILL.iconStack(), JEIRecipeTypes.SAWMILL);
		registration.addRecipeCatalyst(new ItemStack(WoodenDevices.WORKBENCH), JEIRecipeTypes.BLUEPRINT);
		registration.addRecipeCatalyst(IEMultiblockLogic.AUTO_WORKBENCH.iconStack(), JEIRecipeTypes.BLUEPRINT);
		registration.addRecipeCatalyst(IEMultiblockLogic.SQUEEZER.iconStack(), JEIRecipeTypes.SQUEEZER);
		registration.addRecipeCatalyst(IEMultiblockLogic.FERMENTER.iconStack(), JEIRecipeTypes.FERMENTER);
		registration.addRecipeCatalyst(IEMultiblockLogic.REFINERY.iconStack(), JEIRecipeTypes.REFINERY);
		registration.addRecipeCatalyst(IEMultiblockLogic.ARC_FURNACE.iconStack(), JEIRecipeTypes.ARC_FURNACE, JEIRecipeTypes.ARC_FURNACE_RECYCLING);
		registration.addRecipeCatalyst(IEMultiblockLogic.BOTTLING_MACHINE.iconStack(), JEIRecipeTypes.BOTTLING_MACHINE);
		registration.addRecipeCatalyst(IEMultiblockLogic.MIXER.iconStack(), JEIRecipeTypes.MIXER);
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration)
	{
		registration.addRecipeClickArea(CokeOvenScreen.class, 58, 36, 11, 13, JEIRecipeTypes.COKE_OVEN);
		registration.addRecipeClickArea(AlloySmelterScreen.class, 84, 35, 22, 16, JEIRecipeTypes.ALLOY);
		registration.addRecipeClickArea(BlastFurnaceScreen.class, 76, 35, 22, 15, JEIRecipeTypes.BLAST_FURNACE, JEIRecipeTypes.BLAST_FUEL);

		registration.addRecipeClickArea(SqueezerScreen.class, 90, 19, 20, 33, JEIRecipeTypes.SQUEEZER);
		registration.addRecipeClickArea(FermenterScreen.class, 90, 19, 20, 33, JEIRecipeTypes.FERMENTER);
		registration.addRecipeClickArea(RefineryScreen.class, 92, 24, 14, 20, JEIRecipeTypes.REFINERY);
		registration.addRecipeClickArea(ArcFurnaceScreen.class, 81, 38, 23, 35, JEIRecipeTypes.ARC_FURNACE, JEIRecipeTypes.ARC_FURNACE_RECYCLING);
		registration.addRecipeClickArea(MixerScreen.class, 52, 11, 16, 47, JEIRecipeTypes.MIXER);

		registration.addRecipeClickArea(ModWorkbenchScreen.class, 4, 41, 53, 18, JEIRecipeTypes.BLUEPRINT);
		registration.addRecipeClickArea(AutoWorkbenchScreen.class, 90, 12, 39, 37, JEIRecipeTypes.BLUEPRINT);

		registration.addRecipeClickArea(CraftingTableScreen.class, 88, 31, 28, 23, RecipeTypes.CRAFTING);

		registration.addGhostIngredientHandler(IEContainerScreen.class, new IEGhostItemHandler());
		registration.addGhostIngredientHandler(FluidSorterScreen.class, new FluidSorterGhostHandler());
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
							new ResourceLocation(Lib.MODID, "jei_bucket_"+BuiltInRegistries.FLUID.getKey(f).getPath()),
							List.of(Lazy.of(() -> bucket)),
							IngredientWithSize.of(new ItemStack(Items.BUCKET)),
							new FluidTagInput(tag.get(), 1000)
					));
			}
		return recipes;
	}
}