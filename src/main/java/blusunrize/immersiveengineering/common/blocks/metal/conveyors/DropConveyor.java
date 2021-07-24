/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

/**
 * @author BluSunrize - 20.08.2016
 */
public class DropConveyor extends BasicConveyor
{
	public static final ResourceLocation NAME = new ResourceLocation(MODID, "dropper");
	/// Items will be spawned when this space is empty in the block below:
	private static final VoxelShape REQUIRED_SPACE = Shapes.box(0.25, 0.75, 0.25, 0.75, 1.0, 0.75);
	private VoxelShape cachedDownShape = Shapes.empty();
	private boolean cachedOpenBelow = true;

	public DropConveyor(BlockEntity tile)
	{
		super(tile);
	}

	@Override
	public void handleInsertion(ItemEntity entity, ConveyorDirection conDir, double distX, double distZ)
	{
		if(!isPowered())
		{
			BlockPos posDown = getTile().getBlockPos().below();
			BlockEntity inventoryTile = getTile().getLevel().getBlockEntity(posDown);
			boolean contact = Math.abs(getFacing().getAxis()==Axis.Z?(getTile().getBlockPos().getZ()+.5-entity.getZ()):
				(getTile().getBlockPos().getX()+.5-entity.getX())) < .2;

		LazyOptional<IItemHandler> cap = LazyOptional.empty();
		if(contact&&!(inventoryTile instanceof IConveyorTile))
			cap = CapabilityUtils.findItemHandlerAtPos(getTile().getLevel(), posDown, Direction.UP, true);

			if(cap.isPresent())
				cap.ifPresent(itemHandler ->
				{
					ItemStack stack = entity.getItem();
					ItemStack temp = ItemHandlerHelper.insertItem(itemHandler, stack.copy(), true);
					if(temp.isEmpty()||temp.getCount() < stack.getCount())
					{
						temp = ItemHandlerHelper.insertItem(itemHandler, stack, false);
						if(temp.isEmpty())
							entity.discard();
						else if(temp.getCount() < stack.getCount())
							entity.setItem(temp);
					}
				});
			else if(contact&&isEmptySpace(getTile().getLevel(), posDown, inventoryTile))
			{
				entity.setDeltaMovement(0, entity.getDeltaMovement().y, 0);
				entity.setPos(getTile().getBlockPos().getX()+.5, getTile().getBlockPos().getY()-.5, getTile().getBlockPos().getZ()+.5);
				if(!(inventoryTile instanceof IConveyorTile))
					ConveyorHandler.revertMagnetSuppression(entity, (IConveyorTile)getTile());
			}
			else
				super.handleInsertion(entity, conDir, distX, distZ);
		}
		else
			super.handleInsertion(entity, conDir, distX, distZ);
	}

	boolean isEmptySpace(Level world, BlockPos pos, BlockEntity tile)
	{
		// Special case conveyors, so items can be dropped through covered ones.
		if(tile instanceof IConveyorTile)
			return true;
		BlockState state = world.getBlockState(pos);
		VoxelShape shape = state.getCollisionShape(world, pos);
		// Combining voxelshapes is a little expensive, so only calculate
		// when the voxelshape changes. Identity compare is sufficent since they're
		// usually precomputed.
		if (shape != cachedDownShape)
		{
			cachedOpenBelow = !Shapes.joinIsNotEmpty(REQUIRED_SPACE, shape, BooleanOp.AND);
			cachedDownShape = shape;
		}
		return cachedOpenBelow;
	}

	public static ResourceLocation texture_on = new ResourceLocation("immersiveengineering:block/conveyor/dropper");
	public static ResourceLocation texture_off = new ResourceLocation("immersiveengineering:block/conveyor/dropper_off");

	@Override
	public ResourceLocation getActiveTexture()
	{
		return texture_on;
	}

	@Override
	public ResourceLocation getInactiveTexture()
	{
		return texture_off;
	}
}
