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
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ExcavatorMultiblock extends IETemplateMultiblock
{
	public ExcavatorMultiblock()
	{
		super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/excavator"),
				new BlockPos(1, 1, 5), new BlockPos(1, 1, 5), () -> Multiblocks.excavator.getDefaultState());
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, Direction side, PlayerEntity player)
	{
		final boolean excavatorFormed = super.createStructure(world, pos, side, player);
		if(excavatorFormed)
		{
			// Try to also form the bucket wheel
			TileEntity clickedTE = world.getTileEntity(pos);
			if(clickedTE instanceof ExcavatorTileEntity)
			{
				ExcavatorTileEntity excavator = (ExcavatorTileEntity)clickedTE;
				BlockPos wheelCenter = excavator.getWheelCenterPos();
				IEMultiblocks.BUCKET_WHEEL.createStructure(world, wheelCenter, side.rotateYCCW(), player);
				if(excavator.getIsMirrored())
				{
					TileEntity wheel = world.getTileEntity(wheelCenter);
					if(wheel!=null&&wheel instanceof BucketWheelTileEntity)
						((BucketWheelTileEntity)wheel).adjustStructureFacingAndMirrored(side.rotateY(), excavator.getIsMirrored());
				}
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
	public void renderFormedStructure(MatrixStack transform, IRenderTypeBuffer buffer)
	{
		if(renderStack==null)
			renderStack = new ItemStack(Multiblocks.excavator);
		transform.translate(3, 1.5, 4);
		transform.rotate(new Quaternion(0, 225, 0, true));
		transform.rotate(new Quaternion(-20, 0, 0, true));
		transform.scale(5.25F, 5.25F, 5.25F);

		ClientUtils.mc().getItemRenderer().renderItem(
				renderStack,
				TransformType.GUI,
				0xf000f0,
				OverlayTexture.NO_OVERLAY,
				transform, buffer
		);
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}
}