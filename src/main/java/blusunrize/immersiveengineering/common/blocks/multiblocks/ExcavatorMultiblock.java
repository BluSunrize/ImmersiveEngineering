/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.BucketWheelTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.ExcavatorTileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ExcavatorMultiblock extends IETemplateMultiblock
{
	public ExcavatorMultiblock()
	{
		super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/excavator"),
				new BlockPos(1, 1, 5), new BlockPos(1, 1, 5), new BlockPos(3, 3, 6),
				() -> Multiblocks.excavator.defaultBlockState());
	}

	@Override
	public boolean createStructure(Level world, BlockPos pos, Direction side, Player player)
	{
		final boolean excavatorFormed = super.createStructure(world, pos, side, player);
		if(excavatorFormed)
		{
			// Try to also form the bucket wheel
			BlockEntity clickedTE = world.getBlockEntity(pos);
			if(clickedTE instanceof ExcavatorTileEntity)
			{
				ExcavatorTileEntity excavator = (ExcavatorTileEntity)clickedTE;
				BlockPos wheelCenter = excavator.getWheelCenterPos();
				IEMultiblocks.BUCKET_WHEEL.createStructure(world, wheelCenter, side.getCounterClockWise(), player);
				BlockEntity wheel = world.getBlockEntity(wheelCenter);
				if(wheel instanceof BucketWheelTileEntity)
					((BucketWheelTileEntity)wheel).adjustStructureFacingAndMirrored(side.getClockWise(), excavator.getIsMirrored());
			}
		}
		return excavatorFormed;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	private static ItemStack renderStack;

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderFormedStructure(PoseStack transform, MultiBufferSource buffer)
	{
		if(renderStack==null)
			renderStack = new ItemStack(Multiblocks.excavator);
		transform.translate(3, 1.5, 4);
		transform.mulPose(new Quaternion(0, 225, 0, true));
		transform.mulPose(new Quaternion(-20, 0, 0, true));
		transform.scale(5.25F, 5.25F, 5.25F);

		ClientUtils.mc().getItemRenderer().renderStatic(
				renderStack,
				TransformType.GUI,
				0xf000f0,
				OverlayTexture.NO_OVERLAY,
				transform, buffer,
				0
		);
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}
}