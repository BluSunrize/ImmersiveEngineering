/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.client.ClientProxy;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.entities.EntityRevolvershot;
import blusunrize.immersiveengineering.common.entities.EntityRevolvershotFlare;
import blusunrize.immersiveengineering.common.entities.EntityRevolvershotHoming;
import blusunrize.immersiveengineering.common.entities.EntityWolfpackShot;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.ITextureOverride;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemLingeringPotion;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemSplashPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ItemBullet extends ItemIEBase implements ITextureOverride//IBullet
{
	public ItemBullet()
	{
//		super("bullet", 64, "emptyCasing","emptyShell","casull","armorPiercing","buckshot","HE","dragonsbreath","homing","wolfpack","silver","potion","flare");
		super("bullet", 64, "empty_casing", "empty_shell", "bullet");

		BulletHandler.emptyCasing = new ItemStack(this, 1, 0);
		BulletHandler.emptyShell = new ItemStack(this, 1, 1);
		BulletHandler.basicCartridge = new ItemStack(this, 1, 2);
	}

	public static void initBullets()
	{
		BulletHandler.registerBullet("casull", new BulletHandler.DamagingBullet(
				entities -> IEDamageSources.causeCasullDamage((EntityRevolvershot)entities[0], entities[1]),
				IEConfig.Tools.bulletDamage_Casull,
				BulletHandler.emptyCasing,
				new ResourceLocation("immersiveengineering:items/bullet_casull")));

		BulletHandler.registerBullet("armor_piercing", new BulletHandler.DamagingBullet(
				entities -> IEDamageSources.causePiercingDamage((EntityRevolvershot)entities[0], entities[1]),
				IEConfig.Tools.bulletDamage_AP,
				BulletHandler.emptyCasing,
				new ResourceLocation("immersiveengineering:items/bullet_armor_piercing")));

		BulletHandler.registerBullet("buckshot", new BulletHandler.DamagingBullet(
				entities -> IEDamageSources.causeBuckshotDamage((EntityRevolvershot)entities[0], entities[1]),
				IEConfig.Tools.bulletDamage_Buck,
				true,
				false,
				BulletHandler.emptyShell,
				new ResourceLocation("immersiveengineering:items/bullet_buckshot"))
		{
			@Override
			public int getProjectileCount(EntityPlayer shooter)
			{
				return 10;
			}
		});

		BulletHandler.registerBullet("he", new BulletHandler.DamagingBullet(null, 0, BulletHandler.emptyCasing, new ResourceLocation("immersiveengineering:items/bullet_he"))
		{
			@Override
			public void onHitTarget(World world, RayTraceResult target, EntityLivingBase shooter, Entity projectile, boolean headshot)
			{
				world.createExplosion(shooter, projectile.posX, projectile.posY, projectile.posZ, 2, false);
			}

			@Override
			public Entity getProjectile(@Nullable EntityPlayer shooter, ItemStack cartridge, Entity projectile, boolean charged)
			{
				if(projectile instanceof EntityRevolvershot)
				{
					((EntityRevolvershot)projectile).setGravity(0.05f);
					((EntityRevolvershot)projectile).setMovementDecay(0.9f);
				}
				return projectile;
			}

			@Override
			public SoundEvent getSound()
			{
				return IESounds.revolverFireThump;
			}
		});

		BulletHandler.registerBullet("silver", new BulletHandler.DamagingBullet(
				entities -> IEDamageSources.causeSilverDamage((EntityRevolvershot)entities[0], entities[1]),
				IEConfig.Tools.bulletDamage_Silver,
				BulletHandler.emptyCasing,
				new ResourceLocation("immersiveengineering:items/bullet_silver")));

		BulletHandler.registerBullet("dragonsbreath", new BulletHandler.DamagingBullet(
				entities -> IEDamageSources.causeDragonsbreathDamage((EntityRevolvershot)entities[0], entities[1]),
				IEConfig.Tools.bulletDamage_Dragon,
				true,
				true,
				BulletHandler.emptyShell,
				new ResourceLocation("immersiveengineering:items/bullet_dragonsbreath"))
		{
			@Override
			public int getProjectileCount(EntityPlayer shooter)
			{
				return 10;
			}

			@Override
			public Entity getProjectile(EntityPlayer shooter, ItemStack cartridge, Entity projectile, boolean electro)
			{
				((EntityRevolvershot)projectile).setTickLimit(10);
				projectile.setFire(3);
				return projectile;
			}
		});

		BulletHandler.registerBullet("potion", new PotionBullet());

		BulletHandler.registerBullet("flare", new FlareBullet());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list)
	{
		if(this.isInCreativeTab(tab))
		{
			list.add(new ItemStack(this, 1, 0));
			list.add(new ItemStack(this, 1, 1));
			for(Map.Entry<String, IBullet> entry : BulletHandler.registry.entrySet())
				if(entry.getValue().isProperCartridge())
				{
					ItemStack s = new ItemStack(this, 1, 2);
					ItemNBTHelper.setString(s, "bullet", entry.getKey());
					list.add(s);
				}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public FontRenderer getFontRenderer(ItemStack stack)
	{
		return ClientProxy.itemFont;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag)
	{
		if(stack.getItemDamage()==2)
		{
			String key = ItemNBTHelper.getString(stack, "bullet");
			IBullet bullet = BulletHandler.getBullet(key);
			if(bullet!=null)
				bullet.addTooltip(stack, world, list, flag);
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		if(stack.getItemDamage()==2)
		{
			String s = "item.immersiveengineering.bullet.";
			String key = ItemNBTHelper.getString(stack, "bullet");
			// handle legacy bullets
			key = BulletHandler.handleLeagcyNames(key);
			s += key;
			IBullet bullet = BulletHandler.getBullet(key);
			if(bullet!=null)
				s = bullet.getTranslationKey(stack, s);
			return I18n.translateToLocal(s+".name").trim();
		}
		return super.getItemStackDisplayName(stack);
	}

	@Override
	public boolean hasCustomItemColours()
	{
		return true;
	}

	@Override
	public int getColourForIEItem(ItemStack stack, int pass)
	{
		if(stack.getMetadata()==2&&ItemNBTHelper.hasKey(stack, "bullet"))
		{
			IBullet bullet = BulletHandler.getBullet(ItemNBTHelper.getString(stack, "bullet"));
			if(bullet!=null)
				return bullet.getColour(stack, pass);
		}
		return super.getColourForIEItem(stack, pass);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getModelCacheKey(ItemStack stack)
	{
		if(stack.getMetadata()==2&&ItemNBTHelper.hasKey(stack, "bullet"))
			return ItemNBTHelper.getString(stack, "bullet");
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<ResourceLocation> getTextures(ItemStack stack, String key)
	{
		IBullet bullet = BulletHandler.getBullet(key);
		if(bullet!=null)
			return Arrays.asList(bullet.getTextures());
		return Arrays.asList(new ResourceLocation("immersiveengieering:items/bullet_casull"));
	}

	//	@Override
//	public ItemStack getCasing(ItemStack stack)
//	{
//		return new ItemStack(this, 1, stack.getItemDamage()==1||stack.getItemDamage()==4||stack.getItemDamage()==6||stack.getItemDamage()==11?1:0);
//	}
//	@Override
//	public boolean canSpawnBullet(ItemStack bulletStack)
//	{
//		return bulletStack!=null && bulletStack.getItemDamage()>1 && (bulletStack.getItemDamage()!=10||ItemNBTHelper.getItemStack(bulletStack, "potion")!=null);
//	}
//	@Override
//	public void spawnBullet(EntityPlayer player, ItemStack bulletStack, boolean electro)
//	{
//		Vec3d vec = player.getLookVec();
//		int type = bulletStack.getItemDamage()-2;
//		switch(type)
//		{
//			case 0://casull
//				doSpawnBullet(player, vec, vec, type, bulletStack, electro);
//				break;
//			case 1://armorPiercing
//				doSpawnBullet(player, vec, vec, type, bulletStack, electro);
//				break;
//			case 2://buckshot
//				for(int i=0; i<10; i++)
//				{
//					Vec3d vecDir = vec.add(player.getRNG().nextGaussian()*.1,player.getRNG().nextGaussian()*.1,player.getRNG().nextGaussian()*.1);
//					doSpawnBullet(player, vec, vecDir, type, bulletStack, electro);
//				}
//				break;
//			case 3://HE
//				doSpawnBullet(player, vec, vec, type, bulletStack, electro);
//				break;
//			case 4://dragonsbreath
//				for(int i=0; i<30; i++)
//				{
//					Vec3d vecDir = vec.add(player.getRNG().nextGaussian()*.1,player.getRNG().nextGaussian()*.1,player.getRNG().nextGaussian()*.1);
//					EntityRevolvershot shot = doSpawnBullet(player, vec, vecDir, type, bulletStack, electro);
//					shot.setTickLimit(10);
//					shot.setFire(3);
//				}
//				break;
//			case 5://homing
//				EntityRevolvershotHoming bullet = new EntityRevolvershotHoming(player.world, player, vec.xCoord*1.5,vec.yCoord*1.5,vec.zCoord*1.5, type, bulletStack);
//				bullet.motionX = vec.xCoord;
//				bullet.motionY = vec.yCoord;
//				bullet.motionZ = vec.zCoord;
//				bullet.bulletElectro = electro;
//				player.world.spawnEntity(bullet);
//				break;
//			case 6://wolfpack
//				doSpawnBullet(player, vec, vec, type, bulletStack, electro);
//				break;
//			case 7://Silver
//				doSpawnBullet(player, vec, vec, type, bulletStack, electro);
//				break;
//			case 8://Potion
//				EntityRevolvershot shot = doSpawnBullet(player, vec, vec, type, bulletStack, electro);
//				shot.bulletPotion = ItemNBTHelper.getItemStack(bulletStack, "potion");
//				break;
//			case 9://Flare
//				EntityRevolvershotFlare flare = new EntityRevolvershotFlare(player.world, player, vec.xCoord*1.5,vec.yCoord*1.5,vec.zCoord*1.5, type, bulletStack);
//				flare.motionX = vec.xCoord;
//				flare.motionY = vec.yCoord;
//				flare.motionZ = vec.zCoord;
//				flare.bulletElectro = electro;
//				flare.colour = this.getColourForIEItem(bulletStack, 1);
//				flare.setColourSynced();
//				player.world.spawnEntity(flare);
//				break;
//		}
//	}


	public static class PotionBullet extends BulletHandler.DamagingBullet
	{
		public PotionBullet()
		{
			super(entities -> IEDamageSources.causePotionDamage((EntityRevolvershot)entities[0], entities[1]),
					IEConfig.Tools.bulletDamage_Potion,
					BulletHandler.emptyCasing,
					new ResourceLocation("immersiveengineering:items/bullet_potion"), new ResourceLocation("immersiveengineering:items/bullet_potion_layer"));
		}

		@Override
		public String getTranslationKey(ItemStack cartridge, String baseName)
		{
			ItemStack pot = ItemNBTHelper.getItemStack(cartridge, "potion");
			if(!pot.isEmpty())
				if(pot.getItem() instanceof ItemLingeringPotion)
					baseName += ".linger";
				else if(pot.getItem() instanceof ItemSplashPotion)
					baseName += ".splash";
			return baseName;
		}

		@Override
		public Entity getProjectile(EntityPlayer shooter, ItemStack cartridge, Entity projectile, boolean electro)
		{
			((EntityRevolvershot)projectile).bulletPotion = ItemNBTHelper.getItemStack(cartridge, "potion");
			return projectile;
		}

		@Override
		public void onHitTarget(World world, RayTraceResult target, EntityLivingBase shooter, Entity projectile, boolean headshot)
		{
			super.onHitTarget(world, target, shooter, projectile, headshot);
			EntityRevolvershot bullet = (EntityRevolvershot)projectile;
			if(!bullet.bulletPotion.isEmpty()&&bullet.bulletPotion.hasTagCompound())
			{
				PotionType potionType = PotionUtils.getPotionFromItem(bullet.bulletPotion);
				List<PotionEffect> effects = PotionUtils.getEffectsFromStack(bullet.bulletPotion);
				if(effects!=null)
					if(bullet.bulletPotion.getItem() instanceof ItemLingeringPotion)
					{
						EntityAreaEffectCloud entityareaeffectcloud = new EntityAreaEffectCloud(bullet.world, bullet.posX, bullet.posY, bullet.posZ);
						entityareaeffectcloud.setOwner(shooter);
						entityareaeffectcloud.setRadius(3.0F);
						entityareaeffectcloud.setRadiusOnUse(-0.5F);
						entityareaeffectcloud.setWaitTime(10);
						entityareaeffectcloud.setRadiusPerTick(-entityareaeffectcloud.getRadius()/(float)entityareaeffectcloud.getDuration());
						entityareaeffectcloud.setPotion(potionType);
						for(PotionEffect potioneffect : effects)
							entityareaeffectcloud.addEffect(new PotionEffect(potioneffect.getPotion(), potioneffect.getDuration(), potioneffect.getAmplifier()));
						bullet.world.spawnEntity(entityareaeffectcloud);
					}
					else if(bullet.bulletPotion.getItem() instanceof ItemSplashPotion)
					{
						List<EntityLivingBase> livingEntities = bullet.world.getEntitiesWithinAABB(EntityLivingBase.class, bullet.getEntityBoundingBox().grow(4.0D, 2.0D, 4.0D));
						if(livingEntities!=null&&!livingEntities.isEmpty())
							for(EntityLivingBase living : livingEntities)
								if(living.canBeHitWithPotion())
								{
									double dist = bullet.getDistanceSq(living);
									if(dist < 16D)
									{
										double dist2 = 1-Math.sqrt(dist)/4D;
										if(living==target.entityHit)
											dist2 = 1D;
										for(PotionEffect p : effects)
											if(p.getPotion().isInstant())
												p.getPotion().affectEntity(bullet, shooter, living, p.getAmplifier(), dist2);
											else
											{
												int j = (int)(dist2*p.getDuration()+.5D);
												if(j > 20)
													living.addPotionEffect(new PotionEffect(p.getPotion(), j, p.getAmplifier()));
											}
									}
								}

					}
					else if(target.entityHit instanceof EntityLivingBase)
						for(PotionEffect p : effects)
						{
							if(p.getDuration() < 1)
								p = new PotionEffect(p.getPotion(), 1);
							((EntityLivingBase)target.entityHit).addPotionEffect(p);
						}
				world.playEvent(2002, new BlockPos(bullet), PotionUtils.getPotionColor(potionType));
			}
		}


		@Override
		public void addTooltip(ItemStack stack, World world, List<String> list, ITooltipFlag flag)
		{
			ItemStack pot = ItemNBTHelper.getItemStack(stack, "potion");
			if(!pot.isEmpty()&&pot.getItem() instanceof ItemPotion)
				PotionUtils.addPotionTooltip(pot, list, 1f);
		}

		@Override
		public int getColour(ItemStack stack, int layer)
		{
			if(layer==1)
			{
				ItemStack pot = ItemNBTHelper.getItemStack(stack, "potion");
				return pot.isEmpty()?0xff385dc6: PotionUtils.getPotionColorFromEffectList(PotionUtils.getEffectsFromStack(pot));
			}
			return 0xffffffff;
		}
	}

	public static class FlareBullet implements BulletHandler.IBullet
	{
		static ResourceLocation[] textures = {new ResourceLocation("immersiveengineering:items/bullet_flare"), new ResourceLocation("immersiveengineering:items/bullet_flare_layer")};

		public FlareBullet()
		{
		}

		@Override
		public Entity getProjectile(EntityPlayer shooter, ItemStack cartridge, Entity projectile, boolean electro)
		{
			EntityRevolvershotFlare flare = shooter!=null?new EntityRevolvershotFlare(projectile.world, shooter, projectile.motionX*1.5, projectile.motionY*1.5, projectile.motionZ*1.5, this, cartridge): new EntityRevolvershotFlare(projectile.world, projectile.posX, projectile.posY, projectile.posZ, 0, 0, 0, this);
			flare.motionX = projectile.motionX;
			flare.motionY = projectile.motionY;
			flare.motionZ = projectile.motionZ;
			flare.bulletElectro = electro;
			flare.colour = this.getColour(cartridge, 1);
			flare.setColourSynced();
			return flare;
		}

		@Override
		public void onHitTarget(World world, RayTraceResult target, EntityLivingBase shooter, Entity projectile, boolean headshot)
		{
		}

		@Override
		public ItemStack getCasing(ItemStack stack)
		{
			return BulletHandler.emptyShell;
		}

		@Override
		public ResourceLocation[] getTextures()
		{
			return textures;
		}

		@Override
		public void addTooltip(ItemStack stack, World world, List<String> list, ITooltipFlag flag)
		{
			if(stack.getItem() instanceof IColouredItem)
			{
				String hexCol = Integer.toHexString(((IColouredItem)stack.getItem()).getColourForIEItem(stack, 1));
				list.add(I18n.translateToLocalFormatted(Lib.DESC_INFO+"bullet.flareColour", "<hexcol="+hexCol+":#"+hexCol+">"));
			}
		}

		@Override
		public int getColour(ItemStack stack, int layer)
		{
			if(layer!=1)
				return 0xffffffff;
			return ItemNBTHelper.hasKey(stack, "flareColour")?ItemNBTHelper.getInt(stack, "flareColour"): 0xcc2e06;
		}

		@Override
		public boolean isValidForTurret()
		{
			return true;
		}
	}

	public static class HomingBullet extends BulletHandler.DamagingBullet
	{
		public HomingBullet(float damage, ResourceLocation... textures)
		{
			super(new Function<Entity[], DamageSource>()
				  {
					  @Override
					  public DamageSource apply(Entity[] entities)
					  {
						  return IEDamageSources.causeHomingDamage((EntityRevolvershot)entities[0], entities[1]);
					  }
				  },
					damage,
					BulletHandler.emptyCasing,
					textures);
		}

		@Override
		public Entity getProjectile(EntityPlayer shooter, ItemStack cartridge, Entity projectile, boolean electro)
		{
			EntityRevolvershotHoming shot = shooter!=null?new EntityRevolvershotHoming(projectile.world, shooter, projectile.motionX*1.5, projectile.motionY*1.5, projectile.motionZ*1.5, this, cartridge): new EntityRevolvershotHoming(projectile.world, projectile.posX, projectile.posY, projectile.posZ, 0, 0, 0, this);
			shot.motionX = projectile.motionX;
			shot.motionY = projectile.motionY;
			shot.motionZ = projectile.motionZ;
			shot.bulletElectro = electro;
			return shot;
		}

		@Override
		public int getColour(ItemStack stack, int layer)
		{
			return 0xffffffff;
		}
	}

	public static class WolfpackBullet extends BulletHandler.DamagingBullet
	{
		public WolfpackBullet()
		{
			super(entities -> IEDamageSources.causeWolfpackDamage((EntityRevolvershot)entities[0], entities[1]),
					IEConfig.Tools.bulletDamage_Wolfpack,
					BulletHandler.emptyShell,
					new ResourceLocation("immersiveengineering:items/bullet_wolfpack"));
		}

		@Override
		public void onHitTarget(World world, RayTraceResult target, EntityLivingBase shooter, Entity projectile, boolean headshot)
		{
			super.onHitTarget(world, target, shooter, projectile, headshot);
			Vec3d v = new Vec3d(-projectile.motionX, -projectile.motionY, -projectile.motionZ);
			int split = 6;
			for(int i = 0; i < split; i++)
			{
				float angle = i*(360f/split);
				Matrix4 matrix = new Matrix4();
				matrix.rotate(angle, v.x, v.y, v.z);
				Vec3d vecDir = new Vec3d(0, 1, 0);
				vecDir = matrix.apply(vecDir);

				EntityWolfpackShot bullet = shooter!=null?new EntityWolfpackShot(world, shooter, vecDir.x*1.5, vecDir.y*1.5, vecDir.z*1.5, this, null): new EntityWolfpackShot(world, 0, 0, 0, 0, 0, 0, this);
				if(target.entityHit instanceof EntityLivingBase)
					bullet.targetOverride = (EntityLivingBase)target.entityHit;
				bullet.setPosition(target.hitVec.x+vecDir.x, target.hitVec.y+vecDir.y, target.hitVec.z+vecDir.z);
				bullet.motionX = vecDir.x*.375;
				bullet.motionY = vecDir.y*.375;
				bullet.motionZ = vecDir.z*.375;
				world.spawnEntity(bullet);
			}
		}

		@Override
		public int getColour(ItemStack stack, int layer)
		{
			return 0xffffffff;
		}
	}

	public static class WolfpackPartBullet extends BulletHandler.DamagingBullet
	{
		public WolfpackPartBullet()
		{
			super(entities -> IEDamageSources.causeWolfpackDamage((EntityRevolvershot)entities[0], entities[1]),
					IEConfig.Tools.bulletDamage_WolfpackPart,
					BulletHandler.emptyCasing,
					new ResourceLocation("immersiveengineering:items/bullet_wolfpack"));
		}

		@Override
		public boolean isProperCartridge()
		{
			return false;
		}

		@Override
		public int getColour(ItemStack stack, int layer)
		{
			return 0xffffffff;
		}
	}
}