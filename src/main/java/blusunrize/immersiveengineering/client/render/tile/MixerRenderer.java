/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.render.tile.DynamicModel.ModelType;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.MixerTileEntity;
import blusunrize.immersiveengineering.dummy.GlStateManager;
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
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fluids.FluidStack;

public class MixerRenderer extends TileEntityRenderer<MixerTileEntity>
{
	private final DynamicModel<Direction> dynamic = DynamicModel.createSided(
			new ResourceLocation(ImmersiveEngineering.MODID, "block/metal_multiblock/mixer_agitator.obj"),
			"mixer", ModelType.OBJ);

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
		IBakedModel model = dynamic.get(te.getFacing());

		ClientUtils.bindAtlas();
		matrixStack.push();
		matrixStack.translate(.5, .5, .5);

		if(te.getIsMirrored())
			matrixStack.scale(te.getFacing().getXOffset()==0?-1: 1, 1, te.getFacing().getZOffset()==0?-1: 1);

		matrixStack.push();
		matrixStack.translate(te.getFacing()==Direction.SOUTH||te.getFacing()==Direction.WEST?-.5: .5, 0, te.getFacing()==Direction.SOUTH||te.getFacing()==Direction.EAST?.5: -.5);
		float agitator = te.animation_agitator-(!te.shouldRenderAsActive()?0: (1-partialTicks)*9f);
		matrixStack.rotate(new Quaternion(new Vector3f(0, 1, 0), agitator, true));

		matrixStack.translate(-0.5, -0.5, -0.5);
		blockRenderer.getBlockModelRenderer().renderModel(te.getWorldNonnull(), model, state, blockPos, matrixStack,
				bufferIn.getBuffer(RenderType.getSolid()), true, te.getWorld().rand, 0,
				combinedOverlayIn, EmptyModelData.INSTANCE);

		matrixStack.pop();

		matrixStack.scale(.0625f, 1, .0625f);
		matrixStack.rotate(new Quaternion(new Vector3f(1, 0, 0), 90, true));
		matrixStack.translate(8, -8, .625f);

		for(int i = te.tank.getFluidTypes()-1; i >= 0; i--)
		{
			FluidStack fs = te.tank.fluids.get(i);
			if(fs!=null&&fs.getFluid()!=null)
			{
				int col = fs.getFluid().getAttributes().getColor(fs);
				GlStateManager.color3f((col >> 16&255)/255.0f, (col >> 8&255)/255.0f, (col&255)/255.0f);

				float yy = fs.getAmount()/(float)te.tank.getCapacity()*1.125f;
				matrixStack.translate(0, 0, -yy);
				float w = (i < te.tank.getFluidTypes()-1||yy >= .125)?26: 16+yy/.0125f;
				ClientUtils.drawRepeatedFluidSprite(bufferIn, matrixStack, fs, -w/2, -w/2, w, w);
			}
		}

		matrixStack.pop();
	}
}