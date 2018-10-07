/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.energy.IRotationAcceptor;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasObjProperty;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;

public class TileEntityWindmill extends TileEntityIEBase implements ITickable, IDirectionalTile, ITileDrop, IPlayerInteraction, IHasObjProperty
{
	public EnumFacing facing = EnumFacing.NORTH;
	public float prevRotation = 0;
	public float rotation = 0;
	public float turnSpeed = 0;
	public float perTick = 0;
	public int sails = 0;

	public boolean canTurn = false;

//	@Override
//	public boolean hasFastRenderer()
//	{
//		return true;
//	}

	@Override
	public void update()
	{
		if(world.getTotalWorldTime()%128==((getPos().getX()^getPos().getZ())&127))
			canTurn = checkArea();
		if(!canTurn)
			return;

		double mod = .00005;
		if(!world.isRaining())
			mod *= .75;
		if(!world.isThundering())
			mod *= .66;
//		if(getPos().getY()>200)
//			mod *= 2;
//		else if(getPos().getY()>150)
//			mod *= 1.5;
//		else if(getPos().getY()>100)
//			mod *= 1.25;
//		else if(getPos().getY()<70)
//			mod *= .33;
		mod *= getSpeedModifier();


		prevRotation = (float)(turnSpeed*mod);
		rotation += turnSpeed*mod;
		rotation %= 1;
		perTick = (float)(turnSpeed*mod);

		if(!world.isRemote)
		{
			TileEntity tileEntity = Utils.getExistingTileEntity(world, pos.offset(facing));
			if(tileEntity instanceof IRotationAcceptor)
			{
				IRotationAcceptor dynamo = (IRotationAcceptor)tileEntity;
				double power = turnSpeed*mod*800;
				dynamo.inputRotation(Math.abs(power), facing);
			}
		}
	}

	protected float getSpeedModifier()
	{
		return .5f+sails*.125f;
	}

	public boolean checkArea()
	{
		if(facing.getAxis()==EnumFacing.Axis.Y)
			return false;

		turnSpeed = 0;
		for(int hh = -4; hh <= 4; hh++)
		{
			int r = Math.abs(hh)==4?1: Math.abs(hh)==3?2: Math.abs(hh)==2?3: 4;
			for(int ww = -r; ww <= r; ww++)
				if((hh!=0||ww!=0)&&!world.isAirBlock(getPos().add((facing.getAxis()==Axis.Z?ww: 0), hh, (facing.getAxis()==Axis.Z?0: ww))))
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
					BlockPos pos = getPos().add(0, hh, 0).offset(facing.getOpposite(), dd).offset(facing.rotateY(), ww);
					if(!world.isBlockLoaded(pos)||world.isAirBlock(pos))
						turnSpeed++;
					else if(world.getTileEntity(pos) instanceof TileEntityWindmill)
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
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		facing = EnumFacing.byIndex(nbt.getInteger("facing"));
		sails = nbt.getInteger("sails");
		//prevRotation = nbt.getFloat("prevRotation");
		rotation = nbt.getFloat("rotation");
		turnSpeed = nbt.getFloat("turnSpeed");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("facing", facing.ordinal());
		nbt.setInteger("sails", sails);
		//nbt.setFloat("prevRotation", prevRotation);
		nbt.setFloat("rotation", rotation);
		nbt.setFloat("turnSpeed", turnSpeed);
	}

	@SideOnly(Side.CLIENT)
	private AxisAlignedBB renderAABB;

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(renderAABB==null)
			renderAABB = new AxisAlignedBB(getPos().getX()-(facing.getAxis()==Axis.Z?6: 0), getPos().getY()-6, getPos().getZ()-(facing.getAxis()==Axis.Z?0: 6), getPos().getX()+(facing.getAxis()==Axis.Z?7: 0), getPos().getY()+7, getPos().getZ()+(facing.getAxis()==Axis.Z?0: 7));
		return renderAABB;
	}

	@Override
	public EnumFacing getFacing()
	{
		return facing;
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 6;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(EnumFacing axis)
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
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(sails < 8&&OreDictionary.itemMatches(new ItemStack(IEContent.itemMaterial, 1, 12), heldItem, false))
		{
			this.sails++;
			heldItem.shrink(1);
			return true;
		}
		return false;
	}

	@Override
	public ItemStack getTileDrop(EntityPlayer player, IBlockState state)
	{
		ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
		if(sails > 0)
			ItemNBTHelper.setInt(stack, "sails", sails);
		return stack;
	}

	@Override
	public void readOnPlacement(EntityLivingBase placer, ItemStack stack)
	{
		if(ItemNBTHelper.hasKey(stack, "sails"))
			this.sails = ItemNBTHelper.getInt(stack, "sails");
	}
}