/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.BlockIEMultiblock;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.Properties;

public class BlockMetalMultiblocks extends BlockIEMultiblock<BlockTypes_MetalMultiblock>
{
	public BlockMetalMultiblocks()
	{
		super("metal_multiblock", Material.IRON, PropertyEnum.create("type", BlockTypes_MetalMultiblock.class), ItemBlockIEBase.class, IEProperties.DYNAMICRENDER, IEProperties.BOOLEANS[0], Properties.AnimationProperty, IEProperties.OBJ_TEXTURE_REMAP);
		setHardness(3.0F);
		setResistance(15.0F);
		this.setMetaBlockLayer(BlockTypes_MetalMultiblock.TANK.getMeta(), BlockRenderLayer.CUTOUT);
		this.setMetaBlockLayer(BlockTypes_MetalMultiblock.DIESEL_GENERATOR.getMeta(), BlockRenderLayer.CUTOUT);
		this.setMetaBlockLayer(BlockTypes_MetalMultiblock.BOTTLING_MACHINE.getMeta(), BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT);
		this.setAllNotNormalBlock();
		lightOpacity = 0;
	}

	@Override
	public boolean useCustomStateMapper()
	{
		return true;
	}

	@Override
	public String getCustomStateMapping(int meta, boolean itemBlock)
	{
		if(BlockTypes_MetalMultiblock.values()[meta].needsCustomState())
			return BlockTypes_MetalMultiblock.values()[meta].getCustomState();
		return null;
	}

	@Override
	public EnumPushReaction getPushReaction(IBlockState state)
	{
		return EnumPushReaction.BLOCK;
	}

	@Override
	public TileEntity createBasicTE(World world, BlockTypes_MetalMultiblock type)
	{
		switch(type)
		{
			case METAL_PRESS:
				return new TileEntityMetalPress();
			case CRUSHER:
				return new TileEntityCrusher();
			case TANK:
				return new TileEntitySheetmetalTank();
			case SILO:
				return new TileEntitySilo();
			case ASSEMBLER:
				return new TileEntityAssembler();
			case AUTO_WORKBENCH:
				return new TileEntityAutoWorkbench();
			case BOTTLING_MACHINE:
				return new TileEntityBottlingMachine();
			case SQUEEZER:
				return new TileEntitySqueezer();
			case FERMENTER:
				return new TileEntityFermenter();
			case REFINERY:
				return new TileEntityRefinery();
			case DIESEL_GENERATOR:
				return new TileEntityDieselGenerator();
			case EXCAVATOR:
				return new TileEntityExcavator();
			case BUCKET_WHEEL:
				return new TileEntityBucketWheel();
			case ARC_FURNACE:
				return new TileEntityArcFurnace();
			case LIGHTNINGROD:
				return new TileEntityLightningrod();
			case MIXER:
				return new TileEntityMixer();
		}
		return null;
	}


	//	@Override
	//	public void breakBlock(World world, int x, int y, int z, Block par5, int par6)
	//	{
	//		TileEntity tileEntity = world.getTileEntity(x, y, z);
	//		if(tileEntity instanceof TileEntityMultiblockPart && world.getGameRules().getGameRuleBooleanValue("doTileDrops"))
	//		{
	//			TileEntityMultiblockPart tile = (TileEntityMultiblockPart)tileEntity;
	//			if(!tile.formed && tile.pos==-1 && tile.getOriginalBlock()!=null)
	//				world.spawnEntity(new EntityItem(world, x+.5,y+.5,z+.5, tile.getOriginalBlock().copy()));
	//
	//			if(tileEntity instanceof IInventory)
	//			{
	//				if(!world.isRemote && ((TileEntityMultiblockPart)tileEntity).formed)
	//				{
	//					TileEntity master = ((TileEntityMultiblockPart)tileEntity).master();
	//					if(master==null)
	//						master = tileEntity;
	//					for(int i=0; i<((IInventory)master).getSizeInventory(); i++)
	//					{
	//						ItemStack stack = ((IInventory)master).getStackInSlot(i);
	//						if(stack!=null)
	//						{
	//							float fx = Utils.RAND.nextFloat() * 0.8F + 0.1F;
	//							float fz = Utils.RAND.nextFloat() * 0.8F + 0.1F;
	//
	//							EntityItem entityitem = new EntityItem(world, x+fx, y+.5, z+fz, stack);
	//							entityitem.motionX = Utils.RAND.nextGaussian()*.05;
	//							entityitem.motionY = Utils.RAND.nextGaussian()*.05+.2;
	//							entityitem.motionZ = Utils.RAND.nextGaussian()*.05;
	//							if(stack.hasTagCompound())
	//								entityitem.getEntityItem().setTagCompound((NBTTagCompound)stack.getTagCompound().copy());
	//							world.spawnEntity(entityitem);
	//						}
	//					}
	//				}
	//			}
	//		}
	//		super.breakBlock(world, x, y, z, par5, par6);
	//	}
	//	@Override
	//	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
	//	{
	//		return new ArrayList<ItemStack>();
	//	}
	//	@Override
	//	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player)
	//	{
	//		return getOriginalBlock(world, x, y, z);
	//	}

