/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemEngineersBlueprint extends ItemIEBase//ItemUpgradeableTool
{
	public ItemEngineersBlueprint()
	{
		super("blueprint", 1, null);
	}

	@Override
	public String getTranslationKey(ItemStack stack)
	{
		return this.getTranslationKey();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag)
	{
		String key = ItemNBTHelper.getString(stack, "blueprint");
		if(key!=null&&!key.isEmpty()&&BlueprintCraftingRecipe.blueprintCategories.contains(key))
		{
			String formatKey = Lib.DESC_INFO+"blueprint."+key;
			String formatted = I18n.format(formatKey);
			list.add(formatKey.equals(formatted)?key: formatted);
			if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)||Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
			{
				list.add(I18n.format(Lib.DESC_INFO+"blueprint.creates1"));
				BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(key);
				if(recipes.length > 0)
					for(int i = 0; i < recipes.length; i++)
						list.add(" "+recipes[i].output.getDisplayName());
			}
			else
				list.add(I18n.format(Lib.DESC_INFO+"blueprint.creates0"));
		}
	}


	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list)
	{
		if(this.isInCreativeTab(tab))
			for(String key : BlueprintCraftingRecipe.blueprintCategories)
			{
				ItemStack stack = new ItemStack(this);
				ItemNBTHelper.setString(stack, "blueprint", key);
				list.add(stack);
			}
	}

	@Nonnull
	public BlueprintCraftingRecipe[] getRecipes(ItemStack stack)
	{
		return BlueprintCraftingRecipe.findRecipes(ItemNBTHelper.getString(stack, "blueprint"));
	}

//	@Override
//	public boolean canModify(ItemStack stack)
//	{
//		return true;
//	}
//
//	@Override
//	public Slot[] getWorkbenchSlots(Container container, ItemStack stack)
//	{
//		IItemHandler inv = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
//		LinkedHashSet<Slot> slots = new LinkedHashSet<Slot>();
//
//		slots.add(new IESlot.BlueprintInput(container, inv, 0, 74, 21, stack));
//		slots.add(new IESlot.BlueprintInput(container, inv, 1, 92, 21, stack));
//		slots.add(new IESlot.BlueprintInput(container, inv, 2, 74, 39, stack));
//		slots.add(new IESlot.BlueprintInput(container, inv, 3, 92, 39, stack));
//		slots.add(new IESlot.BlueprintInput(container, inv, 4, 74, 57, stack));
//		slots.add(new IESlot.BlueprintInput(container, inv, 5, 92, 57, stack));
//
//		BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(ItemNBTHelper.getString(stack, "blueprint"));
//		for(int i = 0; i < recipes.length; i++)
//		{
//			int y = 21+(i < 9?i/3: (-(i-6)/3))*18;
//			slots.add(new IESlot.BlueprintOutput(container, inv, 6+i, 118+(i%3*18), y, stack, recipes[i]));
//		}
//		return slots.toArray(new Slot[slots.size()]);
//	}
//
//	public void updateOutputs(ItemStack stack)
//	{
//		BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(ItemNBTHelper.getString(stack, "blueprint"));
//		IItemHandlerModifiable handler = (IItemHandlerModifiable)stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
//		NonNullList<ItemStack> query = NonNullList.withSize(6, ItemStack.EMPTY);
//		for(int i = 0; i < handler.getSlots(); i++)
//			if(i < 6)
//				query.set(i, handler.getStackInSlot(i));
//			else
//			{
//				handler.setStackInSlot(i, ItemStack.EMPTY);
//				int craftable = recipes[i-6].getMaxCrafted(query);
//				if(craftable > 0)
//					handler.setStackInSlot(i, Utils.copyStackWithAmount(recipes[i-6].output, Math.min(recipes[i-6].output.getCount()*craftable, 64-(64%recipes[i-6].output.getCount()))));
//			}
//	}
//
//	public void reduceInputs(BlueprintCraftingRecipe recipe, ItemStack stack, ItemStack crafted, Container contained)
//	{
//		IItemHandlerModifiable handler = (IItemHandlerModifiable)stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
//		NonNullList<ItemStack> query = NonNullList.withSize(6, ItemStack.EMPTY);
//		for(int i = 0; i < 6; i++)
//			query.set(i, handler.getStackInSlot(i));
//		recipe.consumeInputs(query, crafted.getCount()/recipe.output.getCount());
//		for(int i = 0; i < 6; i++)
//			handler.setStackInSlot(i, query.get(i));
//	}
//
//	@Override
//	public int getSlotCount(ItemStack stack)
//	{
//		return 6+BlueprintCraftingRecipe.findRecipes(ItemNBTHelper.getString(stack, "blueprint")).length;
//	}
//
//	@Override
//	public boolean canTakeFromWorkbench(ItemStack stack)
//	{
//		IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
//		for(int i = 0; i < 6; i++)
//			if(!handler.getStackInSlot(i).isEmpty())
//				return false;
//		return true;
//	}
}