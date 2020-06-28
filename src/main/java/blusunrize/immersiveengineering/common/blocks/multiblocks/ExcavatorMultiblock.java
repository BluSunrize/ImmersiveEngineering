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
import blusunrize.immersiveengineering.common.blocks.metal.ExcavatorTileEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
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
				BlockPos wheelCenter = ((ExcavatorTileEntity)clickedTE).getWheelCenterPos();
				IEMultiblocks.BUCKET_WHEEL.createStructure(world, wheelCenter, side.rotateY(), player);
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
	public void renderFormedStructure()
	{
		if(renderStack==null)
			renderStack = new ItemStack(Multiblocks.excavator);
		GlStateManager.translated(2, 1.5, 2.875);
		GlStateManager.rotatef(-225, 0, 1, 0);
		GlStateManager.rotatef(-20, 1, 0, 0);
		GlStateManager.scaled(5.25, 5.25, 5.25);

		GlStateManager.disableCull();
		ClientUtils.mc().getItemRenderer().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
		GlStateManager.enableCull();
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}
}