package blusunrize.immersiveengineering.common.blocks.metal;

import static net.minecraftforge.common.util.ForgeDirection.DOWN;
import static net.minecraftforge.common.util.ForgeDirection.UP;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.client.render.BlockRenderMetalDecoration;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWallmount;
import blusunrize.immersiveengineering.common.util.Utils;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Optional.Interface(iface = "blusunrize.aquatweaks.api.IAquaConnectable", modid = "AquaTweaks")
public class BlockMetalDecoration extends BlockIEBase implements blusunrize.aquatweaks.api.IAquaConnectable
{
	public static final int META_fence=0;
	public static final int META_scaffolding=1;
	public static final int META_lantern=2;
	public static final int META_structuralArm=3;
	public static final int META_radiator=4;
	public static final int META_heavyEngineering=5;
	public static final int META_generator=6;
	public static final int META_lightEngineering=7;
	public static final int META_connectorStructural=8;
	public static final int META_wallMount=9;
	public static final int META_sheetMetal=10;

	public BlockMetalDecoration()
	{
		super("metalDecoration", Material.iron,3, ItemBlockMetalDecorations.class, "fence","scaffolding","lantern","structuralArm",
				"radiator","heavyEngineering","generator","lightEngineering",
				"connectorStructural","wallMount","sheetMetal");
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
		if(meta==META_fence)
			return side==UP;
		if(meta==META_scaffolding)
			return true;
		if(world.getTileEntity(x,y,z) instanceof TileEntityStructuralArm)
		{
			if(side==UP)
				return ((TileEntityStructuralArm)world.getTileEntity(x,y,z)).inverted;
			else if(side==DOWN)
				return !((TileEntityStructuralArm)world.getTileEntity(x,y,z)).inverted;
			else
				return ((TileEntityStructuralArm)world.getTileEntity(x,y,z)).facing==side.getOpposite().ordinal();
		}
		if(meta==META_radiator||meta==META_heavyEngineering||meta==META_generator||meta==META_lightEngineering)
			return true;
		if(world.getTileEntity(x,y,z) instanceof TileEntityWallmount)
		{
			if(side==UP)
				return ((TileEntityWallmount)world.getTileEntity(x,y,z)).inverted;
			else if(side==DOWN)
				return !((TileEntityWallmount)world.getTileEntity(x,y,z)).inverted;
			else
				return true;
		}
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
		if(meta==META_fence)
			return true;
		return super.shouldSideBeRendered(world, x, y, z, side);
	}

	@Override
	@SideOnly(Side.CLIENT)
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
		icons[META_lantern][0] = iconRegister.registerIcon("immersiveengineering:lantern_0");
		//		icons[META_lantern][0] = iconRegister.registerIcon("immersiveengineering:metalDeco_lantern_bottom");
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
			icons[META_connectorStructural][i] = iconRegister.registerIcon("immersiveengineering:metalDeco_connectorStructural");
			icons[META_wallMount][i] = iconRegister.registerIcon("immersiveengineering:metalDeco_wallmount");
			icons[META_sheetMetal][i] = iconRegister.registerIcon("immersiveengineering:metalDeco_sheetMetal");
		}
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		if(world.getBlockMetadata(x, y, z)==META_fence)
			this.setBlockBounds(canConnectFenceTo(world,x-1,y,z)?0:.375f,0,canConnectFenceTo(world,x,y,z-1)?0:.375f, canConnectFenceTo(world,x+1,y,z)?1:.625f,1,canConnectFenceTo(world,x,y,z+1)?1:.625f);
		else if(world.getTileEntity(x, y, z) instanceof TileEntityLantern)
		{
			int f = ((TileEntityLantern)world.getTileEntity(x, y, z)).facing ;
			if(f<2)
				this.setBlockBounds(.25f,f==1?0:.125f,.25f, .75f,f==1?.875f:1f,.75f);
			else
				this.setBlockBounds(f==5?0:.25f,0,f==3?0:.25f, f==4?1:.75f,.875f,f==2?1:.75f);
		}
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
		else if(world.getTileEntity(x, y, z) instanceof TileEntityWallmount)
		{
			TileEntityWallmount arm = (TileEntityWallmount)world.getTileEntity(x, y, z);
			int f = arm.facing;
			if(arm.sideAttached>0)
				this.setBlockBounds(f==4?0:f==5?.375f:.3125f,arm.inverted?.3125f:0,f==2?0:f==3?.375f:.3125f, f==5?1:f==4?.625f:.6875f,arm.inverted?1:.6875f,f==3?1:f==2?.625f:.6875f);
			else
				this.setBlockBounds(f==5?0:.3125f,arm.inverted?.375f:0,f==3?0:.3125f, f==4?1:.6875f,arm.inverted?1:.625f,f==2?1:.6875f);
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
		if (world.getBlock(x, y, z) == this) {
			if (world.getBlockMetadata(x, y, z) == META_fence)
				this.setBlockBounds(canConnectFenceTo(world, x - 1, y, z) ? 0 : .375f, 0, canConnectFenceTo(world, x, y, z - 1) ? 0 : .375f, canConnectFenceTo(world, x + 1, y, z) ? 1 : .625f, 1.5f, canConnectFenceTo(world, x, y, z + 1) ? 1 : .625f);
			else if (world.getBlockMetadata(x, y, z) == META_scaffolding)
				this.setBlockBounds(.0625f, 0, .0625f, .9375f, 1, .9375f);
			else
				this.setBlockBoundsBasedOnState(world, x, y, z);
		} else {
			this.setBlockBounds(0, 0, 0, 1, 1, 1);
		}
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
		if(meta==META_lantern)
			return new TileEntityLantern();
		if(meta==META_structuralArm)
			return new TileEntityStructuralArm();
		if(meta==META_connectorStructural)
			return new TileEntityConnectorStructural();
		if(meta==META_wallMount)
			return new TileEntityWallmountMetal();
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
		if(world.getTileEntity(x,y,z) instanceof TileEntityConnectorStructural)
		{	
			if(Utils.isHammer(player.getCurrentEquippedItem()))
			{
				((TileEntityConnectorStructural)world.getTileEntity(x,y,z)).rotation += 22.5f;
				((TileEntityConnectorStructural)world.getTileEntity(x,y,z)).rotation %= 360;
				world.getTileEntity(x,y,z).markDirty();
				world.markBlockForUpdate(x, y, z);
				return true;
			}
		}
		return false;
	}

	@Optional.Method(modid = "AquaTweaks")
	public boolean shouldRenderFluid(IBlockAccess world, int x, int y, int z)
	{
		int meta = world.getBlockMetadata(x, y, z);
		return meta==META_fence
				|| meta==META_lantern
				|| meta==META_connectorStructural
				|| meta==META_wallMount;
	}

	@Optional.Method(modid = "AquaTweaks")
	public boolean canConnectTo(IBlockAccess world, int x, int y, int z, int side)
	{
		int meta = world.getBlockMetadata(x, y, z);
		return meta==META_fence
				|| meta==META_lantern
				|| meta==META_connectorStructural
				|| meta==META_wallMount;
	}
}