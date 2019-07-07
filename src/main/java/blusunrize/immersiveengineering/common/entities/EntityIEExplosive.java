/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.util.IEExplosion;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.Builder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.init.Particles;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nonnull;
import java.util.Optional;

public class EntityIEExplosive extends TNTEntity
{
	public static final EntityType<EntityIEExplosive> TYPE = new Builder<>(EntityIEExplosive.class, EntityIEExplosive::new)
			.build(ImmersiveEngineering.MODID+":explosive");

	private float explosionPower;
	private boolean explosionSmoke = true;
	private boolean explosionFire = false;
	private float explosionDropChance;
	public BlockState block;
	private ITextComponent name;

	private static final DataParameter<Optional<BlockState>> dataMarker_block = EntityDataManager.createKey(EntityIEExplosive.class, DataSerializers.OPTIONAL_BLOCK_STATE);
	private static final DataParameter<Integer> dataMarker_fuse = EntityDataManager.createKey(EntityIEExplosive.class, DataSerializers.VARINT);

	public EntityIEExplosive(World world)
	{
		super(world);
	}

	public EntityIEExplosive(World world, double x, double y, double z, LivingEntity igniter, BlockState blockstate, float explosionPower)
	{
		super(world, x, y, z, igniter);
		this.explosionPower = explosionPower;
		this.block = blockstate;
		this.explosionDropChance = 1/explosionPower;
		this.setBlockSynced();
	}

	public EntityIEExplosive(World world, BlockPos pos, LivingEntity igniter, BlockState blockstate, float explosionPower)
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
	protected void registerData()
	{
		super.registerData();
		this.dataManager.register(dataMarker_block, Optional.empty());
		this.dataManager.register(dataMarker_fuse, 0);
	}

	private void setBlockSynced()
	{
		if(this.block!=null)
		{
			this.dataManager.set(dataMarker_block, Optional.of(this.block));
			this.dataManager.set(dataMarker_fuse, this.getFuse());
		}
	}

	private void getBlockSynced()
	{
		this.block = this.dataManager.get(dataMarker_block).orElse(null);
		this.setFuse(this.dataManager.get(dataMarker_fuse));
	}

	@Nonnull
	@Override
	public ITextComponent getName()
	{
		if(this.block!=null&&name==null)
		{
			ItemStack s = new ItemStack(this.block.getBlock(), 1);
			if(!s.isEmpty()&&s.getItem()!=Items.AIR)
				name = s.getDisplayName();
		}
		if(name!=null)
			return name;
		return super.getName();
	}

	@Override
	protected void writeAdditional(CompoundNBT tagCompound)
	{
		super.writeAdditional(tagCompound);
		tagCompound.setFloat("explosionPower", explosionPower);
		tagCompound.setBoolean("explosionSmoke", explosionSmoke);
		tagCompound.setBoolean("explosionFire", explosionFire);
		if(this.block!=null)
			tagCompound.setInt("block", Block.getStateId(this.block));
	}

	@Override
	protected void readAdditional(CompoundNBT tagCompound)
	{
		super.readAdditional(tagCompound);
		explosionPower = tagCompound.getFloat("explosionPower");
		explosionSmoke = tagCompound.getBoolean("explosionSmoke");
		explosionFire = tagCompound.getBoolean("explosionFire");
		if(tagCompound.hasKey("block"))
			this.block = Block.getStateById(tagCompound.getInt("block"));
	}


	@Override
	public void tick()
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
		if(newFuse <= 0)
		{
			this.remove();

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
			this.world.spawnParticle(Particles.SMOKE, this.posX, this.posY+0.5D, this.posZ, 0.0D, 0.0D, 0.0D);
		}
	}

	@Nonnull
	@Override
	public EntityType<?> getType()
	{
		return TYPE;
	}
}