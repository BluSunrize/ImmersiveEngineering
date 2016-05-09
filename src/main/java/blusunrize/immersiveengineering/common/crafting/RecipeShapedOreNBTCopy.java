package blusunrize.immersiveengineering.common.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class RecipeShapedOreNBTCopy extends ShapedOreRecipe
{
	int targetSlot;
	public RecipeShapedOreNBTCopy(ItemStack result, int targetSlot, Object... recipe)
	{
		super(result, recipe);
		this.targetSlot = targetSlot;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting matrix)
	{
		ItemStack out = output.copy();
		if(matrix.getStackInSlot(targetSlot)!=null && matrix.getStackInSlot(targetSlot).hasTagCompound())
			out.setTagCompound((NBTTagCompound)matrix.getStackInSlot(targetSlot).getTagCompound().copy());
		return out;
	}
}