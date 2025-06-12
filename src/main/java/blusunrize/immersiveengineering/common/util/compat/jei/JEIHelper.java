/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.IEApiDataComponents;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorType;
import blusunrize.immersiveengineering.client.gui.*;
import blusunrize.immersiveengineering.common.gui.CraftingTableMenu;
import blusunrize.immersiveengineering.common.items.bullets.IEBullets;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDevices;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import blusunrize.immersiveengineering.common.util.IELogger;
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
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JeiPlugin
public class JEIHelper implements IModPlugin
{
	private static final ResourceLocation UID = IEApi.ieLoc("main");
	public static final ResourceLocation JEI_GUI = IEApi.ieLoc("textures/gui/jei_elements.png");
	public static IDrawableStatic slotDrawable;
	public static IRecipeSlotRichTooltipCallback fluidTooltipCallback = new IEFluidTooltipCallback();

	@Override
	public ResourceLocation getPluginUid()
	{
		return UID;
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration subtypeRegistry)
	{
		subtypeRegistry.registerSubtypeInterpreter(
				Misc.BLUEPRINT.asItem(),
				makeInterpreter(
						IEApiDataComponents::getBlueprintType,
						s -> s
				)
		);
		subtypeRegistry.registerSubtypeInterpreter(
				Misc.POTION_BUCKET.asItem(),
				makeInterpreter(
						stack -> stack.get(DataComponents.POTION_CONTENTS),
						p -> p.potion().map(Holder::getRegisteredName).orElse("")
				)
		);
		for(IConveyorType<?> conveyor : ConveyorHandler.getConveyorTypes())
			subtypeRegistry.registerSubtypeInterpreter(
					ConveyorHandler.getBlock(conveyor).asItem(),
					makeInterpreter(
							stack -> stack.getOrDefault(IEDataComponents.DEFAULT_COVER, Blocks.AIR),
							Block::getDescriptionId
					)
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
				MixerRecipeCategory.getDefault(guiHelper),
				MixerRecipeCategory.getPotions(guiHelper),
				BottlingMachineRecipeCategory.getDefault(guiHelper),
				BottlingMachineRecipeCategory.getPotions(guiHelper),
				BottlingMachineRecipeCategory.getBuckets(guiHelper)
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
		registration.addRecipes(JEIRecipeTypes.METAL_PRESS, getFilteredAndSorted(MetalPressRecipe.STANDARD_RECIPES, IJEIRecipe::listInJEI, compareInRecipe(o -> o.mold.getDescriptionId())));
		registration.addRecipes(JEIRecipeTypes.CRUSHER, getFiltered(CrusherRecipe.RECIPES, IJEIRecipe::listInJEI));
		registration.addRecipes(JEIRecipeTypes.SAWMILL, getFiltered(SawmillRecipe.RECIPES, IJEIRecipe::listInJEI));
		registration.addRecipes(JEIRecipeTypes.BLUEPRINT, getFilteredAndSorted(BlueprintCraftingRecipe.RECIPES, IJEIRecipe::listInJEI, compareInRecipe(o -> o.blueprintCategory)));
		registration.addRecipes(JEIRecipeTypes.SQUEEZER, getFiltered(SqueezerRecipe.RECIPES, IJEIRecipe::listInJEI));
		registration.addRecipes(JEIRecipeTypes.FERMENTER, getFiltered(FermenterRecipe.RECIPES, IJEIRecipe::listInJEI));
		registration.addRecipes(JEIRecipeTypes.REFINERY, getFiltered(RefineryRecipe.RECIPES, IJEIRecipe::listInJEI));
		registration.addRecipes(JEIRecipeTypes.ARC_FURNACE_RECYCLING, getFilteredAndSorted(ArcFurnaceRecipe.RECIPES, input -> input.isSpecialType(ArcRecyclingRecipe.SPECIAL_TYPE)&&input.listInJEI(), compareIDs()));
		registration.addRecipes(JEIRecipeTypes.ARC_FURNACE, getFilteredAndSorted(ArcFurnaceRecipe.RECIPES, input -> input.isNotSpecialType()&&input.listInJEI(), compareIDs()));
		getPartitioned(MixerRecipe.RECIPES, r -> {
			if(r.getFluidOutputs().stream().anyMatch(s -> s.is(IETags.fluidPotion)))
				return JEIRecipeTypes.MIXER_POTIONS;
			else
				return JEIRecipeTypes.MIXER;
		}, compareIDs()).forEach(registration::addRecipes);
		getPartitioned(BottlingMachineRecipe.RECIPES, r -> {
			if(r.getItemOutputs().stream().anyMatch(s -> s.is(Tags.Items.POTIONS)||s.is(BulletHandler.getBulletItem(IEBullets.POTION))))
				return JEIRecipeTypes.BOTTLING_MACHINE_POTIONS;
			else
				return JEIRecipeTypes.BOTTLING_MACHINE;
		}, compareIDs()).forEach(registration::addRecipes);
		registration.addRecipes(JEIRecipeTypes.BOTTLING_MACHINE_BUCKETS, getFluidBucketRecipes());
	}

	private <T extends Recipe<?>> List<RecipeHolder<T>> getRecipes(CachedRecipeList<T> cachedList)
	{
		return getFiltered(cachedList, $ -> true);
	}

	private <T extends Recipe<?>> List<RecipeHolder<T>> getFiltered(CachedRecipeList<T> cachedList, Predicate<T> include)
	{
		return getFilteredAndSorted(cachedList, include, null);
	}

	private <T extends Recipe<?>> List<RecipeHolder<T>> getFilteredAndSorted(CachedRecipeList<T> cachedList, Predicate<T> include, @Nullable Comparator<RecipeHolder<T>> sorting)
	{
		Stream<RecipeHolder<T>> ret = cachedList.getRecipes(Minecraft.getInstance().level).stream()
				.filter(h -> include.test(h.value()));
		if(sorting!=null)
			ret = ret.sorted(sorting);
		return ret.toList();
	}

	private <T extends MultiblockRecipe> Map<RecipeType<RecipeHolder<T>>, List<RecipeHolder<T>>> getPartitioned(
			CachedRecipeList<T> cachedList, Function<T, RecipeType<RecipeHolder<T>>> grouping, Comparator<RecipeHolder<T>> sorting
	)
	{
		return cachedList.getRecipes(Minecraft.getInstance().level).stream()
				.filter(h -> h.value().listInJEI()) // filter to JEI visible
				.sorted(sorting)
				.collect(Collectors.groupingBy(h -> grouping.apply(h.value()))); // group with function
	}

	private <T extends Recipe<?>, C extends Comparable<? super C>> Comparator<RecipeHolder<T>> compareIDs()
	{
		return (h1, h2) -> h1.id().compareNamespaced(h2.id());
	}

	private <T extends Recipe<?>, C extends Comparable<? super C>> Comparator<RecipeHolder<T>> compareInRecipe(Function<? super T, C> keyExtractor)
	{
		return (h1, h2) -> {
			int ret = keyExtractor.apply(h1.value()).compareTo(keyExtractor.apply(h2.value()));
			return ret!=0?ret: h1.id().compareNamespaced(h2.id());
		};
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
		registration.addRecipeCatalyst(IEMultiblockLogic.BOTTLING_MACHINE.iconStack(), JEIRecipeTypes.BOTTLING_MACHINE, JEIRecipeTypes.BOTTLING_MACHINE_POTIONS);
		registration.addRecipeCatalyst(IEMultiblockLogic.MIXER.iconStack(), JEIRecipeTypes.MIXER, JEIRecipeTypes.MIXER_POTIONS);
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
		registration.addRecipeClickArea(MixerScreen.class, 52, 11, 16, 47, JEIRecipeTypes.MIXER, JEIRecipeTypes.MIXER_POTIONS);

		registration.addRecipeClickArea(ModWorkbenchScreen.class, 4, 41, 53, 18, JEIRecipeTypes.BLUEPRINT);
		registration.addRecipeClickArea(AutoWorkbenchScreen.class, 90, 12, 39, 37, JEIRecipeTypes.BLUEPRINT);

		registration.addRecipeClickArea(CraftingTableScreen.class, 88, 31, 28, 23, RecipeTypes.CRAFTING);

		registration.addGhostIngredientHandler(IEContainerScreen.class, new IEGhostItemHandler());
		registration.addGhostIngredientHandler(FluidSorterScreen.class, new FluidSorterGhostHandler());
	}

	private List<RecipeHolder<BottlingMachineRecipe>> getFluidBucketRecipes()
	{
		return BuiltInRegistries.FLUID.holders()
				.filter(holder -> holder.value().isSource(holder.value().defaultFluidState()))
				.filter(holder -> !holder.value().getBucket().getDefaultInstance().isEmpty())
				.map(holder -> {
					ItemStack bucket = holder.value().getBucket().getDefaultInstance();
					ResourceLocation key = holder.key().location();
					return new RecipeHolder<>(
							IEApi.ieLoc("jei_bucket_"+key.getNamespace()+"_"+key.getPath()),
							new BottlingMachineRecipe(
									new TagOutputList(new TagOutput(bucket)),
									IngredientWithSize.of(new ItemStack(Items.BUCKET)),
									SizedFluidIngredient.of(holder.value(), 1000)
							)
					);
				}).toList();
	}

	private <T> ISubtypeInterpreter<ItemStack> makeInterpreter(
			Function<ItemStack, T> componentGetter,
			Function<T, String> legacyStringGetter
	)
	{
		return new ISubtypeInterpreter<ItemStack>()
		{

			@Override
			public @Nullable Object getSubtypeData(ItemStack itemStack, UidContext uidContext)
			{
				return componentGetter.apply(itemStack);
			}

			// deprecated for future removal?
			@Override
			public String getLegacyStringSubtypeInfo(ItemStack itemStack, UidContext uidContext)
			{
				return componentGetter.andThen(legacyStringGetter).apply(itemStack);
			}
		};
	}
}