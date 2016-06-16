package blusunrize.immersiveengineering.common.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenCrate;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight;
import net.minecraft.world.gen.structure.StructureVillagePieces.Start;
import net.minecraft.world.gen.structure.StructureVillagePieces.Village;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.fml.common.registry.VillagerRegistry.IVillageCreationHandler;

public class VillageEngineersHouse extends StructureVillagePieces.Village
{
	static ChestGenHooks crateContents = new ChestGenHooks("IE:CRATES", 
			Lists.newArrayList(
					new WeightedRandomChestContent(IEContent.itemMaterial,0, 2,7, 20),
					new WeightedRandomChestContent(IEContent.itemMaterial,11, 1,2, 8),
					new WeightedRandomChestContent(IEContent.itemMaterial,12, 1,1, 5),
					new WeightedRandomChestContent(IEContent.itemMetal,0, 2,5, 16),
					new WeightedRandomChestContent(IEContent.itemMetal,1, 1,4, 10),
					new WeightedRandomChestContent(IEContent.itemMetal,2, 1,4, 10),
					new WeightedRandomChestContent(IEContent.itemMetal,7, 1,4, 8),
					new WeightedRandomChestContent(IEContent.itemBlueprint,BlueprintCraftingRecipe.blueprintCategories.indexOf("bullet"), 1,1, 5),
					new WeightedRandomChestContent(IEContent.itemBlueprint,BlueprintCraftingRecipe.blueprintCategories.indexOf("specialBullet"), 1,1, 2),
					new WeightedRandomChestContent(IEContent.itemBlueprint,BlueprintCraftingRecipe.blueprintCategories.indexOf("electrode"), 1,1, 1),
					new WeightedRandomChestContent(IEContent.itemShader,0, 1,1, 5)
					), 3,9);

	public VillageEngineersHouse()
	{
	}
	public VillageEngineersHouse(Start villagePiece, int par2, Random par3Random, StructureBoundingBox par4StructureBoundingBox, EnumFacing facing)
	{
		super(villagePiece, par2);
		this.coordBaseMode = facing;
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
			if(groundLevel<0)
				return true;
			boundingBox.offset(0, groundLevel - boundingBox.maxY+10-1, 0);
		}

