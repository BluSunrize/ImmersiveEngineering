/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Item;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderRegistryEntry;
import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.client.render.IEOBJItemRenderer;
import blusunrize.immersiveengineering.common.entities.RevolvershotEntity;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.gui.RevolverContainer;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IBulletContainer;
import blusunrize.immersiveengineering.common.network.MessageSpeedloaderSync;
import blusunrize.immersiveengineering.common.util.*;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

public class RevolverItem extends UpgradeableToolItem implements IOBJModelCallback<ItemStack>, ITool, IBulletContainer
{
	public RevolverItem()
	{
		super("revolver", withIEOBJRender().maxStackSize(1).setTEISR(() -> () -> IEOBJItemRenderer.INSTANCE), "REVOLVER");
	}

	public static UUID speedModUUID = Utils.generateNewUUID();
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
				evt.addSprite(new ResourceLocation(ImmersiveEngineering.MODID, "revolvers/revolver_"+key.substring(0, split).toLowerCase()));
			}
	}

	public static void retrieveRevolverTextures(AtlasTexture map)
	{
		revolverDefaultTexture = map.getAtlasSprite("immersiveengineering:revolvers/revolver");
		for(String key : specialRevolversByTag.keySet())
			if(!key.isEmpty()&&!specialRevolversByTag.get(key).tag.isEmpty())
			{
				int split = key.lastIndexOf("_");
				if(split < 0)
					split = key.length();
				revolverIcons.put(key, map.getAtlasSprite("immersiveengineering:revolvers/revolver_"+key.substring(0, split).toLowerCase()));
			}
	}

	@Override
	public int getSlotCount(ItemStack stack)
	{
		return 18+2+1;
	}

	@Override
	public Slot[] getWorkbenchSlots(Container container, ItemStack stack, Supplier<World> getWorld)
	{
		IItemHandler inv = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
				.orElseThrow(RuntimeException::new);
		return new Slot[]
				{
						new IESlot.Upgrades(container, inv, 18+0, 80, 32, "REVOLVER", stack, true, getWorld),
						new IESlot.Upgrades(container, inv, 18+1, 100, 32, "REVOLVER", stack, true, getWorld)
				};
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
	{
		if(slotChanged)
			return true;

		LazyOptional<ShaderWrapper> wrapperOld = oldStack.getCapability(CapabilityShader.SHADER_CAPABILITY);
		LazyOptional<Boolean> sameShader = wrapperOld.map(wOld->{
			LazyOptional<ShaderWrapper> wrapperNew = newStack.getCapability(CapabilityShader.SHADER_CAPABILITY);
			return wrapperNew.map(w->ItemStack.areItemStacksEqual(wOld.getShaderItem(), w.getShaderItem()))
					.orElse(true);
		});
		if (!sameShader.orElse(true))
			return true;
		if(ItemNBTHelper.hasKey(oldStack, "elite")||ItemNBTHelper.hasKey(newStack, "elite"))
			return !ItemNBTHelper.getString(oldStack, "elite").equals(ItemNBTHelper.getString(newStack, "elite"));

		return false;
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt)
	{
		if(!stack.isEmpty())
			return new IEItemStackHandler(stack)
			{
				final LazyOptional<ShaderWrapper_Item> shaders = ApiUtils.constantOptional(
						new ShaderWrapper_Item(new ResourceLocation("immersiveengineering", "revolver"), stack));

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

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
	{
		String tag = getRevolverDisplayTag(stack);
		if(!tag.isEmpty())
			list.add(new TranslationTextComponent(Lib.DESC_FLAVOUR+"revolver."+tag));
		else if(ItemNBTHelper.hasKey(stack, "flavour"))
			list.add(new TranslationTextComponent(Lib.DESC_FLAVOUR+"revolver."+ItemNBTHelper.getString(stack, "flavour")));
		else
			list.add(new TranslationTextComponent(Lib.DESC_FLAVOUR+"revolver"));
	}

	@Nonnull
	@Override
	public String getTranslationKey(ItemStack stack)
	{
		String tag = getRevolverDisplayTag(stack);
		if(!tag.isEmpty())
			return this.getTranslationKey()+"."+tag;
		return super.getTranslationKey(stack);
	}

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(@Nonnull EquipmentSlotType slot, ItemStack stack)
	{
		Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);
		if(slot==EquipmentSlotType.MAINHAND)
		{
			if(getUpgrades(stack).getBoolean("fancyAnimation"))
				multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2, Operation.ADDITION));
			double melee = getUpgrades(stack).getDouble("melee");
			if(melee!=0)
			{
				multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", melee, Operation.ADDITION));
				multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D, Operation.ADDITION));
			}
			double speed = getUpgrades(stack).getDouble("speed");
			if(speed!=0)
				multimap.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(speedModUUID, "Weapon modifier", speed, Operation.MULTIPLY_BASE));
		}
		return multimap;
	}

	@Override
	public UseAction getUseAction(ItemStack p_77661_1_)
	{
		return UseAction.BOW;
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity ent, int slot, boolean inHand)
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

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand)
	{
		ItemStack revolver = player.getHeldItem(hand);
		if(!world.isRemote)
		{
			if(player.isSneaking())
			{
				openGui(player, hand==Hand.MAIN_HAND?EquipmentSlotType.MAINHAND: EquipmentSlotType.OFFHAND);
				return new ActionResult<>(ActionResultType.SUCCESS, revolver);
			}
			else if(player.getCooledAttackStrength(1) >= 1)
			{
				if(this.getUpgrades(revolver).getBoolean("nerf"))
					world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1f, 0.6f);
				else
				{
					if(getShootCooldown(revolver) > 0||ItemNBTHelper.hasKey(revolver, "reload"))
						return new ActionResult<>(ActionResultType.PASS, revolver);

					NonNullList<ItemStack> bullets = getBullets(revolver, false);

					if(isEmpty(revolver, false))
						for(int i = 0; i < player.inventory.getSizeInventory(); i++)
						{
							ItemStack stack = player.inventory.getStackInSlot(i);
							if(stack.getItem() instanceof SpeedloaderItem&&!((SpeedloaderItem)stack.getItem()).isEmpty(stack))
							{
								for(ItemStack b : bullets)
									if(!b.isEmpty())
										world.addEntity(new ItemEntity(world, player.posX, player.posY, player.posZ, b));
								setBullets(revolver, ((SpeedloaderItem)stack.getItem()).getContainedItems(stack));
								((SpeedloaderItem)stack.getItem()).setContainedItems(stack, NonNullList.withSize(8, ItemStack.EMPTY));
								player.inventory.markDirty();
								if(player instanceof ServerPlayerEntity)
									ImmersiveEngineering.packetHandler.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)player),
											new MessageSpeedloaderSync(i, hand));

								ItemNBTHelper.putInt(revolver, "reload", 60);
								return new ActionResult<>(ActionResultType.SUCCESS, revolver);
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
								Vec3d vec = player.getLookVec();
								boolean electro = getUpgrades(revolver).getBoolean("electro");
								int count = bullet.getProjectileCount(player);
								if(count==1)
								{
									Entity entBullet = getBullet(player, vec, bullet, electro);
									player.world.addEntity(bullet.getProjectile(player, bullets.get(0), entBullet, electro));
								}
								else
									for(int i = 0; i < count; i++)
									{
										Vec3d vecDir = vec.add(player.getRNG().nextGaussian()*.1, player.getRNG().nextGaussian()*.1, player.getRNG().nextGaussian()*.1);
										Entity entBullet = getBullet(player, vecDir, bullet, electro);
										player.world.addEntity(bullet.getProjectile(player, bullets.get(0), entBullet, electro));
									}
								bullets.set(0, bullet.getCasing(bullets.get(0)).copy());

								float noise = 1;
								Utils.attractEnemies(player, 64*noise);
								SoundEvent sound = bullet.getSound();
								if(sound==null)
									sound = IESounds.revolverFire;
								world.playSound(null, player.posX, player.posY, player.posZ, sound, SoundCategory.PLAYERS, noise, 1f);
							}
							else
								world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_NOTE_BLOCK_HAT, SoundCategory.PLAYERS, 1f, 1f);
						}
						else
							world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_NOTE_BLOCK_HAT, SoundCategory.PLAYERS, 1f, 1f);

						NonNullList<ItemStack> cycled = NonNullList.withSize(getBulletCount(revolver), ItemStack.EMPTY);
						for(int i = 1; i < cycled.size(); i++)
							cycled.set(i-1, bullets.get(i));
						cycled.set(cycled.size()-1, bullets.get(0));
						setBullets(revolver, cycled);
						player.inventory.markDirty();
						ItemNBTHelper.putInt(revolver, "cooldown", getMaxShootCooldown(revolver));
						return new ActionResult<>(ActionResultType.SUCCESS, revolver);
					}
				}
			}
		}
		else if(!player.isSneaking())
		{
			if(getShootCooldown(revolver) > 0||ItemNBTHelper.hasKey(revolver, "reload"))
				return new ActionResult<>(ActionResultType.PASS, revolver);
			NonNullList<ItemStack> bullets = getBullets(revolver);
			if(!bullets.get(0).isEmpty()&&bullets.get(0).getItem() instanceof BulletItem&&ItemNBTHelper.hasKey(bullets.get(0), "bullet"))
			{
				Triple<ItemStack, ShaderRegistryEntry, ShaderCase> shader = ShaderRegistry.getStoredShaderAndCase(revolver);
				if(shader!=null)
				{
					Vec3d pos = Utils.getLivingFrontPos(player, .75, player.getHeight()*.75, hand==Hand.MAIN_HAND?player.getPrimaryHand(): player.getPrimaryHand().opposite(), false, 1);
					shader.getMiddle().getEffectFunction().execute(world, shader.getLeft(), revolver, shader.getRight().getShaderType().toString(), pos, player.getForward(), .125f);
				}
			}
			return new ActionResult<>(ActionResultType.SUCCESS, revolver);
		}
		return new ActionResult<>(ActionResultType.SUCCESS, revolver);
	}

	private RevolvershotEntity getBullet(PlayerEntity player, Vec3d vecDir, IBullet type, boolean electro)
	{
		IELogger.logger.info("Starting with motion vector {}", vecDir);
		RevolvershotEntity bullet = new RevolvershotEntity(player.world, player, vecDir.x*1.5, vecDir.y*1.5, vecDir.z*1.5, type);
		bullet.setMotion(vecDir.scale(2));
		bullet.bulletElectro = electro;
		return bullet;
	}

	public int getShootCooldown(ItemStack stack)
	{
		return ItemNBTHelper.getInt(stack, "cooldown");
	}

	public int getMaxShootCooldown(ItemStack stack)
	{
		return 15;
	}

	public boolean isEmpty(ItemStack stack, boolean allowCasing)
	{
		LazyOptional<IItemHandler> invCap = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		return invCap.map(inv-> {
			for(int i = 0; i < inv.getSlots(); i++)
			{
				ItemStack b = inv.getStackInSlot(i);
				boolean isValid = true;
				if(!allowCasing)
					isValid = b.getItem() instanceof BulletItem&&ItemNBTHelper.hasKey(b, "bullet");
				if(!b.isEmpty()&&isValid)
					return false;
			}
			return true;
		}).orElse(true);
	}

	@Override
	public NonNullList<ItemStack> getBullets(ItemStack revolver, boolean remote)
	{
		if(!remote&&isEmpty(revolver, true))
			remote = true;
		else if(remote&&!ItemNBTHelper.hasKey(revolver, "bullets"))
			remote = false;
		if(!remote)
			return ListUtils.fromItems(this.getContainedItems(revolver).subList(0, getBulletCount(revolver)));
		else
			return Utils.readInventory(revolver.getOrCreateTag().getList("bullets", NBT.TAG_COMPOUND), getBulletCount(revolver));
	}

	public void setBullets(ItemStack revolver, NonNullList<ItemStack> bullets)
	{
		IItemHandlerModifiable inv = (IItemHandlerModifiable)revolver.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
				.orElseThrow(RuntimeException::new);
		for(int i = 0; i < 18; i++)
			inv.setStackInSlot(i, ItemStack.EMPTY);
		for(int i = 0; i < bullets.size(); i++)
			inv.setStackInSlot(i, bullets.get(i));
	}

	@Override
	public int getBulletCount(ItemStack revolver)
	{
		return 8+this.getUpgrades(revolver).getInt("bullets");
	}

	@Override
	public CompoundNBT getUpgradeBase(ItemStack stack)
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

	@OnlyIn(Dist.CLIENT)
	@Override
	public TextureAtlasSprite getTextureReplacement(ItemStack stack, String material)
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
		CompoundNBT upgrades = this.getUpgrades(stack);
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
	public Matrix4 handlePerspective(ItemStack stack, TransformType cameraTransformType, Matrix4 perspective, @Nullable LivingEntity entity)
	{
		if(entity instanceof PlayerEntity&&(cameraTransformType==TransformType.FIRST_PERSON_RIGHT_HAND||cameraTransformType==TransformType.FIRST_PERSON_LEFT_HAND||cameraTransformType==TransformType.THIRD_PERSON_RIGHT_HAND||cameraTransformType==TransformType.THIRD_PERSON_LEFT_HAND))
		{
			boolean main = (cameraTransformType==TransformType.FIRST_PERSON_RIGHT_HAND||cameraTransformType==TransformType.THIRD_PERSON_RIGHT_HAND)==(entity.getPrimaryHand()==HandSide.RIGHT);
			boolean left = cameraTransformType==TransformType.FIRST_PERSON_LEFT_HAND||cameraTransformType==TransformType.THIRD_PERSON_LEFT_HAND;
			if(getUpgrades(stack).getBoolean("fancyAnimation")&&main)
			{
				float f = ((PlayerEntity)entity).getCooledAttackStrength(ClientUtils.mc().getRenderPartialTicks());
				if(f < 1)
				{
					float angle = f*-6.28318f;
					if(left)
						angle *= -1;
					perspective.translate(0, 1.5-f, 0);
					perspective.rotate(angle, 0, 0, 1);
				}
			}

			//Re-grab stack because the other one doesn't do reloads properly
			stack = main?entity.getHeldItemMainhand(): entity.getHeldItemOffhand();
			if(ItemNBTHelper.hasKey(stack, "reload"))
			{
				float f = 3-ItemNBTHelper.getInt(stack, "reload")/20f; //Reload time in seconds, for coordinating with audio
				if(f > .35&&f < 1.95)
					if(f < .5)
						perspective.translate((.35-f)*2, 0, 0).rotate(2.64*(f-.35), 0, 0, left?-1: 1);
					else if(f < .6)
						perspective.translate((f-.5)*6, (.5-f)*1, 0).rotate(.87266, 0, 0, left?-1: 1);
					else if(f < 1.7)
						perspective.translate(0, -.6, 0).rotate(.87266, 0, 0, left?-1: 1);
					else if(f < 1.8)
						perspective.translate((1.8-f)*6, (f-1.8)*1, 0).rotate(.87266, 0, 0, left?-1: 1);
					else
						perspective.translate((f-1.95f)*2, 0, 0).rotate(2.64*(1.95-f), 0, 0, left?-1: 1);
			}
			else if(((PlayerEntity)entity).openContainer instanceof RevolverContainer)
				perspective.translate(left?.4: -.4, .4, 0).rotate(.87266, 0, 0, left?-1: 1);
		}
		return perspective;
	}

	private static final String[][] groups = {{"frame"}, {"cylinder"}};

	@OnlyIn(Dist.CLIENT)
	@Override
	public String[][] getSpecialGroups(ItemStack stack, TransformType transform, LivingEntity entity)
	{
		return groups;
	}

	private static final Matrix4 matOpen = new Matrix4().translate(-.625, .25, 0).rotate(-.87266, 0, 0, 1);
	private static final Matrix4 matClose = new Matrix4().translate(-.625, .25, 0);
	private static final Matrix4 matCylinder = new Matrix4().translate(0, .6875, 0);

	@Nonnull
	@Override
	public Matrix4 getTransformForGroups(ItemStack stack, String[] groups, TransformType transform, LivingEntity entity,
										 Matrix4 mat, float partialTicks)
	{
		if(entity instanceof PlayerEntity&&(transform==TransformType.FIRST_PERSON_RIGHT_HAND||transform==TransformType.FIRST_PERSON_LEFT_HAND||transform==TransformType.THIRD_PERSON_RIGHT_HAND||transform==TransformType.THIRD_PERSON_LEFT_HAND))
		{
			boolean main = (transform==TransformType.FIRST_PERSON_RIGHT_HAND||transform==TransformType.THIRD_PERSON_RIGHT_HAND)==(entity.getPrimaryHand()==HandSide.RIGHT);
			boolean left = transform==TransformType.FIRST_PERSON_LEFT_HAND||transform==TransformType.THIRD_PERSON_LEFT_HAND;
			//Re-grab stack because the other one doesn't do reloads properly
			stack = main?entity.getHeldItemMainhand(): entity.getHeldItemOffhand();
			if(ItemNBTHelper.hasKey(stack, "reload"))
			{
				float f = 3-ItemNBTHelper.getInt(stack, "reload")/20f; //Reload time in seconds, for coordinating with audio
				if("frame".equals(groups[0]))
				{
					if(f < .35||f > 1.95)
						return matClose;
					else if(f < .5)
						return mat.setIdentity().translate(-.625, .25, 0).rotate(-2.64*(f-.35), 0, 0, 1);
					else if(f < 1.8)
						return matOpen;
					else
						return mat.setIdentity().translate(-.625, .25, 0).rotate(-2.64*(1.95-f), 0, 0, 1);
				}
				else if(f > 2.5&&f < 2.9)
					return mat.setIdentity().translate(0, .6875, 0).rotate(-15.70795*(f-2.5), left?-1: 1, 0, 0);
			}
			else if("frame".equals(groups[0])&&((PlayerEntity)entity).openContainer instanceof RevolverContainer)
				return matOpen;
		}
		return "frame".equals(groups[0])?matClose: matCylinder;
	}

	@Override
	public void onCreated(ItemStack stack, World world, PlayerEntity player)
	{
		if(stack.isEmpty()||player==null)
			return;

		String uuid = player.getUniqueID().toString();
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
		this.recalculateUpgrades(stack, world);
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
		CompoundNBT baseUpgrades = new CompoundNBT();
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

	@Override
	public void removeFromWorkbench(PlayerEntity player, ItemStack stack)
	{
		stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
				.ifPresent(inv-> {
					if(!inv.getStackInSlot(18).isEmpty()&&!inv.getStackInSlot(19).isEmpty())
						Utils.unlockIEAdvancement(player, "main/upgrade_revolver");
				});
	}

	@Override
	public boolean canModify(ItemStack stack)
	{
		return true;//TODO
	}

	@Nullable
	@Override
	public CompoundNBT getShareTag(ItemStack stack)
	{
		CompoundNBT ret = super.getShareTag(stack);
		if(ret==null)
			ret = new CompoundNBT();
		else
			ret = ret.copy();
		final CompoundNBT retFinal = ret;
		stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(handler->
		{
			NonNullList<ItemStack> bullets = NonNullList.withSize(getBulletCount(stack), ItemStack.EMPTY);
			for(int i = 0; i < getBulletCount(stack); i++)
				bullets.set(i, handler.getStackInSlot(i));
			retFinal.put("bullets", Utils.writeInventory(bullets));
		});
		return retFinal;
	}

	public static final ArrayListMultimap<String, SpecialRevolver> specialRevolvers = ArrayListMultimap.create();
	public static final Map<String, SpecialRevolver> specialRevolversByTag = new HashMap<String, SpecialRevolver>();

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

	@Override
	public boolean isTool(ItemStack item)
	{
		return true;
	}
}