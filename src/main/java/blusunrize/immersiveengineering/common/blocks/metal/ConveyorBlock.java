/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorBelt;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import blusunrize.immersiveengineering.common.blocks.IETileProviderBlock;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class ConveyorBlock extends IETileProviderBlock implements ConveyorHandler.IConveyorBlock
{
	public static final Supplier<Properties> PROPERTIES = () -> Properties.of(Material.METAL)
			.sound(SoundType.METAL)
			.strength(3.0F, 15.0F)
			.noOcclusion();

	private final ResourceLocation typeName;
	public static final EnumProperty<Direction> FACING = IEProperties.FACING_HORIZONTAL;

	public ConveyorBlock(ResourceLocation type, Properties props)
	{
		super(props);
		this.typeName = type;
		lightOpacity = 0;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(FACING, BlockStateProperties.WATERLOGGED);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag advanced)
	{
		String flavourKey = getDescriptionId()+".flavour";
		if(I18n.exists(flavourKey))
			tooltip.add(new TranslatableComponent(flavourKey));
	}

	@Override
	public void onIEBlockPlacedBy(BlockPlaceContext context, BlockState state)
	{
		super.onIEBlockPlacedBy(context, state);
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockEntity tile = world.getBlockEntity(pos);
		if(tile instanceof ConveyorBeltTileEntity)
		{
			ConveyorBeltTileEntity conveyor = (ConveyorBeltTileEntity)tile;
			Direction f = conveyor.getFacing();
			tile = world.getBlockEntity(pos.relative(f));
			BlockEntity tileUp = world.getBlockEntity(pos.relative(f).offset(0, 1, 0));
			IConveyorBelt subType = conveyor.getConveyorSubtype();
			if(subType!=null&&(!(tile instanceof IConveyorTile)||((IConveyorTile)tile).getFacing()==f.getOpposite())
					&&tileUp instanceof IConveyorTile&&((IConveyorTile)tileUp).getFacing()!=f.getOpposite()
					&&world.isEmptyBlock(pos.offset(0, 1, 0)))
				subType.setConveyorDirection(ConveyorDirection.UP);
		}
	}

	@Override
	public BlockEntity createTileEntity(@Nonnull BlockState state, @Nonnull BlockGetter world)
	{
		return new ConveyorBeltTileEntity(typeName);
	}

	@Override
	public boolean allowHammerHarvest(BlockState blockState)
	{
		return true;
	}

	@Override
	public ResourceLocation getTypeName()
	{
		return typeName;
	}
}