/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Item;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderRegistryEntry;
import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.api.utils.ItemUtils;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.client.render.IEOBJItemRenderer;
import blusunrize.immersiveengineering.common.entities.RevolvershotEntity;
import blusunrize.immersiveengineering.common.gui.IEContainerTypes;
import blusunrize.immersiveengineering.common.gui.IEContainerTypes.ItemContainerType;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.gui.RevolverContainer;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IBulletContainer;
import blusunrize.immersiveengineering.common.network.MessageSpeedloaderSync;
import blusunrize.immersiveengineering.common.util.*;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoublePredicate;
import java.util.function.Function;
import java.util.function.Supplier;

public class RevolverItem extends UpgradeableToolItem implements IOBJModelCallback<ItemStack>, ITool, IBulletContainer
{
	public RevolverItem()
	{
		super(withIEOBJRender().stacksTo(1).setISTER(() -> () -> IEOBJItemRenderer.INSTANCE), "REVOLVER");
	}

	public static UUID speedModUUID = Utils.generateNewUUID();
	public static UUID luckModUUID = Utils.generateNewUUID();
	public static HashMap<String, TextureAtlasSprite> revolverIcons = new HashMap<>();
	public static TextureAtlasSprite revolverDefaultTexture;

	public static void addRevolverTextures(TextureStitchEvent.Pre evt)
	{
		evt.addSprite(new ResourceLocation(ImmersiveEngineering.MODID, "revolvers/revolver"));
		for(String key : specialRevolversByTag.keySet())
			if(!key.isEmpty()&&!specialRevolversByTag.get(key).tag.isEmpty())
			{
				int split = key.lastIndexOf("_");
				if(split < 0)
					split = key.length();
				evt.addSprite(new ResourceLocation(ImmersiveEngineering.MODID, "revolvers/revolver_"+key.substring(0, split).toLowerCase(Locale.US)));
			}
	}

	public static void retrieveRevolverTextures(TextureAtlas map)
	{
		revolverDefaultTexture = map.getSprite(new ResourceLocation("immersiveengineering:revolvers/revolver"));
		for(String key : specialRevolversByTag.keySet())
			if(!key.isEmpty()&&!specialRevolversByTag.get(key).tag.isEmpty())
			{
				int split = key.lastIndexOf("_");
				if(split < 0)
					split = key.length();
				revolverIcons.put(key, map.getSprite(new ResourceLocation("immersiveengineering:revolvers/revolver_"+key.substring(0, split).toLowerCase(Locale.US))));
			}
	}

	/* ------------- CORE ITEM METHODS ------------- */

	@Override
	public boolean isTool(ItemStack item)
	{
		return true;
	}

