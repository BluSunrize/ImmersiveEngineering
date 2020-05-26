/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.blocks.IETileProviderBlock;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class StripCurtainBlock extends IETileProviderBlock
{
	public static BooleanProperty CEILING_ATTACHED = BooleanProperty.create("ceiling_attached");
	public static EnumProperty<Direction> FACING = IEProperties.FACING_HORIZONTAL;

	public StripCurtainBlock()
	{
		super("strip_curtain", Block.Properties.create(Material.WOOL).hardnessAndResistance(0.8F).notSolid(),
				BlockItemIE::new, CEILING_ATTACHED, FACING);
		setLightOpacity(0);
		setHasColours();
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world)
	{
		return new StripCurtainTileEntity();
	}

	/*TODO why is this here? Added in the original 1.8 commit
		@Override
		@OnlyIn(Dist.CLIENT)
		public int getRenderColour(IBlockState state, @Nullable IBlockReader worldIn, @Nullable BlockPos pos, int tintIndex)
		{
			return 0xFFFFFF;
		}*/

	@Override
	public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced)
	{
		if(ItemNBTHelper.hasKey(stack, "colour"))
		{
			String hexCol = Integer.toHexString(ItemNBTHelper.getInt(stack, "colour"));
			tooltip.add(new TranslationTextComponent(Lib.DESC_INFO+"colour", "<hexcol="+hexCol+":#"+hexCol+">"));
		}
	}

	@Override
	public boolean allowHammerHarvest(BlockState blockState)
	{
		return true;
	}
}
