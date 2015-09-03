package blusunrize.immersiveengineering.client.nei;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.GuiArcFurnace;
import blusunrize.immersiveengineering.common.util.Utils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class NEIArcFurnaceHandler extends TemplateRecipeHandler
{
	public class CachedArcFurnaceRecipe extends CachedRecipe
	{
		PositionedStack[] inputs;
		PositionedStack output;
		PositionedStack slag;
		public int time;
		public int energy;
		public CachedArcFurnaceRecipe(ArcFurnaceRecipe recipe)
		{
			inputs = new PositionedStack[recipe.additives.length+1];
			if(recipe.input!=null)
				inputs[0] = new PositionedStack(recipe.input, 28, 0);
			for(int i=0; i<recipe.additives.length; i++)
				if(recipe.additives[i]!=null)
					inputs[i+1] = new PositionedStack(recipe.additives[i], 20+i%2*18, 24+i/2*18);
			if(recipe.output!=null)
				output = new PositionedStack(recipe.output, 122,16);
			if(recipe.slag!=null)
				slag = new PositionedStack(recipe.slag, 122,36);
			time = recipe.time;
			energy = recipe.energyPerTick;
		}
		@Override
		public PositionedStack getOtherStack()
		{
			return slag;
		}
		@Override
		public List<PositionedStack> getIngredients()
		{
			return getCycledIngredients(cycleticks/20, Arrays.asList(inputs));
		}
		@Override
		public PositionedStack getResult()
		{
			return output;
		}
	}

	@Override
	public Class<? extends GuiContainer> getGuiClass()
	{
		return GuiArcFurnace.class;
	}

	@Override
	public void loadTransferRects()
	{
		transferRects.add(new RecipeTransferRect(new Rectangle(76,26, 32,40), "ieArcFurnace"));
	}
	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if(outputId == getOverlayIdentifier())
		{
			for(ArcFurnaceRecipe r : ArcFurnaceRecipe.recipeList)
				if(r!=null && r.input!=null)
					this.arecipes.add(new CachedArcFurnaceRecipe(r));
		}
		else
			super.loadCraftingRecipes(outputId, results);
	}
	@Override
	public String getRecipeName()
	{
		return StatCollector.translateToLocal("tile.ImmersiveEngineering.metalMultiblock.arcFurnace.name");
	}
	@Override
	public String getGuiTexture()
	{
		return "immersiveengineering:textures/gui/arcFurnace.png";
	}
	@Override
	public String getOverlayIdentifier()
	{
		return "ieArcFurnace";
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
			for(ArcFurnaceRecipe r : ArcFurnaceRecipe.recipeList)
				if(r!=null && r.input!=null && (Utils.stackMatchesObject(result, r.output)||(r.slag!=null&&Utils.stackMatchesObject(result, r.slag))))
					this.arecipes.add(new CachedArcFurnaceRecipe(r));
	}
	@Override
	public void loadUsageRecipes(ItemStack ingredient)
	{
		if(ingredient!=null)
			for(ArcFurnaceRecipe r : ArcFurnaceRecipe.recipeList)
				if(r!=null && r.input!=null)
				{
					if(Utils.stackMatchesObject(ingredient, r.input))
						this.arecipes.add(new CachedArcFurnaceRecipe(r));
					else
						for(Object o : r.additives)
							if(Utils.stackMatchesObject(ingredient, o))
								this.arecipes.add(new CachedArcFurnaceRecipe(r));
				}
	}

	@Override
	public void drawBackground(int recipe)
	{
		GL11.glPushMatrix();
		GL11.glColor4f(1, 1, 1, 1);

		CachedArcFurnaceRecipe r = (CachedArcFurnaceRecipe) this.arecipes.get(recipe%arecipes.size());
		if(r!=null)
		{
			ClientUtils.drawSlot(28, 0, 16, 16);
			for(int i=0; i<4; i++)
				ClientUtils.drawSlot(20+i%2*18, 24+i/2*18, 16, 16);
			ClientUtils.drawSlot(r.output.relx, r.output.rely, 16, 16);
			if(r.slag!=null)
				ClientUtils.drawSlot(r.slag.relx, r.slag.rely, 16, 16);

			String s = r.energy+" RF/t";
			ClientUtils.font().drawString(s, 88-ClientUtils.font().getStringWidth(s)/2,32, 0x777777, false);
			s = r.time+" ticks";
			ClientUtils.font().drawString(s, 84-ClientUtils.font().getStringWidth(s)/2,44, 0x777777, false);
			GL11.glColor4f(1, 1, 1, 1);
			changeTexture("textures/gui/container/furnace.png");
			drawTexturedModalRect(72,16, 80,35, 22,16);
			int w = (this.cycleticks/2)%22;
			drawTexturedModalRect(72,16, 177,14, w,16);
		}
		GL11.glPopMatrix();
	}

}