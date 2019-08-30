/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public class BlockIESlab extends SlabBlock
{

	private final boolean isSlab;

	public BlockIESlab(String name, Properties props, Class<? extends BlockItem> itemBlock, boolean isSlab)
	{
		super(props);
		this.isSlab = isSlab;
		ResourceLocation registryName = new ResourceLocation(ImmersiveEngineering.MODID, name);
		setRegistryName(registryName);

		IEContent.registeredIEBlocks.add(this);
		try
		{
			Item.Properties itemProps = new Item.Properties().group(ImmersiveEngineering.itemGroup);
			IEContent.registeredIEItems.add(itemBlock.getConstructor(Block.class, Item.Properties.class)
					.newInstance(this, itemProps));
		} catch(Exception e)
		{
			//TODO e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity)
	{
		if(isSlab)
		{
			double relativeEntityPosition = entity.getPosition().getY()-pos.getY();
			switch(state.get(SlabBlock.TYPE))
			{
				case TOP:
					return 0.5 < relativeEntityPosition&&relativeEntityPosition < 1;
				case BOTTOM:
					return 0 < relativeEntityPosition&&relativeEntityPosition < 0.5;
				case DOUBLE:
					return true;
			}
		}
		return false;
	}
}