/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.CrusherTileEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MultiblockCrusher extends TemplateMultiblock
{
	public MultiblockCrusher()
	{
		//TODO swap x and z?
		super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/crusher"),
				new BlockPos(1, 0, 2), new BlockPos(0, 1, 2));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	static ItemStack renderStack;

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderFormedStructure()
	{
		if(renderStack==null)
			renderStack = new ItemStack(Multiblocks.crusher);
		GlStateManager.translated(1.5, 1.5, 2.5);
		GlStateManager.rotatef(-45, 0, 1, 0);
		GlStateManager.rotatef(-20, 1, 0, 0);
		GlStateManager.scaled(5.5, 5.5, 5.5);

		GlStateManager.disableCull();
		ClientUtils.mc().getItemRenderer().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
		GlStateManager.enableCull();
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}

	@Override
	protected void replaceStructureBlock(BlockInfo info, World world, BlockPos actualPos, boolean mirrored, Direction clickDirection, Vec3i offsetFromMaster)
	{
		BlockState crusherState = Multiblocks.crusher.getDefaultState()
				.with(IEProperties.FACING_HORIZONTAL, clickDirection.getOpposite());
		world.setBlockState(actualPos, crusherState);
		TileEntity curr = world.getTileEntity(actualPos);
		if(curr instanceof CrusherTileEntity)
		{
			CrusherTileEntity tile = (CrusherTileEntity)curr;
			tile.formed = true;
			tile.posInMultiblock =
					tile.offsetToMaster = offsetFromMaster;
			tile.mirrored = mirrored;
			tile.markDirty();
			world.addBlockEvent(actualPos, crusherState.getBlock(), 255, 0);
		}
	}
}