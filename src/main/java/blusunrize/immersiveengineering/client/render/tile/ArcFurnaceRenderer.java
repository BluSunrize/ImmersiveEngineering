/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.client.utils.RenderUtils;
import blusunrize.immersiveengineering.common.blocks.metal.ArcFurnaceBlockEntity;
import blusunrize.immersiveengineering.common.register.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class ArcFurnaceRenderer extends IEBlockEntityRenderer<ArcFurnaceBlockEntity>
{
	private TextureAtlasSprite hotMetal_flow = null;
	private TextureAtlasSprite hotMetal_still = null;

	public static final String NAME = "arc_furnace_electrodes";
	public static DynamicModel ELECTRODES;
	public static final ResourceLocation HOT_METLA_STILL = new ResourceLocation(ImmersiveEngineering.MODID, "block/fluid/hot_metal_still");
	public static final ResourceLocation HOT_METLA_FLOW = new ResourceLocation(ImmersiveEngineering.MODID, "block/fluid/hot_metal_flow");

	@Override
	public void render(ArcFurnaceBlockEntity te, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn,
					   int combinedLightIn, int combinedOverlayIn)
	{
		if(!te.formed||te.isDummy()||!te.getLevelNonnull().hasChunkAt(te.getBlockPos()))
			return;
		List<String> renderedParts = null;
		for(int i = 0; i < ArcFurnaceBlockEntity.ELECTRODE_COUNT; i++)
			if(!te.getInventory().get(ArcFurnaceBlockEntity.FIRST_ELECTRODE_SLOT+i).isEmpty())
			{
				if(renderedParts==null)
					renderedParts = Lists.newArrayList("electrode"+(i+1));
				else
					renderedParts.add("electrode"+(i+1));
			}
		if(renderedParts==null)
			return;
		if(te.shouldRenderAsActive())
			renderedParts.add("active");

		BlockPos blockPos = te.getBlockPos();
		BlockState state = te.getLevel().getBlockState(blockPos);
		if(state.getBlock()!=Multiblocks.arcFurnace.get())
			return;
		IEObjState objState = new IEObjState(VisibilityList.show(renderedParts));

		matrixStack.pushPose();
		List<BakedQuad> quads = ELECTRODES.get().getQuads(state, null, Utils.RAND, new SinglePropertyModelData<>(objState, Model.IE_OBJ_STATE));
		matrixStack.pushPose();
		rotateForFacing(matrixStack, te.getFacing());
		RenderUtils.renderModelTESRFast(
				quads, bufferIn.getBuffer(RenderType.solid()), matrixStack, combinedLightIn, combinedOverlayIn
		);
		matrixStack.popPose();
		matrixStack.translate(.5, .5, .5);

		if(te.pouringMetal > 0)
		{
			if(hotMetal_flow==null)
			{
				TextureAtlas blockMap = ClientUtils.mc().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS);
				hotMetal_still = blockMap.getSprite(HOT_METLA_STILL);
				hotMetal_flow = blockMap.getSprite(HOT_METLA_FLOW);
			}
			matrixStack.mulPose(new Quaternion(new Vector3f(0, 1, 0), -te.getFacing().toYRot()+180, true));
			int process = 40;
			float speed = 5f;
			int pour = process-te.pouringMetal;
			float h = (pour > (process-speed)?((process-pour)/speed*27): pour > speed?27: (pour/speed*27))/16f;
			matrixStack.translate(-.5f, 1.25-.6875f, 1.5f);
			VertexConsumer fullbright = bufferIn.getBuffer(IERenderTypes.SOLID_FULLBRIGHT);
			matrixStack.pushPose();
			if(pour > (process-speed))
				matrixStack.translate(0, -1.6875f+h, 0);
			if(h > 1)
			{
				matrixStack.translate(0, -h, 0);
				RenderUtils.renderTexturedBox(fullbright, matrixStack, .375F, 0, .375F, .625F, 1, .625F, hotMetal_flow, true);
				matrixStack.translate(0, 1, 0);
				RenderUtils.renderTexturedBox(fullbright, matrixStack, .375F, 0, .375F, .625F, h-1, .625F, hotMetal_flow, true);
				matrixStack.translate(0, -1, 0);
				matrixStack.translate(0, h, 0);
			}
			else
			{
				matrixStack.translate(0, -h, 0);
				RenderUtils.renderTexturedBox(fullbright, matrixStack, .375F, 0, .375F, .625F, h, .625F, hotMetal_flow, true);
				matrixStack.translate(0, h, 0);
			}
			if(pour > (process-speed))
				matrixStack.translate(0, 1.6875f-h, 0);
			if(pour > speed)
			{
				float h2 = (pour > (process-speed)?.625f: pour/(process-speed)*.625f);
				matrixStack.translate(0, -1.6875f, 0);
				RenderUtils.renderTexturedBox(fullbright, matrixStack, .125F, 0, .125F, .875F, h2, .875F, hotMetal_still, false);
				matrixStack.translate(0, 1.6875f, 0);
			}
			matrixStack.popPose();
		}
		matrixStack.popPose();
	}
}