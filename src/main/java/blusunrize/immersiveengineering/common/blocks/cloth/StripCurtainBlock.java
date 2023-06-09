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
import blusunrize.immersiveengineering.client.utils.FontUtils;
import blusunrize.immersiveengineering.common.blocks.IEEntityBlock;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class StripCurtainBlock extends IEEntityBlock<StripCurtainBlockEntity>
{
	public static BooleanProperty CEILING_ATTACHED = BooleanProperty.create("ceiling_attached");
	public static EnumProperty<Direction> FACING = IEProperties.FACING_HORIZONTAL;
	public static final Supplier<Properties> PROPERTIES = () -> Block.Properties.of()
			.mapColor(MapColor.WOOL)
			.ignitedByLava()
			.sound(SoundType.WOOL)
			.strength(0.8F)
			.noOcclusion();

	public StripCurtainBlock(Properties props)
	{
		super(IEBlockEntities.STRIP_CURTAIN, props);
		setLightOpacity(0);
		setHasColours();
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(CEILING_ATTACHED, FACING);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag advanced)
	{
		if(ItemNBTHelper.hasKey(stack, "colour"))
		{
			int color = ItemNBTHelper.getInt(stack, "colour");
			tooltip.add(FontUtils.withAppendColoredColour(Component.translatable(Lib.DESC_INFO+"colour"), color));
		}
	}
}
