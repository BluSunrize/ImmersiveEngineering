package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidContainerItem;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.render.BlockRenderMetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ICustomBoundingboxes;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockMetalMultiblocks extends BlockIEBase implements ICustomBoundingboxes
{
	public static int META_lightningRod=0;
	public static int META_dieselGenerator=1;
	public static int META_squeezer=2;
	public static int META_fermenter=3;
	public static int META_refinery=4;
	public static int META_crusher=5;
	public static int META_bucketWheel=6;
	public static int META_excavator=7;
	public static int META_arcFurnace=8;
	public static int META_tank=9;
	public static int META_silo=10;
	public BlockMetalMultiblocks()
	{
		super("metalMultiblock", Material.iron, 4, ItemBlockIEBase.class,
				"lightningRod","dieselGenerator",
				"industrialSqueezer","fermenter","refinery",
				"crusher","bucketWheel","excavator","arcFurnace",
				"tank","silo");
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
			if(i!=META_dieselGenerator && i!=META_refinery && i!=META_crusher && i!=META_bucketWheel && i!=META_excavator && i!=META_arcFurnace)
				list.add(new ItemStack(item, 1, i));
	}

	@Override
	@SideOnly(Side.CLIENT)
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
		//all others
		for(int i=0;i<4;i++)
		{
			icons[4][i] = iconRegister.registerIcon("immersiveengineering:storage_Steel");
			icons[5][i] = iconRegister.registerIcon("immersiveengineering:storage_Steel");
			icons[6][i] = iconRegister.registerIcon("immersiveengineering:storage_Steel");
			icons[7][i] = iconRegister.registerIcon("immersiveengineering:storage_Steel");
			icons[8][i] = iconRegister.registerIcon("immersiveengineering:storage_Steel");
			icons[9][i] = iconRegister.registerIcon("immersiveengineering:storage_Steel");
			icons[10][i] = iconRegister.registerIcon("immersiveengineering:storage_Steel");
		}
	}
	@Override
	@SideOnly(Side.CLIENT)
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
			if(!tile.formed && tile.pos==-1 && tile.getOriginalBlock()!=null)
				world.spawnEntityInWorld(new EntityItem(world, x+.5,y+.5,z+.5, tile.getOriginalBlock().copy()));
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
		if(world.getTileEntity(x, y, z) instanceof TileEntityRefinery)
		{
			if(!player.isSneaking() && ((TileEntityRefinery)world.getTileEntity(x, y, z)).formed )
			{
				TileEntityRefinery te = ((TileEntityRefinery)world.getTileEntity(x, y, z)).master();
				if(te==null)
					te = ((TileEntityRefinery)world.getTileEntity(x, y, z));
				if(!world.isRemote)
					player.openGui(ImmersiveEngineering.instance, Lib.GUIID_Refinery, world, te.xCoord, te.yCoord, te.zCoord);
				return true;
			}
		}
		if(world.getTileEntity(x, y, z) instanceof TileEntityDieselGenerator && Utils.isHammer(player.getCurrentEquippedItem()) &&((TileEntityDieselGenerator)world.getTileEntity(x, y, z)).pos==40)
		{
			TileEntityDieselGenerator te = ((TileEntityDieselGenerator)world.getTileEntity(x, y, z)).master();
			if(te==null)
				te = ((TileEntityDieselGenerator)world.getTileEntity(x, y, z));
			te.mirrored = !te.mirrored;
			te.markDirty();
			world.markBlockForUpdate(te.xCoord, te.yCoord, te.zCoord);
		}
		if(world.getTileEntity(x, y, z) instanceof TileEntityArcFurnace)
		{
			if(!player.isSneaking() && ((TileEntityArcFurnace)world.getTileEntity(x, y, z)).formed )
			{
				TileEntityArcFurnace te = ((TileEntityArcFurnace)world.getTileEntity(x, y, z));
				if(te.pos==2||te.pos==25|| (te.pos>25 && te.pos%5>0 && te.pos%5<4 && te.pos%25/5<4))
				{
					TileEntityArcFurnace master = te.master();
					if(master==null)
						master = te;
					if(!world.isRemote)
						player.openGui(ImmersiveEngineering.instance, Lib.GUIID_ArcFurnace, world, master.xCoord, master.yCoord, master.zCoord);
					return true;
				}
			}
		}
		if(!player.isSneaking() && world.getTileEntity(x, y, z) instanceof TileEntitySheetmetalTank)
		{
			if(!world.isRemote)
			{
				TileEntitySheetmetalTank tank = (TileEntitySheetmetalTank)world.getTileEntity(x, y, z);
				TileEntitySheetmetalTank master = tank.master();
				if(master==null)
					master = tank;
				if(Utils.fillFluidHandlerWithPlayerItem(world, master, player))
					return true;
				if(Utils.fillPlayerItemFromFluidHandler(world, master, player, master.tank.getFluid()))
					return true;
				if(player.getCurrentEquippedItem()!=null && player.getCurrentEquippedItem().getItem() instanceof IFluidContainerItem)
					return true;
			}
			return true;
		}
		if(!world.isRemote && !player.isSneaking() && world.getTileEntity(x, y, z) instanceof TileEntitySilo)
		{
			TileEntitySilo te = ( TileEntitySilo)world.getTileEntity(x, y, z);
			TileEntitySilo master = te.master();
			if(master==null)
				master = te;
			System.out.println("ident: "+master.identStack);
			System.out.println("amount: "+master.storageAmount);
		}
		return false;
	}


	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityDieselGenerator)
		{
			TileEntityDieselGenerator tile = (TileEntityDieselGenerator)world.getTileEntity(x, y, z);
			if(tile.pos==39||tile.pos==40||tile.pos==41)
				return side == ForgeDirection.UP;
			else if(tile.pos==36||tile.pos==38)
				return true;
			else if(tile.pos==21)
				return true;
		}
		if(world.getTileEntity(x, y, z) instanceof TileEntityRefinery)
		{
			TileEntityRefinery tile = (TileEntityRefinery)world.getTileEntity(x, y, z);
			if(tile.pos==9 && side.ordinal()==tile.facing)
				return true;
		}
		if(world.getTileEntity(x, y, z) instanceof TileEntityCrusher)
		{
			TileEntityCrusher tile = (TileEntityCrusher)world.getTileEntity(x, y, z);
			if(tile.pos%5==0)
				return true;
			else if(tile.pos==9 && side.ordinal()==tile.facing)
				return true;
		}
		if(world.getTileEntity(x, y, z) instanceof TileEntityExcavator)
		{
			TileEntityExcavator tile = (TileEntityExcavator)world.getTileEntity(x, y, z);
			if(tile.pos<27)
				return true;
		}
		if(world.getTileEntity(x, y, z) instanceof TileEntityArcFurnace)
		{
			TileEntityArcFurnace tile = (TileEntityArcFurnace)world.getTileEntity(x, y, z);
			if(tile.pos==2 || tile.pos==25 || tile.pos==52)
				return side.ordinal()==tile.facing || (tile.pos==52?side==ForgeDirection.UP:false);
			if(tile.pos==82 || tile.pos==86 || tile.pos==88 || tile.pos==112)
				return side==ForgeDirection.UP;
			if( (tile.pos>=21&&tile.pos<=23) || (tile.pos>=46&&tile.pos<=48) || (tile.pos>=71&&tile.pos<=73))
				return side.getOpposite().ordinal()==tile.facing;
		}
		if(world.getTileEntity(x, y, z) instanceof TileEntitySheetmetalTank || world.getTileEntity(x, y, z) instanceof TileEntitySilo)
		{
			TileEntityMultiblockPart tile = (TileEntityMultiblockPart)world.getTileEntity(x, y, z);
			return tile.pos==4||tile.pos==(tile instanceof TileEntitySilo?58:40)||(tile.pos>=18&&tile.pos<(tile instanceof TileEntitySilo?54:36));
		}
		return super.isSideSolid(world, x, y, z, side);
	}

	@Override
	public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityDieselGenerator)
			return ((TileEntityDieselGenerator)world.getTileEntity(x, y, z)).pos==21 || ((TileEntityDieselGenerator)world.getTileEntity(x, y, z)).pos==23;
		if(world.getTileEntity(x, y, z) instanceof TileEntityRefinery)
			return ((TileEntityRefinery)world.getTileEntity(x, y, z)).pos==9 && side==((TileEntityRefinery)world.getTileEntity(x, y, z)).facing;
		if(world.getTileEntity(x, y, z) instanceof TileEntityCrusher)
			return ((TileEntityCrusher)world.getTileEntity(x, y, z)).pos==9 && side==((TileEntityCrusher)world.getTileEntity(x, y, z)).facing;
		if(world.getTileEntity(x, y, z) instanceof TileEntityExcavator)
			return ((TileEntityExcavator)world.getTileEntity(x, y, z)).pos==3 || ((TileEntityExcavator)world.getTileEntity(x, y, z)).pos==5;
		if(world.getTileEntity(x, y, z) instanceof TileEntityArcFurnace)
			return ((TileEntityArcFurnace)world.getTileEntity(x, y, z)).pos==25;
		return false;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityMultiblockPart)
		{
			float[] bounds = ((TileEntityMultiblockPart)world.getTileEntity(x, y, z)).getBlockBounds();
			if(bounds!=null && bounds.length>5)
				this.setBlockBounds(bounds[0],bounds[1],bounds[2], bounds[3],bounds[4],bounds[5]);
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
				int fl = tile.facing;
				int fw = tile.facing;
				if(tile.mirrored)
					fw = ForgeDirection.OPPOSITES[fw];
				if(pos/15==0 && (pos%5==1||pos%5==3))
				{
					if(pos%5==1)
					{
						this.setBlockBounds(fl==4||fw==3?.1875f:fw==2?.5625f:0, 0, fl==2||fw==4?.1875f:fw==5?.5625f:0, fl==5||fw==2?.8125f:fw==3?.4375f:1, 1, fl==3||fw==5?.8125f:fw==4?.4375f:1);
						super.addCollisionBoxesToList(world, x, y, z, aabb, list, ent);
						this.setBlockBounds(fl==4||fw==3?.1875f:fl==5?.5625f:0, 0, fl==2||fw==4?.1875f:fl==3?.5625f:0, fl==5||fw==2?.8125f:fl==4?.4375f:1, 1, fl==3||fw==5?.8125f:fl==2?.4375f:1);
					}
					else
					{
						this.setBlockBounds(fl==4||fw==2?.1875f:fw==3?.5625f:0, 0, fl==2||fw==5?.1875f:fw==4?.5625f:0, fl==5||fw==3?.8125f:fw==2?.4375f:1, 1, fl==3||fw==4?.8125f:fw==5?.4375f:1);
						super.addCollisionBoxesToList(world, x, y, z, aabb, list, ent);
						this.setBlockBounds(fl==4||fw==2?.1875f:fl==5?.5625f:0, 0, fl==2||fw==5?.1875f:fl==3?.5625f:0, fl==5||fw==3?.8125f:fl==4?.4375f:1, 1, fl==3||fw==4?.8125f:fl==2?.4375f:1);
					}
				}
				else if(pos/15==2 && (pos%5==1||pos%5==3))
				{
					if(pos%5==1)
					{
						this.setBlockBounds(fl==5||fw==3?.1875f:fw==2?.5625f:0, 0, fl==3||fw==4?.1875f:fw==5?.5625f:0, fl==4||fw==2?.8125f:fw==3?.4375f:1, 1, fl==2||fw==5?.8125f:fw==4?.4375f:1);
						super.addCollisionBoxesToList(world, x, y, z, aabb, list, ent);
						this.setBlockBounds(fl==5||fw==3?.1875f:fl==4?.5625f:0, 0, fl==3||fw==4?.1875f:fl==2?.5625f:0, fl==4||fw==2?.8125f:fl==5?.4375f:1, 1, fl==2||fw==5?.8125f:fl==3?.4375f:1);
					}
					else
					{
						this.setBlockBounds(fl==5||fw==2?.1875f:fw==3?.5625f:0, 0, fl==3||fw==5?.1875f:fw==4?.5625f:0, fl==4||fw==3?.8125f:fw==2?.4375f:1, 1, fl==2||fw==4?.8125f:fw==5?.4375f:1);
						super.addCollisionBoxesToList(world, x, y, z, aabb, list, ent);
						this.setBlockBounds(fl==5||fw==2?.1875f:fl==4?.5625f:0, 0, fl==3||fw==5?.1875f:fl==2?.5625f:0, fl==4||fw==3?.8125f:fl==5?.4375f:1, 1, fl==2||fw==4?.8125f:fl==3?.4375f:1);
					}
				}

			}
		}
		super.addCollisionBoxesToList(world, x, y, z, aabb, list, ent);
	}
	@Override
	public Set<AxisAlignedBB> addCustomSelectionBoxesToList(World world, int x, int y, int z, EntityPlayer player)
	{
		Set<AxisAlignedBB> set = new HashSet<AxisAlignedBB>();
		if(world.getTileEntity(x, y, z) instanceof TileEntityCrusher)
		{
			TileEntityCrusher tile = (TileEntityCrusher)world.getTileEntity(x, y, z);
			int pos = tile.pos;
			if(pos%15>=11&&pos%15<=13)
			{		
				//				ChunkCoordinates cc = new ChunkCoordinates(x,y,z);
				//				ClientEventHandler.additionalBlockBounds.removeAll(cc);
				int fl = tile.facing;
				int fw = tile.facing;
				if(tile.mirrored)
					fw = ForgeDirection.OPPOSITES[fw];
				if(pos/15==0 && (pos%5==1||pos%5==3))
				{
					if(pos%5==1)
					{	
						set.add(AxisAlignedBB.getBoundingBox(fl==4?.4375f:fw==3?.1875f:fw==2?.5625f:0, 0, fl==2?.4375f:fw==4?.1875f:fw==5?.5625f:0, fl==5?.5625f:fw==2?.8125f:fw==3?.4375f:1, 1, fl==3?.5625f:fw==5?.8125f:fw==4?.4375f:1));
						//						AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(fl==4?.4375f:fw==3?.1875f:fw==2?.5625f:0, 0, fl==2?.4375f:fw==4?.1875f:fw==5?.5625f:0, fl==5?.5625f:fw==2?.8125f:fw==3?.4375f:1, 1, fl==3?.5625f:fw==5?.8125f:fw==4?.4375f:1);
						//						ClientEventHandler.addAdditionalBlockBounds(cc, aabb);
						set.add(AxisAlignedBB.getBoundingBox(fl==4||fw==3?.1875f:fl==5?.5625f:0, 0, fl==2||fw==4?.1875f:fl==3?.5625f:0, fl==5||fw==2?.8125f:fl==4?.4375f:1, 1, fl==3||fw==5?.8125f:fl==2?.4375f:1));
						//						aabb = AxisAlignedBB.getBoundingBox(fl==4||fw==3?.1875f:fl==5?.5625f:0, 0, fl==2||fw==4?.1875f:fl==3?.5625f:0, fl==5||fw==2?.8125f:fl==4?.4375f:1, 1, fl==3||fw==5?.8125f:fl==2?.4375f:1);
						//						ClientEventHandler.addAdditionalBlockBounds(cc, aabb);
					}
					else
					{
						set.add(AxisAlignedBB.getBoundingBox(fl==4?.4375f:fw==2?.1875f:fw==3?.5625f:0, 0, fl==2?.4375f:fw==5?.1875f:fw==4?.5625f:0, fl==5?.5625f:fw==3?.8125f:fw==2?.4375f:1, 1, fl==3?.5625f:fw==4?.8125f:fw==5?.4375f:1));
						//						AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(fl==4?.4375f:fw==2?.1875f:fw==3?.5625f:0, 0, fl==2?.4375f:fw==5?.1875f:fw==4?.5625f:0, fl==5?.5625f:fw==3?.8125f:fw==2?.4375f:1, 1, fl==3?.5625f:fw==4?.8125f:fw==5?.4375f:1);
						//						ClientEventHandler.addAdditionalBlockBounds(cc, aabb);
						set.add(AxisAlignedBB.getBoundingBox(fl==4||fw==2?.1875f:fl==5?.5625f:0, 0, fl==2||fw==5?.1875f:fl==3?.5625f:0, fl==5||fw==3?.8125f:fl==4?.4375f:1, 1, fl==3||fw==4?.8125f:fl==2?.4375f:1));
						//						aabb = AxisAlignedBB.getBoundingBox(fl==4||fw==2?.1875f:fl==5?.5625f:0, 0, fl==2||fw==5?.1875f:fl==3?.5625f:0, fl==5||fw==3?.8125f:fl==4?.4375f:1, 1, fl==3||fw==4?.8125f:fl==2?.4375f:1);
						//						ClientEventHandler.addAdditionalBlockBounds(cc, aabb);
					}
				}
				else if(pos/15==2 && (pos%5==1||pos%5==3))
				{
					if(pos%5==1)
					{
						set.add(AxisAlignedBB.getBoundingBox(fl==5?.4375f:fw==3?.1875f:fw==2?.5625f:0, 0, fl==3?.4375f:fw==4?.1875f:fw==5?.5625f:0, fl==4?.5625f:fw==2?.8125f:fw==3?.4375f:1, 1, fl==2?.5625f:fw==5?.8125f:fw==4?.4375f:1));
						//						AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(fl==5?.4375f:fw==3?.1875f:fw==2?.5625f:0, 0, fl==3?.4375f:fw==4?.1875f:fw==5?.5625f:0, fl==4?.5625f:fw==2?.8125f:fw==3?.4375f:1, 1, fl==2?.5625f:fw==5?.8125f:fw==4?.4375f:1);
						//						ClientEventHandler.addAdditionalBlockBounds(cc, aabb);
						set.add(AxisAlignedBB.getBoundingBox(fl==5||fw==3?.1875f:fl==4?.5625f:0, 0, fl==3||fw==4?.1875f:fl==2?.5625f:0, fl==4||fw==2?.8125f:fl==5?.4375f:1, 1, fl==2||fw==5?.8125f:fl==3?.4375f:1));
						//						aabb = AxisAlignedBB.getBoundingBox(fl==5||fw==3?.1875f:fl==4?.5625f:0, 0, fl==3||fw==4?.1875f:fl==2?.5625f:0, fl==4||fw==2?.8125f:fl==5?.4375f:1, 1, fl==2||fw==5?.8125f:fl==3?.4375f:1);
						//						ClientEventHandler.addAdditionalBlockBounds(cc, aabb);
					}
					else
					{
						set.add(AxisAlignedBB.getBoundingBox(fl==5?.4375f:fw==2?.1875f:fw==3?.5625f:0, 0, fl==3?.4375f:fw==5?.1875f:fw==4?.5625f:0, fl==4?.5625f:fw==3?.8125f:fw==2?.4375f:1, 1, fl==2?.5625f:fw==4?.8125f:fw==5?.4375f:1));
						//						AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(fl==5?.4375f:fw==2?.1875f:fw==3?.5625f:0, 0, fl==3?.4375f:fw==5?.1875f:fw==4?.5625f:0, fl==4?.5625f:fw==3?.8125f:fw==2?.4375f:1, 1, fl==2?.5625f:fw==4?.8125f:fw==5?.4375f:1);
						//						ClientEventHandler.addAdditionalBlockBounds(cc, aabb);
						set.add(AxisAlignedBB.getBoundingBox(fl==5||fw==2?.1875f:fl==4?.5625f:0, 0, fl==3||fw==5?.1875f:fl==2?.5625f:0, fl==4||fw==3?.8125f:fl==5?.4375f:1, 1, fl==2||fw==4?.8125f:fl==3?.4375f:1));
						//						aabb = AxisAlignedBB.getBoundingBox(fl==5||fw==2?.1875f:fl==4?.5625f:0, 0, fl==3||fw==5?.1875f:fl==2?.5625f:0, fl==4||fw==3?.8125f:fl==5?.4375f:1, 1, fl==2||fw==4?.8125f:fl==3?.4375f:1);
						//						ClientEventHandler.addAdditionalBlockBounds(cc, aabb);
					}
				}
			}
			else if(pos==1 || pos==3 || pos==31 || pos==33)
			{
				//				ChunkCoordinates cc = new ChunkCoordinates(x,y,z);
				//				ClientEventHandler.additionalBlockBounds.get(cc).clear();
				int fl = tile.facing;
				int fw = tile.facing;
				if(tile.mirrored)
					fw = ForgeDirection.OPPOSITES[fw];
				set.add(AxisAlignedBB.getBoundingBox(0,0,0, 1,.5,1));
				//				AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(0,0,0, 1,.5,1);
				//				ClientEventHandler.addAdditionalBlockBounds(cc, aabb);

				set.add(AxisAlignedBB.getBoundingBox(fw==(pos%30==1?2:3)||fl==(pos<30?5:4)?.5:.25,  .5,  fl==(pos<30?3:2)||fw==(pos%30==1?5:4)?.5:.25,    fw==(pos%30==1?2:3)||fl==(pos<30?5:4)?.75:.5,  1.5,  fl==(pos<30?3:2)||fw==(pos%30==1?5:4)?.75:.5));
				//				aabb = AxisAlignedBB.getBoundingBox(fw==(pos%30==1?2:3)||fl==(pos<30?5:4)?.5:.25,  .5,  fl==(pos<30?3:2)||fw==(pos%30==1?5:4)?.5:.25,    fw==(pos%30==1?2:3)||fl==(pos<30?5:4)?.75:.5,  1.5,  fl==(pos<30?3:2)||fw==(pos%30==1?5:4)?.75:.5);
				//				ClientEventHandler.addAdditionalBlockBounds(cc, aabb);
			}
		}
		return set;
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
		//		if(world.getTileEntity(x, y, z) instanceof TileEntityCrusher)
		//		{
		//			TileEntityCrusher tile = (TileEntityCrusher)world.getTileEntity(x, y, z);
		//			int pos = tile.pos;
		//			if(pos%15>=11&&pos%15<=13)
		//			{		
		//				ChunkCoordinates cc = new ChunkCoordinates(x,y,z);
		//				ClientEventHandler.additionalBlockBounds.removeAll(cc);
		//				int fl = tile.facing;
		//				int fw = tile.facing;
		//				if(tile.mirrored)
		//					fw = ForgeDirection.OPPOSITES[fw];
		//				if(pos/15==0 && (pos%5==1||pos%5==3))
		//				{
		//					if(pos%5==1)
		//					{	
		//						AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(fl==4?.4375f:fw==3?.1875f:fw==2?.5625f:0, 0, fl==2?.4375f:fw==4?.1875f:fw==5?.5625f:0, fl==5?.5625f:fw==2?.8125f:fw==3?.4375f:1, 1, fl==3?.5625f:fw==5?.8125f:fw==4?.4375f:1);
		//						ClientEventHandler.addAdditionalBlockBounds(cc, aabb);
		//						aabb = AxisAlignedBB.getBoundingBox(fl==4||fw==3?.1875f:fl==5?.5625f:0, 0, fl==2||fw==4?.1875f:fl==3?.5625f:0, fl==5||fw==2?.8125f:fl==4?.4375f:1, 1, fl==3||fw==5?.8125f:fl==2?.4375f:1);
		//						ClientEventHandler.addAdditionalBlockBounds(cc, aabb);
		//					}
		//					else
		//					{
		//						AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(fl==4?.4375f:fw==2?.1875f:fw==3?.5625f:0, 0, fl==2?.4375f:fw==5?.1875f:fw==4?.5625f:0, fl==5?.5625f:fw==3?.8125f:fw==2?.4375f:1, 1, fl==3?.5625f:fw==4?.8125f:fw==5?.4375f:1);
		//						ClientEventHandler.addAdditionalBlockBounds(cc, aabb);
		//						aabb = AxisAlignedBB.getBoundingBox(fl==4||fw==2?.1875f:fl==5?.5625f:0, 0, fl==2||fw==5?.1875f:fl==3?.5625f:0, fl==5||fw==3?.8125f:fl==4?.4375f:1, 1, fl==3||fw==4?.8125f:fl==2?.4375f:1);
		//						ClientEventHandler.addAdditionalBlockBounds(cc, aabb);
		//					}
		//				}
		//				else if(pos/15==2 && (pos%5==1||pos%5==3))
		//				{
		//					if(pos%5==1)
		//					{
		//						AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(fl==5?.4375f:fw==3?.1875f:fw==2?.5625f:0, 0, fl==3?.4375f:fw==4?.1875f:fw==5?.5625f:0, fl==4?.5625f:fw==2?.8125f:fw==3?.4375f:1, 1, fl==2?.5625f:fw==5?.8125f:fw==4?.4375f:1);
		//						ClientEventHandler.addAdditionalBlockBounds(cc, aabb);
		//						aabb = AxisAlignedBB.getBoundingBox(fl==5||fw==3?.1875f:fl==4?.5625f:0, 0, fl==3||fw==4?.1875f:fl==2?.5625f:0, fl==4||fw==2?.8125f:fl==5?.4375f:1, 1, fl==2||fw==5?.8125f:fl==3?.4375f:1);
		//						ClientEventHandler.addAdditionalBlockBounds(cc, aabb);
		//					}
		//					else
		//					{
		//						AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(fl==5?.4375f:fw==2?.1875f:fw==3?.5625f:0, 0, fl==3?.4375f:fw==5?.1875f:fw==4?.5625f:0, fl==4?.5625f:fw==3?.8125f:fw==2?.4375f:1, 1, fl==2?.5625f:fw==4?.8125f:fw==5?.4375f:1);
		//						ClientEventHandler.addAdditionalBlockBounds(cc, aabb);
		//						aabb = AxisAlignedBB.getBoundingBox(fl==5||fw==2?.1875f:fl==4?.5625f:0, 0, fl==3||fw==5?.1875f:fl==2?.5625f:0, fl==4||fw==3?.8125f:fl==5?.4375f:1, 1, fl==2||fw==4?.8125f:fl==3?.4375f:1);
		//						ClientEventHandler.addAdditionalBlockBounds(cc, aabb);
		//					}
		//				}
		//			}
		//			else if(pos==1 || pos==3 || pos==31 || pos==33)
		//			{
		//				ChunkCoordinates cc = new ChunkCoordinates(x,y,z);
		//				ClientEventHandler.additionalBlockBounds.get(cc).clear();
		//				int fl = tile.facing;
		//				int fw = tile.facing;
		//				if(tile.mirrored)
		//					fw = ForgeDirection.OPPOSITES[fw];
		//				AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(0,0,0, 1,.5,1);
		//				ClientEventHandler.addAdditionalBlockBounds(cc, aabb);
		//
		//				aabb = AxisAlignedBB.getBoundingBox(fw==(pos%30==1?2:3)||fl==(pos<30?5:4)?.5:.25,  .5,  fl==(pos<30?3:2)||fw==(pos%30==1?5:4)?.5:.25,    fw==(pos%30==1?2:3)||fl==(pos<30?5:4)?.75:.5,  1.5,  fl==(pos<30?3:2)||fw==(pos%30==1?5:4)?.75:.5);
		//				ClientEventHandler.addAdditionalBlockBounds(cc, aabb);
		//			}
		//		}
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
		case 6://6 bucketWheel
			return new TileEntityBucketWheel();
		case 7://7 excavator
			return new TileEntityExcavator();
		case 8://8 arcFurnace
			return new TileEntityArcFurnace();
		case 9://9 tank
			return new TileEntitySheetmetalTank();
		case 10://10 silo
			return new TileEntitySilo();
		}
		return null;
	}
}