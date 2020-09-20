/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.SawmillTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.EmptyModelData;

public class SawmillRenderer extends TileEntityRenderer<SawmillTileEntity>
{
	public static DynamicModel<Direction> BLADE;

	public SawmillRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}

	@Override
	public void render(SawmillTileEntity te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!te.formed||te.isDummy()||!te.getWorldNonnull().isBlockLoaded(te.getPos()))
			return;

		//Grab model
		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockPos blockPos = te.getPos();
		BlockState state = te.getWorld().getBlockState(blockPos);
		if(state.getBlock()!=Multiblocks.sawmill)
			return;

		//Outer GL Wrapping, initial translation
		matrixStack.push();
		matrixStack.translate(.5, 0, .5);
		bufferIn = TileRenderUtils.mirror(te, matrixStack, bufferIn);


		IVertexBuilder solidBuilder = bufferIn.getBuffer(RenderType.getSolid());

		Direction facing = te.getFacing();
		float dir = facing==Direction.SOUTH?180: facing==Direction.NORTH?0: facing==Direction.EAST?-90: 90;
		matrixStack.rotate(new Quaternion(0, dir, 0, true));

		// Sawblade
		matrixStack.push();
		matrixStack.translate(1, .125, -.5);
		matrixStack.rotate(new Quaternion(0, 0, te.animation_bladeRotation, true));
		ClientUtils.renderModelTESRFast(
				BLADE.get(Direction.NORTH).getQuads(state, null, te.getWorldNonnull().rand, EmptyModelData.INSTANCE),
				solidBuilder, matrixStack, combinedLightIn);
		matrixStack.pop();

		// Items
		ItemStack log = new ItemStack(Blocks.OAK_LOG);
		ItemStack stripped = new ItemStack(Blocks.STRIPPED_OAK_LOG);
		ItemStack planks = new ItemStack(Blocks.OAK_PLANKS);

		int total = 200;
		int step = ClientUtils.mc().player.ticksExisted%total;
		float relative = step/(float)total;
		ItemStack rendered = relative < .3125?log: relative < .8625?stripped: planks;
		renderItem(rendered, relative, matrixStack, bufferIn, combinedLightIn, combinedOverlayIn);

		matrixStack.pop();
	}

	private void renderItem(ItemStack stack, float progress, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		float xOffset = -2.5f+progress*5;
		matrixStack.push();
		matrixStack.translate(xOffset, .375, 0);
		matrixStack.rotate(new Quaternion(0, 0, 90, true));
		ClientUtils.mc().getItemRenderer().renderItem(stack, TransformType.FIXED,
				combinedLightIn, combinedOverlayIn, matrixStack, bufferIn);
		matrixStack.pop();
	}
}
