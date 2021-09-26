/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.item;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.items.BuzzsawItem;
import blusunrize.immersiveengineering.common.items.SawbladeItem;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class BuzzsawCallbacks implements ItemCallback<BuzzsawCallbacks.Key>
{
	public static final BuzzsawCallbacks INSTANCE = new BuzzsawCallbacks();

	@Override
	public Key extractKey(ItemStack stack, LivingEntity owner)
	{
		CompoundTag upgrades = BuzzsawItem.getUpgradesStatic(stack);
		final boolean hasQuiver = BuzzsawItem.hasQuiverUpgrade(stack);
		final int numBlades = hasQuiver?3: 1;
		List<ResourceLocation> bladeTextures = new ArrayList<>();
		for(int i = 0; i < numBlades; ++i)
		{
			ItemStack sawblade = BuzzsawItem.getSawblade(stack, i);
			if(sawblade.getItem() instanceof SawbladeItem sawbladeItem)
				bladeTextures.add(sawbladeItem.getSawbladeTexture());
			else
				bladeTextures.add(null);
		}
		return new Key(bladeTextures, hasQuiver, upgrades.getBoolean("oiled"), upgrades.getBoolean("launcher"));
	}

	@Override
	public TextureAtlasSprite getTextureReplacement(Key key, String group, String material)
	{
		if("blade".equals(material))
		{
			int spare = "upgrade_blades1".equals(group)?1: "upgrade_blades2".equals(group)?2: 0;
			ResourceLocation rl = key.bladeTexture.get(spare);
			if(rl!=null)
				return ClientUtils.getSprite(rl);
		}
		return null;
	}

	@Override
	public boolean shouldRenderGroup(Key key, String group, RenderType layer)
	{
		if("body".equals(group))
			return true;
		if("blade".equals(group))
			return key.bladeTexture.get(0)!=null;

		if("upgrade_lube".equals(group))
			return key.oiled();
		if("upgrade_launcher".equals(group))
			return key.launcher();
		if("upgrade_blades0".equals(group))
			return key.hasQuiver();
		if("upgrade_blades1".equals(group))
			return key.hasQuiver()&&key.bladeTexture().get(1)!=null;
		if("upgrade_blades2".equals(group))
			return key.hasQuiver()&&key.bladeTexture().get(2)!=null;
		return true;
	}

	private static final String[][] GROUP_BLADE = {{"blade"}};

	@Override
	public String[][] getSpecialGroups(ItemStack stack, TransformType transform, LivingEntity entity)
	{
		return GROUP_BLADE;
	}

	private static final Transformation MAT_FIXED = new Transformation(new Vector3f(0.60945f, 0, 0), null, null, null);

	@Nonnull
	@Override
	public Transformation getTransformForGroups(ItemStack stack, String[] groups, TransformType transform, LivingEntity entity, float partialTicks)
	{
		if(!Tools.BUZZSAW.get().shouldRotate(entity, stack, transform))
			return MAT_FIXED;
		float ticksPerRotation = 10f;
		float angle = (entity.tickCount%ticksPerRotation+partialTicks)/ticksPerRotation*(float)(2*Math.PI);
		return new Transformation(
				new Vector3f(0.60945f, 0, 0),
				new Quaternion(0, angle, 0, false),
				null, null);
	}

	public record Key(List<ResourceLocation> bladeTexture, boolean hasQuiver, boolean oiled, boolean launcher)
	{
	}
}
