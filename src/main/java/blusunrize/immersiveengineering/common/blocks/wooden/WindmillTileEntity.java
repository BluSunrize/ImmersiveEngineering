/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.api.energy.IRotationAcceptor;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.api.utils.shapes.CachedVoxelShapes;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.items.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.temp.IETickableBlockEntity;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class WindmillTileEntity extends IEBaseTileEntity implements IETickableBlockEntity, IStateBasedDirectional,
		IReadOnPlacement, IPlayerInteraction, IHasObjProperty, IBlockBounds
{
	public float rotation = 0;
	public float turnSpeed = 0;
	public int sails = 0;

	public WindmillTileEntity()
	{
		super(IETileTypes.WINDMILL.get());
	}

	@Override
	public void tickCommon()
	{
		rotation += getActualTurnSpeed();
		rotation %= 1;
	}

	public double getActualTurnSpeed() {
		if (turnSpeed == 0)
			return 0;
		double mod = .00005;
		if(!world.isRaining())
			mod *= .75;
		if(!world.isThundering())
			mod *= .66;
		mod *= getSpeedModifier();
		return mod * turnSpeed;
	}

	@Override
	public void tickServer()
	{
		if(world.getGameTime()%128==((getPos().getX()^getPos().getZ())&127))
		{
			final float oldTurnSpeed = turnSpeed;
			turnSpeed = computeTurnSpeed();
			if(oldTurnSpeed!=turnSpeed)
				markContainingBlockForUpdate(null);
		}
		if(turnSpeed == 0)
			return;

		TileEntity tileEntity = SafeChunkUtils.getSafeTE(world, pos.offset(getFacing().getOpposite()));
		if(tileEntity instanceof IRotationAcceptor)
		{
			IRotationAcceptor dynamo = (IRotationAcceptor)tileEntity;
			double power = getActualTurnSpeed()*800;
			dynamo.inputRotation(Math.abs(power), getFacing().getOpposite());
		}
	}

	protected float getSpeedModifier()
	{
		return .5f+sails*.125f;
	}

	public float computeTurnSpeed()
	{
		Direction facing = getFacing();
		if(facing.getAxis()==Direction.Axis.Y)
			return 0;

		float turnSpeed = 0;
		for(int hh = -4; hh <= 4; hh++)
		{
			int r = Math.abs(hh)==4?1: Math.abs(hh)==3?2: Math.abs(hh)==2?3: 4;
			for(int ww = -r; ww <= r; ww++)
				if((hh!=0||ww!=0)&&!world.isAirBlock(getPos().offset(facing.rotateY(), ww).up(hh)))
					return 0;
		}

		int blocked = 0;
		for(int hh = -4; hh <= 4; hh++)
		{
			int r = Math.abs(hh)==4?1: Math.abs(hh)==3?2: Math.abs(hh)==2?3: 4;
			for(int ww = -r; ww <= r; ww++)
			{
				for(int dd = 1; dd < 8; dd++)
				{
					BlockPos pos = getPos().up(hh)
							.offset(facing, dd)
							.offset(facing.rotateY(), ww);
					if(!world.isAreaLoaded(pos, 1)||world.isAirBlock(pos))
						turnSpeed++;
					else if(world.getTileEntity(pos) instanceof WindmillTileEntity)
					{
						blocked += 20;
						turnSpeed -= 179;
					}
					else
						blocked++;
				}
			}
			if(blocked > 100)
				return 0;
		}

		return turnSpeed;
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		sails = nbt.getInt("sails");
		rotation = nbt.getFloat("rotation");
		turnSpeed = nbt.getFloat("turnSpeed");
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.putInt("sails", sails);
		nbt.putFloat("rotation", rotation);
		nbt.putFloat("turnSpeed", turnSpeed);
	}

	@OnlyIn(Dist.CLIENT)
	private AxisAlignedBB renderAABB;

	@OnlyIn(Dist.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(renderAABB==null)
		{
			Direction facing = getFacing();
			renderAABB = new AxisAlignedBB(
					getPos().getX()-(facing.getAxis()==Axis.Z?6: 0),
					getPos().getY()-6,
					getPos().getZ()-(facing.getAxis()==Axis.Z?0: 6),
					getPos().getX()+(facing.getAxis()==Axis.Z?7: 0),
					getPos().getY()+7,
					getPos().getZ()+(facing.getAxis()==Axis.Z?0: 7)
			);
		}
		return renderAABB;
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL_PREFER_SIDE;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return true;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vector3d hit, LivingEntity entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return false;
	}

	@Override
	public VisibilityList compileDisplayList(BlockState state)
	{
		return VisibilityList.hideAll();
	}

	@Override
	public boolean interact(Direction side, PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(sails < 8&&heldItem.getItem()==Ingredients.windmillSail.asItem())
		{
			this.sails++;
			if(!player.abilities.isCreativeMode)
				heldItem.shrink(1);
			return true;
		}
		return false;
	}

	@Override
	public void readOnPlacement(LivingEntity placer, ItemStack stack)
	{
		if(ItemNBTHelper.hasKey(stack, "sails"))
			this.sails = ItemNBTHelper.getInt(stack, "sails");
	}

	private static final CachedVoxelShapes<Direction> SHAPES = new CachedVoxelShapes<>(WindmillTileEntity::getShape);

	private static List<AxisAlignedBB> getShape(Direction key)
	{
		return Lists.newArrayList(
				key.getAxis()==Axis.Z?new AxisAlignedBB(.0625, .0625, 0, .9375, .9375, 1):
						new AxisAlignedBB(0, .0625, .0625, 1, .9375, .9375)
		);
	}

	@Nonnull
	@Override
	public VoxelShape getBlockBounds(@Nullable ISelectionContext ctx)
	{
		return SHAPES.get(this.getFacing());
	}
}