package blusunrize.immersiveengineering.client.nei;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.oredict.OreDictionary;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.CrusherRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import blusunrize.immersiveengineering.common.util.Utils;
import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class NEICrusherHandler extends TemplateRecipeHandler
{
	public class CachedCrusherRecipe extends CachedRecipe
	{
		PositionedStack input;
		PositionedStack output;
		PositionedStack secondary;
		public float secondaryChance;
		public int energy;
		public CachedCrusherRecipe(CrusherRecipe recipe)
		{
			Object in = recipe.input;
			if(in instanceof String)
				in = OreDictionary.getOres((String)in);
			input = new PositionedStack(in, 84, 0);
			output = new PositionedStack(recipe.output, 84,40);
			if(recipe.secondaryOutput!=null)
			{
				output.relx-=10;
				secondary = new PositionedStack(recipe.secondaryOutput, 94,40);
				secondaryChance = recipe.secondaryChance;
			}
			energy = recipe.energy;
		}
		@Override
		public PositionedStack getOtherStack()
		{
			return secondary;
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
		//		changeTexture(getGuiTexture());
		
		CachedCrusherRecipe r = (CachedCrusherRecipe) this.arecipes.get(recipe%arecipes.size());
		if(r!=null)
		{
			GuiDraw.drawRect(r.input.relx-1, r.input.rely-1, 17,17, 0xff373737);
			GuiDraw.drawRect(r.input.relx, r.input.rely, 17,17, 0xffffffff);
			GuiDraw.drawRect(r.input.relx, r.input.rely, 16,16, 0xff8b8b8b);
			GuiDraw.drawRect(r.output.relx-1, r.output.rely-1, 17,17, 0xff373737);
			GuiDraw.drawRect(r.output.relx, r.output.rely, 17,17, 0xffffffff);
			GuiDraw.drawRect(r.output.relx, r.output.rely, 16,16, 0xff8b8b8b);
			if(r.secondary!=null)
			{
			GuiDraw.drawRect(r.secondary.relx-1, r.secondary.rely-1, 17,17, 0xff373737);
			GuiDraw.drawRect(r.secondary.relx, r.secondary.rely, 17,17, 0xffffffff);
			GuiDraw.drawRect(r.secondary.relx, r.secondary.rely, 16,16, 0xff8b8b8b);
			}
			
			String s = r.energy+" RF";
			ClientUtils.font().drawString(s, 140-ClientUtils.font().getStringWidth(s)/2,20, 0x777777, false);
			if(r.secondary!=null)
			{
				String chance = Utils.formatDouble(r.secondaryChance*100, "0.0")+"%";
//				r.secondaryChance*100+"%";
				ClientUtils.font().drawString(chance, 120,46, 0x777777, false);
			}
			GL11.glColor4f(1, 1, 1, 1);
			changeTexture("textures/gui/container/furnace.png");
			GL11.glRotatef(90, 0, 0, 1);
			drawTexturedModalRect(18,-100, 82,35, 20,15);
			GL11.glRotatef(-90, 0, 0, 1);

			GL11.glTranslatef(40, 40, 100);
			GL11.glRotatef(-15, 1, 0, 0);
			GL11.glRotatef(200, 0, 1, 0);
			GL11.glScalef(10, -10, 10);

			TileEntityCrusher gen = new TileEntityCrusher();
			gen.pos=17;
			gen.formed=true;
			TileEntityRendererDispatcher.instance.renderTileEntityAt(gen, 0.0D, 0.0D, 0.0D, 0.0F);
		}
		GL11.glPopMatrix();
	}

}