/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.IConveyorBlockEntity;
import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorBelt;
import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorType;
import blusunrize.immersiveengineering.common.blocks.IEEntityBlock;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDecoration;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class ConveyorBlock extends IEEntityBlock<ConveyorBeltBlockEntity<?>> implements ConveyorHandler.IConveyorBlock
{
	public static final Supplier<Properties> PROPERTIES = () -> Properties.of()
			.mapColor(MapColor.METAL)
			.sound(SoundType.METAL)
			.strength(3.0F, 15.0F)
			.noOcclusion();
	public static final String DEFAULT_COVER = "defaultCover";

	private final IConveyorType<?> type;
	public static final EnumProperty<Direction> FACING = IEProperties.FACING_HORIZONTAL;

	public ConveyorBlock(IConveyorType<?> type, Properties props)
	{
		super((pos, state) -> new ConveyorBeltBlockEntity<>(type, pos, state), props);
		this.type = type;
		lightOpacity = 0;
	}

	@Override
	public void fillCreativeTab(Output out)
	{
		out.accept(this);
		if(type.acceptsCovers())
			out.accept(makeCovered(
					this, MetalDecoration.STEEL_SCAFFOLDING.get(MetalScaffoldingType.STANDARD).get()
			));
	}

	public static ItemStack makeCovered(ItemLike conveyor, Block cover)
	{
		ItemStack covered = new ItemStack(conveyor, 1);
		covered.getOrCreateTag().putString(DEFAULT_COVER, BuiltInRegistries.BLOCK.getKey(cover).toString());
		return covered;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(FACING, BlockStateProperties.WATERLOGGED);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag advanced)
	{
		String flavourKey = getDescriptionId()+".flavour";
		if(I18n.exists(flavourKey))
			tooltip.add(Component.translatable(flavourKey));
	}

	@Override
	public void onIEBlockPlacedBy(BlockPlaceContext context, BlockState state)
	{
		super.onIEBlockPlacedBy(context, state);
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockEntity tile = world.getBlockEntity(pos);
		if(tile instanceof ConveyorBeltBlockEntity conveyor)
		{
			IConveyorBelt conveyorInst = conveyor.getConveyorInstance();
			if(conveyorInst!=null)
				conveyorInst.setCover(getCover(context.getItemInHand()));
			Direction f = conveyor.getFacing();
			tile = world.getBlockEntity(pos.relative(f));
			BlockEntity tileUp = world.getBlockEntity(pos.relative(f).offset(0, 1, 0));
			IConveyorBelt subType = conveyor.getConveyorInstance();
			if(subType!=null&&(!(tile instanceof IConveyorBlockEntity outputConv)||outputConv.getFacing()==f.getOpposite())
					&&tileUp instanceof IConveyorBlockEntity convUp&&convUp.getFacing()!=f.getOpposite()
					&&world.isEmptyBlock(pos.offset(0, 1, 0)))
				subType.setConveyorDirection(ConveyorDirection.UP);
		}
	}

	public static Block getCover(ItemStack conveyor)
	{
		ResourceLocation coverID = new ResourceLocation(ItemNBTHelper.getString(conveyor, ConveyorBlock.DEFAULT_COVER));
		Block result = ForgeRegistries.BLOCKS.getValue(coverID);
		if(result!=null)
			return result;
		else
			return Blocks.AIR;
	}

	@Override
	public IConveyorType<?> getType()
	{
		return type;
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
	{
		if(this.type.isTicking())
			return super.getTicker(world, state, type);
		else
			return null;
	}
}