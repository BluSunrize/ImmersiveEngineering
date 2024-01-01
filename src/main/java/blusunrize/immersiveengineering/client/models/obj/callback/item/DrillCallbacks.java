/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.item;

import blusunrize.immersiveengineering.api.client.ieobj.ItemCallback;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.obj.callback.item.DrillCallbacks.Key;
import blusunrize.immersiveengineering.common.items.DieselToolItem;
import blusunrize.immersiveengineering.common.items.DrillItem;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class DrillCallbacks implements ItemCallback<Key>
{
	public static final DrillCallbacks INSTANCE = new DrillCallbacks();

	@Override
	public Key extractKey(ItemStack stack, LivingEntity owner)
	{
		CompoundTag upgrades = DrillItem.getUpgradesStatic(stack);
		ItemStack head = DrillItem.getHeadStatic(stack);
		ResourceLocation headTexture;
		if(head.getItem() instanceof IDrillHead headItem)
			headTexture = headItem.getDrillTexture(stack, head);
		else
			headTexture = null;
		return new Key(
				headTexture,
				upgrades.getInt("damage"),
				upgrades.getBoolean("waterproof"),
				upgrades.getBoolean("oiled"),
				upgrades.getBoolean("fortune")
		);
	}


	@Override
	public TextureAtlasSprite getTextureReplacement(Key key, String group, String material)
	{
		if(!"head".equals(material))
			return null;
		else
			return ClientUtils.getSprite(key.headTexture());
	}

	@Override
	public boolean shouldRenderGroup(Key key, String group, RenderType layer)
	{
		if(group.equals("drill_frame")||group.equals("drill_grip"))
			return true;
		if(group.equals("upgrade_waterproof"))
			return key.waterproof();
		if(group.equals("upgrade_speed"))
			return key.oiled();
		if(group.equals("upgrade_fortune"))
			return key.fortune();
		if(key.headTexture()!=null)
		{
			if(group.equals("drill_head"))
				return true;

			if(group.equals("upgrade_damage0"))
				return key.damage() > 0;
			if(group.equals("upgrade_damage1")||group.equals("upgrade_damage2"))
				return key.damage() > 1;
			if(group.equals("upgrade_damage3")||group.equals("upgrade_damage4"))
				return key.damage() > 2;
		}
		return false;
	}

	@Override
	public Transformation applyTransformations(Key key, String group, Transformation transform)
	{
		if(group.equals("drill_head")&&key.damage() <= 0)
			return transform.compose(new Transformation(new Vector3f(-.25f, 0, 0), null, null, null));
		return transform;
	}

	private static final List<List<String>> ROTATING = List.of(
			List.of("drill_head", "upgrade_damage0"),
			List.of("upgrade_damage1", "upgrade_damage2"),
			List.of("upgrade_damage3", "upgrade_damage4")
	);
	private static final List<List<String>> FIXED = List.of(List.of(
			"upgrade_damage1", "upgrade_damage2", "upgrade_damage3", "upgrade_damage4"
	));

	@Override
	public List<List<String>> getSpecialGroups(ItemStack stack, ItemDisplayContext transform, LivingEntity entity)
	{
		if(shouldRotate(Tools.DRILL, entity, stack, transform))
			return ROTATING;
		else
			return FIXED;
	}

	private static final Transformation MAT_AUGERS = new Transformation(new Vector3f(.441f, 0, 0), null, null, null);

	@Nonnull
	@Override
	public Transformation getTransformForGroups(ItemStack stack, List<String> groups, ItemDisplayContext transform, LivingEntity entity, float partialTicks)
	{
		if(groups==FIXED.get(0))
			return MAT_AUGERS;
		float angle = (entity.tickCount%60+partialTicks)/60f*(float)(2*Math.PI);
		Quaternionf rotation = null;
		Vector3f translation = null;
		if("drill_head".equals(groups.get(0)))
			rotation = new Quaternionf().rotateXYZ(angle, 0, 0);
		else if("upgrade_damage1".equals(groups.get(0)))
		{
			translation = new Vector3f(.46875f, 0, 0);
			rotation = new Quaternionf().rotateXYZ(0, angle, 0);
		}
		else if("upgrade_damage3".equals(groups.get(0)))
		{
			translation = new Vector3f(.46875f, 0, 0);
			rotation = new Quaternionf().rotateXYZ(0, 0, angle);
		}
		return new Transformation(translation, rotation, null, null);
	}

	public static boolean shouldRotate(
			Supplier<? extends DieselToolItem> item, LivingEntity entity, ItemStack stack, ItemDisplayContext transform
	)
	{
		return entity!=null&&item.get().canToolBeUsed(stack)&&
				(entity.getItemInHand(InteractionHand.MAIN_HAND)==stack||entity.getItemInHand(InteractionHand.OFF_HAND)==stack)&&
				(transform==ItemDisplayContext.FIRST_PERSON_RIGHT_HAND||transform==ItemDisplayContext.FIRST_PERSON_LEFT_HAND||
						transform==ItemDisplayContext.THIRD_PERSON_RIGHT_HAND||transform==ItemDisplayContext.THIRD_PERSON_LEFT_HAND);
	}

	@Override
	public Key getDefaultKey()
	{
		return new Key(null, 0, false, false, false);
	}

	public record Key(
			@Nullable ResourceLocation headTexture, int damage, boolean waterproof, boolean oiled, boolean fortune
	)
	{
	}
}
