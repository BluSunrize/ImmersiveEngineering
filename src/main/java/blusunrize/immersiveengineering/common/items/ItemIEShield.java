package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEEnergyItem;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.IEDamageSources.TeslaDamageSource;
import blusunrize.immersiveengineering.common.util.IEPotions;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.block.BlockDispenser;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemIEShield extends ItemUpgradeableTool implements IIEEnergyItem, IOBJModelCallback<ItemStack>
{
	public ItemIEShield()
	{
		super("shield", 1, "SHIELD");
		this.setMaxDamage(1024);
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, ItemArmor.DISPENSER_BEHAVIOR);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		if(this.getMaxEnergyStored(stack)>0)
		{
			String stored = this.getEnergyStored(stack)+"/"+this.getMaxEnergyStored(stack);
			list.add(I18n.format(Lib.DESC+"info.energyStored", stored));
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity ent, int slot, boolean inHand)
	{
		if(!world.isRemote && (!inHand || !(ent instanceof EntityLivingBase) || ((EntityLivingBase)ent).getActiveItemStack()!=stack))//Don't recharge if in use, to avoid flickering
		{
			if(getUpgrades(stack).hasKey("flash_cooldown") && this.extractEnergy(stack, 20, true)==20)
			{
				this.extractEnergy(stack, 20, false);
				int cooldown = getUpgrades(stack).getInteger("flash_cooldown");
				if(--cooldown<=0)
					getUpgrades(stack).removeTag("flash_cooldown");
				else
					getUpgrades(stack).setInteger("flash_cooldown", cooldown);
			}
			if(getUpgrades(stack).hasKey("shock_cooldown") && this.extractEnergy(stack, 20, true)==20)
			{
				this.extractEnergy(stack, 20, false);
				int cooldown = getUpgrades(stack).getInteger("shock_cooldown");
				if(--cooldown<=0)
					getUpgrades(stack).removeTag("shock_cooldown");
				else
					getUpgrades(stack).setInteger("shock_cooldown", cooldown);
			}
		}
	}

	public void damageShield(ItemStack stack, EntityPlayer player, int damage, DamageSource source, float amount, LivingAttackEvent event)
	{
		stack.damageItem(damage, player);
		if(getUpgrades(stack).getBoolean("flash") && getUpgrades(stack).getInteger("flash_cooldown")<=0)
		{
			Vec3d look = player.getLookVec();
			//Offsets Player position by look backwards, then truncates cone at 1
			List<EntityLivingBase> targets = Utils.getTargetsInCone(player.getEntityWorld(), player.getPositionVector().subtract(look), player.getLookVec().scale(9), 1.57079f, .5f);
			for(EntityLivingBase t : targets)
				if(!player.equals(t))
					t.addPotionEffect(new PotionEffect(IEPotions.flashed,100,1));
			getUpgrades(stack).setInteger("flash_cooldown",40);
		}
		if(getUpgrades(stack).getBoolean("shock") && getUpgrades(stack).getInteger("shock_cooldown")<=0)
		{
			boolean b = false;
			if(event.getSource().isProjectile() && event.getSource().getSourceOfDamage()!=null)
			{
				Entity projectile = event.getSource().getSourceOfDamage();
				projectile.setDead();
				event.setCanceled(true);
				b = true;
			}
			if(event.getSource().getEntity()!=null && event.getSource().getEntity() instanceof EntityLivingBase && event.getSource().getEntity().getDistanceSqToEntity(player)<4)
			{
				TeslaDamageSource dmgsrc = IEDamageSources.causeTeslaDamage(1, true);
				dmgsrc.apply(event.getSource().getEntity());
				b = true;
			}
			if(b)
			{
				getUpgrades(stack).setInteger("shock_cooldown",40);
				player.world.playSound(null, player.posX, player.posY, player.posZ, IESounds.spark, SoundCategory.BLOCKS, 2.5F, 0.5F+player.world.rand.nextFloat());
			}
		}
	}

	@Override
	public boolean getIsRepairable(ItemStack stack, ItemStack material)
	{
		return Utils.compareToOreName(material, "ingotSteel");
	}

	@Override
	public int getMaxEnergyStored(ItemStack container)
	{
		return (getUpgrades(container).getBoolean("flash")||getUpgrades(container).getBoolean("shock"))?1600:0;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 72000;
	}
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
	{
		ItemStack itemstack = playerIn.getHeldItem(handIn);
		playerIn.setActiveHand(handIn);
		return new ActionResult(EnumActionResult.SUCCESS, itemstack);
	}
	@Override
	public EnumAction getItemUseAction(ItemStack stack)
	{
		return EnumAction.BLOCK;
	}

	@Override
	public TextureAtlasSprite getTextureReplacement(ItemStack object, String material)
	{
		return null;
	}
	@Override
	public boolean shouldRenderGroup(ItemStack object, String group)
	{
		if("flash".equals(group))
			return getUpgrades(object).getBoolean("flash");
		else if("shock".equals(group))
			return getUpgrades(object).getBoolean("shock");
		return true;
	}
	@Override
	public Matrix4 handlePerspective(ItemStack Object, TransformType cameraTransformType, Matrix4 perspective, EntityLivingBase entity)
	{
		if(entity!=null && entity.isHandActive())
			if((entity.getActiveHand()==EnumHand.MAIN_HAND) == (entity.getPrimaryHand()==EnumHandSide.RIGHT))
			{
				if(cameraTransformType==TransformType.FIRST_PERSON_RIGHT_HAND)
					perspective.rotate(-.15, 1, 0, 0).translate(-.25, .5, -.4375);
				else if(cameraTransformType==TransformType.THIRD_PERSON_RIGHT_HAND)
					perspective.rotate(0.52359, 1, 0, 0).rotate(0.78539, 0, 1, 0).translate(.40625, -.125, -.125);
			}
			else
			{
				if(cameraTransformType==TransformType.FIRST_PERSON_LEFT_HAND)
					perspective.rotate(.15, 1, 0, 0).translate(.25, .375, .4375);
				else if(cameraTransformType==TransformType.THIRD_PERSON_LEFT_HAND)
					perspective.rotate(-0.52359, 1, 0, 0).rotate(0.78539, 0, 1, 0).translate(.1875, .3125, .5625);
			}
		return perspective;
	}

	@Override
	public boolean canModify(ItemStack stack)
	{
		return true;
	}
	@Override
	public Slot[] getWorkbenchSlots(Container container, ItemStack stack, IInventory invItem)
	{
		return new Slot[]
				{
						new IESlot.Upgrades(container, invItem,0, 80,32, "SHIELD", stack, true),
						new IESlot.Upgrades(container, invItem,1,100,32, "SHIELD", stack, true)
//						new IESlot.Upgrades(container, invItem,2,100,32, "SHIELD", stack, true)
				};

	}
	@Override
	public int getInternalSlots(ItemStack stack)
	{
		return 2;
	}
}