	@Nullable
	@Override
	public CompoundTag getShareTag(ItemStack stack)
	{
		CompoundTag ret = super.getShareTag(stack);
		if(ret==null)
			ret = new CompoundTag();
		else
			ret = ret.copy();
		final CompoundTag retFinal = ret;
		stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(handler ->
		{
			NonNullList<ItemStack> bullets = NonNullList.withSize(getBulletCount(stack), ItemStack.EMPTY);
			for(int i = 0; i < getBulletCount(stack); i++)
				bullets.set(i, handler.getStackInSlot(i));
			retFinal.put("bullets", ContainerHelper.saveAllItems(new CompoundTag(), bullets));
		});
		return retFinal;
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt)
	{
		if(!stack.isEmpty())
			return new IEItemStackHandler(stack)
			{
				final LazyOptional<ShaderWrapper_Item> shaders = CapabilityUtils.constantOptional(
						new ShaderWrapper_Item(new ResourceLocation(ImmersiveEngineering.MODID, "revolver"), stack));

				@Nonnull
				@Override
				public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing)
				{
					if(capability==CapabilityShader.SHADER_CAPABILITY)
						return shaders.cast();
					return super.getCapability(capability, facing);
				}
			};
		return null;
	}

	/* ------------- INTERNAL INVENTORY ------------- */

	@Override
	public int getSlotCount()
	{
		return 18+2+1;
	}

	@Override
	public Slot[] getWorkbenchSlots(AbstractContainerMenu container, ItemStack stack, Supplier<Level> getWorld, Supplier<Player> getPlayer)
	{
		IItemHandler inv = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
				.orElseThrow(RuntimeException::new);
		return new Slot[]
				{
						new IESlot.Upgrades(container, inv, 18+0, 80, 32, "REVOLVER", stack, true, getWorld, getPlayer),
						new IESlot.Upgrades(container, inv, 18+1, 100, 32, "REVOLVER", stack, true, getWorld, getPlayer)
				};
	}

	@Override
	public boolean canModify(ItemStack stack)
	{
		return true;
	}

	@Override
	public void removeFromWorkbench(Player player, ItemStack stack)
	{
		stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
				.ifPresent(inv -> {
					if(!inv.getStackInSlot(18).isEmpty()&&!inv.getStackInSlot(19).isEmpty())
						Utils.unlockIEAdvancement(player, "main/upgrade_revolver");
				});
	}

	/* ------------- NAME, TOOLTIP, SUB-ITEMS ------------- */

	@Nonnull
	@Override
	public String getDescriptionId(ItemStack stack)
	{
		String tag = getRevolverDisplayTag(stack);
		if(!tag.isEmpty())
			return this.getDescriptionId()+"."+tag;
		return super.getDescriptionId(stack);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		String tag = getRevolverDisplayTag(stack);
		if(!tag.isEmpty())
			list.add(new TranslatableComponent(Lib.DESC_FLAVOUR+"revolver."+tag));
		else if(ItemNBTHelper.hasKey(stack, "flavour"))
			list.add(new TranslatableComponent(Lib.DESC_FLAVOUR+"revolver."+ItemNBTHelper.getString(stack, "flavour")));
		else
			list.add(new TranslatableComponent(Lib.DESC_FLAVOUR+"revolver"));

		CompoundTag perks = getPerks(stack);
		for(String key : perks.getAllKeys())
		{
			RevolverPerk perk = RevolverPerk.get(key);
			if(perk!=null)
				list.add(new TextComponent("  ").append(perk.getDisplayString(perks.getDouble(key))));
		}
	}

	/* ------------- ATTRIBUTES, UPDATE, RIGHTCLICK ------------- */

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@Nonnull EquipmentSlot slot, ItemStack stack)
	{
		Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		if(slot==EquipmentSlot.MAINHAND)
		{
			if(getUpgrades(stack).getBoolean("fancyAnimation"))
				builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2, Operation.ADDITION));
			double melee = getUpgradeValue_d(stack, "melee");
			if(melee!=0)
			{
				builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", melee, Operation.ADDITION));
				builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.4000000953674316D, Operation.ADDITION));
			}
		}
		if(slot.getType()==Type.HAND)
		{
			double speed = getUpgradeValue_d(stack, "speed");
			if(speed!=0)
				builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(speedModUUID, "Weapon modifier", speed, Operation.MULTIPLY_BASE));

			double luck = getUpgradeValue_d(stack, RevolverPerk.LUCK.getNBTKey());
			if(luck!=0)
				builder.put(Attributes.LUCK, new AttributeModifier(luckModUUID, "Weapon modifier", luck, Operation.ADDITION));
		}
		return builder.build();
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity ent, int slot, boolean inHand)
	{
		super.inventoryTick(stack, world, ent, slot, inHand);
		{
			if(ItemNBTHelper.hasKey(stack, "reload"))
			{
				int reload = ItemNBTHelper.getInt(stack, "reload")-1;
				if(reload <= 0)
					ItemNBTHelper.remove(stack, "reload");
				else
					ItemNBTHelper.putInt(stack, "reload", reload);
			}
			if(ItemNBTHelper.hasKey(stack, "cooldown"))
			{
				int cooldown = ItemNBTHelper.getInt(stack, "cooldown")-1;
				if(cooldown <= 0)
					ItemNBTHelper.remove(stack, "cooldown");
				else
					ItemNBTHelper.putInt(stack, "cooldown", cooldown);
			}
		}
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack)
	{
		return UseAnim.BOW;
	}

	@Nonnull
	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, @Nonnull InteractionHand hand)
	{
		ItemStack revolver = player.getItemInHand(hand);
		if(!world.isClientSide)
		{
			if(player.isShiftKeyDown())
			{
				openGui(player, hand);
				return new InteractionResultHolder<>(InteractionResult.SUCCESS, revolver);
			}
			else if(player.getAttackStrengthScale(1) >= 1)
			{
				if(this.getUpgrades(revolver).getBoolean("nerf"))
					world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1f, 0.6f);
				else
				{
					if(getShootCooldown(revolver) > 0||ItemNBTHelper.hasKey(revolver, "reload"))
						return new InteractionResultHolder<>(InteractionResult.PASS, revolver);

					NonNullList<ItemStack> bullets = getBullets(revolver, false);

					if(isEmpty(revolver, false))
						for(int i = 0; i < player.inventory.getContainerSize(); i++)
						{
							ItemStack stack = player.inventory.getItem(i);
							if(stack.getItem() instanceof SpeedloaderItem&&!((SpeedloaderItem)stack.getItem()).isEmpty(stack))
							{
								for(ItemStack b : bullets)
									if(!b.isEmpty())
										world.addFreshEntity(new ItemEntity(world, player.getX(), player.getY(), player.getZ(), b));
								setBullets(revolver, ((SpeedloaderItem)stack.getItem()).getContainedItems(stack), true);
								((SpeedloaderItem)stack.getItem()).setContainedItems(stack, NonNullList.withSize(8, ItemStack.EMPTY));
								player.inventory.setChanged();
								if(player instanceof ServerPlayer)
									ImmersiveEngineering.packetHandler.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)player),
											new MessageSpeedloaderSync(i, hand));

								ItemNBTHelper.putInt(revolver, "reload", 60);
								return new InteractionResultHolder<>(InteractionResult.SUCCESS, revolver);
							}
						}

					if(!ItemNBTHelper.hasKey(revolver, "reload"))
					{
						Item bullet0 = bullets.get(0).getItem();
						if(bullet0 instanceof BulletItem)
						{
							IBullet bullet = ((BulletItem)bullet0).getType();
							if(bullet!=null)
							{
								Vec3 vec = player.getLookAngle();
								boolean electro = getUpgrades(revolver).getBoolean("electro");
								int count = bullet.getProjectileCount(player);
								if(count==1)
								{
									Entity entBullet = getBullet(player, vec, bullet, electro);
									player.level.addFreshEntity(bullet.getProjectile(player, bullets.get(0), entBullet, electro));
								}
								else
									for(int i = 0; i < count; i++)
									{
										Vec3 vecDir = vec.add(player.getRandom().nextGaussian()*.1, player.getRandom().nextGaussian()*.1, player.getRandom().nextGaussian()*.1);
										Entity entBullet = getBullet(player, vecDir, bullet, electro);
										player.level.addFreshEntity(bullet.getProjectile(player, bullets.get(0), entBullet, electro));
									}
								bullets.set(0, bullet.getCasing(bullets.get(0)).copy());

								float noise = 0.5f;
								if(hasUpgradeValue(revolver, RevolverPerk.NOISE.getNBTKey()))
									noise *= (float)getUpgradeValue_d(revolver, RevolverPerk.NOISE.getNBTKey());
								Utils.attractEnemies(player, 64*noise);
								SoundEvent sound = bullet.getSound();
								if(sound==null)
									sound = IESounds.revolverFire;
								world.playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, noise, 1f);
							}
							else
								world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, 1f, 1f);
						}
						else
							world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, 1f, 1f);

						rotateCylinder(revolver, player, true, bullets);
						ItemNBTHelper.putInt(revolver, "cooldown", getMaxShootCooldown(revolver));
						return new InteractionResultHolder<>(InteractionResult.SUCCESS, revolver);
					}
				}
			}
		}
		else if(!player.isShiftKeyDown())
		{
			if(getShootCooldown(revolver) > 0||ItemNBTHelper.hasKey(revolver, "reload"))
				return new InteractionResultHolder<>(InteractionResult.PASS, revolver);
			NonNullList<ItemStack> bullets = getBullets(revolver);
			if(!bullets.get(0).isEmpty()&&bullets.get(0).getItem() instanceof BulletItem)
			{
				Triple<ItemStack, ShaderRegistryEntry, ShaderCase> shader = ShaderRegistry.getStoredShaderAndCase(revolver);
				if(shader!=null)
				{

					Vec3 pos = Utils.getLivingFrontPos(player, .75, player.getBbHeight()*.75, ItemUtils.getLivingHand(player, hand), false, 1);
					shader.getMiddle().getEffectFunction().execute(world, shader.getLeft(), revolver,
							shader.getRight().getShaderType().toString(), pos,
							Vec3.directionFromRotation(player.getRotationVector()), .125f);
				}
			}
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, revolver);
		}
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, revolver);
	}

	public int getShootCooldown(ItemStack stack)
	{
		return ItemNBTHelper.getInt(stack, "cooldown");
	}

	public int getMaxShootCooldown(ItemStack stack)
	{
		if(hasUpgradeValue(stack, RevolverPerk.COOLDOWN.getNBTKey()))
			return (int)Math.ceil(15*getUpgradeValue_d(stack, RevolverPerk.COOLDOWN.getNBTKey()));
		return 15;
	}

	/* ------------- IBulletContainer ------------- */

	@Override
	public int getBulletCount(ItemStack revolver)
	{
		return 8+this.getUpgrades(revolver).getInt("bullets");
	}

	@Override
	public NonNullList<ItemStack> getBullets(ItemStack revolver, boolean remote)
	{
		if(!remote&&isEmpty(revolver, true))
			remote = true;
		else if(remote&&(
				!ItemNBTHelper.hasKey(revolver, "bullets", NBT.TAG_LIST)
						||revolver.getOrCreateTag().getList("bullets", NBT.TAG_COMPOUND).isEmpty()
		))
			remote = false;
		if(!remote)
			return ListUtils.fromItems(this.getContainedItems(revolver).subList(0, getBulletCount(revolver)));
		else
		{
			NonNullList<ItemStack> stacks = NonNullList.withSize(getBulletCount(revolver), ItemStack.EMPTY);
			ContainerHelper.loadAllItems(revolver.getOrCreateTag().getCompound("bullets"), stacks);
			return stacks;
		}
	}

	/* ------------- BULLET UTILITY ------------- */

	private RevolvershotEntity getBullet(Player player, Vec3 vecDir, IBullet type, boolean electro)
	{
		IELogger.logger.info("Starting with motion vector {}", vecDir);
		RevolvershotEntity bullet = new RevolvershotEntity(player.level, player, vecDir.x*1.5, vecDir.y*1.5, vecDir.z*1.5, type);
		bullet.setDeltaMovement(vecDir.scale(2));
		bullet.bulletElectro = electro;
		return bullet;
	}

	public void setBullets(ItemStack revolver, NonNullList<ItemStack> bullets, boolean ignoreExtendedMag)
	{
		IItemHandlerModifiable inv = (IItemHandlerModifiable)revolver.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
				.orElseThrow(RuntimeException::new);
		for(int i = 0; i < 18; i++)
			inv.setStackInSlot(i, ItemStack.EMPTY);
		if(ignoreExtendedMag&&getUpgrades(revolver).getInt("bullets") > 0)
			for(int i = 0; i < bullets.size(); i++)
				inv.setStackInSlot(i < 2?i: i+getUpgrades(revolver).getInt("bullets"), bullets.get(i));
		else
			for(int i = 0; i < bullets.size(); i++)
				inv.setStackInSlot(i, bullets.get(i));
	}

	public void rotateCylinder(ItemStack revolver, Player player, boolean forward, NonNullList<ItemStack> bullets)
	{
		NonNullList<ItemStack> cycled = NonNullList.withSize(getBulletCount(revolver), ItemStack.EMPTY);
		int offset = forward?-1: 1;
		for(int i = 0; i < cycled.size(); i++)
			cycled.set((i+offset+cycled.size())%cycled.size(), bullets.get(i));
		setBullets(revolver, cycled, false);
		player.inventory.setChanged();
	}

	public void rotateCylinder(ItemStack revolver, Player player, boolean forward)
	{
		NonNullList<ItemStack> bullets = getBullets(revolver);
		rotateCylinder(revolver, player, forward, bullets);
	}

	public boolean isEmpty(ItemStack stack, boolean allowCasing)
	{
		LazyOptional<IItemHandler> invCap = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		return invCap.map(inv -> {
			for(int i = 0; i < inv.getSlots(); i++)
			{
				ItemStack b = inv.getStackInSlot(i);
				boolean isValid = true;
				if(!allowCasing)
					isValid = b.getItem() instanceof BulletItem;
				if(!b.isEmpty()&&isValid)
					return false;
			}
			return true;
		}).orElse(true);
	}

	/* ------------- UPGRADES & PERKS ------------- */

	@Override
	public CompoundTag getUpgradeBase(ItemStack stack)
	{
		return ItemNBTHelper.getTagCompound(stack, "baseUpgrades");
	}

	public String getRevolverDisplayTag(ItemStack revolver)
	{
		String tag = ItemNBTHelper.getString(revolver, "elite");
		if(!tag.isEmpty())
		{
			int split = tag.lastIndexOf("_");
			if(split < 0)
				split = tag.length();
			return tag.substring(0, split);
		}
		return "";
	}

	public CompoundTag getPerks(ItemStack stack)
	{
		return ItemNBTHelper.getTagCompound(stack, "perks");
	}

	public boolean hasUpgradeValue(ItemStack stack, String key)
	{
		return getUpgrades(stack).contains(key)||getPerks(stack).contains(key);
	}

	public double getUpgradeValue_d(ItemStack stack, String key)
	{
		return getUpgrades(stack).getDouble(key)+getPerks(stack).getDouble(key);
	}

	/* ------------- CRAFTING ------------- */

	@Override
	public void onCraftedBy(ItemStack stack, Level world, Player player)
	{
		if(stack.isEmpty()||player==null)
			return;

		String uuid = player.getUUID().toString();
		if(specialRevolvers.containsKey(uuid))
		{
			ArrayList<SpecialRevolver> list = new ArrayList<>(specialRevolvers.get(uuid));
			if(!list.isEmpty())
			{
				list.add(null);
				String existingTag = ItemNBTHelper.getString(stack, "elite");
				if(existingTag.isEmpty())
					applySpecialCrafting(stack, list.get(0));
				else
				{
					int i = 0;
					for(; i < list.size(); i++)
						if(list.get(i)!=null&&existingTag.equals(list.get(i).tag))
							break;
					int next = (i+1)%list.size();
					applySpecialCrafting(stack, list.get(next));
				}
			}
		}
		this.recalculateUpgrades(stack, world, player);
	}

	public void applySpecialCrafting(ItemStack stack, SpecialRevolver r)
	{
		if(r==null)
		{
			ItemNBTHelper.remove(stack, "elite");
			ItemNBTHelper.remove(stack, "flavour");
			ItemNBTHelper.remove(stack, "baseUpgrades");
			return;
		}
		if(r.tag!=null&&!r.tag.isEmpty())
			ItemNBTHelper.putString(stack, "elite", r.tag);
		if(r.flavour!=null&&!r.flavour.isEmpty())
			ItemNBTHelper.putString(stack, "flavour", r.flavour);
		CompoundTag baseUpgrades = new CompoundTag();
		for(Map.Entry<String, Object> e : r.baseUpgrades.entrySet())
		{
			if(e.getValue() instanceof Boolean)
				baseUpgrades.putBoolean(e.getKey(), (Boolean)e.getValue());
			else if(e.getValue() instanceof Integer)
				baseUpgrades.putInt(e.getKey(), (Integer)e.getValue());
			else if(e.getValue() instanceof Float)
				baseUpgrades.putDouble(e.getKey(), (Float)e.getValue());
			else if(e.getValue() instanceof Double)
				baseUpgrades.putDouble(e.getKey(), (Double)e.getValue());
			else if(e.getValue() instanceof String)
				baseUpgrades.putString(e.getKey(), (String)e.getValue());
		}
		ItemNBTHelper.setTagCompound(stack, "baseUpgrades", baseUpgrades);
	}

	/* ------------- RENDERING ------------- */

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
	{
		if(slotChanged)
			return true;

		LazyOptional<ShaderWrapper> wrapperOld = oldStack.getCapability(CapabilityShader.SHADER_CAPABILITY);
		Optional<Boolean> sameShader = wrapperOld.map(wOld -> {
			LazyOptional<ShaderWrapper> wrapperNew = newStack.getCapability(CapabilityShader.SHADER_CAPABILITY);
			return wrapperNew.map(w -> ItemStack.matches(wOld.getShaderItem(), w.getShaderItem()))
					.orElse(true);
		});
		if(!sameShader.orElse(true))
			return true;
		if(ItemNBTHelper.hasKey(oldStack, "elite")||ItemNBTHelper.hasKey(newStack, "elite"))
			return !ItemNBTHelper.getString(oldStack, "elite").equals(ItemNBTHelper.getString(newStack, "elite"));

		return false;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public TextureAtlasSprite getTextureReplacement(ItemStack stack, String group, String material)
	{
		String tag = ItemNBTHelper.getString(stack, "elite");
		if(!tag.isEmpty())
			return revolverIcons.get(tag);
		else
			return revolverDefaultTexture;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean shouldRenderGroup(ItemStack stack, String group)
	{
		if(group.equals("frame")||group.equals("cylinder")||group.equals("barrel")||group.equals("cosmetic_compensator"))
			return true;

		HashSet<String> render = new HashSet<String>();
		String tag = ItemNBTHelper.getString(stack, "elite");
		String flavour = ItemNBTHelper.getString(stack, "flavour");
		if(!tag.isEmpty()&&specialRevolversByTag.containsKey(tag))
		{
			SpecialRevolver r = specialRevolversByTag.get(tag);
			if(r!=null&&r.renderAdditions!=null)
				Collections.addAll(render, r.renderAdditions);
		}
		else if(!flavour.isEmpty()&&specialRevolversByTag.containsKey(flavour))
		{
			SpecialRevolver r = specialRevolversByTag.get(flavour);
			if(r!=null&&r.renderAdditions!=null)
				Collections.addAll(render, r.renderAdditions);
		}
		CompoundTag upgrades = this.getUpgrades(stack);
		if(upgrades.getInt("bullets") > 0&&!render.contains("dev_mag"))
			render.add("player_mag");
		if(upgrades.getDouble("melee") > 0&&!render.contains("dev_bayonet"))
		{
			render.add("bayonet_attachment");
			render.add("player_bayonet");
		}
		if(upgrades.getBoolean("electro"))
		{
			render.add("player_electro_0");
			render.add("player_electro_1");
		}
		return render.contains(group);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void handlePerspective(ItemStack stack, TransformType cameraTransformType, PoseStack mat, @Nullable LivingEntity entity)
	{
		if(entity instanceof Player&&(cameraTransformType==TransformType.FIRST_PERSON_RIGHT_HAND||cameraTransformType==TransformType.FIRST_PERSON_LEFT_HAND||cameraTransformType==TransformType.THIRD_PERSON_RIGHT_HAND||cameraTransformType==TransformType.THIRD_PERSON_LEFT_HAND))
		{
			boolean main = (cameraTransformType==TransformType.FIRST_PERSON_RIGHT_HAND||cameraTransformType==TransformType.THIRD_PERSON_RIGHT_HAND)==(entity.getMainArm()==HumanoidArm.RIGHT);
			boolean left = cameraTransformType==TransformType.FIRST_PERSON_LEFT_HAND||cameraTransformType==TransformType.THIRD_PERSON_LEFT_HAND;
			if(getUpgrades(stack).getBoolean("fancyAnimation")&&main)
			{
				float f = ((Player)entity).getAttackStrengthScale(ClientUtils.mc().getFrameTime());
				if(f < 1)
				{
					float angle = f*-6.28318f;
					if(left)
						angle *= -1;
					mat.translate(0, 1.5-f, 0);
					mat.mulPose(new Quaternion(new Vector3f(0, 0, 1), angle, false));
				}
			}

			//Re-grab stack because the other one doesn't do reloads properly
			stack = main?entity.getMainHandItem(): entity.getOffhandItem();
			if(ItemNBTHelper.hasKey(stack, "reload"))
			{
				float f = 3-ItemNBTHelper.getInt(stack, "reload")/20f; //Reload time in seconds, for coordinating with audio
				if(f > .35&&f < 1.95)
					if(f < .5)
					{
						mat.translate((.35-f)*2, 0, 0);
						mat.mulPose(new Quaternion(new Vector3f(0, 0, left?-1: 1), 2.64F*(f-.35F), false));
					}
					else if(f < .6)
					{
						mat.translate((f-.5)*6, (.5-f)*1, 0);
						mat.mulPose(new Quaternion(new Vector3f(0, 0, left?-1: 1), .87266F, false));
					}
					else if(f < 1.7)
					{
						mat.translate(0, -.6, 0);
						mat.mulPose(new Quaternion(new Vector3f(0, 0, left?-1: 1), .87266F, false));
					}
					else if(f < 1.8)
					{
						mat.translate((1.8-f)*6, (f-1.8)*1, 0);
						mat.mulPose(new Quaternion(new Vector3f(0, 0, left?-1: 1), .87266F, false));
					}
					else
					{
						mat.translate((f-1.95f)*2, 0, 0);
						mat.mulPose(new Quaternion(new Vector3f(0, 0, left?-1: 1), 2.64F*(1.95F-f), false));
					}
			}
			else if(((Player)entity).containerMenu instanceof RevolverContainer)
			{
				mat.translate(left?.4: -.4, .4, 0);
				mat.mulPose(new Quaternion(new Vector3f(0, 0, left?-1: 1), .87266F, false));
			}
		}
	}

	private static final String[][] groups = {{"frame"}, {"cylinder"}};

	@OnlyIn(Dist.CLIENT)
	@Override
	public String[][] getSpecialGroups(ItemStack stack, TransformType transform, LivingEntity entity)
	{
		return groups;
	}

	@OnlyIn(Dist.CLIENT)
	private static Transformation matOpen;
	@OnlyIn(Dist.CLIENT)
	private static Transformation matClose;
	@OnlyIn(Dist.CLIENT)
	private static Transformation matCylinder;

	@Nonnull
	@Override
	@OnlyIn(Dist.CLIENT)
	public Transformation getTransformForGroups(ItemStack stack, String[] groups, TransformType transform, LivingEntity entity,
													  float partialTicks)
	{
		if(matOpen==null)
			matOpen = new Transformation(new Vector3f(-.625F, .25F, 0), new Quaternion(0, 0, -.87266f, false), null, null);
		if(matClose==null)
			matClose = new Transformation(new Vector3f(-.625F, .25F, 0), null, null, null);
		if(matCylinder==null)
			matCylinder = new Transformation(new Vector3f(0, .6875F, 0), null, null, null);
		if(entity instanceof Player&&(transform==TransformType.FIRST_PERSON_RIGHT_HAND||transform==TransformType.FIRST_PERSON_LEFT_HAND||transform==TransformType.THIRD_PERSON_RIGHT_HAND||transform==TransformType.THIRD_PERSON_LEFT_HAND))
		{
			boolean main = (transform==TransformType.FIRST_PERSON_RIGHT_HAND||transform==TransformType.THIRD_PERSON_RIGHT_HAND)==(entity.getMainArm()==HumanoidArm.RIGHT);
			boolean left = transform==TransformType.FIRST_PERSON_LEFT_HAND||transform==TransformType.THIRD_PERSON_LEFT_HAND;
			//Re-grab stack because the other one doesn't do reloads properly
			stack = main?entity.getMainHandItem(): entity.getOffhandItem();
			if(ItemNBTHelper.hasKey(stack, "reload"))
			{
				float f = 3-ItemNBTHelper.getInt(stack, "reload")/20f; //Reload time in seconds, for coordinating with audio
				if("frame".equals(groups[0]))
				{
					if(f < .35||f > 1.95)
						return matClose;
					else if(f < .5)
						return new Transformation(
								new Vector3f(-.625f, .25f, 0),
								new Quaternion(0, 0, -2.64F*(f-.35F), false),
								null, null);
					else if(f < 1.8)
						return matOpen;
					else
						return new Transformation(
								new Vector3f(-.625f, .25f, 0),
								new Quaternion(0, 0, -2.64f*(1.95f-f), false),
								null, null);
				}
				else if(f > 2.5&&f < 2.9)
				{
					float angle = (left?-1: 1)*-15.70795f*(f-2.5f);
					return new Transformation(
							new Vector3f(0, .6875f, 0),
							new Quaternion(angle, 0, 0, false),
							null, null);
				}
			}
			else if("frame".equals(groups[0])&&((Player)entity).containerMenu instanceof RevolverContainer)
				return matOpen;
		}
		return "frame".equals(groups[0])?matClose: matCylinder;
	}

	@Nullable
	@Override
	protected ItemContainerType<?> getContainerType()
	{
		return IEContainerTypes.REVOLVER;
	}

	/* ------------- INNER CLASSES ------------- */

	public static final ArrayListMultimap<String, SpecialRevolver> specialRevolvers = ArrayListMultimap.create();
	public static final Map<String, SpecialRevolver> specialRevolversByTag = new HashMap<>();

	public static class SpecialRevolver
	{
		public final String[] uuid;
		public final String tag;
		public final String flavour;
		public final HashMap<String, Object> baseUpgrades;
		public final String[] renderAdditions;

		public SpecialRevolver(String[] uuid, String tag, String flavour, HashMap<String, Object> baseUpgrades, String[] renderAdditions)
		{
			this.uuid = uuid;
			this.tag = tag;
			this.flavour = flavour;
			this.baseUpgrades = baseUpgrades;
			this.renderAdditions = renderAdditions;
		}
	}

	@ParametersAreNonnullByDefault
	public enum RevolverPerk
	{
		COOLDOWN(f -> f > 1,
				f -> Utils.NUMBERFORMAT_PREFIXED.format((1-f)*100),
				(l, r) -> l*r,
				1, -0.75, -0.05),
		NOISE(f -> f > 1,
				f -> Utils.NUMBERFORMAT_PREFIXED.format((f-1)*100),
				(l, r) -> l*r,
				1, -.9, -0.1),
		LUCK(f -> f < 0,
				f -> Utils.NUMBERFORMAT_PREFIXED.format(f*100),
				(l, r) -> l+r,
				0, 3, 0.5);

		private final DoublePredicate isBadValue;
		private final Function<Double, String> valueFormatter;
		private final DoubleBinaryOperator valueConcat;
		private final double generate_median;
		private final double generate_deviation;
		private final double generate_luckScale;

		RevolverPerk(DoublePredicate isBadValue, Function<Double, String> valueFormatter, DoubleBinaryOperator valueConcat, double generate_median, double generate_deviation, double generate_luckScale)
		{
			this.isBadValue = isBadValue;
			this.valueFormatter = valueFormatter;
			this.valueConcat = valueConcat;
			this.generate_median = generate_median;
			this.generate_deviation = generate_deviation;
			this.generate_luckScale = generate_luckScale;
		}

		public String getNBTKey()
		{
			return name().toLowerCase(Locale.US);
		}

		public Component getDisplayString(double value)
		{
			String key = Lib.DESC_INFO+"revolver.perk."+this.toString();
			return new TranslatableComponent(key, valueFormatter.apply(value))
					.withStyle(isBadValue.test(value)?ChatFormatting.RED: ChatFormatting.BLUE);
		}

		public static Component getFormattedName(Component name, CompoundTag perksTag)
		{
			double averageTier = 0;
			for(String key : perksTag.getAllKeys())
			{
				RevolverItem.RevolverPerk perk = RevolverItem.RevolverPerk.get(key);
				double value = perksTag.getDouble(key);
				double dTier = (value-perk.generate_median)/perk.generate_deviation*3;
				averageTier += dTier;
				int iTier = (int)Mth.clamp((dTier < 0?Math.floor(dTier): Math.ceil(dTier)), -3, 3);
				String translate = Lib.DESC_INFO+"revolver.perk."+perk.name().toLowerCase(Locale.US)+".tier"+iTier;
				name = new TranslatableComponent(translate).append(name);
			}

			int rarityTier = (int)Math.ceil(Mth.clamp(averageTier+3, 0, 6)/6*5);
			Rarity rarity = rarityTier==5?Lib.RARITY_MASTERWORK: rarityTier==4?Rarity.EPIC: rarityTier==3?Rarity.RARE: rarityTier==2?Rarity.UNCOMMON: Rarity.COMMON;
			return name.copy().withStyle(rarity.color);
		}

		public static int calculateTier(CompoundTag perksTag)
		{
			double averageTier = 0;
			for(String key : perksTag.getAllKeys())
			{
				RevolverItem.RevolverPerk perk = RevolverItem.RevolverPerk.get(key);
				double value = perksTag.getDouble(key);
				double dTier = (value-perk.generate_median)/perk.generate_deviation*3;
				averageTier += dTier;
			}
			return (int)Math.ceil(Mth.clamp(averageTier+3, 0, 6)/6*5);
		}

		public double concat(double left, double right)
		{
			return this.valueConcat.applyAsDouble(left, right);
		}

		public double generateValue(Random rand, boolean isBad, float luck)
		{
			double d = Utils.generateLuckInfluencedDouble(generate_median, generate_deviation, luck, rand, isBad, generate_luckScale);
			int i = (int)(d*100);
			d = i/100d;
			return d;
		}

		@Override
		public String toString()
		{
			return this.name().toLowerCase(Locale.US);
		}

		public static RevolverPerk get(String name)
		{
			try
			{
				return valueOf(name.toUpperCase(Locale.US));
			} catch(Exception e)
			{
				return null;
			}
		}

		public static RevolverPerk getRandom(Random rand)
		{
			int i = rand.nextInt(values().length);
			return values()[i];
		}

		public static CompoundTag generatePerkSet(Random rand, float luck)
		{
			RevolverPerk goodPerk = RevolverPerk.getRandom(rand);
			RevolverPerk badPerk = RevolverPerk.LUCK;
			//RevolverPerk.getRandom(rand);
			double val = goodPerk.generateValue(rand, false, luck);

			CompoundTag perkCompound = new CompoundTag();
			if(goodPerk==badPerk)
				val = (val+badPerk.generateValue(rand, true, luck))/2;
			else
				perkCompound.putDouble(badPerk.getNBTKey(), badPerk.generateValue(rand, true, luck));
			perkCompound.putDouble(goodPerk.getNBTKey(), val);

			return perkCompound;
		}
	}

}