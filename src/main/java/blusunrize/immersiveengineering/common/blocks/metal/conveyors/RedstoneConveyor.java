/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.api.tool.conveyor.BasicConveyorType;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorType;
import blusunrize.immersiveengineering.client.render.conveyor.RedstoneConveyorRender;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

/**
 * @author BluSunrize - 06.05.2017
 */
public class RedstoneConveyor extends ConveyorBase
{
	public static final ResourceLocation NAME = new ResourceLocation(MODID, "redstone");
	public static final IConveyorType<RedstoneConveyor> TYPE = new BasicConveyorType<>(
			NAME, false, true, RedstoneConveyor::new, () -> new RedstoneConveyorRender(texture_on, texture_off), false
	);

	private boolean panelRight = true;

	public RedstoneConveyor(BlockEntity tile)
	{
		super(tile);
	}

	@Override
	public IConveyorType<RedstoneConveyor> getType()
	{
		return TYPE;
	}

	/* Prevent Diagonals */

	@Override
	public boolean changeConveyorDirection()
	{
		return false;
	}

	@Override
	public boolean setConveyorDirection(ConveyorDirection dir)
	{
		return false;
	}

	/* Redstone & Player Interaction */

	@Override
	public boolean isActive()
	{
		return !isPowered();
	}

	@Override
	public boolean playerInteraction(Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ, Direction side)
	{
		if(super.playerInteraction(player, hand, heldItem, hitX, hitY, hitZ, side))
			return true;
		if(Utils.isHammer(heldItem)&&player.isShiftKeyDown())
		{
			panelRight = !panelRight;
			return true;
		}
		return false;
	}

	/* NBT */

	@Override
	public CompoundTag writeConveyorNBT()
	{
		CompoundTag nbt = super.writeConveyorNBT();
		nbt.putBoolean("panelRight", panelRight);
		return nbt;
	}

	@Override
	public void readConveyorNBT(CompoundTag nbt)
	{
		super.readConveyorNBT(nbt);
		panelRight = nbt.getBoolean("panelRight");
	}

	/* Selection Box */

	private static final Map<Direction, VoxelShape> WALL_SHAPES = Maps.newEnumMap(ImmutableMap.of(
			Direction.NORTH, Block.box(13, 2, 5, 16, 16, 14.5),
			Direction.SOUTH, Block.box(0, 2, 1.5, 3, 16, 11),
			Direction.WEST, Block.box(5, 2, 0, 14.5, 16, 3),
			Direction.EAST, Block.box(1.5, 2, 13, 11, 16, 16)));

	@Override
	public VoxelShape getSelectionShape()
	{
		VoxelShape ret = conveyorBounds;
		VoxelShape extensionShape = WALL_SHAPES.get(panelRight?getFacing(): getFacing().getOpposite());
		if(extensionShape!=null)
			ret = Shapes.joinUnoptimized(ret, extensionShape, BooleanOp.OR);
		return ret;
	}

	public boolean isPanelRight()
	{
		return panelRight;
	}
}
