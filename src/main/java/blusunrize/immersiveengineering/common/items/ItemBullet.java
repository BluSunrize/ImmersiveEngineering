package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.client.ClientProxy;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.entities.EntityRevolvershot;
import blusunrize.immersiveengineering.common.entities.EntityRevolvershotFlare;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.ITextureOverride;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemLingeringPotion;
import net.minecraft.item.ItemSplashPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ItemBullet extends ItemIEBase implements ITextureOverride//IBullet
{
	public ItemBullet()
	{
//		super("bullet", 64, "emptyCasing","emptyShell","casull","armorPiercing","buckshot","HE","dragonsbreath","homing","wolfpack","silver","potion","flare");
		super("bullet", 64, "emptyCasing", "emptyShell", "bullet");

		BulletHandler.emptyCasing = new ItemStack(this, 1, 0);
		BulletHandler.emptyShell = new ItemStack(this, 1, 1);

		BulletHandler.registerBullet("casull", new BulletHandler.DamagingBullet(
				new Function<Entity[], DamageSource>()
				{
					@Override
					public DamageSource apply(Entity[] entities)
					{
						return IEDamageSources.causeCasullDamage((EntityRevolvershot) entities[0], entities[1]);
					}
				},
				(float) Config.getDouble("BulletDamage-Casull"),
				BulletHandler.emptyCasing,
				new ResourceLocation("immersiveengineering:items/bullet_casull")));

		BulletHandler.registerBullet("armorPiercing", new BulletHandler.DamagingBullet(
				new Function<Entity[], DamageSource>()
				{
					@Override
					public DamageSource apply(Entity[] entities)
					{
						return IEDamageSources.causePiercingDamage((EntityRevolvershot) entities[0], entities[1]);
					}
				},
				(float) Config.getDouble("BulletDamage-AP"),
				BulletHandler.emptyCasing,
				new ResourceLocation("immersiveengineering:items/bullet_armorPiercing")));

		BulletHandler.registerBullet("buckshot", new BulletHandler.DamagingBullet(
				new Function<Entity[], DamageSource>()
				{
					@Override
					public DamageSource apply(Entity[] entities)
					{
						return IEDamageSources.causeCasullDamage((EntityRevolvershot) entities[0], entities[1]);
					}
				},
				(float) Config.getDouble("BulletDamage-Buck"),
				BulletHandler.emptyCasing,
				new ResourceLocation("immersiveengineering:items/bullet_buckshot"))
		{
			@Override
			public int getProjectileCount(EntityPlayer shooter, ItemStack cartridge)
			{
				return 10;
			}
		});

		BulletHandler.registerBullet("HE", new BulletHandler.DamagingBullet(null, 0, BulletHandler.emptyCasing, new ResourceLocation("immersiveengineering:items/bullet_HE"))
		{
			@Override
			public void onHitTarget(World world, RayTraceResult target, EntityLivingBase shooter, Entity projectile, boolean headshot)
			{
				world.createExplosion(shooter, projectile.posX, projectile.posY, projectile.posZ, 2, false);
			}
		});

		BulletHandler.registerBullet("silver", new BulletHandler.DamagingBullet(
				new Function<Entity[], DamageSource>()
				{
					@Override
					public DamageSource apply(Entity[] entities)
					{
						return IEDamageSources.causeSilverDamage((EntityRevolvershot) entities[0], entities[1]);
					}
				},
				(float) Config.getDouble("BulletDamage-Silver"),
				BulletHandler.emptyCasing,
				new ResourceLocation("immersiveengineering:items/bullet_silver")));

		BulletHandler.registerBullet("dragonsbreath", new BulletHandler.DamagingBullet(
				new Function<Entity[], DamageSource>()
				{
					@Override
					public DamageSource apply(Entity[] entities)
					{
						return IEDamageSources.causeCasullDamage((EntityRevolvershot) entities[0], entities[1]);
					}
				},
				(float) Config.getDouble("BulletDamage-Dragon"),
				BulletHandler.emptyCasing,
				new ResourceLocation("immersiveengineering:items/bullet_dragonsbreath"))
		{
			@Override
			public int getProjectileCount(EntityPlayer shooter, ItemStack cartridge)
			{
				return 30;
			}

			@Override
			public Entity getProjectile(EntityPlayer shooter, ItemStack cartridge, Entity protetile, boolean electro)
			{
				((EntityRevolvershot) protetile).setTickLimit(10);
				protetile.setFire(3);
				return protetile;
			}
		});

//		homing, Homing, homing
//		wolfpack, Wolfpack, wolfpack

		BulletHandler.registerBullet("potion", new PotionBullet());

		BulletHandler.registerBullet("flare", new FlareBullet());
	}

	public static ItemStack getBulletStack(String key)
	{
		ItemStack stack = new ItemStack(IEContent.itemBullet, 1, 2);
		ItemNBTHelper.setString(stack, "bullet", key);
		return stack;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list)
	{
//		for(int i=0;i<getSubNames().length;i++)
//			if((i!=7&&i!=8) || (Loader.isModLoaded("Botania")&&Config.getBoolean("compat_Botania")))
//				list.add(new ItemStack(this,1,i));
		list.add(new ItemStack(this, 1, 0));
		list.add(new ItemStack(this, 1, 1));
		for(Map.Entry<String, IBullet> entry : BulletHandler.registry.entrySet())
		{
			ItemStack s = new ItemStack(this, 1, 2);
			ItemNBTHelper.setString(s, "bullet", entry.getKey());
			list.add(s);
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
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced)
	{
//		if(stack.getItemDamage()==10)
//		{
//			ItemStack pot = ItemNBTHelper.getItemStack(stack, "potion");
//			if(pot!=null && pot.getItem() instanceof ItemPotion)
//			{
//				List effects = PotionUtils.getEffectsFromStack(pot);
//				HashMultimap hashmultimap = HashMultimap.create();
//				Iterator iterator1;
//				if(effects != null && !effects.isEmpty())
//				{
//					iterator1 = effects.iterator();
//					while(iterator1.hasNext())
//					{
//						PotionEffect potioneffect = (PotionEffect)iterator1.next();
//						String s1 = I18n.format(potioneffect.getEffectName()).trim();
//						Potion potion = potioneffect.getPotion();
//						Map<IAttribute, AttributeModifier> map = potion.getAttributeModifierMap();
//
//						if(map!=null && map.size()>0)
//						{
//							Iterator<Entry<IAttribute, AttributeModifier>> iterator = map.entrySet().iterator();
//							while (iterator.hasNext())
//							{
//								Entry<IAttribute, AttributeModifier> entry = iterator.next();
//								AttributeModifier attributemodifier = entry.getValue();
//								AttributeModifier attributemodifier1 = new AttributeModifier(attributemodifier.getName(), potion.getAttributeModifierAmount(potioneffect.getAmplifier(), attributemodifier), attributemodifier.getOperation());
//								hashmultimap.put((entry.getKey()).getAttributeUnlocalizedName(), attributemodifier1);
//							}
//						}
//
//						if (potioneffect.getAmplifier()>0)
//							s1 = s1 + " " + I18n.format("potion.potency." + potioneffect.getAmplifier()).trim();
//						if (potioneffect.getDuration()>20)
//							s1 = s1 + " (" + Potion.getPotionDurationString(potioneffect,1) + ")";
//						if (potion.isBadEffect())
//							list.add(TextFormatting.RED + s1);
//						else
//							list.add(TextFormatting.GRAY + s1);
//					}
//				}
//				else
//				{
//					String s = I18n.format("potion.empty").trim();
//					list.add(TextFormatting.GRAY + s);
//				}
//				if(!hashmultimap.isEmpty())
//				{
//					list.add("");
//					list.add(TextFormatting.DARK_PURPLE + I18n.format("potion.effects.whenDrank"));
//					iterator1 = hashmultimap.entries().iterator();
//
//					while(iterator1.hasNext())
//					{
//						Entry entry1 = (Entry)iterator1.next();
//						AttributeModifier attributemodifier2 = (AttributeModifier)entry1.getValue();
//						double d0 = attributemodifier2.getAmount();
//						double d1;
//
//						if(attributemodifier2.getOperation()!=1 && attributemodifier2.getOperation()!=2)
//							d1 = attributemodifier2.getAmount();
//						else
//							d1 = attributemodifier2.getAmount() * 100.0D;
//
//						if (d0>0.0D)
//							list.add(TextFormatting.BLUE + I18n.format("attribute.modifier.plus." + attributemodifier2.getOperation(), ItemStack.DECIMALFORMAT.format(d1), I18n.format("attribute.name." + entry1.getKey())));
//						else if(d0<0.0D)
//						{
//							d1 *= -1.0D;
//							list.add(TextFormatting.RED + I18n.format("attribute.modifier.take." + attributemodifier2.getOperation(), ItemStack.DECIMALFORMAT.format(d1), I18n.format("attribute.name." + entry1.getKey())));
//						}
//					}
//				}
//			}
//		}
//		else if(stack.getItemDamage()==11)
//		{
//			String hexCol = Integer.toHexString(this.getColourForIEItem(stack, 1));
//			list.add(I18n.format(Lib.DESC_INFO+"bullet.flareColour", "<hexcol="+hexCol+":#"+hexCol+">"));
//		}
	}
	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		if(stack.getItemDamage() == 2)
		{
			String s = "item.immersiveengineering.bullet.";
			String key = ItemNBTHelper.getString(stack, "bullet");
			s += key;
			IBullet bullet = BulletHandler.getBullet(key);
			if(bullet != null)
				s = bullet.getUnlocalizedName(stack, s);
			return I18n.format(s+".name").trim();
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
		if(stack.getMetadata() == 2 && ItemNBTHelper.hasKey(stack, "bullet"))
		{
			IBullet bullet = BulletHandler.getBullet(ItemNBTHelper.getString(stack, "bullet"));
			if(bullet != null)
				return bullet.getColour(stack, pass);
		}
		return super.getColourForIEItem(stack, pass);
	}
	@Override
	@SideOnly(Side.CLIENT)
	public String getModelCacheKey(ItemStack stack)
	{
		if(stack.getMetadata() == 2 && ItemNBTHelper.hasKey(stack, "bullet"))
			return ItemNBTHelper.getString(stack, "bullet");
		return null;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public List<ResourceLocation> getTextures(ItemStack stack, String key)
	{
		IBullet bullet = BulletHandler.getBullet(key);
		if(bullet != null)
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
//					Vec3d vecDir = vec.addVector(player.getRNG().nextGaussian()*.1,player.getRNG().nextGaussian()*.1,player.getRNG().nextGaussian()*.1);
//					doSpawnBullet(player, vec, vecDir, type, bulletStack, electro);
//				}
//				break;
//			case 3://HE
//				doSpawnBullet(player, vec, vec, type, bulletStack, electro);
//				break;
//			case 4://dragonsbreath
//				for(int i=0; i<30; i++)
//				{
//					Vec3d vecDir = vec.addVector(player.getRNG().nextGaussian()*.1,player.getRNG().nextGaussian()*.1,player.getRNG().nextGaussian()*.1);
//					EntityRevolvershot shot = doSpawnBullet(player, vec, vecDir, type, bulletStack, electro);
//					shot.setTickLimit(10);
//					shot.setFire(3);
//				}
//				break;
//			case 5://homing
//				EntityRevolvershotHoming bullet = new EntityRevolvershotHoming(player.worldObj, player, vec.xCoord*1.5,vec.yCoord*1.5,vec.zCoord*1.5, type, bulletStack);
//				bullet.motionX = vec.xCoord;
//				bullet.motionY = vec.yCoord;
//				bullet.motionZ = vec.zCoord;
//				bullet.bulletElectro = electro;
//				player.worldObj.spawnEntityInWorld(bullet);
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
//				EntityRevolvershotFlare flare = new EntityRevolvershotFlare(player.worldObj, player, vec.xCoord*1.5,vec.yCoord*1.5,vec.zCoord*1.5, type, bulletStack);
//				flare.motionX = vec.xCoord;
//				flare.motionY = vec.yCoord;
//				flare.motionZ = vec.zCoord;
//				flare.bulletElectro = electro;
//				flare.colour = this.getColourForIEItem(bulletStack, 1);
//				flare.setColourSynced();
//				player.worldObj.spawnEntityInWorld(flare);
//				break;
//		}
//	}


	public static class PotionBullet extends BulletHandler.DamagingBullet
	{
		public PotionBullet()
		{
			super(new Function<Entity[], DamageSource>()
				  {
					  @Override
					  public DamageSource apply(Entity[] entities)
					  {
						  return IEDamageSources.causePotionDamage((EntityRevolvershot) entities[0], entities[1]);
					  }
				  },
					(float) Config.getDouble("BulletDamage-Potion"),
					BulletHandler.emptyCasing,
					new ResourceLocation("immersiveengineering:items/bullet_potion"), new ResourceLocation("immersiveengineering:items/bullet_potion_layer"));
		}

		@Override
		public String getUnlocalizedName(ItemStack cartridge, String baseName)
		{
			ItemStack pot = ItemNBTHelper.getItemStack(cartridge, "potion");
			if(pot != null)
				if(pot.getItem() instanceof ItemLingeringPotion)
					baseName += ".linger";
				else if(pot.getItem() instanceof ItemSplashPotion)
					baseName += ".splash";
			return baseName;
		}

		@Override
		public Entity getProjectile(EntityPlayer shooter, ItemStack cartridge, Entity protetile, boolean electro)
		{
			((EntityRevolvershot) protetile).bulletPotion = ItemNBTHelper.getItemStack(cartridge, "potion");
			return protetile;
		}

		@Override
		public void onHitTarget(World world, RayTraceResult target, EntityLivingBase shooter, Entity projectile, boolean headshot)
		{
			super.onHitTarget(world, target, shooter, projectile, headshot);
			EntityRevolvershot bullet = (EntityRevolvershot) projectile;
			PotionType potionType = PotionUtils.getPotionFromItem(bullet.bulletPotion);
			List<PotionEffect> effects = PotionUtils.getEffectsFromStack(bullet.bulletPotion);
			if(effects != null)
				if(bullet.bulletPotion.getItem() instanceof ItemLingeringPotion)
				{
					EntityAreaEffectCloud entityareaeffectcloud = new EntityAreaEffectCloud(bullet.worldObj, bullet.posX, bullet.posY, bullet.posZ);
					entityareaeffectcloud.setOwner(shooter);
					entityareaeffectcloud.setRadius(3.0F);
					entityareaeffectcloud.setRadiusOnUse(-0.5F);
					entityareaeffectcloud.setWaitTime(10);
					entityareaeffectcloud.setRadiusPerTick(-entityareaeffectcloud.getRadius() / (float) entityareaeffectcloud.getDuration());
					entityareaeffectcloud.setPotion(potionType);
					for(PotionEffect potioneffect : effects)
						entityareaeffectcloud.addEffect(new PotionEffect(potioneffect.getPotion(), potioneffect.getDuration(), potioneffect.getAmplifier()));
					bullet.worldObj.spawnEntityInWorld(entityareaeffectcloud);
				} else if(bullet.bulletPotion.getItem() instanceof ItemSplashPotion)
				{
					List<EntityLivingBase> livingEntities = bullet.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, bullet.getEntityBoundingBox().expand(4.0D, 2.0D, 4.0D));
					if(livingEntities != null && !livingEntities.isEmpty())
						for(EntityLivingBase living : livingEntities)
							if(living.canBeHitWithPotion())
							{
								double dist = bullet.getDistanceSqToEntity(living);
								if(dist < 16D)
								{
									double dist2 = 1 - Math.sqrt(dist) / 4D;
									if(living == target.entityHit)
										dist2 = 1D;
									for(PotionEffect p : effects)
										if(p.getPotion().isInstant())
											p.getPotion().affectEntity(bullet, shooter, living, p.getAmplifier(), dist2);
										else
										{
											int j = (int) (dist2 * p.getDuration() + .5D);
											if(j > 20)
												living.addPotionEffect(new PotionEffect(p.getPotion(), j, p.getAmplifier()));
										}
								}
							}

				} else if(target.entityHit instanceof EntityLivingBase)
					for(PotionEffect p : effects)
					{
						if(p.getDuration() < 1)
							p = new PotionEffect(p.getPotion(), 1);
						((EntityLivingBase) target.entityHit).addPotionEffect(p);
					}
			world.playEvent(2002, new BlockPos(bullet), PotionType.getID(potionType));
		}

		@Override
		public int getColour(ItemStack stack, int layer)
		{
			if(layer == 1)
			{
				ItemStack pot = ItemNBTHelper.getItemStack(stack, "potion");
				if(pot != null)
					return PotionUtils.getPotionColorFromEffectList(PotionUtils.getEffectsFromStack(pot));
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
		public Entity getProjectile(EntityPlayer shooter, ItemStack cartridge, Entity protetile, boolean electro)
		{
			Vec3d vec = shooter.getLookVec();
			EntityRevolvershotFlare flare = new EntityRevolvershotFlare(shooter.worldObj, shooter, vec.xCoord * 1.5, vec.yCoord * 1.5, vec.zCoord * 1.5, "flare", cartridge);
			flare.motionX = vec.xCoord;
			flare.motionY = vec.yCoord;
			flare.motionZ = vec.zCoord;
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
		public int getColour(ItemStack stack, int layer)
		{
			if(layer != 1)
				return 0xffffffff;
			return ItemNBTHelper.hasKey(stack, "flareColour") ? ItemNBTHelper.getInt(stack, "flareColour") : 0xcc2e06;
		}
	}

}