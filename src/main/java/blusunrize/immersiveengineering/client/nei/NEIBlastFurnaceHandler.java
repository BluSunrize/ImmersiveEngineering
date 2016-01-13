package blusunrize.immersiveengineering.client.nei;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.oredict.OreDictionary;

public class NEIBlastFurnaceHandler extends TemplateRecipeHandler
{
	public class CachedBlastFurnaceRecipe extends CachedRecipe
	{
		PositionedStack input;
		PositionedStack output;
		public int time;
		List<PositionedStack> fuels = new ArrayList();
		public CachedBlastFurnaceRecipe(BlastFurnaceRecipe recipe)
		{
			Object in = recipe.input;
			if(in instanceof String)
				in = OreDictionary.getOres((String)in);
			input = new PositionedStack(in, 47, 9);
			output = new PositionedStack(recipe.output, 107,27);
			time = recipe.time;

			for(Object fuel : BlastFurnaceRecipe.blastFuels.keySet())
				fuels.add( new PositionedStack(fuel, 47,45) );
		}
		@Override
		public PositionedStack getOtherStack()
		{
			return fuels.get((NEIBlastFurnaceHandler.this.cycleticks/20)%fuels.size());
		}
		@Override
		public PositionedStack getIngredient()
		{
			return input;
		}
		@Override
		public PositionedStack getResult()
		{
			return output;
		}
	}

	@Override
	public void loadTransferRects()
	{
		transferRects.add(new RecipeTransferRect(new Rectangle(66,21, 32,28), "ieBlastFurnace"));
	}
	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if(outputId == getOverlayIdentifier())
		{
			for(BlastFurnaceRecipe r : BlastFurnaceRecipe.recipeList)
				if(r!=null)
					this.arecipes.add(new CachedBlastFurnaceRecipe(r));
		}
		else
			super.loadCraftingRecipes(outputId, results);
	}
	@Override
	public String getRecipeName()
	{
		return StatCollector.translateToLocal("desc.ImmersiveEngineering.name.blastFurnace");
	}
	@Override
	public String getGuiTexture()
	{
		return "immersiveengineering:textures/gui/blastFurnace.png";
	}
	@Override
	public String getOverlayIdentifier()
	{
		return "ieBlastFurnace";
	}
	@Override
	public int recipiesPerPage()
	{
		return 2;
	}
	@Override
	public void loadCraftingRecipes(ItemStack result)
	{
		if(result!=null)
			for(BlastFurnaceRecipe r : BlastFurnaceRecipe.recipeList)
				if(r!=null && Utils.stackMatchesObject(result, r.output))
					this.arecipes.add(new CachedBlastFurnaceRecipe(r));
	}
	@Override
	public void loadUsageRecipes(ItemStack ingredient)
	{
		if(ingredient!=null)
		{
			BlastFurnaceRecipe r = BlastFurnaceRecipe.findRecipe(ingredient);
			if(r!=null)
				this.arecipes.add(new CachedBlastFurnaceRecipe(r));
			else if(BlastFurnaceRecipe.isValidBlastFuel(ingredient))
			{
				for(BlastFurnaceRecipe r1 : BlastFurnaceRecipe.recipeList)
					if(r1!=null)
						this.arecipes.add(new CachedBlastFurnaceRecipe(r1));
			}
		}
	}

	@Override
	public List<String> handleItemTooltip(GuiRecipe gui, ItemStack stack, List<String> currenttip, int recipe)
	{
		if(recipe%2==0 && BlastFurnaceRecipe.isValidBlastFuel(stack))
			currenttip.add(EnumChatFormatting.GRAY+StatCollector.translateToLocalFormatted("desc.ImmersiveEngineering.info.blastFuelTime", BlastFurnaceRecipe.getBlastFuelTime(stack)));
		return currenttip;
	}
	@Override
	public void drawBackground(int recipe)
	{
		GL11.glColor4f(1, 1, 1, 1);
		changeTexture(getGuiTexture());
		drawTexturedModalRect(-5,0, 0,8, 176, 68);
		CachedBlastFurnaceRecipe r = (CachedBlastFurnaceRecipe) this.arecipes.get(recipe%arecipes.size());
		if(r!=null)
		{
			String s = r.time+" Ticks";
			ClientUtils.font().drawString(s, 115-ClientUtils.font().getStringWidth(s)/2,53, 0xaaaaaa, true);
		}
	}

}