/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.common.util.IEExplosion;
import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

public class EntityIEExplosive extends EntityTNTPrimed
{
	float explosionPower;
	boolean explosionSmoke = true;
	boolean explosionFire = false;
	float explosionDropChance;
	public IBlockState block;
	String name;

	private static final DataParameter<Optional<IBlockState>> dataMarker_block = EntityDataManager.createKey(EntityIEExplosive.class, DataSerializers.OPTIONAL_BLOCK_STATE);
	private static final DataParameter<Integer> dataMarker_fuse = EntityDataManager.createKey(EntityIEExplosive.class, DataSerializers.VARINT);

	public EntityIEExplosive(World world)
	{
		super(world);
	}

	public EntityIEExplosive(World world, double x, double y, double z, EntityLivingBase igniter, IBlockState blockstate, float explosionPower)
	{
		super(world, x, y, z, igniter);
		this.explosionPower = explosionPower;
		this.block = blockstate;
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
		this.dataManager.register(dataMarker_block, Optional.absent());
		this.dataManager.register(dataMarker_fuse, Integer.valueOf(0));
	}

	public void setBlockSynced()
	{
		if(this.block!=null)
		{
			this.dataManager.set(dataMarker_block, Optional.of(this.block));
			this.dataManager.set(dataMarker_fuse, this.getFuse());
		}
	}

	public void getBlockSynced()
	{
		this.block = this.dataManager.get(dataMarker_block).orNull();
		this.setFuse(this.dataManager.get(dataMarker_fuse));
	}

	@Override
	public String getName()
	{
		if(this.block!=null&&name==null)
		{
			ItemStack s = new ItemStack(this.block.getBlock(), 1, this.block.getBlock().getMetaFromState(this.block));
			if(!s.isEmpty()&&s.getItem()!=Items.AIR)
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
			tagCompound.setInteger("block", Block.getStateId(this.block));
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound tagCompound)
	{
		super.readEntityFromNBT(tagCompound);
		explosionPower = tagCompound.getFloat("explosionPower");
		explosionSmoke = tagCompound.getBoolean("explosionSmoke");
		explosionFire = tagCompound.getBoolean("explosionFire");
		if(tagCompound.hasKey("block"))
			this.block = Block.getStateById(tagCompound.getInteger("block"));
	}


	@Override
	public void onUpdate()
	{
		if(world.isRemote&&this.block==null)
			this.getBlockSynced();

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.motionY -= 0.03999999910593033D;
		this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.9800000190734863D;
		this.motionY *= 0.9800000190734863D;
		this.motionZ *= 0.9800000190734863D;

		if(this.onGround)
		{
			this.motionX *= 0.699999988079071D;
			this.motionZ *= 0.699999988079071D;
			this.motionY *= -0.5D;
		}
		int newFuse = this.getFuse()-1;
		this.setFuse(newFuse);
		if(newFuse-- <= 0)
		{
			this.setDead();

			if(!this.world.isRemote)
			{
				Explosion explosion = new IEExplosion(world, this, posX, posY+(height/16f), posZ, explosionPower, explosionFire, explosionSmoke).setDropChance(explosionDropChance);
				if(!ForgeEventFactory.onExplosionStart(world, explosion))
				{
					explosion.doExplosionA();
					explosion.doExplosionB(true);
				}
			}
		}
		else
		{
			this.handleWaterMovement();
			this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY+0.5D, this.posZ, 0.0D, 0.0D, 0.0D);
		}
	}
}