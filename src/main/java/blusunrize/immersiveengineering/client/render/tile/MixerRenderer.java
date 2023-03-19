/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.client.utils.RenderUtils;
import blusunrize.immersiveengineering.common.blocks.metal.MixerBlockEntity;
import blusunrize.immersiveengineering.common.register.IEBlocks.Multiblocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fluids.FluidStack;

import static blusunrize.immersiveengineering.client.ClientUtils.getSprite;
import static com.mojang.blaze3d.vertex.DefaultVertexFormat.*;

public class MixerRenderer extends IEBlockEntityRenderer<MixerBlockEntity>
{
	public static final String NAME = "mixer_agitator";
	public static DynamicModel AGITATOR;

	@Override
	public void render(MixerBlockEntity te, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!te.formed||te.isDummy()||!te.getLevelNonnull().hasChunkAt(te.getBlockPos()))
			return;

		final BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
		BlockPos blockPos = te.getBlockPos();
		BlockState state = te.getLevel().getBlockState(blockPos);
		if(state.getBlock()!=Multiblocks.MIXER.get())
			return;

		matrixStack.pushPose();
		matrixStack.translate(.5, .5, .5);

		bufferIn = BERenderUtils.mirror(te, matrixStack, bufferIn);
		matrixStack.pushPose();
		matrixStack.translate(te.getFacing()==Direction.SOUTH||te.getFacing()==Direction.WEST?-.5: .5, 0, te.getFacing()==Direction.SOUTH||te.getFacing()==Direction.EAST?.5: -.5);
		float agitator = te.animation_agitator-(!te.shouldRenderAsActive()?0: (1-partialTicks)*9f);
		matrixStack.mulPose(new Quaternion(new Vector3f(0, 1, 0), agitator, true));

		matrixStack.translate(-0.5, -0.5, -0.5);
		blockRenderer.getModelRenderer().renderModel(
				matrixStack.last(), bufferIn.getBuffer(RenderType.solid()), state, AGITATOR.get(),
				1, 1, 1,
				combinedLightIn, combinedOverlayIn, ModelData.EMPTY, RenderType.solid()
		);

		matrixStack.popPose();

		matrixStack.translate(te.getFacing()==Direction.SOUTH||te.getFacing()==Direction.WEST?-.5: .5, -.625f, te.getFacing()==Direction.SOUTH||te.getFacing()==Direction.EAST?.5: -.5);

		for(int i = te.tank.getFluidTypes()-1; i >= 0; i--)
		{
			FluidStack fs = te.tank.fluids.get(i);
			if(fs!=null&&fs.getFluid()!=null)
			{
				float yy = fs.getAmount()/(float)te.tank.getCapacity()*1.0625f;
				matrixStack.translate(0, yy, 0);
				float w = (i < te.tank.getFluidTypes()-1||yy >= .125)?.8125f: 0.5f+yy*2.5f;
				double px = w*16;

				IClientFluidTypeExtensions props = IClientFluidTypeExtensions.of(fs.getFluid());
				TextureAtlasSprite sprite = getSprite(props.getStillTexture(fs));
				VertexConsumer consumer = bufferIn.getBuffer(RenderType.solid());
				Matrix4f matrix4f = matrixStack.last().pose();

				int col = props.getTintColor(fs);
				float r = (col>>16&255)/255.0f;
				float g = (col>>8&255)/255.0f;
				float b = (col&255)/255.0f;

				Vec3 from = new Vec3(-w, 0, -w);
				Vec3 to = new Vec3(0, 0, 0);
				for(int v = 0; v < 4; v++)
				{
					Vec3 start = from.add(v%2==1?w: 0, 0, v > 1?w: 0);
					Vec3 end = to.add(v%2==1?w: 0, 0, v > 1?w: 0);
					float uMin = sprite.getU(v%2==0?16-px: 0);
					float uMax = sprite.getU(v%2==0?16: px);
					float vMin = sprite.getV(v > 1?0: 16-px);
					float vMax = sprite.getV(v > 1?px: 16);

					consumer.vertex(matrix4f, (float)start.x, 0, (float)start.z).color(r, g, b, 1).uv(uMin, vMin)
							.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(combinedLightIn).normal(0, 1, 0).endVertex();
					consumer.vertex(matrix4f, (float)start.x, 0, (float)end.z).color(r, g, b, 1).uv(uMin, vMax)
							.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(combinedLightIn).normal(0, 1, 0).endVertex();
					consumer.vertex(matrix4f, (float)end.x, 0, (float)end.z).color(r, g, b, 1).uv(uMax, vMax)
							.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(combinedLightIn).normal(0, 1, 0).endVertex();
					consumer.vertex(matrix4f, (float)end.x, 0, (float)start.z).color(r, g, b, 1).uv(uMax, vMin)
							.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(combinedLightIn).normal(0, 1, 0).endVertex();
				}
			}
		}

		matrixStack.popPose();
	}
}