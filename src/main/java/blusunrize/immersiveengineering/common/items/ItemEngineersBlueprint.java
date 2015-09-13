package blusunrize.immersiveengineering.common.items;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;

import org.lwjgl.input.Keyboard;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.common.gui.ContainerModWorkbench;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.gui.InventoryStorageItem;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemEngineersBlueprint extends ItemUpgradeableTool
{
	public ItemEngineersBlueprint()
	{
		super("blueprint", 1, null);
	}

	@Override
	public String[] getSubNames()
	{
		return BlueprintCraftingRecipe.blueprintCategories.toArray(new String[BlueprintCraftingRecipe.blueprintCategories.size()]);
	}
	@Override
	public void registerIcons(IIconRegister ir)
	{
		this.icons[0] = ir.registerIcon("immersiveengineering:blueprint");
	}
	@Override
	public IIcon getIconFromDamage(int meta)
	{
		return icons[0];
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
		String[] sub = getSubNames();
		if(stack.getItemDamage()<sub.length)
		{
			list.add(StatCollector.translateToLocalFormatted(Lib.DESC_INFO+"blueprint."+sub[stack.getItemDamage()]));
			if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)||Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
			{
				list.add(StatCollector.translateToLocal(Lib.DESC_INFO+"blueprint.creates1"));
				BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(sub[stack.getItemDamage()]);
				if(recipes.length>0)
					for(int i=0; i<recipes.length; i++)
						list.add(" "+recipes[i].output.getDisplayName());
			}
			else
				list.add(StatCollector.translateToLocal(Lib.DESC_INFO+"blueprint.creates0"));
		}
	}

	@Override
	public WeightedRandomChestContent getChestGenBase(ChestGenHooks chest, Random random, WeightedRandomChestContent original)
	{
		original.theItemId.func_135074_t();
		if(random.nextDouble()<.125f)
		{
			original.theItemId.setStackDisplayName("Super Special BluPrintz");
			ItemNBTHelper.setLore(original.theItemId, "Congratulations!","You have found an easter egg!");
		}
		return original;
	}

	@Override
	public boolean canModify(ItemStack stack)
	{
		return true;
	}
	@Override
	public Slot[] getWorkbenchSlots(Container container, ItemStack stack, InventoryStorageItem invItem)
	{
		LinkedHashSet<Slot> slots = new LinkedHashSet<Slot>();

		slots.add( new IESlot.BlueprintInput(container, invItem, 0, 80,21, stack));
		slots.add( new IESlot.BlueprintInput(container, invItem, 1, 98,21, stack));
		slots.add( new IESlot.BlueprintInput(container, invItem, 2, 80,39, stack));
		slots.add( new IESlot.BlueprintInput(container, invItem, 3, 98,39, stack));
		slots.add( new IESlot.BlueprintInput(container, invItem, 4, 80,57, stack));
		slots.add( new IESlot.BlueprintInput(container, invItem, 5, 98,57, stack));

		String[] sub = getSubNames();
		if(stack.getItemDamage()<sub.length)
		{
			BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(sub[stack.getItemDamage()]);
			for(int i=0; i<recipes.length; i++)
				slots.add( new IESlot.BlueprintOutput(container, invItem, 6+i, 134+(i%2*18), 57-(i/2 *18), stack, recipes[i]));
		}

		return slots.toArray(new Slot[slots.size()]);
	}

	public void updateOutputs(ItemStack stack)
	{
		String[] sub = getSubNames();
		if(stack.getItemDamage()<sub.length)
		{
			BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(sub[stack.getItemDamage()]);	
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
				work.toolInv.stackList = query;
		}
	}

	@Override
	public int getInternalSlots(ItemStack stack)
	{
		String[] sub = getSubNames();
		if(stack.getItemDamage()<sub.length)
			return 6 + BlueprintCraftingRecipe.findRecipes(sub[stack.getItemDamage()]).length;
		return 6;
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