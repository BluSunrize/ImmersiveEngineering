/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.render.tile.DynamicModel.ModelType;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.DieselGeneratorTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.data.EmptyModelData;

public class DieselGeneratorRenderer extends TileEntityRenderer<DieselGeneratorTileEntity>
{
	private final DynamicModel<Direction> dynamic = DynamicModel.createSided(
			new ResourceLocation(ImmersiveEngineering.MODID, "block/metal_multiblock/diesel_generator_fan.obj"),
			"diesel_gen", ModelType.OBJ);

	public DieselGeneratorRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}

	@Override
	public void render(DieselGeneratorTileEntity te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!te.formed||te.isDummy()||!te.getWorldNonnull().isBlockLoaded(te.getPos()))
			return;

		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockPos blockPos = te.getPos();
		BlockState state = te.getWorld().getBlockState(blockPos);
		if(state.getBlock()!=Multiblocks.dieselGenerator)
			return;
		IBakedModel model = dynamic.get(te.getFacing());

		matrixStack.push();
		matrixStack.translate(0, .6875, 0);
		matrixStack.translate(0.5, 0, 0.5);

		matrixStack.rotate(new Quaternion(new Vector3f(new Vec3d(te.getFacing().getDirectionVec())),
				te.animation_fanRotation+(te.animation_fanRotationStep*partialTicks), true));
		matrixStack.translate(-0.5, 0, -0.5);

		blockRenderer.getBlockModelRenderer().renderModel(te.getWorldNonnull(), model, state, blockPos, matrixStack,
				bufferIn.getBuffer(RenderType.getSolid()), true, te.getWorld().rand, 0, combinedOverlayIn,
				EmptyModelData.INSTANCE);

		matrixStack.pop();
	}
}