		//Clear Space
		this.fillWithBlocks(world, box, 0,0,0, 10,9,8, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
		//Cobble
		this.fillWithBlocks(world, box, 1,0,1, 9,0,8, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
		this.fillWithBlocks(world, box, 6,0,1, 9,0,2, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
		//Stair
		int stairMeta = coordBaseMode==EnumFacing.NORTH?2: coordBaseMode==EnumFacing.SOUTH?1: coordBaseMode==EnumFacing.WEST?3: 0;
		this.setBlockState(world, Blocks.stone_stairs.getStateFromMeta(stairMeta), 4,0,0, box);

		//Pillars
		this.fillWithBlocks(world, box, 1,1,3, 1,4,3, IEContent.blockTreatedWood.getDefaultState(), IEContent.blockTreatedWood.getDefaultState(), false);
		this.fillWithBlocks(world, box, 1,1,8, 1,6,8, IEContent.blockTreatedWood.getDefaultState(), IEContent.blockTreatedWood.getDefaultState(), false);
		this.fillWithBlocks(world, box, 9,1,3, 9,6,3, IEContent.blockTreatedWood.getDefaultState(), IEContent.blockTreatedWood.getDefaultState(), false);
		this.fillWithBlocks(world, box, 9,1,8, 9,6,8, IEContent.blockTreatedWood.getDefaultState(), IEContent.blockTreatedWood.getDefaultState(), false);
		this.fillWithBlocks(world, box, 1,4,3, 9,4,8, IEContent.blockTreatedWood.getDefaultState(), IEContent.blockTreatedWood.getDefaultState(), false);
		this.fillWithBlocks(world, box, 6,5,3, 6,7,3, IEContent.blockTreatedWood.getDefaultState(), IEContent.blockTreatedWood.getDefaultState(), false);
		this.fillWithBlocks(world, box, 1,5,5, 1,6,5, IEContent.blockTreatedWood.getDefaultState(), IEContent.blockTreatedWood.getDefaultState(), false);

		this.fillWithBlocks(world, box, 2,4,5, 8,4,7, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);

		//Wool
		this.fillWithBlocks(world, box, 2,0,3, 5,0,4, Blocks.wool.getStateFromMeta(13), Blocks.wool.getStateFromMeta(13), false);
		this.fillWithBlocks(world, box, 2,0,4, 8,0,7, Blocks.wool.getStateFromMeta(13), Blocks.wool.getStateFromMeta(13), false);
		this.fillWithBlocks(world, box, 6,4,4, 8,4,4, Blocks.wool.getStateFromMeta(13), Blocks.wool.getStateFromMeta(13), false);
		this.fillWithBlocks(world, box, 2,4,5, 7,4,5, Blocks.wool.getStateFromMeta(13), Blocks.wool.getStateFromMeta(13), false);
		this.fillWithBlocks(world, box, 2,4,6, 6,4,6, Blocks.wool.getStateFromMeta(13), Blocks.wool.getStateFromMeta(13), false);
		this.fillWithBlocks(world, box, 2,4,7, 4,4,7, Blocks.wool.getStateFromMeta(13), Blocks.wool.getStateFromMeta(13), false);

		//Walls
		//Front
		this.fillWithBlocks(world, box, 2,1,3, 8,3,3, Blocks.planks.getDefaultState(),Blocks.planks.getDefaultState(), false);
		this.fillWithBlocks(world, box, 7,5,3, 8,6,3, Blocks.planks.getDefaultState(),Blocks.planks.getDefaultState(), false);
		this.setBlockState(world, Blocks.planks.getDefaultState(), 7,7,3, box);
		this.fillWithBlocks(world, box, 6,5,4, 6,7,4, Blocks.planks.getDefaultState(),Blocks.planks.getDefaultState(), false);
		this.fillWithBlocks(world, box, 2,5,5, 5,6,5, Blocks.planks.getDefaultState(),Blocks.planks.getDefaultState(), false);
		this.fillWithBlocks(world, box, 3,7,5, 5,7,5, Blocks.planks.getDefaultState(),Blocks.planks.getDefaultState(), false);
		this.setBlockState(world, Blocks.planks.getDefaultState(), 5,8,5, box);
		//Back
		this.fillWithBlocks(world, box, 2,1,8, 8,3,8, Blocks.planks.getDefaultState(),Blocks.planks.getDefaultState(), false);
		this.fillWithBlocks(world, box, 2,5,8, 8,6,8, Blocks.planks.getDefaultState(),Blocks.planks.getDefaultState(), false);
		this.fillWithBlocks(world, box, 3,7,8, 7,7,8, Blocks.planks.getDefaultState(),Blocks.planks.getDefaultState(), false);
		this.setBlockState(world, Blocks.planks.getDefaultState(), 5,8,8, box);
		//Left
		this.fillWithBlocks(world, box, 1,1,4, 1,3,7, Blocks.planks.getDefaultState(),Blocks.planks.getDefaultState(), false);
		this.fillWithBlocks(world, box, 1,5,6, 1,5,7, Blocks.planks.getDefaultState(),Blocks.planks.getDefaultState(), false);
		//Right
		this.fillWithBlocks(world, box, 9,1,4, 9,3,7, Blocks.planks.getDefaultState(),Blocks.planks.getDefaultState(), false);
		this.fillWithBlocks(world, box, 9,5,4, 9,6,7, Blocks.planks.getDefaultState(),Blocks.planks.getDefaultState(), false);

		//Windows
		//Front
		this.setBlockState(world, Blocks.glass_pane.getDefaultState(), 2,2,3, box);
		this.setBlockState(world, Blocks.glass_pane.getDefaultState(), 6,2,3, box);
		this.setBlockState(world, Blocks.glass_pane.getDefaultState(), 8,2,3, box);
		this.fillWithBlocks(world, box, 7,6,3, 8,6,3, Blocks.glass_pane.getDefaultState(),Blocks.glass_pane.getDefaultState(), false);
		//Back
		this.fillWithBlocks(world, box, 3,2,8, 5,2,8, Blocks.glass_pane.getDefaultState(),Blocks.glass_pane.getDefaultState(), false);
		this.fillWithBlocks(world, box, 3,6,8, 4,6,8, Blocks.glass_pane.getDefaultState(),Blocks.glass_pane.getDefaultState(), false);
		this.fillWithBlocks(world, box, 6,6,8, 7,6,8, Blocks.glass_pane.getDefaultState(),Blocks.glass_pane.getDefaultState(), false);
		//Left
		this.fillWithBlocks(world, box, 1,2,5, 1,2,6, Blocks.glass_pane.getDefaultState(),Blocks.glass_pane.getDefaultState(), false);
		this.fillWithBlocks(world, box, 1,6,6, 1,6,7, Blocks.glass_pane.getDefaultState(),Blocks.glass_pane.getDefaultState(), false);
		//Right
		this.fillWithBlocks(world, box, 9,2,5, 9,2,6, Blocks.glass_pane.getDefaultState(),Blocks.glass_pane.getDefaultState(), false);
		this.fillWithBlocks(world, box, 9,6,5, 9,6,6, Blocks.glass_pane.getDefaultState(),Blocks.glass_pane.getDefaultState(), false);

		//Fences
		this.fillWithBlocks(world, box, 1,1,1, 1,1,2, IEContent.blockWoodenDecoration.getStateFromMeta(0),IEContent.blockWoodenDecoration.getStateFromMeta(0), false);
		this.fillWithBlocks(world, box, 2,1,1, 3,1,1, IEContent.blockWoodenDecoration.getStateFromMeta(0),IEContent.blockWoodenDecoration.getStateFromMeta(0), false);
		this.fillWithBlocks(world, box, 5,1,1, 5,1,2, IEContent.blockWoodenDecoration.getStateFromMeta(0),IEContent.blockWoodenDecoration.getStateFromMeta(0), false);
		this.fillWithBlocks(world, box, 1,5,3, 1,5,4, IEContent.blockWoodenDecoration.getStateFromMeta(0),IEContent.blockWoodenDecoration.getStateFromMeta(0), false);
		this.fillWithBlocks(world, box, 2,5,3, 5,5,3, IEContent.blockWoodenDecoration.getStateFromMeta(0),IEContent.blockWoodenDecoration.getStateFromMeta(0), false);
		this.fillWithBlocks(world, box, 7,1,6, 7,5,6, IEContent.blockWoodenDecoration.getStateFromMeta(0),IEContent.blockWoodenDecoration.getStateFromMeta(0), false);

		//Doors
		EnumFacing doorMeta = EnumFacing.getHorizontal(this.getMetadataWithOffset(Blocks.oak_door, 1));
		this.placeDoorCurrentPosition(world, box, rand, 4,1,3, doorMeta);
		if(coordBaseMode==EnumFacing.NORTH || coordBaseMode==EnumFacing.SOUTH)
		{
			this.placeDoorCurrentPosition(world, box, rand, 4, 5, 5, doorMeta);
			this.placeDoorCurrentPosition(world, box, rand, 3, 5, 5, doorMeta);
		}
		else
		{
			this.placeDoorCurrentPosition(world, box, rand, 3,5,5, doorMeta);
			this.placeDoorCurrentPosition(world, box, rand, 4,5,5, doorMeta);
		}

		//Lanterns
		this.placeLantern(world, box, 5,3,6, 0);
		this.placeLantern(world, box, 5,7,6, 0);

		//Stairs
		setBlockState(world, Blocks.oak_stairs.getStateFromMeta(stairMeta), 8,1,6, box);
		stairMeta = this.getMetadataWithOffset(Blocks.oak_stairs, 1);
		setBlockState(world, Blocks.planks.getDefaultState(), 8,1,7, box);
		setBlockState(world, Blocks.oak_stairs.getStateFromMeta(stairMeta), 7,2,7, box);
		setBlockState(world, Blocks.oak_stairs.getStateFromMeta(stairMeta), 6,3,7, box);
		setBlockState(world, Blocks.oak_stairs.getStateFromMeta(stairMeta), 5,4,7, box);

		//Roof
		this.fillWithBlocks(world, box, 0,6,4, 0,6,8, Blocks.stone_slab.getStateFromMeta(12),Blocks.stone_slab.getStateFromMeta(12), false);
		this.fillWithBlocks(world, box, 1,7,4, 1,7,8, Blocks.stone_slab.getStateFromMeta(4),Blocks.stone_slab.getStateFromMeta(4), false);
		this.fillWithBlocks(world, box, 3,8,4, 3,8,8, Blocks.stone_slab.getStateFromMeta(4),Blocks.stone_slab.getStateFromMeta(4), false);
		this.fillWithBlocks(world, box, 5,9,2, 5,9,8, Blocks.stone_slab.getStateFromMeta(4),Blocks.stone_slab.getStateFromMeta(4), false);
		this.fillWithBlocks(world, box, 7,8,2, 7,8,8, Blocks.stone_slab.getStateFromMeta(4),Blocks.stone_slab.getStateFromMeta(4), false);
		this.fillWithBlocks(world, box, 9,7,2, 9,7,8, Blocks.stone_slab.getStateFromMeta(4),Blocks.stone_slab.getStateFromMeta(4), false);
		this.fillWithBlocks(world, box, 10,6,2, 10,6,8, Blocks.stone_slab.getStateFromMeta(12),Blocks.stone_slab.getStateFromMeta(12), false);

		stairMeta = this.getMetadataWithOffset(Blocks.oak_stairs, 0);
		this.fillWithBlocks(world, box, 2,7,4, 2,7,8, Blocks.brick_stairs.getStateFromMeta(stairMeta),Blocks.brick_stairs.getStateFromMeta(stairMeta), false);
		this.fillWithBlocks(world, box, 4,8,4, 4,8,8, Blocks.brick_stairs.getStateFromMeta(stairMeta),Blocks.brick_stairs.getStateFromMeta(stairMeta), false);
		stairMeta = this.getMetadataWithOffset(Blocks.oak_stairs, 1);
		this.fillWithBlocks(world, box, 6,8,2, 6,8,8, Blocks.brick_stairs.getStateFromMeta(stairMeta),Blocks.brick_stairs.getStateFromMeta(stairMeta), false);
		this.fillWithBlocks(world, box, 8,7,2, 8,7,8, Blocks.brick_stairs.getStateFromMeta(stairMeta),Blocks.brick_stairs.getStateFromMeta(stairMeta), false);

		this.fillWithBlocks(world, box, 2,7,5, 2,8,5, Blocks.brick_block.getDefaultState(),Blocks.brick_block.getDefaultState(), false);
		this.fillWithBlocks(world, box, 7,8,4, 7,9,4, Blocks.brick_block.getDefaultState(),Blocks.brick_block.getDefaultState(), false);


		//Details
		try{
			this.placeCrate(world,box,rand, 6,0,1, crateContents.getItems(rand), 2+rand.nextInt(4));
			this.placeCrate(world,box,rand, 8,0,2, crateContents.getItems(rand), 2+rand.nextInt(4));
			this.placeCrate(world,box,rand, 5,1,7, crateContents.getItems(rand), 2+rand.nextInt(4));
			this.placeItemframe(rand, world, 4,3,3, coordBaseMode, new ItemStack(IEContent.itemTool,1,0));
		}catch(Exception e)
		{
			e.printStackTrace();
		}

		//There are no vilalgers to spawn! :V
		//this.spawnVillagers(world, box, 4, 1, 2, 1);
		return true;
	}

	protected boolean placeCrate(World world, StructureBoundingBox box, Random rand, int x, int y, int z, List<WeightedRandomChestContent> list, int amount)
	{
		int i1 = this.getXWithOffset(x, z);
		int j1 = this.getYWithOffset(y);
		int k1 = this.getZWithOffset(x, z);
		BlockPos pos = new BlockPos(i1,j1,k1);
		if(box.isVecInside(pos) && (world.getBlockState(pos)!=IEContent.blockWoodenDevice0.getStateFromMeta(0)))
		{
			world.setBlockState(pos, IEContent.blockWoodenDevice0.getStateFromMeta(0), 2);
			TileEntity tile = world.getTileEntity(pos);
			if(tile instanceof TileEntityWoodenCrate)
				Utils.generateChestContents(rand, list, ((TileEntityWoodenCrate)tile), amount);
			return true;
		}
		else
			return false;
	}

	protected boolean placeLantern(World world, StructureBoundingBox box, int x, int y, int z, int facing)
	{
		int i1 = this.getXWithOffset(x, z);
		int j1 = this.getYWithOffset(y);
		int k1 = this.getZWithOffset(x, z);
		BlockPos pos = new BlockPos(i1,j1,k1);
		if(box.isVecInside(pos))
		{
			//			world.setBlock(pos, IEContent.blockMetalDecoration0,BlockMetalDecoration1.META_lantern, 2);
			//			TileEntity tile = world.getTileEntity(pos);
			//			if(tile instanceof TileEntityLantern)
			//				((TileEntityLantern)tile).facing = facing;
			return true;
		}
		else
			return false;
	}
	public void placeItemframe(Random random, World world, int x, int y, int z, EnumFacing side, ItemStack stack)
	{
		int i1 = this.getXWithOffset(x, z);
		int j1 = this.getYWithOffset(y);
		int k1 = this.getZWithOffset(x, z);

		EntityItemFrame e = new EntityItemFrame(world, new BlockPos(i1,j1,k1), side);
		e.setDisplayedItem(stack);
		if(e.onValidSurface() && world.getEntitiesWithinAABB(EntityHanging.class, AxisAlignedBB.fromBounds(i1-.125,j1,k1-.125,i1+1.125,j1+1,k1+1.125)).isEmpty())
			if (!world.isRemote)
				world.spawnEntityInWorld(e);
	}

	@Override
	protected int func_180779_c(int i, int previousProfession)
	{
		//Changed to return Smith while Engineers don't exist
		return 3;
		//return Config.getInt("villager_engineer");
	}

	public static class VillageManager implements IVillageCreationHandler
	{
		@Override
		public Village buildComponent(PieceWeight villagePiece, Start startPiece, List<StructureComponent> pieces, Random random, int p1, int p2, int p3, EnumFacing facing, int p5)
		{
			StructureBoundingBox box = StructureBoundingBox.getComponentToAddBoundingBox(p1, p2, p3, 0, 0, 0, 11, 10, 9, facing);
			return (!canVillageGoDeeper(box)) || (StructureComponent.findIntersecting(pieces, box) != null) ? null : new VillageEngineersHouse(startPiece, p5, random, box, facing);
		}
		@Override
		public PieceWeight getVillagePieceWeight(Random random, int i)
		{
			return new StructureVillagePieces.PieceWeight(VillageEngineersHouse.class, 15, MathHelper.getRandomIntegerInRange(random, 0 + i, 1 + i));
		}
		@Override
		public Class<?> getComponentClass()
		{
			return VillageEngineersHouse.class;
		}
	}
}
