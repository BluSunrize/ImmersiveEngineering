package blusunrize.immersiveengineering.client.nei;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMetalPress;
import blusunrize.immersiveengineering.common.util.Utils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.oredict.OreDictionary;

public class NEIMetalPressHandler extends TemplateRecipeHandler
{
	public class CachedMetalPressRecipe extends CachedRecipe
	{
		PositionedStack input;
		PositionedStack mould;
		PositionedStack output;
		public int energy;
		public CachedMetalPressRecipe(MetalPressRecipe recipe)
		{
			Object in = recipe.input;
			if(in instanceof String)
				in = OreDictionary.getOres((String)in);
			if(in instanceof List)
				in = new ArrayList<ItemStack>((List)in);
			for(ItemStack s : (ArrayList<ItemStack>)in)
				s.stackSize = recipe.inputSize;
			input = new PositionedStack(in, 44, 9);
			mould = new PositionedStack(recipe.mold.oreID!=-1?OreDictionary.getOres(OreDictionary.getOreName(recipe.mold.oreID)):recipe.mold.stack, 75, 9);
			output = new PositionedStack(recipe.output, 107,9);
			energy = recipe.energy;
		}
		@Override
		public PositionedStack getOtherStack()
		{
			return mould;
		}
		@Override
		public List<PositionedStack> getIngredients()
		{
			input.setPermutationToRender(NEIMetalPressHandler.this.cycleticks/20 % input.items.length);
			return Arrays.asList(input);
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
		transferRects.add(new RecipeTransferRect(new Rectangle(66,21, 32,28), "ieMetalPress"));
	}
	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if(outputId == getOverlayIdentifier())
		{
			for(MetalPressRecipe r : MetalPressRecipe.recipeList.values())
				if(r!=null)
					this.arecipes.add(new CachedMetalPressRecipe(r));
		}
		else
			super.loadCraftingRecipes(outputId, results);
	}
	@Override
	public String getRecipeName()
	{
		return StatCollector.translateToLocal("tile.ImmersiveEngineering.metalMultiblock.metalPress.name");
	}
	@Override
	public String getGuiTexture()
	{
		return "textures/gui/container/furnace.png";
	}
	@Override
	public String getOverlayIdentifier()
	{
		return "ieMetalPress";
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
			for(MetalPressRecipe r : MetalPressRecipe.recipeList.values())
				if(r!=null && Utils.stackMatchesObject(result, r.output))
					this.arecipes.add(new CachedMetalPressRecipe(r));
	}
	@Override
	public void loadUsageRecipes(ItemStack ingredient)
	{
		if(ingredient!=null)
			for(MetalPressRecipe r : MetalPressRecipe.recipeList.values())
				if(r!=null && (Utils.stackMatchesObject(ingredient, r.input) || r.mold.equals(ApiUtils.createComparableItemStack(ingredient))))
					this.arecipes.add(new CachedMetalPressRecipe(r));
	}

	@Override
	public void drawBackground(int recipe)
	{
		CachedMetalPressRecipe r = (CachedMetalPressRecipe) this.arecipes.get(recipe%arecipes.size());
		if(r!=null)
		{
			try{

			GL11.glColor4f(1, 1, 1, 1);

			GL11.glPushMatrix();
			GL11.glTranslatef(32, 25, 100);
			GL11.glRotatef(-25, 1, 0, 0);
			GL11.glRotatef(-120, 0, 1, 0);
			GL11.glScalef(12, -12, 12);
			TileEntityMetalPress tile = new TileEntityMetalPress();
			tile.pos=4;
			tile.formed=true;
			ClientUtils.bindAtlas(0);
			ClientUtils.tes().startDrawingQuads();
			ClientUtils.handleStaticTileRenderer(tile, false);
			ClientUtils.tes().draw();
			TileEntityRendererDispatcher.instance.renderTileEntityAt(tile, 0.0D, 0.0D, 0.0D, 0.0F);
			GL11.glPopMatrix();

			ClientUtils.drawSlot(r.input.relx,r.input.rely, 16,16);
			ClientUtils.drawSlot(r.mould.relx,r.mould.rely,16,16);
			ClientUtils.drawSlot(r.output.relx,r.output.rely, 20,20);
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	
	}

}