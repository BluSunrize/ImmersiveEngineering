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
import blusunrize.immersiveengineering.common.blocks.wooden.ModWorkbenchBlockEntity;
import blusunrize.immersiveengineering.common.items.EngineersBlueprintItem;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ModWorkbenchRenderer extends IEBlockEntityRenderer<ModWorkbenchBlockEntity>
{

	@Override
	public void render(ModWorkbenchBlockEntity te, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(te.isDummy()||!te.getLevelNonnull().hasChunkAt(te.getBlockPos()))
			return;

		matrixStack.pushPose();
		matrixStack.translate(.5, .5, .5);

		Direction facing = te.getFacing();

		float angle = facing==Direction.NORTH?0: facing==Direction.WEST?Mth.HALF_PI: facing==Direction.EAST?-Mth.HALF_PI: Mth.PI;

		matrixStack.mulPose(new Quaternionf().rotateY(angle));

		ItemStack stack = te.getInventory().get(0);
		boolean showIngredients = true;
		if(!stack.isEmpty())
		{
			if(stack.getItem() instanceof EngineersBlueprintItem)
			{
				matrixStack.pushPose();
				double playerDistanceSq = ClientUtils.mc().player.distanceToSqr(Vec3.atCenterOf(te.getBlockPos()));
				if(playerDistanceSq < 120)
				{
					BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(te.getLevel(), ItemNBTHelper.getString(stack, "blueprint"));

					int l = recipes.length;
					int perRow = l > 6?l-3: l > 4?l-2: l==1?2: l==2?3: l;
					matrixStack.translate(0, .501, 0);
					matrixStack.mulPose(new Quaternionf()
							.rotateX(-Mth.HALF_PI)
							.rotateZ(-Mth.PI / 8)
					);
					matrixStack.translate(0.39, l > 4?.72: .78, 0);
					float scale = l > 4?.009375f: .012f;
					matrixStack.scale(scale, -scale, scale);
					int rendered = 0;
					for(int i = 0; i < l; i++)
					{
						BlueprintCraftingRecipe recipe = recipes[i%recipes.length];
						BlueprintLines blueprint = recipe==null?null: AutoWorkbenchRenderer.getBlueprintDrawable(recipe, te.getLevelNonnull());
						if(blueprint!=null)
						{
							double dX = rendered < perRow?(.93725/scale-perRow*16.6)+rendered*16.6: (.70375/scale-rendered%perRow*16.6);
							double dY = rendered < perRow?0: -.15625;
							matrixStack.translate(dX, dY/scale, 0);

							//Width depends on distance
							float texScale = blueprint.textureScale/16f;
							matrixStack.scale(1/texScale, 1/texScale, 1/texScale);
							blueprint.draw(matrixStack, bufferIn, combinedLightIn);
							matrixStack.scale(texScale, texScale, texScale);
							matrixStack.translate(-dX, -dY/scale, 0);
							rendered++;
						}
					}
				}
				matrixStack.popPose();
			}
			else
			{
				showIngredients = false;
				matrixStack.pushPose();
				matrixStack.translate(0, .5625, 0);

				matrixStack.mulPose(new Quaternionf()
						.rotateY(Mth.PI)
						.rotateX(Mth.HALF_PI)
				);
				matrixStack.translate(-.875, 0, 0);
				matrixStack.scale(.75f, .75f, .75f);
				ClientUtils.mc().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.FIXED,
						combinedLightIn, combinedOverlayIn, matrixStack, bufferIn, 0);
				matrixStack.popPose();
			}
		}
		if(showIngredients)
		{
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
					matrixStack.pushPose();
					matrixStack.mulPose(new Quaternionf().rotateY(Mth.PI).rotateX(Mth.HALF_PI));
					matrixStack.translate(dX, dZ, -.515);
					matrixStack.scale(.25f, .25f, .25f);
					{
						try
						{
							ClientUtils.mc().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.FIXED,
									combinedLightIn, combinedOverlayIn, matrixStack, bufferIn, 0);
						} catch(Exception e)
						{
							e.printStackTrace();
						}
					}
					matrixStack.popPose();
				}
			}
		}

		matrixStack.popPose();

	}
}