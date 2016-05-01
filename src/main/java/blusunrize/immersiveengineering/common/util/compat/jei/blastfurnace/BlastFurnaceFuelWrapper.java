package blusunrize.immersiveengineering.common.util.compat.jei.blastfurnace;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

public class BlastFurnaceFuelWrapper extends BlankRecipeWrapper
{
	private final List<ItemStack> fuel;
	private final String burnTime;
	private final IDrawableAnimated flame;

	public BlastFurnaceFuelWrapper(IGuiHelper guiHelper, List<ItemStack> fuel, int burnTime)
	{
		this.fuel = fuel;
		this.burnTime = StatCollector.translateToLocalFormatted("gui.jei.category.fuel.burnTime", burnTime);

		ResourceLocation furnaceBackgroundLocation = new ResourceLocation("minecraft", "textures/gui/container/furnace.png");
		IDrawableStatic flameDrawable = guiHelper.createDrawable(furnaceBackgroundLocation, 176, 0, 14, 14);
		this.flame = guiHelper.createAnimatedDrawable(flameDrawable, burnTime, IDrawableAnimated.StartDirection.TOP, true);
	}

	@Override
	public List<ItemStack> getInputs()
	{
		return fuel;
	}

	@Override
	public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight)
	{
		minecraft.fontRendererObj.drawString(burnTime, 24, 12, Color.gray.getRGB());
	}

	@Override
	public void drawAnimations(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight)
	{
		flame.draw(minecraft, 2, 0);
	}

	public static List<BlastFurnaceFuelWrapper> getRecipes(IGuiHelper guiHelper)
	{
		List<BlastFurnaceFuelWrapper> recipes = new ArrayList<>();
		for(Map.Entry<Object, Integer> e : BlastFurnaceRecipe.blastFuels.entrySet())
		{
			Object o = e.getKey();
			List<ItemStack> list;
			if(o instanceof ItemStack)
				list = Arrays.asList((ItemStack)o);
			else
				list = (List<ItemStack>)o;
			recipes.add(new BlastFurnaceFuelWrapper(guiHelper, list, e.getValue()));
		}
		return recipes;
	}
}