/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsIE;
import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.ArcFurnaceTileEntity;
import blusunrize.immersiveengineering.common.blocks.stone.BlockTypes_StoneDecoration;
import blusunrize.immersiveengineering.common.util.Utils;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class MultiblockArcFurnace extends TemplateMultiblock
{
	public static MultiblockArcFurnace instance = new MultiblockArcFurnace();

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return true;
	}

	//@OnlyIn(Dist.CLIENT)
	static ItemStack renderStack = ItemStack.EMPTY;

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderFormedStructure()
	{
		if(renderStack.isEmpty())
			renderStack = new ItemStack(Multiblocks.arcFurnace);

		GlStateManager.translated(2.5, 2.25, 2.25);
		GlStateManager.rotatef(-45, 0, 1, 0);
		GlStateManager.rotatef(-20, 1, 0, 0);
		GlStateManager.scaled(6.5, 6.5, 6.5);

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
	protected void form(World world, BlockPos pos, Rotation rot, Mirror mirror, Direction side)
	{
		side = side.getOpposite();
		BlockState state = Multiblocks.arcFurnace.getDefaultState();
		state = state.with(IEProperties.FACING_HORIZONTAL, side);
		for (BlockInfo info:getStructureManual())
		{
			BlockPos pos2 = withSettingsAndOffset(pos, info.pos, mirror, rot);

			world.setBlockState(pos2, state);
			TileEntity curr = world.getTileEntity(pos2);
			if(curr instanceof ArcFurnaceTileEntity)
			{
				ArcFurnaceTileEntity tile = (ArcFurnaceTileEntity)curr;
				tile.formed = true;
				tile.pos = info.pos.getY()*25+l*5+(w+2);
				tile.offset = new int[]{(side==Direction.WEST?-l+2: side==Direction.EAST?l-2: side==Direction.NORTH?ww: -ww), h-1, (side==Direction.NORTH?-l+2: side==Direction.SOUTH?l-2: side==Direction.EAST?ww: -ww)};
				tile.mirrored = mirrored;
				tile.markDirty();
				world.addBlockEvent(pos2, IEContent.blockMetalMultiblock, 255, 0);
			}
		}
	}
}