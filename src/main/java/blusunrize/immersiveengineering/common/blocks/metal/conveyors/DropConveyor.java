/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.api.tool.conveyor.BasicConveyorType;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.IConveyorBlockEntity;
import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorType;
import blusunrize.immersiveengineering.api.utils.ItemUtils;
import blusunrize.immersiveengineering.client.render.conveyor.BasicConveyorRender;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.capabilities.Capabilities;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

/**
 * @author BluSunrize - 20.08.2016
 */
public class DropConveyor extends ConveyorBase
{
	public static final ResourceLocation NAME = new ResourceLocation(MODID, "dropper");
	public static ResourceLocation texture_on = new ResourceLocation("immersiveengineering:block/conveyor/dropper");
	public static ResourceLocation texture_off = new ResourceLocation("immersiveengineering:block/conveyor/dropper_off");
	public static final IConveyorType<DropConveyor> TYPE = new BasicConveyorType<>(
			NAME, false, true, DropConveyor::new, () -> new BasicConveyorRender<>(texture_on, texture_off)
	);

	/// Items will be spawned when this space is empty in the block below:
	private static final VoxelShape REQUIRED_SPACE = Shapes.box(0.25, 0.75, 0.25, 0.75, 1.0, 0.75);
	private VoxelShape cachedDownShape = Shapes.empty();
	private boolean cachedOpenBelow = true;

	public DropConveyor(BlockEntity tile)
	{
		super(tile);
	}

	@Override
	public IConveyorType<DropConveyor> getType()
	{
		return TYPE;
	}

	@Override
	public void handleInsertion(ItemEntity entity, ConveyorDirection conDir, double distX, double distZ)
	{
		if(!isPowered())
		{
			BlockPos posDown = getBlockEntity().getBlockPos().below();
			BlockEntity inventoryTile = getBlockEntity().getLevel().getBlockEntity(posDown);
			boolean contact = Math.abs(getFacing().getAxis()==Axis.Z?(getBlockEntity().getBlockPos().getZ()+.5-entity.getZ()):
					(getBlockEntity().getBlockPos().getX()+.5-entity.getX())) < .2;

			if(contact&&!(inventoryTile instanceof IConveyorBlockEntity))
				ItemUtils.tryInsertEntity(getBlockEntity().getLevel(), posDown, Direction.UP, entity);
			if(entity.isAlive()&&contact&&isEmptySpace(getBlockEntity().getLevel(), posDown, inventoryTile))
			{
				entity.setDeltaMovement(0, entity.getDeltaMovement().y, 0);
				entity.setPos(getBlockEntity().getBlockPos().getX()+.5, getBlockEntity().getBlockPos().getY()-.5, getBlockEntity().getBlockPos().getZ()+.5);
				if(!(inventoryTile instanceof IConveyorBlockEntity))
					ConveyorHandler.revertMagnetSuppression(entity, (IConveyorBlockEntity)getBlockEntity());
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
		if(tile instanceof IConveyorBlockEntity)
			return true;
		// Inventories should only be targeted by insertion, not dropping
		if(tile!=null&&tile.getCapability(Capabilities.ITEM_HANDLER, Direction.UP).isPresent())
			return false;
		BlockState state = world.getBlockState(pos);
		VoxelShape shape = state.getCollisionShape(world, pos);
		// Combining voxelshapes is a little expensive, so only calculate
		// when the voxelshape changes. Identity compare is sufficent since they're
		// usually precomputed.
		if(shape!=cachedDownShape)
		{
			cachedOpenBelow = !Shapes.joinIsNotEmpty(REQUIRED_SPACE, shape, BooleanOp.AND);
			cachedDownShape = shape;
		}
		return cachedOpenBelow;
	}
}
