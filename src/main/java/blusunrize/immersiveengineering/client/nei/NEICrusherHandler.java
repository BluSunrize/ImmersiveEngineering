package blusunrize.immersiveengineering.client.nei;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;
import static codechicken.lib.gui.GuiDraw.getMousePosition;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.Utils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class NEICrusherHandler extends TemplateRecipeHandler
{
	public class CachedCrusherRecipe extends CachedRecipe
	{
		PositionedStack input;
		PositionedStack output;
		PositionedStack[] secondary;
		public float[] secondaryChance;
		public int energy;
		public CachedCrusherRecipe(CrusherRecipe recipe)
		{
			Object in = recipe.input;
			input = new PositionedStack(in, 70, 0);

			output = new PositionedStack(recipe.output, 70,40);
			if(recipe.secondaryOutput!=null)
			{
				secondary = new PositionedStack[recipe.secondaryOutput.length];
				secondaryChance = new float[recipe.secondaryOutput.length];
				for(int i=0; i<secondary.length; i++)
				{
					secondary[i] = new PositionedStack(recipe.secondaryOutput[i], 90+i*18,40);
					secondaryChance[i] = recipe.secondaryChance[i];
				}
			}
			energy = recipe.energy;
		}
		@Override
		public List<PositionedStack> getOtherStacks()
		{
			return Arrays.asList(secondary!=null?secondary:new PositionedStack[0]);
		}
		@Override
		public List<PositionedStack> getIngredients()
		{
			return getCycledIngredients(cycleticks/20, Arrays.asList(input));
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
		transferRects.add(new RecipeTransferRect(new Rectangle(4,4, 58,40), "ieCrusher"));
	}
	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if(outputId == getOverlayIdentifier())
		{
			for(CrusherRecipe r : CrusherRecipe.recipeList)
				if(r!=null)
					this.arecipes.add(new CachedCrusherRecipe(r));
		}
		else
			super.loadCraftingRecipes(outputId, results);
	}
	@Override
	public String getRecipeName()
	{
		return StatCollector.translateToLocal("tile.ImmersiveEngineering.metalMultiblock.crusher.name");
	}
	@Override
	public String getGuiTexture()
	{
		return "immersiveengineering:textures/gui/blastFurnace.png";
	}
	@Override
	public String getOverlayIdentifier()
	{
		return "ieCrusher";
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
			for(CrusherRecipe r : CrusherRecipe.recipeList)
				if(r!=null && (Utils.stackMatchesObject(result, r.output)||(r.secondaryOutput!=null&&Utils.stackMatchesObject(result, r.secondaryOutput))))
					this.arecipes.add(new CachedCrusherRecipe(r));
	}
	@Override
	public void loadUsageRecipes(ItemStack ingredient)
	{
		if(ingredient!=null)
		{
			CrusherRecipe r = CrusherRecipe.findRecipe(ingredient);
			if(r!=null)
				this.arecipes.add(new CachedCrusherRecipe(r));
		}
	}

	@Override
	public void drawBackground(int recipe)
	{
		GL11.glPushMatrix();
		GL11.glColor4f(1, 1, 1, 1);

		CachedCrusherRecipe r = (CachedCrusherRecipe) this.arecipes.get(recipe%arecipes.size());
		if(r!=null)
		{
			ClientUtils.drawSlot(r.input.relx,r.input.rely, 16,16);
			ClientUtils.drawSlot(r.output.relx,r.output.rely, 16,16);
			if(r.secondary!=null)
				for(PositionedStack ps : r.secondary)
					ClientUtils.drawSlot(ps.relx, ps.rely, 16,16);

			String s = r.energy+" RF";
			ClientUtils.font().drawString(s, 120-ClientUtils.font().getStringWidth(s)/2,20, 0x777777, false);
			GL11.glColor4f(1, 1, 1, 1);
			changeTexture("textures/gui/container/furnace.png");
			GL11.glRotatef(90, 0, 0, 1);
			drawTexturedModalRect(18,-85, 82,35, 20,15);
			GL11.glRotatef(-90, 0, 0, 1);

			GL11.glTranslatef(40, 40, 100);
			GL11.glRotatef(-15, 1, 0, 0);
			GL11.glRotatef(200, 0, 1, 0);
			GL11.glScalef(10, -10, 10);

			TileEntityCrusher gen = new TileEntityCrusher();
			gen.pos=17;
			gen.formed=true;
			ClientUtils.bindAtlas(0);
			ClientUtils.tes().startDrawingQuads();
			ClientUtils.handleStaticTileRenderer(gen, false);
			ClientUtils.tes().draw();
			TileEntityRendererDispatcher.instance.renderTileEntityAt(gen, 0.0D, 0.0D, 0.0D, 0.0F);
		}
		GL11.glPopMatrix();
	}

	@Override
	public List<String> handleItemTooltip(GuiRecipe gui, ItemStack stack, List<String> currenttip, int recipe)
	{
		Point mouse = getMousePosition();
		Point offset = gui.getRecipePosition(recipe);
		Point relMouse = new Point(mouse.x -(gui.width- 176)/2-offset.x, mouse.y-(gui.height-166)/2-offset.y);
		CachedCrusherRecipe r = (CachedCrusherRecipe) this.arecipes.get(recipe%arecipes.size());
		if(r!=null)
		{
			if(r.secondary!=null)
				for(int i=0; i<r.secondary.length; i++)
					if(new Rectangle(r.secondary[i].relx-1, r.secondary[i].rely-1, 18,18).contains(relMouse) && r.secondary[i].contains(stack)) 
						currenttip.add(String.format("%s %.0f%%", StatCollector.translateToLocal(Lib.DESC_INFO+"chance"),r.secondaryChance[i]*100));
		}
		return currenttip;
	}
}