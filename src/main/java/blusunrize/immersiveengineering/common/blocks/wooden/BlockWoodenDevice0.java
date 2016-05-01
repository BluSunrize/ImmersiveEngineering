package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.BlockIETileProvider;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import blusunrize.immersiveengineering.common.entities.EntityIEExplosive;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class BlockWoodenDevice0 extends BlockIETileProvider
{
	public BlockWoodenDevice0()
	{
		super("woodenDevice0",Material.wood, PropertyEnum.create("type", BlockTypes_WoodenDevice0.class), ItemBlockIEBase.class, IEProperties.FACING_HORIZONTAL, IEProperties.SIDECONFIG[0], IEProperties.SIDECONFIG[1], IEProperties.MULTIBLOCKSLAVE);
		this.setHardness(2.0F);
		this.setResistance(5.0F);
	}

	@Override
	public boolean isFullBlock()
	{
		return false;
	}
	@Override
	public boolean isFullCube()
	{
		return false;
	}
	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	public int getExplosivesType(IBlockState state)
	{
		if(!state.getPropertyNames().contains(this.property))
			return -1;
		if(state.getValue(this.property)==BlockTypes_WoodenDevice0.GUNPOWDER_BARREL)
			return 0;
		return -1;
	}
	public void doExplosion(World world, BlockPos pos, IBlockState state, EntityLivingBase igniter, int explosivesType)
	{
		if(!world.isRemote)
		{
			if(explosivesType==0)
			{
				EntityIEExplosive explosive = new EntityIEExplosive(world, pos, igniter, state, 4).setDropChance(1);
				world.spawnEntityInWorld(explosive);
				world.playSoundAtEntity(explosive, "game.tnt.primed", 1.0F, 1.0F);
				world.setBlockToAir(pos);
			}
		}
	}

	@Override
	public boolean canIEBlockBePlaced(World world, BlockPos pos, IBlockState newState, EnumFacing side, float hitX, float hitY, float hitZ, EntityPlayer player, ItemStack stack)
	{
		if(stack.getItemDamage()==BlockTypes_WoodenDevice0.WORKBENCH.getMeta())
		{
			EnumFacing f = EnumFacing.fromAngle(player.rotationYaw);
			if(f.getAxis()==Axis.Z)
			{
				if(!world.getBlockState(pos.add(1,0,0)).getBlock().isReplaceable(world,pos.add(1,0,0)) && !world.getBlockState(pos.add(-1,0,0)).getBlock().isReplaceable(world,pos.add(-1,0,0)))
					return false;
			}
			else
			{
				if(!world.getBlockState(pos.add(0,0,1)).getBlock().isReplaceable(world,pos.add(0,0,1)) && !world.getBlockState(pos.add(0,0,-1)).getBlock().isReplaceable(world,pos.add(0,0,-1)))
					return false;
			}
		}
		return true;
	}


	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		int explosivesType = this.getExplosivesType(state);
		if(explosivesType>=0 && player.getCurrentEquippedItem()!=null)
		{
			Item item = player.getCurrentEquippedItem().getItem();

			if(item==Items.flint_and_steel||item==Items.fire_charge)
			{
				this.doExplosion(world, pos, state, player, explosivesType);
				if(item==Items.flint_and_steel)
					player.getCurrentEquippedItem().damageItem(1, player);
				else if (!player.capabilities.isCreativeMode)
					--player.getCurrentEquippedItem().stackSize;
				return true;
			}
		}
		return super.onBlockActivated(world, pos, state, player, side, hitX, hitY, hitZ);
	}
	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block block)
	{
		super.onNeighborBlockChange(world, pos, state, block);
		int explosivesType = this.getExplosivesType(state);
		if(explosivesType>=0 && world.isBlockPowered(pos))
			this.doExplosion(world, pos, state, null, explosivesType);
	}
	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state)
	{
		super.onBlockAdded(world, pos, state);
		int explosivesType = this.getExplosivesType(state);
		if(explosivesType>=0 && world.isBlockPowered(pos))
			this.doExplosion(world, pos, state, null, explosivesType);
	}
	@Override
	public void onBlockExploded(World world, BlockPos pos, Explosion explosionIn)
	{
		int explosivesType = this.getExplosivesType(world.getBlockState(pos));
		if(explosivesType>=0)
			this.doExplosion(world, pos, world.getBlockState(pos), null, explosivesType);
		super.onBlockExploded(world, pos, explosionIn);
	}
	@Override
	public void onBlockDestroyedByPlayer(World world, BlockPos pos, IBlockState state)
	{
		super.onBlockDestroyedByPlayer(world, pos, state);
		int explosivesType = this.getExplosivesType(state);
		if(explosivesType>=0)
			this.doExplosion(world, pos, state, null, explosivesType);
	}
	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity)
	{
		super.onEntityCollidedWithBlock(world, pos, state, entity);
		int explosivesType = this.getExplosivesType(state);
		if(!world.isRemote && entity instanceof EntityArrow && ((EntityArrow)entity).isBurning() && explosivesType>=0)
			this.doExplosion(world, pos, state, ((EntityArrow)entity).shootingEntity instanceof EntityLivingBase?(EntityLivingBase)((EntityArrow)entity).shootingEntity:null, explosivesType);
	}

	//	@Override
	//	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player)
	//	{
	//		TileEntity te = world.getTileEntity(x, y, z);
	//		if(te instanceof TileEntityWoodenBarrel)
	//		{
	//			ItemStack stack = new ItemStack(this,1,world.getBlockMetadata(x, y, z));
	//			NBTTagCompound tag = new NBTTagCompound();
	//			((TileEntityWoodenBarrel) te).writeTank(tag, true);
	//			if(!tag.hasNoTags())
	//				stack.setTagCompound(tag);
	//			return stack;
	//		}
	//		return super.getPickBlock(target, world, x, y, z, player);
	//	}

	//	@Override
	//	public void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player)
	//	{
	//		TileEntity te = world.getTileEntity(x, y, z);
	//		if(!world.isRemote)
	//		{
	//			if(te instanceof TileEntityWoodenCrate)
	//			{
	//				ItemStack stack = new ItemStack(this, 1, meta);
	//				NBTTagCompound tag = new NBTTagCompound();
	//				((TileEntityWoodenCrate) te).writeInv(tag, true);
	//				if(!tag.hasNoTags())
	//					stack.setTagCompound(tag);
	//				world.spawnEntityInWorld(new EntityItem(world, x+.5, y+.5, z+.5, stack));
	//			}
	//
	//			if(te instanceof TileEntityWoodenBarrel)
	//			{
	//				ItemStack stack = new ItemStack(this, 1, meta);
	//				NBTTagCompound tag = new NBTTagCompound();
	//				((TileEntityWoodenBarrel) te).writeTank(tag, true);
	//				if(!tag.hasNoTags())
	//					stack.setTagCompound(tag);
	//				world.spawnEntityInWorld(new EntityItem(world, x+.5, y+.5, z+.5, stack));
	//			}
	//		}
	//	}

	//	@Override
	//	public void onBlockExploded(World world, int x, int y, int z, Explosion explosion)
	//	{
	//		if(!world.isRemote)
	//		{
	//			TileEntity te = world.getTileEntity(x, y, z);
	//			if(te instanceof TileEntityWoodenCrate)
	//			{
	//				ItemStack stack = new ItemStack(this, 1, world.getBlockMetadata(x, y, z));
	//				NBTTagCompound tag = new NBTTagCompound();
	//				((TileEntityWoodenCrate) te).writeInv(tag, true);
	//				if(!tag.hasNoTags())
	//					stack.setTagCompound(tag);
	//				world.spawnEntityInWorld(new EntityItem(world, x+.5, y+.5, z+.5, stack));
	//			}
	//
	//			if(te instanceof TileEntityWoodenBarrel)
	//			{
	//				ItemStack stack = new ItemStack(this, 1, world.getBlockMetadata(x, y, z));
	//				NBTTagCompound tag = new NBTTagCompound();
	//				((TileEntityWoodenBarrel) te).writeTank(tag, true);
	//				if(!tag.hasNoTags())
	//					stack.setTagCompound(tag);
	//				world.spawnEntityInWorld(new EntityItem(world, x+.5, y+.5, z+.5, stack));
	//			}
	//		}
	//		super.onBlockExploded(world, x, y, z, explosion);
	//	}

	//	@Override
	//	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
	//	{
	//		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
	//		if(metadata==0 || metadata==4 || metadata==6)
	//			return ret;
	//
	//		int count = quantityDropped(metadata, fortune, world.rand);
	//		for(int i = 0; i < count; i++)
	//		{
	//			Item item = getItemDropped(metadata, world.rand, fortune);
	//			if (item != null)
	//			{
	//				ret.add(new ItemStack(item, 1, damageDropped(metadata)));
	//			}
	//		}
	//		return ret;
	//	}
	//	@Override
	//	public void breakBlock(World world, int x, int y, int z, Block par5, int par6)
	//	{
	//		TileEntity tileEntity = world.getTileEntity(x, y, z);
	//		if(tileEntity instanceof TileEntityWoodenPost)
	//		{
	//			int yy=y;
	//			byte type = ((TileEntityWoodenPost)tileEntity).type;
	//			switch(type)
	//			{
	//			case 4:
	//			case 5:
	//			case 6:
	//			case 7:
	//				return;
	//			default:
	//				yy-= ((TileEntityWoodenPost)tileEntity).type;
	//				break;
	//			}
	//
	//			for(int i=0;i<=3;i++)
	//			{
	//				world.setBlockToAir(x,yy+i,z);
	//				if(i==3)
	//				{
	//					TileEntity te;
	//					for(ForgeDirection fd : new ForgeDirection[]{ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.WEST})
	//					{
	//						te = world.getTileEntity(x+fd.offsetX, yy+i, z+fd.offsetZ);
	//						if(te instanceof TileEntityWoodenPost && ((TileEntityWoodenPost) te).type==(2+fd.ordinal()))
	//							world.setBlockToAir(x+fd.offsetX, yy+i, z+fd.offsetZ);
	//					}
	//				}
	//			}
	//			if(type==0 && !world.isRemote && world.getGameRules().getGameRuleBooleanValue("doTileDrops") && !world.restoringBlockSnapshots)
	//				world.spawnEntityInWorld(new EntityItem(world, x+.5,y+.5,z+.5, new ItemStack(this,1,0)));
	//		}
	//		if(tileEntity instanceof TileEntityWatermill)
	//		{
	//			int[] off = ((TileEntityWatermill)tileEntity).offset;
	//			int f = ((TileEntityWatermill)tileEntity).facing;
	//			int xx = x - ((f==2||f==3)?off[0]:0);
	//			int yy = y - off[1];
	//			int zz = z - ((f==2||f==3)?0:off[0]);
	//
	//			if(!(off[0]==0&&off[1]==0) && world.isAirBlock(xx, yy, zz))
	//				return;
	//			world.setBlockToAir(xx, yy, zz);
	//			for(int hh=-2;hh<=2;hh++)
	//			{
	//				int r=hh<-1||hh>1?1:2;
	//				for(int ww=-r;ww<=r;ww++)
	//					world.setBlockToAir(xx+((f==2||f==3)?ww:0), yy+hh, zz+((f==2||f==3)?0:ww));
	//			}
	//		}
	//		if(tileEntity instanceof TileEntityModWorkbench)
	//		{
	//			TileEntityModWorkbench tile = (TileEntityModWorkbench)tileEntity;
	//			int f = tile.facing;
	//			int off = tile.dummyOffset;
	//			if(tile.dummy)
	//				off *= -1;
	//			int xx = x+(f<4?off:0);
	//			int zz = z+(f>3?off:0);
	//
	//			if(world.getTileEntity(xx, y, zz) instanceof TileEntityModWorkbench)
	//				world.setBlockToAir(xx, y, zz);
	//			if(!world.isRemote && !tile.dummy && world.getGameRules().getGameRuleBooleanValue("doTileDrops"))
	//				for(int i=0; i<tile.getSizeInventory(); i++)
	//				{
	//					ItemStack stack = tile.getStackInSlot(i);
	//					if(stack!=null)
	//					{
	//						float fx = world.rand.nextFloat() * 0.8F + 0.1F;
	//						float fz = world.rand.nextFloat() * 0.8F + 0.1F;
	//
	//						EntityItem entityitem = new EntityItem(world, x+fx, y+.5, z+fz, stack);
	//						entityitem.motionX = world.rand.nextGaussian()*.05;
	//						entityitem.motionY = world.rand.nextGaussian()*.05+.2;
	//						entityitem.motionZ = world.rand.nextGaussian()*.05;
	//						if(stack.hasTagCompound())
	//							entityitem.getEntityItem().setTagCompound((NBTTagCompound)stack.getTagCompound().copy());
	//						world.spawnEntityInWorld(entityitem);
	//					}
	//				}
	//		}
	//		super.breakBlock(world, x, y, z, par5, par6);
	//	}


	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		switch(BlockTypes_WoodenDevice0.values()[meta])
		{
		case CRATE:
			return new TileEntityWoodenCrate();
		case WORKBENCH:
			return new TileEntityModWorkbench();
		case BARREL:
			return new TileEntityWoodenBarrel();
		case SORTER:
			return new TileEntitySorter();
		}
		return null;
	}
}