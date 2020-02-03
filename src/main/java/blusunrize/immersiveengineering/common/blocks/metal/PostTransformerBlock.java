/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Connectors;
import blusunrize.immersiveengineering.common.blocks.generic.MiscConnectorBlock;
import com.google.common.collect.ImmutableList;
import net.minecraft.item.Item;

public class PostTransformerBlock extends MiscConnectorBlock
{
	public PostTransformerBlock()
	{
		super("post_transformer", () -> PostTransformerTileEntity.TYPE,
				ImmutableList.of(IEProperties.FACING_HORIZONTAL),
				ImmutableList.of(), null);
	}

	@Override
	public Item asItem()
	{
		return Connectors.transformer.asItem();
	}
}
