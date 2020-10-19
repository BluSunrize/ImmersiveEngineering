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
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.Builder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.Explosion.Mode;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.util.Optional;

public class IEExplosiveEntity extends TNTEntity
{
	public static final EntityType<IEExplosiveEntity> TYPE = Builder
			.<IEExplosiveEntity>create(IEExplosiveEntity::new, EntityClassification.MISC)
			.immuneToFire()
			.size(0.98F, 0.98F)
			.build(ImmersiveEngineering.MODID+":explosive");

	static
	{
		TYPE.setRegistryName(ImmersiveEngineering.MODID, "explosive");
	}

	private float size;
	private Explosion.Mode mode = Mode.BREAK;
	private boolean isFlaming = false;
	private float explosionDropChance;
	public BlockState block;
	private ITextComponent name;

	private static final DataParameter<Optional<BlockState>> dataMarker_block = EntityDataManager.createKey(IEExplosiveEntity.class, DataSerializers.OPTIONAL_BLOCK_STATE);
	private static final DataParameter<Integer> dataMarker_fuse = EntityDataManager.createKey(IEExplosiveEntity.class, DataSerializers.VARINT);

	public IEExplosiveEntity(EntityType<IEExplosiveEntity> type, World world)
	{
		super(type, world);
	}

	public IEExplosiveEntity(World world, double x, double y, double z, LivingEntity igniter, BlockState blockstate, float size)
	{
		super(TYPE, world);
		this.setPosition(x, y, z);
		double jumpingDirection = world.rand.nextDouble()*2*Math.PI;
		this.setMotion(-Math.sin(jumpingDirection)*0.02D, 0.2, -Math.cos(jumpingDirection)*0.02D);
		this.setFuse(80);
		this.prevPosX = x;
		this.prevPosY = y;
		this.prevPosZ = z;
		this.tntPlacedBy = igniter;
		this.size = size;
		this.block = blockstate;
		this.explosionDropChance = 1/size;
		this.setBlockSynced();
	}

	public IEExplosiveEntity(World world, BlockPos pos, LivingEntity igniter, BlockState blockstate, float size)
	{
		this(world, pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5, igniter, blockstate, size);
	}

	public IEExplosiveEntity setMode(Mode smoke)
	{
		this.mode = smoke;
		return this;
	}

	public IEExplosiveEntity setFlaming(boolean fire)
	{
		this.isFlaming = fire;
		return this;
	}

	public IEExplosiveEntity setDropChance(float chance)
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
		tagCompound.putFloat("explosionPower", size);
		tagCompound.putInt("explosionSmoke", mode.ordinal());
		tagCompound.putBoolean("explosionFire", isFlaming);
		if(this.block!=null)
			tagCompound.putInt("block", Block.getStateId(this.block));
	}

	@Override
	protected void readAdditional(CompoundNBT tagCompound)
	{
		super.readAdditional(tagCompound);
		size = tagCompound.getFloat("explosionPower");
		mode = Mode.values()[tagCompound.getInt("explosionSmoke")];
		isFlaming = tagCompound.getBoolean("explosionFire");
		if(tagCompound.contains("block", NBT.TAG_INT))
			this.block = Block.getStateById(tagCompound.getInt("block"));
	}


	@Override
	public void tick()
	{
		if(world.isRemote&&this.block==null)
			this.getBlockSynced();

		this.prevPosX = this.getPosX();
		this.prevPosY = this.getPosY();
		this.prevPosZ = this.getPosZ();
		if(!this.hasNoGravity())
		{
			this.setMotion(this.getMotion().add(0.0D, -0.04D, 0.0D));
		}

		this.move(MoverType.SELF, this.getMotion());
		this.setMotion(this.getMotion().scale(0.98D));
		if(this.onGround)
		{
			this.setMotion(this.getMotion().mul(0.7D, -0.5D, 0.7D));
		}
		int newFuse = this.getFuse()-1;
		this.setFuse(newFuse);
		if(newFuse <= 0)
		{
			this.remove();

			Explosion explosion = new IEExplosion(world, this, getPosX(), getPosY()+(getHeight()/16f), getPosZ(), size, isFlaming, mode)
					.setDropChance(explosionDropChance);
			if(!ForgeEventFactory.onExplosionStart(world, explosion))
			{
				explosion.doExplosionA();
				explosion.doExplosionB(true);
			}
		}
		else
		{
			this.handleWaterMovement();
			this.world.addParticle(ParticleTypes.SMOKE, this.getPosX(), this.getPosY()+0.5D, this.getPosZ(), 0.0D, 0.0D, 0.0D);
		}
	}

	@Nonnull
	@Override
	public EntityType<?> getType()
	{
		return TYPE;
	}

	@Override
	public IPacket<?> createSpawnPacket()
	{
		return NetworkHooks.getEntitySpawningPacket(this);
	}

}