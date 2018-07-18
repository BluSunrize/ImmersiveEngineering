/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.world;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenCrate;
import blusunrize.immersiveengineering.common.util.IEVillagerHandler;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockDoor.EnumHingePosition;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight;
import net.minecraft.world.gen.structure.StructureVillagePieces.Start;
import net.minecraft.world.gen.structure.StructureVillagePieces.Village;
import net.minecraftforge.fml.common.registry.VillagerRegistry.IVillageCreationHandler;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VillageEngineersHouse extends Village
{
	public static ResourceLocation woodenCrateLoot = new ResourceLocation(ImmersiveEngineering.MODID, "chests/engineers_house");

	public VillageEngineersHouse()
	{
	}

	public VillageEngineersHouse(Start villagePiece, int par2, Random par3Random, StructureBoundingBox par4StructureBoundingBox, EnumFacing facing)
	{
		super(villagePiece, par2);
		this.setCoordBaseMode(facing);
		this.boundingBox = par4StructureBoundingBox;
	}

	static List<BlockPos> framesHung = new ArrayList();
	private int groundLevel = -1;

	@Override
	public boolean addComponentParts(World world, Random rand, StructureBoundingBox box)
	{
		if(groundLevel < 0)
		{
			groundLevel = this.getAverageGroundLevel(world, box);
			if(groundLevel < 0)
				return true;
			boundingBox.offset(0, groundLevel-boundingBox.maxY+10-1, 0);
		}

		//Clear Space
		this.fillWithBlocks(world, box, 0, 0, 0, 10, 9, 8, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
		//Cobble
		this.fillWithBlocks(world, box, 1, 0, 1, 9, 0, 8, Blocks.COBBLESTONE.getDefaultState(), Blocks.COBBLESTONE.getDefaultState(), false);
		this.fillWithBlocks(world, box, 6, 0, 1, 9, 0, 2, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
		//Stair
		this.setBlockState(world, Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH), 4, 0, 0, box);

		//Pillars
		this.fillWithBlocks(world, box, 1, 1, 3, 1, 4, 3, IEContent.blockTreatedWood.getDefaultState(), IEContent.blockTreatedWood.getDefaultState(), false);
		this.fillWithBlocks(world, box, 1, 1, 8, 1, 6, 8, IEContent.blockTreatedWood.getDefaultState(), IEContent.blockTreatedWood.getDefaultState(), false);
		this.fillWithBlocks(world, box, 9, 1, 3, 9, 6, 3, IEContent.blockTreatedWood.getDefaultState(), IEContent.blockTreatedWood.getDefaultState(), false);
		this.fillWithBlocks(world, box, 9, 1, 8, 9, 6, 8, IEContent.blockTreatedWood.getDefaultState(), IEContent.blockTreatedWood.getDefaultState(), false);
		this.fillWithBlocks(world, box, 1, 4, 3, 9, 4, 8, IEContent.blockTreatedWood.getDefaultState(), IEContent.blockTreatedWood.getDefaultState(), false);
		this.fillWithBlocks(world, box, 6, 5, 3, 6, 7, 3, IEContent.blockTreatedWood.getDefaultState(), IEContent.blockTreatedWood.getDefaultState(), false);
		this.fillWithBlocks(world, box, 1, 5, 5, 1, 6, 5, IEContent.blockTreatedWood.getDefaultState(), IEContent.blockTreatedWood.getDefaultState(), false);

		this.fillWithBlocks(world, box, 2, 4, 5, 8, 4, 7, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);

		//Wool
		this.fillWithBlocks(world, box, 2, 0, 3, 5, 0, 4, Blocks.WOOL.getStateFromMeta(13), Blocks.WOOL.getStateFromMeta(13), false);
		this.fillWithBlocks(world, box, 2, 0, 4, 8, 0, 7, Blocks.WOOL.getStateFromMeta(13), Blocks.WOOL.getStateFromMeta(13), false);
		this.fillWithBlocks(world, box, 6, 4, 4, 8, 4, 4, Blocks.WOOL.getStateFromMeta(13), Blocks.WOOL.getStateFromMeta(13), false);
		this.fillWithBlocks(world, box, 2, 4, 5, 7, 4, 5, Blocks.WOOL.getStateFromMeta(13), Blocks.WOOL.getStateFromMeta(13), false);
		this.fillWithBlocks(world, box, 2, 4, 6, 6, 4, 6, Blocks.WOOL.getStateFromMeta(13), Blocks.WOOL.getStateFromMeta(13), false);
		this.fillWithBlocks(world, box, 2, 4, 7, 4, 4, 7, Blocks.WOOL.getStateFromMeta(13), Blocks.WOOL.getStateFromMeta(13), false);

		//Walls
		//Front
		this.fillWithBlocks(world, box, 2, 1, 3, 8, 3, 3, Blocks.PLANKS.getDefaultState(), Blocks.PLANKS.getDefaultState(), false);
		this.fillWithBlocks(world, box, 7, 5, 3, 8, 6, 3, Blocks.PLANKS.getDefaultState(), Blocks.PLANKS.getDefaultState(), false);
		this.setBlockState(world, Blocks.PLANKS.getDefaultState(), 7, 7, 3, box);
		this.fillWithBlocks(world, box, 6, 5, 4, 6, 7, 4, Blocks.PLANKS.getDefaultState(), Blocks.PLANKS.getDefaultState(), false);
		this.fillWithBlocks(world, box, 2, 5, 5, 5, 6, 5, Blocks.PLANKS.getDefaultState(), Blocks.PLANKS.getDefaultState(), false);
		this.fillWithBlocks(world, box, 3, 7, 5, 5, 7, 5, Blocks.PLANKS.getDefaultState(), Blocks.PLANKS.getDefaultState(), false);
		this.setBlockState(world, Blocks.PLANKS.getDefaultState(), 5, 8, 5, box);
		//Back
		this.fillWithBlocks(world, box, 2, 1, 8, 8, 3, 8, Blocks.PLANKS.getDefaultState(), Blocks.PLANKS.getDefaultState(), false);
		this.fillWithBlocks(world, box, 2, 5, 8, 8, 6, 8, Blocks.PLANKS.getDefaultState(), Blocks.PLANKS.getDefaultState(), false);
		this.fillWithBlocks(world, box, 3, 7, 8, 7, 7, 8, Blocks.PLANKS.getDefaultState(), Blocks.PLANKS.getDefaultState(), false);
		this.setBlockState(world, Blocks.PLANKS.getDefaultState(), 5, 8, 8, box);
		//Left
		this.fillWithBlocks(world, box, 1, 1, 4, 1, 3, 7, Blocks.PLANKS.getDefaultState(), Blocks.PLANKS.getDefaultState(), false);
		this.fillWithBlocks(world, box, 1, 5, 6, 1, 5, 7, Blocks.PLANKS.getDefaultState(), Blocks.PLANKS.getDefaultState(), false);
		//Right
		this.fillWithBlocks(world, box, 9, 1, 4, 9, 3, 7, Blocks.PLANKS.getDefaultState(), Blocks.PLANKS.getDefaultState(), false);
		this.fillWithBlocks(world, box, 9, 5, 4, 9, 6, 7, Blocks.PLANKS.getDefaultState(), Blocks.PLANKS.getDefaultState(), false);

		//Windows
		//Front
		this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 2, 2, 3, box);
		this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 6, 2, 3, box);
		this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 8, 2, 3, box);
		this.fillWithBlocks(world, box, 7, 6, 3, 8, 6, 3, Blocks.GLASS_PANE.getDefaultState(), Blocks.GLASS_PANE.getDefaultState(), false);
		//Back
		this.fillWithBlocks(world, box, 3, 2, 8, 5, 2, 8, Blocks.GLASS_PANE.getDefaultState(), Blocks.GLASS_PANE.getDefaultState(), false);
		this.fillWithBlocks(world, box, 3, 6, 8, 4, 6, 8, Blocks.GLASS_PANE.getDefaultState(), Blocks.GLASS_PANE.getDefaultState(), false);
		this.fillWithBlocks(world, box, 6, 6, 8, 7, 6, 8, Blocks.GLASS_PANE.getDefaultState(), Blocks.GLASS_PANE.getDefaultState(), false);
		//Left
		this.fillWithBlocks(world, box, 1, 2, 5, 1, 2, 6, Blocks.GLASS_PANE.getDefaultState(), Blocks.GLASS_PANE.getDefaultState(), false);
		this.fillWithBlocks(world, box, 1, 6, 6, 1, 6, 7, Blocks.GLASS_PANE.getDefaultState(), Blocks.GLASS_PANE.getDefaultState(), false);
		//Right
		this.fillWithBlocks(world, box, 9, 2, 5, 9, 2, 6, Blocks.GLASS_PANE.getDefaultState(), Blocks.GLASS_PANE.getDefaultState(), false);
		this.fillWithBlocks(world, box, 9, 6, 5, 9, 6, 6, Blocks.GLASS_PANE.getDefaultState(), Blocks.GLASS_PANE.getDefaultState(), false);

		//Fences
		this.fillWithBlocks(world, box, 1, 1, 1, 1, 1, 2, IEContent.blockWoodenDecoration.getStateFromMeta(0), IEContent.blockWoodenDecoration.getStateFromMeta(0), false);
		this.fillWithBlocks(world, box, 2, 1, 1, 3, 1, 1, IEContent.blockWoodenDecoration.getStateFromMeta(0), IEContent.blockWoodenDecoration.getStateFromMeta(0), false);
		this.fillWithBlocks(world, box, 5, 1, 1, 5, 1, 2, IEContent.blockWoodenDecoration.getStateFromMeta(0), IEContent.blockWoodenDecoration.getStateFromMeta(0), false);
		this.fillWithBlocks(world, box, 1, 5, 3, 1, 5, 4, IEContent.blockWoodenDecoration.getStateFromMeta(0), IEContent.blockWoodenDecoration.getStateFromMeta(0), false);
		this.fillWithBlocks(world, box, 2, 5, 3, 5, 5, 3, IEContent.blockWoodenDecoration.getStateFromMeta(0), IEContent.blockWoodenDecoration.getStateFromMeta(0), false);
		this.fillWithBlocks(world, box, 7, 1, 6, 7, 5, 6, IEContent.blockWoodenDecoration.getStateFromMeta(0), IEContent.blockWoodenDecoration.getStateFromMeta(0), false);

		//Doors
		this.generateDoor(world, box, rand, 4, 1, 3, EnumFacing.NORTH, Blocks.OAK_DOOR);
		if(getCoordBaseMode()==EnumFacing.SOUTH||getCoordBaseMode()==EnumFacing.WEST)
		{
			this.placeDoor(world, box, rand, 3, 5, 5, EnumFacing.NORTH, EnumHingePosition.LEFT);
			this.placeDoor(world, box, rand, 4, 5, 5, EnumFacing.NORTH, EnumHingePosition.RIGHT);
		}
		else
		{
			this.placeDoor(world, box, rand, 3, 5, 5, EnumFacing.NORTH, EnumHingePosition.LEFT);
			this.placeDoor(world, box, rand, 4, 5, 5, EnumFacing.NORTH, EnumHingePosition.RIGHT);
		}

		//Lanterns
		this.placeLantern(world, box, 5, 3, 6, 0);
		this.placeLantern(world, box, 5, 7, 6, 0);

		//Stairs
		IBlockState stairs = Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH);
		setBlockState(world, stairs, 8, 1, 6, box);
		stairs = stairs.withRotation(Rotation.COUNTERCLOCKWISE_90);
