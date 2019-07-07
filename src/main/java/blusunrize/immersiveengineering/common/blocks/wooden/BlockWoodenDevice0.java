/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.BlockIETileProvider;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase.ItemBlockIENoInventory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class BlockWoodenDevice0 extends BlockIETileProvider<BlockTypes_WoodenDevice0>
{
	boolean isExploding = false;

	public BlockWoodenDevice0()
	{
		super("wooden_device0", Material.WOOD, PropertyEnum.create("type", BlockTypes_WoodenDevice0.class), ItemBlockIENoInventory.class,
				IEProperties.FACING_ALL, IEProperties.SIDECONFIG[0], IEProperties.SIDECONFIG[1], IEProperties.MULTIBLOCKSLAVE, Properties.AnimationProperty);
		this.setHardness(2.0F);
		this.setResistance(5.0F);
		this.setMetaLightOpacity(BlockTypes_WoodenDevice0.WORKBENCH.getMeta(), 0);
		this.setNotNormalBlock(BlockTypes_WoodenDevice0.WORKBENCH.getMeta());
		this.setMetaMobilityFlag(BlockTypes_WoodenDevice0.WORKBENCH.getMeta(), PushReaction.BLOCK);
	}

	@Override
	protected Direction getDefaultFacing()
	{
		return Direction.UP;
	}

	@Override
	public boolean useCustomStateMapper()
	{
		return true;
	}

	@Override
	public String getCustomStateMapping(int meta, boolean itemBlock)
	{
		if(meta==BlockTypes_WoodenDevice0.WORKBENCH.getMeta())
			return "workbench";
		if(Config.seaonal_festive&&(meta==BlockTypes_WoodenDevice0.CRATE.getMeta()||meta==BlockTypes_WoodenDevice0.REINFORCED_CRATE.getMeta()||meta==BlockTypes_WoodenDevice0.GUNPOWDER_BARREL.getMeta()))
			return "festive";
		return null;
	}

	@Override
	public boolean canIEBlockBePlaced(BlockState newState, BlockItemUseContext context)
	{
		if(stack.getItemDamage()==BlockTypes_WoodenDevice0.WORKBENCH.getMeta())
		{
			Direction f = Direction.fromAngle(player.rotationYaw);
			if(f.getAxis()==Axis.Z)
			{
				return world.getBlockState(pos.add(1, 0, 0)).getBlock().isReplaceable(world, pos.add(1, 0, 0))||world.getBlockState(pos.add(-1, 0, 0)).getBlock().isReplaceable(world, pos.add(-1, 0, 0));
			}
			else
			{
				return world.getBlockState(pos.add(0, 0, 1)).getBlock().isReplaceable(world, pos.add(0, 0, 1))||world.getBlockState(pos.add(0, 0, -1)).getBlock().isReplaceable(world, pos.add(0, 0, -1));
			}
		}
		return true;
	}
	@Override
	public boolean canDropFromExplosion(Explosion explosionIn)
	{
		isExploding = true;
		return super.canDropFromExplosion(explosionIn);
	}

	@Override
	public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, BlockState state, float chance, int fortune)
	{
		if(!isExploding||this.getExplosivesType(state) < 0)
			super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);
		isExploding = false;
	}

	@Override
	public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion)
	{
		if(this.getMetaFromState(world.getBlockState(pos))==BlockTypes_WoodenDevice0.REINFORCED_CRATE.getMeta())
			return 1200000;
		return super.getExplosionResistance(world, pos, exploder, explosion);
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos)
//	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbour)
	{
//		super.onNeighborChange(world, pos, neighbour);
		super.neighborChanged(state, world, pos, block, fromPos);
		int explosivesType = this.getExplosivesType(world.getBlockState(pos));
		if(world instanceof World&&explosivesType >= 0&&world.isBlockPowered(pos))
			this.doExplosion(world, pos, world.getBlockState(pos), null, explosivesType);
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, BlockState state)
	{
		super.onBlockAdded(world, pos, state);
		int explosivesType = this.getExplosivesType(state);
		if(explosivesType >= 0&&world.isBlockPowered(pos))
			this.doExplosion(world, pos, state, null, explosivesType);
	}

	@Override
	public void onBlockExploded(World world, BlockPos pos, Explosion explosionIn)
	{
		int explosivesType = this.getExplosivesType(world.getBlockState(pos));
		if(explosivesType >= 0)
			this.doExplosion(world, pos, world.getBlockState(pos), null, explosivesType);
		super.onBlockExploded(world, pos, explosionIn);
	}

	@Override
	public void onEntityCollision(World world, BlockPos pos, BlockState state, Entity entity)
	{
		super.onEntityCollision(world, pos, state, entity);
		int explosivesType = this.getExplosivesType(state);
		if(!world.isRemote&&entity instanceof AbstractArrowEntity&&entity.isBurning()&&explosivesType >= 0)
			this.doExplosion(world, pos, state, ((AbstractArrowEntity)entity).shootingEntity instanceof LivingEntity?(LivingEntity)((AbstractArrowEntity)entity).shootingEntity: null, explosivesType);
	}


	@Override
	public TileEntity createBasicTE(World world, BlockTypes_WoodenDevice0 type)
	{
		switch(type)
		{
			case CRATE:
				return new TileEntityWoodenCrate();
			case WORKBENCH:
				return new TileEntityModWorkbench();
			case BARREL:
				return new TileEntityWoodenBarrel();
			case SORTER:
				return new TileEntitySorter();
			case REINFORCED_CRATE:
				return new TileEntityWoodenCrate();
			case TURNTABLE:
				return new TileEntityTurntable();
			case FLUID_SORTER:
				return new TileEntityFluidSorter();
		}
		return null;
	}

	@Override
	public boolean allowHammerHarvest(BlockState state)
	{
		return true;
	}
}