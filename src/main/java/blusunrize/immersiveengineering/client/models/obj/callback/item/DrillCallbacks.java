/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.item;

import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.common.items.DrillItem;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class DrillCallbacks implements ItemCallback<DrillCallbacks.Key>
{
	public static final DrillCallbacks INSTANCE = new DrillCallbacks();

	@Override
	public Key extractKey(ItemStack stack, LivingEntity owner)
	{
		CompoundTag upgrades = DrillItem.getUpgradesStatic(stack);
		ItemStack head = DrillItem.getHeadStatic(stack);
		TextureAtlasSprite headTexture;
		if(head.getItem() instanceof IDrillHead headItem)
			headTexture = headItem.getDrillTexture(stack, head);
		else
			headTexture = null;
		return new Key(
				headTexture,
				upgrades.getInt("damage"),
				upgrades.getBoolean("waterproof"),
				upgrades.getBoolean("oiled")
		);
	}


	@Override
	public TextureAtlasSprite getTextureReplacement(Key key, String group, String material)
	{
		if(!"head".equals(material))
			return null;
		else
			return key.headTexture();
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

	private static final String[][] ROTATING = {
			{"drill_head", "upgrade_damage0"},
			{"upgrade_damage1", "upgrade_damage2"},
			{"upgrade_damage3", "upgrade_damage4"}
	};
	private static final String[][] FIXED = {
			{"upgrade_damage1", "upgrade_damage2", "upgrade_damage3", "upgrade_damage4"}
	};

	@Override
	public String[][] getSpecialGroups(ItemStack stack, TransformType transform, LivingEntity entity)
	{
		if(Tools.DRILL.get().shouldRotate(entity, stack, transform))
			return ROTATING;
		else
			return FIXED;
	}

	private static final Transformation MAT_AUGERS = new Transformation(new Vector3f(.441f, 0, 0), null, null, null);

	@Nonnull
	@Override
	public Transformation getTransformForGroups(ItemStack stack, String[] groups, TransformType transform, LivingEntity entity, float partialTicks)
	{
		if(groups==FIXED[0])
			return MAT_AUGERS;
		float angle = (entity.tickCount%60+partialTicks)/60f*(float)(2*Math.PI);
		Quaternion rotation = null;
		Vector3f translation = null;
		if("drill_head".equals(groups[0]))
			rotation = new Quaternion(angle, 0, 0, false);
		else if("upgrade_damage1".equals(groups[0]))
		{
			translation = new Vector3f(.441f, 0, 0);
			rotation = new Quaternion(0, angle, 0, false);
		}
		else if("upgrade_damage3".equals(groups[0]))
		{
			translation = new Vector3f(.441f, 0, 0);
			rotation = new Quaternion(0, 0, angle, false);
		}
		return new Transformation(translation, rotation, null, null);
	}

	public record Key(TextureAtlasSprite headTexture, int damage, boolean waterproof, boolean oiled)
	{
	}
}
