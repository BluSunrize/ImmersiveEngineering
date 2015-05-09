package blusunrize.immersiveengineering.common.blocks.metal;

import static net.minecraftforge.common.util.ForgeDirection.DOWN;
import static net.minecraftforge.common.util.ForgeDirection.UP;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.client.render.BlockRenderMetalDecoration;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.util.Utils;

public class BlockMetalDecoration extends BlockIEBase
{
	public static int META_fence=0;
	public static int META_scaffolding=1;
	public static int META_lantern=2;
	public static int META_structuralArm=3;
	public static int META_radiator=4;
	public static int META_heavyEngineering=5;
	public static int META_generator=6;
	public static int META_lightEngineering=7;
	public static int META_connectorStrutural=8;

	public BlockMetalDecoration()
	{
		super("metalDecoration", Material.iron,3, ItemBlockMetalDecorations.class, "fence","scaffolding","lantern","structuralArm",
				"radiator","heavyEngineering","generator","lightEngineering",
				"connectorStructural");
		setHardness(3.0F);
		setResistance(15.0F);
	}

	@Override
	public int getRenderType()
	{
		return BlockRenderMetalDecoration.renderID;
	}

	@Override
	public int damageDropped(int meta)
	{
		return super.damageDropped(meta);
	}
	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
	{
		ArrayList<ItemStack> ret = super.getDrops(world, x, y, z, metadata, fortune);
		return ret;
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z)
	{
		if(world.getBlockMetadata(x, y, z)==META_lantern)
			return 15;
		return 0;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity ent)
	{
		if(world.getBlockMetadata(x, y, z)==META_scaffolding)
		{
			float f5 = 0.15F;
			if (ent.motionX < (double)(-f5))
				ent.motionX = (double)(-f5);
			if (ent.motionX > (double)f5)
				ent.motionX = (double)f5;
			if (ent.motionZ < (double)(-f5))
				ent.motionZ = (double)(-f5);
			if (ent.motionZ > (double)f5)
				ent.motionZ = (double)f5;

			ent.fallDistance = 0.0F;
			if (ent.motionY < -0.15D)
				ent.motionY = -0.15D;

			if(ent.motionY<0 && ent instanceof EntityPlayer && ent.isSneaking())
			{
				ent.motionY=.05;
				return;
			}
			if(ent.isCollidedHorizontally)
				ent.motionY=.2;
		}
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side)
	{
		int meta = world.getBlockMetadata(x, y, z);
		if(meta==META_scaffolding)
			return side==UP || side==DOWN;

		return super.isSideSolid(world, x, y, z, side);
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y ,int z, int side)
	{
		//		int meta = world.getBlockMetadata(x, y, z);
		//		if(meta==1||meta==2||meta==3)
		//			return true;
		int meta = world.getBlockMetadata(x+(side==4?1:side==5?-1:0),y+(side==0?1:side==1?-1:0),z+(side==2?1:side==3?-1:0));
		if(meta==META_scaffolding)
			return (world.getBlock(x, y, z)==this&&world.getBlockMetadata(x,y,z)==1)?false:true;

		return super.shouldSideBeRendered(world, x, y, z, side);
	}

