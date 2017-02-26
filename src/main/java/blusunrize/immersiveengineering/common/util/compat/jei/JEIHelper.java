package blusunrize.immersiveengineering.common.util.compat.jei;

import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalMultiblock;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
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
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

@JEIPlugin
public class JEIHelper implements IModPlugin
{
	public static IJeiHelpers jeiHelpers;
	public static IModRegistry modRegistry;
	public static IDrawable slotDrawable;

	@Override
	public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry)
	{
		//NBT Ignorance
		subtypeRegistry.registerNbtInterpreter(Item.getItemFromBlock(IEContent.blockConveyor), new ISubtypeInterpreter()
		{
			@Nullable
			@Override
			public String getSubtypeInfo(@Nonnull ItemStack itemStack)
			{
				if(itemStack != null && ItemNBTHelper.hasKey(itemStack, "conveyorType"))
					return ItemNBTHelper.getString(itemStack, "conveyorType");
				return null;
			}
		});
		subtypeRegistry.registerNbtInterpreter(IEContent.itemBullet, new ISubtypeInterpreter()
		{
			@Nullable
			@Override
			public String getSubtypeInfo(@Nonnull ItemStack itemStack)
			{
				if(itemStack != null && itemStack.getMetadata() == 2 && ItemNBTHelper.hasKey(itemStack, "bullet"))
					return ItemNBTHelper.getString(itemStack, "bullet");
				return null;
			}
		});
	}

	@Override
	public void registerIngredients(IModIngredientRegistration registry)
	{
	}

	@Override
	public void register(IModRegistry registryIn)
	{
		modRegistry = registryIn;
		jeiHelpers = modRegistry.getJeiHelpers();
		//Blacklist
		jeiHelpers.getIngredientBlacklist().addIngredientToBlacklist(new ItemStack(IEContent.blockCrop,1,OreDictionary.WILDCARD_VALUE));
		jeiHelpers.getIngredientBlacklist().addIngredientToBlacklist(new ItemStack(IEContent.itemFakeIcons,1,OreDictionary.WILDCARD_VALUE));
		jeiHelpers.getIngredientBlacklist().addIngredientToBlacklist(new ItemStack(IEContent.blockStoneDevice,1,OreDictionary.WILDCARD_VALUE));
		jeiHelpers.getIngredientBlacklist().addIngredientToBlacklist(new ItemStack(IEContent.blockMetalMultiblock,1,OreDictionary.WILDCARD_VALUE));

		modRegistry.getRecipeTransferRegistry().addRecipeTransferHandler(new AssemblerRecipeTransferHandler(), VanillaRecipeCategoryUid.CRAFTING);
		modRegistry.addRecipeCategoryCraftingItem(new ItemStack(IEContent.blockMetalMultiblock,1,BlockTypes_MetalMultiblock.ASSEMBLER.getMeta()), VanillaRecipeCategoryUid.CRAFTING);

		//Recipes
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
		slotDrawable = guiHelper.getSlotDrawable();
		IERecipeCategory[] categories = {
				new CokeOvenRecipeCategory(guiHelper),
				new BlastFurnaceRecipeCategory(guiHelper),
				new BlastFurnaceFuelCategory(guiHelper),
				new MetalPressRecipeCategory(guiHelper),
				new CrusherRecipeCategory(guiHelper),
				new WorkbenchRecipeCategory(guiHelper),
				new SqueezerRecipeCategory(guiHelper),
				new FermenterRecipeCategory(guiHelper),
				new RefineryRecipeCategory(guiHelper),
				new ArcFurnaceRecipeCategory(guiHelper),
				new BottlingMachineRecipeCategory(guiHelper),
				new MixerRecipeCategory(guiHelper)
		};
		modRegistry.addRecipeCategories(categories);
		modRegistry.addRecipeHandlers(categories);

		modRegistry.addRecipes(new ArrayList(CokeOvenRecipe.recipeList));
		modRegistry.addRecipes(new ArrayList(BlastFurnaceRecipe.recipeList));
		modRegistry.addRecipes(new ArrayList(BlastFurnaceRecipe.blastFuels));
		modRegistry.addRecipes(new ArrayList(MetalPressRecipe.recipeList.values()));
		modRegistry.addRecipes(new ArrayList(CrusherRecipe.recipeList));
		modRegistry.addRecipes(new ArrayList(BlueprintCraftingRecipe.recipeList.values()));
		modRegistry.addRecipes(new ArrayList(SqueezerRecipe.recipeList));
		modRegistry.addRecipes(new ArrayList(FermenterRecipe.recipeList));
		modRegistry.addRecipes(new ArrayList(RefineryRecipe.recipeList));
		modRegistry.addRecipes(new ArrayList(ArcFurnaceRecipe.recipeList));
		modRegistry.addRecipes(new ArrayList(BottlingMachineRecipe.recipeList));
		modRegistry.addRecipes(new ArrayList(MixerRecipe.recipeList));
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
	{
	}
}