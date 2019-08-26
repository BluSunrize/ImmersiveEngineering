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
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.blocks.stone.AlloySmelterTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MultiblockAlloySmelter extends TemplateMultiblock
{
	public static MultiblockAlloySmelter instance = new MultiblockAlloySmelter();

	public MultiblockAlloySmelter()
	{
		super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/alloy_smelter"), new BlockPos(0, 2, 0));
	}

	@Override
	public float getManualScale()
	{
		return 20;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderFormedStructure()
	{
	}

	@Override
	protected void form(World world, BlockPos pos, Rotation rot, Mirror mirror, Direction sideHit)
	{
		BlockState state = Multiblocks.alloySmelter.getDefaultState();
		state = state.with(IEProperties.FACING_HORIZONTAL, sideHit.getOpposite());
		for(int h = 0; h <= 1; h++)
			for(int l = 0; l <= 1; l++)
				for(int w = 0; w <= 1; w++)
				{
					BlockPos pos2 = withSettingsAndOffset(pos, new BlockPos(l, h, w), mirror, rot);
					world.setBlockState(pos2, state);
					TileEntity curr = world.getTileEntity(pos2);
					if(curr instanceof AlloySmelterTileEntity)
					{
						AlloySmelterTileEntity currBlast = (AlloySmelterTileEntity)curr;
						currBlast.offset = new int[]{pos2.getX()-pos.getX(), pos2.getY()-pos.getY(), pos2.getZ()-pos.getZ()};
						currBlast.posInMultiblock = (h+1)*9+(l+1)*3+(w+1);
						currBlast.formed = true;
						currBlast.markDirty();
						world.addBlockEvent(pos2, state.getBlock(), 255, 0);
					}
				}
	}
}