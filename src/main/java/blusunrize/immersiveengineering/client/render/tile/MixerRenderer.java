/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.MixerTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fluids.FluidStack;

public class MixerRenderer extends TileEntityRenderer<MixerTileEntity>
{
	public static DynamicModel<Direction> AGITATOR;

	public MixerRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}

	@Override
	public void render(MixerTileEntity te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!te.formed||te.isDummy()||!te.getWorldNonnull().isBlockLoaded(te.getPos()))
			return;

		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockPos blockPos = te.getPos();
		BlockState state = te.getWorld().getBlockState(blockPos);
		if(state.getBlock()!=Multiblocks.mixer)
			return;
		IBakedModel model = AGITATOR.get(te.getFacing());

		matrixStack.push();
		matrixStack.translate(.5, .5, .5);

		bufferIn = TileRenderUtils.mirror(te, matrixStack, bufferIn);
		matrixStack.push();
		matrixStack.translate(te.getFacing()==Direction.SOUTH||te.getFacing()==Direction.WEST?-.5: .5, 0, te.getFacing()==Direction.SOUTH||te.getFacing()==Direction.EAST?.5: -.5);
		float agitator = te.animation_agitator-(!te.shouldRenderAsActive()?0: (1-partialTicks)*9f);
		matrixStack.rotate(new Quaternion(new Vector3f(0, 1, 0), agitator, true));

		matrixStack.translate(-0.5, -0.5, -0.5);
		blockRenderer.getBlockModelRenderer().renderModel(te.getWorldNonnull(), model, state, blockPos, matrixStack,
				bufferIn.getBuffer(RenderType.getSolid()), true, te.getWorld().rand, 0,
				combinedOverlayIn, EmptyModelData.INSTANCE);

		matrixStack.pop();

		matrixStack.translate(te.getFacing()==Direction.SOUTH||te.getFacing()==Direction.WEST?-.5: .5, -.625f, te.getFacing()==Direction.SOUTH||te.getFacing()==Direction.EAST?.5: -.5);
		matrixStack.scale(.0625f, 1, .0625f);
		matrixStack.rotate(new Quaternion(90, 0, 0, true));

		for(int i = te.tank.getFluidTypes()-1; i >= 0; i--)
		{
			FluidStack fs = te.tank.fluids.get(i);
			if(fs!=null&&fs.getFluid()!=null)
			{
				float yy = fs.getAmount()/(float)te.tank.getCapacity()*1.125f;
				matrixStack.translate(0, 0, -yy);
				float w = (i < te.tank.getFluidTypes()-1||yy >= .125)?26: 16+yy/.0125f;
				GuiHelper.drawRepeatedFluidSprite(bufferIn.getBuffer(RenderType.getTranslucent()), matrixStack, fs,
						-w/2, -w/2, w, w);
			}
		}

		matrixStack.pop();
	}
}