//		stairMeta = this.getMetadataWithOffset(Blocks.OAK_STAIRS, 1);
		setBlockState(world, Blocks.PLANKS.getDefaultState(), 8, 1, 7, box);
		setBlockState(world, stairs, 7, 2, 7, box);
		setBlockState(world, stairs, 6, 3, 7, box);
		setBlockState(world, stairs, 5, 4, 7, box);

		//Roof
		IBlockState brickSlab = Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.BRICK);
		IBlockState brickSlabInverted = brickSlab.withProperty(BlockSlab.HALF, EnumBlockHalf.TOP);
		this.fillWithBlocks(world, box, 0, 6, 4, 0, 6, 8, brickSlabInverted, brickSlabInverted, false);
		this.fillWithBlocks(world, box, 1, 7, 4, 1, 7, 8, brickSlab, brickSlab, false);
		this.fillWithBlocks(world, box, 3, 8, 4, 3, 8, 8, brickSlab, brickSlab, false);
		this.fillWithBlocks(world, box, 5, 9, 2, 5, 9, 8, brickSlab, brickSlab, false);
		this.fillWithBlocks(world, box, 7, 8, 2, 7, 8, 8, brickSlab, brickSlab, false);
		this.fillWithBlocks(world, box, 9, 7, 2, 9, 7, 8, brickSlab, brickSlab, false);
		this.fillWithBlocks(world, box, 10, 6, 2, 10, 6, 8, brickSlabInverted, brickSlabInverted, false);

		IBlockState brickStairs = Blocks.BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST);
		this.fillWithBlocks(world, box, 2, 7, 4, 2, 7, 8, brickStairs, brickStairs, false);
		this.fillWithBlocks(world, box, 4, 8, 4, 4, 8, 8, brickStairs, brickStairs, false);
		brickStairs = brickStairs.withRotation(Rotation.CLOCKWISE_180);
		this.fillWithBlocks(world, box, 6, 8, 2, 6, 8, 8, brickStairs, brickStairs, false);
		this.fillWithBlocks(world, box, 8, 7, 2, 8, 7, 8, brickStairs, brickStairs, false);

		this.fillWithBlocks(world, box, 2, 7, 5, 2, 8, 5, Blocks.BRICK_BLOCK.getDefaultState(), Blocks.BRICK_BLOCK.getDefaultState(), false);
		this.fillWithBlocks(world, box, 7, 8, 4, 7, 9, 4, Blocks.BRICK_BLOCK.getDefaultState(), Blocks.BRICK_BLOCK.getDefaultState(), false);


		//Details
		try
		{
			this.placeCrate(world, box, rand, 6, 0, 1);
			this.placeCrate(world, box, rand, 8, 0, 2);
			this.placeCrate(world, box, rand, 5, 1, 7);
			this.placeItemframe(rand, world, 4, 3, 2, getCoordBaseMode().getOpposite(), new ItemStack(IEContent.itemTool, 1, 0));
		} catch(Exception e)
		{
			e.printStackTrace();
		}

		for(int zz = 0; zz <= 9; zz++)
			for(int xx = 0; xx <= 10; xx++)
			{
				this.clearCurrentPositionBlocksUpwards(world, xx, 10, zz, box);
				this.replaceAirAndLiquidDownwards(world, Blocks.COBBLESTONE.getDefaultState(), xx, -1, zz, box);
			}

		if(IEConfig.enableVillagers)
			this.spawnVillagers(world, box, 4, 1, 2, 1);
		return true;
	}

	protected boolean placeCrate(World world, StructureBoundingBox box, Random rand, int x, int y, int z)
	{
		int i1 = this.getXWithOffset(x, z);
		int j1 = this.getYWithOffset(y);
		int k1 = this.getZWithOffset(x, z);
		BlockPos pos = new BlockPos(i1, j1, k1);
		if(box.isVecInside(pos)&&(world.getBlockState(pos)!=IEContent.blockWoodenDevice0.getStateFromMeta(0)))
		{
			world.setBlockState(pos, IEContent.blockWoodenDevice0.getStateFromMeta(0), 2);
			TileEntity tile = world.getTileEntity(pos);
			if(tile instanceof TileEntityWoodenCrate)
				((TileEntityWoodenCrate)tile).lootTable = woodenCrateLoot;
			return true;
		}
		else
			return false;
	}

	protected void placeDoor(World worldIn, StructureBoundingBox boundingBoxIn, Random rand, int x, int y, int z, EnumFacing facing, EnumHingePosition hinge)
	{
		this.setBlockState(worldIn, Blocks.OAK_DOOR.getDefaultState().withProperty(BlockDoor.FACING, facing).withProperty(BlockDoor.HINGE, hinge), x, y, z, boundingBoxIn);
		this.setBlockState(worldIn, Blocks.OAK_DOOR.getDefaultState().withProperty(BlockDoor.FACING, facing).withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER).withProperty(BlockDoor.HINGE, hinge), x, y+1, z, boundingBoxIn);
	}

	protected boolean placeLantern(World world, StructureBoundingBox box, int x, int y, int z, int facing)
	{
		int i1 = this.getXWithOffset(x, z);
		int j1 = this.getYWithOffset(y);
		int k1 = this.getZWithOffset(x, z);
		BlockPos pos = new BlockPos(i1, j1, k1);
		//			world.setBlock(pos, IEContent.blockMetalDecoration0,BlockMetalDecoration1.META_lantern, 2);
//			TileEntity tile = world.getTileEntity(pos);
//			if(tile instanceof TileEntityLantern)
//				((TileEntityLantern)tile).facing = facing;
		return box.isVecInside(pos);
	}

	public void placeItemframe(Random random, World world, int x, int y, int z, EnumFacing side, ItemStack stack)
	{
		int i1 = this.getXWithOffset(x, z);
		int j1 = this.getYWithOffset(y);
		int k1 = this.getZWithOffset(x, z);

		EntityItemFrame e = new EntityItemFrame(world, new BlockPos(i1, j1, k1), side);
		e.setDisplayedItem(stack);
		if(e.onValidSurface()&&world.getEntitiesWithinAABB(EntityHanging.class, new AxisAlignedBB(i1-.125, j1, k1-.125, i1+1.125, j1+1, k1+1.125)).isEmpty())
			if(!world.isRemote)
				world.spawnEntity(e);
	}

	@Override
	protected VillagerProfession chooseForgeProfession(int count, net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession prof)
	{
		return IEVillagerHandler.PROF_ENGINEER;
	}
//	protected int func_180779_c(int i, int previousProfession)
//	{
//		//Changed to return Smith while Engineers don't exist
//		return 3;
//		//return Config.getInt("villager_engineer");
//	}

	public static class VillageManager implements IVillageCreationHandler
	{
		@Override
		public Village buildComponent(PieceWeight villagePiece, Start startPiece, List<StructureComponent> pieces, Random random, int p1, int p2, int p3, EnumFacing facing, int p5)
		{
			StructureBoundingBox box = StructureBoundingBox.getComponentToAddBoundingBox(p1, p2, p3, 0, 0, 0, 11, 10, 9, facing);
			return (!canVillageGoDeeper(box))||(StructureComponent.findIntersecting(pieces, box)!=null)?null: new VillageEngineersHouse(startPiece, p5, random, box, facing);
		}

		@Override
		public PieceWeight getVillagePieceWeight(Random random, int i)
		{
			return new PieceWeight(VillageEngineersHouse.class, 15, MathHelper.getInt(random, 0+i, 1+i));
		}

		@Override
		public Class<?> getComponentClass()
		{
			return VillageEngineersHouse.class;
		}
	}
}
