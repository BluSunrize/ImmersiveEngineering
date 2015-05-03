package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.ClientEventHandler;
import blusunrize.immersiveengineering.client.render.BlockRenderMetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.Utils;

public class BlockMetalMultiblocks extends BlockIEBase
{
	public static int META_lightningRod=0;
	public static int META_dieselGenerator=1;
	public static int META_squeezer=2;
	public static int META_fermenter=3;
	public static int META_refinery=4;
	public static int META_crusher=5;
	public BlockMetalMultiblocks()
	{
		super("metalMultiblock", Material.iron, 4, ItemBlockIEBase.class,
				"lightningRod","dieselGenerator",
				"industrialSqueezer","fermenter","refinery",
				"crusher");
		setHardness(3.0F);
		setResistance(15.0F);
	}


	@Override
	public boolean allowHammerHarvest(int meta)
	{
		return true;
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List list)
	{
		for(int i=0; i<subNames.length; i++)
			if(i!=META_dieselGenerator && i!=META_refinery && i!=META_crusher)
				list.add(new ItemStack(item, 1, i));
	}

	@Override
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		//0 lightningRod
		icons[0][0] = iconRegister.registerIcon("immersiveengineering:metal_lightningrod_top");
		icons[0][1] = iconRegister.registerIcon("immersiveengineering:metal_lightningrod_top");
		icons[0][2] = iconRegister.registerIcon("immersiveengineering:metal_lightningrod_side");
		icons[0][3] = iconRegister.registerIcon("immersiveengineering:metal_lightningrod_side");
		//1 dieselGenerator
		for(int i=0;i<4;i++)
			icons[1][i] = iconRegister.registerIcon("immersiveengineering:storage_Steel");
		//2 industrialSqueezer
		icons[2][0] = iconRegister.registerIcon("immersiveengineering:metal_squeezer");
		icons[2][1] = iconRegister.registerIcon("immersiveengineering:metal_multiblockTop");
		icons[2][2] = iconRegister.registerIcon("immersiveengineering:metal_multiblockSqueezer0");
		icons[2][3] = iconRegister.registerIcon("immersiveengineering:metal_multiblockSqueezer1");
		//3 fermenter
		icons[3][0] = iconRegister.registerIcon("immersiveengineering:metal_fermenter");
		icons[3][1] = iconRegister.registerIcon("immersiveengineering:metal_multiblockTop");
		icons[3][2] = iconRegister.registerIcon("immersiveengineering:metal_multiblockFermenter0");
		icons[3][3] = iconRegister.registerIcon("immersiveengineering:metal_multiblockFermenter1");
		//4 fermenter
		icons[4][0] = iconRegister.registerIcon("immersiveengineering:metal_fermenter");
		icons[4][1] = iconRegister.registerIcon("immersiveengineering:metal_multiblockTop");
		icons[4][2] = iconRegister.registerIcon("immersiveengineering:metal_multiblockFermenter0");
		icons[4][3] = iconRegister.registerIcon("immersiveengineering:metal_multiblockFermenter1");
		//5 crusher
		for(int i=0;i<4;i++)
			icons[5][i] = iconRegister.registerIcon("immersiveengineering:storage_Steel");
	}
	@Override
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side)
	{
		if(world.getBlockMetadata(x, y, z)==META_squeezer)
		{
			if(world.getTileEntity(x, y, z) instanceof TileEntitySqueezer && ((TileEntitySqueezer)world.getTileEntity(x,y,z)).formed)
				return icons[META_squeezer][ side==0||side==1?1 : ((TileEntitySqueezer)world.getTileEntity(x,y,z)).facing==side?2: 3];
			return icons[META_squeezer][0];
		}
		if(world.getBlockMetadata(x, y, z)==META_fermenter)
		{
			if(world.getTileEntity(x, y, z) instanceof TileEntityFermenter && ((TileEntityFermenter)world.getTileEntity(x,y,z)).formed)
				return icons[META_fermenter][ side==0||side==1?1 : ((TileEntityFermenter)world.getTileEntity(x,y,z)).facing==side?2: 3];
			return icons[META_fermenter][0];
		}
		if(world.getBlockMetadata(x, y, z)==META_refinery)
		{
			if(world.getTileEntity(x, y, z) instanceof TileEntityRefinery && ((TileEntityRefinery)world.getTileEntity(x,y,z)).formed)
				return icons[META_refinery][ side==0||side==1?1 : ((TileEntityRefinery)world.getTileEntity(x,y,z)).facing==side?2: 3];
			return icons[META_refinery][0];
		}
		return super.getIcon(world, x, y, z, side);
	}
	@Override
	public int getRenderType()
	{
		return BlockRenderMetalMultiblocks.renderID;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block par5, int par6)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityMultiblockPart)
		{
			TileEntityMultiblockPart tile = (TileEntityMultiblockPart)world.getTileEntity(x, y, z);
			if(!tile.formed && tile.pos==-1)
				world.spawnEntityInWorld(new EntityItem(world, x+.5,y+.5,z+.5, tile.getOriginalBlock()));
		}

		if(world.getTileEntity(x, y, z) instanceof TileEntityLightningRod)
		{
			byte off = ((TileEntityLightningRod)world.getTileEntity(x, y, z)).type;
			int xx = x- (off%3-1);
			int zz = z- (off/3-1);

			for(int ix=-1;ix<=1;ix++)
				for(int iz=-1;iz<=1;iz++)
					if(world.getTileEntity(xx+ix, y, zz+iz) instanceof TileEntityLightningRod)
					{
						((TileEntityLightningRod)world.getTileEntity(xx+ix, y, zz+iz)).formed=false;
						((TileEntityLightningRod)world.getTileEntity(xx+ix, y, zz+iz)).type=4;
						world.getTileEntity(xx+ix, y, zz+iz).markDirty();
						world.markBlockForUpdate(xx+ix, y, zz+iz);
					}
		}

		super.breakBlock(world, x, y, z, par5, par6);
	}
	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
	{
		return new ArrayList<ItemStack>();
	}
	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player)
	{
		return getOriginalBlock(world, x, y, z);
	}

	public ItemStack getOriginalBlock(World world, int x, int y, int z)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityMultiblockPart)
			return ((TileEntityMultiblockPart)world.getTileEntity(x, y, z)).getOriginalBlock();
		return new ItemStack(this, 1, world.getBlockMetadata(x, y, z));
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityLightningRod && Utils.isHammer(player.getCurrentEquippedItem()))
		{
			for(int xx=-1;xx<=1;xx++)
				for(int zz=-1;zz<=1;zz++)
					if(!(world.getTileEntity(x+xx, y+0, z+zz) instanceof TileEntityLightningRod && !((TileEntityLightningRod)world.getTileEntity(x+xx, y+0, z+zz)).formed))
						return false;
			for(int xx=-1;xx<=1;xx++)
				for(int zz=-1;zz<=1;zz++)
				{
					((TileEntityLightningRod)world.getTileEntity(x+xx, y+0, z+zz)).type=(byte) ((xx+1)+(zz+1)*3);
					((TileEntityLightningRod)world.getTileEntity(x+xx, y+0, z+zz)).formed=true;
					world.getTileEntity(x+xx, y+0, z+zz).markDirty();
					world.markBlockForUpdate(x+xx,y+0,z+zz);
				}
			return true;
		}
		if(world.getTileEntity(x, y, z) instanceof TileEntitySqueezer)
		{
			if(!player.isSneaking() && ((TileEntitySqueezer)world.getTileEntity(x, y, z)).formed )
			{
				TileEntitySqueezer te = ((TileEntitySqueezer)world.getTileEntity(x, y, z)).master();
				if(te==null)
					te = ((TileEntitySqueezer)world.getTileEntity(x, y, z));
				if(!world.isRemote)
					player.openGui(ImmersiveEngineering.instance, Lib.GUIID_Squeezer, world, te.xCoord, te.yCoord, te.zCoord);
				return true;
			}
		}
		if(world.getTileEntity(x, y, z) instanceof TileEntityFermenter)
		{
			if(!player.isSneaking() && ((TileEntityFermenter)world.getTileEntity(x, y, z)).formed )
			{
				TileEntityFermenter te = ((TileEntityFermenter)world.getTileEntity(x, y, z)).master();
				if(te==null)
					te = ((TileEntityFermenter)world.getTileEntity(x, y, z));
				if(!world.isRemote)
					player.openGui(ImmersiveEngineering.instance, Lib.GUIID_Fermenter, world, te.xCoord, te.yCoord, te.zCoord);
				return true;
			}
		}
		return false;
	}


	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityRefinery)
		{
			TileEntityRefinery tile = (TileEntityRefinery)world.getTileEntity(x, y, z);
			if(tile.pos==9 && side.ordinal()==tile.facing)
				return true;
		}
		if(world.getTileEntity(x, y, z) instanceof TileEntityCrusher)
		{
			TileEntityCrusher tile = (TileEntityCrusher)world.getTileEntity(x, y, z);
			if(tile.pos==9 && side.ordinal()==tile.facing)
				return true;
		}
		return super.isSideSolid(world, x, y, z, side);
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityDieselGenerator)
		{
			TileEntityDieselGenerator tile = (TileEntityDieselGenerator)world.getTileEntity(x, y, z);
			int pos = tile.pos;
			if(pos>=3 && pos<36)
			{
				float height = pos%9>=6&&pos>9?.53f:1;
				if(pos%9==0||pos%9==3||pos%9==6)
					this.setBlockBounds((tile.facing==2?.25f:0),0,(tile.facing==5?.25f:0),  (tile.facing==3?.75f:1),height,(tile.facing==4?.75f:1));
				else if(pos%9==2||pos%9==5||pos%9==8)
					this.setBlockBounds((tile.facing==3?.25f:0),0,(tile.facing==4?.25f:0),  (tile.facing==2?.75f:1),height,(tile.facing==5?.75f:1));
				else
					this.setBlockBounds(0,0,0,  1,height,1);
			}
			else
				this.setBlockBounds(0,0,0,1,1,1);
		}
		else if(world.getTileEntity(x, y, z) instanceof TileEntityCrusher)
		{
			TileEntityCrusher tile = (TileEntityCrusher)world.getTileEntity(x, y, z);
			int pos = tile.pos;
			int f = tile.facing;

			if(pos%15>=6&&pos%15<=8)
			{
				if(pos/15==0)
				{
					if(pos%5==1)
						this.setBlockBounds(f==3||f==4?.1875f:0, .5f, f==2||f==4?.1875f:0, f==2||f==5?.8125f:1, 1, f==3||f==5?.8125f:1);
					else if(pos%5==2)
						this.setBlockBounds(f==4?.1875f:0, .5f, f==2?.1875f:0, f==5?.8125f:1, 1, f==3?.8125f:1);
					else if(pos%5==3)
						this.setBlockBounds(f==2||f==4?.1875f:0, .5f, f==2||f==5?.1875f:0, f==3||f==5?.8125f:1, 1, f==3||f==4?.8125f:1);
				}
				else if(pos/15==1)
				{
					if(pos%5==1)
						this.setBlockBounds(f==3?.1875f:0, .5f, f==4?.1875f:0, f==2?.8125f:1, 1, f==5?.8125f:1);
					else if(pos%5==2)
						this.setBlockBounds(0,0,0, 1,1,1);
					else if(pos%5==3)
						this.setBlockBounds(f==2?.1875f:0, .5f, f==5?.1875f:0, f==3?.8125f:1, 1, f==4?.8125f:1);
				}
				else if(pos/15==2)
				{
					if(pos%5==1)
						this.setBlockBounds(f==3||f==5?.1875f:0, .5f, f==3||f==4?.1875f:0, f==2||f==4?.8125f:1, 1, f==2||f==5?.8125f:1);
					else if(pos%5==2)
						this.setBlockBounds(f==5?.1875f:0, .5f, f==3?.1875f:0, f==4?.8125f:1, 1, f==2?.8125f:1);
					else if(pos%5==3)
						this.setBlockBounds(f==2||f==5?.1875f:0, .5f, f==3||f==5?.1875f:0, f==3||f==4?.8125f:1, 1, f==2||f==4?.8125f:1);
				}
			}
			else if(pos%15>=11&&pos%15<=13) 
			{
				if(pos/15==0)
				{
					if(pos%5==2)
						this.setBlockBounds(f==4?.1875f:f==5?.5625f:0, 0, f==2?.1875f:f==3?.5625f:0, f==5?.8125f:f==4?.4375f:1, 1, f==3?.8125f:f==2?.4375f:1);
				}
				else if(pos/15==1)
				{
					if(pos%5==1)
						this.setBlockBounds(f==3?.1875f:f==2?.5625f:0, 0, f==4?.1875f:f==5?.5625f:0, f==2?.8125f:f==3?.4375f:1, 1, f==5?.8125f:f==4?.4375f:1);
					else if(pos%5==2)
						this.setBlockBounds(0,0,0, 0,0,0);
					else if(pos%5==3)
						this.setBlockBounds(f==2?.1875f:f==3?.5625f:0, 0, f==5?.1875f:f==4?.5625f:0, f==3?.8125f:f==2?.4375f:1, 1, f==4?.8125f:f==5?.4375f:1);
				}
				else if(pos/15==2)
				{
					if(pos%5==2)
						this.setBlockBounds(f==5?.1875f:f==4?.5625f:0, 0, f==3?.1875f:f==2?.5625f:0, f==4?.8125f:f==5?.4375f:1, 1, f==2?.8125f:f==3?.4375f:1);
				}
			}
			else if(pos==9)
				this.setBlockBounds(f==5?.5f:0,0,f==3?.5f:0,  f==4?.5f:1,1,f==2?.5f:1);
			else if(pos==1||pos==3 || pos==16||pos==18||pos==24 || (pos>=31&&pos<=34))
				this.setBlockBounds(0,0,0,1,.5f,1);
			else
				this.setBlockBounds(0,0,0,1,1,1);
		}
		else if(world.getTileEntity(x, y, z) instanceof TileEntityRefinery)
		{
			TileEntityRefinery tile = (TileEntityRefinery)world.getTileEntity(x, y, z);
			int pos = tile.pos;
			int f = tile.facing;
			if(pos==7)
				this.setBlockBounds(.0625f,0,.0625f, .9375f,1,.9375f);
			else if(pos==37)
				this.setBlockBounds(f==4?.5f:0,0,f==2?.5f:0, f==5?.5f:1,1,f==3?.5f:1);
			else if(pos==20||pos==25)
				this.setBlockBounds(f==3?.1875f:0,0,f==4?.1875f:0, f==2?.8125f:1,1,f==5?.8125f:1);
			else if(pos==24||pos==29)
				this.setBlockBounds(f==2?.1875f:0,0,f==5?.1875f:0, f==3?.8125f:1,1,f==4?.8125f:1);
			else if((pos>=5&&pos<15&&pos!=9)||(pos>=35&&pos<45))
			{
				this.minY= pos/5>1?0:.375f;
				this.maxY=1;
				this.minX= f==3?.4375f: f==4?.6875f: 0;
				this.maxX= f==2?.5625f: f==5?.3125f: 1;
				this.minZ= f==4?.4375f: f==2?.6875f: 0;
				this.maxZ= f==5?.5625f: f==3?.3125f: 1;
				if(pos%5==4)
				{
					this.minX+=f==3?-.4375f: f==2?.4375f: 0;
					this.maxX+=f==3?-.4375f: f==2?.4375f: 0;
					this.minZ+=f==4?-.4375f: f==5?.4375f: 0;
					this.maxZ+=f==4?-.4375f: f==5?.4375f: 0;
				}
				if(pos/15==2)
				{
					this.minX+=f==4?-.6875f: f==5?.6875f: 0;
					this.maxX+=f==4?-.6875f: f==5?.6875f: 0;
					this.minZ+=f==3?.6875f: f==2?-.6875f: 0;
					this.maxZ+=f==3?.6875f: f==2?-.6875f: 0;
				}
				if(pos%5!=0&&pos%5!=4)
				{
					this.minX = f==2||f==3?0:minX;
					this.maxX = f==2||f==3?1:maxX;
					this.minZ = f==4||f==5?0:minZ;
					this.maxZ = f==4||f==5?1:maxZ;
				}
				//				if(pos%5==0)
				//					this.setBlockBounds(f==3?.4375f:f==2?0:.6875f, 0, f==2?.6875f:f==3?0:.4375f,   f==2?.5625f:f==3?1:1, 1, f==2?1:f==3?.3125f:);
			}
			else if(pos==0||pos==1||pos==3 || pos==30||pos==31||pos==33||pos==34)
				this.setBlockBounds(0,0,0,1,.5f,1);
			else
				this.setBlockBounds(0,0,0,1,1,1);
		}
		else
			this.setBlockBounds(0,0,0,1,1,1);
	}
	@Override
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB aabb, List list, Entity ent)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityCrusher)
		{
			TileEntityCrusher tile = (TileEntityCrusher)world.getTileEntity(x, y, z);
			if(tile.pos%15>=11&&tile.pos%15<=13)
			{	
				int pos = tile.pos;
				int f = tile.facing;
				if(pos/15==0 && (pos%5==1||pos%5==3))
				{
					if(pos%5==1)
					{
						this.setBlockBounds(f==4||f==3?.1875f:f==2?.5625f:0, 0, f==2||f==4?.1875f:f==5?.5625f:0, f==5||f==2?.8125f:f==3?.4375f:1, 1, f==3||f==5?.8125f:f==4?.4375f:1);
						super.addCollisionBoxesToList(world, x, y, z, aabb, list, ent);
						this.setBlockBounds(f==4||f==3?.1875f:f==5?.5625f:0, 0, f==2||f==4?.1875f:f==3?.5625f:0, f==5||f==2?.8125f:f==4?.4375f:1, 1, f==3||f==5?.8125f:f==2?.4375f:1);
					}
					else
					{
						this.setBlockBounds(f==4||f==2?.1875f:f==3?.5625f:0, 0, f==2||f==5?.1875f:f==4?.5625f:0, f==5||f==3?.8125f:f==2?.4375f:1, 1, f==3||f==4?.8125f:f==5?.4375f:1);
						super.addCollisionBoxesToList(world, x, y, z, aabb, list, ent);
						this.setBlockBounds(f==4||f==2?.1875f:f==5?.5625f:0, 0, f==2||f==5?.1875f:f==3?.5625f:0, f==5||f==3?.8125f:f==4?.4375f:1, 1, f==3||f==4?.8125f:f==2?.4375f:1);
					}
				}
				else if(pos/15==2 && (pos%5==1||pos%5==3))
				{
					if(pos%5==1)
					{
						this.setBlockBounds(f==5||f==3?.1875f:f==2?.5625f:0, 0, f==3||f==4?.1875f:f==5?.5625f:0, f==4||f==2?.8125f:f==3?.4375f:1, 1, f==2||f==5?.8125f:f==4?.4375f:1);
						super.addCollisionBoxesToList(world, x, y, z, aabb, list, ent);
						this.setBlockBounds(f==5||f==3?.1875f:f==4?.5625f:0, 0, f==3||f==4?.1875f:f==2?.5625f:0, f==4||f==2?.8125f:f==5?.4375f:1, 1, f==2||f==5?.8125f:f==3?.4375f:1);
					}
					else
					{
						this.setBlockBounds(f==5||f==2?.1875f:f==3?.5625f:0, 0, f==3||f==5?.1875f:f==4?.5625f:0, f==4||f==3?.8125f:f==2?.4375f:1, 1, f==2||f==4?.8125f:f==5?.4375f:1);
						super.addCollisionBoxesToList(world, x, y, z, aabb, list, ent);
						this.setBlockBounds(f==5||f==2?.1875f:f==4?.5625f:0, 0, f==3||f==5?.1875f:f==2?.5625f:0, f==4||f==3?.8125f:f==5?.4375f:1, 1, f==2||f==4?.8125f:f==3?.4375f:1);
					}
				}

			}
		}
		super.addCollisionBoxesToList(world, x, y, z, aabb, list, ent);
	}
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
	{
		this.setBlockBoundsBasedOnState(world,x,y,z);
		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}
	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityCrusher)
		{
			TileEntityCrusher tile = (TileEntityCrusher)world.getTileEntity(x, y, z);
			int pos = tile.pos;
			if(pos%15>=11&&pos%15<=13)
			{		
				ChunkCoordinates cc = new ChunkCoordinates(x,y,z);
				ClientEventHandler.additionalBlockBounds.removeAll(cc);
				int f = tile.facing;
				if(pos/15==0 && (pos%5==1||pos%5==3))
				{
					if(pos%5==1)
					{	
						AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(f==4?.4375f:f==3?.1875f:f==2?.5625f:0, 0, f==2?.4375f:f==4?.1875f:f==5?.5625f:0, f==5?.5625f:f==2?.8125f:f==3?.4375f:1, 1, f==3?.5625f:f==5?.8125f:f==4?.4375f:1);
						ClientEventHandler.addAdditionalBlockBounds(cc, aabb);
						aabb = AxisAlignedBB.getBoundingBox(f==4||f==3?.1875f:f==5?.5625f:0, 0, f==2||f==4?.1875f:f==3?.5625f:0, f==5||f==2?.8125f:f==4?.4375f:1, 1, f==3||f==5?.8125f:f==2?.4375f:1);
						ClientEventHandler.addAdditionalBlockBounds(cc, aabb);
					}
					else
					{
						AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(f==4?.4375f:f==2?.1875f:f==3?.5625f:0, 0, f==2?.4375f:f==5?.1875f:f==4?.5625f:0, f==5?.5625f:f==3?.8125f:f==2?.4375f:1, 1, f==3?.5625f:f==4?.8125f:f==5?.4375f:1);
						ClientEventHandler.addAdditionalBlockBounds(cc, aabb);
						aabb = AxisAlignedBB.getBoundingBox(f==4||f==2?.1875f:f==5?.5625f:0, 0, f==2||f==5?.1875f:f==3?.5625f:0, f==5||f==3?.8125f:f==4?.4375f:1, 1, f==3||f==4?.8125f:f==2?.4375f:1);
						ClientEventHandler.addAdditionalBlockBounds(cc, aabb);
					}
				}
				else if(pos/15==2 && (pos%5==1||pos%5==3))
				{
					if(pos%5==1)
					{
						AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(f==5?.4375f:f==3?.1875f:f==2?.5625f:0, 0, f==3?.4375f:f==4?.1875f:f==5?.5625f:0, f==4?.5625f:f==2?.8125f:f==3?.4375f:1, 1, f==2?.5625f:f==5?.8125f:f==4?.4375f:1);
						ClientEventHandler.addAdditionalBlockBounds(cc, aabb);
						aabb = AxisAlignedBB.getBoundingBox(f==5||f==3?.1875f:f==4?.5625f:0, 0, f==3||f==4?.1875f:f==2?.5625f:0, f==4||f==2?.8125f:f==5?.4375f:1, 1, f==2||f==5?.8125f:f==3?.4375f:1);
						ClientEventHandler.addAdditionalBlockBounds(cc, aabb);
					}
					else
					{
						AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(f==5?.4375f:f==2?.1875f:f==3?.5625f:0, 0, f==3?.4375f:f==5?.1875f:f==4?.5625f:0, f==4?.5625f:f==3?.8125f:f==2?.4375f:1, 1, f==2?.5625f:f==4?.8125f:f==5?.4375f:1);
						ClientEventHandler.addAdditionalBlockBounds(cc, aabb);
						aabb = AxisAlignedBB.getBoundingBox(f==5||f==2?.1875f:f==4?.5625f:0, 0, f==3||f==5?.1875f:f==2?.5625f:0, f==4||f==3?.8125f:f==5?.4375f:1, 1, f==2||f==4?.8125f:f==3?.4375f:1);
						ClientEventHandler.addAdditionalBlockBounds(cc, aabb);
					}
				}
			}
			else if(pos==1 || pos==3 || pos==31 || pos==33)
			{
				ChunkCoordinates cc = new ChunkCoordinates(x,y,z);
				ClientEventHandler.additionalBlockBounds.get(cc).clear();
				int f = tile.facing;
				AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(0,0,0, 1,.5,1);
				ClientEventHandler.addAdditionalBlockBounds(cc, aabb);

				aabb = AxisAlignedBB.getBoundingBox(f==(pos%30==1?2:3)||f==(pos<30?5:4)?.5:.25,  .5,  f==(pos<30?3:2)||f==(pos%30==1?5:4)?.5:.25,    f==(pos%30==1?2:3)||f==(pos<30?5:4)?.75:.5,  1.5,  f==(pos<30?3:2)||f==(pos%30==1?5:4)?.75:.5);
				ClientEventHandler.addAdditionalBlockBounds(cc, aabb);
			}
		}
		this.setBlockBoundsBasedOnState(world,x,y,z);
		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		switch(meta)
		{
		case 0://0 lightningRod
			return new TileEntityLightningRod();
		case 1://1 dieselGenerator
			return new TileEntityDieselGenerator();
		case 2://2 squeezer
			return new TileEntitySqueezer();
		case 3://3 fermenter
			return new TileEntityFermenter();
		case 4://4 fermenter
			return new TileEntityRefinery();
		case 5://5 crusher
			return new TileEntityCrusher();
		}
		return null;
	}

}