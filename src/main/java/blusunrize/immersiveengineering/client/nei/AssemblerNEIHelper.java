package blusunrize.immersiveengineering.client.nei;

import java.util.ArrayList;
import java.util.List;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.gui.GuiAssembler;
import blusunrize.immersiveengineering.common.util.network.MessageTileSync;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IStackPositioner;
import codechicken.nei.recipe.DefaultOverlayHandler;
import codechicken.nei.recipe.IRecipeHandler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.oredict.OreDictionary;

public class AssemblerNEIHelper
{
	public static class StackPositioner implements IStackPositioner
	{
		@Override
		public ArrayList<PositionedStack> positionStacks(ArrayList<PositionedStack> list)
		{
			return list;
		}
	}

	public static class OverlayHandler extends DefaultOverlayHandler
	{
		@Override
		public void overlayRecipe(GuiContainer gui, IRecipeHandler recipe, int recipeIndex, boolean shift)
		{
			if(gui instanceof GuiAssembler)
			{
				for(int i=0; i<((GuiAssembler)gui).tile.patterns.length; i++)
					if(((GuiAssembler)gui).tile.patterns[i].recipe==null)
					{
						NBTTagList tagList = new NBTTagList();
						int offX = -16 + 58*i;
						List<PositionedStack> ingredients = recipe.getIngredientStacks(recipeIndex);
						List<PositionedStack> foundIngredients = new ArrayList<PositionedStack>();
						for(PositionedStack ps : ingredients)
						{
							boolean found = false;
							for(Slot slot : (List<Slot>)gui.inventorySlots.inventorySlots)
							{
								for(ItemStack s : ps.items)
									if(OreDictionary.itemMatches(s, slot.getStack(), false))
									{
										ps.item = s;
										foundIngredients.add(ps);
										found = true;
										break;
									}
								if(found)
									break;
							}
						}

						for(PositionedStack ps : foundIngredients)
						{
							int newX = ps.relx+offX;
							int newY = ps.rely+1;
							for(int j=0; j<9; j++)
							{
								Slot slot = (Slot)gui.inventorySlots.inventorySlots.get(i*10+j);
								if(slot.xDisplayPosition==newX && slot.yDisplayPosition==newY)
								{
									slot.putStack(ps.item);
									NBTTagCompound itemTag = ps.item.writeToNBT(new NBTTagCompound());
									itemTag.setInteger("slot", j);
									tagList.appendTag(itemTag);
								}
							}
						}
						NBTTagCompound tag = new NBTTagCompound();
						tag.setTag("patternSync", tagList);
						tag.setInteger("recipe", i);
						ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(((GuiAssembler) gui).tile, tag));
						break;
					}
			}
		}
		@Override
		public boolean canMoveFrom(Slot slot, GuiContainer gui)
		{
			return super.canMoveFrom(slot, gui);
		}
		//		@Override
		//		public void overlayRecipe(GuiContainer gui, IRecipeHandler recipe, int recipeIndex, boolean shift)
		//		{
		//			List<PositionedStack> ingredients = recipe.getIngredientStacks(recipeIndex);
		//			List<DistributedIngred> ingredStacks = getPermutationIngredients(ingredients);
		//			if(!clearIngredients(gui, ingredients))
		//				return;
		//			findInventoryQuantities(gui, ingredStacks);
		//			List<IngredientDistribution> assignedIngredients = assignIngredients(ingredients, ingredStacks);
		//			if(assignedIngredients == null)
		//				return;
		//			assignIngredSlots(gui, ingredients, assignedIngredients);
		//			int quantity = calculateRecipeQuantity(assignedIngredients);
		//			if(quantity != 0)
		//				moveIngredients(gui, assignedIngredients, quantity);
		//		}
	}
}