	//	public ItemStack getOriginalBlock(World world, int x, int y, int z)
	//	{
	//		TileEntity te = world.getTileEntity(x, y, z);
	//		if(te instanceof TileEntityMultiblockPart)
	//			return ((TileEntityMultiblockPart)te).getOriginalBlock();
	//		return new ItemStack(this, 1, world.getBlockMetadata(x, y, z));
	//	}

	//	@Override
	//	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	//	{
	//		TileEntity curr = world.getTileEntity(x, y, z);
	//		if(curr instanceof TileEntitySqueezer)
	//		{
	//			if(!player.isSneaking() && ((TileEntitySqueezer)curr).formed )
	//			{
	//				TileEntitySqueezer te = ((TileEntitySqueezer)curr).master();
	//				if(te==null)
	//					te = ((TileEntitySqueezer)curr);
	//				if(!world.isRemote)
	//					player.openGui(ImmersiveEngineering.instance, Lib.GUIID_Squeezer, world, te.xCoord, te.yCoord, te.zCoord);
	//				return true;
	//			}
	//		}
	//		else if(curr instanceof TileEntityFermenter)
	//		{
	//			if(!player.isSneaking() && ((TileEntityFermenter)curr).formed )
	//			{
	//				TileEntityFermenter te = ((TileEntityFermenter)curr).master();
	//				if(te==null)
	//					te = ((TileEntityFermenter)curr);
	//				if(!world.isRemote)
	//					player.openGui(ImmersiveEngineering.instance, Lib.GUIID_Fermenter, world, te.xCoord, te.yCoord, te.zCoord);
	//				return true;
	//			}
	//		}
	//		else if(curr instanceof TileEntityRefinery)
	//		{
	//			if(!player.isSneaking() && ((TileEntityRefinery)curr).formed )
	//			{
	//				TileEntityRefinery te = ((TileEntityRefinery)curr).master();
	//				if(te==null)
	//					te = ((TileEntityRefinery)curr);
	//				if(!world.isRemote)
	//					player.openGui(ImmersiveEngineering.instance, Lib.GUIID_Refinery, world, te.xCoord, te.yCoord, te.zCoord);
	//				return true;
	//			}
	//		}
	//		else if(curr instanceof TileEntityDieselGenerator)
	//		{
	//			TileEntityDieselGenerator master = ((TileEntityDieselGenerator)curr).master();
	//			if(master==null)
	//				master = ((TileEntityDieselGenerator)curr);
	//			if(((TileEntityDieselGenerator)curr).pos==40 && Utils.isHammer(player.getCurrentEquippedItem()))
	//			{
	//				master.mirrored = !master.mirrored;
	//				master.markDirty();
	//				world.markBlockForUpdate(master.xCoord, master.yCoord, master.zCoord);
	//			}
	//			else if(!world.isRemote && (((TileEntityDieselGenerator)curr).pos==36 || ((TileEntityDieselGenerator)curr).pos==38))
	//			{
	//				if(Utils.fillFluidHandlerWithPlayerItem(world, master, player))
	//				{
	//					master.markDirty();
	//					world.markBlockForUpdate(master.xCoord,master.yCoord,master.zCoord);
	//					return true;
	//				}
	//				if(player.getCurrentEquippedItem()!=null && player.getCurrentEquippedItem().getItem() instanceof IFluidContainerItem)
	//				{
	//					master.markDirty();
	//					world.markBlockForUpdate(master.xCoord,master.yCoord,master.zCoord);
	//					return true;
	//				}
	//			}
	//		}
	//		else if(curr instanceof TileEntityArcFurnace)
	//		{
	//			if(!player.isSneaking() && ((TileEntityArcFurnace)curr).formed )
	//			{
	//				TileEntityArcFurnace te = ((TileEntityArcFurnace)curr);
	//				if(te.pos==2||te.pos==25|| (te.pos>25 && te.pos%5>0 && te.pos%5<4 && te.pos%25/5<4))
	//				{
	//					TileEntityArcFurnace master = te.master();
	//					if(master==null)
	//						master = te;
	//					if(!world.isRemote)
	//						player.openGui(ImmersiveEngineering.instance, Lib.GUIID_ArcFurnace, world, master.xCoord, master.yCoord, master.zCoord);
	//					return true;
	//				}
	//			}
	//		}
	//		else if(!player.isSneaking() && curr instanceof TileEntitySheetmetalTank)
	//		{
	//			TileEntitySheetmetalTank tank = (TileEntitySheetmetalTank)curr;
	//			TileEntitySheetmetalTank master = tank.master();
	//			if(master==null)
	//				master = tank;
	//			if(Utils.fillFluidHandlerWithPlayerItem(world, master, player))
	//			{
	//				master.markDirty();
	//				world.markBlockForUpdate(master.xCoord,master.yCoord,master.zCoord);
	//				return true;
	//			}
	//			if(Utils.fillPlayerItemFromFluidHandler(world, master, player, master.tank.getFluid()))
	//			{
	//				master.markDirty();
	//				world.markBlockForUpdate(master.xCoord,master.yCoord,master.zCoord);
	//				return true;
	//			}
	//			if(player.getCurrentEquippedItem()!=null && player.getCurrentEquippedItem().getItem() instanceof IFluidContainerItem)
	//			{
	//				master.markDirty();
	//				world.markBlockForUpdate(master.xCoord,master.yCoord,master.zCoord);
	//				return true;
	//			}
	//		}
	//		else if(curr instanceof TileEntityAssembler)
	//		{
	//			if(!player.isSneaking() && ((TileEntityAssembler)curr).formed)
	//			{
	//				TileEntityAssembler te = ((TileEntityAssembler)curr);
	//				TileEntityAssembler master = te.master();
	//				if(master==null)
	//					master = te;
	//				if(!world.isRemote)
	//					player.openGui(ImmersiveEngineering.instance, Lib.GUIID_Assembler, world, master.xCoord, master.yCoord, master.zCoord);
	//				return true;
	//			}
	//		}
	//		return false;
	//	}


	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityMultiblockPart)
		{
			TileEntityMultiblockPart tile = (TileEntityMultiblockPart)te;
			if(tile instanceof TileEntityMultiblockMetal&&((TileEntityMultiblockMetal)tile).isRedstonePos())
				return true;
			if(te instanceof TileEntityMetalPress)
			{
				return tile.pos < 3||(tile.pos==7&&side==EnumFacing.UP);
			}
			else if(te instanceof TileEntityCrusher)
			{
				return tile.pos%5==0||tile.pos==2||tile.pos==9||(tile.pos==19&&side.getOpposite()==tile.facing);
			}
			else if(te instanceof TileEntitySheetmetalTank)
			{
				return tile.pos==4||tile.pos==40||(tile.pos >= 18&&tile.pos < 36);
			}
			else if(te instanceof TileEntitySilo)
			{
				return tile.pos==4||tile.pos==58||(tile.pos >= 18&&tile.pos < 54);
			}
			else if(te instanceof TileEntitySqueezer||te instanceof TileEntityFermenter)
			{
				return tile.pos==0||tile.pos==9||tile.pos==5||(tile.pos==11&&side.getOpposite()==tile.facing);
			}
			else if(te instanceof TileEntityRefinery)
			{
				return tile.pos==2||tile.pos==5||tile.pos==9||(tile.pos==19&&side.getOpposite()==tile.facing)||(tile.pos==27&&side==tile.facing);
			}
			else if(te instanceof TileEntityDieselGenerator)
			{
				if(tile.pos==0||tile.pos==2)
					return side.getAxis()==tile.facing.rotateY().getAxis();
				else if(tile.pos >= 15&&tile.pos <= 17)
					return side==EnumFacing.UP;
				else if(tile.pos==23)
					return side==(tile.mirrored?tile.facing.rotateYCCW(): tile.facing.rotateY());
			}
			else if(te instanceof TileEntityExcavator)
			{
				if(tile.pos%18 < 9||(tile.pos >= 18&&tile.pos < 36))
					return true;
			}
			else if(te instanceof TileEntityArcFurnace)
			{
				if(tile.pos==2||tile.pos==25||tile.pos==52)
					return side.getOpposite()==tile.facing||(tile.pos==52&&side==EnumFacing.UP);
				if(tile.pos==82||tile.pos==86||tile.pos==88||tile.pos==112)
					return side==EnumFacing.UP;
				if((tile.pos >= 21&&tile.pos <= 23)||(tile.pos >= 46&&tile.pos <= 48)||(tile.pos >= 71&&tile.pos <= 73))
					return side==tile.facing;
			}
		}
		//		if(te instanceof TileEntityRefinery)
		//		{
		//			TileEntityRefinery tile = (TileEntityRefinery)te;
		//			if(tile.pos==9 && side.ordinal()==tile.facing)
		//				return true;
		//		}
		return super.isSideSolid(state, world, pos, side);
	}

	//	@Override
	//	public boolean canConnectRedstone(IBlockAccess world, BlockPos pos, EnumFacing side)
	//	{
	//		TileEntity te = world.getTileEntity(x, y, z);
	//		if(te instanceof TileEntityDieselGenerator)
	//			return ((TileEntityDieselGenerator)te).pos==21 || ((TileEntityDieselGenerator)te).pos==23;
	//		if(te instanceof TileEntityRefinery)
	//			return ((TileEntityRefinery)te).pos==9 && side==((TileEntityRefinery)te).facing;
	//		if(te instanceof TileEntityCrusher)
	//			return ((TileEntityCrusher)te).pos==9 && side==((TileEntityCrusher)te).facing;
	//		if(te instanceof TileEntityExcavator)
	//			return ((TileEntityExcavator)te).pos==3 || ((TileEntityExcavator)te).pos==5;
	//		if(te instanceof TileEntityArcFurnace)
	//			return ((TileEntityArcFurnace)te).pos==25;
	//		return false;
	//	}

	//	@Override
	//	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	//	{
	//		TileEntity te = world.getTileEntity(x, y, z);
	//		if(te instanceof TileEntityMultiblockPart)
	//		{
	//			float[] bounds = ((TileEntityMultiblockPart)te).getBlockBounds();
	//			if(bounds!=null && bounds.length>5)
	//				this.setBlockBounds(bounds[0],bounds[1],bounds[2], bounds[3],bounds[4],bounds[5]);
	//			else
	//				this.setBlockBounds(0,0,0,1,1,1);
	//		}
	//		else
	//			this.setBlockBounds(0,0,0,1,1,1);
	//	}
	//	@Override
	//	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB aabb, List list, Entity ent)
	//	{
	//		this.setBlockBoundsBasedOnState(world, x, y, z);
	//		TileEntity tileEntity = world.getTileEntity(x, y, z);
	//		if(tileEntity instanceof TileEntityCrusher)
	//		{
	//			TileEntityCrusher tile = (TileEntityCrusher)tileEntity;
	//			if(tile.pos%15>=11&&tile.pos%15<=13)
	//			{	
	//				int pos = tile.pos;
	//				int fl = tile.facing;
	//				int fw = tile.facing;
	//				if(tile.mirrored)
	//					fw = ForgeDirection.OPPOSITES[fw];
	//				if(pos/15==0 && (pos%5==1||pos%5==3))
	//				{
	//					if(pos%5==1)
	//					{
	//						this.setBlockBounds(fl==4||fw==3?.1875f:fw==2?.5625f:0, 0, fl==2||fw==4?.1875f:fw==5?.5625f:0, fl==5||fw==2?.8125f:fw==3?.4375f:1, 1, fl==3||fw==5?.8125f:fw==4?.4375f:1);
	//						addCollisionBox(world, x, y, z, aabb, list, ent);
	//						this.setBlockBounds(fl==4||fw==3?.1875f:fl==5?.5625f:0, 0, fl==2||fw==4?.1875f:fl==3?.5625f:0, fl==5||fw==2?.8125f:fl==4?.4375f:1, 1, fl==3||fw==5?.8125f:fl==2?.4375f:1);
	//					}
	//					else
	//					{
	//						this.setBlockBounds(fl==4||fw==2?.1875f:fw==3?.5625f:0, 0, fl==2||fw==5?.1875f:fw==4?.5625f:0, fl==5||fw==3?.8125f:fw==2?.4375f:1, 1, fl==3||fw==4?.8125f:fw==5?.4375f:1);
	//						super.addCollisionBoxesToList(world, x, y, z, aabb, list, ent);
	//						this.setBlockBounds(fl==4||fw==2?.1875f:fl==5?.5625f:0, 0, fl==2||fw==5?.1875f:fl==3?.5625f:0, fl==5||fw==3?.8125f:fl==4?.4375f:1, 1, fl==3||fw==4?.8125f:fl==2?.4375f:1);
	//					}
	//				}
	//				else if(pos/15==2 && (pos%5==1||pos%5==3))
	//				{
	//					if(pos%5==1)
	//					{
	//						this.setBlockBounds(fl==5||fw==3?.1875f:fw==2?.5625f:0, 0, fl==3||fw==4?.1875f:fw==5?.5625f:0, fl==4||fw==2?.8125f:fw==3?.4375f:1, 1, fl==2||fw==5?.8125f:fw==4?.4375f:1);
	//						addCollisionBox(world, x, y, z, aabb, list, ent);
	//						this.setBlockBounds(fl==5||fw==3?.1875f:fl==4?.5625f:0, 0, fl==3||fw==4?.1875f:fl==2?.5625f:0, fl==4||fw==2?.8125f:fl==5?.4375f:1, 1, fl==2||fw==5?.8125f:fl==3?.4375f:1);
	//					}
	//					else
	//					{
	//						this.setBlockBounds(fl==5||fw==2?.1875f:fw==3?.5625f:0, 0, fl==3||fw==5?.1875f:fw==4?.5625f:0, fl==4||fw==3?.8125f:fw==2?.4375f:1, 1, fl==2||fw==4?.8125f:fw==5?.4375f:1);
	//						addCollisionBox(world, x, y, z, aabb, list, ent);
	//						this.setBlockBounds(fl==5||fw==2?.1875f:fl==4?.5625f:0, 0, fl==3||fw==5?.1875f:fl==2?.5625f:0, fl==4||fw==3?.8125f:fl==5?.4375f:1, 1, fl==2||fw==4?.8125f:fl==3?.4375f:1);
	//					}
	//				}
	//
	//			}
	//		}
	//		addCollisionBox(world, x, y, z, aabb, list, ent);
	//	}
	//	@Override
	//	public ArrayList<AxisAlignedBB> addCustomSelectionBoxesToList(World world, int x, int y, int z)
	//	{
	//		ArrayList<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
	//		TileEntity tileEntity = world.getTileEntity(x, y, z);
	//		if(tileEntity instanceof TileEntityCrusher)
	//		{
	//			TileEntityCrusher tile = (TileEntityCrusher)tileEntity;
	//			int pos = tile.pos;
	//			if(pos%15>=11&&pos%15<=13)
	//			{		
	//				int fl = tile.facing;
	//				int fw = tile.facing;
	//				if(tile.mirrored)
	//					fw = ForgeDirection.OPPOSITES[fw];
	//				if(pos/15==0 && (pos%5==1||pos%5==3))
	//				{
	//					if(pos%5==1)
	//					{	
	//						list.add(AxisAlignedBB.getBoundingBox(fl==4?.4375f:fw==3?.1875f:fw==2?.5625f:0, 0, fl==2?.4375f:fw==4?.1875f:fw==5?.5625f:0, fl==5?.5625f:fw==2?.8125f:fw==3?.4375f:1, 1, fl==3?.5625f:fw==5?.8125f:fw==4?.4375f:1));
	//						list.add(AxisAlignedBB.getBoundingBox(fl==4||fw==3?.1875f:fl==5?.5625f:0, 0, fl==2||fw==4?.1875f:fl==3?.5625f:0, fl==5||fw==2?.8125f:fl==4?.4375f:1, 1, fl==3||fw==5?.8125f:fl==2?.4375f:1));
	//					}
	//					else
	//					{
	//						list.add(AxisAlignedBB.getBoundingBox(fl==4?.4375f:fw==2?.1875f:fw==3?.5625f:0, 0, fl==2?.4375f:fw==5?.1875f:fw==4?.5625f:0, fl==5?.5625f:fw==3?.8125f:fw==2?.4375f:1, 1, fl==3?.5625f:fw==4?.8125f:fw==5?.4375f:1));
	//						list.add(AxisAlignedBB.getBoundingBox(fl==4||fw==2?.1875f:fl==5?.5625f:0, 0, fl==2||fw==5?.1875f:fl==3?.5625f:0, fl==5||fw==3?.8125f:fl==4?.4375f:1, 1, fl==3||fw==4?.8125f:fl==2?.4375f:1));
	//					}
	//				}
	//				else if(pos/15==2 && (pos%5==1||pos%5==3))
	//				{
	//					if(pos%5==1)
	//					{
	//						list.add(AxisAlignedBB.getBoundingBox(fl==5?.4375f:fw==3?.1875f:fw==2?.5625f:0, 0, fl==3?.4375f:fw==4?.1875f:fw==5?.5625f:0, fl==4?.5625f:fw==2?.8125f:fw==3?.4375f:1, 1, fl==2?.5625f:fw==5?.8125f:fw==4?.4375f:1));
	//						list.add(AxisAlignedBB.getBoundingBox(fl==5||fw==3?.1875f:fl==4?.5625f:0, 0, fl==3||fw==4?.1875f:fl==2?.5625f:0, fl==4||fw==2?.8125f:fl==5?.4375f:1, 1, fl==2||fw==5?.8125f:fl==3?.4375f:1));
	//					}
	//					else
	//					{
	//						list.add(AxisAlignedBB.getBoundingBox(fl==5?.4375f:fw==2?.1875f:fw==3?.5625f:0, 0, fl==3?.4375f:fw==5?.1875f:fw==4?.5625f:0, fl==4?.5625f:fw==3?.8125f:fw==2?.4375f:1, 1, fl==2?.5625f:fw==4?.8125f:fw==5?.4375f:1));
	//						list.add(AxisAlignedBB.getBoundingBox(fl==5||fw==2?.1875f:fl==4?.5625f:0, 0, fl==3||fw==5?.1875f:fl==2?.5625f:0, fl==4||fw==3?.8125f:fl==5?.4375f:1, 1, fl==2||fw==4?.8125f:fl==3?.4375f:1));
	//					}
	//				}
	//			}
	//			else if(pos==1 || pos==3 || pos==31 || pos==33)
	//			{
	//				int fl = tile.facing;
	//				int fw = tile.facing;
	//				if(tile.mirrored)
	//					fw = ForgeDirection.OPPOSITES[fw];
	//				list.add(AxisAlignedBB.getBoundingBox(0,0,0, 1,.5,1));
	//				list.add(AxisAlignedBB.getBoundingBox(fw==(pos%30==1?2:3)||fl==(pos<30?5:4)?.5:.25,  .5,  fl==(pos<30?3:2)||fw==(pos%30==1?5:4)?.5:.25,    fw==(pos%30==1?2:3)||fl==(pos<30?5:4)?.75:.5,  1.5,  fl==(pos<30?3:2)||fw==(pos%30==1?5:4)?.75:.5));
	//			}
	//			else
	//			{
	//				float[] bounds = ((TileEntityMultiblockPart)tileEntity).getBlockBounds();
	//				if(bounds!=null && bounds.length>5)
	//					list.add(AxisAlignedBB.getBoundingBox(bounds[0],bounds[1],bounds[2], bounds[3],bounds[4],bounds[5]));
	//				else
	//					list.add(AxisAlignedBB.getBoundingBox(0,0,0, 1,1,1));
	//			}
	//		}
	//		return list;
	//	}
	//	@Override
	//	public boolean addSpecifiedSubBox(World world, int x, int y, int z, EntityPlayer player, AxisAlignedBB box, Vec3 hitVec, ArrayList<AxisAlignedBB> list)
	//	{
	//		return false;
	//	}
	//
	//
	//	@Override
	//	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
	//	{
	//		this.setBlockBoundsBasedOnState(world,x,y,z);
	//		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	//	}
	//	@Override
	//	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
	//	{
	//		this.setBlockBoundsBasedOnState(world,x,y,z);
	//		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	//	}


	//	@Override
	//	public boolean hasComparatorInputOverride() {
	//		return true;
	//	}
	//	@Override
	//	public int getComparatorInputOverride(World world, int x,
	//			int y, int z, int side) {
	//		TileEntity te = world.getTileEntity(x, y, z);
	//		if (te instanceof TileEntitySilo)
	//		{
	//			return ((TileEntitySilo)te).getComparatorOutput();
	//		}
	//		else if (te instanceof TileEntitySheetmetalTank)
	//		{
	//			return ((TileEntitySheetmetalTank)te).getComparatorOutput();
	//		}
	//		return super.getComparatorInputOverride(world, x, y,
	//				z, side);
	//	}

	@Override
	public boolean allowHammerHarvest(IBlockState state)
	{
		return true;
	}
}