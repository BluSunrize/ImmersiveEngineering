package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.common.util.IEExplosion;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

public class EntityIEExplosive extends EntityTNTPrimed
{
	float explosionPower;
	boolean explosionSmoke = true;
	boolean explosionFire = false;
	float explosionDropChance;
	public Block block;
	public int meta;
	String name;

	static int dataMarker_block = 12;
	static int dataMarker_meta = 13;
	static int dataMarker_fuse = 14;

	public EntityIEExplosive(World world)
	{
		super(world);
	}
	public EntityIEExplosive(World world, double x, double y, double z, EntityLivingBase igniter, IBlockState blockstate, float explosionPower)
	{
		super(world, x, y, z, igniter);
		this.explosionPower = explosionPower;
		this.block = blockstate.getBlock();
		this.meta = blockstate.getBlock().getMetaFromState(blockstate);
		this.explosionDropChance = 1/explosionPower;
		this.setBlockSynced();
	}
	public EntityIEExplosive(World world, BlockPos pos, EntityLivingBase igniter, IBlockState blockstate, float explosionPower)
	{
		this(world, pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5, igniter, blockstate, explosionPower);
	}

	public EntityIEExplosive setSmoking(boolean smoke)
	{
		this.explosionSmoke = smoke;
		return this;
	}
	public EntityIEExplosive setFlaming(boolean fire)
	{
		this.explosionFire = fire;
		return this;
	}
	public EntityIEExplosive setDropChance(float chance)
	{
		this.explosionDropChance = chance;
		return this;
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.dataWatcher.addObject(dataMarker_block, "");
		this.dataWatcher.addObject(dataMarker_meta, Integer.valueOf(0));
		this.dataWatcher.addObject(dataMarker_fuse, Integer.valueOf(0));
	}

	public void setBlockSynced()
	{
		if(this.block!=null)
		{
			this.dataWatcher.updateObject(dataMarker_block, this.block.getRegistryName());
			this.dataWatcher.updateObject(dataMarker_meta, this.meta);
			this.dataWatcher.updateObject(dataMarker_fuse, this.fuse);
		}
	}
	public void getBlockSynced()
	{
		this.block = Block.getBlockFromName(this.dataWatcher.getWatchableObjectString(dataMarker_block));
		this.meta = this.dataWatcher.getWatchableObjectInt(dataMarker_meta);
		this.fuse = this.dataWatcher.getWatchableObjectInt(dataMarker_fuse);
	}

	@Override
	public String getName()
	{
		if(this.block!=null && name==null)
		{
			ItemStack s = new ItemStack(this.block,1,this.meta);
			if(s!=null && s.getItem()!=null)
				name = s.getDisplayName();
		}
		if(name!=null)
			return name;
		return super.getName();
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound tagCompound)
	{
		super.writeEntityToNBT(tagCompound);
		tagCompound.setFloat("explosionPower", explosionPower);
		tagCompound.setBoolean("explosionSmoke", explosionSmoke);
		tagCompound.setBoolean("explosionFire", explosionFire);
		if(this.block!=null)
		{
			tagCompound.setString("block", this.block.getRegistryName());
			tagCompound.setInteger("meta", this.meta);
		}
	}
	@Override
	protected void readEntityFromNBT(NBTTagCompound tagCompound)
	{
		super.readEntityFromNBT(tagCompound);
		explosionPower = tagCompound.getFloat("explosionPower");
		explosionSmoke = tagCompound.getBoolean("explosionSmoke");
		explosionFire = tagCompound.getBoolean("explosionFire");
		if(tagCompound.hasKey("block"))
		{
			this.block = Block.getBlockFromName(tagCompound.getString("block"));
			this.meta = tagCompound.getInteger("meta");
		}
	}


	@Override
	public void onUpdate()
	{
		if(worldObj.isRemote && this.block==null)
			this.getBlockSynced();

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.motionY -= 0.03999999910593033D;
		this.moveEntity(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.9800000190734863D;
		this.motionY *= 0.9800000190734863D;
		this.motionZ *= 0.9800000190734863D;

		if(this.onGround)
		{
			this.motionX *= 0.699999988079071D;
			this.motionZ *= 0.699999988079071D;
			this.motionY *= -0.5D;
		}

		if(this.fuse--<=0)
		{
			this.setDead();

			if(!this.worldObj.isRemote)
			{
				Explosion explosion = new IEExplosion(worldObj, this, posX,posY+(height/16f),posZ, explosionPower, explosionFire, explosionSmoke).setDropChance(explosionDropChance);
				if(!ForgeEventFactory.onExplosionStart(worldObj, explosion))
				{
					explosion.doExplosionA();
					explosion.doExplosionB(true);
				}
			}
		}
		else
		{
			this.handleWaterMovement();
			this.worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + 0.5D, this.posZ, 0.0D, 0.0D, 0.0D, new int[0]);
		}
	}
}