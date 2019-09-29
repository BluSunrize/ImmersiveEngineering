/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.function.Predicate;

public class RotationUtil
{
	public static HashSet<Predicate<BlockState>> permittedRotation = new HashSet<>();
	public static HashSet<Predicate<TileEntity>> permittedTileRotation = new HashSet<>();

	static
	{
		permittedRotation.add(state -> {
			//preventing extended pistons from rotating
			return !((state.getBlock()==Blocks.PISTON||state.getBlock()==Blocks.STICKY_PISTON)&&state.get(PistonBlock.EXTENDED));
		});
		permittedRotation.add(state -> {
			//beds don't like being rotated piecewise
			return !(state.getBlock() instanceof BedBlock);
		});
		/*permittedRotation.add(state -> {
			//A lot of the RS stuff breaks when rotated
			Block b = state.getBlock();
			return b!=Blocks.TRIPWIRE_HOOK&&b!=Blocks.STONE_BUTTON&&b!=Blocks.WOODEN_BUTTON&&b!=Blocks.LEVER&&b!=Blocks.REDSTONE_TORCH;
		});
		permittedRotation.add(state -> {
			//misc things don't like floating in the air...
			Block b = state.getBlock();
			return b!=Blocks.TORCH&&b!=Blocks.LADDER&&b!=Blocks.WALL_SIGN&&b!=Blocks.WALL_BANNER;
		});*/
		permittedRotation.add(state -> {
			//preventing endportals, skulls from rotating
			return !(state.getBlock()==Blocks.END_PORTAL_FRAME||state.getBlock() instanceof SkullBlock);
		});
		permittedTileRotation.add(tile -> {
			//preventing double chests from rotating
			if(tile instanceof ChestTileEntity)
				return tile.getBlockState().get(ChestBlock.TYPE)==ChestType.SINGLE;
			return true;
		});
	}

	public static boolean rotateBlock(World world, BlockPos pos, Direction axis)
	{
		BlockState state = world.getBlockState(pos);
		for(Predicate<BlockState> pred : permittedRotation)
			if(!pred.test(state))
				return false;
		if(state.getBlock().hasTileEntity(state))
		{
			TileEntity tile = world.getTileEntity(pos);
			if(tile!=null)
				for(Predicate<TileEntity> pred : permittedTileRotation)
					if(!pred.test(tile))
						return false;
		}
		//TODO this is not the right kind of rotation...
		BlockState newState = state.rotate(world, pos, Rotation.CLOCKWISE_90);
		if(newState!=state)
		{
			world.setBlockState(pos, newState);
			return true;
		}
		else
			return false;
	}

	public static boolean rotateEntity(Entity entity, PlayerEntity player)
	{
		if(entity instanceof ArmorStandEntity)
		{
//			float f = (float)MathHelper.floor_float((MathHelper.wrapAngleTo180_float(playerIn.rotationYaw - 180.0F) + 22.5F) / 45.0F) * 45.0F;
//			((EntityArmorStand)entity).rotationYaw+=22.5;
//			((EntityArmorStand)entity).rotationYaw%=360;
		}
		return false;
	}
}