	@Override
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		//Fence
		icons[META_fence][0] = iconRegister.registerIcon("immersiveengineering:storage_Steel");
		icons[META_fence][1] = iconRegister.registerIcon("immersiveengineering:storage_Steel");
		icons[META_fence][2] = iconRegister.registerIcon("immersiveengineering:storage_Steel");
		//Scaffolding
		icons[META_scaffolding][0] = iconRegister.registerIcon("immersiveengineering:metalDeco_scaffolding_top");
		icons[META_scaffolding][1] = iconRegister.registerIcon("immersiveengineering:metalDeco_scaffolding_top");
		icons[META_scaffolding][2] = iconRegister.registerIcon("immersiveengineering:metalDeco_scaffolding_side");
		//Lantern
		icons[META_lantern][0] = iconRegister.registerIcon("immersiveengineering:metalDeco_lantern_bottom");
		icons[META_lantern][1] = iconRegister.registerIcon("immersiveengineering:metalDeco_lantern_top");
		icons[META_lantern][2] = iconRegister.registerIcon("immersiveengineering:metalDeco_lantern_side");
		//Arm
		icons[META_structuralArm][0] = iconRegister.registerIcon("immersiveengineering:metalDeco_scaffolding_top");
		icons[META_structuralArm][1] = iconRegister.registerIcon("immersiveengineering:metalDeco_scaffolding_top");
		icons[META_structuralArm][2] = iconRegister.registerIcon("immersiveengineering:metalDeco_scaffolding_side");
		for(int i=0;i<3;i++)
		{
			icons[META_radiator][i] = iconRegister.registerIcon("immersiveengineering:metalDeco_radiator");
			icons[META_heavyEngineering][i] = iconRegister.registerIcon("immersiveengineering:metalDeco_engine");
			icons[META_generator][i] = iconRegister.registerIcon("immersiveengineering:metalDeco_generator");
			icons[META_lightEngineering][i] = iconRegister.registerIcon("immersiveengineering:metalDeco_electricMachine");
			icons[META_connectorStrutural][i] = iconRegister.registerIcon("immersiveengineering:storage_Steel");
		}
	}
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityStructuralArm)
		{
			int playerViewQuarter = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
			int f = playerViewQuarter==0 ? 2:playerViewQuarter==1 ? 5:playerViewQuarter==2 ? 3: 4;
			((TileEntityStructuralArm)world.getTileEntity(x, y, z)).facing = f;
		}
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		if(world.getBlockMetadata(x, y, z)==META_fence)
			this.setBlockBounds(canConnectFenceTo(world,x-1,y,z)?0:.375f,0,canConnectFenceTo(world,x,y,z-1)?0:.375f, canConnectFenceTo(world,x+1,y,z)?1:.625f,1,canConnectFenceTo(world,x,y,z+1)?1:.625f);
		else if(world.getBlockMetadata(x, y, z)==META_lantern)
			this.setBlockBounds(.25f,0,.25f, .75f,.8125f,.75f);
		else if(world.getTileEntity(x, y, z) instanceof TileEntityConnectorStructural)
		{
			float length = .5f;
			switch(((TileEntityConnectorStructural)world.getTileEntity(x, y, z)).facing )
			{
			case 0://UP
				this.setBlockBounds(.25f,0,.25f,  .75f,length,.75f);
				break;
			case 1://DOWN
				this.setBlockBounds(.25f,1-length,.25f,  .75f,1,.75f);
				break;
			case 2://SOUTH
				this.setBlockBounds(.25f,.25f,0,  .75f,.75f,length);
				break;
			case 3://NORTH
				this.setBlockBounds(.25f,.25f,1-length,  .75f,.75f,1);
				break;
			case 4://EAST
				this.setBlockBounds(0,.25f,.25f,  length,.75f,.75f);
				break;
			case 5://WEST
				this.setBlockBounds(1-length,.25f,.25f,  1,.75f,.75f);
				break;
			}
		}
		else
			this.setBlockBounds(0,0,0,1,1,1);
	}
	public boolean canConnectFenceTo(IBlockAccess world, int x, int y, int z)
	{
		Block block = world.getBlock(x, y, z);
		return block != this && block != Blocks.fence_gate ? (block.getMaterial().isOpaque() && block.renderAsNormalBlock() ? block.getMaterial() != Material.gourd : false) : true;
	}

	@Override
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB aabb, List list, Entity ent)
	{
		super.addCollisionBoxesToList(world, x, y, z, aabb, list, ent);
	}
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
	{
		if(world.getBlockMetadata(x, y, z)==META_fence)
			this.setBlockBounds(canConnectFenceTo(world,x-1,y,z)?0:.375f,0,canConnectFenceTo(world,x,y,z-1)?0:.375f, canConnectFenceTo(world,x+1,y,z)?1:.625f,1.5f,canConnectFenceTo(world,x,y,z+1)?1:.625f);
		else if(world.getBlockMetadata(x, y, z)==META_scaffolding)
			this.setBlockBounds(.0625f,0,.0625f, .9375f,1,.9375f);
		else
			this.setBlockBoundsBasedOnState(world,x,y,z);
		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}
	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
	{
		this.setBlockBoundsBasedOnState(world,x,y,z);
		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		if(meta==META_structuralArm)
			return new TileEntityStructuralArm();
		if(meta==META_connectorStrutural)
			return new TileEntityConnectorStructural();
		return null;
	}
	@Override
	public boolean allowHammerHarvest(int metadata)
	{
		return true;
	}


	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		if(Utils.isHammer(player.getCurrentEquippedItem()) && (world.getTileEntity(x,y,z) instanceof TileEntityConnectorStructural) )
		{	
			((TileEntityConnectorStructural)world.getTileEntity(x,y,z)).rotation += 22.5f;
			((TileEntityConnectorStructural)world.getTileEntity(x,y,z)).rotation %= 360;
			return true;
		}
		return false;
	}
}