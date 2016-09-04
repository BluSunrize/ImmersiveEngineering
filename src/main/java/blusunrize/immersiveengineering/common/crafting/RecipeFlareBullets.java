package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import java.util.List;

public class RecipeFlareBullets implements IRecipe
{
	@Override
	public boolean matches(InventoryCrafting inv, World world)
	{
		ItemStack bullet = null;
		List<ItemStack> list = Lists.newArrayList();
		for(int i=0;i<inv.getSizeInventory();i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(stackInSlot!=null)
			{
				if(bullet == null && IEContent.itemBullet.equals(stackInSlot.getItem()) && "flare".equals(ItemNBTHelper.getString(stackInSlot, "bullet")))
					bullet = stackInSlot;
				else if(Utils.isDye(stackInSlot))
					list.add(stackInSlot);
				else
					return false;
			}
		}
		return bullet != null && !list.isEmpty();
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv)
	{
		int[] colourArray = new int[3];
		int j = 0;
		int totalColourSets = 0;
		ItemStack bullet = null;
		for(int i=0;i<inv.getSizeInventory();i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(stackInSlot!=null)
				if(bullet == null && IEContent.itemBullet.equals(stackInSlot.getItem()) && "flare".equals(ItemNBTHelper.getString(stackInSlot, "bullet")))
				{
					bullet = stackInSlot;

					int colour = ((IColouredItem)bullet.getItem()).getColourForIEItem(bullet, 1);
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
		}
		if(bullet!=null)
		{
			ItemStack newBullet = Utils.copyStackWithAmount(bullet, 1);

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
			ItemNBTHelper.setInt(newBullet, "flareColour", newColour);
			return newBullet;
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
		return BulletHandler.getBulletStack("flare");
	}

	@Override
	public ItemStack[] getRemainingItems(InventoryCrafting inv)
	{
		return ForgeHooks.defaultRecipeGetRemainingItems(inv);
	}
}