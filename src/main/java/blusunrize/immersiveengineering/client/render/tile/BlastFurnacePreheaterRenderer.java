package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.client.utils.RenderUtils;
import blusunrize.immersiveengineering.common.blocks.metal.BlastFurnacePreheaterTileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;

public class BlastFurnacePreheaterRenderer extends BlockEntityRenderer<BlastFurnacePreheaterTileEntity>
{
	public static final String NAME = "blastfurnace_preheater_fan";
	public static DynamicModel<Void> MODEL;

	public BlastFurnacePreheaterRenderer(BlockEntityRenderDispatcher arg)
	{
		super(arg);
	}

	@Override
	public void render(BlastFurnacePreheaterTileEntity bEntity, float partial, PoseStack transform, MultiBufferSource buffers, int light, int overlay)
	{
		if(bEntity.isDummy())
			return;
		transform.pushPose();
		transform.translate(0.5, 0.5, 0.5);
		transform.mulPose(new Quaternion(new Vector3f(0, 1, 0), -bEntity.getFacing().toYRot()+180, true));
		final float angle = bEntity.angle+BlastFurnacePreheaterTileEntity.ANGLE_PER_TICK*(bEntity.active?partial: 0);
		Vector3f axis = new Vector3f(0, 0, 1);
		transform.mulPose(new Quaternion(axis, angle, false));
		transform.translate(-0.5, -0.5, -0.5);
		RenderUtils.renderModelTESRFast(
				MODEL.getNullQuads(null, null), buffers.getBuffer(RenderType.solid()), transform, light, overlay
		);
		transform.popPose();
	}
}
