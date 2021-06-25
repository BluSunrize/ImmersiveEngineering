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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class ConveyorBlock extends IETileProviderBlock implements ConveyorHandler.IConveyorBlock
{
	public static final Supplier<Properties> PROPERTIES = () -> Properties.create(Material.IRON)
			.sound(SoundType.METAL)
			.hardnessAndResistance(3.0F, 15.0F)
			.notSolid();

	private final ResourceLocation typeName;
	public static final EnumProperty<Direction> FACING = IEProperties.FACING_HORIZONTAL;

	public ConveyorBlock(ResourceLocation type, Properties props)
	{
		super(props);
		this.typeName = type;
		lightOpacity = 0;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		super.fillStateContainer(builder);
		builder.add(FACING, BlockStateProperties.WATERLOGGED);
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced)
	{
		String flavourKey = getTranslationKey()+".flavour";
		if(I18n.hasKey(flavourKey))
			tooltip.add(new TranslationTextComponent(flavourKey));
	}

	@Override
	public void onIEBlockPlacedBy(BlockItemUseContext context, BlockState state)
	{
		super.onIEBlockPlacedBy(context, state);
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof ConveyorBeltTileEntity)
		{
			ConveyorBeltTileEntity conveyor = (ConveyorBeltTileEntity)tile;
			Direction f = conveyor.getFacing();
			tile = world.getTileEntity(pos.offset(f));
			TileEntity tileUp = world.getTileEntity(pos.offset(f).add(0, 1, 0));
			IConveyorBelt subType = conveyor.getConveyorSubtype();
			if(subType!=null&&(!(tile instanceof IConveyorTile)||((IConveyorTile)tile).getFacing()==f.getOpposite())
					&&tileUp instanceof IConveyorTile&&((IConveyorTile)tileUp).getFacing()!=f.getOpposite()
					&&world.isAirBlock(pos.add(0, 1, 0)))
				subType.setConveyorDirection(ConveyorDirection.UP);
		}
	}

	@Override
	public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world)
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