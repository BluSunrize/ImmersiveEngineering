package blusunrize.immersiveengineering.common.util;

import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RotationUtil
{
	public enum RotationType
	{
		SIXWAYS,
		DIRECTIONAL,
		HORIZONTAL,
		STAIRS,
		RAIL,
		CHEST,
		SIGN
	}
	public static RotationType getRotationType(World world, BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);
		if(state!=null && state.getBlock()!=null)
		{
			EnumFacing[] valid = state.getBlock().getValidRotations(world, pos);
//			return valid!=null&&valid.length>0;
//			if(state.getBlock().equals(Blocks.piston)||state.getBlock().equals(Blocks.sticky_piston))
//				return RotationType.SIXWAYS;
//			else if(state.getBlock().equals(Blocks.dispenser)||state.getBlock().equals(Blocks.dropper))
//				return RotationType.SIXWAYS;
//			else if(state.getBlock() instanceof BlockDirectional && !state.getBlock().equals(Blocks.bed) && !state.getBlock().equals(Blocks.cocoa))
//				return RotationType.DIRECTIONAL;
//			else if(state.getBlock().equals(Blocks.furnace)||state.getBlock().equals(Blocks.lit_furnace)||state.getBlock().equals(Blocks.ender_chest))
//				return RotationType.HORIZONTAL;
//			else if(state.getBlock() instanceof BlockStairs)
//				return RotationType.STAIRS;
//			else if(state.getBlock() instanceof BlockRail)
//				return RotationType.RAIL;
//			else if(state.getBlock().equals(Blocks.chest)||state.getBlock().equals(Blocks.trapped_chest))
//				return RotationType.CHEST;
//			else if(state.getBlock().equals(Blocks.standing_sign))
//				return RotationType.SIGN;
		}
		return null;
	}
	public static boolean rotateBlock(World world, BlockPos pos, EnumFacing side, EntityPlayer player)
	{
		IBlockState state = world.getBlockState(pos);
//		RotationType type = getRotationType(world, pos);
//		if(type==null)
//			return false;
		PropertyDirection propDir = null;
		EnumFacing facing = null;
		return state.getBlock().rotateBlock(world, pos, side);

//		switch(type)
//		{
//		case SIXWAYS:
//			for(IProperty prop : state.getPropertyNames())
//				if(prop instanceof PropertyDirection)
//				{
//					propDir = (PropertyDirection)prop;
//					break;
//				}
//			if(propDir!=null)
//			{
//				facing = state.getValue(propDir);
//				EnumFacing fNew = facing.rotateAround(side.getAxis());
//				if(fNew!=facing)
//				{
//					if(side.getAxisDirection()==AxisDirection.NEGATIVE|player.isSneaking())
//						fNew = fNew.getOpposite();
//					state = state.withProperty(propDir, fNew);
//					world.setBlockState(pos, state);
//					return true;
//				}
//			}
//			break;
//		case DIRECTIONAL:
//			facing = state.getValue(BlockDirectional.FACING);
//			state = state.withProperty(BlockDirectional.FACING, player.isSneaking()?facing.rotateYCCW():facing.rotateY());
//			world.setBlockState(pos, state);
//			return true;
//		case HORIZONTAL:
//			for(IProperty prop : state.getPropertyNames())
//				if(prop instanceof PropertyDirection)
//				{
//					propDir = (PropertyDirection)prop;
//					break;
//				}
//			if(propDir!=null)
//			{
//				facing = state.getValue(propDir);
//				state = state.withProperty(propDir, player.isSneaking()?facing.rotateYCCW():facing.rotateY());
//				world.setBlockState(pos, state);
//				return true;
//			}
//			break;
//		case STAIRS:
//			for(IProperty prop : state.getPropertyNames())
//				if(prop instanceof PropertyDirection)
//				{
//					propDir = (PropertyDirection)prop;
//					break;
//				}
//			if(propDir!=null)
//			{
//				facing = state.getValue(propDir);
//				state = state.withProperty(propDir, player.isSneaking()?facing.rotateYCCW():facing.rotateY());
//				world.setBlockState(pos, state);
//				return true;
//			}
//			break;
//		case RAIL:
//			EnumRailDirection railDir = state.getValue(BlockRail.SHAPE);
//			EnumRailDirection newRailDir = railDir;
//			if(railDir==EnumRailDirection.NORTH_SOUTH)
//				newRailDir = EnumRailDirection.EAST_WEST;
//			if(railDir==EnumRailDirection.EAST_WEST)
//				newRailDir = EnumRailDirection.NORTH_SOUTH;
//			//			else if(railDir.isAscending())
//			//				newRailDir = railDir==EnumRailDirection.ASCENDING_NORTH?EnumRailDirection.ASCENDING_EAST: railDir==EnumRailDirection.ASCENDING_EAST?EnumRailDirection.ASCENDING_SOUTH: EnumRailDirection.ASCENDING_NORTH;
//			if(newRailDir!=railDir)
//			{
//				state = state.withProperty(BlockRail.SHAPE, newRailDir);
//				world.setBlockState(pos, state);
//				return true;
//			}
//			break;
//		case CHEST:
//			facing = state.getValue(BlockChest.FACING);
//			for(EnumFacing offset : EnumFacing.HORIZONTALS)
//				if(world.getBlockState(pos.offset(offset)).getBlock()==state.getBlock())
//				{
//					state = state.withProperty(BlockChest.FACING, facing.getOpposite());
//					world.setBlockState(pos, state);
//					world.setBlockState(pos.offset(offset), state);
//					return true;
//				}
//			state = state.withProperty(BlockChest.FACING, player.isSneaking()?facing.rotateYCCW():facing.rotateY());
//			world.setBlockState(pos, state);
//			return true;
//		case SIGN:
//			int signRotation = state.getValue(BlockStandingSign.ROTATION);
//			signRotation = state.getValue(BlockStandingSign.ROTATION)+(player.isSneaking()?15:1);
//			state = state.withProperty(BlockStandingSign.ROTATION, signRotation%16);
//			world.setBlockState(pos, state);
//			return true;
//		default:
//			break;
//		}
//		return false;
	}
	
	public static boolean rotateEntity(Entity entity, EntityPlayer player)
	{
		if(entity instanceof EntityArmorStand)
		{
//			float f = (float)MathHelper.floor_float((MathHelper.wrapAngleTo180_float(playerIn.rotationYaw - 180.0F) + 22.5F) / 45.0F) * 45.0F;
//			((EntityArmorStand)entity).rotationYaw+=22.5;
//			((EntityArmorStand)entity).rotationYaw%=360;
		}
		return false;
	}
}