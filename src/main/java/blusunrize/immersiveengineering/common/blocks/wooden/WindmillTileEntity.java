/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.IRotationAcceptor;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasObjProperty;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.items.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.loot.LootContext.Builder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

public class WindmillTileEntity extends IEBaseTileEntity implements ITickableTileEntity, IStateBasedDirectional, ITileDrop, IPlayerInteraction, IHasObjProperty
{
	public static TileEntityType<WindmillTileEntity> TYPE;
	public float prevRotation = 0;
	public float rotation = 0;
	public float turnSpeed = 0;
	public float perTick = 0;
	public int sails = 0;

	public boolean canTurn = false;

	public WindmillTileEntity()
	{
		super(TYPE);
	}

	@Override
	public void tick()
	{
		if(world.getGameTime()%128==((getPos().getX()^getPos().getZ())&127))
			canTurn = checkArea();
		if(!canTurn)
			return;

		double mod = .00005;
		if(!world.isRaining())
			mod *= .75;
		if(!world.isThundering())
			mod *= .66;
		mod *= getSpeedModifier();


		prevRotation = (float)(turnSpeed*mod);
		rotation += turnSpeed*mod;
		rotation %= 1;
		perTick = (float)(turnSpeed*mod);

		if(!world.isRemote)
		{
			TileEntity tileEntity = Utils.getExistingTileEntity(world, pos.offset(getFacing()));
			if(tileEntity instanceof IRotationAcceptor)
			{
				IRotationAcceptor dynamo = (IRotationAcceptor)tileEntity;
				double power = turnSpeed*mod*800;
				dynamo.inputRotation(Math.abs(power), getFacing());
			}
		}
	}

	protected float getSpeedModifier()
	{
		return .5f+sails*.125f;
	}

	public boolean checkArea()
	{
		if(getFacing().getAxis()==Direction.Axis.Y)
			return false;

		turnSpeed = 0;
		for(int hh = -4; hh <= 4; hh++)
		{
			int r = Math.abs(hh)==4?1: Math.abs(hh)==3?2: Math.abs(hh)==2?3: 4;
			for(int ww = -r; ww <= r; ww++)
				if((hh!=0||ww!=0)&&!world.isAirBlock(getPos().add((getFacing().getAxis()==Axis.Z?ww: 0), hh, (getFacing().getAxis()==Axis.Z?0: ww))))
					return false;
		}

		int blocked = 0;
		for(int hh = -4; hh <= 4; hh++)
		{
			int r = Math.abs(hh)==4?1: Math.abs(hh)==3?2: Math.abs(hh)==2?3: 4;
			for(int ww = -r; ww <= r; ww++)
			{
				for(int dd = 1; dd < 8; dd++)
				{
					BlockPos pos = getPos().add(0, hh, 0).offset(getFacing().getOpposite(), dd).offset(getFacing().rotateY(), ww);
					if(!world.isBlockLoaded(pos)||world.isAirBlock(pos))
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
				return false;
		}

		return true;
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		setFacing(Direction.byIndex(nbt.getInt("facing")));
		sails = nbt.getInt("sails");
		//prevRotation = nbt.getFloat("prevRotation");
		rotation = nbt.getFloat("rotation");
		turnSpeed = nbt.getFloat("turnSpeed");
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.putInt("facing", getFacing().ordinal());
		nbt.putInt("sails", sails);
		//nbt.putFloat("prevRotation", prevRotation);
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
			renderAABB = new AxisAlignedBB(getPos().getX()-(getFacing().getAxis()==Axis.Z?6: 0), getPos().getY()-6, getPos().getZ()-(getFacing().getAxis()==Axis.Z?0: 6), getPos().getX()+(getFacing().getAxis()==Axis.Z?7: 0), getPos().getY()+7, getPos().getZ()+(getFacing().getAxis()==Axis.Z?0: 7));
		return renderAABB;
	}

	@Override
	public EnumProperty<Direction> getFacingProperty()
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
		return false;
	}

	@Override
	public boolean canHammerRotate(Direction side, float hitX, float hitY, float hitZ, LivingEntity entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return false;
	}

	static ArrayList<String> emptyDisplayList = new ArrayList();

	@Override
	public ArrayList<String> compileDisplayList()
	{
		return emptyDisplayList;
	}

	@Override
	public boolean interact(Direction side, PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(sails < 8&&heldItem.getItem()==Ingredients.windmillSail)
		{
			this.sails++;
			heldItem.shrink(1);
			return true;
		}
		return false;
	}

	@Override
	public List<ItemStack> getTileDrops(Builder context)
	{
		ItemStack stack = new ItemStack(getBlockState().getBlock());
		if(sails > 0)
			ItemNBTHelper.putInt(stack, "sails", sails);
		return ImmutableList.of(stack);
	}

	@Override
	public void readOnPlacement(LivingEntity placer, ItemStack stack)
	{
		if(ItemNBTHelper.hasKey(stack, "sails"))
			this.sails = ItemNBTHelper.getInt(stack, "sails");
	}
}