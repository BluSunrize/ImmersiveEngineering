package blusunrize.immersiveengineering.common.util.compat.jei;

import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe.BlastFurnaceFuel;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalMultiblock;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
		subtypeRegistry.registerSubtypeInterpreter(Item.getItemFromBlock(IEContent.blockConveyor), new ISubtypeInterpreter()
		{
			@Nullable
			@Override
			public String getSubtypeInfo(@Nonnull ItemStack itemStack)
			{
				if(!itemStack.isEmpty()&&ItemNBTHelper.hasKey(itemStack, "conveyorType"))
					return ItemNBTHelper.getString(itemStack, "conveyorType");
				return null;
			}
		});
		subtypeRegistry.registerSubtypeInterpreter(IEContent.itemBullet, new ISubtypeInterpreter()
		{
			@Nullable
			@Override
			public String getSubtypeInfo(@Nonnull ItemStack itemStack)
			{
				if(!itemStack.isEmpty()&&itemStack.getMetadata()==2&&ItemNBTHelper.hasKey(itemStack, "bullet"))
					return ItemNBTHelper.getString(itemStack, "bullet");
				return null;
			}
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
		categories.put(CokeOvenRecipe.class, new CokeOvenRecipeCategory(guiHelper));
		categories.put(AlloyRecipe.class, new AlloySmelterRecipeCategory(guiHelper));
		categories.put(BlastFurnaceRecipe.class, new BlastFurnaceRecipeCategory(guiHelper));
		categories.put(BlastFurnaceFuel.class, new BlastFurnaceFuelCategory(guiHelper));
		categories.put(MetalPressRecipe.class, new MetalPressRecipeCategory(guiHelper));
		categories.put(CrusherRecipe.class, new CrusherRecipeCategory(guiHelper));
		categories.put(BlueprintCraftingRecipe.class, new WorkbenchRecipeCategory(guiHelper));
		categories.put(SqueezerRecipe.class, new SqueezerRecipeCategory(guiHelper));
		categories.put(FermenterRecipe.class, new FermenterRecipeCategory(guiHelper));
		categories.put(RefineryRecipe.class, new RefineryRecipeCategory(guiHelper));
		categories.put(ArcFurnaceRecipe.class, new ArcFurnaceRecipeCategory(guiHelper));
		categories.put(BottlingMachineRecipe.class, new BottlingMachineRecipeCategory(guiHelper));
		categories.put(MixerRecipe.class, new MixerRecipeCategory(guiHelper));
		registry.addRecipeCategories(categories.values().toArray(new IRecipeCategory[categories.size()]));
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
		modRegistry.addRecipeCatalyst(new ItemStack(IEContent.blockMetalMultiblock, 1, BlockTypes_MetalMultiblock.ASSEMBLER.getMeta()), VanillaRecipeCategoryUid.CRAFTING);

		for(IERecipeCategory<Object, IRecipeWrapper> cat : categories.values())
		{
			cat.addCatalysts(registryIn);
			modRegistry.handleRecipes(cat.getRecipeClass(), cat, cat.getRecipeCategoryUid());
		}
//		modRegistry.addRecipeHandlers(categories);

		modRegistry.addRecipes(new ArrayList(CokeOvenRecipe.recipeList), "ie.cokeoven");
		modRegistry.addRecipes(new ArrayList(AlloyRecipe.recipeList), "ie.alloysmelter");
		modRegistry.addRecipes(new ArrayList(BlastFurnaceRecipe.recipeList), "ie.blastfurnace");
		modRegistry.addRecipes(new ArrayList(BlastFurnaceRecipe.blastFuels), "ie.blastfurnace.fuel");
		modRegistry.addRecipes(new ArrayList(MetalPressRecipe.recipeList.values()), "ie.metalPress");
		modRegistry.addRecipes(new ArrayList(CrusherRecipe.recipeList), "ie.crusher");
		modRegistry.addRecipes(new ArrayList(BlueprintCraftingRecipe.recipeList.values()), "ie.workbench");
		modRegistry.addRecipes(new ArrayList(SqueezerRecipe.recipeList), "ie.squeezer");
		modRegistry.addRecipes(new ArrayList(FermenterRecipe.recipeList), "ie.fermenter");
		modRegistry.addRecipes(new ArrayList(RefineryRecipe.recipeList), "ie.refinery");
		modRegistry.addRecipes(new ArrayList(ArcFurnaceRecipe.recipeList), "ie.arcFurnace");
		modRegistry.addRecipes(new ArrayList(BottlingMachineRecipe.recipeList), "ie.bottlingMachine");
		modRegistry.addRecipes(new ArrayList(MixerRecipe.recipeList), "ie.mixer");
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
	{
		final IRecipeRegistry registry = jeiRuntime.getRecipeRegistry();
		IECompatModule.jeiAddFunc = recipe ->
		{
			IERecipeCategory factory = getFactory(recipe.getClass());
			if(factory!=null)
				registry.addRecipe(factory.getRecipeWrapper(recipe), factory.getUid());
		};
		IECompatModule.jeiRemoveFunc = recipe ->
		{
			IERecipeCategory factory = getFactory(recipe.getClass());
			if(factory!=null)
				registry.removeRecipe(factory.getRecipeWrapper(recipe), factory.getUid());
		};
	}

	private IERecipeCategory getFactory(Class recipeClass)
	{
		IERecipeCategory factory = this.categories.get(recipeClass);

		if(factory==null&&recipeClass!=Object.class)
			factory = getFactory(recipeClass.getSuperclass());

		return factory;
	}
}