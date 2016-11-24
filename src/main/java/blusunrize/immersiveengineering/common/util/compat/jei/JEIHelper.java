package blusunrize.immersiveengineering.common.util.compat.jei;

import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalMultiblock;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.arcfurnace.ArcFurnaceRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.arcfurnace.ArcFurnaceRecipeWrapper;
import blusunrize.immersiveengineering.common.util.compat.jei.blastfurnace.BlastFurnaceFuelCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.blastfurnace.BlastFurnaceFuelWrapper;
import blusunrize.immersiveengineering.common.util.compat.jei.blastfurnace.BlastFurnaceRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.blastfurnace.BlastFurnaceRecipeWrapper;
import blusunrize.immersiveengineering.common.util.compat.jei.cokeoven.CokeOvenRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.cokeoven.CokeOvenRecipeWrapper;
import blusunrize.immersiveengineering.common.util.compat.jei.crusher.CrusherRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.crusher.CrusherRecipeWrapper;
import blusunrize.immersiveengineering.common.util.compat.jei.fermenter.FermenterRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.fermenter.FermenterRecipeWrapper;
import blusunrize.immersiveengineering.common.util.compat.jei.metalpress.MetalPressRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.metalpress.MetalPressRecipeWrapper;
import blusunrize.immersiveengineering.common.util.compat.jei.refinery.RefineryRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.refinery.RefineryRecipeWrapper;
import blusunrize.immersiveengineering.common.util.compat.jei.squeezer.SqueezerRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.squeezer.SqueezerRecipeWrapper;
import blusunrize.immersiveengineering.common.util.compat.jei.workbench.WorkbenchRecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.workbench.WorkbenchRecipeWrapper;
import com.google.common.collect.Lists;
import mezz.jei.api.*;
import mezz.jei.api.ISubtypeRegistry.ISubtypeInterpreter;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@JEIPlugin
public class JEIHelper implements IModPlugin
{
	public static IJeiHelpers jeiHelpers;
	public static IModRegistry modRegistry;

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
		jeiHelpers.getItemBlacklist().addItemToBlacklist(new ItemStack(IEContent.blockCrop,1,OreDictionary.WILDCARD_VALUE));
		jeiHelpers.getItemBlacklist().addItemToBlacklist(new ItemStack(IEContent.itemFakeIcons,1,OreDictionary.WILDCARD_VALUE));
		jeiHelpers.getItemBlacklist().addItemToBlacklist(new ItemStack(IEContent.blockStoneDevice,1,OreDictionary.WILDCARD_VALUE));
		jeiHelpers.getItemBlacklist().addItemToBlacklist(new ItemStack(IEContent.blockMetalMultiblock,1,OreDictionary.WILDCARD_VALUE));

		modRegistry.getRecipeTransferRegistry().addRecipeTransferHandler(new AssemblerRecipeTransferHandler(), VanillaRecipeCategoryUid.CRAFTING);
		modRegistry.addRecipeCategoryCraftingItem(new ItemStack(IEContent.blockMetalMultiblock,1,BlockTypes_MetalMultiblock.ASSEMBLER.getMeta()), VanillaRecipeCategoryUid.CRAFTING);

		//Recipes
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
		IERecipeCategory[] categories = {
				new CokeOvenRecipeCategory(guiHelper),
				new BlastFurnaceRecipeCategory(guiHelper),
				new BlastFurnaceFuelCategory(guiHelper),
				new MetalPressRecipeCategory(guiHelper),
				new CrusherRecipeCategory(guiHelper),
				new WorkbenchRecipeCategory(guiHelper),
				new SqueezerRecipeCategory(guiHelper),
				new FermenterRecipeCategory(guiHelper),
				new RefineryRecipeCategory(guiHelper)
		};
		modRegistry.addRecipeCategories(categories);
		modRegistry.addRecipeHandlers(categories);

		modRegistry.addRecipes(CokeOvenRecipeWrapper.getRecipes(jeiHelpers));
		modRegistry.addRecipes(BlastFurnaceRecipeWrapper.getRecipes(jeiHelpers));
		modRegistry.addRecipes(BlastFurnaceFuelWrapper.getRecipes(guiHelper));
		modRegistry.addRecipes(MetalPressRecipeWrapper.getRecipes(jeiHelpers));
		modRegistry.addRecipes(CrusherRecipeWrapper.getRecipes(jeiHelpers));
		modRegistry.addRecipes(WorkbenchRecipeWrapper.getRecipes(jeiHelpers));
		modRegistry.addRecipes(SqueezerRecipeWrapper.getRecipes(jeiHelpers));
		modRegistry.addRecipes(FermenterRecipeWrapper.getRecipes(jeiHelpers));
		modRegistry.addRecipes(RefineryRecipeWrapper.getRecipes(jeiHelpers));

		initArcFurnaceRecipes(modRegistry, guiHelper);
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
	{
	}

	public void initArcFurnaceRecipes(IModRegistry registryIn, IGuiHelper guiHelper)
	{
		List<ArcFurnaceRecipeCategory> categoryList = Lists.newArrayList(new ArcFurnaceRecipeCategory(guiHelper));
		HashMap<String, Class<? extends ArcFurnaceRecipeWrapper>> dynamicSubclasses = new HashMap();
		for(String specialType : ArcFurnaceRecipe.specialRecipeTypes)
		{
			try{
				Class<? extends ArcFurnaceRecipeWrapper> subWrapper = ArcFurnaceRecipeWrapper.createSubWrapper(specialType);
				dynamicSubclasses.put(specialType, subWrapper);
				categoryList.add(new ArcFurnaceRecipeCategory(guiHelper, specialType, subWrapper));
			}catch(Exception e)
			{
				IELogger.error("The dynamic JEI recipe handler for the ArcFurnace - "+specialType+", threw an error! Report this!");
				e.printStackTrace();
			}
		}
		ArcFurnaceRecipeCategory[] categories = categoryList.toArray(new ArcFurnaceRecipeCategory[categoryList.size()]);
		registryIn.addRecipeCategories(categories);
		registryIn.addRecipeHandlers(categories);

		List<ArcFurnaceRecipeWrapper> arcFurnaceRecipes = new ArrayList<>();
		for(ArcFurnaceRecipe r : ArcFurnaceRecipe.recipeList)
		{
			ArcFurnaceRecipeWrapper wrapper = null;
			if(r.specialRecipeType==null)
				wrapper = new ArcFurnaceRecipeWrapper(r);
			else if(dynamicSubclasses.containsKey(r.specialRecipeType))
			{
				try {
					wrapper = dynamicSubclasses.get(r.specialRecipeType).getConstructor(ArcFurnaceRecipe.class).newInstance(r);
				} catch (Exception e) {
					IELogger.error("Adding an ArcFurnaceRecipe: "+r.specialRecipeType+", threw an error! Report this!");
					e.printStackTrace();
				}
			}
			if(wrapper!=null)
				arcFurnaceRecipes.add(wrapper);
		}
		registryIn.addRecipes(arcFurnaceRecipes);
	}
}