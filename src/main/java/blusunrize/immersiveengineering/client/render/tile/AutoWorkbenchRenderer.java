/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.api.utils.client.ModelDataUtils;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.obj.callback.DynamicSubmodelCallbacks;
import blusunrize.immersiveengineering.client.render.tile.BlueprintRenderer.BlueprintLines;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.AutoWorkbenchLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.AutoWorkbenchLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInWorld;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Quaternionf;

import java.util.List;

import static blusunrize.immersiveengineering.client.render.tile.BlueprintRenderer.getBlueprintDrawable;

public class AutoWorkbenchRenderer extends IEBlockEntityRenderer<MultiblockBlockEntityMaster<State>>
{
	public static final String NAME = "auto_workbench_animated";
	public static DynamicModel DYNAMIC;

	@Override
	public void render(MultiblockBlockEntityMaster<State> blockEntity, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		final BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
		BakedModel model = DYNAMIC.get();
		final IMultiblockBEHelperMaster<State> helper = blockEntity.getHelper();
		final State state = helper.getState();

		//Item Displacement
		float[][] itemDisplays = new float[state.processor.getQueueSize()][];
		//Animations
		float drill = 0;
		float lift = 0;
		float press = 0;
		float liftPress = 0;

		for(int i = 0; i < itemDisplays.length; i++)
		{
			MultiblockProcess<?, ?> process = state.processor.getQueue().get(i);
			if(process==null||process.processTick <= 0||process.processTick==process.getMaxTicks(blockEntity.getLevel()))
				continue;
			//+partialTicks
			float processTimer = ((float)process.processTick)/process.getMaxTicks(blockEntity.getLevel())*180;
			if(processTimer <= 9)
				continue;

			float itemX = -1;
			float itemY = -.34375f;
			float itemZ = -.9375f;
			float itemAngle = Mth.HALF_PI;

			if(processTimer <= 24)//slide
			{
				itemAngle = 67.5f * Mth.PI / 180;
				if(processTimer <= 19)
				{
					itemZ += .25+(19-processTimer)/10f*.5f;
					itemY += .25+(19-processTimer)/10f*.21875f;
				}
				else
				{
					itemZ += (24-processTimer)/5f*.25f;
					itemY += (24-processTimer)/5f*.25f;
				}
			}
			else if(processTimer <= 40)
			{
				itemX += (processTimer-24)/16f;
			}
			else if(processTimer <= 100)
			{
				itemX += 1;
				float drillStep = 0;
				if(processTimer <= 60)
				{
					lift = (processTimer-40)/20f*.3125f;
					drillStep = 4+(60-processTimer)*4;
				}
				else if(processTimer <= 80)
				{
					lift = .3125f;
					drillStep = 4;
				}
				else
				{
					lift = (100-processTimer)/20f*.3125f;
					drillStep = 4+(processTimer-80)*4;
				}
				if(drillStep > 0)
					drill = processTimer%drillStep/drillStep*2 * Mth.PI;
				itemY += Math.max(0, lift-.0625);
			}
			else if(processTimer <= 116)
			{
				itemX += 1;
				itemZ += (processTimer-100)/16f;
			}
			else if(processTimer <= 132)
			{
				itemX += 1+(processTimer-116)/16f;
				itemZ += 1;
			}
			else if(processTimer <= 172)
			{
				itemX += 2;
				itemZ += 1;
				if(processTimer <= 142)
					press = (processTimer-132)/10f;
				else if(processTimer <= 162)
					press = 1;
				else
					press = (172-processTimer)/10f;
				liftPress = press*.0625f;
				itemY += liftPress;
			}
			else if(processTimer <= 180)
			{
				itemX += 2+(processTimer-172)/16f;
				itemZ += 1;
			}
			itemDisplays[i] = new float[]{processTimer, itemX, itemY, itemZ, itemAngle};

		}

		final MultiblockOrientation orientation = helper.getContext().getLevel().getOrientation();
		//Outer GL Wrapping, initial translation
		matrixStack.pushPose();
		bufferIn = BERenderUtils.mirror(orientation, matrixStack, bufferIn);
		Direction facing = orientation.front();
		if(orientation.mirrored())
		{
			if(facing.getAxis()==Axis.Z)
				matrixStack.translate(-1, 0, 0);
			else
				matrixStack.translate(0, 0, -1);
		}
		rotateForFacing(matrixStack, facing);
		matrixStack.translate(0.5, 0.5, 0.5);

		matrixStack.pushPose();
		ItemStack blueprintStack = state.inventory.getStackInSlot(AutoWorkbenchLogic.BLUEPRINT_SLOT);
		if(!blueprintStack.isEmpty())
			renderModelPart(matrixStack, blockRenderer, bufferIn, model, combinedLightIn, combinedOverlayIn, "blueprint");


		matrixStack.translate(0, lift, 0);
		renderModelPart(matrixStack, blockRenderer, bufferIn, model, combinedLightIn, combinedOverlayIn, "lift");
		matrixStack.translate(0, -lift, 0);

		float tx = 0;
		float tz = -.9375f;
		matrixStack.pushPose();
		matrixStack.translate(tx, 0, tz);
		matrixStack.mulPose(new Quaternionf().rotateXYZ(0, drill, 0));
		renderModelPart(matrixStack, blockRenderer, bufferIn, model, combinedLightIn, combinedOverlayIn, "drill");
		matrixStack.popPose();

		tx = 0;
		tz = -.59375f;
		matrixStack.pushPose();
		matrixStack.translate(tx, -.21875, tz);
		matrixStack.mulPose(new Quaternionf().rotateXYZ(press*Mth.HALF_PI, 0, 0));
		renderModelPart(matrixStack, blockRenderer, bufferIn, model, combinedLightIn, combinedOverlayIn, "press");
		matrixStack.popPose();

		matrixStack.translate(0, liftPress, 0);
		renderModelPart(matrixStack, blockRenderer, bufferIn, model, combinedLightIn, combinedOverlayIn, "pressLift");

		matrixStack.popPose();

		//DRAW ITEMS HERE
		for(int i = 0; i < itemDisplays.length; i++)
			if(itemDisplays[i]!=null)
			{
				MultiblockProcess<?, ?> process = state.processor.getQueue().get(i);
				if(!(process instanceof MultiblockProcessInWorld<?> inWorld))
					continue;

				float scale = .3125f;
				List<ItemStack> dList = inWorld.getDisplayItem(blockEntity.getLevel());
				if(!dList.isEmpty())
					if(dList.size() < 2)
					{
						matrixStack.pushPose();
						matrixStack.translate(itemDisplays[i][1], itemDisplays[i][2], itemDisplays[i][3]);
						matrixStack.mulPose(new Quaternionf().rotateXYZ(itemDisplays[i][4], 0, 0));
						matrixStack.scale(scale, scale, .5f);
						ClientUtils.mc().getItemRenderer().renderStatic(
								dList.get(0), ItemDisplayContext.FIXED,
								combinedLightIn, combinedOverlayIn, matrixStack, bufferIn,
								blockEntity.getLevel(), 0
						);
						matrixStack.popPose();
					}
					else
					{
						int size = dList.size();
						int lines = (int)Math.ceil(size/2f);
						float spacer = (lines-1)*.234375f;
						for(int d = 0; d < size; d++)
						{
							float oX = (size > 2?-.3125f: 0)+(lines-d/2)*.0625f+d%2*.3125f;
							float oZ = -spacer/2f+d/2*.234375f;
							float oY = 0;

							float localItemX = itemDisplays[i][1]+oX;
							float localItemY = itemDisplays[i][2]+oY;
							float localItemZ = itemDisplays[i][3]+oZ;
							float subProcess = itemDisplays[i][0]-d/2*4;
							float localAngle = itemDisplays[i][4];
							if(subProcess <= 24)//slide
							{
								localAngle = 67.5f;
								if(subProcess <= 19)
								{
									localItemZ = -1+.25f+(19-subProcess)/10f*.5f;
									localItemY = -.34375f+.25f+(19-subProcess)/10f*.21875f;
								}
								else
								{
									localItemZ = -1+(oZ-(24-subProcess)/5f*oZ);
									localItemY = -.34375f+(24-subProcess)/5f*.25f;
								}
							}
							matrixStack.pushPose();
							matrixStack.translate(localItemX, localItemY, localItemZ);
							matrixStack.mulPose(new Quaternionf().rotateXYZ((float)Math.toRadians(localAngle), 0, 0));
							matrixStack.scale(scale, scale, .5f);
							ClientUtils.mc().getItemRenderer().renderStatic(
									dList.get(d), ItemDisplayContext.FIXED,
									combinedLightIn, combinedOverlayIn, matrixStack, bufferIn,
									blockEntity.getLevel(), 0
							);
							matrixStack.popPose();
						}
					}
			}

		//Blueprint
		double playerDistanceSq = ClientUtils.mc().player.distanceToSqr(Vec3.atCenterOf(blockEntity.getBlockPos()));

		if(!blueprintStack.isEmpty()&&playerDistanceSq < 1000)
		{
			BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(blockEntity.getLevel(), ItemNBTHelper.getString(blueprintStack, "blueprint"));
			BlueprintCraftingRecipe recipe = (state.selectedRecipe < 0||state.selectedRecipe >= recipes.length)?null: recipes[state.selectedRecipe];
			BlueprintLines blueprint = recipe==null?null: getBlueprintDrawable(recipe, blockEntity.getLevel());
			if(blueprint!=null)
			{
				matrixStack.pushPose();
				matrixStack.translate(-.195, .125, .97);
				matrixStack.mulPose(new Quaternionf().rotateXYZ(-Mth.PI / 4, 0, 0));
				float scale = .5f/blueprint.textureScale;
				matrixStack.scale(scale, -scale, scale);
				matrixStack.translate(0.5, 0.5, 0.5);
				blueprint.draw(matrixStack, bufferIn, combinedLightIn);
				matrixStack.popPose();
			}
		}
		matrixStack.popPose();
	}

	public static void renderModelPart(
			PoseStack matrix, final BlockRenderDispatcher blockRenderer, MultiBufferSource buffers,
			BakedModel model, int light, int overlay, String parts
	)
	{
		matrix.pushPose();
		matrix.translate(-0.5, -0.5, -0.5);
		ModelData data = ModelDataUtils.single(DynamicSubmodelCallbacks.getProperty(), VisibilityList.show(parts));

		blockRenderer.getModelRenderer().renderModel(
				matrix.last(), buffers.getBuffer(RenderType.solid()), null, model,
				1, 1, 1,
				light, overlay, data, RenderType.solid()
		);
		matrix.popPose();
	}
}
