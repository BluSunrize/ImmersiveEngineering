/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.render.tile.AutoWorkbenchRenderer.BlueprintLines;
import blusunrize.immersiveengineering.common.blocks.wooden.ModWorkbenchTileEntity;
import blusunrize.immersiveengineering.common.items.EngineersBlueprintItem;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;

public class ModWorkbenchRenderer extends TileEntityRenderer<ModWorkbenchTileEntity>
{

	@Override
	public void render(ModWorkbenchTileEntity te, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(te.isDummy()||!te.getWorldNonnull().isBlockLoaded(te.getPos()))
			return;

		GlStateManager.pushMatrix();
		GlStateManager.translated(x+.5, y+.5, z+.5);

		Direction facing = te.getFacing();

		float angle = facing==Direction.NORTH?0: facing==Direction.WEST?90: facing==Direction.EAST?-90: 180;

		GlStateManager.rotatef(angle, 0, 1, 0);

		ItemStack stack = te.getInventory().get(0);
		boolean showIngredients = true;
		if(!stack.isEmpty())
		{
			if(stack.getItem() instanceof EngineersBlueprintItem)
			{
				GlStateManager.pushMatrix();
				double playerDistanceSq = ClientUtils.mc().player.getDistanceSq(new Vec3d(te.getPos()));
				if(playerDistanceSq < 120)
				{
					BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(ItemNBTHelper.getString(stack, "blueprint"));

					float lineWidth = playerDistanceSq < 25?1: playerDistanceSq < 40?.5f: .1f;
					int l = recipes.length;
					int perRow = l > 6?l-3: l > 4?l-2: l==1?2: l==2?3: l;
					GlStateManager.translated(0, .501, 0);
					GlStateManager.rotatef(-90, 1, 0, 0);
					GlStateManager.rotatef(-22.5f, 0, 0, 1);
					GlStateManager.translated(0.39, l > 4?.72: .78, 0);
					float scale = l > 4?.009375f: .012f;
					GlStateManager.scalef(scale, -scale, scale);
					for(int i = 0; i < l; )
					{
						BlueprintCraftingRecipe recipe = recipes[i%recipes.length];
						BlueprintLines blueprint = recipe==null?null: AutoWorkbenchRenderer.getBlueprintDrawable(recipe, te.getWorldNonnull());
						if(blueprint!=null)
						{
							double dX = i < perRow?(.93725/scale-perRow*16.6)+i*16.6: (.70375/scale-i%perRow*16.6);
							double dY = i < perRow?0: -.15625;
							GlStateManager.translated(dX, dY/scale, 0);

							//Width depends on distance
							GlStateManager.disableCull();
							GlStateManager.disableTexture();
							GlStateManager.enableBlend();
							float texScale = blueprint.textureScale/16f;
							GlStateManager.scalef(1/texScale, 1/texScale, 1/texScale);
							GlStateManager.color3f(1, 1, 1);
							blueprint.draw(lineWidth);
							GlStateManager.scalef(texScale, texScale, texScale);
							GlStateManager.enableAlphaTest();
							GlStateManager.enableTexture();
							GlStateManager.enableCull();
							GlStateManager.translated(-dX, -dY/scale, 0);
							i++;
						}
					}
				}
				GlStateManager.popMatrix();
			}
			else
			{
				showIngredients = false;
				GlStateManager.pushMatrix();
				GlStateManager.disableLighting();
				GlStateManager.translated(0, .5625, 0);

				GlStateManager.rotatef(180, 0, 1, 0);
				GlStateManager.rotatef(90, 1, 0, 0);
				GlStateManager.translated(-.875, 0, 0);
				GlStateManager.scalef(.75f, .75f, .75f);
				{
					try
					{
						ClientUtils.mc().getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
					} catch(Exception e)
					{
						e.printStackTrace();
					}
				}
				GlStateManager.enableLighting();
				GlStateManager.popMatrix();
			}
		}
		if(showIngredients)
		{
			GlStateManager.disableLighting();
			for(int i = 1; i < te.getInventory().size(); i++)
			{
				double dX, dZ;
				if(i < 5)
				{
					dX = -.5+(i==2?-.0625: i==4?.03215: 0);
					dZ = i*.25-.625;
				}
				else
				{
					dX = -1.25;
					dZ = -.125+(i-5)*-.25;
				}

				stack = te.getInventory().get(i);
				if(!stack.isEmpty())
				{
					GlStateManager.pushMatrix();
					GlStateManager.rotatef(180, 0, 1, 0);
					GlStateManager.rotatef(90, 1, 0, 0);
					GlStateManager.translated(dX, dZ, -.515);
					GlStateManager.scalef(.25f, .25f, .25f);
					{
						try
						{
							ClientUtils.mc().getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
						} catch(Exception e)
						{
							e.printStackTrace();
						}
					}
					GlStateManager.popMatrix();
				}
			}
			GlStateManager.enableLighting();
		}

		GlStateManager.popMatrix();

	}
}