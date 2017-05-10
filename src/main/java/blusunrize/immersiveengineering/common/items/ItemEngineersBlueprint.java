package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.gui.ContainerModWorkbench;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.LinkedHashSet;
import java.util.List;

public class ItemEngineersBlueprint extends ItemUpgradeableTool
{
	public ItemEngineersBlueprint()
	{
		super("blueprint", 1, null);
	}

	public static ItemStack getTypedBlueprint(String type)
	{
		ItemStack stack = new ItemStack(IEContent.itemBlueprint,1,0);
		ItemNBTHelper.setString(stack, "blueprint", type);
		return stack;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		return this.getUnlocalizedName();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		String key = ItemNBTHelper.getString(stack,"blueprint");
		if(key != null && !key.isEmpty() && BlueprintCraftingRecipe.blueprintCategories.contains(key))
		{
			list.add(I18n.format(Lib.DESC_INFO + "blueprint." + key));
			if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
			{
				list.add(I18n.format(Lib.DESC_INFO + "blueprint.creates1"));
				BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(key);
				if(recipes.length > 0)
					for(int i = 0; i < recipes.length; i++)
						list.add(" " + recipes[i].output.getDisplayName());
			} else
				list.add(I18n.format(Lib.DESC_INFO + "blueprint.creates0"));
		}
	}


	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, NonNullList<ItemStack> list)
	{
		for(String key : BlueprintCraftingRecipe.blueprintCategories)
		{
			ItemStack stack = new ItemStack(this);
			ItemNBTHelper.setString(stack, "blueprint", key);
			list.add(stack);
		}
	}

	@Override
	public boolean canModify(ItemStack stack)
	{
		return true;
	}
	@Override
	public Slot[] getWorkbenchSlots(Container container, ItemStack stack, IInventory invItem)
	{
		LinkedHashSet<Slot> slots = new LinkedHashSet<Slot>();

		slots.add(new IESlot.BlueprintInput(container, invItem, 0, 74, 21, stack));
		slots.add(new IESlot.BlueprintInput(container, invItem, 1, 92, 21, stack));
		slots.add(new IESlot.BlueprintInput(container, invItem, 2, 74, 39, stack));
		slots.add(new IESlot.BlueprintInput(container, invItem, 3, 92, 39, stack));
		slots.add(new IESlot.BlueprintInput(container, invItem, 4, 74, 57, stack));
		slots.add(new IESlot.BlueprintInput(container, invItem, 5, 92, 57, stack));

		BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(ItemNBTHelper.getString(stack,"blueprint"));
		for(int i=0; i<recipes.length; i++)
		{
			int y = 21 + (i < 9 ? i / 3 : (-(i - 6) / 3)) * 18;
			slots.add(new IESlot.BlueprintOutput(container, invItem, 6 + i, 118 + (i % 3 * 18), y, stack, recipes[i]));
		}
		return slots.toArray(new Slot[slots.size()]);
	}

	public void updateOutputs(ItemStack stack)
	{
		BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(ItemNBTHelper.getString(stack,"blueprint"));
		NonNullList<ItemStack> stored = this.getContainedItems(stack);
		NonNullList<ItemStack> query = NonNullList.withSize(6, ItemStack.EMPTY);
		for(int i=0; i<stored.size(); i++)
			if(i<6)
				query.set(i, stored.get(i));
			else
			{
				stored.set(i, ItemStack.EMPTY);
				int craftable = recipes[i-6].getMaxCrafted(query);
				if(craftable>0)
					stored.set(i, Utils.copyStackWithAmount(recipes[i-6].output, Math.min(recipes[i-6].output.getCount() * craftable, 64)));
			}
		this.setContainedItems(stack, stored);
	}

	public void reduceInputs(BlueprintCraftingRecipe recipe, ItemStack stack, ItemStack crafted, Container contained)
	{
		NonNullList<ItemStack> stored = this.getContainedItems(stack);
		NonNullList<ItemStack> query = NonNullList.withSize(6, ItemStack.EMPTY);
		for(int i=0; i<6; i++)
			query.set(i, stored.get(i));
		recipe.consumeInputs(query, crafted.getCount()/recipe.output.getCount());
		for(int i=0; i<6; i++)
			stored.set(i, query.get(i));
		this.setContainedItems(stack, stored);
		if (contained instanceof ContainerModWorkbench)
		{
			ContainerModWorkbench work = (ContainerModWorkbench) contained;
			if(work.toolInv!=null)
			{
				work.toolInv.stackList = query;
				work.onCraftMatrixChanged(work.toolInv);
			}
		}
	}

	@Override
	public int getInternalSlots(ItemStack stack)
	{
		return 6 + BlueprintCraftingRecipe.findRecipes(ItemNBTHelper.getString(stack, "blueprint")).length;
	}

	@Override
	public boolean canTakeFromWorkbench(ItemStack stack)
	{
		NonNullList<ItemStack> stored = this.getContainedItems(stack);
		for(int i=0; i<6; i++)
			if(!stored.get(i).isEmpty())
				return false;
		return true;
	}
}