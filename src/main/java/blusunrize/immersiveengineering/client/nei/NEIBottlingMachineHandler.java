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
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBottlingMachine;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.Utils;
import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class NEIBottlingMachineHandler extends TemplateRecipeHandler
{
	public class CachedBottlingMachineRecipe extends CachedRecipe
	{
		PositionedStack input;
		PositionedStack output;
		FluidStack fluid;
		public CachedBottlingMachineRecipe(BottlingMachineRecipe recipe)
		{
			Object in = recipe.input;
			input = new PositionedStack(in, 46, 8);
			output = new PositionedStack(recipe.output, 105,8);
			fluid = recipe.fluidInput;
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
		transferRects.add(new RecipeTransferRect(new Rectangle(64,8, 38,36), "ieBottlingMachine"));
	}
	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if(outputId == getOverlayIdentifier())
		{
			for(BottlingMachineRecipe r : BottlingMachineRecipe.recipeList)
				if(r!=null)
					this.arecipes.add(new CachedBottlingMachineRecipe(r));
		}
		else
			super.loadCraftingRecipes(outputId, results);
	}
	@Override
	public String getRecipeName()
	{
		return StatCollector.translateToLocal("tile.ImmersiveEngineering.metalMultiblock.bottlingMachine.name");
	}
	@Override
	public String getGuiTexture()
	{
		return "immersiveengineering:textures/gui/fluidProducer.png";
	}
	@Override
	public String getOverlayIdentifier()
	{
		return "ieBottlingMachine";
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
			for(BottlingMachineRecipe r : BottlingMachineRecipe.recipeList)
				if(r!=null && (Utils.stackMatchesObject(result, r.output)))
					this.arecipes.add(new CachedBottlingMachineRecipe(r));
	}
	@Override
	public void loadUsageRecipes(ItemStack ingredient)
	{
		if(ingredient!=null)
		{
			for(BottlingMachineRecipe r : BottlingMachineRecipe.recipeList)
				if(r!=null && Utils.stackMatchesObject(ingredient, r.input))
					this.arecipes.add(new CachedBottlingMachineRecipe(r));
		}
	}

	@Override
	public void drawBackground(int recipe)
	{
		GL11.glPushMatrix();
		GL11.glColor4f(1, 1, 1, 1);
		CachedBottlingMachineRecipe r = (CachedBottlingMachineRecipe) this.arecipes.get(recipe%arecipes.size());
		if(r!=null)
		{
			ClientUtils.drawSlot(r.input.relx,r.input.rely, 16,16);
			ClientUtils.drawSlot(r.output.relx,r.output.rely, 20,20);
			ClientUtils.drawSlot(16,22, 18,50);

			int timer = 30;
			int step = cycleticks%(timer*8)/timer;
			int fluidHeight = 50-(step*5);
			ClientUtils.drawRepeatedFluidIcon(r.fluid.getFluid(), 15,55-fluidHeight, 18,fluidHeight);
			changeTexture(getGuiTexture());
			GL11.glColor4f(.5f, .5f, .5f, 1);
			drawTexturedModalRect(15,5, 179,33, 16,47);
			
			GL11.glColor4f(1, 1, 1, 1);
			changeTexture("textures/gui/container/furnace.png");
			drawTexturedModalRect(74,8, 82,35, 20,16);
			drawTexturedModalRect(74,8, 179,14, (int)((cycleticks%timer)/(float)timer*20),16);
			
			GL11.glTranslatef(89, 50, 100);
			//			GL11.glTranslatef(58, 50, 100);
			GL11.glRotatef(-45, 1, 0, 0);
			GL11.glRotatef(180, 0, 1, 0);
			GL11.glScalef(12, -12, 12);

			TileEntityBottlingMachine tile = new TileEntityBottlingMachine();
			tile.pos=4;
			tile.formed=true;
			ClientUtils.bindAtlas(0);
			ClientUtils.tes().startDrawingQuads();
			ClientUtils.handleStaticTileRenderer(tile, false);
			ClientUtils.tes().draw();
			TileEntityRendererDispatcher.instance.renderTileEntityAt(tile, 0.0D, 0.0D, 0.0D, 0.0F);
		}
		GL11.glPopMatrix();
	}

	@Override
	public List<String> handleItemTooltip(GuiRecipe gui, ItemStack stack, List<String> currenttip, int recipe)
	{
		Point mouse = getMousePosition();
		Point offset = gui.getRecipePosition(recipe);
		Point relMouse = new Point(mouse.x -(gui.width- 176)/2-offset.x, mouse.y-(gui.height-166)/2-offset.y);
		CachedBottlingMachineRecipe r = (CachedBottlingMachineRecipe) this.arecipes.get(recipe%arecipes.size());
		if(r!=null && r.fluid!=null)
		{
			if(new Rectangle(15,5, 18,50).contains(relMouse)) 
			{
				currenttip.add(r.fluid.getLocalizedName());
				currenttip.add("§7"+r.fluid.amount+" mB");
			}
		}
		return currenttip;
	}
}