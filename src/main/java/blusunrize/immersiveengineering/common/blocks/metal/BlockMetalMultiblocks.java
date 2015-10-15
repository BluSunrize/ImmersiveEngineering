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
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
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
	public static final int META_lightningRod=0;
	public static final int META_dieselGenerator=1;
	public static final int META_squeezer=2;
	public static final int META_fermenter=3;
	public static final int META_refinery=4;
	public static final int META_crusher=5;
	public static final int META_bucketWheel=6;
	public static final int META_excavator=7;
	public static final int META_arcFurnace=8;
	public static final int META_tank=9;
	public static final int META_silo=10;
	public static final int META_assembler=11;
	public BlockMetalMultiblocks()
	{
		super("metalMultiblock", Material.iron, 4, ItemBlockIEBase.class,
				"lightningRod","dieselGenerator",
				"industrialSqueezer","fermenter","refinery",
				"crusher","bucketWheel","excavator","arcFurnace",
				"tank","silo","assembler");
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
			if(i!=META_dieselGenerator && i!=META_refinery && i!=META_crusher && i!=META_bucketWheel && i!=META_excavator && i!=META_arcFurnace && i!=META_tank && i!=META_silo && i!=META_assembler)
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
		icons[META_dieselGenerator][0] = iconRegister.registerIcon("immersiveengineering:metal_multiblock_dieselGenerator");
		icons[META_refinery][0] = iconRegister.registerIcon("immersiveengineering:metal_multiblock_refinery");
		icons[META_crusher][0] = iconRegister.registerIcon("immersiveengineering:metal_multiblock_crusher");
		icons[META_excavator][0] = iconRegister.registerIcon("immersiveengineering:metal_multiblock_excavator");
		icons[META_arcFurnace][0] = iconRegister.registerIcon("immersiveengineering:metal_multiblock_arcFurnace_inactive");
		icons[META_arcFurnace][1] = iconRegister.registerIcon("immersiveengineering:metal_multiblock_arcFurnace_active");
		icons[META_assembler][0] = iconRegister.registerIcon("immersiveengineering:metal_multiblock_assembler");
		for(int i=0;i<4;i++)
		{
			icons[6][i] = iconRegister.registerIcon("immersiveengineering:storage_Steel");
			icons[9][i] = iconRegister.registerIcon("immersiveengineering:storage_Steel");
			icons[10][i] = iconRegister.registerIcon("immersiveengineering:storage_Steel");
		}
	}
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if(world.getBlockMetadata(x, y, z)==META_squeezer)
		{
			if(te instanceof TileEntitySqueezer && ((TileEntitySqueezer)te).formed)
				return icons[META_squeezer][ side==0||side==1?1 : ((TileEntitySqueezer)te).facing==side?2: 3];
			return icons[META_squeezer][0];
		}
		if(world.getBlockMetadata(x, y, z)==META_fermenter)
		{
			if(te instanceof TileEntityFermenter && ((TileEntityFermenter)te).formed)
				return icons[META_fermenter][ side==0||side==1?1 : ((TileEntityFermenter)te).facing==side?2: 3];
			return icons[META_fermenter][0];
		}
		if(world.getBlockMetadata(x, y, z)==META_refinery)
		{
			if(te instanceof TileEntityRefinery && ((TileEntityRefinery)te).formed)
				return icons[META_refinery][ side==0||side==1?1 : ((TileEntityRefinery)te).facing==side?2: 3];
			return icons[META_refinery][0];
		}
		return super.getIcon(world, x, y, z, side);
	}
	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}
	@Override
	public int getRenderType()
	{
		return BlockRenderMetalMultiblocks.renderID;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block par5, int par6)
	{
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if(tileEntity instanceof TileEntityMultiblockPart && world.getGameRules().getGameRuleBooleanValue("doTileDrops"))
		{
			TileEntityMultiblockPart tile = (TileEntityMultiblockPart)tileEntity;
			if(!tile.formed && tile.pos==-1 && tile.getOriginalBlock()!=null)
				world.spawnEntityInWorld(new EntityItem(world, x+.5,y+.5,z+.5, tile.getOriginalBlock().copy()));

			if(tileEntity instanceof IInventory)
			{
				if(!world.isRemote && ((TileEntityMultiblockPart)tileEntity).formed)
				{
					TileEntity master = ((TileEntityMultiblockPart)tileEntity).master();
					if(master==null)
						master = tileEntity;
					for(int i=0; i<((IInventory)master).getSizeInventory(); i++)
					{
						ItemStack stack = ((IInventory)master).getStackInSlot(i);
						if(stack!=null)
						{
							float fx = world.rand.nextFloat() * 0.8F + 0.1F;
							float fz = world.rand.nextFloat() * 0.8F + 0.1F;

							EntityItem entityitem = new EntityItem(world, x+fx, y+.5, z+fz, stack);
							entityitem.motionX = world.rand.nextGaussian()*.05;
							entityitem.motionY = world.rand.nextGaussian()*.05+.2;
							entityitem.motionZ = world.rand.nextGaussian()*.05;
							if(stack.hasTagCompound())
								entityitem.getEntityItem().setTagCompound((NBTTagCompound)stack.getTagCompound().copy());
							world.spawnEntityInWorld(entityitem);
						}
					}
				}
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
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileEntityMultiblockPart)
			return ((TileEntityMultiblockPart)te).getOriginalBlock();
		return new ItemStack(this, 1, world.getBlockMetadata(x, y, z));
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity curr = world.getTileEntity(x, y, z);
		if(curr instanceof TileEntitySqueezer)
		{
			if(!player.isSneaking() && ((TileEntitySqueezer)curr).formed )
			{
				TileEntitySqueezer te = ((TileEntitySqueezer)curr).master();
				if(te==null)
					te = ((TileEntitySqueezer)curr);
				if(!world.isRemote)
					player.openGui(ImmersiveEngineering.instance, Lib.GUIID_Squeezer, world, te.xCoord, te.yCoord, te.zCoord);
				return true;
			}
		}
		if(curr instanceof TileEntityFermenter)
		{
			if(!player.isSneaking() && ((TileEntityFermenter)curr).formed )
			{
				TileEntityFermenter te = ((TileEntityFermenter)curr).master();
				if(te==null)
					te = ((TileEntityFermenter)curr);
				if(!world.isRemote)
					player.openGui(ImmersiveEngineering.instance, Lib.GUIID_Fermenter, world, te.xCoord, te.yCoord, te.zCoord);
				return true;
			}
		}
		if(curr instanceof TileEntityRefinery)
		{
			if(!player.isSneaking() && ((TileEntityRefinery)curr).formed )
			{
				TileEntityRefinery te = ((TileEntityRefinery)curr).master();
				if(te==null)
					te = ((TileEntityRefinery)curr);
				if(!world.isRemote)
					player.openGui(ImmersiveEngineering.instance, Lib.GUIID_Refinery, world, te.xCoord, te.yCoord, te.zCoord);
				return true;
			}
		}
		if(curr instanceof TileEntityDieselGenerator && Utils.isHammer(player.getCurrentEquippedItem()) &&((TileEntityDieselGenerator)curr).pos==40)
		{
			TileEntityDieselGenerator te = ((TileEntityDieselGenerator)curr).master();
			if(te==null)
				te = ((TileEntityDieselGenerator)curr);
			te.mirrored = !te.mirrored;
			te.markDirty();
			world.markBlockForUpdate(te.xCoord, te.yCoord, te.zCoord);
		}
		if(curr instanceof TileEntityArcFurnace)
		{
			if(!player.isSneaking() && ((TileEntityArcFurnace)curr).formed )
			{
				TileEntityArcFurnace te = ((TileEntityArcFurnace)curr);
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
		if(!player.isSneaking() && curr instanceof TileEntitySheetmetalTank)
		{
			if(!world.isRemote)
			{
				TileEntitySheetmetalTank tank = (TileEntitySheetmetalTank)curr;
				TileEntitySheetmetalTank master = tank.master();
				if(master==null)
					master = tank;
				if(Utils.fillFluidHandlerWithPlayerItem(world, master, player))
				{
					master.markDirty();
					world.markBlockForUpdate(master.xCoord,master.yCoord,master.zCoord);
					return true;
				}
				if(Utils.fillPlayerItemFromFluidHandler(world, master, player, master.tank.getFluid()))
				{
					master.markDirty();
					world.markBlockForUpdate(master.xCoord,master.yCoord,master.zCoord);
					return true;
				}
				if(player.getCurrentEquippedItem()!=null && player.getCurrentEquippedItem().getItem() instanceof IFluidContainerItem)
				{
					master.markDirty();
					world.markBlockForUpdate(master.xCoord,master.yCoord,master.zCoord);
					return true;
				}
			}
			return true;
		}
		if(curr instanceof TileEntityAssembler)
		{
			if(!player.isSneaking() && ((TileEntityAssembler)curr).formed)
			{
				TileEntityAssembler te = ((TileEntityAssembler)curr);
				TileEntityAssembler master = te.master();
				if(master==null)
					master = te;
				if(!world.isRemote)
					player.openGui(ImmersiveEngineering.instance, Lib.GUIID_Assembler, world, master.xCoord, master.yCoord, master.zCoord);
				return true;
			}
		}
		return false;
	}


	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileEntityDieselGenerator)
		{
			TileEntityDieselGenerator tile = (TileEntityDieselGenerator)te;
			if(tile.pos==39||tile.pos==40||tile.pos==41)
				return side == ForgeDirection.UP;
			else if(tile.pos==36||tile.pos==38)
				return true;
			else if((tile.pos==21&&!tile.master().mirrored) || (tile.pos==23&&tile.master().mirrored))
				return true;
		}
		if(te instanceof TileEntityRefinery)
		{
			TileEntityRefinery tile = (TileEntityRefinery)te;
			if(tile.pos==9 && side.ordinal()==tile.facing)
				return true;
		}
		if(te instanceof TileEntityCrusher)
		{
			TileEntityCrusher tile = (TileEntityCrusher)te;
			if(tile.pos%5==0)
				return true;
			else if(tile.pos==9 && side.ordinal()==tile.facing)
				return true;
		}
		if(te instanceof TileEntityExcavator)
		{
			TileEntityExcavator tile = (TileEntityExcavator)te;
			if(tile.pos<27)
				return true;
		}
		if(te instanceof TileEntityArcFurnace)
		{
			TileEntityArcFurnace tile = (TileEntityArcFurnace)te;
			if(tile.pos==2 || tile.pos==25 || tile.pos==52)
				return side.ordinal()==tile.facing || (tile.pos==52?side==ForgeDirection.UP:false);
			if(tile.pos==82 || tile.pos==86 || tile.pos==88 || tile.pos==112)
				return side==ForgeDirection.UP;
			if( (tile.pos>=21&&tile.pos<=23) || (tile.pos>=46&&tile.pos<=48) || (tile.pos>=71&&tile.pos<=73))
				return side.getOpposite().ordinal()==tile.facing;
		}
		if(te instanceof TileEntitySheetmetalTank || te instanceof TileEntitySilo)
		{
			TileEntityMultiblockPart tile = (TileEntityMultiblockPart)te;
			return tile.pos==4||tile.pos==(tile instanceof TileEntitySilo?58:40)||(tile.pos>=18&&tile.pos<(tile instanceof TileEntitySilo?54:36));
		}
		return super.isSideSolid(world, x, y, z, side);
	}

	@Override
	public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileEntityDieselGenerator)
			return ((TileEntityDieselGenerator)te).pos==21 || ((TileEntityDieselGenerator)te).pos==23;
		if(te instanceof TileEntityRefinery)
			return ((TileEntityRefinery)te).pos==9 && side==((TileEntityRefinery)te).facing;
		if(te instanceof TileEntityCrusher)
			return ((TileEntityCrusher)te).pos==9 && side==((TileEntityCrusher)te).facing;
		if(te instanceof TileEntityExcavator)
			return ((TileEntityExcavator)te).pos==3 || ((TileEntityExcavator)te).pos==5;
		if(te instanceof TileEntityArcFurnace)
			return ((TileEntityArcFurnace)te).pos==25;
		return false;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileEntityMultiblockPart)
		{
			float[] bounds = ((TileEntityMultiblockPart)te).getBlockBounds();
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
		this.setBlockBoundsBasedOnState(world, x, y, z);
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if(tileEntity instanceof TileEntityCrusher)
		{
			TileEntityCrusher tile = (TileEntityCrusher)tileEntity;
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
						addCollisionBox(world, x, y, z, aabb, list, ent);
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
						addCollisionBox(world, x, y, z, aabb, list, ent);
						this.setBlockBounds(fl==5||fw==3?.1875f:fl==4?.5625f:0, 0, fl==3||fw==4?.1875f:fl==2?.5625f:0, fl==4||fw==2?.8125f:fl==5?.4375f:1, 1, fl==2||fw==5?.8125f:fl==3?.4375f:1);
					}
					else
					{
						this.setBlockBounds(fl==5||fw==2?.1875f:fw==3?.5625f:0, 0, fl==3||fw==5?.1875f:fw==4?.5625f:0, fl==4||fw==3?.8125f:fw==2?.4375f:1, 1, fl==2||fw==4?.8125f:fw==5?.4375f:1);
						addCollisionBox(world, x, y, z, aabb, list, ent);
						this.setBlockBounds(fl==5||fw==2?.1875f:fl==4?.5625f:0, 0, fl==3||fw==5?.1875f:fl==2?.5625f:0, fl==4||fw==3?.8125f:fl==5?.4375f:1, 1, fl==2||fw==4?.8125f:fl==3?.4375f:1);
					}
				}

			}
		}
		addCollisionBox(world, x, y, z, aabb, list, ent);
	}
	@Override
	public ArrayList<AxisAlignedBB> addCustomSelectionBoxesToList(World world, int x, int y, int z)
	{
		ArrayList<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if(tileEntity instanceof TileEntityCrusher)
		{
			TileEntityCrusher tile = (TileEntityCrusher)tileEntity;
			int pos = tile.pos;
			if(pos%15>=11&&pos%15<=13)
			{		
				int fl = tile.facing;
				int fw = tile.facing;
				if(tile.mirrored)
					fw = ForgeDirection.OPPOSITES[fw];
				if(pos/15==0 && (pos%5==1||pos%5==3))
				{
					if(pos%5==1)
					{	
						list.add(AxisAlignedBB.getBoundingBox(fl==4?.4375f:fw==3?.1875f:fw==2?.5625f:0, 0, fl==2?.4375f:fw==4?.1875f:fw==5?.5625f:0, fl==5?.5625f:fw==2?.8125f:fw==3?.4375f:1, 1, fl==3?.5625f:fw==5?.8125f:fw==4?.4375f:1));
						list.add(AxisAlignedBB.getBoundingBox(fl==4||fw==3?.1875f:fl==5?.5625f:0, 0, fl==2||fw==4?.1875f:fl==3?.5625f:0, fl==5||fw==2?.8125f:fl==4?.4375f:1, 1, fl==3||fw==5?.8125f:fl==2?.4375f:1));
					}
					else
					{
						list.add(AxisAlignedBB.getBoundingBox(fl==4?.4375f:fw==2?.1875f:fw==3?.5625f:0, 0, fl==2?.4375f:fw==5?.1875f:fw==4?.5625f:0, fl==5?.5625f:fw==3?.8125f:fw==2?.4375f:1, 1, fl==3?.5625f:fw==4?.8125f:fw==5?.4375f:1));
						list.add(AxisAlignedBB.getBoundingBox(fl==4||fw==2?.1875f:fl==5?.5625f:0, 0, fl==2||fw==5?.1875f:fl==3?.5625f:0, fl==5||fw==3?.8125f:fl==4?.4375f:1, 1, fl==3||fw==4?.8125f:fl==2?.4375f:1));
					}
				}
				else if(pos/15==2 && (pos%5==1||pos%5==3))
				{
					if(pos%5==1)
					{
						list.add(AxisAlignedBB.getBoundingBox(fl==5?.4375f:fw==3?.1875f:fw==2?.5625f:0, 0, fl==3?.4375f:fw==4?.1875f:fw==5?.5625f:0, fl==4?.5625f:fw==2?.8125f:fw==3?.4375f:1, 1, fl==2?.5625f:fw==5?.8125f:fw==4?.4375f:1));
						list.add(AxisAlignedBB.getBoundingBox(fl==5||fw==3?.1875f:fl==4?.5625f:0, 0, fl==3||fw==4?.1875f:fl==2?.5625f:0, fl==4||fw==2?.8125f:fl==5?.4375f:1, 1, fl==2||fw==5?.8125f:fl==3?.4375f:1));
					}
					else
					{
						list.add(AxisAlignedBB.getBoundingBox(fl==5?.4375f:fw==2?.1875f:fw==3?.5625f:0, 0, fl==3?.4375f:fw==5?.1875f:fw==4?.5625f:0, fl==4?.5625f:fw==3?.8125f:fw==2?.4375f:1, 1, fl==2?.5625f:fw==4?.8125f:fw==5?.4375f:1));
						list.add(AxisAlignedBB.getBoundingBox(fl==5||fw==2?.1875f:fl==4?.5625f:0, 0, fl==3||fw==5?.1875f:fl==2?.5625f:0, fl==4||fw==3?.8125f:fl==5?.4375f:1, 1, fl==2||fw==4?.8125f:fl==3?.4375f:1));
					}
				}
			}
			else if(pos==1 || pos==3 || pos==31 || pos==33)
			{
				int fl = tile.facing;
				int fw = tile.facing;
				if(tile.mirrored)
					fw = ForgeDirection.OPPOSITES[fw];
				list.add(AxisAlignedBB.getBoundingBox(0,0,0, 1,.5,1));
				list.add(AxisAlignedBB.getBoundingBox(fw==(pos%30==1?2:3)||fl==(pos<30?5:4)?.5:.25,  .5,  fl==(pos<30?3:2)||fw==(pos%30==1?5:4)?.5:.25,    fw==(pos%30==1?2:3)||fl==(pos<30?5:4)?.75:.5,  1.5,  fl==(pos<30?3:2)||fw==(pos%30==1?5:4)?.75:.5));
			}
			else
			{
				float[] bounds = ((TileEntityMultiblockPart)tileEntity).getBlockBounds();
				if(bounds!=null && bounds.length>5)
					list.add(AxisAlignedBB.getBoundingBox(bounds[0],bounds[1],bounds[2], bounds[3],bounds[4],bounds[5]));
				else
					list.add(AxisAlignedBB.getBoundingBox(0,0,0, 1,1,1));
			}
		}
		return list;
	}
	@Override
	public boolean addSpecifiedSubBox(World world, int x, int y, int z, EntityPlayer player, AxisAlignedBB box, Vec3 hitVec, ArrayList<AxisAlignedBB> list)
	{
		return false;
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
		this.setBlockBoundsBasedOnState(world,x,y,z);
		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		switch(meta)
		{
		case META_lightningRod:
			return new TileEntityLightningRod();
		case META_dieselGenerator:
			return new TileEntityDieselGenerator();
		case META_squeezer:
			return new TileEntitySqueezer();
		case META_fermenter:
			return new TileEntityFermenter();
		case META_refinery:
			return new TileEntityRefinery();
		case META_crusher:
			return new TileEntityCrusher();
		case META_bucketWheel:
			return new TileEntityBucketWheel();
		case META_excavator:
			return new TileEntityExcavator();
		case META_arcFurnace:
			return new TileEntityArcFurnace();
		case META_tank:
			return new TileEntitySheetmetalTank();
		case META_silo:
			return new TileEntitySilo();
		case META_assembler:
			return new TileEntityAssembler();
		}
		return null;
	}
	@Override
	public boolean hasComparatorInputOverride() {
		return true;
	}
	@Override
	public int getComparatorInputOverride(World world, int x,
			int y, int z, int side) {
		TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof TileEntitySilo)
		{
			return ((TileEntitySilo)te).getComparatorOutput();
		}
		else if (te instanceof TileEntitySheetmetalTank)
		{
			return ((TileEntitySheetmetalTank)te).getComparatorOutput();
		}
		return super.getComparatorInputOverride(world, x, y,
				z, side);
	}
}