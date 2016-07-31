package blusunrize.immersiveengineering.common.crafting;

import java.util.List;

import blusunrize.immersiveengineering.common.items.IEItemInterfaces;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import com.google.common.collect.Lists;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class RecipeEarmuffs implements IRecipe
{
	@Override
	public boolean matches(InventoryCrafting inv, World world)
	{
		ItemStack earmuffs = null;
		ItemStack armor = null;
		List<ItemStack> list = Lists.newArrayList();
		for(int i=0;i<inv.getSizeInventory();i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(stackInSlot!=null)
				if(earmuffs==null && IEContent.itemEarmuffs.equals(stackInSlot.getItem()))
					earmuffs = stackInSlot;
				else if(armor==null && stackInSlot.getItem() instanceof ItemArmor && ((ItemArmor)stackInSlot.getItem()).getEquipmentSlot()== EntityEquipmentSlot.HEAD && !IEContent.itemEarmuffs.equals(stackInSlot.getItem()))
					armor = stackInSlot;
				else if(Utils.isDye(stackInSlot))
					list.add(stackInSlot);
				else
					return false;
		}
		if(earmuffs!=null && (armor!=null||!list.isEmpty()))
			return true;
		else if(armor!=null && ItemNBTHelper.hasKey(armor, "IE:Earmuffs") && earmuffs==null && list.isEmpty())
			return true;
		return false;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv)
	{
		ItemStack earmuffs = null;
		ItemStack armor = null;
		int[] colourArray = new int[3];
		int j = 0;
		int totalColourSets = 0;
		for(int i=0;i<inv.getSizeInventory();i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(stackInSlot!=null)
				if(earmuffs==null && IEContent.itemEarmuffs.equals(stackInSlot.getItem()))
				{
					earmuffs = stackInSlot;
					int colour = ((IColouredItem)earmuffs.getItem()).getColourForIEItem(earmuffs, 0);
					float r = (float)(colour >> 16 & 255) / 255.0F;
					float g = (float)(colour >> 8 & 255) / 255.0F;
					float b = (float)(colour & 255) / 255.0F;
					j = (int)((float)j + Math.max(r, Math.max(g, b)) * 255.0F);
					colourArray[0] = (int)((float)colourArray[0] + r * 255.0F);
					colourArray[1] = (int)((float)colourArray[1] + g * 255.0F);
					colourArray[2] = (int)((float)colourArray[2] + b * 255.0F);
					++totalColourSets;
				}
				else if(Utils.isDye(stackInSlot))
				{
					float[] afloat = EntitySheep.getDyeRgb(EnumDyeColor.byDyeDamage(Utils.getDye(stackInSlot)));
					int r = (int)(afloat[0] * 255.0F);
					int g = (int)(afloat[1] * 255.0F);
					int b = (int)(afloat[2] * 255.0F);
					j += Math.max(r, Math.max(g, b));
					colourArray[0] += r;
					colourArray[1] += g;
					colourArray[2] += b;
					++totalColourSets;
				}
				else if(armor==null && stackInSlot.getItem() instanceof ItemArmor && ((ItemArmor)stackInSlot.getItem()).armorType==EntityEquipmentSlot.HEAD && !IEContent.itemEarmuffs.equals(stackInSlot.getItem()))
					armor = stackInSlot;
		}

		if(earmuffs!=null)
		{
			if(totalColourSets>1)
			{
				int r = colourArray[0] / totalColourSets;
				int g = colourArray[1] / totalColourSets;
				int b = colourArray[2] / totalColourSets;
				float colourMod = (float)j / (float)totalColourSets;
				float highestColour = (float)Math.max(r, Math.max(g, b));
				r = (int)((float)r * colourMod / highestColour);
				g = (int)((float)g * colourMod / highestColour);
				b = (int)((float)b * colourMod / highestColour);
				int newColour = (r << 8) + g;
				newColour = (newColour << 8) + b;
				ItemNBTHelper.setInt(earmuffs, "IE:EarmuffColour", newColour);
			}
			ItemStack output;
			if(armor!=null)
			{
				output = armor.copy();
				ItemNBTHelper.setItemStack(output, "IE:Earmuffs", earmuffs.copy());
			}
			else
				output = earmuffs.copy();
			return output;
		}
		else if(armor!=null && ItemNBTHelper.hasKey(armor, "IE:Earmuffs"))
		{
			ItemStack output = armor.copy();
			ItemNBTHelper.remove(output, "IE:Earmuffs");
			return output;
		}
		return null;
	}

	@Override
	public int getRecipeSize()
	{
		return 10;
	}
	@Override
	public ItemStack getRecipeOutput()
	{
		return new ItemStack(IEContent.itemBullet,1,10);
	}

	@Override
	public ItemStack[] getRemainingItems(InventoryCrafting inv)
	{
		ItemStack[] remaining = ForgeHooks.defaultRecipeGetRemainingItems(inv);
		for(int i=0;i<remaining.length;i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(stackInSlot!=null && ItemNBTHelper.hasKey(stackInSlot, "IE:Earmuffs"))
				remaining[i] = ItemNBTHelper.getItemStack(stackInSlot, "IE:Earmuffs");
		}
		return remaining;
	}
}