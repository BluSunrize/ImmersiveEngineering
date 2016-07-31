package blusunrize.immersiveengineering.common.items;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.common.gui.ContainerModWorkbench;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
		list.add(I18n.format(Lib.DESC_INFO+"blueprint."+key));
		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)||Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
		{
			list.add(I18n.format(Lib.DESC_INFO+"blueprint.creates1"));
			BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(key);
			if(recipes.length>0)
				for(int i=0; i<recipes.length; i++)
					list.add(" "+recipes[i].output.getDisplayName());
		}
		else
			list.add(I18n.format(Lib.DESC_INFO+"blueprint.creates0"));
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

		slots.add( new IESlot.BlueprintInput(container, invItem, 0, 80,21, stack));
		slots.add( new IESlot.BlueprintInput(container, invItem, 1, 98,21, stack));
		slots.add( new IESlot.BlueprintInput(container, invItem, 2, 80,39, stack));
		slots.add( new IESlot.BlueprintInput(container, invItem, 3, 98,39, stack));
		slots.add( new IESlot.BlueprintInput(container, invItem, 4, 80,57, stack));
		slots.add( new IESlot.BlueprintInput(container, invItem, 5, 98,57, stack));

		BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(ItemNBTHelper.getString(stack,"blueprint"));
		for(int i=0; i<recipes.length; i++)
			slots.add( new IESlot.BlueprintOutput(container, invItem, 6+i, 134+(i%2*18), 57-(i/2 *18), stack, recipes[i]));
		return slots.toArray(new Slot[slots.size()]);
	}

	public void updateOutputs(ItemStack stack)
	{
		BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(ItemNBTHelper.getString(stack,"blueprint"));
		ItemStack[] stored = this.getContainedItems(stack);
		ItemStack[] query = new ItemStack[6];
		for(int i=0; i<stored.length; i++)
			if(i<6)
				query[i] = stored[i];
			else
			{
				stored[i] = null;
				int craftable = recipes[i-6].getMaxCrafted(query);
				if(craftable>0)
					stored[i] = Utils.copyStackWithAmount(recipes[i-6].output, Math.min(recipes[i-6].output.stackSize*craftable, 64));
			}
		this.setContainedItems(stack, stored);
	}

	public void reduceInputs(BlueprintCraftingRecipe recipe, ItemStack stack, ItemStack crafted, Container contained)
	{
		ItemStack[] stored = this.getContainedItems(stack);
		ItemStack[] query = new ItemStack[6];
		for(int i=0; i<6; i++)
			query[i] = stored[i];
		recipe.consumeInputs(query, crafted.stackSize/recipe.output.stackSize);
		for(int i=0; i<6; i++)
			stored[i] = query[i];
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
		ItemStack[] stored = this.getContainedItems(stack);
		for(int i=0; i<6; i++)
			if(stored[i]!=null)
				return false;
		return true;
	}
}